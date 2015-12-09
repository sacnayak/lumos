package edu.cmu.ssnayak.lumos.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

import edu.cmu.ssnayak.lumos.Commons;
import edu.cmu.ssnayak.lumos.MainActivity;
import edu.cmu.ssnayak.lumos.MessageActivity;
import edu.cmu.ssnayak.lumos.R;
import edu.cmu.ssnayak.lumos.data.DataProvider;


/**
 * Created by snayak on 12/5/15.
 */
public class LocationService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    private static final String TAG = "****LocationService****";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final float ACCURACY = 25;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;

    @Override
    public void onCreate(){
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.d(TAG, "Firing onCreate:");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand fired");
        //log error dialog if GooglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            Log.d(TAG, "GooglePlayServiceUnavailable");
        }
        createLocationRequest();
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    protected void createLocationRequest() {
        Log.d(TAG, "createLocationRequest fired");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "GooglePlayServiceUnavailable", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected : " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged: ");
        mCurrentLocation = location;
        Log.d(TAG, String.valueOf(mCurrentLocation.getLatitude()));
        Log.d(TAG, String.valueOf(mCurrentLocation.getLongitude()));

        isNotify(mCurrentLocation);

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    private boolean isNotify(Location currentLocation) {
        Cursor c = getContentResolver().query(DataProvider.CONTENT_URI_MESSAGES, null, null, null, null);
        while(c.moveToNext()) {
            //check if messages table has messages which are unread
            if((c.getInt(c.getColumnIndex(DataProvider.COL_READ)) == 0) && inVicinity(c) &&
                    (c.getString(c.getColumnIndex(DataProvider.COL_TO)).equalsIgnoreCase(Commons.getPreferredEmail()))) {
                publishMessage(c);
            }
            c.move(1);
        }
        return false;
    }

    private boolean inVicinity(Cursor cursor) {
        if(mCurrentLocation == null) {return false;}
        double messageLatitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DataProvider.COL_LAT)));
        double messageLongitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DataProvider.COL_LONG)));

        Location location = new Location("MessageLocation");
        location.setLatitude(messageLatitude);
        location.setLongitude(messageLongitude);

        float distance = mCurrentLocation.distanceTo(location);
        boolean inVicinity = false;
        if(distance < ACCURACY) {
            inVicinity = true;
            Log.d(TAG, "inVicinity");
        }
        if(!inVicinity) {
            Log.d(TAG, "notInVicinity");
        }
        return inVicinity;
    }

    private void publishMessage(Cursor cursor) {
        // prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("ChatId", cursor.getString(cursor.getColumnIndex(DataProvider.COL_FROM)));
        intent.putExtra("MsgID", cursor.getString(cursor.getColumnIndex(DataProvider.COL_ID)));

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification notification  = new Notification.Builder(this)
                .setContentTitle(Commons.profileMap.get(cursor.getString(cursor.getColumnIndex(DataProvider.COL_FROM))))
                .setContentText(cursor.getString(cursor.getColumnIndex(DataProvider.COL_MSG)))
                .setSmallIcon(R.drawable.ic_plusone_standard_off_client)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();

        String _id = cursor.getString(cursor.getColumnIndex(DataProvider.COL_ID));
        //update DB as read
        ContentValues values = new ContentValues(2);
        values.put(DataProvider.COL_ID, _id);
        values.put(DataProvider.COL_READ, 1);

        getContentResolver().update(DataProvider.CONTENT_URI_MESSAGES, values, DataProvider.COL_ID+"=?",new String[] {String.valueOf(_id)});

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
