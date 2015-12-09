package edu.cmu.ssnayak.lumos;

import android.database.Cursor;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.cmu.ssnayak.lumos.data.DataProvider;
import edu.cmu.ssnayak.lumos.data.MessageAdapter;
import edu.cmu.ssnayak.lumos.model.Message;

/**
 * A simple extension of the Fragment class. Holds the view for all the messages
 * received. This is implemented using a RecyclerView (LinearLayoutManager)
 * Adapter for the view is DataProvider
 * @author snayak
 */
public class MessageListFragment extends Fragment {

    private RecyclerView messageListView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //email id vs list of message map
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

    /**
     * Operations to be done on Create of Fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
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
        //create email vs messagelist map from db
        populateMessageList();
        //done such that only the latest message is displayed in the screen
        List<Message> displayMessageList = new ArrayList<Message>();
        populateDisplayMessageList(displayMessageList);

        //instantiate the adapter for the recycler view
        mAdapter = new MessageAdapter(getActivity(), displayMessageList);
        //associate adapter to view
        messageListView.setAdapter(mAdapter);

        return root;
    }

    /**
     * ensures that only the latest message is dispalyed for each
     * conversation
     * @param displayMessageList
     */
    private void populateDisplayMessageList(List<Message> displayMessageList) {
        for (String key : this.messageMap.keySet()) {
            List<Message> messages = messageMap.get(key);
            //add latest message to display
            displayMessageList.add(new Message(messages.get(0)));
        }
    }

    /**
     * From the DB, creates a email id vs message list map
     * of all readable messages (messages that have been received)
     */
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
                    //sort in descending order such that the latest message is on top of the list
                    Collections.sort(messageList, new Commons.MessageComparator());
                    messageMap.put(from, messageList);
                } else {
                    //if there is no message in this location
                    List<Message> messageList = new ArrayList<Message>();
                    messageList.add(message);
                    //sort in descending order such that the latest message is on top of the list
                    Collections.sort(messageList, new Commons.MessageComparator());
                    messageMap.put(from, messageList);
                }
            }
            c.move(1);
        }
    }



}
