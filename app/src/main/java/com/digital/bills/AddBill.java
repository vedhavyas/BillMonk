package com.digital.bills;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class AddBill extends ActionBarActivity {

    private Activity activity;
    private Bitmap billImage;
    private MaterialEditText billIDBox, billDescBox, billAmountBox;
    private Spinner categorySpinner;
    private boolean single_bill;
    private int folderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);
        single_bill = getIntent().getBooleanExtra(Constants.SINGLE_BILL, false);
        if(!single_bill){
            folderID = getIntent().getIntExtra(Constants.FOLDER_ID, -1);
        }
        setUpActivity();

    }

    private void setUpActivity(){
        activity = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Bill");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        billIDBox = (MaterialEditText) findViewById(R.id.billIDBox);
        billDescBox = (MaterialEditText) findViewById(R.id.billDescBox);
        billAmountBox = (MaterialEditText) findViewById(R.id.billAmountBox);
        categorySpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Categories, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        Button pickBillBtn = (Button) findViewById(R.id.addBillImageBtn);
        Button saveBillBtn = (Button) findViewById(R.id.saveBillBtn);

        pickBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBillFromCamera();
            }
        });

        saveBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SaveBill().execute();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SELECT_PICTURE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                billImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            } catch (IOException e) {
            }
        }
    }

    private void getBillFromCamera(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.SELECT_PICTURE);
    }

    private void returnResult(int result, int billID) {
        Intent intent = new Intent();
        intent.putExtra(Constants.BILL_ID, billID);
        setResult(result, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(single_bill){
            startMainActivity();
        }else{
            returnResult(Activity.RESULT_CANCELED, -1);
        }
    }

    private void startMainActivity(){
        Intent mainActivityIntent = new Intent(this, Main.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
    }

    private class SaveBill extends AsyncTask<Void, Void, Integer>{
        private SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText("Saving Bill...");
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                ParseUser user = ParseUser.getCurrentUser();
                ParseRelation<ParseObject> billRelation = user.getRelation(Constants.BILL_RELATION);
                ParseObject parseBill = new ParseObject(Constants.BILL_CLASS);
                parseBill.put(Constants.BILL_ID, billIDBox.getText().toString());
                parseBill.put(Constants.BILL_DESC, billDescBox.getText().toString());
                parseBill.put(Constants.BILL_AMOUNT, billAmountBox.getText().toString());
                if (billImage != null) {
                    ParseFile image = new ParseFile(Constants.BILL_IMAGE_FILE, Utilities.getBlob(Utilities.getReSizedBitmap(billImage)));
                    image.save();
                    parseBill.put(Constants.BILL_IMAGE, image);
                }
                parseBill.put(Constants.BILL_CATEGORY, String.valueOf(categorySpinner.getSelectedItem()));
                parseBill.save();
                billRelation.add(parseBill);
                user.save();

                Database db = Database.getInstance(activity);
                if(!single_bill){
                    String folderObjectId = db.getFolderObjectID(folderID);
                    Log.d("Log", folderObjectId);
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FOLDER_CLASS);
                    ParseObject folderObject = query.get(folderObjectId);
                    ParseRelation<ParseObject> billFolderRelation = folderObject.getRelation(Constants.BILL_RELATION);
                    billFolderRelation.add(parseBill);
                    folderObject.save();
                }

                Bill bill = new Bill();
                bill.setObjectID(parseBill.getObjectId());
                bill.setBillID(billIDBox.getText().toString());
                bill.setDescription(billDescBox.getText().toString());
                bill.setAmount(Float.parseFloat(billAmountBox.getText().toString()));
                if (billImage != null) {
                    bill.setBill(Utilities.getBlob(Utilities.getReSizedBitmap(billImage)));
                }
                bill.setCategory(String.valueOf(categorySpinner.getSelectedItem()));
                int result = db.saveBill(bill);
                if (result > 0 && !single_bill) {
                    db.saveFolderBill(folderID, result);
                }
                return result;
            }catch (ParseException e){
                Log.d("Error", e.getMessage());
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            pDialog.cancel();
            if(result > 0){
                if(single_bill){
                    startMainActivity();
                }else{
                    returnResult(Activity.RESULT_OK, result);
                }
            }else{
                if(single_bill){
                    startMainActivity();
                }else{
                    returnResult(Activity.RESULT_CANCELED, -1);
                }
            }
        }
    }

}
