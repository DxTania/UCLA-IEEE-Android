package com.ucla_ieee.app.content;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.user.SessionManager;
import com.ucla_ieee.app.util.JsonServerUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class AnnouncementsTask extends AsyncTask<Void, Void, String> {
    private MainActivity mContext;
    private SessionManager mSessionManager;
    private JsonServerUtil mUtil;

    public AnnouncementsTask(Context context) {
        mContext = (MainActivity) context;
        mSessionManager = new SessionManager(mContext);
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
            Toast.makeText(mContext, "Couldn't load announcements", Toast.LENGTH_SHORT).show();
            mContext.finishAnnouncementsTask();
            return;
        }

        if (announcements.size() > 0) {
            JsonArray oldAnnouncements = mSessionManager.getAnnouncements();
            int lastId = 0; // so we can still mark things as unread, give old announcements length of 0
            if (oldAnnouncements == null) {
                oldAnnouncements = new JsonArray();
            }
            for (int i = 0; i < oldAnnouncements.size(); i++) {
                JsonObject oldAnnouncement = oldAnnouncements.get(i).getAsJsonObject();
                if (oldAnnouncement.get("id").getAsInt() > lastId) {
                    lastId = oldAnnouncement.get("id").getAsInt();
                }
            }
            for (int j = 0; j < announcements.size(); j++) {
                JsonObject announcement = announcements.get(j).getAsJsonObject();
                announcement.addProperty("unread", false);
                for (int k = 0; k < oldAnnouncements.size(); k++) {
                    JsonObject oldAnnouncement = oldAnnouncements.get(k).getAsJsonObject();
                    if (!announcement.get("content").getAsString().equals(oldAnnouncement.get("content").getAsString())
                            && announcement.get("id").getAsInt() == oldAnnouncement.get("id").getAsInt()) {
                        // Updated announcement override (mark as unread)
                        announcement.addProperty("unread", true);
                    }
                }
                if (announcement.get("id").getAsInt() > lastId) {
                    // mark as unread
                    announcement.addProperty("unread", true);
                }
            }

            mSessionManager.storeAnnouncements(announcements.toString());
            if (mContext.getAnnouncementsActivity() != null) {
                mContext.getAnnouncementsActivity().updateAnnouncements(announcements);
            }
        }

        mContext.finishAnnouncementsTask();
    }
}