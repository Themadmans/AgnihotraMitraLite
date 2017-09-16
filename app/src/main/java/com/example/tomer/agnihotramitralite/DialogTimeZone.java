package com.example.tomer.agnihotramitralite;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.TimeZone;

public class DialogTimeZone extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_time_zone);

        final ArrayAdapter<String> adapter =
                new ArrayAdapter <> (this,android.R.layout.simple_list_item_1);

        String[]TZ = TimeZone.getAvailableIDs();
        ArrayList<String> TZ1 = new ArrayList<String>();
        for(int i = 0; i < TZ.length ; i++) { // TZ.lenght
            if(!(TZ1.contains(TimeZone.getTimeZone(TZ[i]).getDisplayName()))) {
                String a;
                a= TimeZone.getTimeZone(TZ[i]).getDisplayName() +  "(" + TimeZone.getTimeZone(TZ[i]).getID() + ")";
                TZ1.add(a);
            }
        }
        for(int i = 0; i < TZ1.size(); i++) {  //TZ1.size
            adapter.add(TZ1.get(i));
        }
        final ArrayList<String> TZ2 = TZ1;
        final ListView lv = (ListView) findViewById(R.id.listviewtimezone);

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder ad = new AlertDialog.Builder(DialogTimeZone.this);
                final String selected= (String) lv.getItemAtPosition(position);
                ad.setTitle("Confirm timezone for location");
                ad.setMessage(selected);
                ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tz = selected.substring(selected.indexOf("(")+1,selected.indexOf(")"));
                        Log.d("Raju",TZ2.get(position) + tz  + position);
                        Intent intent = new Intent();
                        intent.putExtra("tz",tz);
                        setResult(900,intent);
                        finish();

                    }
                });
                ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                ad.create();
                ad.show();
            }
        });
       // final Spinner TimeZoneSpinner  = (Spinner) findViewById(R.id.TimeZoneSpinner);
        //TimeZoneSpinner.setAdapter(adapter);
        for(int i = 0; i < TZ1.size(); i++) {  //TZ1.zies
            if(TZ1.get(i).equals(TimeZone.getDefault().getID())) {
                lv.setSelection(i);
            }
        }
        EditText editText = (EditText) findViewById(R.id.editTextSearch);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    }

