package com.sam.bubbleactions;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewParent;

/**
 * Created by sam on 10/30/15.
 */
public class BubbleActions {

    private static final String TAG = BubbleActions.class.getSimpleName();

    public interface Callback {
        void doAction();
    }

    Action[] actions = new Action[BubbleActionOverlay.MAX_ACTIONS];
    int numActions = 0;
    Drawable indicator;
    BubbleActionLayout bubbleActionLayout;

    /**
     * Use the builder methods.
     * @param bubbleActionLayout
     */
    private BubbleActions(BubbleActionLayout bubbleActionLayout) {
        this.bubbleActionLayout = bubbleActionLayout;
    }

    /**
     * Find the {@link BubbleActionLayout} by traversing up the view hierarchy.
     * @param view Our starting point in the crawl up the view hierarchy
     * @return the {@link BubbleActionLayout} above the view in the view hierarchy
     */
    public static BubbleActionLayout findBubbleActionLayout(View view) {
        while (view != null) {
            if (view instanceof BubbleActionLayout) {
                return (BubbleActionLayout) view;
            }

            ViewParent parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }

        throw new IllegalStateException(TAG + ": must be a descendant of a " + BubbleActionLayout.class.getSimpleName() + ".");
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
        BubbleActions actions = new BubbleActions(findBubbleActionLayout(view));
        actions.indicator = indicator;
        return actions;
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
        Resources resources = bubbleActionLayout.getResources();
        Resources.Theme theme = bubbleActionLayout.getContext().getTheme();
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
     * Show the bubble actions. Unlike the show method for SnackBar, this method is not thread safe.
     */
    public void show() {
        if (bubbleActionLayout == null) {
            throw new IllegalStateException(TAG + ": trying to show bubble actions when not a descendant of a BubbleActionLayout.");
        }

        bubbleActionLayout.showOverlay(this);
    }

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
