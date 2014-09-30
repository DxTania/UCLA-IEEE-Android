package com.ucla_ieee.app.calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.signin.SessionManager;

import java.text.SimpleDateFormat;
import java.util.*;


public class CalendarActivity extends Fragment {

    private CaldroidFragment mCaldroidFragment;
    private TextView mDayTextView;
    private List<Event> mEvents, mSelectedEvents;
    private SimpleDateFormat mDateComp;
    private SimpleDateFormat mHumanDate;
    private Date mSelectedDate;
    private Date mPreviousSelection;
    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {
            boolean color = false;
            mSelectedDate = date;
            ArrayList<Event> newSelectedEvents = new ArrayList<Event>();
            for (Event e : mEvents) {
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
    private EventListAdapter mEventListAdapter;
    private TextView mNoEventsView;
    private MainActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        mDateComp = new SimpleDateFormat("yyyyMMdd");
        mHumanDate = new SimpleDateFormat("MMMM dd, yyyy");
        mPreviousSelection = null;
        mEvents = new ArrayList<Event>();
        mSelectedEvents = new ArrayList<Event>();
        mEventListAdapter = new EventListAdapter(getActivity(), mSelectedEvents);
        mSelectedDate = new Date();

        // Create calendar
        mCaldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        if (savedInstanceState != null) {
            mCaldroidFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE");
        } else {
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            mCaldroidFragment.setArguments(args);
        }
        mCaldroidFragment.setCaldroidListener(listener);
        getFragmentManager().beginTransaction()
                .replace(R.id.calendar, mCaldroidFragment)
                .addToBackStack(null)
                .commit();

        mDayTextView = (TextView) rootView.findViewById(R.id.dayText);
        mDayTextView.setText("Events for " + mHumanDate.format(new Date()));
        mNoEventsView = (TextView) rootView.findViewById(R.id.noEventsText);

        // Show/get cached events
        SessionManager sessionManager = new SessionManager(getActivity());
        JsonArray events = sessionManager.getCalReq();
        if (events != null) {
            addEvents(events);
        }

        // Set event list adapter
        ListView eventListView = (ListView) rootView.findViewById(R.id.eventList);
//        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent eventIntent = new Intent(getActivity(), EventActivity.class);
//                eventIntent.putExtra("Event", mSelectedEvents.get(position));
//                startActivity(eventIntent);
//            }
//        });
        eventListView.setAdapter(mEventListAdapter);

        // Start async task to check if new events have been added
        mActivity = (MainActivity) getActivity();
        mActivity.startCalendarAsyncCall(this);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.setCalendar(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // TODO: figureout saving the month they were looking @
        if (mCaldroidFragment != null) {
            mCaldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }
    }

    public void addEvents(JsonArray events) {
        ArrayList<Event> cancelled = new ArrayList<Event>();
        ArrayList<Event> newEvents = EventManager.createEvents(events, cancelled);
        // new Events does no include
        // cancelled ones
        // create two lists cancelled nd not cancelled
        // remove stale on both
        // Remove stale events & add new ones
        EventManager.removeStaleEvents(cancelled, mEvents, true);
        EventManager.removeStaleEvents(newEvents, mEvents, false);
        EventManager.removeStaleEvents(newEvents, mSelectedEvents, false);
        mEvents.addAll(newEvents);
        Collections.sort(mEvents, new DateComp());

        // Remove colored date from calendar
        for (Event event : cancelled) {
            Date start = event.getStartDate();
            if (start != null) {
                mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_white, start);
            }
        }

        // Add new events to calendar and color the dates
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
            // TODO: color for events for TODAY differently
        }
        mCaldroidFragment.refreshView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            mActivity.startCalendarAsyncCall(this);
            Toast.makeText(getActivity(), "Loading new events...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
