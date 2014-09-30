package com.ucla_ieee.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.ucla_ieee.app.signin.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MainPageFragment extends Fragment {

    private SessionManager sessionManager;
    private View rootView;
    private TextView mPointsView;
    private NewsFeedListAdapter mNewsFeedListAdapter;
    private ListView mNewsFeedListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPointsView = (TextView) rootView.findViewById(R.id.numPoints);
        sessionManager = new SessionManager(getActivity());
        mPointsView.setText(String.valueOf(sessionManager.getPoints()));

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.startCalendarAsyncCall(null);
        mainActivity.startUserAsyncCall(mPointsView);
        mainActivity.startAnnouncementsAsyncCall(null);

        // News Feed
        List<News> newsFeed = new ArrayList<News>();
        newsFeed.add(new News("Content", "Date2", "announcement"));
        newsFeed.add(new News("Content", "Date", "calendar"));
        mNewsFeedListAdapter = new NewsFeedListAdapter(getActivity(), newsFeed);

        mNewsFeedListView = (ListView) rootView.findViewById(R.id.newsFeed);
        mNewsFeedListView.setAdapter(mNewsFeedListAdapter);

        return rootView;
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
