package edu.cmu.ssnayak.lumos;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.ssnayak.lumos.data.ChatAdapter;
import edu.cmu.ssnayak.lumos.data.DataProvider;
import edu.cmu.ssnayak.lumos.model.Message;

/**
 * Created by snayak on 12/7/15.
 */
public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";

    private Toolbar toolbar;
    private RecyclerView messageListView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SupportMapFragment mMapFragment;
    private GoogleMap googleMap;
    private String chatID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String chatId = intent.getStringExtra("ChatId");
        String msgId = intent.getStringExtra("MsgID");
        this.chatID = chatId;
        Log.d(TAG, "Intent key:" + chatId);

        setContentView(R.layout.activity_message);
        setTitle(Commons.profileMap.get(chatId));

        toolbar = (Toolbar) findViewById(R.id.messageToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messageListView = (RecyclerView) findViewById(R.id.chat_list_view);
        //since items are not dynamic for now, improves performance
        messageListView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        messageListView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        List<Message> messageList = new ArrayList<Message>();
        populateMessageList(messageList);
        mAdapter = new ChatAdapter(this, messageList);
        messageListView.setAdapter(mAdapter);

        //work with the map
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_chat);
        initializeMap(messageList);
    }

    private void populateMessageList(List<Message> messageList) {

        Cursor c = getContentResolver().query(DataProvider.CONTENT_URI_MESSAGES, null, null, null, null);
        while(c.moveToNext()) {
            //check if messages table has messages which are read
            if(c.getString(c.getColumnIndex(DataProvider.COL_FROM)).equalsIgnoreCase(this.chatID) &&
                    c.getInt(c.getColumnIndex(DataProvider.COL_READ)) == 1) {
                String from = c.getString(c.getColumnIndex(DataProvider.COL_FROM));
                String msgtxt = c.getString(c.getColumnIndex(DataProvider.COL_MSG));
                String lat = c.getString(c.getColumnIndex(DataProvider.COL_LAT));
                String llong = c.getString(c.getColumnIndex(DataProvider.COL_LONG));
                Message message = new Message(from, Commons.profileMap.get(from), msgtxt,lat, llong, "", true);
                messageList.add(message);
            }
            c.move(1);
        }
    }

    private void initializeMap(List<Message> messageList) {
        // Try to obtain the map from the SupportMapFragment.
        googleMap = mMapFragment.getMap();

        // Check if we were successful in obtaining the map.
        if (googleMap != null) {

        }
    }

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

}
