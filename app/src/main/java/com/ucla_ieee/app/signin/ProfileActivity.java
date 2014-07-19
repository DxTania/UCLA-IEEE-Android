package com.ucla_ieee.app.signin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.util.FadeActivity;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends FadeActivity {
    private MembershipEditTask mAuthTask = null;

    private EditText email, name, memberId;
    private TextView emailText, nameText, memberIdText, passwordText;
    private Boolean alertReady = false;
    private String cachePassword = "";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("My Membership");

        sessionManager = new SessionManager(this);

        setUpViews();
        setUpImageButtons();
        setUpSaveChanges();

        // TODO: Populate events list, get events since day of most recent event they attended
        // TODO: QR Codes should be the id of the calendar event. SHould we verify checking in with GPS?
        ListView attendedEvents = (ListView) findViewById(R.id.attendedEventList);
        TextView noEvents = (TextView) findViewById(R.id.noEventsText);
        noEvents.setVisibility(View.VISIBLE);
    }

    // Click listener for save changes button
    private void setUpSaveChanges() {
        Button saveChanges = (Button) findViewById(R.id.saveChanges);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send post request
                mAuthTask = new MembershipEditTask(ProfileActivity.this, passwordText);

                String newEmail, newName, newId, newPassword;
                newEmail = email.getText().toString();
                newName = name.getText().toString();
                newId = memberId.getText().toString();
                newPassword = passwordText.getText().toString();

                List<BasicNameValuePair> valuePairs = new ArrayList<BasicNameValuePair>();
                if (!newEmail.equals(sessionManager.getEmail())) {
                    valuePairs.add(new BasicNameValuePair("newEmail", newEmail));
                }
                if (!newName.equals(sessionManager.getName())) {
                    valuePairs.add(new BasicNameValuePair("newName", newName));
                }
                if (!newId.equals(sessionManager.getIEEEId())) {
                    valuePairs.add(new BasicNameValuePair("newId", newId));
                }
                if (!TextUtils.isEmpty(newPassword)) {
                    valuePairs.add(new BasicNameValuePair("newPassword", newPassword));
                    valuePairs.add(new BasicNameValuePair("password", cachePassword));
                }

                if (valuePairs.size() == 0) {
                    Toast.makeText(ProfileActivity.this, "Nothing to change", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuthTask.execute(valuePairs);
                toggleOffEdits();
            }
        });
    }

    // Click listeners for image buttons
    private void setUpImageButtons() {
        ImageButton aboutNextReward = (ImageButton) findViewById(R.id.aboutNextReward);
        aboutNextReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.nextRewardHint);
                ll.setVisibility(ll.getVisibility() == View.VISIBLE? View.GONE : View.VISIBLE);
            }
        });

        ImageButton changeEmail = (ImageButton) findViewById(R.id.editEmail);
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
                setUpAlertDialog();
            }
        });
    }

    // Password change alert dialog TODO: Can't change password twice in a row and can't undo
    private void setUpAlertDialog() {
        View ll = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.password_snippet, null);
        final EditText newPasswordView = (EditText) ll.findViewById(R.id.newPassword);
        final EditText retypedPasswordView = (EditText) ll.findViewById(R.id.retypedPassword);
        final EditText passwordView = (EditText) ll.findViewById(R.id.curPassword);

        final AlertDialog alert = new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Update Password")
                .setMessage("Please fill in the following fields")
                .setView(ll)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {}
                }).create();

        if (!alertReady) {
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button button = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newPassword = newPasswordView.getText().toString();
                                String retypedPassword = retypedPasswordView.getText().toString();

                                if (!newPassword.equals(retypedPassword)) {
                                    retypedPasswordView.setError("Passwords don't match!");
                                    retypedPasswordView.requestFocus();
                                } else {
                                    cachePassword = passwordView.getText().toString();
                                    passwordText.setText(newPassword);
                                    alert.dismiss();
                                }
                            }
                        });
                    }
                }
            });
            alertReady = true;
        }

        alert.show();
    }

    private void setUpViews() {
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

        passwordText = (TextView) findViewById(R.id.passwordText);
    }

    private void toggleOffEdits() {
        toggleVisibility(email, emailText, true);
        toggleVisibility(name, nameText, true);
        toggleVisibility(memberId, memberIdText, true);
    }

    private void toggleVisibility(EditText editText, TextView textView, boolean forceText) {
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
}
