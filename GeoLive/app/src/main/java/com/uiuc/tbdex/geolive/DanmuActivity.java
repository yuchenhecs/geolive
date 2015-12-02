package com.uiuc.tbdex.geolive;

import android.graphics.drawable.LayerDrawable;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.simple.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class DanmuActivity extends AppCompatActivity {

    private EditText mInputMessageView;



    // Danmu
    private DanmuHandler mDanmuHandler;
    private RelativeLayout mDanmuContainer;

    private ArrayList<Danmu> mDanmus;
//    private ArrayList<Danmu> mTestDanmus;

    private int mVerticalSpace = 0;
    private Set<Integer> mOccupiedLine = new HashSet<>();

    private Random mRandom = new Random();

    protected int mTextSize = 20;
    protected int mTextSizeInPx;

    // SocketIO
    private String mUsername = "test";
    private String mRoomTitle = "test";
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
        setContentView(R.layout.activity_danmu);

        mInputMessageView = (EditText) findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.send || actionId == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });

        // send button
        ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "sending", Toast.LENGTH_SHORT).show();
                attemptSend();
            }
        });

        mDanmuContainer = (RelativeLayout) findViewById(R.id.danmu_container);
//        ViewTreeObserver vto = mDanmuContainer.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                LayerDrawable ld = (LayerDrawable)mDanmuContainer.getBackground();
//                ld.setLayerInset(1, 0, mDanmuContainer.getHeight() / 2, 0, 0);
//                ViewTreeObserver obs = mDanmuContainer.getViewTreeObserver();
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    obs.removeOnGlobalLayoutListener(this);
//                } else {
//                    obs.removeGlobalOnLayoutListener(this);
//                }
//            }
//        });

        mTextSizeInPx = ScreenUtils.dp2px(getApplicationContext(), mTextSize);
//        mVerticalSpace = mDanmuContainer.getMeasuredHeight() - mTextSizeInPx - 30;

//        Log.d("Danmu", Integer.toString(mVerticalSpace));
//        Log.d("Danmu", Integer.toString(mTextSizeInPx));

//        mTestDanmus = new ArrayList<>();
//        mTestDanmus.add(new Danmu("前方福利"));
//        mTestDanmus.add(new Danmu("见福打"));
//        mTestDanmus.add(new Danmu("23333333333333"));
//        mTestDanmus.add(new Danmu("Let's go, 王宝强"));

        mDanmuHandler = new DanmuHandler(this);

//        new Thread(new CreateDanmu()).start();

        // socket.io
        setUpSocketIo();

        attemptLogin();
    }

    @Override
    public void onStop() {
        super.onStop();

        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

//        mSocket.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_danmu, menu);
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


//    private class CreateDanmu implements Runnable {
//
//        @Override
//        public void run() {
//            for (int i = 0; i < mTestDanmus.size(); i++) {
//                mDanmuHandler.obtainMessage(1, i, 0).sendToTarget();
//                SystemClock.sleep(500);
//            }
//        }
//    }


    private static class DanmuHandler extends Handler {
        private WeakReference<DanmuActivity> ref;

        DanmuHandler(DanmuActivity danmuActivity) {
            ref = new WeakReference<DanmuActivity>(danmuActivity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                DanmuActivity danmuActivity = ref.get();
//                if (danmuActivity != null && danmuActivity.mTestDanmus != null) {
//                    String content = danmuActivity.mTestDanmus.get(msg.arg1).getContent();
//
//                    danmuActivity.showDanmu(content);
//                }
                if (danmuActivity != null) {
                    String content = danmuActivity.mDanmus.get(msg.arg1).getContent();
                    danmuActivity.showDanmu(content);
                }
            }
        }
    }


    private void showDanmu(String content) {
        final TextView textView = new TextView(getApplicationContext());

        final LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.topMargin = getRandomTopMargin();

        Log.d("Danmu", Integer.toString(layoutParams.topMargin));

        int leftMargin = mDanmuContainer.getRight() - mDanmuContainer.getLeft() -mDanmuContainer.getPaddingLeft();

        textView.setText(content);
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(mTextSize);
        textView.setTextColor(0xff000000);
        textView.setSingleLine(true);

        Animation animation = new TranslateAnimation(leftMargin, -ScreenUtils.getScreenW(getApplicationContext()), 0, 0);
        animation.setDuration(5000);
        animation.setFillAfter(true);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mOccupiedLine.remove(layoutParams.topMargin);
                mDanmuContainer.removeView(textView);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        textView.startAnimation(animation);
        mDanmuContainer.addView(textView);
    }


    private int getRandomTopMargin() {
        if (mVerticalSpace == 0) {
            mVerticalSpace = mDanmuContainer.getHeight() - mTextSizeInPx - 30;
        }

//        Log.d("Danmu", Integer.toString(mVerticalSpace));
//        Log.d("Danmu", Integer.toString(mTextSizeInPx));

        int totalLines = mVerticalSpace/mTextSizeInPx;

        int proposedLine = mRandom.nextInt(totalLines);
        while (mOccupiedLine.contains(proposedLine)) {
            proposedLine = mRandom.nextInt(totalLines);
        }

        mOccupiedLine.add(proposedLine);

        return proposedLine * mTextSizeInPx;

//        Log.d("Danmu", Integer.toString(ScreenUtils.getScreenH(getApplicationContext())));
//        Log.d("Danmu", Integer.toString(ScreenUtils.px2dp(getApplicationContext(), ScreenUtils.getScreenH(getApplicationContext()))));
//        return mTextSize + mRandom.nextInt(ScreenUtils.getScreenH(getApplicationContext()) - mTextSize);

//        return mRandom.nextInt(mVerticalSpace);
    }

    private void attemptLogin() {
        JSONObject data = new JSONObject();
        data.put("username", mUsername);
        data.put("roomid", mRoomTitle);

        mSocket.emit("add user", data);
    }

    private void addMessage(String username, String message) {
//        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
//                .username(username).message(message).build());
//        mAdapter.notifyItemInserted(mMessages.size() - 1);
//        scrollToBottom();
        mDanmus.add(new Danmu(message));
        mDanmuHandler.obtainMessage(1, mDanmus.size() - 1, 0).sendToTarget();
    }

    private void addLog(String message) {
//        mMessages.add(new Message.Builder(Message.TYPE_LOG)
//                .message(message).build());
//        mAdapter.notifyItemInserted(mMessages.size() - 1);
//        scrollToBottom();
        mDanmus.add(new Danmu(message));
        mDanmuHandler.obtainMessage(1, mDanmus.size() - 1, 0).sendToTarget();
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }


    private void attemptSend() {
        if (mUsername == null) return;
        if (!mSocket.connected()) return;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message);

        mSocket.emit("new message", message);
    }


    private void setUpSocketIo() {
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
//        mSocket.on("typing", onTyping);
//        mSocket.on("stop typing", onStopTyping);

        mSocket.connect();

//        if (mSocket.connected()) {
//            Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT);
//        }
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "receiving", Toast.LENGTH_SHORT);

                    org.json.JSONObject data = (org.json.JSONObject) args[0];
//                    JSONObject data = (JSONObject) args[0];
//                    String username;
//                    String message;
//                    if (data.get("username") == null || data.get("message") == null) return;
//                    username = data.get("username").toString();
//                    message = data.get("message").toString();
//
//                    Toast.makeText(getApplicationContext(), username, Toast.LENGTH_SHORT);
//                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);

                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    addMessage(username, message);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Join", Toast.LENGTH_SHORT).show();
                    org.json.JSONObject data = (org.json.JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, username));
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    org.json.JSONObject data = (org.json.JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_left, username));
                    addParticipantsLog(numUsers);
//                    removeTyping(username);
                }
            });
        }
    };

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
}
