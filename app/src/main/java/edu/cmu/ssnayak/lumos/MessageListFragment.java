package edu.cmu.ssnayak.lumos;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple extension of the Fragment class. Holds the view for all the texts.
 *
 */
public class MessageListFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_message_list, container, false);
    }
}
