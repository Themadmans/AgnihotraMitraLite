package com.example.tomer.agnihotramitralite;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    String MyPreferences = "Myprefs";
    private int currentlocation = 0;
    private String timezoneparameters = "";
    private String timezone = "Asia/Kolkata";
    String tz = null;
    ProgressBar pb;
    ProgressDialog pd;
    int alarmtotal = 0; // USed to total sunset and sunrise minutes to avoid duplicate runs of setAlarm , ideally this should go to preference change listener
    boolean ENABLE_MENU = false;   // Disabling menu for now ...will in clude in next version
    boolean OLD_CELL = false;  // different codes for new and old cells ...

    Handler handler = new Handler();
    Runnable runnable;
    TextView timetv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // REMOVE BELOW FOR 4.1 above...check duality use

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        pd = new ProgressDialog(this);
        pd.setMessage(" Please wait , timings are being downloaded..");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        setContentView(R.layout.activity_main);
        final SharedPreferences sharedPreferences = getSharedPreferences(MyPreferences, MODE_PRIVATE);

        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Toast.makeText(MainActivity.this,"Done",Toast.LENGTH_SHORT).show();
                loadlocation();
            }
        });

        final SharedPreferences.Editor editor = sharedPreferences.edit();


        timetv = (TextView) findViewById(R.id.textViewTime);


        if (sharedPreferences.getInt("Location", 0)==0) {
            TextView tv1 = (TextView) findViewById(R.id.textViewLocation);
            TextView tv2 = (TextView) findViewById(R.id.textViewSunrise);
            TextView tv3 = (TextView) findViewById(R.id.textViewSunset);
            tv1.setText("Location Not set, Click on Icon !");
            tv2.setVisibility(View.INVISIBLE);
            tv3.setVisibility(View.INVISIBLE);
        }
        else {
loadlocation();
            checkforupdate(1);  // Checking if data updatation needed for first location...the default one
        }
        ImageView im= (ImageView) findViewById(R.id.locationimg);
            im.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sharedPreferences.getInt("Location", 0)==0) {
                        if(isNetworkAvailable())
                               runforlocation();
                        else
                            Toast.makeText(MainActivity.this," Please turn on internet.", Toast.LENGTH_SHORT).show();
                    }

                    else loadlocation();
        }
            });

           im.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View v) {
                   if (sharedPreferences.getInt("Location", 0)!=0) {
                   deletealllocation();
                   return false;
               }
               return false; }
            });
}


    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
        Date todaydate = Calendar.getInstance().getTime();
        final String datetoday = simpleDateFormat.format(todaydate);

        timetv = (TextView) findViewById(R.id.textViewTime);
        TextView datetv = (TextView) findViewById(R.id.textViewdate);
        datetv.setText(datetoday);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runnable = this;
                gettimenow();
                handler.postDelayed(this, 1000);
            }
        }, 1000);

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

    }


    String adrs;  // Making them global due to hanlding of 900 request code activity...need to run preparedb twice...
    SimpleDateFormat simpleDateFormat1; // made it global to reduce load on runnable

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 420 && data != null && resultCode != 0) {
            final double lats = data.getDoubleExtra("Latitude", 23);
            final double longs = data.getDoubleExtra("Longitude", 78);
            final String adrs=data.getStringExtra("Address");
            PrepareDB(lats, longs, adrs, 1); //3 for 500 activity / OLD CELL
        } else {
            Toast.makeText(this, "Something went wrong ! Please try again !", Toast.LENGTH_SHORT).show();
            Log.d("raju", "REquest code issue");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void gettimenow() {
        simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss");
        Date todaydate = Calendar.getInstance().getTime();
        String time1 = simpleDateFormat1.format(todaydate);
        timetv.setText(time1);
    }


    public void PrepareDB(Double lats, Double longs, String Address, int runcode) {
//runcode 0 for call coming from 840 activity, 1 for DialogTimezone activity run
        String MyPreferences = "Myprefs";   // to store some personalization variables - run, location, location no.
Log.d("Raju", "In prepare db");
        SharedPreferences sharedPreferences = getSharedPreferences(MyPreferences, MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final double lat = lats;
        final double longi = longs;
        final String Address1 = Address;


        Date todaydate = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        final String date1 = simpleDateFormat.format(todaydate);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(simpleDateFormat.parse(date1));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.add(Calendar.MONTH, 3);
        final String date2 = simpleDateFormat.format(c.getTime()); // first three months


        if (runcode == 1) {
         tz = TimeZone.getDefault().getID();
                    String querystring1 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date1 + "&end_date=" + date2;
                    new QuerytoAPI(MainActivity.this, 1, pd).execute(querystring1);
                    if (Address1 != " ") {
                         if (tz != null) {
                                editor.putInt("Location", 1);
                                editor.putString("Location1", Address1);
                                editor.putString("Timezone1", tz);
                                editor.putLong("Latitude1", Double.doubleToRawLongBits(lat));
                                editor.putLong("Longitude1", Double.doubleToRawLongBits(longi));
                                //   loadlocation(1);  UNable to load as the Backgound download process not completes before coming here..
                            editor.commit();
                        }
                    }
                }
           else if (runcode == 2)  // Call from Update Databse
        {
   tz = sharedPreferences.getString("Timezone1", "Asia/Kolkata");
            double latitude = Double.longBitsToDouble(sharedPreferences.getLong("Latitude1", 23));
            double longitude = Double.longBitsToDouble(sharedPreferences.getLong("Longitude1", 78));
            String querystring = "lat_deg=" + latitude + "&lon_deg=" + longitude + "&timeZoneId=" + tz + "&date=" + date1 + "&end_date=" + date2;
            Log.d("RAJU", querystring);
            new QuerytoAPI(MainActivity.this, 1, pd).execute(querystring);
        }

    }


    public void runforlocation() {
        Intent intent = new Intent(getApplicationContext(), ManualEntry.class);
        startActivityForResult(intent, 420);
    }


    public void loadlocation() {
        String MyPreferences = "Myprefs";   // to store some personalization variables - run, location, location no.

        SharedPreferences sharedPreferences = getSharedPreferences(MyPreferences, MODE_PRIVATE);
        currentlocation = 1;

        TextView tv1 = (TextView) findViewById(R.id.textViewLocation);
        TextView tv2 = (TextView) findViewById(R.id.textViewSunrise);
        tv2.setVisibility(View.VISIBLE);
        TextView tv3 = (TextView) findViewById(R.id.textViewSunset);
        TextView tv4 = (TextView) findViewById(R.id.textViewdate);
        tv3.setVisibility(View.VISIBLE);
        tv1.setText(sharedPreferences.getString("Location1", " Location Not Set ") + "\n" + sharedPreferences.getString("Timezone1", " TimeZone Not Set "));
        checkforupdate(1);


        DBhelper dBhelper = new DBhelper(this, 1);
        Date todaydate = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMMM yyyy");
        String date1 = simpleDateFormat.format(todaydate);
        Entrydate entrydate = dBhelper.getDate(date1);

        if (entrydate != null) {
            tv2.setVisibility(View.VISIBLE);
            tv3.setVisibility(View.VISIBLE);
            tv2.setText(entrydate.getSunrise());
            tv3.setText(entrydate.getSunset());
            Log.d("Piku", entrydate.getSunrise() + " " + entrydate.getDate());
        } else {
            //  tv2.setTextColor(Color.RED);
            // tv3.setTextColor(Color.RED);
            tv2.setText("");
            tv3.setText("");
        }

    }

    public void checkforupdate(int loc) {
        Date todaydate = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        final String date1 = simpleDateFormat.format(todaydate);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(simpleDateFormat.parse(date1));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DAY_OF_MONTH, 7);  // 7 days from today , to check whether we have data or not...
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.updatelayout);
        String d = simpleDateFormat.format(c.getTime());
        final DBhelper dBhelper = new DBhelper(this, loc);
        Entrydate entrydate = dBhelper.getDate(d);
        Log.d("Deva", date1 + " " + simpleDateFormat.format(c.getTime()));
        Log.d("Deva", " " + entrydate + " " + (dBhelper.getDate(date1)));

        if ((entrydate == null && dBhelper.getDate(date1) != null)) // i.e. today is available 7 days after is not...
        {
            ImageView updatebutton = (ImageView) findViewById(R.id.imageViewUpdate);
            linearLayout.setVisibility(View.VISIBLE);
            updatebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dBhelper.deleteallrecords();  // Cleearing all records first...
                    PrepareDB(0.0, 0.0, "null", 2); // Updating database with existing longi, lats
                    Log.d(" ERROR ", " do ka  " + currentlocation);
                    Toast.makeText(getApplicationContext(), "Updating...", Toast.LENGTH_SHORT).show();
                }
            });

            Log.d("Deva", "Here in if");
        } else {
            linearLayout.setVisibility(View.GONE);

            Log.d("Deva", "Here in else ");
        }

    }


    public void deletealllocation() {
final        String MyPreferences = "Myprefs";   // to store some personalization variables - run, location, location no.
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("Delete Location ? ");
        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences(MyPreferences, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("Location", 0);
                editor.putString("Location1", "Location Not Set ! ");
                editor.putString("Timezone1", "TimeZone Not Set ! ");
                editor.commit();

                DBhelper dBhelper = new DBhelper(MainActivity.this, -1);
                dBhelper.deleteall();
                TextView tv1 = (TextView) findViewById(R.id.textViewLocation);
                TextView tv2 = (TextView) findViewById(R.id.textViewSunrise);
                TextView tv3 = (TextView) findViewById(R.id.textViewSunset);
                tv1.setText("Location Not set, Click on Icon !");
                tv2.setVisibility(View.INVISIBLE);
                tv3.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Location deleted ! ", Toast.LENGTH_SHORT).show();
                }
        });
        adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
adb.create().show();
        }
}




