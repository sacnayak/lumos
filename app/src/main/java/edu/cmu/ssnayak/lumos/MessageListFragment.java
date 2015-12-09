package edu.cmu.ssnayak.lumos;

import android.database.Cursor;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.cmu.ssnayak.lumos.data.DataProvider;
import edu.cmu.ssnayak.lumos.data.MessageAdapter;
import edu.cmu.ssnayak.lumos.model.Message;

/**
 * A simple extension of the Fragment class. Holds the view for all the texts.
 *
 */
public class MessageListFragment extends Fragment {

    private RecyclerView messageListView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    HashMap<String, List<Message>> messageMap;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DropFragment.
     */
    public static MessageListFragment newInstance() {
        MessageListFragment fragment = new MessageListFragment();
        return fragment;
    }

    public MessageListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_message_list, container, false);

        messageListView = (RecyclerView) root.findViewById(R.id.message_list_view);
        //since items are not dynamic for now, improves performance
        messageListView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        messageListView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)

        populateMessageList();
        List<Message> displayMessageList = new ArrayList<Message>();
        populateDisplayMessageList(displayMessageList);
        mAdapter = new MessageAdapter(getActivity(), displayMessageList);
        messageListView.setAdapter(mAdapter);

        return root;
    }

    private void populateDisplayMessageList(List<Message> displayMessageList) {
        for (String key : this.messageMap.keySet()) {
            List<Message> messages = messageMap.get(key);
            //add latest message to display
            displayMessageList.add(new Message(messages.get(0)));
        }
    }

    private void populateMessageList() {
        //initialize or re-initialize messageMap
        messageMap = new HashMap<String, List<Message>>();

        Cursor c = getActivity().getContentResolver().query(DataProvider.CONTENT_URI_MESSAGES, null, null, null, null);
        while(c.moveToNext()) {
            //check if messages table has messages which are unread
            if(c.getInt(c.getColumnIndex(DataProvider.COL_READ)) == 1) {
                String row_id = c.getString(c.getColumnIndex(DataProvider.COL_ID));
                String from = c.getString(c.getColumnIndex(DataProvider.COL_FROM));
                String to = c.getString(c.getColumnIndex(DataProvider.COL_TO));
                String msgtxt = c.getString(c.getColumnIndex(DataProvider.COL_MSG));
                String lat = c.getString(c.getColumnIndex(DataProvider.COL_LAT));
                String llong = c.getString(c.getColumnIndex(DataProvider.COL_LONG));
                String dateTime = c.getString(c.getColumnIndex(DataProvider.COL_TIME));
                Message message = new Message(row_id, from, Commons.profileMap.get(from), msgtxt,lat, llong, "", true, dateTime,
                        to, Commons.profileMap.get(to));

                if(messageMap.get(from) != null) {
                    //if there already is a message in this location
                    List<Message> messageList = messageMap.get(from);
                    messageList.add(message);
                    Collections.sort(messageList, new Commons.MessageComparator());
                    messageMap.put(from, messageList);
                } else {
                    //if there is no message in this location
                    List<Message> messageList = new ArrayList<Message>();
                    messageList.add(message);
                    Collections.sort(messageList, new Commons.MessageComparator());
                    messageMap.put(from, messageList);
                }
            }
            c.move(1);
        }
    }



}
