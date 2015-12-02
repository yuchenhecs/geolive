package com.uiuc.tbdex.geolive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Yan Geng on 11/8/2015.
 */
public class CreateroomActivity extends AppCompatActivity{
    Button mCreateButtom;
    EditText mRoomName;
    EditText mRoomID;
    private String mUsername;
    private Socket mSocket;
    private String x, y;


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
        setContentView(R.layout.activity_createroom);

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        //mSocket.on("search result", onSearchResult);
        //mSocket.on("topk result", onTopkResult);
        mSocket.connect();

        Intent intent = getIntent();
        mUsername = intent.getStringExtra("username");
        x = intent.getStringExtra("x");
        y = intent.getStringExtra("y");
        mRoomName = (EditText) findViewById(R.id.name);
        mRoomID = (EditText) findViewById(R.id.roomid);

        mCreateButtom = (Button) findViewById(R.id.create);
       // Toast.makeText(this, mRoomName.getText(), Toast.LENGTH_SHORT).show();
       // Toast.makeText(this, mRoomID.getText(), Toast.LENGTH_SHORT).show();

        mCreateButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCreateRoom();
                Intent intent = new Intent(CreateroomActivity.this , ChatRoomActivity.class);
                intent.putExtra("username", mUsername);
                //String roomtitle = mRoomID.getText().toString();
                intent.putExtra("roomtitle", mRoomID.getText().toString());
               // Toast.makeText(CreateroomActivity.this, mRoomID.getText(), Toast.LENGTH_SHORT).show();

                startActivity(intent);
            }
        });


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

    private void doCreateRoom(){
       // Toast.makeText(this, mRoomName.getText(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, mRoomID.getText(), Toast.LENGTH_SHORT).show();


        //mSocket.emit("room ...", mRoomID.getText());
        JSONObject data = new JSONObject();
       // Toast.makeText(this, "check", Toast.LENGTH_SHORT).show();

        try {
            data.put("username", mUsername);
            data.put("roomname", mRoomName.getText());
            data.put("roomid", mRoomID.getText());

            data.put("longitude", x);
            data.put("latitude", y);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        mSocket.emit("create room", data);
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
