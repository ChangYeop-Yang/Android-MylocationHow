package com.net.location.mylocationhow;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import basic.GPS;
import basic.RecyclerAdapter.WeatherAdapter;

/**
 * Created by Mari on 2015-08-25.
 */
public class Fragment_Weather extends Fragment
{
    /* View */
    private View mView = null;

    /* Double */
    private int mGridX = 0; /* X 좌표 */
    private int mGridY = 0; /* Y 좌표 */

    /* String */
    private final String mTag[] = {"day", "hour", "wfKor", "tmn", "tmx", "pop", "temp", "wdKor"};
    private final String mHeadTag = "wid";

    /* ArrayList */
    private ArrayList<String> mTownWeather[] = new ArrayList[8];

    /* RecyclerView */
    private RecyclerView mRecyclerView[] = null;

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
         mView = inflater.inflate(R.layout.fragment_weather, container, false);

         /* KMA */
         changeDFSXY(GPS.getLatitude(), GPS.getLongitude());
         String KMA_URL = String.format("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=%d&gridy=%d", mGridX, mGridY);

         new KMAWeatherPerser().execute(KMA_URL); /* 현재 날씨 Parser */

         /* RecyclerView */
         mRecyclerView = new RecyclerView[]{(RecyclerView)mView.findViewById(R.id.WeatherExcpetionRecycler1), (RecyclerView)mView.findViewById(R.id.WeatherExcpetionRecycler2)};
         for(int count = 0, mLength = mRecyclerView.length; count < mLength; count++) { setRecyclerView(mRecyclerView[count]); }

         return mView;
     }

    /* setRecyclerView Method */
    private void setRecyclerView(RecyclerView mRecyclerView)
    {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /* ChangDFSXY */
    private void changeDFSXY(double mMapX, double mMapY)
    {
                        /* LCC Value */
        /* Double */
        final double RE = 6371.00877; /* 지구의 반경 (Km) */
        final double GRID = 5.0; /* 격자 간격 (Km) */
        final double SLAT1 = 30.0; /* 투영 위도 1 */
        final double SLAT2 = 60.0; /* 투영 위도 1 */
        final double OLON = 126.0; /* 기준점 경도 */
        final double OLAT = 38.0; /* 기준점 위도 */

        /* Int */
        final int XO = 43; /* 기준점 X 좌표 */
        final int YO = 136; /* 기준점 Y 좌표 */

                        /* DFS Value */
        /* Double */
        final double DEGRAD = Math.PI / 180.0;
        final double RADDEG = 180.0 / Math.PI;
        final double DRE = RE / GRID;
        final double DSLAT1 = SLAT1 * DEGRAD;
        final double DSLAT2 = SLAT2 * DEGRAD;
        final double DOLON = OLON * DEGRAD;
        final double DOLAT = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + DSLAT2 * 0.5) / Math.tan(Math.PI * 0.25 + DSLAT1 * 0.5);
        sn = Math.log(Math.cos(DSLAT1) / Math.cos(DSLAT2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + DSLAT1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(DSLAT1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + DOLAT * 0.5);
        ro = DRE * sf / Math.pow(ro, sn);

        /* 좌표 변환 */
        double ra = Math.tan(Math.PI * 0.25 + (mMapX) * DEGRAD * 0.5);
        ra = DRE * sf / Math.pow(ra, sn);
        double theta = mMapY * DEGRAD - DOLON;
        if(theta > Math.PI) { theta -= 2.0 * Math.PI; }
        if(theta < -Math.PI) { theta += 2.0 * Math.PI; }
        theta *= sn;
        mGridX = (int)Math.floor(ra * Math.sin(theta) + XO + 0.5);
        mGridY = (int)Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
    }

    /* IconMatch Method */
    private void setIconWeather(String mString, ImageView mImageView)
    {
        /* Weather Icon Match Switch */
        switch (mString)
        {
            case ("맑음") : { mImageView.setImageResource(R.drawable.ic_sun); break; }
            case ("구름 조금") : { mImageView.setImageResource(R.drawable.ic_cloudy); break; }
            case ("구름 많음") : { mImageView.setImageResource(R.drawable.ic_verycloudy); break; }
            case ("흐림") : { mImageView.setImageResource(R.drawable.ic_blur); break; }
            case ("비") : { mImageView.setImageResource(R.drawable.ic_rain); break; }
            case ("눈/비") : { mImageView.setImageResource(R.drawable.ic_snow); break; }
            case ("눈") : { mImageView.setImageResource(R.drawable.ic_snow); break; }
        }
    }

    /* matchWeatherIcon */
    private void matchNowWeather()
    {
        /* TextView */
        ((TextView)mView.findViewById(R.id.WeatherTimeTextView)).setText(mTownWeather[1].get(0)); /* 기상시각 */
        ((TextView)mView.findViewById(R.id.WeatherStateTextView)).setText(mTownWeather[2].get(0)); /* 날씨상태 */
        ((TextView)mView.findViewById(R.id.WeatherRainTextView)).setText(mTownWeather[5].get(0)); /* 강수량 */
        ((TextView)mView.findViewById(R.id.WeatherTempTextView)).setText(mTownWeather[6].get(0)); /* 온도 */
        ((TextView)mView.findViewById(R.id.WeatherWindTextView)).setText(mTownWeather[7].get(0)); /* 바람방향 */

        /* ImageView */
        setIconWeather(mTownWeather[2].get(0), (ImageView) mView.findViewById(R.id.WeatherNowImageView));
    }

    /* KMA Weather Parser */
    private class KMAWeatherPerser extends AsyncTask<String, Void, Void>
    {
        /* ProgressDialog */
        private ProgressDialog Dialog = null;

        /* Int */
        private int mTagLength = mTag.length;

         /* 프로세스가 실행되기 전에 실행 되는 부분 - 초기 설정 부분 */
        protected void onPreExecute()
        {
		    /* Dialog 설정 구문 */
            Dialog = new ProgressDialog(mView.getContext());
            Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); /* 원형 프로그래스 다이얼 로그 스타일로 설정 */
            Dialog.setMessage("잠시만 기다려주세요.");
            Dialog.show();

            /* ArrayList */
            for(int count = 0; count < mTagLength; count++) { mTownWeather[count] = new ArrayList<String>(8); }
        }

        @Override
        protected Void doInBackground(String... params)
        {
            try
            {
			/* Xml pull 파실 객체 생성 */
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();

			/* 외부 사이트 연결 관련 구문 */
                URL url = new URL(params[0]); /* URL 객체 생성 */
                InputStream in = url.openStream(); /* 해당 URL로 연결 */
                parser.setInput(in, "UTF-8"); /* 외부 사이트 데이터와 인코딩 방식을 설정 */

		    /* XML 파싱 관련 변수 관련 구문 */
                int eventType = parser.getEventType(); /* 파싱 이벤트  관련 저장 변수 생성 */
                boolean isItemTag = false;
                String tagName = null; /* Tag의 이름을 저장 하는 변수 생성 */

		        /* XML 문서를 읽어 들이는 구문 */
                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType == XmlPullParser.START_TAG)
                    {
                        tagName = parser.getName();
                        if (tagName.equals(mHeadTag)) { isItemTag = true; } /* XML channel 시작과 끝부분 */

                        /* Tag */
                        for(int count = 0; count < mTagLength; count++)
                        {
                            if(tagName.equals(mTag[count]))
                            {
                                String mTemp = "";
                                switch (mTag[count]) /* 기호 부착 */
                                {
                                    case ("temp") : { mTemp = " ℃"; break; }
                                    case ("tmx") : { mTemp = " ℃"; break; }
                                    case ("tmn") : { mTemp = " ℃"; break; }
                                    case ("hour") : { mTemp = " H"; break; }
                                    case ("pop") : { mTemp = " %"; break; }
                                } mTownWeather[count].add(parser.nextText() + mTemp);
                            }
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG) { tagName = parser.getName(); if(tagName.equals(mHeadTag)) { isItemTag = false; } }
                    eventType = parser.next(); /* 다음 XML 객체로 이동 */
                }
            }
            catch (Exception e)
            {
                Log.e("KMAXmlParser", "KMA Error.");
                Toast.makeText(mView.getContext(), "KMA Download Error.", Toast.LENGTH_SHORT).show();
            } return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            /* 현재 날씨 */
            matchNowWeather(); /* 현재 날씨 정보 */

            /* int */
            final int mDay1Point[] = {mTownWeather[0].indexOf("1"), mTownWeather[0].lastIndexOf("1")};
            final int mDay2Point[] = {mTownWeather[0].indexOf("2"), mTownWeather[0].lastIndexOf("2")};

            setRecyclerView((RecyclerView)mView.findViewById(R.id.WeatherExcpetionRecycler1), mDay1Point[0], mDay1Point[1]);
            if( mDay2Point[0] != 0 ) { setRecyclerView((RecyclerView)mView.findViewById(R.id.WeatherExcpetionRecycler2), mDay2Point[0], mDay2Point[1]); }
            else { ((RecyclerView)mView.findViewById(R.id.WeatherExcpetionRecycler2)).setEnabled(false); }

            Dialog.dismiss(); /* Dialog 종료 */
            super.onPostExecute(result);
        }

        /* setRecyclerView Method */
        private void setRecyclerView(RecyclerView mRecyclerView, int mStart, int mLength)
        {
            /* ArrayList */
            ArrayList<WeatherAdapter.MyWeatherData> mMyDateArrayList = new ArrayList<WeatherAdapter.MyWeatherData>(10);

            /* String */
            String mString[] = new String[5];

            for(int count = mStart; count < mLength; count++)
            {
                /* String */
                mString[0] = mTownWeather[1].get(count);
                mString[1] = mTownWeather[2].get(count);
                mString[2] = mTownWeather[3].get(count);
                mString[3] = mTownWeather[4].get(count);
                mString[4] = mTownWeather[5].get(count);

                mMyDateArrayList.add(new WeatherAdapter.MyWeatherData(mString[0], mString[1], mString[2], mString[3], mString[4]));
            } mRecyclerView.setAdapter(new WeatherAdapter(mView.getContext(), mMyDateArrayList));
        }
    }
 }
