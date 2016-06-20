package com.net.location.mylocationhow;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import basic.Vibrate;
import travel.TourLocalXmlParser;

/**
 * Created by Mari on 2015-09-04.
 */
public class CultureActivity extends BaseActivity
{
    /* String */
    private String mStartDate = null;

    /* RecyclerView */
    private RecyclerView mRecyclerView = null;

    /* Spinner */
    private Spinner mSpinner[] = null;

    /* Button */
    private Button mDateButton = null;
    
    /* Context */
    private Context mContext = CultureActivity.this;

    /* Int */
    private int mSwitch = 2;
    private int mPage[] = {1, 1};
    private int mAreaCode[] = {0, 0, 0};

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culture);

        /* ToolBar */
        mToolbar = (Toolbar) findViewById(R.id.CultureToolbar); /* ToolBar */
        /* ToolBar Title */
        mToolbar.setTitle("문화"); /* ToolBar Title Text */
        mToolbar.setSubtitle("관광지");
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        mToolbar.setNavigationIcon(R.drawable.ic_arrow); /* Icon Setting */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* TabLayout */
        mTabLayout = (TabLayout) findViewById(R.id.CultureTab);
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.ic_rangeculture_white).setTag("관광지")); /* 관광지 */
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.ic_festival_white).setTag("축제")); /* 축제 */
        settingTab(mTabLayout); /* Tab Setting */

        /* Spinner */
        final int mArrayList[] = { R.array.CultureAdminArea, R.array.CultureType, R.array.CultureType };
        mSpinner = new Spinner[]{(Spinner)findViewById(R.id.CultureAdminAreaSpinner), (Spinner)findViewById(R.id.CultureSubAreaSpinner), (Spinner)findViewById(R.id.CultureSubAdminAreaSpinner)};
        for(int count = 0, mLength = mSpinner.length; count < mLength; count++) { settingSpinner(mSpinner[count], mArrayList[count], count); }

        /* ActionButton */
        final FloatingActionButton mFloat[] = {(FloatingActionButton)findViewById(R.id.CultureFindButton), (FloatingActionButton)findViewById(R.id.CultureNextButton), (FloatingActionButton)findViewById(R.id.CultureBackButton)};
        for(int count = 0, mLength = mFloat.length; count < mLength; count++) { setActionButton(mFloat[count], count); }

        /* RecyclerView */
        mRecyclerView = (RecyclerView) findViewById(R.id.CultureRecycler);
        setRecyclerView(mRecyclerView, (FloatingActionsMenu)findViewById(R.id.CultureMultipleDownMenu));

        mDateButton = (Button)findViewById(R.id.CultureDateButton);
        setDatePickerDialog(mDateButton);
    }

    /* setDatePickerDialog Method */
    private void setDatePickerDialog(Button mDateButton)
    {
        mDateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* Date Picker */
                DatePickerDialog.OnDateSetListener mDate = new DatePickerDialog.OnDateSetListener()
                { @Override public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) { mStartDate = String.format("%d%d%d", year, monthOfYear, dayOfMonth); } };

                /* DatePickerDialog */
                DatePickerDialog alert = new DatePickerDialog(mContext, mDate, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                alert.show();
            }
        });
    }

    /* Setting Spinner */
    private void settingSpinner(Spinner mSpinner, int mStringArray, final int mSwitch)
    {
        /* ArrayAdapter */
        ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(this, mStringArray, R.layout.custom_spinner);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);

        /* String */
        final String mTourRegionTag[] = {"code", "name"};

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                /* String */
                String mString = parent.getItemAtPosition(position).toString();

                switch (mSwitch)
                {
                    /* Admin Array */
                    case (0):
                    {
                        /* Match AdminAreaCode */
                        matchAdminAreaCode(mString);
                        new TourLocalXmlParser(mContext, createTourURL(1), mTourHeadTag, mTourRegionTag, (Spinner) findViewById(R.id.CultureSubAdminAreaSpinner)).execute(); break;
                    }
                    /* Type */
                    case (1): { matchTypeCode(mString); break; }
                    /* Sub Admin Array */
                    case (2): { mAreaCode[1] = position; break; }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /* Setting ActionButton */
    private void setActionButton(FloatingActionButton mAction, final int mInt)
    {
        /* String */
        final String mTourTag[] = {"addr1", "contentid", "mapx", "mapy", "tel", "title", "firstimage"};

        mAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Vibrate.Vibrator(mContext); /* 진동 */
                switch (mInt)
                {
                    /* 검색 */
                    case (0) :
                    {
                        mPage[0] = 1; /* 페이지 초기화 */

                        try { mPage[1] = new TourLocalXmlParser(mContext, createTourURL(mSwitch), mTourHeadTag, mTourTag, mRecyclerView, 10).execute().get(); }
                        catch (InterruptedException e) { e.printStackTrace();}
                        catch (ExecutionException e) { e.printStackTrace(); } break;
                    }
                    /* 다음 페이지 */
                    case (1) :
                    {
                        if (++mPage[0] > (mPage[1] / ScrollItemCount)) { Toast.makeText(mContext, "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = (mPage[1] / ScrollItemCount) - 1; }
                        else { new TourLocalXmlParser(mContext, createTourURL(mSwitch), mTourHeadTag, mTourTag, mRecyclerView, 10).execute(); } break;
                    }
                    /* 이전 페이지 */
                    case (2) :
                    {
                        if (--mPage[0] <= 0) { Toast.makeText(mContext, "첫 페이지 입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = 1; }
                        else { new TourLocalXmlParser(mContext, createTourURL(mSwitch), mTourHeadTag, mTourTag, mRecyclerView, 10).execute(); } break;
                    }
                }
            }
        });
    }

    /* Tab Setting Method */
    private void settingTab(TabLayout mTabLayout)
    {
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(mContext, tab.getTag().toString(), Toast.LENGTH_SHORT).show(); /* Toast */

                /* Tab Switch */
                switch (tab.getPosition())
                {
                    /* 관광지 */
                    case (0):
                    {
                        /* ToolBar */
                        mToolbar.setSubtitle("관광지");
                        /* Visibility */
                        mSpinner[1].setVisibility(View.VISIBLE);
                        mDateButton.setVisibility(View.GONE);
                        mSwitch = 2; break;
                    }
                    /* 행사 */
                    case (1):
                    {
                        /* Calender */
                        mStartDate = String.format("%d%d%d", mCalendar.get(mCalendar.YEAR), mCalendar.get(mCalendar.MONTH), mCalendar.get(mCalendar.DAY_OF_MONTH));

                        /* ToolBar */
                        mToolbar.setSubtitle("행사");
                        /* Visibility */
                        mSpinner[1].setVisibility(View.GONE);
                        mDateButton.setVisibility(View.VISIBLE);
                        mSwitch = 3; break;
                    }
                }

            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    /* Match AdminAreaCode Method */
    private void matchAdminAreaCode(String mArea)
    {
        switch (mArea)
        {
            /* 특별시 */
            case ("서울") : { mAreaCode[0]=1; break; }
            case ("세종") : { mAreaCode[0]=8; break; }
            case ("제주도") : { mAreaCode[0]=39; break; }
    		/* 광역시 */
            case ("인천") : { mAreaCode[0]=2; break; }
            case ("대전") : { mAreaCode[0]=3; break; }
            case ("대구") : { mAreaCode[0]=4; break; }
            case ("광주") : { mAreaCode[0]=5; break; }
            case ("부산") : { mAreaCode[0]=6; break; }
            case ("울산") : { mAreaCode[0]=7; break; }
    		/* 도 */
            case ("경기도") : { mAreaCode[0]=31; break; }
            case ("강원도") : { mAreaCode[0]=32; break; }
            case ("충청북도") : { mAreaCode[0]=33; break; }
            case ("충청남도") : { mAreaCode[0]=34; break; }
            case ("경상북도") : { mAreaCode[0]=35; break; }
            case ("경상남도") : { mAreaCode[0]=36; break; }
            case ("전라북도") : { mAreaCode[0]=37; break; }
            case ("전라남도") : { mAreaCode[0]=38; break; }
        }
    }

    /* Match TypeCode Method */
    private void matchTypeCode(String mType)
    {
        switch (mType)
        {
            case ("관광지"): { mAreaCode[2] = 12; break; }
            case ("문화시설"): { mAreaCode[2] = 14; break; }
            case ("레포츠"): { mAreaCode[2] = 28; break; }
            case ("쇼핑"): { mAreaCode[2] = 38; break; }
        }
    }

    /* CreateTourURL Method */
    private String createTourURL(int mSwitch)
    {
        /* StringBuffer */
        StringBuffer mStringBuffer = null;

        /* String */
        final String APPNAME = getResources().getString(R.string.app_name);
        final String APIKEY = getResources().getString(R.string.TOUR_APIKEY);

        switch (mSwitch)
        {
            case (1) : /* 지역 코드 조회 */
            {
                /* StringBuffer 관련 구문 */
                mStringBuffer= new StringBuffer("http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaCode?ServiceKey="); mStringBuffer.append(APIKEY); /* API KEY */
                mStringBuffer.append("&MobileOS=AND&MobileApp="); mStringBuffer.append(APPNAME); /* APP NAME */
                mStringBuffer.append("&numOfRows=40&areaCode="); mStringBuffer.append(mAreaCode[0]); /* 지역코드 삽입 */ break;
            }
            case (2) : /* 지역 관광지 조회 */
            {
                /* StringBuffer 관련 구문 */
                mStringBuffer = new StringBuffer("http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList?ServiceKey=");mStringBuffer.append(APIKEY); /* API KEY */
                mStringBuffer.append("&contentTypeId=");  mStringBuffer.append(mAreaCode[2]); /* 관관 타입 코드 */
                mStringBuffer.append("&areaCode="); mStringBuffer.append(mAreaCode[0]); /* 지역 코드 */
                mStringBuffer.append("&sigunguCode="); if(mAreaCode[1] != 0) { mStringBuffer.append(mAreaCode[1]); } /* 시/구/군 의 값이 사용자에 의해 입력 받았을 경우 */
                mStringBuffer.append("&cat1=&cat2=&cat3=&listYN=Y&MobileOS=AND&MobileApp="); mStringBuffer.append(APPNAME);
                mStringBuffer.append("&arrange=O&numOfRows="); mStringBuffer.append(ScrollItemCount); mStringBuffer.append("&pageNo="); mStringBuffer.append(mPage[0]); break;
            }
            case (3) : /* 지역 축제 조회 */
            {
                /* StringBuffer 관련 구문 */
                mStringBuffer = new StringBuffer("http://api.visitkorea.or.kr/openapi/service/rest/KorService/searchFestival?ServiceKey="); mStringBuffer.append(APIKEY); /* API KEY */
                mStringBuffer.append("&eventStartDate="); mStringBuffer.append(mStartDate); /* 날짜 */
                mStringBuffer.append("&eventEndDate=&areaCode="); mStringBuffer.append(mAreaCode[0]); /* 지역코드 삽입 */
                mStringBuffer.append("&sigunguCode="); if(mAreaCode[1] != 0) { mStringBuffer.append(mAreaCode[1]); }
                mStringBuffer.append("&cat1=&cat2=&cat3=&listYN=Y&MobileOS=AND&MobileApp="); mStringBuffer.append(APPNAME);
                mStringBuffer.append("&arrange=O&numOfRows="); mStringBuffer.append(ScrollItemCount); mStringBuffer.append("&pageNo="); mStringBuffer.append(mPage[0]); break;
            }
        } return mStringBuffer.toString();
    }
}
