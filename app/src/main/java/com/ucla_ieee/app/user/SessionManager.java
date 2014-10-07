package com.ucla_ieee.app.user;

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

    // File name for shared prefs
    private static final String PREF_NAME = "UCLAIEEEPrefs";
    private SharedPreferences mSharedPrefs;
    private Editor mEditor;
    private Context mContext;
    private JsonServerUtil mUtil;

    public SessionManager(Context context) {
        mContext = context;
        mSharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = null;
        mUtil = new JsonServerUtil();
    }

    public void loginUser(JsonObject json, String cookie) {
        mEditor = mSharedPrefs.edit();
        mEditor.putBoolean(Keys.LOGGED_IN.s(), true);
        // Email
        mEditor.putString(Keys.EMAIL.s(), !json.get(Keys.EMAIL.s()).isJsonNull() ?
                json.get(Keys.EMAIL.s()).getAsString() : "");
        // Cookie
        mEditor.putString(Keys.COOKIE.s(), cookie);
        // Name
        mEditor.putString(Keys.NAME.s(), !json.get(Keys.NAME.s()).isJsonNull() ?
                json.get(Keys.NAME.s()).getAsString() : "");
        // IEEE ID
        mEditor.putString(Keys.IEEE_ID.s(), !json.get(Keys.IEEE_ID.s()).isJsonNull() ?
                json.get(Keys.IEEE_ID.s()).getAsString() : "");
        // Points
        mEditor.putInt(Keys.POINTS.s(), !json.get(Keys.POINTS.s()).isJsonNull() ?
                json.get(Keys.POINTS.s()).getAsInt() : 0);
        mEditor.commit();
    }

    public void logoutUser() {
        mEditor = mSharedPrefs.edit();
        mEditor.putBoolean(Keys.LOGGED_IN.s(), false);
        for (Keys key : Keys.values()) {
            mEditor.remove(key.s());
        }
        mEditor.commit();
    }

    public String getSyncToken() {
        return mSharedPrefs.getString(Keys.TOKEN.s(), null);
    }

    public void setSyncToken(String token) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.TOKEN.s(), token);
        mEditor.commit();
    }

    public void storeCalReq(String s) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.JSON.s(), s);
        mEditor.commit();
    }

    public void storeAnnouncements(String s) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.ANNOUNCEMENTS.s(), s);
        mEditor.commit();
    }

    public void updateSession(String email, String name, String id, int points) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.EMAIL.s(), email);
        mEditor.putString(Keys.NAME.s(), name);
        mEditor.putString(Keys.IEEE_ID.s(), id);
        mEditor.putInt(Keys.POINTS.s(), points);
        mEditor.commit();
    }

    public void updateAttendedEvents(String events) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.ATTENDED_EVENTS.s(), events);
        mEditor.commit();
    }

    public void addAttendedEvent(JsonObject event) {
        JsonArray attendedEvents = getAttendedEvents();
        attendedEvents.add(event);
        updateAttendedEvents(attendedEvents.toString());
    }

    public boolean isLoggedIn() {
        return mSharedPrefs.getBoolean(Keys.LOGGED_IN.s(), false);
    }

    public JsonArray getCalReq() {
        return mUtil.getJsonArrayFromString(mSharedPrefs.getString(Keys.JSON.s(), null));
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

    public JsonArray getAttendedEvents() {
        return mUtil.getJsonArrayFromString(mSharedPrefs.getString(Keys.ATTENDED_EVENTS.s(), null));
    }

    public JsonArray getAnnouncements() {
        return mUtil.getJsonArrayFromString(mSharedPrefs.getString(Keys.ANNOUNCEMENTS.s(), null));
    }

    public int getPoints() {
        return mSharedPrefs.getInt(Keys.POINTS.s(), 0);
    }

    public enum Keys {
        LOGGED_IN("IsLoggedIn"), EMAIL("email"), TOKEN("syncToken"), JSON("json"), COOKIE("cookie"),
        NAME("name"), IEEE_ID("ieee_id"), ANNOUNCEMENTS("announcements"), POINTS("points"),
        ATTENDED_EVENTS("attended_events"), NEWS("news");

        private final String text;

        private Keys(final String text) {
            this.text = text;
        }

        public String s() {
            return text;
        }
    }
}
