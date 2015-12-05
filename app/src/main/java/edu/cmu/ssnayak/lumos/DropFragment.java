package edu.cmu.ssnayak.lumos;


import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
 */
public class DropFragment extends Fragment implements LocationListener {

    private RequestQueue queue;
    private SupportMapFragment mMapFragment;
    private GoogleMap googleMap;
    private Handler handler;
    private ArrayList<String> mSuggestions = new ArrayList<String>();
    private AutoCompleteTextView searchLocationAutocomplete;
    private Button sendMessageButton;
    private Marker dropMarker;



    private View.OnClickListener sendMessage = new View.OnClickListener() {
        public void onClick(View view) {
            Button btn = (Button) view;
            TextView tv = (TextView) getView().findViewById(R.id.message);

            String receiver = ((EditText) getView().findViewById(R.id.receiver)).getText().toString();
            String message = ((EditText) getView().findViewById(R.id.message)).getText().toString();

            //Grab the selected location from the map

            Location location = googleMap.getMyLocation();
            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            Log.d("Receiver: ", receiver);
            Log.d("Sending Message:", message);
            Log.d("Tagged at Latitude: ", latitude);
            Log.d("Tagged at Longitude: ", longitude);
            //send the message to server -> GCM
            send(tv.getText().toString(), receiver, latitude, longitude);

            ((EditText) getView().findViewById(R.id.receiver)).setText(null);
            ((EditText) getView().findViewById(R.id.message)).setText(null);

            Cursor c = getActivity().getContentResolver().query(DataProvider.CONTENT_URI_MESSAGES, null, null, null, null);
            while(c.moveToNext()) {
                Log.d("Iterating over: ", "Messages from DB");
                Log.d("From: ", " " + c.getString(c.getColumnIndex(DataProvider.COL_FROM)));
                Log.d("To: ", " " +c.getString(c.getColumnIndex(DataProvider.COL_TO)));
                Log.d("Msg: ", " " +c.getString(c.getColumnIndex(DataProvider.COL_MSG)));
                Log.d("Lat: ", " " +c.getString(c.getColumnIndex(DataProvider.COL_LAT)));
                Log.d("Long: ", " " +c.getString(c.getColumnIndex(DataProvider.COL_LONG)));
                if(Commons.getPreferredEmail()==c.getString(c.getColumnIndex(DataProvider.COL_FROM))) {
                    Log.d("Message from DB:", " " +c.getString(c.getColumnIndex(DataProvider.COL_MSG)));
                }
                c.move(1);
            }


        }
    };

    public DropFragment() {
        //default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor c = getActivity().getContentResolver().query(DataProvider.CONTENT_URI_MESSAGES, null, null, null, null);
        while(c.moveToNext()) {
            Log.d("Iterating over: ", "Messages from DB");
            Log.d("From: ", " " + c.getString(c.getColumnIndex(DataProvider.COL_FROM)));
            Log.d("To: ", " " +c.getString(c.getColumnIndex(DataProvider.COL_TO)));
            Log.d("Msg: ", " " +c.getString(c.getColumnIndex(DataProvider.COL_MSG)));
            Log.d("Lat: ", " " +c.getString(c.getColumnIndex(DataProvider.COL_LAT)));
            Log.d("Long: ", " " +c.getString(c.getColumnIndex(DataProvider.COL_LONG)));
            if(Commons.getPreferredEmail()==c.getString(c.getColumnIndex(DataProvider.COL_FROM))) {
                Log.d("Message from DB:", " " +c.getString(c.getColumnIndex(DataProvider.COL_MSG)));
            }
            c.move(1);
        }

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
                googleMap.setMyLocationEnabled(true);
                googleMap.setTrafficEnabled(true);
                googleMap.setBuildingsEnabled(true);
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
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
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    onLocationChanged(location);
                }
                locationManager.requestLocationUpdates(provider, 120000, 0, this);

            }
        }
    }

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
                if (dropMarker != null)
                    dropMarker.remove();
                dropMarker = googleMap.addMarker(new MarkerOptions().title(place).position(latlng));
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

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    private void send(final String txt, final String profileEmail, final String lat, final String llong) {
        Log.d("Message sent is: " , txt);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    ServerUtilities.send(txt, profileEmail, lat, llong);

                    ContentValues values = new ContentValues(4);
                    values.put(DataProvider.COL_MSG, txt);
                    values.put(DataProvider.COL_TO, profileEmail);
                    values.put(DataProvider.COL_FROM, Commons.getPreferredEmail());
                    values.put(DataProvider.COL_LAT, lat);
                    values.put(DataProvider.COL_LONG, llong);

                    getActivity().getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);

                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
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
