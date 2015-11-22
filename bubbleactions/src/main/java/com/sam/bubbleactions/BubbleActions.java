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
 * Inspired by the Pinterest Android app, BubbleActions make it easy to perform actions on ui
 * elements by simply dragging your finger. BubbleActions uses a fluent interface to build and show
 * actions similar to SnackBar or AlertDialog.
 */
public class BubbleActions {

    private static final String TAG = BubbleActions.class.getSimpleName();

    /**
     * A {@link Callback#doAction()} call cooresponding to a particular action is invoked on the
     * main thread when the user lifts their finger from the screen while on top of the action.
     */
    public interface Callback {
        void doAction();
    }

    private ViewGroup root;
    private BubbleActionOverlay overlay;
    private Method getLastTouchPoint;
    private Object viewRootImpl;
    private Point touchPoint = new Point();
    private boolean showing = false;
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
     * Opens up BubbleActions on the view argument.
     *
     * @param view the view that the BubbleActions are contextually connected to. The
     *             view must have a root view.
     * @return a BubbleActions instance
     */
    public static BubbleActions on(View view) {
        return on(view, R.drawable.bubble_actions_indicator);
    }

    /**
     * Open up BubbleActions on a view using the specified resource id as the indicator.
     *
     * @param view the view that the BubbleActions are contextually connected to. The
     *             view must have a root view.
     * @param indicatorRes indicator resource id
     * @return a BubbleActions instance
     */
    public static BubbleActions on(View view, int indicatorRes) {
        return on(view, ResourcesCompat.getDrawable(view.getResources(), indicatorRes, view.getContext().getTheme()));
    }

    /**
     * Open up BubbleActions on a view, using the specified drawable as the indicator.
     *
     * @param view the view that the BubbleActions are contextually connected to. The
     *             view must have a root view.
     * @param indicator indicator drawable
     * @return a BubbleActions instance
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

    /**
     * Set the typeface of the labels for the BubbleActions.
     *
     * @param typeface the typeface to set on the labels
     * @return the BubbleActions instance that called this method
     */
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
     * @return the BubbleActions instance that called this method
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
     * @return the BubbleActions instance that called this method
     */
    public BubbleActions addAction(CharSequence actionName, Drawable foreground, Drawable background, Callback callback) {
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

        return this;
    }

    /**
     * Show the bubble actions. Internally this will do 3 things:
     *      1. Add the overlay to the root view
     *      2. Use reflection to get the last touched xy location
     *      3. Animate the overlay in
     */
    public void show() {
        if (showing) {
            return;
        }

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

        showing = true;
        overlay.showOverlay();
    }

    void hideOverlay() {
        showing = false;
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
