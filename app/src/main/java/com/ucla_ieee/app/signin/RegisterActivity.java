package com.ucla_ieee.app.signin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
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


public class RegisterActivity extends Activity {
    private RegisterTask mAuthTask = null;
    private LinearLayout registerForm;
    private ProgressBar registerProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle("Register");

        final EditText emailView = (EditText) findViewById(R.id.reg_email);
        final EditText passwordView = (EditText) findViewById(R.id.reg_password);
        final EditText fnameView = (EditText) findViewById(R.id.reg_firstName);
        final EditText lnameView = (EditText) findViewById(R.id.reg_lastName);
        final EditText rpasswordView = (EditText) findViewById(R.id.retype_password);
        registerForm = (LinearLayout) findViewById(R.id.register_form);
        registerProgress = (ProgressBar) findViewById(R.id.register_progress);

        emailView.setText(getIntent().getStringExtra("email"));

        Button register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailView.getText().toString();
                String password = passwordView.getText().toString();
                String rpassword = rpasswordView.getText().toString();
                String firstname = fnameView.getText().toString();
                String lastname = lnameView.getText().toString();


                if (!rpassword.equals(password)) {
                    rpasswordView.setError("Passwords don't match!");
                    rpasswordView.requestFocus();
                    return;
                }

                if (!LoginActivity.isPasswordValid(password)) {
                    passwordView.setError("Passwords must be at least 4 char long and include at " +
                            "least one number");
                    passwordView.requestFocus();
                    return;
                }

                showProgress(true);
                mAuthTask = new RegisterTask(email, password, firstname, lastname);
                mAuthTask.execute((Void) null);
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            registerForm.setVisibility(show ? View.GONE : View.VISIBLE);
            registerForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registerForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            registerProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            registerProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registerProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            registerProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            registerForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RegisterTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;
        private final String mFirstName;
        private final String mLastName;

        RegisterTask(String email, String password, String firstname, String lastname) {
            mEmail = email;
            mPassword = password;
            mFirstName = firstname;
            mLastName = lastname;
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
            mAuthTask = null;
            showProgress(false);

            if (TextUtils.isEmpty(response)) {
                Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            JsonObject json;
            try {
                JsonParser parser = new JsonParser();
                json = (JsonObject) parser.parse(response);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (json.get("success") != null && json.get("success").getAsInt() == 1) {
                // Welcome user
                String firstName = json.get("user").getAsJsonObject().get("name").getAsString();
                Toast.makeText(RegisterActivity.this, "Welcome to IEEE, " + firstName + "!", Toast.LENGTH_SHORT).show();

                // Log in user
                SessionManager sessionManager = new SessionManager(getApplicationContext());
                sessionManager.loginUser(json.get("user").getAsJsonObject(), json.get("cookie").getAsString());

                // Start main IEEE activity, clearing logout activity
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
