package com.digital.bills;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.rengwuxian.materialedittext.MaterialEditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchBills extends Fragment {

    private Activity activity;

    public SearchBills() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_bills, container, false);
        setUpActivity(rootView);
        return rootView;
    }

    private void setUpActivity(View view){
        activity = getActivity();
        final MaterialEditText descBox = (MaterialEditText) view.findViewById(R.id.billDescBox);
        final Spinner categorySpinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.Categories, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        final MaterialEditText folderBox = (MaterialEditText) view.findViewById(R.id.folderNameBox);

        Button descSearchBtn = (Button) view.findViewById(R.id.descSearchBtn);
        Button categorySearchBtn = (Button) view.findViewById(R.id.categorySearchBtn);
        Button folderSearchBtn = (Button) view.findViewById(R.id.folderSearchBtn);

        descSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startShowActivity(Constants.DESC_SEARCH, descBox.getText().toString());
            }
        });

        categorySearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startShowActivity(Constants.CATEGORY_SEARCH, String.valueOf(categorySpinner.getSelectedItem()));
            }
        });

        folderSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startShowActivity(Constants.FOLDER_SEARCH, folderBox.getText().toString());
            }
        });
    }

    private void startShowActivity(int which, String data){
        Intent intent = new Intent(activity, SearchResultView.class);
        intent.putExtra(Constants.WHICH_SEARCH, which);
        intent.putExtra(Constants.SEARCH_DATA, data);
        startActivity(intent);
    }

}
