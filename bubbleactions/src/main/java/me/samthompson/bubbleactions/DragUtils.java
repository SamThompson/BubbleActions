package me.samthompson.bubbleactions;

/**
 * Created by st028 on 12/7/15.
 */
class DragUtils {

    public static final String DRAG_LABEL = "me.samthompson.bubbleactions.BubbleActions";

    private DragUtils() {}

    public static boolean isDragForMe(CharSequence draglabel) {
        return DRAG_LABEL.equals(draglabel);
    }
}
