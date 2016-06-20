package com.net.location.mylocationhow;

import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import basic.BitmapSave;
import basic.GPS;
import daum.DaumLocalXmlParser;

/**
 * Created by Mari on 2015-08-25.
 */
public class Fragment_Location extends Fragment
{
    /* View */
    private View mView = null;

    /* Context */
    private Context mContext = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
            /* View */
        mView = inflater.inflate(R.layout.fragment_location, container, false);

        /* Context */
        mContext = mView.getContext();

        /* TextView */
        final TextView mDAUMTextView[] = {(TextView)mView.findViewById(R.id.LocationFoodTextView), (TextView)mView.findViewById(R.id.LocationHotelTextView), (TextView)mView.findViewById(R.id.LocationOilTextView), (TextView)mView.findViewById(R.id.LocationConvenienceTextView)};
        final TextView mFootTextView[] = {(TextView)mView.findViewById(R.id.LocationCultureTextView), (TextView)mView.findViewById(R.id.LocationRoadTextView)};
        for(int count = 0, mLength = mDAUMTextView.length; count < mLength; count++) { setDAUMLocationRange(mDAUMTextView[count], count); }
        for(int count = 0, mLength = mFootTextView.length; count < mLength; count++) { setFootRange(mFootTextView[count], count); }
        GPS.createMapAddress(mContext, (TextView) mView.findViewById(R.id.LocationNowText));

        /* ImageView */
        GPS.createNowLocationMap(mContext, (ImageView) mView.findViewById(R.id.LocationImageView));

        return mView;
    }

    /* Create DAUM URL Method */
    private String createDAUMURL(String mCode)
    {
        StringBuffer mStringBuffer = new StringBuffer("https://apis.daum.net/local/v1/search/category.xml?apikey="); mStringBuffer.append(getResources().getString(R.string.DAUM_LOCALAPIKEY)); /* API KEY */
        mStringBuffer.append("&code="); mStringBuffer.append(mCode); /* Code */
        mStringBuffer.append("&location="); mStringBuffer.append(GPS.getLatitude()); mStringBuffer.append(","); mStringBuffer.append(GPS.getLongitude()); /* 위도와 경도 */
        mStringBuffer.append("&radius=20000&sort=2&page=1");

        return mStringBuffer.toString();
    }

    /* Setting DAUM Location Range Method */
    private void setDAUMLocationRange(TextView mTextView, int mSwitch)
    {
        /* String */
        final String mTag[] = {"totalCount"};

        switch (mSwitch)
        {
            /* 주변 음식점 현황 */
            case (0) : { new DaumLocalXmlParser(mContext, createDAUMURL("FD6"), "channel", mTag, mTextView).execute(); break; }
            /* 주변 숙박시설 현황 */
            case (1) : { new DaumLocalXmlParser(mContext, createDAUMURL("AD5"), "channel", mTag, mTextView).execute(); break; }
            /* 주변 주유소 현황 */
            case (2) : { new DaumLocalXmlParser(mContext, createDAUMURL("OL7"), "channel", mTag, mTextView).execute(); break; }
            /* 주변 편의점 현황 */
            case (3) : { new DaumLocalXmlParser(mContext, createDAUMURL("CS2"), "channel", mTag, mTextView).execute(); break; }
        }
    }

    /* Setting Foot Range Method */
    private void setFootRange(TextView mTextView, int mSwitch)
    {
        /* SQLite 관련 변수 */
        BaseActivity.dbHelper mHelper = null;
        SQLiteDatabase mDataBase = null;

        /* SQLite */
        try /* 읽기/쓰기 모드로 데이터베이스를 오픈 */
        {
            mHelper = new BaseActivity.dbHelper(getActivity());
            mDataBase = mHelper.getWritableDatabase();
        }
        catch(SQLiteException ex) /* 읽기 전용 모드로 데이터베이스를 오픈 */
        {
            mDataBase = mHelper.getReadableDatabase();
            Toast.makeText(mContext, "SQLite 문제가 발생 하였습니다.", Toast.LENGTH_SHORT).show();
        }

        switch (mSwitch)
        {
            /* 문화 발도장 현황 */
            case (0) : { mTextView.setText(String.format("%d 개", mDataBase.rawQuery("SELECT * FROM culture_db", null).getCount())); break; }
            /* 코스 발도장 현황 */
            case (1) : { mTextView.setText(String.format("%d 개", mDataBase.rawQuery("SELECT * FROM road_db", null).getCount())); break; }
        }
    }
}