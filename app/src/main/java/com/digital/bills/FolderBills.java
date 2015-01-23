package com.digital.bills;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;

import java.util.ArrayList;
import java.util.List;


public class FolderBills extends ActionBarActivity {

    private int folderID;
    private String folderName;
    private Activity activity;
    private BillsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_bills);
        setUpActivity();
    }

    private void setUpActivity(){
        activity = this;
        Database db =Database.getInstance(activity);
        folderName = getIntent().getStringExtra(Constants.FOLDER_NAME);
        folderID = db.getFolderID(folderName);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(folderName);
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
        SwipeDismissListener dismissListener = new SwipeDismissListener(recList, new SwipeDismissListener.SwipeListener() {
            @Override
            public boolean canSwipe(int position) {
                return true;
            }

            @Override
            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                deleteBills(reverseSortedPositions);
            }

            @Override
            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                deleteBills(reverseSortedPositions);
            }
        });
        recList.addOnItemTouchListener(dismissListener);

        final FloatingActionButton addBillBtn = (FloatingActionButton) findViewById(R.id.addBillBtn);
        addBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AddBill.class);
                intent.putExtra(Constants.FOLDER_ID, folderID);
                startActivityForResult(intent, Constants.ADD_BILL);
            }
        });

        FloatingActionButton generatePDF = (FloatingActionButton) findViewById(R.id.generateBill);
        generatePDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDFGenerator pdfGenerator = new PDFGenerator(activity, adapter.getBills(), folderName);
                String fileName = pdfGenerator.generatePdf();
                if(fileName != null) {
                    Toast.makeText(activity, "PDF "+fileName+" generated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, PDFViewer.class);
                    intent.putExtra(Constants.PDF_FILE_NAME, fileName);
                    startActivity(intent);
                }
            }
        });

        new GetFolderBills().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.ADD_BILL && resultCode == RESULT_OK){
            int billId = data.getIntExtra(Constants.BILL_ID, -1);
            Database db = Database.getInstance(activity);
            Bill bill = db.getBill(billId);
            if(bill != null) {
                adapter.addBill(bill);
            }
        }else if(requestCode == Constants.EDIT_BILL && resultCode == RESULT_OK){
            new GetFolderBills().execute();
        }
    }

    private void deleteBills(int[] reverseSortedPositions){
        Bill bill;
        List<Bill> removedBills = new ArrayList<>();

        for (int position : reverseSortedPositions) {
            bill = adapter.getBill(position);
            adapter.removeBill(position);
            removedBills.add(bill);
        }

        showUndoSnack(removedBills);
    }

    private void showUndoSnack(final List<Bill> removedBills){
        String text;
        if (removedBills.size() > 1) {
            text = removedBills.size() + " bills deleted";
        } else {
            text = removedBills.size() + " bill deleted";
        }

        SnackbarManager.show(
                Snackbar.with(activity)
                        .text(text)
                        .textColor(getResources().getColor(R.color.com_gray))
                        .actionLabel("Undo")
                        .actionColor(getResources().getColor(R.color.com_red))
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .eventListener(new EventListener() {
                            @Override
                            public void onShow(Snackbar snackbar) {
                            }

                            @Override
                            public void onShown(Snackbar snackbar) {
                            }

                            @Override
                            public void onDismiss(Snackbar snackbar) {
                            }

                            @Override
                            public void onDismissed(Snackbar snackbar) {
                                Database db = Database.getInstance(activity);
                                db.deleteFolderBillsWithObjects(removedBills);
                            }
                        })
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                for (Bill bill : removedBills) {
                                    adapter.addBill(bill);
                                }
                                removedBills.clear();
                            }
                        }));
    }

    private void updateAdapter(List<Bill> bills){
        if(bills != null){
            adapter.setBills(bills);
        }
    }


    private class GetFolderBills extends AsyncTask<Void, Void, List<Bill>> {
        @Override
        protected List<Bill> doInBackground(Void... params) {
            Database db = Database.getInstance(activity);
            return db.getFolderBills(folderID);
        }

        @Override
        protected void onPostExecute(List<Bill> bills) {
            super.onPostExecute(bills);
            updateAdapter(bills);
        }
    }
}
