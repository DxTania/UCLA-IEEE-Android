package com.ucla_ieee.app.signin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.ucla_ieee.app.R;


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
                mAuthTask = new RegisterTask(RegisterActivity.this, email, password, firstname, lastname);
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

    public void cancelRegister() {
        mAuthTask = null;
        showProgress(false);
    }

}
