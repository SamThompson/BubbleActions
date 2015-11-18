package com.sam.bubbleactions;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sam on 11/2/15.
 */
class BubbleView extends LinearLayout {

    // In order to prevent clipping, the bubble starts out smaller than the space it's given
    private static final float DESELECTED_SCALE = 0.85f;

    private static final float SELECTED_SCALE = 1f;

    private static final int ANIMATION_DURATION = 150;

    private static final float OVERSHOOT_TENSION = 1.5f;

    private boolean animatedIn = false;
    private OvershootInterpolator overshootInterpolator;
    BubbleActions.Callback callback;
    TextView textView;
    ImageView imageView;

    public BubbleView(Context context) {
        super(context);

        overshootInterpolator = new OvershootInterpolator(OVERSHOOT_TENSION);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.bubble_actions_bubble_item, this, true);
        textView = (TextView) getChildAt(0);
        imageView = (ImageView) getChildAt(1);
        imageView.setOnDragListener(dragListener);
        imageView.setScaleX(DESELECTED_SCALE);
        imageView.setScaleY(DESELECTED_SCALE);
    }

    void animateIn(float endX, float endY) {
        setVisibility(VISIBLE);
        ViewCompat.animate(this)
                .translationX(endX)
                .translationY(endY)
                .alpha(1f)
                .setInterpolator(overshootInterpolator)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);
                        animatedIn = true;
                    }
                })
                .setDuration(ANIMATION_DURATION);
    }

    void animateOut(float startX, float startY) {
        ViewCompat.animate(this)
                .translationX(startX)
                .translationY(startY)
                .alpha(0f)
                .setInterpolator(null)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);
                        setVisibility(INVISIBLE);
                        imageView.setScaleX(DESELECTED_SCALE);
                        imageView.setScaleY(DESELECTED_SCALE);
                        imageView.setSelected(false);
                        textView.setVisibility(INVISIBLE);
                        animatedIn = false;
                    }
                })
                .setDuration(ANIMATION_DURATION);
    }

    /**
     * OnDragListener for the ImageView. The correct behavior is only to register a drag enter only
     * if we enter the ImageView (otherwise it would still register a drag enter if we touch the
     * TextView).
     */
    OnDragListener dragListener = new OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            // Gotcha: you need to return true for drag end and start
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    if (animatedIn) {
                        imageView.setSelected(true);
                        ViewCompat.animate(imageView)
                                .scaleX(SELECTED_SCALE)
                                .scaleY(SELECTED_SCALE)
                                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(View view) {
                                        super.onAnimationStart(view);
                                        textView.setVisibility(VISIBLE);
                                        ViewCompat.animate(textView)
                                                .alpha(1f)
                                                .setListener(null)
                                                .setDuration(ANIMATION_DURATION);
                                    }
                                })
                                .setDuration(ANIMATION_DURATION);
                    }

                    return animatedIn;
                case DragEvent.ACTION_DRAG_EXITED:
                    if (animatedIn) {
                        imageView.setSelected(false);
                        ViewCompat.animate(imageView)
                                .scaleX(DESELECTED_SCALE)
                                .scaleY(DESELECTED_SCALE)
                                .setDuration(ANIMATION_DURATION)
                                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(View view) {
                                        super.onAnimationStart(view);
                                        ViewCompat.animate(textView)
                                                .alpha(0f)
                                                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(View view) {
                                                        super.onAnimationEnd(view);
                                                        textView.setVisibility(INVISIBLE);
                                                    }
                                                })
                                                .setDuration(ANIMATION_DURATION);
                                    }
                                });
                    }

                    return animatedIn;
                case DragEvent.ACTION_DROP:
                    callback.doAction();
                    return true;
            }
            return false;
        }
    };

}
