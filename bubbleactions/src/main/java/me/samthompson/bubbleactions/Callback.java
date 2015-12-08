package me.samthompson.bubbleactions;

/**
 * A {@link Callback#doAction()} call cooresponding to a particular action is invoked on the
 * main thread when the user lifts their finger from the screen while on top of the action.
 */
public interface Callback {
    void doAction();
}

