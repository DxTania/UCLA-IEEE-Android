package com.ucla_ieee.app.user;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Sends a request to server to authorize and change member stats
 */
public class MembershipEditTask extends AsyncTask<List<BasicNameValuePair>, Void, String> {

    private final String mEmail;
    private final String mCookie;
    private final MembershipFragment mContext;
    private SessionManager mSessionManager;
    private TextView mTextView;
    private JsonServerUtil mUtil;

    public MembershipEditTask(Fragment context, TextView passwordText) {
        mContext = (MembershipFragment) context;
        mSessionManager = new SessionManager(mContext.getActivity());
        mEmail = mSessionManager.getString(SessionManager.Keys.EMAIL);
        mCookie = mSessionManager.getString(SessionManager.Keys.COOKIE);
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
            Toast.makeText(mContext.getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (json.get("success") != null && json.get("success").getAsInt() == 1) {
            mSessionManager.updateSession(json.getAsJsonObject("user"));
            mTextView.setText("");
            Toast.makeText(mContext.getActivity(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            if (json.get("error_message") != null) {
                Toast.makeText(mContext.getActivity(), json.get("error_message").getAsString(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext.getActivity(), "Something went really wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
