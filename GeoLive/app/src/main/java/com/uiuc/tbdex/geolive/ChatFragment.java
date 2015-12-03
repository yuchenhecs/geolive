package com.uiuc.tbdex.geolive;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String mUsername;
    private String mRoomTitle;
    private Socket mSocket;


    public ChatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();

        mUsername = ((Chat)getActivity()).getmUsername();
        mRoomTitle = ((Chat)getActivity()).getmRoomTitle();
        mSocket = ((Chat)getActivity()).getmSocket();

        // set up recycler view
        mMessagesView = (RecyclerView) getView().findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mAdapter = new MessageAdapter(getActivity().getApplicationContext(), mMessages);
        mMessagesView.setAdapter(mAdapter);

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

        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("show people",onShowPeople);
    }


    @Override
    public void onStop() {
        super.onStop();

        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("show people", onShowPeople);
    }


    private void addMessage(String username, String message) {
        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addLog(String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }


    private void attemptSend() {

//        Toast.makeText(getActivity().getApplicationContext(), mUsername, Toast.LENGTH_SHORT).show();

        if (mUsername == null) return;
        //if (!mSocket.connected()) return;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message);

        mSocket.emit("new message", message);
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
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
                    JSONObject data = (JSONObject) args[0];
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
