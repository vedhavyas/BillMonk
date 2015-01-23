package com.digital.bills;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ObjectService extends Service {

    private Handler handler;
    public ObjectService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        new Thread(new GetObjects()).start();
    }

    private void fetchBills() throws ParseException {
        ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> billRelation = user.getRelation(Constants.BILL_RELATION);
        List<ParseObject> parseObjects = billRelation.getQuery().find();
        List<Bill> bills = new ArrayList<>();
        for(ParseObject object : parseObjects){
            Bill bill = new Bill();
            bill.setObjectID(object.getObjectId());
            bill.setBillID(object.getString(Constants.BILL_ID));
            bill.setDescription(object.getString(Constants.BILL_DESC));
            bill.setCategory(object.getString(Constants.BILL_CATEGORY));
            bill.setAmount(Float.parseFloat(object.getString(Constants.BILL_AMOUNT)));
            if(object.getParseFile(Constants.BILL_IMAGE) != null){
                bill.setBill(object.getParseFile(Constants.BILL_IMAGE).getData());
            }

            bills.add(bill);
        }

        Database db = Database.getInstance(this);
        for(Bill bill : bills){
            db.saveBill(bill);
        }
    }

    private List<ParseObject> fetchFolders() throws ParseException {
        ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> folderRelation = user.getRelation(Constants.FOLDER_RELATION);
        List<ParseObject> folders = folderRelation.getQuery().find();
        Database db = Database.getInstance(this);
        for(ParseObject object : folders){
            db.createFolder(object.getObjectId(), object.getString(Constants.FOLDER_NAME));
        }

        return folders;
    }

    private void linkFoldersAndBills(List<ParseObject> folders) throws ParseException {
        Database db = Database.getInstance(this);
        ParseRelation <ParseObject> billsRelation;
        List<ParseObject> billObjects;
        int billId = -1;
        for(ParseObject folderObject : folders){
            billsRelation = folderObject.getRelation(Constants.BILL_RELATION);
            billObjects = billsRelation.getQuery().find();
            for(ParseObject billObject : billObjects){
                billId = db.getBillID(billObject.getObjectId());
                if(billId > 0){
                    db.saveFolderBill(db.getFolderID(folderObject.getString(Constants.FOLDER_NAME)), billId);
                }
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private class GetObjects implements Runnable{

        @Override
        public void run() {
            try {
                fetchBills();
                List<ParseObject> folders = fetchFolders();
                linkFoldersAndBills(folders);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onDestroy();
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
