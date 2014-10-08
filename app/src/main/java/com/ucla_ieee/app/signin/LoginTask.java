package com.ucla_ieee.app.signin;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
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
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class LoginTask extends AsyncTask<Void, Void, String> {

    private final String mEmail;
    private final String mPassword;
    private final TextView mPasswordView;
    private final TextView mEmailView;
    private final LoginActivity mActivity;
    private final JsonServerUtil mUtil;

    LoginTask(LoginActivity activity, String email, String password,
              TextView passwordView, TextView emailView) {
        mEmail = email;
        mPassword = password;
        mPasswordView = passwordView;
        mEmailView = emailView;
        mActivity = activity;
        mUtil = new JsonServerUtil();
    }

    @Override
    protected String doInBackground(Void... params) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/users.php");

        List<NameValuePair> loginParams = new ArrayList<NameValuePair>();
        loginParams.add(new BasicNameValuePair("service", "login"));
        loginParams.add(new BasicNameValuePair("email", mEmail));
        loginParams.add(new BasicNameValuePair("password", mPassword));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(loginParams));
            HttpResponse response = httpClient.execute(httpPost);
            return mUtil.getStringFromServerResponse(response.getEntity());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        mActivity.cancelLogin();

        JsonObject json = mUtil.getJsonObjectFromString(response);
        if (json == null) {
            Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (json.get("success") != null && json.get("success").getAsInt() == 1) {
            // log in user
            SessionManager sessionManager = new SessionManager(mActivity.getApplicationContext());
            sessionManager.loginUser(json.get("user").getAsJsonObject(), json.get("cookie").getAsString());

            // start main ieee activity
            Intent intent = new Intent(mActivity, MainActivity.class);
            mActivity.startActivity(intent);
            mActivity.finish();
            mActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            if (json.get("error_code").getAsInt() == 0) {
                mPasswordView.setError(mActivity.getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else if (json.get("error_code").getAsInt() == 1) {
                mEmailView.setError(mActivity.getString(R.string.error_incorrect_email));
                mEmailView.requestFocus();
            } else {
                Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCancelled() {
        mActivity.cancelLogin();
    }
}
