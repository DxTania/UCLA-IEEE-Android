package com.ucla_ieee.app.scan;

import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.user.SessionManager;
import com.ucla_ieee.app.util.JsonServerUtil;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks in user to event indicated by id in QR code (eventId)
 */
public class CheckInScanTask extends AsyncTask<Void, Void, String> {

    private final SessionManager mSessionManager;
    private final MainActivity mContext;
    private final JsonServerUtil mUtil;
    private final String mEventId;

    public CheckInScanTask(MainActivity context, String eventId) {
        mSessionManager = new SessionManager(context);
        mContext = context;
        mUtil = new JsonServerUtil();
        mEventId = eventId;
    }

    @Override
    protected String doInBackground(Void... params) {
        // TODO: Verify checking in with GPS (send coords to php or get coords from php?)

        HttpClient httpClient = new DefaultHttpClient();

        List<NameValuePair> checkInParams = new ArrayList<NameValuePair>();
        checkInParams.add(new BasicNameValuePair("service", "check_in"));
        checkInParams.add(new BasicNameValuePair("cookie", mSessionManager.getCookie()));
        checkInParams.add(new BasicNameValuePair("email", mSessionManager.getEmail()));
        checkInParams.add(new BasicNameValuePair("eventId", mEventId));

        HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/users.php");

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(checkInParams));
            HttpResponse response = httpClient.execute(httpPost);
            return mUtil.getStringFromServerResponse(response.getEntity());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        JsonObject json = mUtil.getJsonObjectFromString(response);
        if (json == null) {
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            mContext.getTaskManager().finishCheckInTask();
            return;
        }

        if (json.get("success").getAsInt() == 1) {
            Toast.makeText(mContext, "Thanks for checking in!", Toast.LENGTH_SHORT).show();
            mSessionManager.updateSession(json.getAsJsonObject("user"));
            mSessionManager.addAttendedEvent(json.get("event").getAsJsonObject());

        } else {
            if (json.get("error_message") != null) {
                Toast.makeText(mContext, json.get("error_message").getAsString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Something went seriously wrong", Toast.LENGTH_SHORT).show();
            }
        }

        mContext.getTaskManager().finishCheckInTask();
    }
}