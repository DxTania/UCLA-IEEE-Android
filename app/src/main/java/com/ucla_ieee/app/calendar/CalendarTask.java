package com.ucla_ieee.app.calendar;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ucla_ieee.app.calendar.CalendarActivity;
import com.ucla_ieee.app.calendar.EventManager;
import com.ucla_ieee.app.signin.SessionManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an asynchronous get task used to retrieve calendar events
 */
public class CalendarTask extends AsyncTask<Void, Void, String> {
    private static final String CAL_ID = "umh1upatck4qihkji9k6ntpc9k@group.calendar.google.com";
    private static final String API_KEY = "AIzaSyAgLz-5vEBqTeJtCv_eiW0zQjKMlJqcztI";

    SessionManager mSessionManager;
    CalendarActivity mParent;

    public CalendarTask(CalendarActivity parent) {
        mSessionManager = new SessionManager(parent);
        mParent = parent;
    }

    @Override
    protected String doInBackground(Void... params) {

        HttpClient httpClient = new DefaultHttpClient();

        List<NameValuePair> calendarParams = new ArrayList<NameValuePair>();
        calendarParams.add(new BasicNameValuePair("key", API_KEY));
        // TODO: Only retrieve events for current school year
        String syncToken = mSessionManager.getSyncToken();
        if (!TextUtils.isEmpty(syncToken)) {
            calendarParams.add(new BasicNameValuePair("syncToken", syncToken));
        }
        String paramString = URLEncodedUtils.format(calendarParams, "UTF-8");

        HttpGet httpGet = new HttpGet("https://www.googleapis.com/calendar/v3/calendars/"
                + CAL_ID + "/events?" + paramString);

        // Read and parse HTTP response
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder builder = new StringBuilder();

                String st;
                while((st = reader.readLine()) != null) {
                    builder.append(st).append("\n");
                }

                instream.close();
                return builder.toString();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (TextUtils.isEmpty(response)) {
            Toast.makeText(mParent, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonParser parser = new JsonParser();
        JsonObject json;
        try {
            json = (JsonObject) parser.parse(response);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            Toast.makeText(mParent, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        String nextSyncToken = json.get("nextSyncToken").getAsString();
        if (!TextUtils.isEmpty(nextSyncToken)) {
            mSessionManager.setSyncToken(nextSyncToken);
        }

        JsonArray newItems = json.get("items").getAsJsonArray();
        if (mSessionManager.getCalReq() == null) {
            mSessionManager.storeCalReq(newItems.toString());
        } else {
            JsonArray prevItems = parser.parse(mSessionManager.getCalReq()).getAsJsonArray();
            if (newItems.size() > 0 && prevItems != null) {
                // Make sure we don't duplicate events
                String items = EventManager.reviseJson(newItems, prevItems);
                mSessionManager.storeCalReq(items);
            } else if (prevItems == null) {
                // We don't have anything cached, store entire request
            } // Else no new items, leave stored req alone
        }

        // TODO: Deal with 410 GONE response
        if (newItems.size() > 0) {
            mParent.addEvents(newItems);
        }
    }
}