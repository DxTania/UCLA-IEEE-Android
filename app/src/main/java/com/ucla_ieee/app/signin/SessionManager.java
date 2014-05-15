package com.ucla_ieee.app.signin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Helper functions for session management of the user
 */
public class SessionManager {

    private SharedPreferences mSharedPrefs;
    private Editor mEditor;
    private Context mContext;

    // File name for shared prefs
    private static final String PREF_NAME = "UCLAIEEEPrefs";

    // Keys
    private static final String KEY_LOGGED_IN = "IsLoggedIn";
    public static final String KEY_EMAIL = "email";

    public SessionManager (Context context) {
        mContext = context;
        mSharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPrefs.edit();
    }

    public void loginUser (String email) {
        mEditor.putBoolean(KEY_LOGGED_IN, true);
        mEditor.putString(KEY_EMAIL, email);
        mEditor.commit();
    }

    public void logoutUser () {
        mEditor.clear();
        mEditor.commit();
    }

    public boolean isLoggedIn () {
        return mSharedPrefs.getBoolean(KEY_LOGGED_IN, false);
    }
}
