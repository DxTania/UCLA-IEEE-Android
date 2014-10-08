package com.ucla_ieee.app.signin;

import android.content.Intent;
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
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class RegisterTask extends AsyncTask<Void, Void, String> {

    private final RegisterActivity registerActivity;
    private final String mEmail;
    private final String mPassword;
    private final String mFirstName;
    private final String mLastName;
    private final String mMajor;
    private final String mYear;
    private final JsonServerUtil mUtil;

    RegisterTask(RegisterActivity registerActivity, String email, String password, String firstname, String lastname,
     String major, String year) {
        this.registerActivity = registerActivity;
        mEmail = email;
        mPassword = password;
        mFirstName = firstname;
        mLastName = lastname;
        mMajor = major;
        mYear = year;
        mUtil = new JsonServerUtil();
    }

    @Override
    protected String doInBackground(Void... params) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/users.php");

        List<NameValuePair> registerParams = new ArrayList<NameValuePair>();
        registerParams.add(new BasicNameValuePair("service", "register"));
        registerParams.add(new BasicNameValuePair("email", mEmail));
        registerParams.add(new BasicNameValuePair("password", mPassword));
        registerParams.add(new BasicNameValuePair("firstname", mFirstName));
        registerParams.add(new BasicNameValuePair("lastname", mLastName));
        registerParams.add(new BasicNameValuePair("major", mMajor));
        registerParams.add(new BasicNameValuePair("year", mYear));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(registerParams));
            HttpResponse response = httpClient.execute(httpPost);
            return mUtil.getStringFromServerResponse(response.getEntity());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        registerActivity.cancelRegister();

        JsonObject json = mUtil.getJsonObjectFromString(response);
        if (json == null) {
            Toast.makeText(registerActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (json.get("success") != null && json.get("success").getAsInt() == 1) {
            // Welcome user
            String firstName = json.get("user").getAsJsonObject().get("name").getAsString();
            Toast.makeText(registerActivity, "Welcome to IEEE, " + firstName + "!", Toast.LENGTH_SHORT).show();

            // Log in user
            SessionManager sessionManager = new SessionManager(registerActivity.getApplicationContext());
            sessionManager.loginUser(json.get("user").getAsJsonObject(), json.get("cookie").getAsString());

            // Start main IEEE activity, clearing logout activity
            Intent intent = new Intent(registerActivity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            registerActivity.startActivity(intent);
            registerActivity.finish();
        } else {
            if (json.get("error_message") != null) {
                Toast.makeText(registerActivity, json.get("error_message").getAsString(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(registerActivity, "Something went really wrong", Toast.LENGTH_SHORT).show();
            }
            registerActivity.finish();
        }
        registerActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCancelled() {
        registerActivity.cancelRegister();
    }
}
