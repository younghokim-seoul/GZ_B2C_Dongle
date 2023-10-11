package com.hoho.android.usbserial.examples.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PerfectScrollableTextView extends androidx.appcompat.widget.AppCompatTextView {
    public PerfectScrollableTextView(@NonNull Context context) {
        super(context);
        setVerticalScrollBarEnabled(true);
        setHorizontallyScrolling(false);
    }

    public PerfectScrollableTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(true);
        setHorizontallyScrolling(false);
    }

    public PerfectScrollableTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVerticalScrollBarEnabled(true);
        setHorizontallyScrolling(false);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) super.onFocusChanged(true, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused) super.onWindowFocusChanged(focused);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
