package edu.cmu.ssnayak.lumos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Patterns;

import edu.cmu.ssnayak.lumos.client.Constants;
import edu.cmu.ssnayak.lumos.model.Message;

/**
 *
 */
public class Commons extends Application {

    public static final String PROFILE_ID = "profile_id";

    public static final String ACTION_REGISTER = "edu.cmu.ssnayak.lumos.REGISTER";
    public static final String EXTRA_STATUS = "status";
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 0;

    //parameters recognized by demo server
    public static final String FROM = "email";
    public static final String REG_ID = "regId";
    public static final String MSG = "msg";
    public static final String TO = "email2";
    public static final String LAT = "lat";
    public static final String LONG = "long";

    public static String[] email_arr;

    private static SharedPreferences prefs;

    public static final Map<String, String> profileMap;
    static
    {
        profileMap = new HashMap<String, String>();
        profileMap.put("sachin.nayak101@gmail.com", "Sachin Nayak");
        profileMap.put("ajayan.subramanian@gmail.com", "Ajayan Subramanian");
        profileMap.put("samarth7b@gmail.com", "Samarth Bahuguna");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        List<String> emailList = getEmailList();
        email_arr = emailList.toArray(new String[emailList.size()]);
    }

    private List<String> getEmailList() {
        List<String> lst = new ArrayList<String>();
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                lst.add(account.name);
            }
        }
        return lst;
    }

    public static String getPreferredEmail() {
        return prefs.getString("chat_email_id", email_arr.length==0 ? "abc@example.com" : email_arr[0]);
    }

    public static String getDisplayName() {
        String email = getPreferredEmail();
        return prefs.getString("display_name", email.substring(0, email.indexOf('@')));
    }

    public static boolean isNotify() {
        return prefs.getBoolean("notifications_new_message", true);
    }

    public static String getRingtone() {
        return prefs.getString("notifications_new_message_ringtone", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    public static String getServerUrl() {
        return prefs.getString("server_url_pref", Constants.SERVER_URL);
    }

    public static String getSenderId() {
        return prefs.getString("sender_id_pref", Constants.SENDER_ID);
    }

    static class MessageComparator implements Comparator<Message>
    {
        public int compare(Message m1, Message m2) {
            return m2.getmDateTime().compareTo(m1.getmDateTime());
        }
    }

}
