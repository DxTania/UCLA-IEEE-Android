package com.ucla_ieee.app.signin;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.user.SessionManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
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

    RegisterTask(RegisterActivity registerActivity, String email, String password, String firstname, String lastname,
     String major, String year) {
        this.registerActivity = registerActivity;
        mEmail = email;
        mPassword = password;
        mFirstName = firstname;
        mLastName = lastname;
        mMajor = major;
        mYear = year;
    }

    @Override
    protected String doInBackground(Void... params) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/users.php");

        List<NameValuePair> loginParams = new ArrayList<NameValuePair>();
        loginParams.add(new BasicNameValuePair("service", "register"));
        loginParams.add(new BasicNameValuePair("email", mEmail));
        loginParams.add(new BasicNameValuePair("password", mPassword));
        loginParams.add(new BasicNameValuePair("firstname", mFirstName));
        loginParams.add(new BasicNameValuePair("lastname", mLastName));
        loginParams.add(new BasicNameValuePair("major", mMajor));
        loginParams.add(new BasicNameValuePair("year", mYear));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(loginParams));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder builder = new StringBuilder();

                String st;
                while ((st = reader.readLine()) != null) {
                    builder.append(st).append("\n");
                }

                instream.close();
                return builder.toString();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        registerActivity.cancelRegister();

        if (TextUtils.isEmpty(response)) {
            Toast.makeText(registerActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
            registerActivity.finish();
            return;
        }

        JsonObject json;
        try {
            JsonParser parser = new JsonParser();
            json = (JsonObject) parser.parse(response);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
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
            Toast.makeText(registerActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
            registerActivity.finish();
        }
        registerActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCancelled() {
        registerActivity.cancelRegister();
    }
}
