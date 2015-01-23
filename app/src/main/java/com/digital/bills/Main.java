package com.digital.bills;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.parse.ParseUser;

import it.neokree.materialnavigationdrawer.MaterialAccount;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.MaterialSection;
import it.neokree.materialnavigationdrawer.MaterialSectionListener;


public class Main extends MaterialNavigationDrawer<Fragment> {

    private static final String TRANSPARENT_COLOR = "#9e9e9e";

    private MaterialAccount account;
    private Activity activity;
    private MaterialSection listRecent, addBill, viewFolder, logout, searchBills;
    private Handler handler;

    @Override
    public void init(Bundle bundle) {
        activity = this;
        int comRed = getResources().getColor(R.color.com_red);
        handler = new Handler();
        account = new MaterialAccount("", "", new ColorDrawable(Color.parseColor(TRANSPARENT_COLOR)), getResources().getDrawable(R.drawable.ic_nav_background));
        this.addAccount(account);
        new Thread(new GetUserAsync()).start();
        allowArrowAnimation();
        addMultiPaneSupport();

        listRecent = this.newSection("Recent Bills", new ListRecent()).setSectionColor(comRed, comRed);
        addSection(listRecent);

        addBill = this.newSection("Add bill", new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection materialSection) {
                Intent addBillIntent = new Intent(activity, AddBill.class);
                addBillIntent.putExtra(Constants.SINGLE_BILL, true);
                startActivityForResult(addBillIntent, Constants.ADD_BILL);
                materialSection.unSelect();
            }
        }).setSectionColor(comRed, comRed);
        addSection(addBill);

        viewFolder = this.newSection("View Folders", new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection materialSection) {
                Intent intent = new Intent(activity, ViewFolders.class);
                startActivity(intent);
                materialSection.unSelect();
            }
        });
        addSection(viewFolder);

        searchBills = this.newSection("Search bills", new SearchBills()).setSectionColor(comRed, comRed);
        addSection(searchBills);


        logout = this.newSection("Logout", new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection materialSection) {
                materialSection.unSelect();
                ParseUser.logOut();
                Database db = Database.getInstance(activity);
                db.dropTables();
                Intent intent = new Intent(activity, StartScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        addBottomSection(logout);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.ADD_BILL && resultCode == RESULT_OK){
            Toast.makeText(activity, "Bill Added", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetUserAsync implements Runnable {

        @Override
        public void run() {
            Database db = Database.getInstance(getBaseContext());
            SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE);
            User localUser = db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
            if(localUser != null) {
                if(localUser.getName() != null){
                    account.setTitle(localUser.getName());
                }else{
                    account.setTitle(localUser.getEmail());
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyAccountDataChanged();
                    }
                });
            }
        }
    }
}
