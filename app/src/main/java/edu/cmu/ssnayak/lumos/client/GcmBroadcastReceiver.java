package edu.cmu.ssnayak.lumos.client;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;

import edu.cmu.ssnayak.lumos.Commons;
import edu.cmu.ssnayak.lumos.MainActivity;
import edu.cmu.ssnayak.lumos.R;
import edu.cmu.ssnayak.lumos.data.DataProvider;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "GcmBroadcastReceiver";
	
	private Context ctx;	

	@Override
	public void onReceive(Context context, Intent intent) {
		ctx = context;
        Log.d("Yahooo!", "Message Received!");
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();
		
		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
			
			String messageType = gcm.getMessageType(intent);
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("Send error", false);
				
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server", false);
				
			} else {
				String msg = intent.getStringExtra(DataProvider.COL_MSG);
				String email = intent.getStringExtra(DataProvider.COL_FROM);
                String lat = intent.getStringExtra(DataProvider.COL_LAT);
                String llong = intent.getStringExtra(DataProvider.COL_LONG);
                Log.d("Msg", msg);
                Log.d("From", email);
                Log.d("To", Commons.getPreferredEmail());
                Log.d("lat", lat);
                Log.d("long", llong);

                ContentValues values = new ContentValues(6);
				values.put(DataProvider.COL_MSG, msg);
				values.put(DataProvider.COL_FROM, email);
				values.put(DataProvider.COL_TO, Commons.getPreferredEmail());
                values.put(DataProvider.COL_LAT, lat);
                values.put(DataProvider.COL_LONG, llong);
                values.put(DataProvider.COL_READ, 0);//default unread

				context.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
				
				if (Commons.isNotify()) {
					sendNotification("New message", true);
				}
			}
			setResultCode(Activity.RESULT_OK);
			
		} finally {
			mWakeLock.release();
		}
	}
	
	private void sendNotification(String text, boolean launchApp) {
		NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		//FIXME
		Notification.Builder mBuilder = new Notification.Builder(ctx)
			.setAutoCancel(true)
                .setContentTitle(ctx.getString(R.string.app_name))
                    .setContentText(text);

		if (!TextUtils.isEmpty(Commons.getRingtone())) {
			mBuilder.setSound(Uri.parse(Commons.getRingtone()));
		}
		
		if (launchApp) {
			Intent intent = new Intent(ctx, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(pi);
		}
		
		mNotificationManager.notify(1, mBuilder.getNotification());
	}
}
