package com.ucla_ieee.app.content;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.*;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.calendar.EventManager;
import com.ucla_ieee.app.content.AnnouncementsListAdapter;
import com.ucla_ieee.app.signin.SessionManager;
import com.ucla_ieee.app.util.JsonServerUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
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
