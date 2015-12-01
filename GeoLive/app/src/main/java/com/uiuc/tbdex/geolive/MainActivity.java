package com.uiuc.tbdex.geolive;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
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
import android.view.View.OnClickListener;

import android.support.v4.widget.DrawerLayout;
//import android.app.Fragment;
//import android.app.FragmentManager;


import android.widget.Button;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

//import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import io.socket.client.IO;
import io.socket.client.Socket;




import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private RecyclerView mRecyclerView;
    private List<ChatRoom> mChatRooms;

    private String mUsername;


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    private Button mButton;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

       // mButton = (Button)findViewById(R.id.pink_icon);
        findViewById(R.id.pink_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CreateroomActivity.class);
                intent.putExtra("username", mUsername);
                startActivity(intent);
            }

        });


/*
        PopularLayout = findViewById(R.id.popular_layout);
        LocationLayout = findViewById(R.id.location_layout);
        //PopularLayout.setOnClickListener(this);
        //LocationLayout.setOnClickListener(this);

        PopularLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                setTabSelection(0);
            }
        });

        LocationLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTabSelection(1);
            }
        });

        fragmentManager = getFragmentManager();


        setTabSelection(0);
*/

    }
/*
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.popular_layout:
                setTabSelection(0);
                break;

            case R.id.location_layout:
                setTabSelection(1);
                break;
            default:
                break;

        }
    }

    private void setTabSelection(int index){
        clearSelection();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch(index){
            case 0:
                PopularLayout.setBackgroundColor(0xff0000ff);
                if(mPopular == null){
                    mPopular = new Popular();
                    transaction.add(R.id.content, mPopular);
                }
                else{
                    transaction.show(mPopular);
                }
                break;

            case 1:
                PopularLayout.setBackgroundColor(0xff0000ff);
                if(mLocation == null){
                    mLocation = new Location();
                    transaction.add(R.id.content, mLocation);
                }
                else{
                    transaction.show(mLocation);
                }
                break;
        }
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (mPopular != null) {
            transaction.hide(mPopular);
        }
        if (mLocation != null) {
            transaction.hide(mLocation);
        }
    }
    private void clearSelection(){
        PopularLayout.setBackgroundColor(0xffffffff);
        LocationLayout.setBackgroundColor(0xffffffff);
    }

*/
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
                mTitle = getString(R.string.title_section1);
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
        mChatRooms.add(new ChatRoom("room1"));
        mChatRooms.add(new ChatRoom("room2"));
        mChatRooms.add(new ChatRoom("New Room"));
    }


    private void initializeAdapter() {
        LocRecoRecyclerViewAdapter locRecoRecyclerViewAdapter = new LocRecoRecyclerViewAdapter(getApplicationContext(), mUsername, mChatRooms);
        mRecyclerView.setAdapter(locRecoRecyclerViewAdapter);
    }

//    private void enterChatRoom(String chatRoomTitle) {
//        Intent intent = new Intent(this, ChatRoomActivity.class);
//        intent.putExtra("username", mUsername);
//        intent.putExtra("roomtitle", chatRoomTitle);
//        startActivity(intent);
//    }
}