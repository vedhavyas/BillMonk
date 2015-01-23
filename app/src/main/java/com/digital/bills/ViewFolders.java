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

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ViewFolders extends ActionBarActivity {

    private Activity activity;
    private FolderAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_folders);
        setUpActivity();
    }

    private void setUpActivity(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Your Folders");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activity = this;
        RecyclerView recList = (RecyclerView)findViewById(R.id.recyclerView);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        adapter = new FolderAdapter(activity, new ArrayList<String>());
        adapter.setOnFolderClickListener(new OnFolderClickListener() {
            @Override
            public void onItemClick(View v, String folderName) {
                Intent intent = new Intent(activity, FolderBills.class);
                intent.putExtra(Constants.FOLDER_NAME, folderName);
                startActivity(intent);
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
                deleteFolder(reverseSortedPositions);
            }

            @Override
            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                deleteFolder(reverseSortedPositions);
            }
        });
        recList.addOnItemTouchListener(dismissListener);

        FloatingActionButton addFolderBtn = (FloatingActionButton) findViewById(R.id.addFolderBtn);
        addFolderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFolderDialog();
            }
        });

        new GetFolders().execute();
    }

    private void showFolderDialog(){
        View alertMessageView = activity.getLayoutInflater().inflate(R.layout.folder_name_box, null);
        final MaterialEditText messageBox = (MaterialEditText) alertMessageView.findViewById(R.id.folderNameBox);


        new MaterialDialog.Builder(activity)
                .customView(alertMessageView, false)
                .title("New Folder Name")
                .titleColor(getResources().getColor(R.color.com_red))
                .positiveText("Set")
                .positiveColor(getResources().getColor(R.color.com_red))
                .cancelable(true)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                        new CreateFolder(messageBox.getText().toString()).execute();
                    }
                })
                .build()
                .show();
    }

    private void deleteFolder(int[] reverseSortedPositions){
        String folder;
        List<String> removedFolders = new ArrayList<>();

        for (int position : reverseSortedPositions) {
            folder = adapter.getFolder(position);
            adapter.removeFolder(position);
            removedFolders.add(folder);
        }

        showUndoSnack(removedFolders);
    }

    private void showUndoSnack(final List<String> removedFolders){
        String text;
        if (removedFolders.size() > 1) {
            text = removedFolders.size() + " folders deleted";
        } else {
            text = removedFolders.size() + " folder deleted";
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
                                db.removeFolders(removedFolders);
                            }
                        })
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                for (String folder : removedFolders) {
                                    adapter.addFolder(folder);
                                }
                                removedFolders.clear();
                            }
                        }));
    }

    private void updateAdapter(List<String> folders){
        if(folders != null){
            adapter.setFolders(folders);
        }
    }

    private class CreateFolder extends AsyncTask<Void, Void, Integer>{

        SweetAlertDialog pDialog;
        String folderName;

        private CreateFolder(String folderName) {
            this.folderName = folderName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText("Creating folder...");
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                ParseUser user = ParseUser.getCurrentUser();
                ParseRelation<ParseObject> folderRelation = user.getRelation(Constants.FOLDER_RELATION);
                ParseObject folder = new ParseObject(Constants.FOLDER_CLASS);
                folder.put(Constants.FOLDER_NAME, folderName);
                folder.save();
                folderRelation.add(folder);
                user.save();
                Database db = Database.getInstance(activity);
                return db.createFolder(folder.getObjectId(), folderName);
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
                adapter.addFolder(folderName);
                Toast.makeText(activity, "Folder created", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity, "Failed to create a Folder", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class GetFolders extends AsyncTask<Void, Void, List<String>>{
        @Override
        protected List<String> doInBackground(Void... params) {
            Database db = Database.getInstance(activity);
            return db.getFolders();
        }

        @Override
        protected void onPostExecute(List<String> folders) {
            super.onPostExecute(folders);
            updateAdapter(folders);
        }
    }
}