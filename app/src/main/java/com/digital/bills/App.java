package com.digital.bills;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;

/**
 * Authored by vedhavyas on 1/12/14.
 * Project My Bill Lite
 */


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        {
            Parse.initialize(this, "OCFlulMNpb06tukKl65V1pdekgi5adtC8oZTW8v2", "JlEfasN4VmEqoB4XkknOxU0SYTSTl3Plg24MLcUO");
            ParseInstallation.getCurrentInstallation().saveInBackground();
            ParseACL defaultACL = new ParseACL();
            defaultACL.setPublicReadAccess(true);
            defaultACL.setPublicWriteAccess(false);
            ParseACL.setDefaultACL(defaultACL, true);
        }

    }
}
