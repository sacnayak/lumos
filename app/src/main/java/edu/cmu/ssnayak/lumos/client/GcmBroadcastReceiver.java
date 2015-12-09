package edu.cmu.ssnayak.lumos.client;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import edu.cmu.ssnayak.lumos.Commons;
import edu.cmu.ssnayak.lumos.data.DataProvider;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * The GCM Broadcast Receiver receives push notification from the Google
 * Cloud Messaging server and stores it locally in SQLite
 * @author snayak
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GcmBroadcastReceiver";

    private Context context;

    /**
     * On receive of a broadcast to be overriden
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.d("Yahooo!", "Message Received!");
        //Acquire wake lock to process intent received from queue
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

            String messageType = gcm.getMessageType(intent);
            //process data sent in from GCM
            String msg = intent.getStringExtra(DataProvider.COL_MSG);
            String email = intent.getStringExtra(DataProvider.COL_FROM);
            String lat = intent.getStringExtra(DataProvider.COL_LAT);
            String llong = intent.getStringExtra(DataProvider.COL_LONG);
            Log.d(TAG, "Msg: " + msg);
            Log.d(TAG, "From: " + email);
            Log.d(TAG, "To: " + Commons.getPreferredEmail());
            Log.d(TAG, "lat: " + lat);
            Log.d(TAG, "long: " + llong);

            //insert received message in to database currently
            //mark 'read' as unread so that it pops up at the appropriate location
            ContentValues values = new ContentValues(6);
            values.put(DataProvider.COL_MSG, msg);
            values.put(DataProvider.COL_FROM, email);
            values.put(DataProvider.COL_TO, Commons.getPreferredEmail());
            values.put(DataProvider.COL_LAT, lat);
            values.put(DataProvider.COL_LONG, llong);
            values.put(DataProvider.COL_READ, 0);//default unread

            //insert
            context.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
            //operation succeeded
            setResultCode(Activity.RESULT_OK);
        } finally {
            mWakeLock.release();
        }
    }
}
