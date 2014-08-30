package com.ucla_ieee.app.content;

import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.ucla_ieee.app.signin.SessionManager;
import com.ucla_ieee.app.util.JsonServerUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class AnnouncementsTask extends AsyncTask<Void, Void, String> {
    private AnnouncementsActivity mParent;
    private SessionManager mSessionManager;
    private JsonServerUtil mUtil;

    public AnnouncementsTask(AnnouncementsActivity context) {
        mParent = context;
        mSessionManager = new SessionManager(mParent.getActivity());
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
        JsonArray announcements = mUtil.getJsonArrayFromString(response);
        if (announcements == null) {
            Toast.makeText(mParent.getActivity(), "Couldn't load announcements", Toast.LENGTH_SHORT).show();
            return;
        }

        // We only care about the latest 10 or so, don't bother
        if (announcements.size() > 0) {
            mSessionManager.setAnnouncements(announcements.toString());
            mParent.updateAnnouncements(announcements);
        }
    }
}