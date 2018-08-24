package com.wkl.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wkl.widget.bundle.cdv.CountDownView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoEqualizer(View view) {
        startActivity(new Intent(this, EqualizerAcrtivity.class));
    }

    public void startCountDown(View view) {
        CountDownView cdv = (CountDownView) view;
        cdv.startCountDown();
    }
}
