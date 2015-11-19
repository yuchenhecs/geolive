package com.uiuc.tbdex.geolive;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;


public class DanmuActivity extends AppCompatActivity {
    private DanmuHandler mDanmuHandler;

    private RelativeLayout mDanmuContainer;

    private ArrayList<Danmu> mTestDanmus;

    private int mVerticalSpace;
//    protected Rect mRect = new Rect();
    private Random mRandom = new Random();

    protected int mTextSize = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danmu);

        mDanmuContainer = (RelativeLayout) findViewById(R.id.danmu_container);

        mVerticalSpace = mDanmuContainer.getBottom() - mDanmuContainer.getTop() -
                mDanmuContainer.getPaddingTop() - mDanmuContainer.getPaddingBottom();


        mTestDanmus = new ArrayList<>();
        mTestDanmus.add(new Danmu("前方福利"));
        mTestDanmus.add(new Danmu("见福打"));
        mTestDanmus.add(new Danmu("23333333333333"));
        mTestDanmus.add(new Danmu("Let's go, 王宝强"));

        mDanmuHandler = new DanmuHandler(this);

        new Thread(new CreateDanmu()).start();
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


    private class CreateDanmu implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < mTestDanmus.size(); i++) {
                mDanmuHandler.obtainMessage(1, i, 0).sendToTarget();
                SystemClock.sleep(2000);
            }
        }
    }


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
                if (danmuActivity != null && danmuActivity.mTestDanmus != null) {
                    String content = danmuActivity.mTestDanmus.get(msg.arg1).getContent();

                    danmuActivity.showDanmu(content);
                }
            }
        }
    }


    private void showDanmu(String content) {
        final TextView textView = new TextView(getApplicationContext());

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.topMargin = getRandomTopMargin();

        int leftMargin = mDanmuContainer.getRight() - mDanmuContainer.getLeft() -mDanmuContainer.getPaddingLeft();

        textView.setText(content);
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(mTextSize);
        textView.setTextColor(0xff000000);

        Animation animation = new TranslateAnimation(leftMargin, -ScreenUtils.getScreenW(getApplicationContext()), 0, 0);
        animation.setDuration(2000);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
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
        int margin = 0;

        return mRandom.nextInt(ScreenUtils.getScreenH(getApplicationContext()) - mTextSize);
    }
}
