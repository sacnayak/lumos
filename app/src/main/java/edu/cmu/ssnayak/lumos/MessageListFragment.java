package edu.cmu.ssnayak.lumos;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.ssnayak.lumos.client.Constants;
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

    private SimpleCursorAdapter adapter;

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
        List<Message> messageList = new ArrayList<Message>();
        populateMessageList(messageList);

        mAdapter = new MessageAdapter(getActivity(), messageList);
        messageListView.setAdapter(mAdapter);

        return root;
    }

    private void populateMessageList(List<Message> messageList) {
        Cursor c = getActivity().getContentResolver().query(DataProvider.CONTENT_URI_MESSAGES, null, null, null, null);
        while(c.moveToNext()) {
            //check if messages table has messages which are unread
            if(c.getInt(c.getColumnIndex(DataProvider.COL_READ)) == 0) {
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



}
