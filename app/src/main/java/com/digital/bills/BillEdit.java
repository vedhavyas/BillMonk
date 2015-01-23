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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.List;


public class BillEdit extends ActionBarActivity {

    private Activity activity;
    private int billId;
    private MaterialEditText billIDBox, billDescBox, billAmountBox;
    private Spinner categorySpinner;
    private ImageView imageView;
    private Bitmap billImage;
    private Bill bill;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_edit);
        setUpActivity();
    }

    private void setUpActivity(){
        activity = this;
        billId = getIntent().getIntExtra(Constants.BILL_ID ,-1);
        Database db = Database.getInstance(activity);
        bill = db.getBill(billId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Bill");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.imageView);
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
                new UpdateBill().execute();
            }
        });
        if(bill.getBill() != null) {
            imageView.setImageBitmap(Utilities.getBitmapFromBlob(bill.getBill()));
        }

        if(bill.getBillID() != null){
            billIDBox.setText(bill.getBillID());
        }

        if(bill.getDescription() != null){
            billDescBox.setText(bill.getDescription());
        }

        billAmountBox.setText(String.valueOf(bill.getAmount()));
    }


    private void returnResult(int result) {
        Intent intent = new Intent();
        setResult(result, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        returnResult(RESULT_CANCELED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SELECT_PICTURE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                billImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imageView.setImageBitmap(billImage);
            } catch (IOException e) {
            }
        }
    }

    private void getBillFromCamera(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.SELECT_PICTURE);
    }

    private class UpdateBill extends AsyncTask<Void, Void, Integer>{
        @Override
        protected Integer doInBackground(Void... params) {
            Database db = Database.getInstance(activity);
            bill.setBillID(billIDBox.getText().toString());
            bill.setDescription(billDescBox.getText().toString());
            bill.setAmount(Float.parseFloat(billAmountBox.getText().toString()));
            if (billImage != null) {
                bill.setBill(Utilities.getBlob(Utilities.getReSizedBitmap(billImage)));
            }
            bill.setCategory(String.valueOf(categorySpinner.getSelectedItem()));

            return db.updateBill(bill);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if(result > 0){
                returnResult(RESULT_OK);
            }else {
                returnResult(RESULT_CANCELED);
            }
        }
    }
}
