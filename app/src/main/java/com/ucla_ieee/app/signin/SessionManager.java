package com.ucla_ieee.app.signin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.util.JsonServerUtil;

/**
 * Helper functions for session management of the user
 */
public class SessionManager {

    private SharedPreferences mSharedPrefs;
    private Editor mEditor;
    private Context mContext;
    private JsonServerUtil mUtil;

    // File name for shared prefs
    private static final String PREF_NAME = "UCLAIEEEPrefs";

    public enum Keys {
        LOGGED_IN("IsLoggedIn"), EMAIL("email"), TOKEN("syncToken"), JSON("json"), COOKIE("cookie"),
        NAME("name"), IEEE_ID("ieee_id"), ANNOUNCEMENTS("announcements"), POINTS("points");

        private final String text;

        private Keys(final String text) {
            this.text = text;
        }

        public String s() {
            return text;
        }
    }

    public SessionManager (Context context) {
        mContext = context;
        mSharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPrefs.edit();
        mUtil = new JsonServerUtil();
    }

    public void loginUser (JsonObject json, String cookie) {
        mEditor.putBoolean(Keys.LOGGED_IN.s(), true);
        // Email
        mEditor.putString(Keys.EMAIL.s(), !json.get(Keys.EMAIL.s()).isJsonNull()?
                json.get(Keys.EMAIL.s()).getAsString() : "");
        // Cookie
        mEditor.putString(Keys.COOKIE.s(), cookie);
        // Name
        mEditor.putString(Keys.NAME.s(), !json.get(Keys.NAME.s()).isJsonNull()?
                json.get(Keys.NAME.s()).getAsString() : "");
        // IEEE ID
        mEditor.putString(Keys.IEEE_ID.s(), !json.get(Keys.IEEE_ID.s()).isJsonNull()?
                json.get(Keys.IEEE_ID.s()).getAsString() : "");
        // TODO: get points too
        mEditor.commit();
    }

    public void logoutUser () {
        mEditor.putBoolean(Keys.LOGGED_IN.s(), false);
        for (Keys key : Keys.values()) {
            mEditor.remove(key.s());
        }
        mEditor.commit();
    }

    public void setSyncToken(String token) {
        mEditor.putString(Keys.TOKEN.s(), token);
        mEditor.commit();
    }

    public String getSyncToken() {
        return mSharedPrefs.getString(Keys.TOKEN.s(), null);
    }

    public void storeCalReq(String s) {
        mEditor.putString(Keys.JSON.s(), s);
        mEditor.commit();
    }

    public JsonArray getCalReq() {
        return mUtil.getJsonArrayFromString(mSharedPrefs.getString(Keys.JSON.s(), null));
    }

    public boolean isLoggedIn () {
        return mSharedPrefs.getBoolean(Keys.LOGGED_IN.s(), false);
    }

    public String getEmail() {
        return mSharedPrefs.getString(Keys.EMAIL.s(), "");
    }

    public String getName() {
        return mSharedPrefs.getString(Keys.NAME.s(), "");
    }

    public String getIEEEId() {
        return mSharedPrefs.getString(Keys.IEEE_ID.s(), "");
    }

    public String getCookie() {
        return mSharedPrefs.getString(Keys.COOKIE.s(), null);
    }

    public JsonArray getAnnouncements() {
        return mUtil.getJsonArrayFromString(mSharedPrefs.getString(Keys.ANNOUNCEMENTS.s(), null));
    }

    public void setAnnouncements(String s) {
        mEditor.putString(Keys.ANNOUNCEMENTS.s(), s);
        mEditor.commit();
    }

    public void updateSession(String email, String name, String id, int points) {
        mEditor.putString(Keys.EMAIL.s(), email);
        mEditor.putString(Keys.NAME.s(), name);
        mEditor.putString(Keys.IEEE_ID.s(), id);
        mEditor.putInt(Keys.POINTS.s(), points);
        mEditor.commit();
    }

    public void setPoints(int amt) {
        mEditor.putInt(Keys.POINTS.s(), amt);
        mEditor.commit();
    }

    public int getPoints() {
        return mSharedPrefs.getInt(Keys.POINTS.s(), 0);
    }
}
