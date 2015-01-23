package com.digital.bills;

import android.app.Activity;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.joanzapata.pdfview.PDFView;

import java.io.File;


public class PDFViewer extends ActionBarActivity {

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);
        setUpActivity();
    }

    private void setUpActivity(){
        activity = this;
        String fileName = getIntent().getStringExtra(Constants.PDF_FILE_NAME);
        PDFView pdfView = (PDFView) findViewById(R.id.pdfview);
        pdfView.fromFile(new File(Environment.getExternalStorageDirectory(), fileName))
                .defaultPage(1)
                .enableSwipe(true)
                .showMinimap(false)
                .load();
    }

}
