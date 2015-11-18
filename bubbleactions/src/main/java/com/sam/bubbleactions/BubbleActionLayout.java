package com.sam.bubbleactions;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * Created by sam on 10/30/15.
 */
public class BubbleActionLayout extends FrameLayout {

    private BubbleActionOverlay bubbleActionOverlay;
    private float lastDownX;
    private float lastDownY;

    public BubbleActionLayout(Context context) {
        this(context, null, 0);
    }

    public BubbleActionLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleActionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BubbleActionLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        bubbleActionOverlay = new BubbleActionOverlay(getContext());
    }

    /**
     * Add a child view to the {@link BubbleActionLayout} over which the bubble actions will be
     * drawn.
     *
     * Currently only one child is supported.
     *
     * @param child
     */
    @Override
    public void addView(View child) {
        if (getChildCount() > 0) {
            throw new IllegalStateException(this.getClass().toString() + ": cannot have additional children.");
        }

        super.addView(child, -1, generateDefaultLayoutParams());
        super.addView(bubbleActionOverlay, -1, generateDefaultLayoutParams());
    }

    @Override
    public void addView(View child, int index) {
        addView(child);
    }

    @Override
    public void addView(View child, int width, int height) {
        addView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        addView(child);
    }

    /**
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_MOVE) {
            lastDownX = getLeft() + ev.getX();
            lastDownY = getTop() + ev.getY();
        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * @param bubbleActions
     */
    void showOverlay(BubbleActions bubbleActions) {
        bubbleActionOverlay.showOverlay(lastDownX, lastDownY, bubbleActions);
    }

    /**
     * Set the typeface for the labels that appear above the bubbles
     * @param typeface
     */
    public void setLabelTypeface(Typeface typeface) {
        bubbleActionOverlay.setLabelTypeface(typeface);
    }

}
