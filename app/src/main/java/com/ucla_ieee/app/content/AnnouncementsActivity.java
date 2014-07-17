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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
        // TODO: Make an async call to download new announcements
        // TODO: cache announcements (and load these first)
        // TODO: Make a class to easily handle json
        // announcements.setAdapter();
        String eventJson = sessionManager.getAnnouncements();
//        if (!TextUtils.isEmpty(eventJson)) {
//            JsonArray json;
//            try {
//                JsonParser parser = new JsonParser();
//                json = (JsonArray) parser.parse(eventJson);
//            } catch (JsonSyntaxException e) {
//                e.printStackTrace();
//                return;
//            }
//            addAnnouncements(json);
//        }

        announcements.setAdapter(mListAdapter);

        AnnouncementsTask announcementsTask = new AnnouncementsTask(this);
        announcementsTask.execute((Void) null);
    }

    public void addAnnouncements(JsonArray events) {

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

    public class AnnouncementsTask extends AsyncTask<Void, Void, String> {
        private Context mParent;
        private SessionManager mSessionManager;
        private JsonServerUtil mUtil;

        public AnnouncementsTask(Context context) {
            mParent = context;
            mSessionManager = new SessionManager(mParent);
            mUtil = new JsonServerUtil();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://ieeebruins.org/membership_serve/announcements.php");
            HttpResponse response;
            try {
                response = httpClient.execute(httpGet);
            } catch (IOException e) {
                return null;
            }
            return mUtil.getStringFromServerResponse(response.getEntity());
        }

        @Override
        protected void onPostExecute(String response) {
            if (TextUtils.isEmpty(response)) {
                Toast.makeText(mParent, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.i("ANNOUNCEMENTS", response);
            // TODO: add announcements to adapter and update adapter
            JsonArray announcements = mUtil.getJsonArrayFromString(response);
            if (announcements == null) {
                return; // error
            }

            // TODO: change method in session manager to return a json array
            if (mSessionManager.getAnnouncements() == null) {
                mSessionManager.storeCalReq(announcements.toString());
            } else {
                JsonArray prevItems = mUtil.getJsonArrayFromString(mSessionManager.getAnnouncements());
                if (announcements.size() > 0 && prevItems != null) {
                    // Make sure we don't duplicate events
                    mSessionManager.setAnnouncements(announcements.toString());
                } else if (prevItems == null) {
                    // We don't have anything cached, store entire request
                } // Else no new announcements, leave stored req alone
            }
        }
    }
}
