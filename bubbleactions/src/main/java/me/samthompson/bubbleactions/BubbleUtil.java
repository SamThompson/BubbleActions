package me.samthompson.bubbleactions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.res.ResourcesCompat;

/**
 * A collection of helper methods for use with BubbleActions.
 */
public class BubbleUtil {

    private BubbleUtil() {
    }

    /**
     * Create a drawable to be used as for a bubble. This is not necessarily the best way to create
     * drawables for your bubbles, and you should probably use a layer-list or combine the icon and
     * background in your favorite image editing program.
     *
     * @param context       used to load the resource
     * @param iconRes       resource id of the icon drawable
     * @param backgroundRes resource id for the background drawable
     * @return a drawable where the icon appears over the background
     */
    public static Drawable makeBubbleDrawable(Context context, int iconRes, int backgroundRes) {
        return makeBubbleDrawable(ResourcesCompat.getDrawable(context.getResources(), iconRes, context.getTheme()),
                ResourcesCompat.getDrawable(context.getResources(), backgroundRes, context.getTheme()));
    }

    /**
     * Create a drawable to be used as for a bubble. This is not necessarily the best way to create
     * drawables for your bubbles, and you should probably use a layer-list or combine the icon and
     * background in your favorite image editing program.
     *
     * @param icon       drawable for the icon
     * @param background drawable for the background
     * @return a drawable where the icon appears over the background
     */
    public static Drawable makeBubbleDrawable(Drawable icon, Drawable background) {
        return new LayerDrawable(new Drawable[]{icon, background});
    }

}
