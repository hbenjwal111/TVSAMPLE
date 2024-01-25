package com.oxootv.spagreen.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.oxootv.spagreen.Config;
import com.oxootv.spagreen.Constants;
import com.oxootv.spagreen.R;
import com.oxootv.spagreen.database.DatabaseHelper;
import com.oxootv.spagreen.model.ActiveStatus;
import com.oxootv.spagreen.model.User;
import com.oxootv.spagreen.network.RetrofitClient;
import com.oxootv.spagreen.network.api.LoginApi;
import com.oxootv.spagreen.network.api.SubscriptionApi;
import com.oxootv.spagreen.utils.ToastMsg;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends Activity {
    private EditText etEmail, etPass;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.email_edit_text);
        etPass = findViewById(R.id.password_edit_text);
        progressBar = findViewById(R.id.progress_login);

    }

    public void loginBtn(View view) {
        if (!isValidEmailAddress(etEmail.getText().toString())) {
            new ToastMsg(LoginActivity.this).toastIconError("Please enter valid email");
        } else if (etPass.getText().toString().equals("")) {
            new ToastMsg(LoginActivity.this).toastIconError("Please enter password");
        } else {
            String email = etEmail.getText().toString();
            String pass = etPass.getText().toString();
            login(email, pass);
        }

    }

    private void login(String email, final String password) {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<User> call = api.postLoginStatus(Config.API_KEY, email, password);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        if (db.getUserDataCount() > 1) {
                            db.deleteUserData();
                        } else {
                            if (db.getUserDataCount() == 0) {
                                db.insertUserData(user);
                            } else {
                                db.updateUserData(user, 1);
                            }
                        }
                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();

                        //save user login time, expire time
                        updateSubscriptionStatus(user.getUserId());

                    } else {
                        new ToastMsg(LoginActivity.this).toastIconError(response.body().getData());
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
            }
        });
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public void updateSubscriptionStatus(String userId) {
        //get saved user id
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        ActiveStatus activeStatus = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);

                        if (db.getActiveStatusCount() > 1) {
                            db.deleteAllActiveStatusData();
                        } else {

                            if (db.getActiveStatusCount() == 0) {
                                db.insertActiveStatusData(activeStatus);
                            } else {
                                db.updateActiveStatus(activeStatus, 1);
                            }
                        }
                        Intent intent = new Intent(LoginActivity.this, LeanbackActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        startActivity(intent);
                        finish();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
