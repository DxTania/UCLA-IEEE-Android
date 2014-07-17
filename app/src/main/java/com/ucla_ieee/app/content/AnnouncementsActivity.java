package com.ucla_ieee.app.content;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.signin.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class AnnouncementsActivity extends Activity {
    AnnouncementsListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);
        setTitle("Announcements");

        ListView announcements = (ListView) findViewById(R.id.announcementsList);
        SessionManager sessionManager = new SessionManager(this);

        mListAdapter = new AnnouncementsListAdapter(this, new ArrayList<Announcement>());

        // TODO: cache announcements (and load these first).. max?
        addAnnouncements(sessionManager.getAnnouncements());
        announcements.setAdapter(mListAdapter);

        AnnouncementsTask announcementsTask = new AnnouncementsTask(this);
        announcementsTask.execute((Void) null);
    }

    public void addAnnouncements(JsonArray announcements) {
        // TODO: Don't just clear them...?
        if (announcements != null) {
            // only add new announcements?
            List<Announcement> announcementList = new ArrayList<Announcement>();
            for (JsonElement announcement : announcements) {
                String content = announcement.getAsJsonObject().get("content").getAsString();
                String date = announcement.getAsJsonObject().get("datePosted").getAsString();
                // int uid = announcement.getAsJsonObject().get("uid").getAsInt();
                announcementList.add(new Announcement(content, date, 0));
            }
            mListAdapter.clear();
            mListAdapter.addAll(announcementList);
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.announcements, menu);
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
}
