package edu.cmu.ssnayak.lumos.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import edu.cmu.ssnayak.lumos.R;
import edu.cmu.ssnayak.lumos.model.Message;

/**
 * Created by snayak on 12/7/15.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;


    public ChatAdapter(Context context, List<Message> messageList) {
        this.messageList = messageList;
        this.context = context;
    }

    public void swap(List<Message> newMessageList) {
        messageList.clear();
        messageList.addAll(newMessageList);
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View messageView = inflater.inflate(R.layout.chat_list_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(messageView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder viewHolder, int position) {
        Message message = messageList.get(position);

        TextView nameTextView = viewHolder.messageTextView;
        nameTextView.setText(message.getMsgText());

        TextView senderTextView = viewHolder.senderTextView;
        senderTextView.setText(message.getSenderName());
    }

    @Override
    public int getItemCount() {
        int count = 0;

        Iterator<Message> messageIterator = this.messageList.iterator();
        while (messageIterator.hasNext()) {
            if(messageIterator.next().isRead()) {
                count++;
            }
        }

        return count;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView senderTextView;
        public TextView messageTextView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            senderTextView = (TextView) itemView.findViewById(R.id.senderName);
            messageTextView = (TextView) itemView.findViewById(R.id.senderMessage);
        }
    }


}
