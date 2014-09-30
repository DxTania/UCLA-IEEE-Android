package com.ucla_ieee.app.signin;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
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
 * Sends a request to server to authorize and change member stats
 */
public class UpdateTask extends AsyncTask<Void, Void, String> {

    private final MainActivity mContext;
    private SessionManager mSessionManager;
    private JsonServerUtil mUtil;

    public UpdateTask(Context context) {
        mContext = (MainActivity) context;
        mSessionManager = new SessionManager(mContext);
        mUtil = new JsonServerUtil();
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d("DEBUGZ", "Starting async task");

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/users.php");

        List<NameValuePair> getParams = new ArrayList<NameValuePair>();
        getParams.add(new BasicNameValuePair("service", "get_user"));
        getParams.add(new BasicNameValuePair("email", mSessionManager.getEmail()));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(getParams));
            HttpResponse response = httpClient.execute(httpPost);
            return mUtil.getStringFromServerResponse(response.getEntity());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        Log.d("DEBUGZ", "Finishing async task");
        JsonObject json = mUtil.getJsonObjectFromString(response);
        if (json == null) {
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (json.get("success") != null && json.get("success").getAsInt() == 1) {

            JsonObject user = json.getAsJsonObject("user");
            String email, name, id;
            int points;
            email = user.get("email").getAsString();
            name = user.get("name").getAsString();
            id = user.get("ieee_id").getAsString();
            points = user.get("points").getAsInt();

            mSessionManager.updateSession(email, name, id, points);

        } else {
            // TODO: dissect more errors
            if (json.get("error_message") != null) {
                Toast.makeText(mContext, json.get("error_message").getAsString(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Something when really wrong", Toast.LENGTH_SHORT).show();
            }
        }

        mContext.setUpdateUserTaskNull();

        if (mContext.currentTag.equals(MainActivity.MAIN_TAG) || mContext.currentTag.equals(MainActivity.PROFILE_TAG)) {
            mContext.doFragment(mContext.currentTag, true);
        }
    }
}
