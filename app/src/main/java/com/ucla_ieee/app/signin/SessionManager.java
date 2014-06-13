package com.ucla_ieee.app.signin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private static final String KEY_TOKEN = "syncToken";
    private static final String KEY_JSON = "json";
    private static final String KEY_COOKIE = "cookie";

    public SessionManager (Context context) {
        mContext = context;
        mSharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPrefs.edit();
    }

    public void loginUser (String email, String cookie) {
        mEditor.putBoolean(KEY_LOGGED_IN, true);
        mEditor.putString(KEY_EMAIL, email);
        mEditor.putString(KEY_COOKIE, cookie);
        mEditor.commit();
    }

    public void logoutUser () {
        mEditor.putBoolean(KEY_LOGGED_IN, false);
        mEditor.remove(KEY_EMAIL);
        mEditor.remove(KEY_JSON);
        mEditor.remove(KEY_TOKEN);
        mEditor.commit();
    }

    public void setSyncToken(String token) {
        mEditor.putString(KEY_TOKEN, token);
        mEditor.commit();
    }

    public String getSyncToken() {
        return mSharedPrefs.getString(KEY_TOKEN, null);
    }

    public void storeCalReq(String s) {
        mEditor.putString(KEY_JSON, s);
        mEditor.commit();
    }

    public String getCalReq() {
        return mSharedPrefs.getString(KEY_JSON, null);
    }

    public boolean isLoggedIn () {
        if (mSharedPrefs.getBoolean(KEY_LOGGED_IN, false)) {
            // send request to server to check login status
        }
        return mSharedPrefs.getBoolean(KEY_LOGGED_IN, false);
    }
}
