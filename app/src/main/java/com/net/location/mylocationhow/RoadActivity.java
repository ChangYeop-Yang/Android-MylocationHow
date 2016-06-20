package com.net.location.mylocationhow;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.concurrent.ExecutionException;

import basic.Vibrate;
import travel.TourLocalXmlParser;

/**
 * Created by Mari on 2015-09-26.
 */
public class RoadActivity extends BaseActivity
{
    /* Context */
    private Context mContext = RoadActivity.this;

    /* RecyclerView */
    private RecyclerView mRecyclerView = null;

    /* Floating ActionButton */
    private FloatingActionButton mMoveFloating[] = null;

    /* Integer */
    private int mPage[] = {1, 1};

    /* String */
    private final String mTag[] = {"contentid", "firstimage", "title", "mapx", "mapy"};
    private String mTabTag = null;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road);

        /* ToolBar */
        mToolbar = (Toolbar) findViewById(R.id.RoadToolbar); /* ToolBar */
        /* ToolBar Title */
        mToolbar.setTitle("코스"); /* ToolBar Title Text */
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        mToolbar.setNavigationIcon(R.drawable.ic_arrow); /* Icon Setting */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* TabLayout */
        mTabLayout = (TabLayout) findViewById(R.id.RoadTab);
        createTab(mTabLayout);
        setTeb(mTabLayout);

        /* RecyclerView */
        mRecyclerView = (RecyclerView)findViewById(R.id.RoadRecyclerView);
        setRecyclerView(mRecyclerView, (FloatingActionsMenu)findViewById(R.id.RoadMultipleDownMenu));

        /* Floating */
        mMoveFloating = new FloatingActionButton[]{(FloatingActionButton)findViewById(R.id.RoadBackButton), (FloatingActionButton)findViewById(R.id.CultureNextButton)};
        for(int count = 0, mLength = mMoveFloating.length; count < mLength; count++) { setFloating(mMoveFloating[count], count); }
    }

    /* createTab Method */
    private void createTab(TabLayout mTab)
    {
        /* int */
        final int mIconInt[] = {R.drawable.ic_man, R.drawable.ic_family, R.drawable.ic_camping, R.drawable.ic_hiling};

        /* String */
        final String mTag[] = {"C0113", "C0112", "C0116", "C0114"};

        /* Create TextTab Button */
        for(int count = 0, mLength = mIconInt.length; count < mLength; count++)
        { mTab.addTab(mTab.newTab().setIcon(mIconInt[count]).setTag(mTag[count])); }
    }

    /* setTab Method */
    private void setTeb(TabLayout mTab)
    {
        mTab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                mPage[0] = 1; /* Page 초기화 */
                switch (tab.getPosition())
                {
                    case (0) : { mToolbar.setSubtitle("나홀로"); break; }
                    case (1) : { mToolbar.setSubtitle("가족"); break; }
                    case (2) : { mToolbar.setSubtitle("캠핑"); break; }
                    case (3) : { mToolbar.setSubtitle("힐링"); break; }
                }

                final String mURL = createRoad( (mTabTag = tab.getTag().toString()) );
                try { mPage[1] = new TourLocalXmlParser(mContext, mURL, mTourHeadTag, mTag, mRecyclerView, 11).execute().get() / ScrollItemCount; }
                catch (InterruptedException e) { e.printStackTrace(); }
                catch (ExecutionException e) { e.printStackTrace(); }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    /* create Road URL Method */
    private String createRoad(String mCode)
    {
        /* StringBuffer */
        StringBuffer mStringBuffer = new StringBuffer("http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList?ServiceKey="); mStringBuffer.append(getResources().getString(R.string.TOUR_APIKEY)); /* API KEY */
        mStringBuffer.append("&contentTypeId=25&areaCode=&sigunguCode=&cat1=C01&cat2="); mStringBuffer.append(mCode); /* Code */
        mStringBuffer.append("&cat3=&listYN=Y&MobileOS=AND&MobileApp="); mStringBuffer.append(getResources().getString(R.string.app_name)); /* App Name */
        mStringBuffer.append("&arrange=O&numOfRows=15&pageNo="); mStringBuffer.append(mPage[0]);
        return mStringBuffer.toString();
    }

    /* Setting Floating Button Method */
    private void setFloating(FloatingActionButton mFloat, final int mSwitch)
    {
        mFloat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* 진동 */
                Vibrate.Vibrator(mContext);
                switch (mSwitch)
                {
                    case (0) : /* 이전 */
                    {
                        if(--mPage[0] < 1) { Toast.makeText(mContext, "첫 페이지 입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = 1; }
                        else { new TourLocalXmlParser(mContext, createRoad(mTabTag), mTourHeadTag, mTag, mRecyclerView, 11).execute(); } break;
                    }
                    case (1) : /* 다음 */
                    {
                        if(++mPage[0] >= mPage[1]) { Toast.makeText(mContext, "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = mPage[1] - 1; }
                        else { new TourLocalXmlParser(mContext, createRoad(mTabTag), mTourHeadTag, mTag, mRecyclerView, 11).execute(); } break;
                    }
                }
            }
        });
    }
}
