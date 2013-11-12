package com.sqisland.android.fraction_view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.sqisland.android.fraction_view.FractionView.OnChangeListener;

public class MainActivity extends Activity implements OnChangeListener {
    private FractionView mFractionView;
    private TextView fractionText;
    private Handler handler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFractionView = (FractionView)findViewById(R.id.fraction);
        fractionText = (TextView)findViewById(R.id.fractionText);
        mFractionView.setOnChangeListener(this);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFractionView.increment();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
        mFractionView.setFraction(1, 10);
    }

    @Override
    public void onChange(final int numerator, final int denominator) {
        fractionText.setText(String.format("%s/%s", numerator, denominator));
    }
}
