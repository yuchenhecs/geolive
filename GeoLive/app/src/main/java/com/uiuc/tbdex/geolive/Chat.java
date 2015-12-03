package com.uiuc.tbdex.geolive;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Chat extends AppCompatActivity {

    private String mUsername;
    private String mRoomTitle;
    private Socket mSocket;

    public String getmUsername() {
        return mUsername;
    }

    public String getmRoomTitle() {
        return mRoomTitle;
    }

    public Socket getmSocket() {
        return mSocket;
    }

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
        setContentView(R.layout.activity_chat);

        final Intent intent = getIntent();
        mUsername = intent.getStringExtra("username");
        mRoomTitle = intent.getStringExtra("roomtitle");

        Button buttonList = (Button)findViewById(R.id.button_list);
        buttonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment chatFragment = new ChatFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, chatFragment, "CHAT");

                transaction.commit();
            }
        });

        Button buttonDanmu = (Button)findViewById(R.id.button_danmu);
        buttonDanmu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment danmuFragment = new DanmuFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, danmuFragment, "DANMU");

                transaction.commit();
            }
        });

        setUpSocketIo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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


    @Override
    public void onStart() {
        super.onStart();

        attemptLogin();
    }

    @Override
    public void onStop() {
        super.onStop();

        // mSocket.disconnect();

        String data = null;
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.emit("leave room", data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //mSocket.disconnect();
//        String data=null;
//        mSocket.emit("leave room",data);
    }


    private void setUpSocketIo() {
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);

//        mSocket.on("typing", onTyping);
//        mSocket.on("stop typing", onStopTyping);

        mSocket.connect();

//        if (mSocket.connected()) {
//            Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT);
//        }
    }


    private void attemptLogin() {
        JSONObject data = new JSONObject();
        try {
            data.put("username", mUsername);
            data.put("roomid", mRoomTitle);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("add user", data);
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


    @Override
    public void onBackPressed() {
        String data=null;
        mSocket.emit("leave room", data);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);

        Intent backMain = new Intent(this, MainActivity.class);
        startActivity(backMain);
    }
}
