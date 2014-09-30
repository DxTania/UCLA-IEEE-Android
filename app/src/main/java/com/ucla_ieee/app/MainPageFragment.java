package com.ucla_ieee.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.calendar.Event;
import com.ucla_ieee.app.calendar.EventManager;
import com.ucla_ieee.app.signin.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainPageFragment extends Fragment {

    private SessionManager mSessionManager;
    private View rootView;
    private TextView mPointsView;
    private NewsFeedListAdapter mNewsFeedListAdapter;
    private ListView mNewsFeedListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPointsView = (TextView) rootView.findViewById(R.id.numPoints);
        mSessionManager = new SessionManager(getActivity());
        mPointsView.setText(String.valueOf(mSessionManager.getPoints()));

        // TODO: Do this less often??
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.startCalendarAsyncCall(null);
        mainActivity.startAnnouncementsAsyncCall(null);
        mainActivity.startUserAsyncCall(mPointsView);

        // News Feed
        List<News> newsFeed = new ArrayList<News>();
        newsFeed.addAll(getRecentAnnouncements());
        newsFeed.addAll(getUpcomingEvents());
        Collections.sort(newsFeed, new Comparator<News>() {
            @Override
            public int compare(News lhs, News rhs) {
                Calendar date = new GregorianCalendar();
                date.set(Calendar.HOUR_OF_DAY, 0);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MILLISECOND, 0);
                long msToday = date.getTimeInMillis();
                long leftDistance = Math.abs(lhs.getRealDate().getTime() - msToday);
                long rightDistance = Math.abs(rhs.getRealDate().getTime() - msToday);
                return leftDistance > rightDistance ? 1 : -1; // sort by closest to today
            }
        });

        mNewsFeedListAdapter = new NewsFeedListAdapter(getActivity(), newsFeed);
        mNewsFeedListView = (ListView) rootView.findViewById(R.id.newsFeed);
        mNewsFeedListView.setAdapter(mNewsFeedListAdapter);

        return rootView;
    }

    /**
     * Return the 5 most recent announcements
     */
    public List<News> getRecentAnnouncements() {
        List<News> recentAnnouncements = new ArrayList<News>();
        JsonArray announcements = mSessionManager.getAnnouncements();
        for (int i = 0; i < announcements.size() && i < 10; i++) {
            JsonObject announcement = announcements.get(i).getAsJsonObject();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            try {
                date = format.parse(announcement.get("datePosted").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }
            SimpleDateFormat newsFormat = new SimpleDateFormat("MMM dd");
            String newsDate = newsFormat.format(date);
            recentAnnouncements.add(new News(announcement.get("content").getAsString(),
                    newsDate, "announcement", date));
        }
        return recentAnnouncements;
    }

    /**
     * Starting from now, return the 5 closest upcoming events
     */
    public List<News> getUpcomingEvents() {
        List<News> upcomingEvents = new ArrayList<News>();
        JsonArray jsonEvents = mSessionManager.getCalReq();
        List<Event> cancelled = new ArrayList<Event>();
        List<Event> events = EventManager.createEvents(jsonEvents, cancelled);

        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                return lhs.getStartDate().compareTo(rhs.getStartDate());
            }
        });

        for (int i = 0; i < events.size() && upcomingEvents.size() < 5; i++) {
            Event event = events.get(i);
            if (event.getStartDate().compareTo(new Date()) > 0) {
                upcomingEvents.add(
                    new News(
                        event.getSummary(),
                        EventManager.getDate(event) + " " + EventManager.getLocationTime(event),
                        "calendar", event.getStartDate())
                );
            }
        }
        return upcomingEvents;
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
}
