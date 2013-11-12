package com.sqisland.android.fraction_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FractionView extends View {
    private static final float ANIMATION_DURATION = 1000f;
    private Paint mSectorPaint;
    private final RectF mSectorOval = new RectF();
    private Paint mCirclePaint;

    private int mNumerator = 1;
    private int mDenominator = 10;

    private OnChangeListener mListener = null;
    private boolean animating = false;
    private long startTimeMillis;

    public FractionView(final Context context) {
        super(context);
        init();
    }

    public FractionView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FractionView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public interface OnChangeListener {
        public void onChange(int numerator, int denominator);
    }

    public void setOnChangeListener(final OnChangeListener listener) {
        mListener = listener;
    }

    private void init() {
        setBackgroundColor(Color.LTGRAY);
        mSectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectorPaint.setColor(Color.BLUE);
        mSectorPaint.setStyle(Paint.Style.FILL);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.CYAN);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    private float getSweepAngle() {
        return mNumerator * 360f / mDenominator;
    }

    public void setFraction(final int numerator, final int denominator) {
        if (numerator < 0)
            return;
        if (denominator <= 0)
            return;
        if (numerator > denominator)
            return;

        mNumerator = numerator;
        mDenominator = denominator;
        animating = true;
        startTimeMillis = SystemClock.uptimeMillis();
        invalidate();

        if (mListener != null)
            mListener.onChange(numerator, denominator);
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle)state;
            mNumerator = bundle.getInt("numerator");
            mDenominator = bundle.getInt("denominator");
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
        } else {
            super.onRestoreInstanceState(state);
        }
        setFraction(mNumerator, mDenominator);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("numerator", mNumerator);
        bundle.putInt("denominator", mDenominator);
        return bundle;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP)
            return true;

        final float x = event.getX(), y = event.getY();

        if (Math.abs(getCenterX() - x) > getRadius() || Math.abs(getCenterY() - y) > getRadius())
            return true;

        increment();

        return true;
    }

    public void increment() {
        // Increment the numerator, cycling back to 0 when we have filled the
        // whole circle.
        int numerator = mNumerator + 1;
        if (numerator > mDenominator)
            numerator = 1;

        setFraction(numerator, mDenominator);
    }

    private int getAvailableWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getAvailableHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getCenterX() {
        return getAvailableWidth() / 2 + getPaddingLeft();
    }

    private int getCenterY() {
        return getAvailableHeight() / 2 + getPaddingTop();
    }

    private int getRadius() {

        return getDiameter() / 2;
    }

    private int getDiameter() {
        return Math.min(getAvailableWidth(), getAvailableHeight()) / 2;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), getRadius(), mCirclePaint);

        mSectorOval.top = (getAvailableHeight() - getDiameter()) / 2 + getPaddingTop();
        mSectorOval.left = (getAvailableWidth() - getDiameter()) / 2 + getPaddingLeft();
        mSectorOval.bottom = mSectorOval.top + getDiameter();
        mSectorOval.right = mSectorOval.left + getDiameter();

        if (animating) {
            final float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / ANIMATION_DURATION;

            if (normalized >= 1f) {
                animating = false;
                canvas.drawArc(mSectorOval, 0, getSweepAngle(), true, mSectorPaint);
            } else {
                final float oldAngle = (mNumerator - 1) * 360f / mDenominator;
                final float deltaAngle = getSweepAngle() - oldAngle;
                final float offset = deltaAngle * normalized;

                canvas.drawArc(mSectorOval, 0, oldAngle + offset, true, mSectorPaint);
                invalidate();
            }
        } else {
            canvas.drawArc(mSectorOval, 0, getSweepAngle(), true, mSectorPaint);
        }
    }
}
