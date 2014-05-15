package com.ucla_ieee.app.calendar;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.gson.*;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.ucla_ieee.app.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class CalendarActivity extends FragmentActivity {
    private static final String CAL_ID = "umh1upatck4qihkji9k6ntpc9k@group.calendar.google.com";
    private static final String API_KEY = "AIzaSyAgLz-5vEBqTeJtCv_eiW0zQjKMlJqcztI";
    private CaldroidFragment mCaldroidFragment;
    private ArrayList<Event> mEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        setTitle("Calendar Events");

        CalendarTask eventsTask = new CalendarTask();
        eventsTask.execute((Void) null);

        mCaldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        mCaldroidFragment.setArguments(args);
        mCaldroidFragment.setCaldroidListener(listener);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, mCaldroidFragment);
        t.commit();

    }

    public void addEvents(JsonArray events) {
        EventCreator e = new EventCreator();
        mEvents = e.createEvents(events);

        for (Event event : mEvents) {
            Date start = event.getmStartDate();
            if (start != null) {
                mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_lime_green, start);
            }
        }

        mCaldroidFragment.refreshView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {
            mCaldroidFragment.setSelectedDates(date, date);
            mCaldroidFragment.refreshView();
        }


        @Override
        public void onLongClickDate(Date date, View view) {

        }
    };

    /**
     * Represents an asynchronous get task used to retrieve calendar events
     */
    public class CalendarTask extends AsyncTask<Void, Void, String> {

        CalendarTask() {  }

        @Override
        protected String doInBackground(Void... params) {

            HttpClient httpClient = new DefaultHttpClient();

            List<NameValuePair> calendarParams = new ArrayList<NameValuePair>();
            calendarParams.add(new BasicNameValuePair("key", API_KEY));
            // Add more parameters here
            String paramString = URLEncodedUtils.format(calendarParams, "UTF-8");

            HttpGet httpGet = new HttpGet("https://www.googleapis.com/calendar/v3/calendars/"
                    + CAL_ID + "/events?" + paramString);

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
                Toast.makeText(CalendarActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject json;
            try {
                JsonParser parser = new JsonParser();
                json = (JsonObject) parser.parse(response);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                Toast.makeText(CalendarActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            addEvents(json.getAsJsonArray("items"));
        }
    }

}
