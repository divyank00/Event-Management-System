package com.example.collegeproject.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.collegeproject.R;
import com.example.collegeproject.base.BaseActivity;
import com.example.collegeproject.network.ObjectModel;
import com.example.collegeproject.response.UserResponse;
import com.example.collegeproject.viewmodel.UserViewModel;
import com.libizo.CustomEditText;

import java.util.regex.Pattern;

import static androidx.navigation.Navigation.findNavController;
import static com.example.collegeproject.helper.SharedPref.setToken;

public class LoginFragment extends Fragment {

    private UserViewModel loginViewModel;
    private CustomEditText mailEditText, passwordEditText;
    private TextView newUser;
    private Button loginButton;
    private ProgressBar loadingProgressBar;
    private Observer<ObjectModel> observer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        mailEditText = view.findViewById(R.id.mail);
        passwordEditText = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.login);
        loadingProgressBar = view.findViewById(R.id.loading);
        newUser = view.findViewById(R.id.newUser);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginButton.performClick();
                    return true;
                }
                return false;
            }
        });

        newUser.setText(getText(R.string.new_user));
        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getView() != null)
                    findNavController(getView()).navigate(R.id.registerFragment);
            }
        });

        observer = new Observer<ObjectModel>() {
            @Override
            public void onChanged(ObjectModel objectModel) {
                if (objectModel.isStatus())
                    successful((UserResponse) objectModel.getObj());
                else
                    failed(objectModel.getMessage());
                loginButton.setEnabled(true);
            }
        };

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                mailEditText.setError(null);
                passwordEditText.setError(null);
                if (mailEditText.getText().toString().trim().isEmpty()) {
                    mailEditText.setError(getString(R.string.mandatory));
                    return;
                }
                if (!isValidEmail(mailEditText.getText().toString().trim())) {
                    mailEditText.setError(getString(R.string.format));
                    return;
                }
                if (passwordEditText.getText().toString().trim().isEmpty()) {
                    passwordEditText.setError(getString(R.string.mandatory));
                    return;
                }
                loginButton.setEnabled(false);
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.loginLiveData(mailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim())
                        .observe(getViewLifecycleOwner(), observer);
            }
        });
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    private void successful(UserResponse model) {
        loadingProgressBar.setVisibility(View.GONE);
        String welcome = getString(R.string.welcome) + model.getName() + "!";
        if (getContext() != null && getContext().getApplicationContext() != null) {
            setToken(getContext(), model.getToken());
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
            if (getActivity() != null)
                getActivity().finish();
            startActivity(new Intent(getContext(), BaseActivity.class));
        }
    }

    private void failed(String errorString) {
        loadingProgressBar.setVisibility(View.GONE);
        if (getContext() != null) {
            Toast.makeText(
                    getContext(),
                    errorString,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void closeKeyboard() {
        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }
}