package com.axa.hue_seek_bar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements HueSeekBar.OnHueSeekBarListener {

    private HueSeekBar hueSeekBar;
    private TextView textAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hueSeekBar = (HueSeekBar) findViewById(R.id.hueSeekBar);
        textAngle = (TextView) findViewById(R.id.textAngle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hueSeekBar.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hueSeekBar.unregister();
    }

    @Override
    public void onTouchMove(double angle, int color) {
        textAngle.setText(String.valueOf(angle));
        textAngle.setTextColor(color);
    }

    @Override
    public void onTouchStart(double angle, int color) {
        textAngle.setText(String.valueOf(angle));
        textAngle.setTextColor(color);
    }

    @Override
    public void onTouchEnd(double angle, int color) {
        textAngle.setText(String.valueOf(angle));
        textAngle.setTextColor(color);
    }
}
