package com.ucla_ieee.app.content;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.signin.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class AnnouncementsActivity extends Fragment {
    AnnouncementsListAdapter mListAdapter;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_announcements, container, false);

        mainActivity = (MainActivity) getActivity();
        mainActivity.startAnnouncementsAsyncCall(this);

        ListView announcements = (ListView) rootView.findViewById(R.id.announcementsList);
        SessionManager sessionManager = new SessionManager(this.getActivity());

        mListAdapter = new AnnouncementsListAdapter(this.getActivity(), new ArrayList<Announcement>());
        updateAnnouncements(sessionManager.getAnnouncements());
        announcements.setAdapter(mListAdapter);

        return rootView;
    }

    public void updateAnnouncements(JsonArray announcements) {
        if (announcements != null) {
            List<Announcement> announcementList = new ArrayList<Announcement>();
            for (int i = 0; i < announcements.size(); i++) {
                JsonObject announcement = announcements.get(i).getAsJsonObject();
                announcementList.add(new Announcement(announcement.get("content").getAsString(),
                        announcement.get("datePosted").getAsString(), announcement.get("id").getAsInt()));
            }
            mListAdapter.clear();
            mListAdapter.addAll(announcementList);
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mainActivity.setAnnouncementsAsyncTaskNull();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
}
