package com.example.tomer.agnihotramitralite;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ManualEntry extends AppCompatActivity  {
    LocationManager lm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);
        Button button = (Button) findViewById(R.id.buttonuselocation);
      final EditText tv1 = (EditText) findViewById(R.id.editTextLat); // Latitude
       final EditText tv2 = (EditText) findViewById(R.id.EditTextLong);  // Longitude
        final EditText tv3 = (EditText) findViewById(R.id.editTextName);  // name
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lats,longs;
                Intent intent = new Intent();
                final String tvtext1= tv1.getText().toString();
                final String tvtext2= tv2.getText().toString();
                final String tvtext3= tv3.getText().toString();
                try {
                    lats = Double.parseDouble(tvtext1);
                    longs = Double.parseDouble(tvtext2);
                    if(lats>-91 && lats<91 && longs > -181 && longs < 181 ) {
                        intent.putExtra("Latitude", lats);
                        intent.putExtra("Longitude", longs);
                        intent.putExtra("Address",tvtext3);
                        setResult(420, intent);
                        Log.d(" Raju ", lats + " " + longs);
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Invalid Latitude or Longitude ", Toast.LENGTH_SHORT);
                }
                catch (NumberFormatException e)
                {
                    Toast.makeText(getApplicationContext(),"Invalid Latitude or Longitude ", Toast.LENGTH_SHORT);
                    Log.d( " Raju ", " FORMAT ERROR ");
                }

                finish();
            }
        });
//       final GPSTracker gps = new GPSTracker(ManualEntry.this);

        // check if GPS enabled

        Button btnloc = (Button) findViewById(R.id.buttonfindlocation);
        btnloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
  /*              if (gps.canGetLocation()) {
                    gps.getLocation();
                    double latitude11 = gps.getLatitude();
                    double longitude11 = gps.getLongitude();
                    tv1.setText(String.valueOf(latitude11));
                    tv2.setText(String.valueOf(longitude11));
                }
            }
        });
    }
} */
               int permissionCheck = ContextCompat.checkSelfPermission(ManualEntry.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
double longitude,latitude;

                if(permissionCheck== PackageManager.PERMISSION_GRANTED) {
                     LocationListener listener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    };
                      lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,listener);
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location location2 = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {  // First Try GPS ...
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            tv1.setText(String.valueOf(latitude));
                            tv2.setText(String.valueOf(longitude));
                            Toast.makeText(getApplicationContext(),"GPS Location Received", Toast.LENGTH_SHORT).show();
                        } else if (location2 != null) {  // If failed try NETWORK
                            longitude = location2.getLongitude();
                            latitude = location2.getLatitude();
                            tv1.setText(String.valueOf(latitude));
                            tv2.setText(String.valueOf(longitude));
                            Toast.makeText(getApplicationContext(),"Network Location Received", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), " Unable to get location coordinates, either enter manually or check GPS and try again in sometime.", Toast.LENGTH_LONG).show();
                        }

                    } } });

    //            else {
      //              Toast.makeText(getApplicationContext(), " Please turn on GPS ! ",Toast.LENGTH_LONG ).show();}}
             //   else
               // {
                  //  Toast.makeText(getApplicationContext(), " App doesn't have permission to access GPS !  Please change settings. ", Toast.LENGTH_LONG).show();
             //   } });

            }


    }




