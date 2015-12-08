package edu.cmu.ssnayak.lumos;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.ssnayak.lumos.data.ChatAdapter;
import edu.cmu.ssnayak.lumos.model.Message;

/**
 * Created by snayak on 12/7/15.
 */
public class MessageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView messageListView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SupportMapFragment mMapFragment;
    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_message);

        setTitle("Ajayan Subramanian");

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
        messageList.add(new Message("Ajayan Subramanian", "Get my pendrive", "23.49", "-43.56" , "", true));
        messageList.add(new Message("Ajayan Subramanian", "Get my pendrive", "23.49", "-43.56" , "", true));
        messageList.add(new Message("Ajayan Subramanian", "Get my pendrive", "23.49", "-43.56" , "", true));
        mAdapter = new ChatAdapter(this, messageList);
        messageListView.setAdapter(mAdapter);

        //work with the map
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_chat);
        initializeMap();
    }

    private void initializeMap() {
        //TODO
        // Try to obtain the map from the SupportMapFragment.
        googleMap = mMapFragment.getMap();
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
