package me.samthompson.bubbleactions_sample;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by sam on 12/9/15.
 */
public class BubbleActionsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }

}
