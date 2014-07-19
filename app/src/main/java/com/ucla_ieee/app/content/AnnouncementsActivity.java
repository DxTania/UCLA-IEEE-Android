package com.ucla_ieee.app.content;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.signin.SessionManager;
import com.ucla_ieee.app.util.FadeActivity;

import java.util.ArrayList;
import java.util.List;


public class AnnouncementsActivity extends FadeActivity {
    AnnouncementsListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);
        setTitle("Announcements");

        ListView announcements = (ListView) findViewById(R.id.announcementsList);
        SessionManager sessionManager = new SessionManager(this);

        mListAdapter = new AnnouncementsListAdapter(this, new ArrayList<Announcement>());
        updateAnnouncements(sessionManager.getAnnouncements());
        announcements.setAdapter(mListAdapter);

        AnnouncementsTask announcementsTask = new AnnouncementsTask(this);
        announcementsTask.execute((Void) null);
    }

    public void updateAnnouncements(JsonArray announcements) {
        if (announcements != null) {
            // TODO: test updated announcements work
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
            if (stored.getId() == announcement.getId()) {
                stored.setContent(announcement.getContent());
                stored.setDate("Updated!" + stored.getDate());
                return true;
            }
        }
        return false;
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
