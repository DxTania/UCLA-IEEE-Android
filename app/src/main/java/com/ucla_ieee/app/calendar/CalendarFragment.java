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
import com.ucla_ieee.app.user.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Calendar fragment class that contains a calendar and listview for events
 * on the currently selected day
 */
public class CalendarFragment extends Fragment {

    private CaldroidFragment mCaldroidFragment;
    private TextView mDayTextView, mNoEventsView;
    private List<Event> mEvents, mSelectedEvents;
    private SimpleDateFormat mDateComp, mHumanDate;
    private Date mSelectedDate, mPreviousSelection, mCurrentDate;
    private EventListAdapter mEventListAdapter;
    private MainActivity mActivity;

    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {
            boolean color = false;
            mSelectedDate = date;
            ArrayList<Event> newSelectedEvents = new ArrayList<Event>();
            for (Event event : mEvents) {
                if (sameDay(event.getStartDate(), date)) {
                    newSelectedEvents.add(event);
                    color = true;
                }
            }
            if (color) {
                mSelectedEvents.clear();
                mSelectedEvents.addAll(newSelectedEvents);
                mDayTextView.setText("Events for " + mHumanDate.format(date));
                mCaldroidFragment.setSelectedDates(date, date);
                if (sameDay(date, new Date())) {
                    mCaldroidFragment.setBackgroundResourceForDate(R.drawable.today_selected, date);
                } else {
                    mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_sky_blue, date);
                }
                if (mPreviousSelection != null && !sameDay(date, mPreviousSelection)) {
                    if (sameDay(mPreviousSelection, new Date())) {
                        if (mCaldroidFragment.getBackgroundResourceForDate(new Date()) != R.drawable.today_no_events) {
                            mCaldroidFragment.setBackgroundResourceForDate(
                                    R.drawable.today_events, mPreviousSelection);
                        }
                    } else {
                        mCaldroidFragment.setBackgroundResourceForDate(
                                R.color.caldroid_lime_green, mPreviousSelection);
                    }
                }
                mPreviousSelection = date;
                mCaldroidFragment.refreshView();
                mEventListAdapter.notifyDataSetChanged();
                mNoEventsView.setVisibility(View.GONE);
            }
        }
    };

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
        mPreviousSelection = new Date();
        mCurrentDate = new Date();
        mActivity = (MainActivity) getActivity();

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

        mCaldroidFragment.setBackgroundResourceForDate(R.drawable.today_no_events, new Date());

        // Show/get cached events
        SessionManager sessionManager = new SessionManager(getActivity());
        JsonArray events = sessionManager.getJsonArray(SessionManager.Keys.CALENDAR);
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

        return rootView;
    }

    public boolean sameDay(Date date1, Date date2) {
        return mDateComp.format(date1).equals(mDateComp.format(date2));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentDate != null) {
            Date oldDate = mCurrentDate;
            mCurrentDate = new Date();
            if (!sameDay(mCurrentDate, oldDate)) {
                int colorOldToday = mCaldroidFragment.getBackgroundResourceForDate(oldDate);
                int colorToday = mCaldroidFragment.getBackgroundResourceForDate(mCurrentDate);
                // Recolor new today
                if (colorToday == R.color.caldroid_lime_green) {
                    mCaldroidFragment.setBackgroundResourceForDate(R.drawable.today_events, mCurrentDate);
                } else if (colorToday == R.color.caldroid_sky_blue) {
                    mCaldroidFragment.setBackgroundResourceForDate(R.drawable.today_selected, mCurrentDate);
                } else {
                    mCaldroidFragment.setBackgroundResourceForDate(R.drawable.today_no_events, mCurrentDate);
                }
                // Restore old date
                if (colorOldToday == R.drawable.today_events) {
                    mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_lime_green, oldDate);
                } else if (colorOldToday == R.drawable.today_selected) {
                    mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_sky_blue, oldDate);
                } else {
                    mCaldroidFragment.setBackgroundResourceForDate(R.color.caldroid_white, oldDate);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // TODO: figure out saving the month they were looking @
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

        // Remove colored date from calendar
        for (Event event : cancelled) {
            Date start = event.getStartDate();
            if (start != null) {
                if (sameDay(mSelectedDate, new Date())) {
                    mCaldroidFragment.setBackgroundResourceForDate(
                            R.drawable.today_no_events, start);
                } else {
                    mCaldroidFragment.setBackgroundResourceForDate(
                            R.color.caldroid_white, start);
                }
            }
        }

        // Add new events to calendar and color the dates
        for (Event event : newEvents) {
            Date start = event.getStartDate();
            if (start != null) {
                if (sameDay(start, new Date())) {
                    mSelectedEvents.add(event);
                    mCaldroidFragment.setBackgroundResourceForDate(R.drawable.today_events, start);
                    if (sameDay(mSelectedDate, new Date())) {
                        mCaldroidFragment.setBackgroundResourceForDate(
                                R.drawable.today_selected, start);
                    }
                } else {
                    mCaldroidFragment.setBackgroundResourceForDate(
                            R.color.caldroid_lime_green, start);
                }
            }
        }

        // Refresh views
        mEventListAdapter.notifyDataSetChanged();
        if (mSelectedEvents.size() > 0) {
            mNoEventsView.setVisibility(View.GONE);
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
            mActivity.getTaskManager().startCalendarAsyncCall(this);
            Toast.makeText(getActivity(), "Updating events...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
