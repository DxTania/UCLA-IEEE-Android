package com.ucla_ieee.app.signin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
import org.w3c.dom.Text;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ProfileActivity extends Activity {
    private MembershipEditTask mAuthTask = null;

    EditText email, name, memberId, password;
    TextView emailText, nameText, memberIdText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("My Membership");

        final SessionManager sessionManager = new SessionManager(this);

        email = (EditText) findViewById(R.id.memberEmail);
        emailText = (TextView) findViewById(R.id.memberEmailText);
        email.setText(sessionManager.getEmail());
        emailText.setText(sessionManager.getEmail());

        name = (EditText) findViewById(R.id.memberName);
        nameText = (TextView) findViewById(R.id.memberNameText);
        name.setText(sessionManager.getName());
        nameText.setText(sessionManager.getName());

        memberId = (EditText) findViewById(R.id.memberId);
        memberIdText = (TextView) findViewById(R.id.memberIdText);
        memberId.setText(sessionManager.getIEEEId());
        memberIdText.setText(sessionManager.getIEEEId());

        password = (EditText) findViewById(R.id.password);
        passwordText = (TextView) findViewById(R.id.passwordText);

        ImageButton aboutNextReward = (ImageButton) findViewById(R.id.aboutNextReward);
        aboutNextReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.nextRewardHint);
                ll.setVisibility(ll.getVisibility() == View.VISIBLE? View.GONE : View.VISIBLE);
            }
        });

        final ImageButton changeEmail = (ImageButton) findViewById(R.id.editEmail);
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibility(email, emailText, false);
                email.requestFocus();
            }
        });

        ImageButton changeName = (ImageButton) findViewById(R.id.editName);
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibility(name, nameText, false);
                name.requestFocus();
            }
        });

        ImageButton changeId = (ImageButton) findViewById(R.id.editMembershipId);
        changeId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibility(memberId, memberIdText, false);
                memberId.requestFocus();
            }
        });

        ImageButton changePassword = (ImageButton) findViewById(R.id.changePassword);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibility(password, passwordText, false);
                password.requestFocus();
            }
        });

        Button saveChanges = (Button) findViewById(R.id.saveChanges);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send post request
                mAuthTask = new MembershipEditTask(sessionManager.getEmail(), sessionManager.getCookie());

                // TODO: email, password, name, id basic name value pairs if value has changed!
                mAuthTask.execute(email.getText().toString());
                toggleOffEdits();
            }
        });

        // TODO: Populate events list
        ListView attendedEvents = (ListView) findViewById(R.id.attendedEventList);
        TextView noEvents = (TextView) findViewById(R.id.noEventsText);
        noEvents.setVisibility(View.VISIBLE);
    }

    public void toggleOffEdits() {
        toggleVisibility(email, emailText, true);
        toggleVisibility(name, nameText, true);
        toggleVisibility(memberId, memberIdText, true);
    }

    public void toggleVisibility(EditText editText, TextView textView, boolean forceText) {
        if (editText.getVisibility() == View.VISIBLE || forceText) {
            editText.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(editText.getText());
        } else {
            editText.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends a request to server to authorize and change member stats
     */
    public class MembershipEditTask extends AsyncTask<String, Void, String> {

        private final String mEmail;
        private final String mCookie;

        MembershipEditTask(String email, String cookie) {
            mEmail = email;
            mCookie = cookie;
        }

        @Override
        protected String doInBackground(String... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/test.php");

            List<NameValuePair> editParams = new ArrayList<NameValuePair>();
            editParams.add(new BasicNameValuePair("service", "edit_member"));
            editParams.add(new BasicNameValuePair("email", mEmail));
            editParams.add(new BasicNameValuePair("cookie", mCookie));

            // TODO: change this so String... => basic name value pairs...

            if (!TextUtils.isEmpty(params[0])) {
                editParams.add(new BasicNameValuePair("newEmail", params[0]));
            }

//            if (!TextUtils.isEmpty(params[1])) {
//                editParams.add(new BasicNameValuePair("newPassword", params[1]));
//            }
//
//            if (!TextUtils.isEmpty(params[2])) {
//                editParams.add(new BasicNameValuePair("newName", params[2]));
//            }
//
//            if (!TextUtils.isEmpty(params[3])) {
//                editParams.add(new BasicNameValuePair("newId", params[3]));
//            }

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(editParams));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

            try {
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    InputStream instream = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                    StringBuilder builder = new StringBuilder();

                    String st;
                    while((st = reader.readLine()) != null) {
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
            // update user in session
            mAuthTask = null;

            if (TextUtils.isEmpty(response)) {
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject json;
            try {
                JsonParser parser = new JsonParser();
                json = (JsonObject) parser.parse(response);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (json.get("success") != null && json.get("success").getAsInt() == 1) {
                // TODO: Display profile? Welcome, name
                SessionManager sessionManager = new SessionManager(getApplicationContext());
                sessionManager.updateSession(json.get("email").getAsString());
                Toast.makeText(ProfileActivity.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: dissect error
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
