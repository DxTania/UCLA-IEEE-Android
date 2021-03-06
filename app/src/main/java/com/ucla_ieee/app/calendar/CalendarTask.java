package com.ucla_ieee.app.calendar;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.user.SessionManager;
import com.ucla_ieee.app.util.JsonServerUtil;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Represents an asynchronous get task used to retrieve calendar events
 */
public class CalendarTask extends AsyncTask<Void, Void, String> {
    private static final String CAL_ID = "umh1upatck4qihkji9k6ntpc9k@group.calendar.google.com";
    private static final String API_KEY = "AIzaSyAgLz-5vEBqTeJtCv_eiW0zQjKMlJqcztI";

    SessionManager mSessionManager;
    MainActivity mContext;
    JsonServerUtil mUtil;

    public CalendarTask(MainActivity parent) {
        mSessionManager = new SessionManager(parent);
        mContext = parent;
        mUtil = new JsonServerUtil();
    }

    @Override
    protected String doInBackground(Void... params) {
        List<NameValuePair> calendarParams = new ArrayList<NameValuePair>();
        calendarParams.add(new BasicNameValuePair("key", API_KEY));
        String syncToken = mSessionManager.getString(SessionManager.Keys.TOKEN);
        if (!TextUtils.isEmpty(syncToken)) {
            calendarParams.add(new BasicNameValuePair("syncToken", syncToken));
        } else {
            // Only get events from the start of the school year in September
            SimpleDateFormat timeMin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            SimpleDateFormat now = new SimpleDateFormat("yyyy-MM-dd");
            int curYear = Calendar.getInstance().get(Calendar.YEAR);
            int curMonth = Calendar.getInstance().get(Calendar.MONTH);
            if (++curMonth < 7) { // If we are towards the end of a school year
                curYear--; // Use September from last year
            }
            try {
                Date yearStart = now.parse(String.valueOf(curYear) + "-09-01");
                calendarParams.add(new BasicNameValuePair("timeMin", timeMin.format(yearStart)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String paramString = URLEncodedUtils.format(calendarParams, "UTF-8");
        HttpGet httpGet = new HttpGet("https://www.googleapis.com/calendar/v3/calendars/"
                + CAL_ID + "/events?" + paramString);

        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse response = httpClient.execute(httpGet);
            return mUtil.getStringFromServerResponse(response.getEntity());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        JsonObject json = mUtil.getJsonObjectFromString(response);
        if (json == null || json.get("items") == null) {
            Toast.makeText(mContext, "Couldn't load new events :(", Toast.LENGTH_SHORT).show();
            mContext.getTaskManager().finishCalendarTask();
            return; // error
        }

        String nextSyncToken = json.get("nextSyncToken").getAsString();
        if (!TextUtils.isEmpty(nextSyncToken)) {
            mSessionManager.setSyncToken(nextSyncToken);
        }

        JsonArray newItems = json.get("items").getAsJsonArray();
        JsonArray prevItems = mSessionManager.getJsonArray(SessionManager.Keys.CALENDAR);
        if (prevItems == null) {
            mSessionManager.storeCalReq(newItems.toString());
        } else if (newItems.size() > 0) {
            // Make sure we don't duplicate events
            String items = EventManager.reviseJson(newItems, prevItems);
            mSessionManager.storeCalReq(items);
        }

        // Add to calendar if possible
        if (newItems.size() > 0) {
            Toast.makeText(mContext, "New events were loaded!", Toast.LENGTH_SHORT).show();
            if (mContext.getTaskManager().getCalendar() != null) {
                mContext.getTaskManager().getCalendar().addEvents(newItems);
            }
        }

        mContext.getTaskManager().finishCalendarTask();
    }
}