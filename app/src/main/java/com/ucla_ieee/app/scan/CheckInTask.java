package com.ucla_ieee.app.scan;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.signin.SessionManager;
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
 */
public class CheckInTask extends AsyncTask<Void, Void, String> {

    private final SessionManager mSessionManager;
    private final Context mContext;
    private final JsonServerUtil mUtil;
    private final String mEventId;

    public CheckInTask(Context context, String eventId) {
        mSessionManager = new SessionManager(context);
        mContext = context;
        mUtil = new JsonServerUtil();
        mEventId = eventId;
    }

    @Override
    protected String doInBackground(Void... params) {

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
            return;
        }

        if (json.get("success").getAsInt() == 1) {
            // TODO: update attended events list (in session manager?)
            Toast.makeText(mContext, "Thanks for checking in!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, json.get("error_message").getAsString(), Toast.LENGTH_SHORT).show();
        }
    }
}