package edu.cmu.ssnayak.lumos.data;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;


import edu.cmu.ssnayak.lumos.MessageActivity;
import edu.cmu.ssnayak.lumos.R;
import edu.cmu.ssnayak.lumos.model.Message;

/**
 * Created by snayak on 12/7/15.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.messageList = messageList;
        this.context = context;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View messageView = inflater.inflate(R.layout.message_list_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(messageView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder viewHolder, int position) {

        Message message = messageList.get(position);

        TextView nameTextView = viewHolder.nameTextView;
        nameTextView.setText(message.getSenderName());

        TextView locationTextView = viewHolder.locationTextView;
        locationTextView.setText(message.getMsgText());

        viewHolder.chatID = message.getSenderName();

        //ImageView contactImage = viewHolder.imageContact;
        //FIXME Hardcoded image
        //contactImage.setImageDrawable(context.getResources().);

    }

    // Return the total count of messages (that are marked 'isRead')
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
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView locationTextView;
        public String chatID;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.senderName);
            locationTextView = (TextView) itemView.findViewById(R.id.senderDropLocation);

            //setting up the onclicklistener
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent= new Intent(context, MessageActivity.class);
            intent.putExtra("ChatId", this.chatID);
            context.startActivity(intent);
        }

    }

}
