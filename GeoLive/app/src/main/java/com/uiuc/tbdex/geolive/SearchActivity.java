package com.uiuc.tbdex.geolive;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
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

    private ArrayList<String> mTopKResult = new ArrayList<>();

    private Socket mSocket;
    private SearchManager mSearchManager;
    private SearchView mSearchView;

    private ListView mListView;
    private AutoCompleteTextView mAutoCompleteTextView;
    private ArrayAdapter<String> mAutoCompleteAdapter;
    private Button mSearchButton;

    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mListView = (ListView) findViewById(android.R.id.list);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.my_search_input);
        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mSocket.emit("request topk", s.toString());
            }
        });

        mSearchButton = (Button) findViewById(R.id.my_search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("room search", mAutoCompleteTextView.getText());
            }
        });

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("search result", onSearchResult);
        mSocket.on("topk result", onTopkResult);
        mSocket.connect();

        handleIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_search, menu);

//        mSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
//        mSearchView.setSearchableInfo(mSearchManager.getSearchableInfo(getComponentName()));
//        mSearchView.setIconifiedByDefault(false);
//
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                mSocket.emit("request topk", newText);
//
//                return true;
//            }
//        });

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


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            mSocket.emit("room search", query);
        }
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


//    private Emitter.Listener onTopkResult = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Looper.prepare();
//
//                    mTopKResult.clear();
////                    Log.d("SearchActivity", "changing suggestion");
//
//                    JSONObject data = (JSONObject) args[0];
//                    JSONArray array;
//
//                    try {
//                        array = data.getJSONArray("keywords");
//
//                        for (int i = 0; i < array.length(); i++) {
//                            mTopKResult.add(array.getString(i));
//                        }
//                    } catch (JSONException e) {
//                        return;
//                    }
//
//                    String[] columns = new String[]{"_id", "text"};
//                    Object[] temp = new Object[]{0, "default"};
//
//                    MatrixCursor cursor = new MatrixCursor(columns);
//
//                    for (int i = 0; i < mTopKResult.size(); i++) {
//                        temp[0] = i;
//                        temp[1] = mTopKResult.get(i);
//
//                        cursor.addRow(temp);
//
//                        Log.d("SearchView", temp[1].toString());
//                    }
//
//                    SearchSuggestionAdapter searchSuggestionAdapter = new SearchSuggestionAdapter(getApplicationContext(), cursor, mTopKResult);
//                    mSearchView.setSuggestionsAdapter(new SearchSuggestionAdapter(getApplicationContext(), cursor, mTopKResult));
//                }
//            }).start();
//        }
//    };

    private void updateSearchSuggestion(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTopKResult.clear();
//                    Log.d("SearchActivity", "changing suggestion");

                JSONObject data = (JSONObject) args[0];
                JSONArray array;

                try {
                    array = data.getJSONArray("keywords");

                    for (int i = 0; i < array.length(); i++) {
                        String temp = array.getString(i);
                        // TODO: don't hard code this
                        String[] tempArray = temp.split("\"");
                        mTopKResult.add(tempArray[3]);
                    }
                } catch (JSONException e) {
                    return;
                }

                mAutoCompleteAdapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.item_search_suggestion_auto, mTopKResult);
                mAutoCompleteTextView.setAdapter(mAutoCompleteAdapter);
                mAutoCompleteTextView.showDropDown();


//                String[] columns = new String[]{"_id", "text"};
//                Object[] temp = new Object[]{0, "default"};
//
//                MatrixCursor cursor = new MatrixCursor(columns);
//
//                for (int i = 0; i < mTopKResult.size(); i++) {
//                    temp[0] = i;
//                    temp[1] = mTopKResult.get(i);
//
//                    cursor.addRow(temp);
//
//                    Log.d("SearchView", temp[1].toString());
//                }
//
//                mSearchView.setSuggestionsAdapter(new SearchSuggestionAdapter(SearchActivity.this, cursor, mTopKResult));
            }
        });
    }

    private Emitter.Listener onSearchResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray array;
                    ArrayList<String> element = new ArrayList<String>();
                    try {
                        array = data.getJSONArray("roomname");

                        for (int i = 0; i < array.length(); i++) {
                            element.add(array.getString(i));
                        }
                    } catch (JSONException e) {
                        return;
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

                    mListView.setAdapter(mAdapter);
                }
            });
        }
    };


    private Emitter.Listener onTopkResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mTopKResult.clear();
////                    Log.d("SearchActivity", "changing suggestion");
//
//                    JSONObject data = (JSONObject) args[0];
//                    JSONArray array;
//
//                    try {
//                        array = data.getJSONArray("keywords");
//
//                        for (int i = 0; i < array.length(); i++) {
//                            mTopKResult.add(array.getString(i));
//                        }
//                    } catch (JSONException e) {
//                        return;
//                    }
//
//                    String[] columns = new String[]{"_id", "text"};
//                    Object[] temp = new Object[]{0, "default"};
//
//                    MatrixCursor cursor = new MatrixCursor(columns);
//
//                    for (int i = 0; i < mTopKResult.size(); i++) {
//                        temp[0] = i;
//                        temp[1] = mTopKResult.get(i);
//
//                        cursor.addRow(temp);
//
//                        Log.d("SearchView", temp[1].toString());
//                    }
//
//                    mSearchView.setSuggestionsAdapter(new SearchSuggestionAdapter(SearchActivity.this, cursor, mTopKResult));
//                    searchSuggestionAdapter.notifyDataSetChanged();
                    updateSearchSuggestion(args);
                }
//            });
//        }
    };


    public class SearchSuggestionAdapter extends CursorAdapter {
        private LayoutInflater cursorInflater;
        private List<String> items;
//        private TextView textView;

        public SearchSuggestionAdapter(Context context, Cursor cursor, List<String> items) {
            super(context, cursor, true);
            this.items = items;
            cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            Log.d("SearchView", "newView");
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return cursorInflater.inflate(R.layout.item_search_suggestion, parent, false);
//            textView = (TextView) view.findViewById(R.id.search_suggestion);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (items.size() > 0) {
                TextView textView = (TextView) view.findViewById(R.id.search_suggestion);
                textView.setText(items.get(cursor.getPosition()));
                Log.d("SearchView", "bindView");
            }
        }
    }

}
