package com.ucla_ieee.app.content;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.user.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class AnnouncementsFragment extends Fragment {
    AnnouncementsListAdapter mListAdapter;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_announcements, container, false);

        ListView announcements = (ListView) rootView.findViewById(R.id.announcementsList);
        SessionManager sessionManager = new SessionManager(this.getActivity());

        mainActivity = (MainActivity) getActivity();
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
                announcementList.add(new Announcement(announcement.get("unread").getAsBoolean(),
                        announcement.get("content").getAsString(),
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
        mainActivity.finishAnnouncementsTask();
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
            mainActivity.startAnnouncementsAsyncCall(this);
            Toast.makeText(getActivity(), "Updating announcements...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
