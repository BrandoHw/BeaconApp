package org.altbeacon.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ibm.cloud.appid.android.api.AppID;
import com.ibm.cloud.appid.android.api.AppIDAuthorizationManager;
import com.ibm.cloud.appid.android.api.AuthorizationException;
import com.ibm.cloud.appid.android.api.LoginWidget;

import org.altbeacon.WorkTracking.R;

/**
 * Fragment representing the login screen
 */
public class LoginFragment extends Fragment implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    TextInputEditText passwordEditText, usernameEditText;
    TextInputLayout passwordTextInput, usernameTextInput;
    TextView titleHeader;
    MaterialButton loginButton, registerButton;
    ProgressBar progressBar;
    LinearLayout paddingLayout;
    RelativeLayout rl_buttons1;

    private final static String region = AppID.REGION_US_SOUTH; //AppID.REGION_SYDNEY or AppID.REGION_UK, change to the region where you App ID instance is deployed.
    private AppID appId;
    private AppIDAuthorizationManager appIdAuthorizationManager;
    private TokensPersistenceManager tokensPersistenceManager;
    private ProgressManager progressManager;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

              // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        appId = AppID.getInstance();
        //appId.initialize(getActivity(), getResources().getString(R.string.authTenantId), region);
        appIdAuthorizationManager = new AppIDAuthorizationManager(appId);
        tokensPersistenceManager = new TokensPersistenceManager(getActivity(), appIdAuthorizationManager);
        this.progressManager = new ProgressManager(getActivity());

        // Views
        passwordTextInput = view.findViewById(R.id.password_text_input);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        usernameEditText = view.findViewById(R.id.username_edit_text);
        usernameTextInput = view.findViewById(R.id.username_text_input);
        titleHeader = view.findViewById(R.id.titleHeader);
        progressBar = view.findViewById(R.id.progressBar);

        //Layout views
        paddingLayout = view.findViewById(R.id.padding);
        rl_buttons1 = view.findViewById(R.id.rl_buttons1);

        //Buttons
        loginButton = view.findViewById(R.id.login_button);
        registerButton = view.findViewById(R.id.register_button);

        //Set On click Listeners for buttons
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

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

        String storedRefreshToken = tokensPersistenceManager.getStoredRefreshToken();
        if (storedRefreshToken != null && !storedRefreshToken.isEmpty()) {
            Log.i("RefreshToken Success", storedRefreshToken);
            refreshTokens(storedRefreshToken);
        }
        Log.i("Create View", "Finished");
        return view;
    }


    private void refreshTokens(String refreshToken) {
        Log.d(logTag("refreshTokens"), "Trying to refresh tokens using a refresh token");
        boolean storedTokenAnonymous = tokensPersistenceManager.isStoredTokenAnonymous();
        AppIdAuthorizationListener appIdAuthorizationListener =
                new AppIdAuthorizationListener(getActivity(), appIdAuthorizationManager, storedTokenAnonymous, progressManager);
        appId.signinWithRefreshToken(getActivity(), refreshToken, appIdAuthorizationListener);
    }

    private void createAccount() {
        Log.d(logTag("onRegisterClick"),"Attempting sign up a new user");
        progressManager.showProgress();
        if (!validateForm()) {
            return;
        }
        AppIdAuthorizationListener appIdAuthorizationListener =
                new AppIdAuthorizationListener(getActivity(), appIdAuthorizationManager, false, progressManager);

        LoginWidget loginWidget = appId.getLoginWidget();
        loginWidget.launchSignUp(getActivity(), appIdAuthorizationListener);
    }

    private void signIn() {
        Log.d(TAG, "SignClicked");
        progressManager.showProgress();
        if (!validateForm()) {
            return;
        }
        Log.d(logTag("onLoginClick"),"Attempting identified authorization");
        String inputEmail = usernameEditText.getText().toString();
        String inputPassword = passwordEditText.getText().toString();

        inputEmail = inputEmail.trim();
        inputPassword = inputPassword.trim();

        AppIdAuthorizationListener appIdAuthorizationListener =
                new AppIdAuthorizationListener(getActivity(), appIdAuthorizationManager, false, progressManager);

        if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
            appIdAuthorizationListener.onAuthorizationFailure(new AuthorizationException("Something didn't work out.\nPlease try entering your email and password again."));
        } else {
            Log.d(TAG, "Signing In with ROP");

            appId.signinWithResourceOwnerPassword(getActivity().getApplicationContext(), inputEmail, inputPassword, appIdAuthorizationListener);
        }
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

    public void onForgotPasswordClick(View v) {
        Log.d(logTag("onForgotPasswordClick"),"forgot password triggered");
        progressManager.showProgress();
        AppIdAuthorizationListener appIdAuthorizationListener =
                new AppIdAuthorizationListener(getActivity(), appIdAuthorizationManager, false, progressManager);
        appId.getLoginWidget().launchForgotPassword(getActivity(), appIdAuthorizationListener);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.register_button) {
            createAccount();
        } else if (i == R.id.login_button) {
            Log.d(TAG, "Login Clicked");
            if (!isPasswordValid(passwordEditText.getText())) {
                passwordTextInput.setError(getString(R.string.mmdt_error_password));
            } else {
                passwordTextInput.setError(null); // Clear the error
                signIn();
            }
        }
    }

    private String logTag(String methodName){
        return getClass().getCanonicalName() + "." + methodName;
    }
}



