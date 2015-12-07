package me.samthompson.bubbleactions;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;

import com.sam.bubbleactions.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Inspired by the Pinterest Android app, BubbleActions make it easy to perform actions on ui
 * elements by simply dragging your finger. BubbleActions uses a fluent interface to build and show
 * actions similar to SnackBar or AlertDialog.
 */
public class BubbleActions {

    private static final String TAG = BubbleActions.class.getSimpleName();

    private ViewGroup root;
    private BubbleActionOverlay overlay;
    private Method getLastTouchPoint;
    private Object viewRootImpl;
    private Point touchPoint = new Point();
    private boolean showing = false;
    Action[] actions = new Action[BubbleActionOverlay.MAX_ACTIONS];
    int numActions = 0;
    Drawable indicator;

    private BubbleActions(ViewGroup root) {
        this.indicator = ResourcesCompat.getDrawable(root.getResources(), R.drawable.bubble_actions_indicator, root.getContext().getTheme());
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
     * Open up BubbleActions on a view.
     *
     * @param view the view that the BubbleActions are contextually connected to. The
     *             view must have a root view.
     * @return a BubbleActions instance
     */
    public static BubbleActions on(View view) {
        View rootView = view.getRootView();
        if (rootView == null) {
            throw new IllegalArgumentException("View argument must have a root view.");
        }

        if (!(rootView instanceof ViewGroup)) {
            throw new IllegalArgumentException("View argument must have a ViewGroup root view");
        }


        return new BubbleActions((ViewGroup) rootView);
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
     * Set the indicator drawable. The default is a semi-transparent circle.
     *
     * @param indicatorRes drawable resource id to be drawn indicating what the bubble actions
     *                     are acting on
     * @return the BubbleActions instance that called this method
     */
    public BubbleActions withIndicator(int indicatorRes) {
        this.indicator = ResourcesCompat.getDrawable(root.getResources(), indicatorRes, root.getContext().getTheme());
        return this;
    }

    /**
     * Set the indicator drawable. The default is a semi-transparent circle.
     *
     * @param indicator drawable to be drawn indicating what the bubble actions are acting on
     * @return the BubbleActions instance that called this method
     */
    public BubbleActions withIndicator(Drawable indicator) {
        this.indicator = indicator;
        return this;
    }

    /**
     * Add an action using resource ids. The foreground is usually an icon signifying the action
     * that the user will be performing. The background is used to determine shadow, although if the
     * foreground does not take up the entire space of the bubble, it can be used to give user
     * feedback. Check out the sample app for an example of this. Actions are not limited to
     * circles.
     *
     * @param actionName The label displayed above the bubble action
     * @param drawableRes The content of the bubble action
     * @param callback A callback run on the main thread when the action is selected
     * @return the BubbleActions instance that called this method
     */
    public BubbleActions addAction(CharSequence actionName, int drawableRes, Callback callback) {
        Resources resources = root.getResources();
        Resources.Theme theme = root.getContext().getTheme();
        addAction(actionName, ResourcesCompat.getDrawable(resources, drawableRes, theme), callback);
        return this;
    }

    /**
     * Add an action using drawables. See the description at {@link #addAction(CharSequence, int, Callback)} for
     * details.
     *
     * @param actionName The label displayed above the bubble action
     * @param drawable The content of the bubble action
     * @param callback A callback run on the main thread when the action is selected
     * @return the BubbleActions instance that called this method
     */
    public BubbleActions addAction(CharSequence actionName, Drawable drawable, Callback callback) {
        if (numActions >= actions.length) {
            throw new IllegalStateException(TAG + ": cannot add more than " + BubbleActionOverlay.MAX_ACTIONS + " actions.");
        }

        if (drawable == null) {
            throw new IllegalArgumentException(TAG + ": the drawable cannot resolve to null.");
        }

        if (callback == null) {
            throw new IllegalArgumentException(TAG + ": the callback must not be null.");
        }

        actions[numActions] = new Action(actionName, drawable, callback);
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
            showOverlay();
        } else {
            overlay.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    overlay.removeOnLayoutChangeListener(this);
                    showOverlay();
                }
            });
        }
    }

    public boolean isShowing() {
        return showing;
    }

    private void showOverlay() {
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

        overlay.getAnimateSetShow()
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);
                        showing = true;
                        overlay.startDrag();
                    }
                })
                .start();
    }

    void removeOverlay() {
        showing = false;
        root.removeView(overlay);
    }

    private View.OnDragListener overlayDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return DragUtils.isDragForMe(event.getClipDescription().getLabel());
                case DragEvent.ACTION_DRAG_ENDED:
                    overlay.getAnimateSetHide()
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(View view) {
                                    super.onAnimationEnd(view);
                                    removeOverlay();
                                }
                            });
                    return true;
            }

            return false;
        }
    };

}
