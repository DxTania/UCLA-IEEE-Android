package com.ucla_ieee.app.signin;

import android.content.Context;
import android.os.AsyncTask;
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
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class ForgotPasswordTask extends AsyncTask<Void, Void, String> {

    private final String mEmail;
    private final JsonServerUtil mUtil;
    private final Context mContext;

    ForgotPasswordTask(Context context, String email) {
        mEmail = email;
        mUtil = new JsonServerUtil();
        mContext = context;
    }

    @Override
    protected String doInBackground(Void... params) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/users.php");

        List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        postParams.add(new BasicNameValuePair("service", "forgot_password"));
        postParams.add(new BasicNameValuePair("email", mEmail));

        HttpResponse response;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postParams));
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            return null;
        }

        return mUtil.getStringFromServerResponse(response.getEntity());
    }

    @Override
    protected void onPostExecute(String response) {
        JsonObject responseJson = mUtil.getJsonObjectFromString(response);
        if (responseJson == null) {
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (responseJson.get("success") != null && responseJson.get("success").getAsInt() == 1) {
            Toast.makeText(mContext, "An email with a temporary password has been sent to the email on file",
                    Toast.LENGTH_LONG).show();
        } else {
            if (responseJson.get("error_message") != null) {
                Toast.makeText(mContext, responseJson.get("error_message").getAsString(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Something went really wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
