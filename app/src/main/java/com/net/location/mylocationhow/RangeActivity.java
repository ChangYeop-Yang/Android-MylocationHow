package com.net.location.mylocationhow;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.concurrent.ExecutionException;

import basic.Vibrate;
import daum.DaumCommon;
import daum.DaumLocalXmlParser;

/**
 * Created by Mari on 2015-09-03.
 */
public class RangeActivity extends BaseActivity
{
    /* Context */
    private Context mContext = RangeActivity.this;

    /* RecyclerView */
    private RecyclerView mRecyclerView = null;

    /* int */
    private final int mTabIcon[] = {(R.drawable.ic_restaurant_white), (R.drawable.ic_hotel_white), (R.drawable.ic_convenience_store_white), (R.drawable.ic_oil_white), (R.drawable.ic_parking_white)};
    private int mPage[] = {1, 1};
    private int mCodeIntager = 0;

    /* String */
    private final String mTabText[] = {"음식점", "숙박", "편의점", "주유소", "주차장"};
    private final String mTag[] = {"title", "latitude", "longitude", "phone", "address", "distance", "placeUrl", "totalCount"};
    private final String mCode[] = {"FD6", "AD5", "CS2", "OL7", "PK6"};

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_range);

        /* ToolBar */
        mToolbar = (Toolbar) findViewById(R.id.RangeToolbar); /* ToolBar */
        /* ToolBar Title */
        mToolbar.setTitle("주변"); /* ToolBar Title Text */
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        mToolbar.setNavigationIcon(R.drawable.ic_arrow); /* Icon Setting */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* TabLayout */
        mTabLayout = (TabLayout) findViewById(R.id.RangeTab);
        for(int count = 0, mLength = mTabIcon.length; count < mLength; count++)
        {
            /* TextView */
            TextView mCustomTabTextView = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            mCustomTabTextView.setText(mTabText[count]); /* Tab Text Setting */
            mCustomTabTextView.setCompoundDrawablesWithIntrinsicBounds(0, mTabIcon[count], 0, 0); /* Tab Icon Setting */
            mTabLayout.addTab(mTabLayout.newTab().setCustomView(mCustomTabTextView).setText(mTabText[count]));
        }

        /* TabLayout Setting */
        settingTab(mTabLayout);

        /* RecyclerView */
        mRecyclerView = (RecyclerView) findViewById(R.id.RangeRecycler);
        setRecyclerView(mRecyclerView, (FloatingActionsMenu) findViewById(R.id.RangeMultipleDownMenu));

        /* Floating Button */
        setFloatingButton((FloatingActionButton)findViewById(R.id.RangeNextButton), 0); /* 다음 버튼 */
        setFloatingButton((FloatingActionButton)findViewById(R.id.RangeBackButton), 1); /* 이전 버튼 */
    }

    /* SetFloatingButton Method */
    private void setFloatingButton(FloatingActionButton mFloating, int mFloatingSwitch)
    {
        switch (mFloatingSwitch)
        {
            case (0) : /* 다음 버튼 */
            {
                mFloating.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Vibrate.Vibrator(mContext); /* 진동 */
                        if (++mPage[0] > (mPage[1] / ScrollItemCount)) { Toast.makeText(mContext, "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = (mPage[1]/ScrollItemCount); }
                        else { new DaumLocalXmlParser(mContext, DaumCommon.createDaumURL(mCode[mCodeIntager], mContext, 1, mPage[0]), mDaumHeadTag, mTag, mCode[mCodeIntager], mRecyclerView).execute(); }
                    }
                }); break;
            }
            case (1) : /* 이전 버튼 */
            {
                mFloating.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Vibrate.Vibrator(mContext); /* 진동 */
                        if(--mPage[0] <= 0) { Toast.makeText(mContext, "첫 페이지 입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = 1; }
                        else { new DaumLocalXmlParser(mContext, DaumCommon.createDaumURL(mCode[mCodeIntager], mContext, 1, mPage[0]), mDaumHeadTag, mTag, mCode[mCodeIntager], mRecyclerView).execute(); }
                    }
                }); break;
            }
        }
    }

    /* Setting Tab Button */
    private void settingTab(TabLayout mTabLayout)
    {
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                /* ToolBar */
                mToolbar.setSubtitle(tab.getText());
                mPage[0] = 1; /* 현재 페이지 초기화 */
                mCodeIntager = tab.getPosition(); /* 탭 번호 저장 */
                Toast.makeText(mContext, tab.getText(), Toast.LENGTH_SHORT).show(); /* Toast */
                /* DaumLocal Range Search */
                try { mPage[1] = new DaumLocalXmlParser(mContext, DaumCommon.createDaumURL(mCode[tab.getPosition()], mContext, 1, mPage[0]), mDaumHeadTag, mTag, mCode[tab.getPosition()], mRecyclerView).execute().get(); }
                catch (InterruptedException e) { e.printStackTrace(); }
                catch (ExecutionException e) { e.printStackTrace(); }
            }
            @Override  public void onTabUnselected(TabLayout.Tab tab) { }
            @Override  public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    /* Optinon 관련 메소드 */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_range, menu);

        /* Search View */
        SearchView mSearchView = (SearchView)menu.findItem(R.id.RangeSearch).getActionView();
        mSearchView.setQueryHint("장소를 입력하세요.");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query) { new DaumLocalXmlParser(mContext, DaumCommon.createDaumURL(query, mContext, 2, mPage[0]), mDaumHeadTag, mTag, "FIN", mRecyclerView).execute(); return false; }
            @Override public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }
}
