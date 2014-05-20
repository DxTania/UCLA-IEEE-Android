package com.ucla_ieee.app.calendar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
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

    private CaldroidFragment mCaldroidFragment;
    private TextView mDayTextView;
    private List<Event> mEvents, mSelectedEvents;
    private SimpleDateFormat mDateComp;
    private SimpleDateFormat mHumanDate;
    private Date mSelectedDate;
    private Date mPreviousSelection;
    private EventListAdapter mEventListAdapter;
    private TextView mNoEventsView;

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
        mNoEventsView = (TextView) findViewById(R.id.noEventsText);
        mSelectedDate = new Date();
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
        CalendarTask eventsTask = new CalendarTask(this);
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
        ArrayList<Event> cancelled = new ArrayList<Event>();
        ArrayList<Event> newEvents = EventManager.createEvents(events, cancelled);
        // create two lists cancelled nd not cancelled
        // remove stale on both
        // Remove stale events & add new ones
        EventManager.removeStaleEvents(cancelled, mEvents, true);
        EventManager.removeStaleEvents(newEvents, mEvents, false);
        EventManager.removeStaleEvents(newEvents, mSelectedEvents, false);
        mEvents.addAll(newEvents);
        Collections.sort(mEvents, new DateComp());

        for(Event event : cancelled) {
            Date start = event.getStartDate();
            if (start != null) {
                mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_white, start);
            }
        }

        // Add new events to calendar
        for (Event event : newEvents) {
            Date start = event.getStartDate();
            if (start != null) {
                mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_lime_green, start);
            }
            if (mDateComp.format(start).equals(mDateComp.format(new Date()))) {
                mSelectedEvents.add(event);
            }
        }

        // Refresh views
        mEventListAdapter.notifyDataSetChanged();
        if (mSelectedEvents.size() > 0) {
            mNoEventsView.setVisibility(View.GONE);
            // TODO: color for events for TODAY
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
            boolean color = false;
            mSelectedDate = date;
            ArrayList<Event> newSelectedEvents = new ArrayList<Event>();
            for (Event e: mEvents) {
                if (mDateComp.format(e.getStartDate()).equals(mDateComp.format(date))) {
                    newSelectedEvents.add(e);
                    color = true;
                }
            }
            if (color) {
                mSelectedEvents.clear();
                mSelectedEvents.addAll(newSelectedEvents);
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
                mEventListAdapter.notifyDataSetChanged();
                mNoEventsView.setVisibility(View.GONE);
            }
        }
    };
}
