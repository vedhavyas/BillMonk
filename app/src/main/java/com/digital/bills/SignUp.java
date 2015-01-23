package com.digital.bills;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.rengwuxian.materialedittext.MaterialEditText;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SignUp extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setUpActivity();
    }


    private void setUpActivity(){
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Sign Up");
        actionbar.setDisplayHomeAsUpEnabled(true);
        final MaterialEditText emailBox = (MaterialEditText) findViewById(R.id.emailBox);
        final MaterialEditText passwordBox = (MaterialEditText) findViewById(R.id.passwordBox);

        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        Button backBtn = (Button) findViewById(R.id.backBtn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(emailBox.getText().toString(), passwordBox.getText().toString());
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void registerUser(String email, String password){
        final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(this);
        pDialog.setTitleText("Registering...");
        pDialog.show();
        final ParseUser newUser = new ParseUser();
        newUser.setUsername(email);
        newUser.setPassword(password);
        newUser.setEmail(email);

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    User user = new User();
                    user.setEmail(newUser.getEmail());
                    new SaveUser(pDialog, user).execute();
                }else{
                    pDialog.cancel();
                }
            }
        });
    }

    private void startMainActivity(){
        Intent mainActivityIntent = new Intent(this, Main.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
    }

    private class SaveUser extends AsyncTask<Void, Void, Void>{

        User user;
        SweetAlertDialog pDialog;

        private SaveUser(SweetAlertDialog pDialog, User user) {
            this.pDialog = pDialog;
            this.user = user;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Database db = Database.getInstance(getBaseContext());
            int result = (int)db.saveUser(user);
            if (result > 0) {
                SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE);
                prefs.edit().putInt(Constants.LOCAL_USER_ID, result).apply();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.cancel();
            startMainActivity();
        }
    }

}
