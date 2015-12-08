package edu.cmu.ssnayak.lumos.model;

/**
 * Created by snayak on 12/7/15.
 */
public class Message {

    private String senderName;
    private String msgText;
    private String mLat;
    private String mLong;
    private String srcImage;
    private boolean isRead;

    private String senderId;

    public Message(String senderId, String senderName, String msgText, String mLat, String mLong, String srcImage, boolean isRead) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.msgText = msgText;
        this.mLat = mLat;
        this.mLong = mLong;
        this.srcImage = srcImage;
        this.isRead = isRead;
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
}
