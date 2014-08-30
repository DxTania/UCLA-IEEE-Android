package com.ucla_ieee.app.content;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.signin.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class AnnouncementsActivity extends Fragment {
    AnnouncementsListAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_announcements, container, false);

        ListView announcements = (ListView) rootView.findViewById(R.id.announcementsList);
        SessionManager sessionManager = new SessionManager(this.getActivity());

        mListAdapter = new AnnouncementsListAdapter(this.getActivity(), new ArrayList<Announcement>());
        updateAnnouncements(sessionManager.getAnnouncements());
        announcements.setAdapter(mListAdapter);

        AnnouncementsTask announcementsTask = new AnnouncementsTask(this);
        announcementsTask.execute((Void) null);

        return rootView;
    }

    public void updateAnnouncements(JsonArray announcements) {
        if (announcements != null) {
            // TODO: test that updated announcements work (if one of the last 10 or so updated)
            List<Announcement> announcementList = new ArrayList<Announcement>();
            for (JsonElement announcement : announcements) {
                String content = announcement.getAsJsonObject().get("content").getAsString();
                String date = announcement.getAsJsonObject().get("datePosted").getAsString();
                int id = announcement.getAsJsonObject().get("id").getAsInt(); // TODO: null pointer exception?
                if (didUpdate(new Announcement(content, date, id))) {
                    continue;
                }
                announcementList.add(new Announcement(content, date, id));
            }
            mListAdapter.addAll(announcementList);
            mListAdapter.notifyDataSetChanged();
        }
    }

    private boolean didUpdate(Announcement announcement) {
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            Announcement stored = mListAdapter.getItem(i);
            if (stored.getId() == announcement.getId()) {// && !stored.getContent().equals(announcement.getContent())) {
                stored.setContent(announcement.getContent());
//                stored.setDate("Updated!" + stored.getDate());
                return true;
            }
        }
        return false;
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
