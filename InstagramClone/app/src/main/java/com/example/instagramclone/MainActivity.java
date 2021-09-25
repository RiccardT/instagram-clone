package com.example.instagramclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, View.OnKeyListener {

    EditText usernameEditTextField;
    EditText passwordEditTextField;
    Button generalSubmitButton;
    TextView loginSignUpSwitch;
    boolean isSetForSignUp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ParseUser.getCurrentUser() != null) {
            Log.i(
            "LOGIN SESSION",
                String.format("User: %s is signed in", ParseUser.getCurrentUser().getUsername())
            );
            transitionToUserListActivity();
        }
        Log.i("LOGIN SESSION", "No user detected on startup");
        usernameEditTextField = findViewById(R.id.usernameInput);
        passwordEditTextField = findViewById(R.id.passwordInput);
        generalSubmitButton = findViewById(R.id.generalSubmissionButton);
        loginSignUpSwitch = findViewById(R.id.loginAndSignUpButtonSwitch);

        ImageView logoImageView = findViewById(R.id.instagramLogo);
        ConstraintLayout background = findViewById(R.id.background);

        passwordEditTextField.setOnKeyListener(this);
        generalSubmitButton.setOnClickListener(this);
        loginSignUpSwitch.setOnClickListener(this);
        logoImageView.setOnClickListener(this);
        background.setOnClickListener(this);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    private void transitionToUserListActivity() {
        Intent userListActivityIntent = new Intent(getApplicationContext(), UserListActivity.class);
        startActivity(userListActivityIntent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.generalSubmissionButton) submitUsernameAndPassword();
        else if (view.getId() == R.id.loginAndSignUpButtonSwitch) switchLoginAndSignUpUI();
        else if (view.getId() == R.id.instagramLogo || view.getId() == R.id.background) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE
            );
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyboardEnterKeyWasPressedDown(i, keyEvent)) {
            submitUsernameAndPassword();
        }
        return false;
    }

    private boolean keyboardEnterKeyWasPressedDown(int i, KeyEvent keyEvent) {
        return i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN;
    }

    private void submitUsernameAndPassword() {
        String username = usernameEditTextField.getText().toString().trim();
        String password = passwordEditTextField.getText().toString().trim();
        if (isSetForSignUp) {
            signUpUser(username, password);
            return;
        }
        loginUser(username, password);
    }

    private void signUpUser(String username, String password) {
        if (username.length() == 0 || password.length() == 0) {
            Utilities.showToastMessage(
                    "Username and password required",
                    this
            );
            return;
        }
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(e -> {
            if (e == null) {
                transitionToUserListActivity();
                Log.i("SIGNUP", String.format("User %s signed up!", username));
                return;
            }
            e.printStackTrace();
            switch (e.getCode()) {
                case ParseException.USERNAME_TAKEN: {
                    Utilities.showToastMessage(
                            "Username is already taken! Please sign in",
                            this
                    );
                    break;
                }
                case ParseException.EMAIL_TAKEN: {
                    // report error
                    break;
                }
                default: {
                    Utilities.showToastMessage(
                            String.format("User %s's signup failed!", username),
                            this
                    );
                }
            }
        });
    }

    private void loginUser(String username, String password) {
        if (username.length() == 0 || password.length() == 0) {
            Utilities.showToastMessage(
                    "Username and password required",
                    this
            );
            return;
        }
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (user != null) {
                transitionToUserListActivity();
                Log.i("LOGIN", String.format("User: %s logged in", username));
                return;
            }
            Utilities.showToastMessage(
                    "login failed",
                    this
            );
            e.printStackTrace();
        });
    }

    private void switchLoginAndSignUpUI() {
        if (isSetForSignUp) {
            generalSubmitButton.setText(R.string.login_button_text);
            loginSignUpSwitch.setText(R.string.switch_text_for_sign_up);
            isSetForSignUp = false;
            return;
        }
        generalSubmitButton.setText(R.string.sign_up_button_text);
        loginSignUpSwitch.setText(R.string.switch_text_for_login);
        isSetForSignUp = true;
    }
}