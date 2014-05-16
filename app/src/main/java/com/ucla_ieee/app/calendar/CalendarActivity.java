package com.ucla_ieee.app.calendar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.gson.*;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.ucla_ieee.app.R;
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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class CalendarActivity extends FragmentActivity {
    private static final String CAL_ID = "umh1upatck4qihkji9k6ntpc9k@group.calendar.google.com";
    private static final String API_KEY = "AIzaSyAgLz-5vEBqTeJtCv_eiW0zQjKMlJqcztI";
    private CaldroidFragment mCaldroidFragment;
    private TextView mDayTextView;
    private List<Event> mEvents, mSelectedEvents;
    private SimpleDateFormat mDateComp;
    private SimpleDateFormat mHumanDate;
    private Date mPreviousSelection;
    private EventListAdapter mEventListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setTitle("Calendar of Events");

        mDayTextView = (TextView) findViewById(R.id.dayText);
        mDateComp = new SimpleDateFormat("yyyyMMdd");
        mHumanDate = new SimpleDateFormat("MMMM dd, yyyy");
        mPreviousSelection = null;
        mEvents = new ArrayList<Event>();
        mSelectedEvents = new ArrayList<Event>();
        mEventListAdapter = new EventListAdapter(this, mSelectedEvents);

        mDayTextView.setText("Events for " + mHumanDate.format(new Date()));

        // Set up calendar
        mCaldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        mCaldroidFragment.setArguments(args);
        mCaldroidFragment.setCaldroidListener(listener);

        // Display calendar
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, mCaldroidFragment);
        t.commit();

        // Show/get cached events
        SessionManager sessionManager = new SessionManager(CalendarActivity.this);
        String eventJson = sessionManager.getCalReq();
        if (!TextUtils.isEmpty(eventJson)) {
            JsonArray json;
            try {
                JsonParser parser = new JsonParser();
                json = (JsonArray) parser.parse(eventJson);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                return;
            }
            addEvents(json);
        }

        // Start async task to check if new events have been added
        CalendarTask eventsTask = new CalendarTask();
        eventsTask.execute((Void) null);

        // Set event list adapter
        ListView eventListView = (ListView) findViewById(R.id.eventList);
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent eventIntent = new Intent(CalendarActivity.this, EventActivity.class);
                eventIntent.putExtra("Event", mSelectedEvents.get(position));
                startActivity(eventIntent);
            }
        });
        eventListView.setAdapter(mEventListAdapter);
    }

    public void addEvents(JsonArray events) {
        ArrayList<Event> newEvents = EventCreator.createEvents(events);
        mEvents.addAll(newEvents);
        Collections.sort(mEvents, new DateComp());

        for (Event event : newEvents) {
            Date start = event.getStartDate();
            if (start != null) {
                mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_lime_green, start);
            }
            if (mDateComp.format(start).equals(mDateComp.format(new Date()))) {
                // TODO: Change this to array adapter!!!
                mSelectedEvents.add(event);
            }
        }
        mEventListAdapter.notifyDataSetChanged();
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
            mSelectedEvents.clear();
            boolean color = false;
            for (Event e: mEvents) {
                if (mDateComp.format(e.getStartDate()).equals(mDateComp.format(date))) {
                    mSelectedEvents.add(e);
                    color = true;
                }
            }
            if (color) {
                mDayTextView.setText("Events for " + mHumanDate.format(date));
                mCaldroidFragment.setSelectedDates(date, date);
                mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_sky_blue, date);
                if (mPreviousSelection != null &&
                        !mDateComp.format(mPreviousSelection).equals(mDateComp.format(date))) {
                    mCaldroidFragment.setBackgroundResourceForDate(
                            R.color.caldroid_lime_green, mPreviousSelection);
                }
                mPreviousSelection = date;
                mCaldroidFragment.refreshView();
            }
            mEventListAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Represents an asynchronous get task used to retrieve calendar events
     */
    public class CalendarTask extends AsyncTask<Void, Void, String> {
        SessionManager sessionManager;

        public CalendarTask() {
            sessionManager = new SessionManager(CalendarActivity.this);
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpClient httpClient = new DefaultHttpClient();

            List<NameValuePair> calendarParams = new ArrayList<NameValuePair>();
            calendarParams.add(new BasicNameValuePair("key", API_KEY));
            // TODO: Only retrieve events for current school year
            String syncToken = sessionManager.getSyncToken();
            if (!TextUtils.isEmpty(syncToken)) {
                calendarParams.add(new BasicNameValuePair("syncToken", syncToken));
            }
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

            String nextSyncToken = json.get("nextSyncToken").getAsString();
            sessionManager.setSyncToken(nextSyncToken);

            // Append new items to end of JsonArray string in user prefs
            String items = json.get("items").getAsJsonArray().toString();
            if (!TextUtils.isEmpty(items) && !items.equals("[]")) {
                String prev = sessionManager.getCalReq();
                String next;
                if (TextUtils.isEmpty(prev)) {
                    next = items;
                } else {
                    next = prev.substring(0, prev.length()-1) + items.substring(1, items.length());
                }
                sessionManager.storeCalReq(next);
                // TODO: What about events that were just updated, not added?
                addEvents(json.getAsJsonArray("items"));
            }
        }
    }

}
