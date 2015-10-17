package com.uiuc.tbdex.geolive;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {

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
                    Toast toast = Toast.makeText(v.getContext(), currentRoom.getTitle(), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }


    List<ChatRoom> mChatRooms;


    LocRecoRecyclerViewAdapter(List<ChatRoom> chatRooms) {
        this.mChatRooms = chatRooms;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_loc_reco_card_view, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.roomTitle.setText(mChatRooms.get(i).getTitle());
        viewHolder.currentRoom = mChatRooms.get(i);
    }


    @Override
    public int getItemCount() {
        return mChatRooms.size();
    }
}
