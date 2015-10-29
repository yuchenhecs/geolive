package com.uiuc.tbdex.geolive;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SearchActivity extends AppCompatActivity {

    Button mSearchButton;
    EditText mSearchBox;
    ListView lv;



    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("search result", onSearchResult);
        mSocket.connect();


        mSearchButton = (Button) findViewById(R.id.add);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearchRequested();
            }
        });

        mSearchBox = (EditText) findViewById(R.id.text);

        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_search);
        lv = (ListView) findViewById(android.R.id.list);

    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };
/*
    private void handleIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query){
        Toast.makeText(this, "search on "+ query, Toast.LENGTH_SHORT).show();

    }
*/
    private void doSearchRequested() {
        Toast.makeText(this, mSearchBox.getText(), Toast.LENGTH_SHORT).show();
        mSocket.emit("room search", mSearchBox.getText());

/*
        ArrayList<String> element_t = new ArrayList<String>();
        element_t.add("roooom1");
        element_t.add("roooom2");
        element_t.add("roooom3");
  */
        //lv.setAdapter(mAdapter);
        // setContentView(lv);

    }

    private Emitter.Listener onSearchResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray array;
                    ArrayList<String> element=new ArrayList<String>();
                    try {
                        array = data.getJSONArray("roomname");

                        for (int i=0;i<array.length();i++){
                            element.add(array.getString(i));
                        }
                    } catch (JSONException e) {
                        return;
                    }
                    for (String str: element){
                        //Log.d("onsearch", str);
                        Toast.makeText(getApplicationContext(), str,Toast.LENGTH_SHORT).show();
                    }

                    ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_expandable_list_item_1, element) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {

                            View view = super.getView(position, convertView, parent);
                            TextView text = (TextView) view.findViewById(android.R.id.text1);

                            text.setTextColor(Color.BLACK);

                            return view;
                        }
                    };
                    lv.setAdapter(mAdapter);



                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
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
}
