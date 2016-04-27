/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package eu.rebelcorp.parse;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;

import android.app.NotificationManager;
import android.content.Context;
import android.app.Activity;
import android.provider.Settings.Secure;

import com.parse.Parse;
import com.parse.ParsePush;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.parse.ParseUser;
import com.parse.LogInCallback;
import com.parse.SaveCallback;
import com.parse.ParseException;

@Kroll.module(name="Parse", id="eu.rebelcorp.parse")
public class ParseModule extends KrollModule
{

    // Module instance
    private static ParseModule module;

    // Standard Debugging variables
    private static final String TAG = "ParseModule";

    // tiapp.xml properties containing Parse's app id and client key
    public static String PROPERTY_APP_ID = "Parse_AppId";
    public static String PROPERTY_CLIENT_KEY = "Parse_ClientKey";
    public static String PROPERTY_SERVER_URL = "Parse_ServerUrl";

    public static final int STATE_RUNNING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_DESTROYED = 3;

    /* Control the state of the activity */
    private int state = STATE_DESTROYED;

    // You can define constants with @Kroll.constant, for example:
    // @Kroll.constant public static final String EXTERNAL_NAME = value;

    public ParseModule()
    {
        super();
        module = this;
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app)
    {
        String appId = TiApplication.getInstance().getAppProperties().getString(ParseModule.PROPERTY_APP_ID, "");
        String clientKey = TiApplication.getInstance().getAppProperties().getString(ParseModule.PROPERTY_CLIENT_KEY, "");
        String serverUrl = TiApplication.getInstance().getAppProperties().getString(ParseModule.PROPERTY_SERVER_URL, "");

        Log.d(TAG, "Initializing with: " + appId + ", " + clientKey + ", " + serverUrl);
        Parse.initialize(new Parse.Configuration.Builder(TiApplication.getInstance())
	        .applicationId(appId)
	        .clientKey(clientKey)
	        .server(serverUrl + "/") // The trailing slash is important.
	        .build()
	    );
    }

    /* Get control over the module's state */
    public void onStart(Activity activity)
    {
        super.onStart(activity);
        setState(STATE_RUNNING);
    }

    public void onResume(Activity activity)
    {
        super.onResume(activity);
        setState(STATE_RUNNING);
    }

    public void onPause(Activity activity)
    {
        super.onPause(activity);
        setState(STATE_STOPPED);
    }

    public void onStop(Activity activity)
    {
        super.onStop(activity);
        setState(STATE_STOPPED);
    }

    public void onDestroy(Activity activity)
    {
        super.onDestroy(activity);
        setState(STATE_DESTROYED);
    }

    private void setState(int state)
    {
        this.state = state;
    }

    /* An accessor from the outside */
    public int getState()
    {
        return state;
    }

    /* Get an instance of that module*/
    public static ParseModule getInstance() {
        return module;
    }

    // Methods
    @Kroll.method
    public void start()
    {
        setState(STATE_RUNNING);
        // Track Push opens
        ParseAnalytics.trackAppOpenedInBackground(TiApplication.getAppRootOrCurrentActivity().getIntent());
        ParseInstallation.getCurrentInstallation().put("androidId", getAndroidId());
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Installation initialization failed: " + e.getMessage());
                }
                module.fireEvent("installationId", getCurrentInstallationId());
            }
        });
    }

    @Kroll.method
    public void enablePush() {
        // Deprecated. Now happens automatically
    }

    @Kroll.method
    public void authenticate(@Kroll.argument String sessionToken) {
        ParseUser.becomeInBackground(sessionToken, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                }
            }
        });
    }

    @Kroll.method
    public void subscribeChannel(@Kroll.argument String channel) {
        ParsePush.subscribeInBackground(channel);
    }

    @Kroll.method
    public void unsubscribeChannel(@Kroll.argument String channel) {
        ParsePush.unsubscribeInBackground(channel);
    }

    @Kroll.method
    public void putValue(@Kroll.argument String key, @Kroll.argument Object value) {
        ParseInstallation.getCurrentInstallation().put(key, value);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    @Kroll.method
    public String getCurrentInstallationId() {
        return ParseInstallation.getCurrentInstallation().getInstallationId();
    }

    @Kroll.method
    public String getObjectId() {
        return ParseInstallation.getCurrentInstallation().getObjectId();
    }
    
    @Kroll.method
    public void notificationClear() {
        TiApplication context = TiApplication.getInstance();
        NotificationManager notifiyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifiyMgr.cancelAll();
    }

    protected String getAndroidId() {
        Context context = TiApplication.getInstance().getApplicationContext();
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }
}
