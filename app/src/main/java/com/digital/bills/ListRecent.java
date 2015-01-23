package com.digital.bills;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListRecent extends Fragment {


    private BillsAdapter adapter;
    public static ListRecent listRecent;
    private Activity activity;

    public ListRecent() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_list_recent, container, false);
        setUpActivity(rootView);
        return rootView;
    }

    private void setUpActivity(View view){
        listRecent = this;
        activity = getActivity();
        RecyclerView recList = (RecyclerView) view.findViewById(R.id.recyclerView);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        adapter = new BillsAdapter(getActivity(), new ArrayList<Bill>());
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
        final SwipeRefreshLayout swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.com_red, R.color.com_gray);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetBills(swipeRefresh).execute();
            }
        });

        FloatingActionButton generatePdf = (FloatingActionButton) view.findViewById(R.id.generateBill);
        generatePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDFGenerator pdfGenerator = new PDFGenerator(activity, adapter.getBills(), "Total");
                String fileName = pdfGenerator.generatePdf();
                if(fileName != null) {
                    Toast.makeText(activity, "PDF "+fileName+" generated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, PDFViewer.class);
                    intent.putExtra(Constants.PDF_FILE_NAME, fileName);
                    startActivity(intent);
                }

            }
        });

        new GetBills(null).execute();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.EDIT_BILL && resultCode == Activity.RESULT_OK){
            new GetBills(null).execute();
        }
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
                                db.removeBills(removedBills);
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


    private class GetBills extends AsyncTask<Void, Void, List<Bill>>{

        private SwipeRefreshLayout swipeRefresh;

        private GetBills(SwipeRefreshLayout swipeRefresh) {
            this.swipeRefresh = swipeRefresh;
        }

        @Override
        protected List<Bill> doInBackground(Void... params) {
            Database db = Database.getInstance(getActivity());
            return db.getAllBills();
        }

        @Override
        protected void onPostExecute(List<Bill> bills) {
            super.onPostExecute(bills);
            if(swipeRefresh != null && swipeRefresh.isRefreshing()){
                swipeRefresh.setRefreshing(false);
            }
            updateAdapter(bills);
        }
    }
}
