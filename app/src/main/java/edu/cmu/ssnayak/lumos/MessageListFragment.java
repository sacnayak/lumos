package edu.cmu.ssnayak.lumos;

import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * A simple extension of the Fragment class. Holds the view for all the texts.
 *
 */
public class MessageListFragment extends Fragment {

    private SimpleCursorAdapter adapter;
    ListView messageListView;

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

        messageListView = (ListView) root.findViewById(R.id.messageListView);

        //FIXME
        String[] columns = new String[] { "_id", "msg", "time" };

        MatrixCursor matrixCursor= new MatrixCursor(columns);
        getActivity().startManagingCursor(matrixCursor);
        matrixCursor.addRow(new Object[]{1, "Hello there", "15:30"});
        matrixCursor.addRow(new Object[]{2, "Hello there!", "15:31"});
        matrixCursor.addRow(new Object[]{3, "Hello there!!", "15:32" });
        String[] from = {BaseColumns._ID, "msg", "time"};
        int[] to = {R.id.text1, R.id.text2};
        //FIXME


        adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.message_list_item,
                matrixCursor,
                from,
                to,
                0);
        messageListView.setAdapter(adapter);
        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                //TODO
                //openConversation(names, i);
            }
        });

        return root;
    }
}
