package edu.cmu.ssnayak.lumos;


import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.ssnayak.lumos.client.Constants;
import edu.cmu.ssnayak.lumos.client.ServerUtilities;
import edu.cmu.ssnayak.lumos.data.DataProvider;

/**
 * Created by snayak on 12/1/15.
 * The fragment in the view pager that enables sending of messages based
 * on a location
 * @author snayak
 */
public class DropFragment extends Fragment implements LocationListener {

    private static final String TAG = "DropFragment";

    //HTTP request queue specific to volley library
    private RequestQueue queue;

    //Map fragment for the view
    private SupportMapFragment mMapFragment;
    //instance of GoogleMap
    private GoogleMap googleMap;

    //handler for background location request processing
    private Handler handler;

    //suggestion list for the location
    private ArrayList<String> mSuggestions = new ArrayList<String>();

    //Android widget for the autocomplete text view on the map fragment
    //which suggests location
    private AutoCompleteTextView searchLocationAutocomplete;

    //the send message button
    private Button sendMessageButton;

    //the map marker
    private Marker dropMarker;

    /**
     * update local instance of dropMarker if dragged
     */
    private GoogleMap.OnMarkerDragListener onMarkerDragListener = new GoogleMap.OnMarkerDragListener() {

        @Override
        public void onMarkerDragStart(Marker marker) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onMarkerDragStart");
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onMarkerDragEnd");
            //update marker object
            dropMarker = marker;
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onMarkerDrag");
        }

    };

    /**
     * On click listener for the send message button
     * Triggers sending a message to GCM
     */
    private View.OnClickListener sendMessage = new View.OnClickListener() {
        public void onClick(View view) {
            Button btn = (Button) view;
            TextView tv = (TextView) getView().findViewById(R.id.message);

            String receiver = ((EditText) getView().findViewById(R.id.receiver)).getText().toString();
            String message = ((EditText) getView().findViewById(R.id.message)).getText().toString();

            //basic error handling
            if(receiver.isEmpty()) {
                Toast.makeText(getActivity().getApplicationContext(),"Please enter a receiver",Toast.LENGTH_SHORT).show();
            } else if(message.isEmpty()) {
                Toast.makeText(getActivity().getApplicationContext(),"Please enter a message",Toast.LENGTH_SHORT).show();
            } else {

                //Grab the selected location from the map
                LatLng latLng = dropMarker.getPosition();

                String latitude = String.valueOf(latLng.latitude);
                String longitude = String.valueOf(latLng.longitude);
                Log.d("Receiver: ", receiver);
                Log.d("Sending Message:", message);
                Log.d("Tagged at Latitude: ", latitude);
                Log.d("Tagged at Longitude: ", longitude);
                //send the message to server -> GCM
                send(tv.getText().toString(), receiver, latitude, longitude);

                //clear the views
                ((EditText) getView().findViewById(R.id.receiver)).setText(null);
                ((EditText) getView().findViewById(R.id.message)).setText(null);

            }
        }
    };

    public DropFragment() {
        //default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //so that the EditText Views are visible when the keyboard slides up
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_drop, container, false);

        searchLocationAutocomplete = (AutoCompleteTextView) rootView.findViewById(R.id.searchLocation);
        handler = new Handler();

        searchLocationAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String place = ((TextView) view).getText().toString();
                onItemSelected(place, searchLocationAutocomplete);
            }
        });

        searchLocationAutocomplete.setThreshold(3);

        searchLocationAutocomplete.addTextChangedListener(new AbstractTextWatcher() {
            @Override
            public void afterTextChanged(final Editable s) {
                handler.removeCallbacks(null);
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchForPlaces(s, searchLocationAutocomplete);
                    }
                }, 1000);

            }
        });

        sendMessageButton = (Button) rootView.findViewById(R.id.send_button);
        sendMessageButton.setOnClickListener(sendMessage);

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(getActivity());
        //hide soft keyboard
        closeSoftKeyBoardAlways();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapFragment = (SupportMapFragment) (getChildFragmentManager().findFragmentById(R.id.map));
        initializeMap();
    }

    /**
     * Map initializer
     */
    private void initializeMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                googleMap.setOnMarkerDragListener(onMarkerDragListener);
                googleMap.setMyLocationEnabled(true);
                googleMap.setTrafficEnabled(true);
                googleMap.setBuildingsEnabled(true);
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

                List<String> providers = locationManager.getProviders(true);
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }

                Location bestLocation = null;
                for (String provider : providers) {
                    Location l = locationManager.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                        // Found best last known location: %s", l);
                        bestLocation = l;
                    }
                }

                if (bestLocation != null) {
                    onLocationChanged(bestLocation);
                }
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(provider, 120000, 0, this);

            }
        }
    }

    /**
     * Hide the SoftKeyboard
     */
    public void closeSoftKeyBoardAlways() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * When an item is selected from the suggestion drop down, this function is fired
     * Puts marker to the right spot on the map, clears view
     * @param place
     * @param view
     */
    public void onItemSelected(String place, AutoCompleteTextView view) {
        closeSoftKeyBoardAlways();
        view.clearListSelection();
        view.dismissDropDown();

        Log.d("Place", place);
        view.clearFocus();
        Geocoder mGeoCoder = new Geocoder(getActivity());
        List<Address> addresses = null;
        try {
            addresses = mGeoCoder.getFromLocationName(place, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            Address address = addresses.get(0);
            LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            if (view.getId() == R.id.searchLocation) {
                if (dropMarker != null) {
                    dropMarker.remove();
                }
                dropMarker = googleMap.addMarker(new MarkerOptions().title(place).position(latlng).draggable(true));
            }
        }

    }

    /**
     * Search Google Places and set autocomplete in Pickup and Drop fields.
     * Uses the volley api to send an HTTP request
     *
     * @param s,    AutoCompleteView query
     * @param view, AutoCompleteView field
     */
    private void searchForPlaces(Editable s, final AutoCompleteTextView view) {
        String inputQuery = s.toString();
        if (inputQuery.isEmpty()) {
            return;
        }
        StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?input=")
                .append(Uri.encode(inputQuery))
                .append("&key=" + Constants.PUBLIC_API_KEY)
                .append("&location=")
                .append(googleMap.getMyLocation().getLatitude() + "," + googleMap.getMyLocation().getLongitude());

        String url = new String(urlBuilder);
        Log.d(getClass().getSimpleName(), url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener() {

                    @Override
                    public void onResponse(Object res) {
                        try {
                            JSONObject responsePlaces = new JSONObject((String) res);
                            JSONArray predictionsArray = responsePlaces.getJSONArray("predictions");
                            mSuggestions.clear();
                            for (int i = 0; i < predictionsArray.length(); i++) {
                                JSONObject predictionObject = predictionsArray.getJSONObject(i);
                                String description = predictionObject.getString("description");
                                mSuggestions.add(description);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mSuggestions);
                            view.setAdapter(adapter);
                            view.showDropDown();
                            Log.d(getClass().getSimpleName(), mSuggestions.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //progressHelper.cancelNotification();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null)
                    Log.d("Error Response", error.getMessage());
                //progressHelper.cancelNotification();
            }
        }
        );
        queue.add(stringRequest);
    }

    /**
     * The Fragment implements LocationListener. We need to follow the user
     * as he moves around. Custom implementation.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        if (dropMarker != null) {
            dropMarker.remove();
        }
        dropMarker = googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    /**
     * Send a message in a background thread. Make Toast if fail, success.
     * @param txt
     * @param profileEmail
     * @param lat
     * @param llong
     */
    private void send(final String txt, final String profileEmail, final String lat, final String llong) {
        Log.d("Message sent is: " , txt);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "Your message has been sent";
                try {
                    ServerUtilities.send(txt, profileEmail, lat, llong);

                    ContentValues values = new ContentValues(6);
                    values.put(DataProvider.COL_MSG, txt);
                    values.put(DataProvider.COL_TO, profileEmail);
                    values.put(DataProvider.COL_FROM, Commons.getPreferredEmail());
                    values.put(DataProvider.COL_LAT, lat);
                    values.put(DataProvider.COL_LONG, llong);

                    //in a real scenario the sender would get an acknowledgement
                    values.put(DataProvider.COL_READ, 0);

                    //update sent message to local databse
                    getActivity().getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);

                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    //Feedback toast
                    Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    /**
     * Used for above TextWatcher implementations. Need not override each method always.
     */
    private class AbstractTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}
