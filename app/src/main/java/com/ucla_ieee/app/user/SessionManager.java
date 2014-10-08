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
    private JsonServerUtil mUtil;

    public SessionManager(Context context) {
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
        // Total Points
        mEditor.putInt(Keys.TOTAL_POINTS.s(), !json.get(Keys.TOTAL_POINTS.s()).isJsonNull() ?
                json.get(Keys.TOTAL_POINTS.s()).getAsInt() : 0);
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

    public void setSyncToken(String token) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.TOKEN.s(), token);
        mEditor.commit();
    }

    public void storeCalReq(String s) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.CALENDAR.s(), s);
        mEditor.commit();
    }

    public void storeAnnouncements(String s) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.ANNOUNCEMENTS.s(), s);
        mEditor.commit();
    }

    public void updateSession(JsonObject user) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.EMAIL.s(), user.get("email").getAsString());
        mEditor.putString(Keys.NAME.s(), user.get("name").getAsString());
        mEditor.putString(Keys.IEEE_ID.s(), user.get("ieee_id").getAsString());
        mEditor.putString(Keys.MAJOR.s(), user.get("major").getAsString());
        mEditor.putString(Keys.YEAR.s(), user.get("year").getAsString());
        mEditor.putInt(Keys.POINTS.s(), user.get("points").getAsInt());
        mEditor.putInt(Keys.TOTAL_POINTS.s(), user.get("total_points").getAsInt());
        mEditor.commit();
    }

    public void updateAttendedEvents(String events) {
        mEditor = mSharedPrefs.edit();
        mEditor.putString(Keys.ATTENDED_EVENTS.s(), events);
        mEditor.commit();
    }

    public void addAttendedEvent(JsonObject event) {
        JsonArray attendedEvents = getJsonArray(Keys.ATTENDED_EVENTS);
        attendedEvents.add(event);
        updateAttendedEvents(attendedEvents.toString());
    }

    public boolean isLoggedIn() {
        return mSharedPrefs.getBoolean(Keys.LOGGED_IN.s(), false);
    }

    public String getString(Keys name) {
        return mSharedPrefs.getString(name.s(), "");
    }

    public JsonArray getJsonArray(Keys name) {
        return mUtil.getJsonArrayFromString(mSharedPrefs.getString(name.s(), null));
    }

    public int getInt(Keys name) {
        return mSharedPrefs.getInt(name.s(), 0);
    }

    public enum Keys {
        LOGGED_IN("IsLoggedIn"), EMAIL("email"), TOKEN("syncToken"), CALENDAR("json"), COOKIE("cookie"),
        NAME("name"), IEEE_ID("ieee_id"), ANNOUNCEMENTS("announcements"), POINTS("points"),
        ATTENDED_EVENTS("attended_events"), NEWS("news"), MAJOR("major"), YEAR("year"), TOTAL_POINTS("total_points");

        private final String text;

        private Keys(final String text) {
            this.text = text;
        }

        public String s() {
            return text;
        }
    }
}
