package com.uiuc.tbdex.geolive;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class DanmuFragment extends Fragment {
    private String mUsername;
    private String mRoomTitle;
    private Socket mSocket;

    private EditText mInputMessageView;

    // Danmu
    private DanmuHandler mDanmuHandler;
    private RelativeLayout mDanmuContainer;

    private ArrayList<Danmu> mDanmus = new ArrayList<>();

    public ArrayList<Danmu> getmDanmus() {
        return mDanmus;
    }
    //    private ArrayList<Danmu> mTestDanmus;

    private int mVerticalSpace = 0;
    private Set<Integer> mOccupiedLine = new HashSet<>();

    private Random mRandom = new Random();

    protected int mTextSize = 20;
    protected int mTextSizeInPx;


    public DanmuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_danmu, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();

        mUsername = ((Chat) getActivity()).getmUsername();
        mRoomTitle = ((Chat) getActivity()).getmRoomTitle();
        mSocket = ((Chat) getActivity()).getmSocket();

        // edit text
        mInputMessageView = (EditText) getView().findViewById(R.id.message_input);
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
        ImageButton sendButton = (ImageButton) getView().findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getActivity().getApplicationContext(), "sending", Toast.LENGTH_SHORT).show();
                attemptSend();
            }
        });

        Button showButton = (Button) getView().findViewById(R.id.showButton);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = null;
                mSocket.emit("show people", data);
            }
        });

        mDanmuContainer = (RelativeLayout) getView().findViewById(R.id.danmu_container);

        mTextSizeInPx = ScreenUtils.dp2px(getActivity().getApplicationContext(), mTextSize);

        mDanmuHandler = new DanmuHandler((Chat)getActivity());

        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("show people", onShowPeople);
    }


    @Override
    public void onStop() {
        super.onStop();

        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("show people", onShowPeople);
    }


    private static class DanmuHandler extends Handler {
        private WeakReference<Chat> ref;

        DanmuHandler(Chat chat) {
            ref = new WeakReference<Chat>(chat);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                Chat chat = ref.get();
//                if (danmuActivity != null && danmuActivity.mTestDanmus != null) {
//                    String content = danmuActivity.mTestDanmus.get(msg.arg1).getContent();
//
//                    danmuActivity.showDanmu(content);
//                }
                if (chat != null) {
                    String content = ((DanmuFragment)chat.getFragmentManager().findFragmentByTag("DANMU"))
                            .getmDanmus().get(msg.arg1).getContent();
                    ((DanmuFragment)chat.getFragmentManager().findFragmentByTag("DANMU")).showDanmu(content);
                }
            }
        }
    }


    private void showDanmu(String content) {
        final TextView textView = new TextView(getActivity().getApplicationContext());

        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.topMargin = getRandomTopMargin();

        Log.d("Danmu", Integer.toString(layoutParams.topMargin));

        int leftMargin = mDanmuContainer.getRight() - mDanmuContainer.getLeft() -mDanmuContainer.getPaddingLeft();

        textView.setText(content);
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(mTextSize);
        textView.setTextColor(0xff000000);
        textView.setSingleLine(true);

        Animation animation = new TranslateAnimation(leftMargin, -ScreenUtils.getScreenW(getActivity().getApplicationContext()), 0, 0);
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
        int azusa = 0; // avoid stuck

        Log.d("Danmu", Integer.toString(mDanmuContainer.getHeight()));
//        if (mVerticalSpace == 0) {
//        Rect rect = new Rect();
//        mDanmuContainer.getWindowVisibleDisplayFrame(rect);
//        mVerticalSpace = rect.height() - mTextSizeInPx - 30;
        mVerticalSpace = mDanmuContainer.getHeight() - mTextSizeInPx - 30;
//        }

//        Log.d("Danmu", Integer.toString(rect.height()));

//        Log.d("Danmu", Integer.toString(mVerticalSpace));
//        Log.d("Danmu", Integer.toString(mTextSizeInPx));

        int totalLines = mVerticalSpace/mTextSizeInPx;

        int proposedLine = mRandom.nextInt(totalLines);
        while (mOccupiedLine.contains(proposedLine) && azusa < 10) {
            Log.d("Danmu", "Not enough space");
            proposedLine = mRandom.nextInt(totalLines);
            azusa++;
        }

        mOccupiedLine.add(proposedLine);

        return proposedLine * mTextSizeInPx;

//        Log.d("Danmu", Integer.toString(ScreenUtils.getScreenH(getApplicationContext())));
//        Log.d("Danmu", Integer.toString(ScreenUtils.px2dp(getApplicationContext(), ScreenUtils.getScreenH(getApplicationContext()))));
//        return mTextSize + mRandom.nextInt(ScreenUtils.getScreenH(getApplicationContext()) - mTextSize);

//        return mRandom.nextInt(mVerticalSpace);
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
//        Log.d("Danmu", mUsername);
        if (mUsername == null) return;
//        if (!mSocket.connected()) return;

        String message = mInputMessageView.getText().toString().trim();
//        Log.d("Danmu", message);
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message);

        mSocket.emit("new message", message);
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(getActivity().getApplicationContext(), "receiving", Toast.LENGTH_SHORT);

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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(getActivity().getApplicationContext(), "Join", Toast.LENGTH_SHORT).show();
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
            getActivity().runOnUiThread(new Runnable() {
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


    private Emitter.Listener onShowPeople = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    org.json.JSONObject data = (org.json.JSONObject) args[0];
                    JSONArray array;
                    ArrayList<String> element = new ArrayList<String>();
                    try {
                        array = data.getJSONArray("people");

                        for (int i = 0; i < array.length(); i++) {
                            element.add(array.getString(i));
                        }
                    } catch (JSONException e) {
                        return;
                    }


                    final CharSequence[] charSequenceItems = element.toArray(new CharSequence[element.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("People in this room");
                    builder.setItems(charSequenceItems, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            // Do something with the selection
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    };

}
