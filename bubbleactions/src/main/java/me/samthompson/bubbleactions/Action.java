package me.samthompson.bubbleactions;

import android.graphics.drawable.Drawable;

/**
 * An abstraction of the bubble action. Each action has a name, a drawable for the bubble,
 * as well as a callback.
 */
class Action {
    CharSequence actionName;
    Drawable bubble;
    Callback callback;

    Action(CharSequence actionName, Drawable bubble, Callback callback) {
        this.actionName = actionName;
        this.bubble = bubble;
        this.callback = callback;
    }
}