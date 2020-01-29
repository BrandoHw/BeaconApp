package org.altbeacon.beaconreference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

/**
 * Fragment representing the login screen
 */
public class LoginFragment extends Fragment implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    TextInputEditText passwordEditText, usernameEditText;
    TextInputLayout passwordTextInput, usernameTextInput;
    TextView verifyStatusTextView, titleHeader;
    MaterialButton loginButton, registerButton, cancelButton, verifyButton;
    ProgressBar progressBar;
    LinearLayout paddingLayout;
    RelativeLayout rl_buttons1, rl_buttons2, verifyLayout;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

              // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        // Views
        passwordTextInput = view.findViewById(R.id.password_text_input);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        usernameEditText = view.findViewById(R.id.username_edit_text);
        usernameTextInput = view.findViewById(R.id.username_text_input);
        verifyStatusTextView = view.findViewById(R.id.verify_status);
        titleHeader = view.findViewById(R.id.titleHeader);
        progressBar = view.findViewById(R.id.progressBar);

        //Layout views
        paddingLayout = view.findViewById(R.id.padding);
        rl_buttons1 = view.findViewById(R.id.rl_buttons1);
        rl_buttons2 = view.findViewById(R.id.rl_buttons2);
        verifyLayout = view.findViewById(R.id.verify_layout);

        //Buttons
        loginButton = view.findViewById(R.id.login_button);
        registerButton = view.findViewById(R.id.register_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        verifyButton = view.findViewById(R.id.verify_button);

        //Set On click Listeners for buttons
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Clear the error once more than 8 characters are typed.
        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isPasswordValid(passwordEditText.getText())) {
                    passwordTextInput.setError(null); //Clear the error
                }
                return false;
            }
        });
        return view;
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }  else {
                            signIn(email, password);
                            // If sign in fails, display a message to the user.}
                        }
                        // [START_EXCLUDE]
                        progressBar.setVisibility(View.GONE);
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            progressBar.setVisibility(View.GONE);
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (!user.isEmailVerified()) {
                                Log.w(TAG, "createUserWithEmail:failure due to unverified email", task.getException());
                                Toast.makeText(getActivity(), "Email has not been verified.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(user);
                            } else {
                                Intent myIntent = new Intent(getActivity(), MonitoringActivity.class);
                                //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                getActivity().startActivity(myIntent);
                                getActivity().finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            //mStatusTextView.setText(R.string.auth_failed);
                        }
                        progressBar.setVisibility(View.GONE);
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void cancelButton() {
        mAuth.signOut();
        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        progressBar.setVisibility(View.GONE);
        if (user != null) {
            verifyStatusTextView.setText(String.format("Email User: %1$s (Verified Status: %2$b)",
                    user.getEmail(), user.isEmailVerified()));

            paddingLayout.setVisibility(View.GONE);
            usernameTextInput.setVisibility(View.GONE);
            passwordTextInput.setVisibility(View.GONE);
            rl_buttons1.setVisibility(View.GONE);
            rl_buttons2.setVisibility(View.VISIBLE);
            verifyLayout.setVisibility(View.VISIBLE);

            verifyButton.setEnabled(!user.isEmailVerified());
        } else {
            paddingLayout.setVisibility(View.VISIBLE);
            usernameTextInput.setVisibility(View.VISIBLE);
            passwordTextInput.setVisibility(View.VISIBLE);
            rl_buttons1.setVisibility(View.VISIBLE);
            rl_buttons2.setVisibility(View.GONE);
            verifyLayout.setVisibility(View.GONE);
        }
    }

    private void sendEmailVerification() {
        // Disable button
        verifyButton.setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        verifyButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(),
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText( getActivity(),
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = usernameEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            usernameEditText.setError("Required.");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Required.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }

    private boolean isPasswordValid(@Nullable Editable text) {
        return text != null && text.length() >= 8;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.register_button) {
            createAccount(usernameEditText.getText().toString(), passwordEditText.getText().toString());
        } else if (i == R.id.login_button) {
            if (!isPasswordValid(passwordEditText.getText())) {
                passwordTextInput.setError(getString(R.string.mmdt_error_password));
            } else {
                passwordTextInput.setError(null); // Clear the error
                signIn(usernameEditText.getText().toString(), passwordEditText.getText().toString());

            }
        } else if (i == R.id.cancel_button) {
            Log.i(TAG, "Cancel Pressed");
            cancelButton();
        } else if (i == R.id.verify_button) {
            Log.i(TAG, "Verify Pressed");
            sendEmailVerification();
        }
    }
}
