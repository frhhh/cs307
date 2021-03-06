package com.example.zuoyangding.aroundme.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.zuoyangding.aroundme.DataModels.GroupClass;
import com.example.zuoyangding.aroundme.DataModels.User;
import com.example.zuoyangding.aroundme.Manifest;
import com.example.zuoyangding.aroundme.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class add_group extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        ,GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //private FirebaseDatabase mDatabase;

    private DatabaseReference mGroupReference;
    //private DatabaseReference mUserRefernece;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    Button backButton;

    //Add by Frank
    private ImageButton GoBackBtm;

    EditText groupName;
    EditText groupTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        backButton = (Button) findViewById(R.id.createGroup);
        groupName = (EditText) findViewById(R.id.enterGroupName);
        groupTopic = (EditText) findViewById(R.id.Topics);
        mGroupReference = FirebaseDatabase.getInstance().getReference().child("Group");
        //mUserRefernece = FirebaseDatabase.getInstance().getReference().child("Users");

        //Add by Frank
        GoBackBtm = (ImageButton) findViewById(R.id.imageButton2);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        int MY_PERMISSION = 0;
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION);
        }
        else {
            createLocationRequest();
            mGoogleApiClient.connect();
        }
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String key = mGroupReference.child("Group").push().getKey();
                long start_date = System.currentTimeMillis();
                ArrayList<String> usr_ids = new ArrayList<String>();
                //User new_u;

                Global_variable global_variable = (Global_variable)getApplicationContext();
                final DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("Users");
                mref.child(global_variable.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> group_ids = (ArrayList<String>) dataSnapshot.child("groupIDs").getValue();
                        User new_u;
                        if(group_ids != null) {
                            group_ids.add(key);
                            mref.child(dataSnapshot.child("userID").getValue().toString()).child("groupIDs").setValue(group_ids);

                        }else {
                            group_ids = new ArrayList<String>();
                            group_ids.add(key);
                            mref.child(dataSnapshot.child("userID").getValue().toString()).child("groupIDs").setValue(group_ids);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                usr_ids.add(global_variable.getUser_id());
                GroupClass group = new GroupClass(groupName.getText().toString(), key, groupTopic.getText().toString(), start_date, null, usr_ids, mLastLocation.getLatitude(), mLastLocation.getLongitude() ,false);


                mGroupReference.child(key).setValue(group);
                mGroupReference.child(key).child("vote").setValue(1);
                mGroupReference.child(key).child("last_message").setValue(start_date);
                //mUserRefernece.child(global_variable.getUser_id()).setValue(new_u);
                Intent i = new Intent(add_group.this, homepage.class);
                startActivity(i);
            }
        });


        //Add by Frank
        GoBackBtm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i=new Intent(add_group.this, group_aroudme.class);
                add_group.this.startActivity(i);
            }
        });

    }
    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(add_group.this, "Connection Failed", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onConnectionSuspended(int cause){
        Toast.makeText(add_group.this, "TEMPORARY disconnected", Toast.LENGTH_LONG).show();
    };
    @Override
    public void onConnected(Bundle connectionHint) {
        //Toast.makeText(add_group.this, "onConnected", Toast.LENGTH_LONG).show();
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createLocationRequest();
                    mGoogleApiClient.connect();
                } else {
                    Intent i = new Intent(add_group.this, homepage.class);
                    startActivity(i);
                }
                return;
            }
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

}
