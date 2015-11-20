package com.uiuc.tbdex.geolive;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by qiuding on 10/17/2015.
 */
public class LocRecoRecyclerViewAdapter extends RecyclerView.Adapter<LocRecoRecyclerViewAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView roomTitle;
        ChatRoom currentRoom;


        ViewHolder(final View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            roomTitle = (TextView) itemView.findViewById(R.id.room_title);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), currentRoom.getTitle(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(mContext, ChatRoomActivity.class);

                    intent.putExtra("username", mUsername);
                    intent.putExtra("roomtitle", currentRoom.getTitle());
                    // start new activity outside of activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
        }
    }


    List<ChatRoom> mChatRooms;
    Context mContext;
    String mUsername;

    LocRecoRecyclerViewAdapter(Context context, String username, List<ChatRoom> chatRooms) {
        mContext = context;
        mChatRooms = chatRooms;
        mUsername = username;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_loc_reco_card_view, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.roomTitle.setText(mChatRooms.get(position).getTitle());
        viewHolder.currentRoom = mChatRooms.get(position);
    }


    @Override
    public int getItemCount() {
        return mChatRooms.size();
    }
}
