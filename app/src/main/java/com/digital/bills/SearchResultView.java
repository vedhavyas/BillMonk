package com.digital.bills;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class SearchResultView extends ActionBarActivity {

    private Activity activity;
    private String searchData;
    private int whichSearch;
    private BillsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_view);
        setUpActivity();
    }

    private void setUpActivity(){
        activity = this;
        whichSearch = getIntent().getIntExtra(Constants.WHICH_SEARCH, -1);
        searchData = getIntent().getStringExtra(Constants.SEARCH_DATA);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search results");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recList = (RecyclerView)findViewById(R.id.recyclerView);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        adapter = new BillsAdapter(activity, new ArrayList<Bill>());
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int billID) {
                Intent intent = new Intent(activity, BillEdit.class);
                intent.putExtra(Constants.BILL_ID, billID);
                startActivityForResult(intent, Constants.EDIT_BILL);
            }
        });
        recList.setAdapter(adapter);

        FloatingActionButton generatePdfBtn = (FloatingActionButton)findViewById(R.id.generateBill);
        generatePdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDFGenerator pdfGenerator = new PDFGenerator(activity, adapter.getBills(), "Total");
                String fileName = pdfGenerator.generatePdf();
                if(fileName != null) {
                    Toast.makeText(activity, "PDF " + fileName + " generated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, PDFViewer.class);
                    intent.putExtra(Constants.PDF_FILE_NAME, fileName);
                    startActivity(intent);
                }
            }
        });

        new GetResultBills().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.EDIT_BILL && resultCode == RESULT_OK){
            new GetResultBills().execute();
        }
    }

    private void updateAdapter(List<Bill> bills){
        if(bills != null){
            adapter.setBills(bills);
        }
    }

    private class GetResultBills extends AsyncTask<Void, Void, List<Bill>>{
        @Override
        protected List<Bill> doInBackground(Void... params) {
            Database db = Database.getInstance(activity);
            if(whichSearch == Constants.CATEGORY_SEARCH){
                return db.getBillsOnCategorySearch(searchData);
            }else if(whichSearch == Constants.DESC_SEARCH){
                return  db.getBillsOnDescSearch(searchData);
            }else{
                return db.getBillsOnFolderSearch(searchData);
            }
        }

        @Override
        protected void onPostExecute(List<Bill> bills) {
            super.onPostExecute(bills);
            updateAdapter(bills);
        }
    }

}
