package com.ucla_ieee.app.content;

import android.content.Context;
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

    public AnnouncementsTask(Context context) {
        mParent = (AnnouncementsActivity) context;
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
        JsonArray announcements = mUtil.getJsonArrayFromString(response);
        if (announcements == null) {
            Toast.makeText(mParent, "Something went wrong", Toast.LENGTH_SHORT).show();
            return; // error
        }

        JsonArray prevItems = mSessionManager.getAnnouncements();
        if (prevItems == null) {
            mSessionManager.setAnnouncements(announcements.toString());
        } else if (announcements.size() > 0) {
            // TODO: Make sure we don't duplicate announcements (if they are udpated)
            // and shouldn't overwrite?? or should it?
            mSessionManager.setAnnouncements(announcements.toString());
            mParent.addAnnouncements(announcements);
        } // Else no new announcements, leave stored req alone
    }
}