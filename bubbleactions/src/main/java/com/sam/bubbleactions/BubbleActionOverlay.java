package com.sam.bubbleactions;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 *
 */
class BubbleActionOverlay extends FrameLayout {

    /**
     * Ripped straight from v21 AOSP. No idea why this is v21+
     */
    static class BackgroundAlphaTypeEvaluator implements TypeEvaluator<Integer> {

        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            int startInt = startValue;
            int startA = (startInt >> 24) & 0xff;
            int startR = (startInt >> 16) & 0xff;
            int startG = (startInt >> 8) & 0xff;
            int startB = startInt & 0xff;

            int endInt = endValue;
            int endA = (endInt >> 24) & 0xff;
            int endR = (endInt >> 16) & 0xff;
            int endG = (endInt >> 8) & 0xff;
            int endB = endInt & 0xff;

            return ((startA + (int)(fraction * (endA - startA))) << 24) |
                    ((startR + (int)(fraction * (endR - startR))) << 16) |
                    ((startG + (int)(fraction * (endG - startG))) << 8) |
                    ((startB + (int)(fraction * (endB - startB))));
        }
    }

    static final int MAX_ACTIONS = 5;

    private static final String TAG = BubbleActionOverlay.class.getSimpleName();

    private static final int ANIMATION_DURATION = 150;

    private float[] actionStartX = new float[MAX_ACTIONS];
    private float[] actionStartY = new float[MAX_ACTIONS];
    private float[] actionEndX = new float[MAX_ACTIONS];
    private float[] actionEndY = new float[MAX_ACTIONS];
    private ClipData dragData;
    private DragShadowBuilder dragShadowBuilder;
    private float startActionDistanceFromCenter;
    private float stopActionDistanceFromCenter;
    private float bubbleDimension;
    private RectF contentClipRect;
    private View bubbleActionIndicator;
    private int numActions = 0;
    private boolean overlayActive = false;
    private ObjectAnimator backgroundAnimator;

    BubbleActionOverlay(Context context) {
        super(context);
        contentClipRect = new RectF();
        dragShadowBuilder = new DragShadowBuilder();
        dragData = new ClipData(TAG, new String[] {TAG}, new ClipData.Item(TAG));

        LayoutInflater inflater = LayoutInflater.from(context);
        bubbleActionIndicator = inflater.inflate(R.layout.bubble_actions_indicator, this, false);
        bubbleActionIndicator.setVisibility(INVISIBLE);
        bubbleActionIndicator.setAlpha(0f);
        addView(bubbleActionIndicator, -1);

        Resources resources = getResources();

        int transparentBackgroundColor = resources.getColor(R.color.bubble_actions_background_transparent);
        int darkenedBackgroundColor = resources.getColor(R.color.bubble_actions_background_darkened);
        setBackgroundColor(transparentBackgroundColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            backgroundAnimator = ObjectAnimator.ofArgb(this, "backgroundColor", transparentBackgroundColor, darkenedBackgroundColor);
        } else {
            backgroundAnimator = ObjectAnimator.ofObject(this, "backgroundColor", new BackgroundAlphaTypeEvaluator(), transparentBackgroundColor, darkenedBackgroundColor);
        }

        backgroundAnimator.setDuration(ANIMATION_DURATION);

        bubbleDimension = (int) getResources().getDimension(R.dimen.bubble_actions_indicator_dimension);
        startActionDistanceFromCenter = getResources().getDimension(R.dimen.bubble_actions_start_distance);
        stopActionDistanceFromCenter = getResources().getDimension(R.dimen.bubble_actions_stop_distance);

        for (int i = 0; i < MAX_ACTIONS; i++) {
            BubbleView itemView = new BubbleView(getContext());
            itemView.setVisibility(INVISIBLE);
            itemView.setAlpha(0f);
            addView(itemView, -1, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    void setLabelTypeface(Typeface typeface) {
        for (int i = 1; i <= MAX_ACTIONS; i++) {
            BubbleView itemView = (BubbleView) getChildAt(i);
            itemView.textView.setTypeface(typeface);
        }
    }

    /**
     * @param bubbleActions
     */
    void setBubbleActions(BubbleActions bubbleActions) {
        numActions = bubbleActions.numActions;
        if (numActions > MAX_ACTIONS) {
            throw new IllegalArgumentException(TAG + ": actions cannot have more than " + MAX_ACTIONS + " actions. ");
        }

        if (bubbleActions.indicator != null) {
            bubbleActionIndicator.setBackground(bubbleActions.indicator);
        } else {
            bubbleActionIndicator.setBackgroundResource(R.drawable.bubble_actions_indicator);
        }

        int children = getChildCount();
        for (int i = 1; i < children; i++) {
            BubbleView itemView = (BubbleView) getChildAt(i);
            if (i - 1 < numActions) {
                BubbleActions.Action action = bubbleActions.actions[i - 1];
                itemView.textView.setText(action.actionName);
                itemView.imageView.setImageDrawable(action.foregroundDrawable);
                itemView.imageView.setBackground(action.backgroundDrawable);
                itemView.callback = action.callback;
            } else {
                itemView.setVisibility(INVISIBLE);
            }
        }
    }

    void showOverlay(float originX, float originY, BubbleActions bubbleActions) {
        numActions = bubbleActions.numActions;
        if (numActions > MAX_ACTIONS) {
            throw new IllegalArgumentException(TAG + ": actions cannot have more than " + MAX_ACTIONS + " actions. ");
        }

        if (bubbleActions.indicator != null) {
            bubbleActionIndicator.setBackground(bubbleActions.indicator);
        } else {
            bubbleActionIndicator.setBackgroundResource(R.drawable.bubble_actions_indicator);
        }

        contentClipRect.set(0, 0, getWidth(), getHeight());
        bubbleActionIndicator.setX(originX - (bubbleActionIndicator.getWidth() / 2.0f));
        bubbleActionIndicator.setY(originY - (bubbleActionIndicator.getHeight() / 2.0f));

        // check if we're too short on any of the sides
        double angleDelta = Math.PI / (numActions + 1);
        float requiredSpace = (float) Math.cos(angleDelta) * (stopActionDistanceFromCenter + bubbleDimension);
        boolean leftOk = contentClipRect.contains(originX - requiredSpace, originY);
        boolean rightOk = contentClipRect.contains(originX + requiredSpace, originY);

        // if this statement is true then we don't have enough space on the sides
        if (!leftOk && !rightOk) {
            throw new IllegalStateException(BubbleActionOverlay.class.toString() + ": view has no space to expand actions.");
        }

        // Kind of tricky logic
        double startingAngle;
        if (rightOk && leftOk) {
            startingAngle = Math.PI + angleDelta;
        } else if (rightOk) {
            startingAngle = -Math.acos((contentClipRect.left - originX) / (stopActionDistanceFromCenter + bubbleDimension));
        } else {
            startingAngle = -Math.acos((contentClipRect.right - originX) / (stopActionDistanceFromCenter + bubbleDimension)) - (numActions - 1) * angleDelta;
        }

        // this looks a little complicated, but it's necessary to maintain the correct z ordering
        // so that the labels do not appear underneath a bubble
        double angle = startingAngle;
        int actionIndex = 0;
        int start = rightOk ? 0 : numActions - 1;
        int end = rightOk ? numActions : -1;
        int delta = rightOk ? 1 : -1;
        for (int i = start; i != end; i += delta) {
            BubbleView bubbleView = (BubbleView) getChildAt(i + 1);
            BubbleActions.Action action = bubbleActions.actions[actionIndex];
            bubbleView.textView.setText(action.actionName);
            bubbleView.imageView.setImageDrawable(action.foregroundDrawable);
            bubbleView.imageView.setBackground(action.backgroundDrawable);
            bubbleView.callback = action.callback;
            float halfWidth = bubbleView.getWidth() / 2.0f;
            float halfHeight = bubbleView.getHeight() / 2.0f;
            float cosAngle = (float) Math.cos(angle);
            float sinAngle = (float) Math.sin(angle);
            actionEndX[i] = originX + stopActionDistanceFromCenter * cosAngle - halfWidth;
            actionEndY[i] = originY + stopActionDistanceFromCenter * sinAngle - halfHeight;
            actionStartX[i] = originX + startActionDistanceFromCenter * cosAngle - halfWidth;
            actionStartY[i] = originY + startActionDistanceFromCenter * sinAngle - halfHeight;
            bubbleView.setX(actionStartX[i]);
            bubbleView.setY(actionStartY[i]);
            angle += angleDelta;
            actionIndex++;
        }

        startDrag(dragData, dragShadowBuilder, null, 0);
    }

    /**
     * Do the animating to show the bubble actions.
     */
    private void animateIn() {
        if (!overlayActive) {
            overlayActive = true;
            backgroundAnimator.start();
            bubbleActionIndicator.setVisibility(VISIBLE);
            ViewCompat.animate(bubbleActionIndicator)
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            super.onAnimationStart(view);
                            for (int i = 0; i < numActions; i++) {
                                final BubbleView child = (BubbleView) getChildAt(i + 1);
                                child.animateIn(actionEndX[i], actionEndY[i]);
                            }
                        }
                    });
        }
    }

    /**
     * Do the animating to hide the bubble actions.
     */
    private void animateOut() {
        if (overlayActive) {
            overlayActive = false;
            ViewCompat.animate(bubbleActionIndicator)
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            super.onAnimationStart(view);
                            for (int i = 0; i < numActions; i++) {
                                final BubbleView child = (BubbleView) getChildAt(i + 1);
                                child.animateOut(actionStartX[i], actionStartY[i]);
                            }
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            super.onAnimationEnd(view);
                            bubbleActionIndicator.setVisibility(INVISIBLE);
                            backgroundAnimator.reverse();
                        }
                    });
        }
    }

    /**
     * There can be a lot of drag events flying around, so we want to make sure that we are
     * processing the right ones.
     *
     * @param event
     * @return
     */
    private static boolean isDragEventForOverlay(DragEvent event) {
        return event.getClipDescription().getLabel().equals(TAG);
    }

    /**
     * When we get the correct type of drag event, show the overlay when the drag starts.
     * When we receive the drag end event, hide the overlay if it is currently showing.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onDragEvent(DragEvent event) {
        final int action = event.getAction();

        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                if (isDragEventForOverlay(event)) {
                    animateIn();
                    return true;
                }

                return false;
            case DragEvent.ACTION_DRAG_ENDED:
                animateOut();
                return true;
        }

        return false;
    }
}