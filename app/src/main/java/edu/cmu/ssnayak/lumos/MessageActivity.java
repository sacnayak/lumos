package edu.cmu.ssnayak.lumos;

import android.content.Intent;
import android.database.Cursor;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.ssnayak.lumos.data.DataProvider;
import edu.cmu.ssnayak.lumos.model.Message;

/**
 * Created by snayak on 12/7/15.
 * Activity to display conversation between two people. Map on to activity_message.xml layout
 * Shows a map with multiple markers indicating various messages left at many locations between
 * you and the other user.
 * Blue marker are the ones user received
 * Red markers are the ones user sent
 * Yellow is the highlighted marker - either latest, or user clicked on it.
 */
public class MessageActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MessageActivity";

    private Toolbar toolbar;
//    private RecyclerView messageListView;
//    private RecyclerView.Adapter mAdapter;
//    private RecyclerView.LayoutManager mLayoutManager;

    //Required for display of Google Map Fragments
    private SupportMapFragment mMapFragment;
    private GoogleMap googleMap;

    //the conversation id or the email id of the sender
    private String chatID;
    //The row id in the table for which the message notification popped out
    private String notificationMsgId;
    //list of markers to be displayed on the UI
    List<Marker> dropMarkers;
    //a map of table row_id vs message object for this conversation
    //maintained for easy access
    Map<String, Message> messageMap;

    //displaying a yellow marker for the latest message OR
    //the clicked marker-message
    private String activeMessageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String chatId = intent.getStringExtra("ChatId");
        String msgId = intent.getStringExtra("MsgID");
        this.chatID = chatId;
        this.notificationMsgId = msgId;
        Log.d(TAG, "Intent keys:" + chatId + ", " + msgId);

        setContentView(R.layout.activity_message);
        setTitle(Commons.profileMap.get(chatId));

        toolbar = (Toolbar) findViewById(R.id.messageToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        populateMessageMap();
        // specify an adapter (see also next example)

        //work with the map
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_chat);
        initializeMap();

        //quick hack to display latest message
        setActiveMessageId();

        List<Message> displayMessage = new ArrayList<Message>();
        //displayMessage.add(messageMap.get(this.activeMessageId));

        setActiveMarker(this.activeMessageId);

        //Switched from recyclerView last minute
        TextView senderName = (TextView) findViewById(R.id.senderName);
        senderName.setText(messageMap.get(this.activeMessageId).getSenderName());
        TextView senderMessage = (TextView) findViewById(R.id.senderMessage);
        senderMessage.setText(messageMap.get(this.activeMessageId).getMsgText());
    }

    /**
     * Logic for setting the message to be highlighted on the map
     * If redirected from android notification, or if clicked by user,
     * or just the latest message
     */
    private void setActiveMessageId() {
        if(this.notificationMsgId != null) {
            activeMessageId = this.notificationMsgId;
            //clear notificationMsgId
            this.notificationMsgId = null;
            return;
        }

        List<Message> receivedMessages = new ArrayList<Message>();

        for (String key : this.messageMap.keySet()) {
            Message message = messageMap.get(key);
            //if you are the receiver then add to list.
            if(message.getReceiverId().equalsIgnoreCase(Commons.getPreferredEmail())) {
                receivedMessages.add(message);
            }
        }
        //sort in descending order
        Collections.sort(receivedMessages, new Commons.MessageComparator());
        //pick the latest of the list and return
        this.activeMessageId = receivedMessages.get(0).get_id();
        return;
    }

    /**
     * Logic to color the active marker to yellow (based on activeMessageId)
     * and pan the map camera to desired location
     * @param msgId
     */
    private void setActiveMarker(String msgId) {
        for(Marker marker : dropMarkers) {
            if(msgId.equalsIgnoreCase(marker.getSnippet())) {
                //set Active Marker
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                //refocus camera
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));

                return;
            }
        }
    }

    /**
     * Query the DB to fetch relevant messages for this conversation
     */
    private void populateMessageMap() {
        //initialize or re-initialize messageMap
        messageMap = new HashMap<String, Message>();

        Cursor c = getContentResolver().query(DataProvider.CONTENT_URI_MESSAGES, null, null, null, null);
        while(c.moveToNext()) {
            //check if messages table has messages which are read
            if((c.getString(c.getColumnIndex(DataProvider.COL_FROM)).equalsIgnoreCase(this.chatID)
            || c.getString(c.getColumnIndex(DataProvider.COL_TO)).equalsIgnoreCase(this.chatID)) &&
                    c.getInt(c.getColumnIndex(DataProvider.COL_READ)) == 1) {
                String _id = c.getString(c.getColumnIndex(DataProvider.COL_ID));
                String from = c.getString(c.getColumnIndex(DataProvider.COL_FROM));
                String to = c.getString(c.getColumnIndex(DataProvider.COL_TO));
                String msgtxt = c.getString(c.getColumnIndex(DataProvider.COL_MSG));
                String lat = c.getString(c.getColumnIndex(DataProvider.COL_LAT));
                String llong = c.getString(c.getColumnIndex(DataProvider.COL_LONG));
                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(llong));
                String dateTime = c.getString(c.getColumnIndex(DataProvider.COL_TIME));
                Message message = new Message(_id, from, Commons.profileMap.get(from), msgtxt,lat, llong, "", true, dateTime,
                        to, Commons.profileMap.get(to));

                //populate the map
                messageMap.put(_id, message);

            }
            c.move(1);
        }
    }

    /**
     * Initialize the map to be shown and place all the
     * markers appropriately
     */
    private void initializeMap() {
        // Try to obtain the map from the SupportMapFragment.
        googleMap = mMapFragment.getMap();
        dropMarkers = new ArrayList<Marker>();

        //set listener
        googleMap.setOnMarkerClickListener(this);

        // Check if we were successful in obtaining the map.
        if (googleMap != null) {
            for (String key : this.messageMap.keySet()) {
                Message message = this.messageMap.get(key);
                LatLng latLng = new LatLng(Double.parseDouble(message.getmLat()), Double.parseDouble(message.getmLong()));
                if(message.getReceiverId().equals(Commons.getPreferredEmail())) {
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .snippet(message.get_id()));//tag each marker with message id
                    dropMarkers.add(marker);
                } else {
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .snippet(message.get_id()));//tag each marker with message id
                    dropMarkers.add(marker);
                }
            }

        }
    }

    /**
     * Back navigation
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Incase the user clicks on a custom marker, switch colors
     * and display the associated message below.
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        String msgId = marker.getSnippet();
        if(msgId.equalsIgnoreCase(this.activeMessageId)) {
            //don't do anything if the same marker is clicked
            return true;
        }
        swapActiveMarkers(msgId);

        List<Message> displayMessage = new ArrayList<Message>();
        displayMessage.add(messageMap.get(this.activeMessageId));

        //Switched from recyclerView last minute
        TextView senderName = (TextView) findViewById(R.id.senderName);
        senderName.setText(messageMap.get(this.activeMessageId).getSenderName());
        TextView senderMessage = (TextView) findViewById(R.id.senderMessage);
        senderMessage.setText(messageMap.get(this.activeMessageId).getMsgText());

        //else udpate view with message associated with new marker
//        messageListView = (RecyclerView) findViewById(R.id.chat_list_view);
//
//        messageListView.swapAdapter(mAdapter,false);

        return true;
    }

    /**
     * utility function to swap between active marker
     * and clicked marker
     * @param msgId
     */
    private void swapActiveMarkers(String msgId) {
        //reset active marker
        for(Marker marker : dropMarkers) {
            if(this.activeMessageId.equalsIgnoreCase(marker.getSnippet())) {
                //set Active Marker
                String receiverId = this.messageMap.get(activeMessageId).getReceiverId();
                if(receiverId.equalsIgnoreCase(Commons.getPreferredEmail())) {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                } else {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
            } else if(msgId.equalsIgnoreCase(marker.getSnippet())) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            }
        }
        this.activeMessageId = msgId;

    }
}
