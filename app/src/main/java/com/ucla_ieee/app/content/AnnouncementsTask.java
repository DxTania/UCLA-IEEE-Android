package com.ucla_ieee.app.content;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.signin.SessionManager;
import com.ucla_ieee.app.util.JsonServerUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class AnnouncementsTask extends AsyncTask<Void, Void, String> {
    private MainActivity mParent;
    private SessionManager mSessionManager;
    private JsonServerUtil mUtil;

    public AnnouncementsTask(Context context) {
        mParent = (MainActivity) context;
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
            Toast.makeText(mParent, "Couldn't load announcements", Toast.LENGTH_SHORT).show();
            return;
        }

        // We only care about the latest 10 or so, don't bother
        if (announcements.size() > 0) {
            JsonArray oldAnnouncements = mSessionManager.getAnnouncements();
            for (int j = 0; j < announcements.size(); j++) {
                JsonObject announcement = announcements.get(j).getAsJsonObject();
                for (int i = 0; i < oldAnnouncements.size(); i++) {
                    JsonObject oldAnnouncement = oldAnnouncements.get(i).getAsJsonObject();
                    if (!announcement.get("content").getAsString().equals(oldAnnouncement.get("content").getAsString())
                            && announcement.get("id").getAsInt() == oldAnnouncement.get("id").getAsInt()) {
                        announcement.addProperty("content", announcement.get("content").getAsString());
                        announcement.addProperty("datePosted", "UPDATED! " + announcement.get("datePosted").getAsString
                                ());
                        Toast.makeText(mParent, "New Announcements!", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            mSessionManager.setAnnouncements(announcements.toString());
            if (mParent.getAnnouncementsActivity() != null) {
                mParent.getAnnouncementsActivity().updateAnnouncements(announcements);
            }
        }

        mParent.setAnnouncementsAsyncTaskNull();
    }
}