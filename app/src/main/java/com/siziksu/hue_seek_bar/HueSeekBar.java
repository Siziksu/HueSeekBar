package com.siziksu.hue_seek_bar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class HueSeekBar extends View {

    private static final int DEFAULT_COLOR = 0xAA000000;
    private static final int SELECTOR_COLOR = 0xFFFFFFFF;
    private static final int PADDING = 0;
    private static final int STARTING_DEGREE = 270;
    private static final int STARTING_ANGLE = -90;
    private static final float SELECTOR_RADIUS_RATIO = 1.5f;
    private static final float COLOR_INCREMENT = 4.25f;
    private static final float SELECTOR_MARGIN = 25;
    private static final int HEX_FF = 255;

    private static final int STYLE_INSET = 0;
    private static final int STYLE_CENTERED = 1;
    private static final int STYLE_OUTSET = 2;

    private static final float DEGREE_0 = 0;
    private static final float DEGREE_60 = 60;
    private static final float DEGREE_90 = 90;
    private static final float DEGREE_120 = 120;
    private static final float DEGREE_180 = 180;
    private static final float DEGREE_240 = 240;
    private static final float DEGREE_270 = -90;
    private static final float DEGREE_300 = 300;
    private static final float DEGREE_360 = 360;

    private static final int sizeOfIntInHalfBytes = 2;
    private static final int numberOfBitsInAHalfByte = 4;
    private static final int halfByte = 0x0F;
    private static final char[] hexDigits = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private OnHueSeekBarListener listener;

    private int layoutWidth;
    private int layoutHeight;

    private final Paint hueSeekBarPaint = new Paint();
    private final Paint selectorPaint = new Paint();

    private int[] gradientColors;
    private float[] gradientPositions;

    private RectF hueSeekBarBounds = new RectF();
    private int hueSeekBarWidth = 24;
    private int hueSeekBarStyle = 1;

    private float hueSeekBarCenterX;
    private float hueSeekBarCenterY;

    private float selectorRadius;
    private float selectorCenterX;
    private float selectorCenterY;

    private float angle = -1.5707774f;

    private boolean canMove;
    private int radius;

    /**
     * Class constructor.
     *
     * @param context the context
     */
    public HueSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * Class constructor.
     *
     * @param context the context
     * @param attrs   the attributes
     */
    public HueSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Class constructor.
     *
     * @param context      the context
     * @param attrs        the attributes
     * @param defStyleAttr the style attributes
     */
    public HueSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Class constructor.
     *
     * @param context      the context
     * @param attrs        the attributes
     * @param defStyleAttr the style attributes
     * @param defStyleRes  the style resources
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HueSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.HueSeekBar));
        }
    }

    private void parseAttributes(TypedArray attributes) {
        hueSeekBarWidth = attributes.getInteger(R.styleable.HueSeekBar_hueSeekBarWidth, hueSeekBarWidth);
        hueSeekBarStyle = attributes.getInteger(R.styleable.HueSeekBar_hueSeekBarStyle, hueSeekBarStyle);
        attributes.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutWidth = w;
        layoutHeight = h;
        setupBounds();
        setupGradientColors();
        setupPaints();
        invalidate();
    }

    private void setupBounds() {
        // HueSeekBar
        int hueSeekBarHalfSize = hueSeekBarWidth / 2;
        int left = hueSeekBarHalfSize - layoutWidth;
        int top = hueSeekBarHalfSize + PADDING;
        int right = layoutWidth - hueSeekBarHalfSize - PADDING;
        int bottom = layoutHeight - hueSeekBarHalfSize - PADDING;
        hueSeekBarBounds = new RectF(left, top, right, bottom);
        hueSeekBarCenterX = 0;
        hueSeekBarCenterY = layoutHeight / 2;
        // Selector
        int diameter = Math.max(layoutWidth, layoutHeight);
        int style = setStyle(hueSeekBarHalfSize);
        radius = diameter / 2 + style;
        selectorRadius = hueSeekBarWidth * SELECTOR_RADIUS_RATIO;
        selectorCenterX = hueSeekBarBounds.centerX() + (radius * (float) Math.cos(Math.toRadians(STARTING_ANGLE)));
        selectorCenterY = hueSeekBarBounds.centerY() + (radius * (float) Math.sin(Math.toRadians(STARTING_ANGLE)));
    }

    private int setStyle(int hueSeekBarHalfSize) {
        if (hueSeekBarStyle == STYLE_INSET) {
            return -hueSeekBarHalfSize;
        }
        if (hueSeekBarStyle == STYLE_OUTSET) {
            return hueSeekBarHalfSize;
        }
        return 0;
    }

    private void setupPaints() {
        setSeekBarPaint();
        setSelectorPaint();
    }

    private void setupGradientColors() {
        setHueColors();
    }

    @NonNull
    private SweepGradient getSweepGradient() {
        return new SweepGradient(0, layoutHeight / 2, gradientColors, gradientPositions);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(hueSeekBarCenterX, hueSeekBarCenterY, radius, hueSeekBarPaint);
        canvas.drawCircle(selectorCenterX, selectorCenterY, selectorRadius, selectorPaint);
    }

    private void setSeekBarPaint() {
        hueSeekBarPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        hueSeekBarPaint.setAntiAlias(true);
        hueSeekBarPaint.setColor(DEFAULT_COLOR);
        hueSeekBarPaint.setStyle(Paint.Style.STROKE);
        hueSeekBarPaint.setStrokeWidth(hueSeekBarWidth);
        Shader shader = getSweepGradient();
        Matrix matrix = new Matrix();
        matrix.preRotate(STARTING_DEGREE, hueSeekBarBounds.centerX(), hueSeekBarBounds.centerY());
        shader.setLocalMatrix(matrix);
        hueSeekBarPaint.setShader(shader);
    }

    private void setSelectorPaint() {
        selectorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setAntiAlias(true);
        selectorPaint.setColor(SELECTOR_COLOR);
    }

    private void setHueColors() {
        gradientColors = new int[]{
                0xffff0000, // red 0º
                0xffff7d00, // orange 30º
                0xffffff00, // yellow 60º
                0xff7dff00, // spring green 90º
                0xff00ff00, // green 120º
                0xff00ff7d, // turquoise 150º
                0xff00ffff, // cyan 180º
                0xff007dff, // ocean 210º
                0xff0000ff, // blue 240º
                0xff7d00ff, // violet 270º
                0xffff00ff, // magenta 300º
                0xffff007d, // raspberry 330º
                0xffff0000, // red 360º
                0x00000000, // transparent
                0x00000000, // transparent
        };
        float multiplier = 0.5f;
        float increment = 0.084f;
        gradientPositions = new float[]{
                0,
                (multiplier * increment),
                (multiplier * increment * 2),
                (multiplier * increment * 3),
                (multiplier * increment * 4),
                (multiplier * increment * 5),
                (multiplier * increment * 6),
                (multiplier * increment * 7),
                (multiplier * increment * 8),
                (multiplier * increment * 9),
                (multiplier * increment * 10),
                (multiplier * increment * 11),
                (multiplier * increment * 12),
                0.5f,
                1,
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float selectorBox = selectorRadius + SELECTOR_MARGIN;
                if (!canMove && x > selectorCenterX - selectorBox && x < selectorCenterX + selectorBox &&
                    y > selectorCenterY - selectorBox && y < selectorCenterY + selectorBox) {
                    canMove = true;
                    if (listener != null) {
                        listener.onTouchStart(getRealAngle(), getColor());
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (canMove) {
                    float dx = event.getX() - hueSeekBarCenterX;
                    float dy = event.getY() - hueSeekBarCenterY;
                    angle = (float) Math.atan2(dy, dx);
                    if (Math.toDegrees(angle) >= DEGREE_270 && Math.toDegrees(angle) <= DEGREE_90) {
                        float cos = (float) Math.cos(angle);
                        float sin = (float) Math.sin(angle);
                        selectorCenterX = hueSeekBarBounds.centerX() + radius * cos;
                        selectorCenterY = hueSeekBarBounds.centerY() + radius * sin;
                        if (listener != null) {
                            listener.onTouchMove(getRealAngle(), getColor());
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                canMove = false;
                if (listener != null) {
                    listener.onTouchEnd(getRealAngle(), getColor());
                }
                break;
            default:
                break;
        }
        if (canMove) {
            invalidate();
        }
        return true;
    }

    private int getColor() {
        double realAngle = getRealAngle();
        int decimal;
        int color = Color.parseColor("#FF000000");
        if (realAngle >= DEGREE_0 && realAngle < DEGREE_60) {
            decimal = (int) (realAngle * COLOR_INCREMENT);
            color = Color.parseColor("#FF" + "FF" + decToHex(decimal) + "00"); // red
        }
        if (realAngle >= DEGREE_60 && realAngle < DEGREE_120) {
            decimal = (int) (HEX_FF - (realAngle - DEGREE_60) * COLOR_INCREMENT);
            color = Color.parseColor("#FF" + decToHex(decimal) + "FF" + "00"); // yellow
        }
        if (realAngle >= DEGREE_120 && realAngle < DEGREE_180) {
            decimal = (int) ((realAngle - DEGREE_120) * COLOR_INCREMENT);
            color = Color.parseColor("#FF" + "00" + "FF" + decToHex(decimal)); // green
        }
        if (realAngle >= DEGREE_180 && realAngle < DEGREE_240) {
            decimal = (int) (HEX_FF - (realAngle - DEGREE_180) * COLOR_INCREMENT);
            color = Color.parseColor("#FF" + "00" + decToHex(decimal) + "FF"); // cyan
        }
        if (realAngle >= DEGREE_240 && realAngle < DEGREE_300) {
            decimal = (int) ((realAngle - DEGREE_240) * COLOR_INCREMENT);
            color = Color.parseColor("#FF" + decToHex(decimal) + "00" + "FF"); // blue
        }
        if (realAngle >= DEGREE_300 && realAngle <= DEGREE_360) {
            decimal = (int) (HEX_FF - (realAngle - DEGREE_300) * COLOR_INCREMENT);
            color = Color.parseColor("#FF" + "FF" + "00" + decToHex(decimal)); // magenta
        }
        return color;
    }

    private double getRealAngle() {
        return Math.round((Math.toDegrees(angle) + DEGREE_90) * 2);
    }

    private String decToHex(int dec) {
        StringBuilder hexBuilder = new StringBuilder(sizeOfIntInHalfBytes);
        hexBuilder.setLength(sizeOfIntInHalfBytes);
        for (int i = sizeOfIntInHalfBytes - 1; i >= 0; --i) {
            int j = dec & halfByte;
            hexBuilder.setCharAt(i, hexDigits[j]);
            dec >>= numberOfBitsInAHalfByte;
        }
        return hexBuilder.toString();
    }

    /**
     * Registers the listener for the touch event of this control.
     *
     * @param listener the listener
     */
    public void register(OnHueSeekBarListener listener) {
        this.listener = listener;
    }

    /**
     * Unregisters the listener for the touch event of this control.
     */
    public void unregister() {
        this.listener = null;
    }

    /**
     * Interface definition for a callback to be invoked when this control has touch events.
     */
    public interface OnHueSeekBarListener {

        /**
         * Fired when the selector is being moved.
         *
         * @param angle the angle at that position
         * @param color the color selected at that point
         */
        void onTouchMove(double angle, int color);

        /**
         * Fired when the touch starts.
         *
         * @param angle the angle at that point
         * @param color the color selected at that point
         */
        void onTouchStart(double angle, int color);

        /**
         * Fired when the touch ends.
         *
         * @param angle the angle at that point
         * @param color the color selected at that point
         */
        void onTouchEnd(double angle, int color);
    }
}
