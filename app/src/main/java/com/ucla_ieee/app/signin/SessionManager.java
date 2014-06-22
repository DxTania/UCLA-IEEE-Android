package com.ucla_ieee.app.signin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

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
    private static final String KEY_NAME = "name";
    private static final String KEY_IEEE_ID = "ieee_id";

    public SessionManager (Context context) {
        mContext = context;
        mSharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPrefs.edit();
    }

    public void loginUser (JsonObject json, String cookie) {
        mEditor.putBoolean(KEY_LOGGED_IN, true);
        mEditor.putString(KEY_EMAIL, !json.get(KEY_EMAIL).isJsonNull()?
                json.get(KEY_EMAIL).getAsString() : "");
        mEditor.putString(KEY_COOKIE, cookie);
        mEditor.putString(KEY_NAME, !json.get(KEY_NAME).isJsonNull()?
                json.get(KEY_NAME).getAsString() : "");
        mEditor.putString(KEY_IEEE_ID, !json.get(KEY_IEEE_ID).isJsonNull()?
                json.get(KEY_IEEE_ID).getAsString() : "");
        mEditor.commit();
    }

    public void logoutUser () {
        // TODO: use enumeration
        mEditor.putBoolean(KEY_LOGGED_IN, false);
        mEditor.remove(KEY_EMAIL);
        mEditor.remove(KEY_JSON);
        mEditor.remove(KEY_TOKEN);
        mEditor.remove(KEY_COOKIE);
        mEditor.remove(KEY_NAME);
        mEditor.remove(KEY_IEEE_ID);
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
        return mSharedPrefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getEmail() {
        return mSharedPrefs.getString(KEY_EMAIL, "");
    }

    public String getName() {
        return mSharedPrefs.getString(KEY_NAME, "");
    }

    public String getIEEEId() {
        return mSharedPrefs.getString(KEY_IEEE_ID, "");
    }

    public String getCookie() {
        return mSharedPrefs.getString(KEY_COOKIE, null);
    }

    public void updateSession(String email, String name, String id) {
        mEditor.putString(KEY_EMAIL, email);
        mEditor.putString(KEY_NAME, name);
        mEditor.putString(KEY_IEEE_ID, id);
        mEditor.commit();
    }
}
