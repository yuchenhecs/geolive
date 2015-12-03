package com.uiuc.tbdex.geolive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Button;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import gms.drive.*;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,ConnectionCallbacks, OnConnectionFailedListener{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private RecyclerView mRecyclerView;
    private List<ChatRoom> mChatRooms;

    private String mUsername;

    private LocRecoRecyclerViewAdapter locRecoRecyclerViewAdapter;

    private GoogleApiClient mGoogleApiClient;

    private boolean mainPageReady=false;

    private boolean ApiReady=false;

    ArrayList<String> popularRoom = new ArrayList<String>();
    ArrayList<String> popularRoomName = new ArrayList<String>();
    ArrayList<String> nearbyRoom = new ArrayList<String>();
    ArrayList<String> nearbyRoomName = new ArrayList<String>();

    String x,y;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    private Button mButton; // Danmu Test Only

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();



        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent intent = getIntent();
        mUsername = intent.getStringExtra("username");

        mRecyclerView = (RecyclerView) findViewById(R.id.loc_reco_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        initializeData();

        initializeAdapter();

        mSocket.connect();


        findViewById(R.id.pink_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CreateroomActivity.class);
                intent.putExtra("username", mUsername);
                intent.putExtra("x", x);
                intent.putExtra("y", y);
                startActivity(intent);
            }

        });


        Button tmpbotton=(Button)findViewById(R.id.button2);
        tmpbotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
//                Toast.makeText(getApplicationContext(), String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude()), Toast.LENGTH_SHORT).show();
            }
        });


        //mChatRooms.clear();
        //locRecoRecyclerViewAdapter.notifyDataSetChanged();
        mSocket.on("popular results", onPopularResults);
        mSocket.on("nearby results", onNearbyResults);

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "GeoLive";
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    private void initializeData() {
        mChatRooms = new ArrayList<>();
        //mChatRooms.add(new ChatRoom("room1","room1"));
        //mChatRooms.add(new ChatRoom("room2","room2"));
        //mChatRooms.add(new ChatRoom("New Room"));
    }


    private void initializeAdapter() {
        locRecoRecyclerViewAdapter = new LocRecoRecyclerViewAdapter(getApplicationContext(), mUsername, mChatRooms);
        mRecyclerView.setAdapter(locRecoRecyclerViewAdapter);
    }

//    private void enterChatRoom(String chatRoomTitle) {
//        Intent intent = new Intent(this, ChatRoomActivity.class);
//        intent.putExtra("username", mUsername);
//        intent.putExtra("roomtitle", chatRoomTitle);
//        startActivity(intent);
//    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the 'Handle Connection Failures' section.

    }

    @Override
    protected void onStart() {
        super.onStart();
        //if (!mResolvingError) {  // more about this later
        mGoogleApiClient.connect();


        //}
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();


        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        Location mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        x=String.valueOf(mLastLocation.getLongitude());
        y=String.valueOf(mLastLocation.getLatitude());

        //ApiReady=true;
        fetchData();
    }

    public void fetchData(){


        JSONObject data = new JSONObject();
        try {
            data.put("longitude", x);
            data.put("latitude", y);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("main page", data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.off("popular results", onPopularResults);
        mSocket.off("nearby results", onNearbyResults);
        mSocket.disconnect();
    }



    private void updateMainPage(){
        mChatRooms.clear();
        //locRecoRecyclerViewAdapter.notifyDataSetChanged();

        mainPageReady=false;

        //initializeAdapter();

        mChatRooms.add(new ChatRoom("◎ Nearby Rooms",null, Color.WHITE,Color.MAGENTA));
        for (int i = 0; i < nearbyRoom.size(); i++) {
            mChatRooms.add(new ChatRoom(nearbyRoomName.get(i),nearbyRoom.get(i)));
        }

        mChatRooms.add(new ChatRoom("◎ Popular Rooms",null, Color.WHITE,Color.MAGENTA));

        for (int i = 0; i < popularRoom.size(); i++) {
            mChatRooms.add(new ChatRoom(popularRoomName.get(i),popularRoom.get(i)));
        }
        //Toast.makeText(getApplicationContext(), "1233333333333333333", Toast.LENGTH_SHORT).show();

        locRecoRecyclerViewAdapter.notifyDataSetChanged();
        popularRoom.clear();
        popularRoomName.clear();
        nearbyRoom.clear();
        nearbyRoomName.clear();

    }

    private Emitter.Listener onPopularResults = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray array;
                    // ArrayList<String> element2 = new ArrayList<String>();
                    try {
                        //mChatRooms.add(new ChatRoom("123", "123"));
                        array = data.getJSONArray("popular_room");
                        for (int i = 0; i < array.length(); i++) {
                            // mChatRooms.add(new ChatRoom(array.getString(i)));
                            popularRoom.add(array.getString(i));
                        }

//                        Toast.makeText(getApplicationContext(), "asdasdasdas", Toast.LENGTH_SHORT).show();

                        array = data.getJSONArray("popular_room_name");
                        for (int i = 0; i < array.length(); i++) {
                            //mChatRooms.add(new ChatRoom(array.getString(i),element.get(i)));
                            popularRoomName.add(array.getString(i));
                        }

                        if(mainPageReady){
                            updateMainPage();
                        }else{
                            mainPageReady=true;
                        }
                        //Toast.makeText(getApplicationContext(), "zzzzzzzzzzzz", Toast.LENGTH_SHORT).show();
                        //initializeAdapter();
                        //locRecoRecyclerViewAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
//                        Toast.makeText(getApplicationContext(), e.toString() , Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            });
        }
    };

    private Emitter.Listener onNearbyResults = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];
                    JSONArray array;
                    //ArrayList<String> element = new ArrayList<String>();
                    // ArrayList<String> element2 = new ArrayList<String>();
                    try {
                        //mChatRooms.add(new ChatRoom("123", "123"));

                        array = data.getJSONArray("nearby_room");
                        for (int i = 0; i < array.length(); i++) {
                            // mChatRooms.add(new ChatRoom(array.getString(i)));
                            nearbyRoom.add(array.getString(i));
                        }

//                        Toast.makeText(getApplicationContext(), "zzzzzzzzzzzzz", Toast.LENGTH_SHORT).show();

                        array = data.getJSONArray("nearby_room_name");
                        for (int i = 0; i < array.length(); i++) {
                            //mChatRooms.add(new ChatRoom(array.getString(i),element.get(i)));
                            nearbyRoomName.add(array.getString(i));
                        }

                        if(mainPageReady){
                            updateMainPage();
                        }else{
                            mainPageReady=true;
                        }
                        //Toast.makeText(getApplicationContext(), "zzzzzzzzzzzz", Toast.LENGTH_SHORT).show();
                        //initializeAdapter();
                        //locRecoRecyclerViewAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
//                        Toast.makeText(getApplicationContext(), e.toString() , Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            });
        }
    };

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}





