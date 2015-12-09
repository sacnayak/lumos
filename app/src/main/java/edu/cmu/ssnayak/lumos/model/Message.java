package edu.cmu.ssnayak.lumos.model;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by snayak on 12/7/15.
 * Model class to hold message details. To be used by MessageAdapter
 * and MessageActivity to render messages
 * @author snayak
 */
public class Message {

    private static final String TAG = "Message";

    private String _id;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String msgText;
    private String mLat;
    private String mLong;
    private String srcImage;
    private boolean isRead;

    private Date mDateTime;

    //Copy Constructor
    public Message(Message srcMessage) {
        this._id = srcMessage.get_id();
        this.senderId = srcMessage.getSenderId();
        this.senderName = srcMessage.getSenderName();
        this.msgText = srcMessage.getMsgText();
        this.mLat = srcMessage.getmLat();
        this.mLong = srcMessage.getmLong();
        this.srcImage = srcMessage.getSrcImage();
        this.isRead = srcMessage.isRead();
        this.mDateTime = srcMessage.getmDateTime();
        this.receiverId = srcMessage.getReceiverId();
        this.receiverName = srcMessage.getReceiverName();
    }

    public Message(String row_id, String senderId, String senderName, String msgText, String mLat, String mLong, String srcImage, boolean isRead, String mDateTime,
    String receiverId, String receiverName) {
        this._id = row_id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.msgText = msgText;
        this.mLat = mLat;
        this.mLong = mLong;
        this.srcImage = srcImage;
        this.isRead = isRead;
        this.receiverName = receiverName;
        this.receiverId = receiverId;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date dt = simpleDateFormat.parse(mDateTime);
            this.mDateTime = dt;
        } catch (Exception e) {
            Log.e(TAG, "Could not parse date in constructor");
        }

    }

    public String getSrcImage() {
        return srcImage;
    }

    public void setSrcImage(String srcImage) {
        this.srcImage = srcImage;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getmLat() {
        return mLat;
    }

    public void setmLat(String mLat) {
        this.mLat = mLat;
    }

    public String getmLong() {
        return mLong;
    }

    public void setmLong(String mLong) {
        this.mLong = mLong;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Date getmDateTime() {
        return mDateTime;
    }

    public void setmDateTime(Date mDateTime) {
        this.mDateTime = mDateTime;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}
