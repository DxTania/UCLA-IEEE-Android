package com.ucla_ieee.app.newsfeed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.calendar.Event;
import com.ucla_ieee.app.calendar.EventManager;
import com.ucla_ieee.app.user.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FrontPageFragment extends Fragment {

    private SessionManager mSessionManager;
    private TextView mPointsView;
    private NewsFeedListAdapter mNewsFeedListAdapter;
    private ListView mNewsFeedListView;
    private View mProgressView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_front_page, container, false);

        mPointsView = (TextView) rootView.findViewById(R.id.numPoints);
        mSessionManager = new SessionManager(getActivity());
        mPointsView.setText(String.valueOf(mSessionManager.getInt(SessionManager.Keys.POINTS)) + "/"
                + String.valueOf(mSessionManager.getInt(SessionManager.Keys.TOTAL_POINTS)));
        mProgressView = rootView.findViewById(R.id.refresh_process);

        final MainActivity mainActivity = (MainActivity) getActivity();

        // Refresh button
        ImageButton refreshButton = (ImageButton) rootView.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewsFeedListView.smoothScrollToPosition(0);
                showProgress(true);
                mainActivity.getTaskManager().startAnnouncementsAsyncCall(null);
                mainActivity.getTaskManager().startCalendarAsyncCall(null);
                mainActivity.getTaskManager().startUserAsyncCall(true);
            }
        });

        // News Feed
        List<News> newsFeed = new ArrayList<News>();
        newsFeed.addAll(getRecentAnnouncements());
        newsFeed.addAll(getUpcomingEvents());

        mNewsFeedListAdapter = new NewsFeedListAdapter(getActivity(), newsFeed);
        mNewsFeedListView = (ListView) rootView.findViewById(R.id.newsFeed);
        mNewsFeedListView.setAdapter(mNewsFeedListAdapter);

        if (mainActivity.loading) {
            showProgress(true);
        }

        return rootView;
    }

    public void updatePoints() {
        mPointsView.setText(String.valueOf(mSessionManager.getInt(SessionManager.Keys.POINTS)) + "/"
                + String.valueOf(mSessionManager.getInt(SessionManager.Keys.TOTAL_POINTS)));
    }

    /**
     * Updates news feed with most recently retrieved announcements and events
     */
    public void updateNews() {
        mNewsFeedListAdapter.clear();
        List<News> news = getRecentAnnouncements();
        news.addAll(getUpcomingEvents());
        mNewsFeedListAdapter.addAll(news);
    }

    /**
     * Return the 5 most recent announcements
     */
    private List<News> getRecentAnnouncements() {
        List<News> recentAnnouncements = new ArrayList<News>();
        JsonArray announcements = mSessionManager.getJsonArray(SessionManager.Keys.ANNOUNCEMENTS);
        if (announcements != null) {
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
                        newsDate, "", "announcement", date));
            }
        }
        return recentAnnouncements;
    }

    /**
     * Starting from now, return the 5 closest upcoming events
     */
    private List<News> getUpcomingEvents() {
        List<News> upcomingEvents = new ArrayList<News>();
        JsonArray jsonEvents = mSessionManager.getJsonArray(SessionManager.Keys.CALENDAR);
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
                upcomingEvents.add(event.getAsNews());
            }
        }
        return upcomingEvents;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
