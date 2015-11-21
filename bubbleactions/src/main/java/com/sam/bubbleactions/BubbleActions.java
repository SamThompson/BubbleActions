package com.sam.bubbleactions;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sam on 10/30/15.
 */
public class BubbleActions {

    private static final String TAG = BubbleActions.class.getSimpleName();

    public interface Callback {
        void doAction();
    }

    private ViewGroup root;
    private BubbleActionOverlay overlay;
    private Method getLastTouchPoint;
    private Object viewRootImpl;
    private Point touchPoint = new Point();
    Action[] actions = new Action[BubbleActionOverlay.MAX_ACTIONS];
    int numActions = 0;
    Drawable indicator;

    private BubbleActions(ViewGroup root, Drawable indicator) {
        this.indicator = indicator;
        this.root = root;
        overlay = new BubbleActionOverlay(root.getContext());
        overlay.setOnDragListener(overlayDragListener);

        // Use reflection to get the ViewRootImpl
        try {
            Method method = root.getClass().getMethod("getViewRootImpl");
            viewRootImpl = method.invoke(root);
            getLastTouchPoint = viewRootImpl.getClass().getMethod("getLastTouchPoint", Point.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open up bubble actions on a view, using the default indicator. Using Bubble actions requires
     * the view to have {@link BubbleActionLayout} as a parent.
     *
     * @param view
     * @return
     */
    public static BubbleActions on(View view) {
        return on(view, R.drawable.bubble_actions_indicator);
    }

    /**
     * Open up bubble actions on a view, using the specified resource as the indicator.
     *
     * @param view
     * @param indicatorRes indicator resource id
     * @return
     */
    public static BubbleActions on(View view, int indicatorRes) {
        return on(view, ResourcesCompat.getDrawable(view.getResources(), indicatorRes, view.getContext().getTheme()));
    }

    /**
     * Open up bubble actions on a view, using the specified drawable as the indicator.
     *
     * @param view
     * @param indicator indicator drawable
     * @return
     */
    public static BubbleActions on(View view, Drawable indicator) {
        View rootView = view.getRootView();
        if (rootView == null) {
            throw new IllegalArgumentException("View argument must have a root view.");
        }

        if (!(rootView instanceof ViewGroup)) {
            throw new IllegalArgumentException("View argument must have a ViewGroup root view");
        }


        return new BubbleActions((ViewGroup) rootView, indicator);
    }

    public BubbleActions withTypeface(Typeface typeface) {
        overlay.setLabelTypeface(typeface);
        return this;
    }

    /**
     * Add an action using resource ids. The foreground is usually an icon signifying the action
     * that the user will be performing. The background is used to determine shadow, although if the
     * foreground does not take up the entire space of the bubble, it can be used to give user
     * feedback. Check out the sample app for an example of this. Actions are not limited to
     * circles.
     *
     * @param foregroundRes The content of the bubble action
     * @param backgroundRes The background of the bubble action used to determine shadow
     * @return
     */
    public BubbleActions addAction(CharSequence actionName, int foregroundRes, int backgroundRes, Callback callback) {
        Resources resources = root.getResources();
        Resources.Theme theme = root.getContext().getTheme();
        addAction(actionName, ResourcesCompat.getDrawable(resources, foregroundRes, theme), ResourcesCompat.getDrawable(resources, backgroundRes, theme), callback);
        return this;
    }

    /**
     * Add an action using drawables. See the description at {@link #addAction(CharSequence, int, int, com.sam.bubbleactions.BubbleActions.Callback)} for
     * details.
     *
     * @param foreground The content of the bubble action
     * @param background The background of the bubble action used to determine shadow
     */
    public void addAction(CharSequence actionName, Drawable foreground, Drawable background, Callback callback) {
        if (numActions >= actions.length) {
            throw new IllegalStateException(TAG + ": cannot add more than " + BubbleActionOverlay.MAX_ACTIONS + " actions.");
        }

        if (foreground == null) {
            throw new IllegalArgumentException(TAG + ": the foreground drawable cannot resolve to null.");
        }

        if (background == null) {
            throw new IllegalArgumentException(TAG + ": the background drawable cannot resolve to null.");
        }

        if (callback == null) {
            throw new IllegalArgumentException(TAG + ": the callback must not be null.");
        }

        actions[numActions] = new Action(actionName, foreground, background, callback);
        numActions++;
    }

    /**
     * Show the bubble actions.
     */
    public void show() {
        if (overlay.getParent() == null) {
            root.addView(overlay);
        }

        if (ViewCompat.isLaidOut(overlay)) {
            setupAndShow();
        } else {
            overlay.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    setupAndShow();
                    overlay.removeOnLayoutChangeListener(this);
                }
            });
        }
    }

    private void setupAndShow() {
        // use reflection to get the last touched xy location
        try {
            getLastTouchPoint.invoke(viewRootImpl, touchPoint);
            overlay.setupOverlay(touchPoint.x, touchPoint.y, this);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        overlay.showOverlay();
    }

    void hideOverlay() {
        root.removeView(overlay);
    }

    private View.OnDragListener overlayDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return overlay.dragStarted(event);
                case DragEvent.ACTION_DRAG_ENDED:
                    return overlay.dragEnded(BubbleActions.this);
            }

            return false;
        }
    };

    /**
     * An abstraction of the bubble action. Each action has a foreground and a background drawable,
     * as well as a callback.
     */
    static class Action {
        CharSequence actionName;
        Drawable foregroundDrawable;
        Drawable backgroundDrawable;
        Callback callback;

        private Action(CharSequence actionName, Drawable foregroundDrawable, Drawable backgroundDrawable, Callback callback) {
            this.actionName = actionName;
            this.foregroundDrawable = foregroundDrawable;
            this.backgroundDrawable = backgroundDrawable;
            this.callback = callback;
        }
    }

}
