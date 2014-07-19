package com.ucla_ieee.app.signin;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.util.JsonServerUtil;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends a request to server to authorize and change member stats
 */
public class MembershipEditTask extends AsyncTask<List<BasicNameValuePair>, Void, String> {

    private final String mEmail;
    private final String mCookie;
    private final ProfileActivity mContext;
    private SessionManager mSessionManager;
    private TextView mTextView;
    private JsonServerUtil mUtil;

    MembershipEditTask(Context context, TextView passwordText) {
        mContext = (ProfileActivity) context;
        mSessionManager = new SessionManager(mContext);
        mEmail = mSessionManager.getEmail();
        mCookie = mSessionManager.getCookie();
        mTextView = passwordText;
        mUtil = new JsonServerUtil();
    }

    @Override
    protected String doInBackground(List<BasicNameValuePair>... params) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/users.php");

        List<NameValuePair> editParams = new ArrayList<NameValuePair>();
        editParams.add(new BasicNameValuePair("service", "edit_member"));
        editParams.add(new BasicNameValuePair("email", mEmail));
        editParams.add(new BasicNameValuePair("cookie", mCookie));

        // Add everything we are changing
        List<BasicNameValuePair> pairs = params[0];
        editParams.addAll(pairs);

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(editParams));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        try {
            HttpResponse response = httpClient.execute(httpPost);
            return mUtil.getStringFromServerResponse(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        JsonObject json = mUtil.getJsonObjectFromString(response);
        if (json == null) {
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (json.get("success") != null && json.get("success").getAsInt() == 1) {

            String email, name, id;
            email = json.get("email").getAsString();
            name = json.get("name").getAsString();
            id = json.get("ieee_id").getAsString();

            mTextView.setText("");

            mSessionManager.updateSession(email, name, id);
            Toast.makeText(mContext, "Changes saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            // TODO: dissect more errors
            if (json.get("error_code") != null && json.get("error_code").getAsInt() == 0) {
                Toast.makeText(mContext, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
