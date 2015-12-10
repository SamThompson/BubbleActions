package me.samthompson.bubbleactions;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.PopupMenu;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Inspired by the Pinterest Android app, BubbleActions make it easy to perform actions on ui
 * elements by simply dragging your finger. BubbleActions uses a fluent interface to build and show
 * actions similar to SnackBar or AlertDialog.
 */
public final class BubbleActions {

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
     * Set the actions using a menu xml resource. There are 3 requirements of the menu xml:
     * 1. The menu cannot have more than 5 items,
     * 2. Each menu item cannot have a submenu, and
     * 3. Each menu item must have an icon, title, and an id
     *
     * @param menuRes  The resource id of the menu
     * @param callback A callback to run on the main thread when an action is selected
     * @return
     */
    public BubbleActions fromMenu(int menuRes, final MenuCallback callback) {
        Menu menu = new PopupMenu(root.getContext(), null).getMenu();
        MenuInflater inflater = new MenuInflater(root.getContext());
        inflater.inflate(menuRes, menu);

        if (menu.size() > BubbleActionOverlay.MAX_ACTIONS) {
            throw new IllegalArgumentException(TAG + ": menu resource cannot have more than "
                    + BubbleActionOverlay.MAX_ACTIONS + "actions.");
        }

        for (int i = 0; i < menu.size(); i++) {
            final MenuItem item = menu.getItem(i);

            if (item.hasSubMenu() || item.getIcon() == null || item.getTitle() == null || item.getItemId() == 0) {
                throw new IllegalArgumentException(TAG + ": menu resource cannot have a submenu and " +
                        "must have an icon, title, and id.");
            }

            final int id = item.getItemId();
            addAction(item.getTitle(), item.getIcon(), new Callback() {
                @Override
                public void doAction() {
                    callback.doAction(id);
                }
            });
        }

        return this;
    }

    /**
     * Add an action using resource ids. The foreground is usually an icon signifying the action
     * that the user will be performing. The background is used to determine shadow, although if the
     * foreground does not take up the entire space of the bubble, it can be used to give user
     * feedback. Check out the sample app for an example of this. Actions are not limited to
     * circles.
     *
     * @param actionName  The label displayed above the bubble action
     * @param drawableRes The content of the bubble action
     * @param callback    A callback run on the main thread when the action is selected
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
     * @param drawable   The content of the bubble action
     * @param callback   A callback run on the main thread when the action is selected
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
     * 1. Add the overlay to the root view
     * 2. Use reflection to get the last touched xy location
     * 3. Animate the overlay in
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

        overlay.setOnAttachStateChangeListener(new BubbleActionOverlay.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                // As identified in SnackBar, if we receive this event, the user did not
                // initiate it, so hide the overlay and remove it from its parent so the state is
                // kept in sync
                overlay.setOnAttachStateChangeListener(null);
                if (isShowing()) {
                    removeOverlay();
                }
            }
        });

        overlay.startDrag();
    }

    void removeOverlay() {
        showing = false;
        root.removeView(overlay);
        overlay.resetBubbleViews();
    }

    private View.OnDragListener overlayDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (DragUtils.isDragForMe(event.getClipDescription().getLabel())) {
                        overlay.getAnimateSetShow()
                                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(View view) {
                                        super.onAnimationStart(view);
                                        overlay.animateDimBackground();
                                    }

                                    @Override
                                    public void onAnimationEnd(View view) {
                                        super.onAnimationEnd(view);
                                        showing = true;
                                    }
                                })
                                .start();
                        return true;
                    } else {
                        return false;
                    }

                case DragEvent.ACTION_DRAG_ENDED:
                    overlay.getAnimateSetHide()
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(View view) {
                                    super.onAnimationStart(view);
                                    overlay.animateUndimBackground();
                                }

                                @Override
                                public void onAnimationEnd(View view) {
                                    super.onAnimationEnd(view);
                                    removeOverlay();
                                }
                            })
                            .start();
                    return true;
            }

            return false;
        }
    };

}
