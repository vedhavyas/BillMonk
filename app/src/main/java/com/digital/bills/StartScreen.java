package com.digital.bills;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.parse.ParseUser;


public class StartScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParseUser user = ParseUser.getCurrentUser();
        if(user != null){
            startMainActivity();
        }
        setContentView(R.layout.activity_start_screen);
        setUpActivity();
    }

    private void startMainActivity(){
        Intent mainActivityIntent = new Intent(this, Main.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
    }

    private void setUpActivity(){
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("My Bill Lite");
        Button userLoginBtn = (Button) findViewById(R.id.userLoginBtn);
        Button merchantLoginBtn = (Button) findViewById(R.id.merchantLoginBtn);
        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);

        userLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity();
            }
        });

        merchantLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignUpActivity();
            }
        });


    }

    private void startLoginActivity(){
        Intent loginIntent = new Intent(this, Login.class);
        startActivity(loginIntent);
    }

    private void startSignUpActivity(){
        Intent signUpIntent = new Intent(this, SignUp.class);
        startActivity(signUpIntent);
    }
}
