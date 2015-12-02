package com.uiuc.tbdex.geolive;

import android.graphics.Color;

/**
 * Created by qiuding on 10/17/2015.
 */
public class ChatRoom {
    private String title;
    private String id;
    private int numOfPeople;
    private int back_color;
    private int color;
    private boolean click=true;

    public ChatRoom(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public ChatRoom(String title, String id,int color,int back_color) {
        this.title = title;
        this.id = id;
        this.color= color;
        this.back_color=back_color;
        this.click=false;
    }
    public int getBackColor(){return back_color;}
    public int getColor(){return  color;}
    public boolean getClick(){return click;}
    public String getTitle() {
        return title;
    }
    public String getId() {
        return id;
    }
}
