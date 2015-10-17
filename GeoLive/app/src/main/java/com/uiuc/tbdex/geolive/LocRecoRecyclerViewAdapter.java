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
public class LocRecoRecyclerViewAdapter extends RecyclerView.Adapter<LocRecoRecyclerViewAdapter.RecommendationViewHolder> {

    public static class RecommendationViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView roomTitle;
        ChatRoom currentRoom;


        RecommendationViewHolder(final View itemView) {
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


    List<ChatRoom> chatRooms;


    LocRecoRecyclerViewAdapter(List<ChatRoom> chatRooms) {
        this.chatRooms = chatRooms;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public RecommendationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.loc_reco_card_view, viewGroup, false);
        return new RecommendationViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecommendationViewHolder recommendationViewHolder, int i) {
        recommendationViewHolder.roomTitle.setText(chatRooms.get(i).getTitle());
        recommendationViewHolder.currentRoom = chatRooms.get(i);
    }


    @Override
    public int getItemCount() {
        return chatRooms.size();
    }
}
