package edu.cmu.ssnayak.lumos.client;

/**
 * Consists of constants to be used throughout the app, in order
 * to interface with Google Cloud Messaging
 * @author snayak
 */
public interface Constants {

    /**
     * Base URL of the Demo Server which forwards message to GCM
     */
	public String SERVER_URL = "http://lumos-server-1149.appspot.com";

    /**
     * Google API project id registered to use GCM.
     */
	public String SENDER_ID = "636712503528";

    /**
     * Google Maps API key
     */
    public String PUBLIC_API_KEY = "AIzaSyCPlxkR74t3eAwL0bL8HLDQM4qARZXH-2w";

}
