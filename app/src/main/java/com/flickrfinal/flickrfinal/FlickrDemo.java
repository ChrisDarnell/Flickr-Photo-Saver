

package com.flickrfinal.flickrfinal;

import android.app.Application;

import com.flickrfinal.flickrfinal.retrofit.FlickrClient;
import com.squareup.otto.Bus;



public class FlickrDemo extends Application {
    protected static Bus mEventBus;
    protected static FlickrClient mFlickrClient;
    protected static String mOwnUserId;

    public static Bus getBusInstance() {
        return mEventBus;
    }

    public static FlickrClient getFlickrClient() {
        return mFlickrClient;
    }

    public static void setFlickrClient(final FlickrClient client) {
        mFlickrClient = client;
    }

    public static void setOwnUserId(final String userId) {
        mOwnUserId = userId;
    }

    public static String getOwnUserId() {
        return mOwnUserId;
    }

    public final void onCreate() {
        super.onCreate();
        mEventBus = new Bus();
    }
}