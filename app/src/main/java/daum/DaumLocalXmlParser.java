package daum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.net.location.mylocationhow.MapActivity;
import com.net.location.mylocationhow.R;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import basic.Vibrate;
import daum.RecyclerAdapter.DaumAdapter;

/**
 * Created by Mari on 2015-08-26.
 */
public class DaumLocalXmlParser extends AsyncTask<Void, Void, Integer>
{
    /* ProgressDialog */
    private ProgressDialog Dialog = null;

    /* Context */
    private Context mContext = null;

    /* String */
    private String mURL = null;
    private String mHeadTag = null;
    private String mTag[] = null;
    private String mCode = null;

    /* int */
    private int mTotalCount = 0;
    private int mTagLength = 0;
    private int mSwitch = 0;

    /* double */
    private double mDouble[] = new double[2];

    /* ArrayList */
    private ArrayList<HashMap<String, String>> mHashList = null;

    /* HashMap */
    private HashMap<String, String> mHash = null;

    /* MapView */
    private MapView mMapView = null;

    /* RecyclerView */
    private RecyclerView mRecyclerView = null;

    /* ImageView */
    private ImageView mImageView = null;

    /* TextView */
    private TextView mTextView = null;

    /* DaumLocalXmlParser */
    public DaumLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], String mCode)
    {
        this.mContext = mContext;
        this.mURL = mURL;
        this.mHeadTag = mHeadTag;
        this.mTag = mTag;
        this.mCode = mCode;
    }
    public DaumLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], String mCode, RecyclerView mRecyclerView)
    {
        this(mContext, mURL, mHeadTag, mTag, mCode);
        this.mRecyclerView = mRecyclerView;
        mSwitch = 1;
    }
    public DaumLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], String mCode, MapView mMapView)
    {
        this(mContext, mURL, mHeadTag, mTag, mCode);
        this.mMapView = mMapView;
        mSwitch = 2;
    }
    public DaumLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], String mCode, double[] mDouble, ImageView mImageView)
    {
        this(mContext, mURL, mHeadTag, mTag, mCode);
        this.mDouble= mDouble; this.mImageView = mImageView;
        mSwitch = 3;
    }
    public DaumLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], TextView mTextView)
    {
        this.mContext = mContext;
        this.mURL = mURL;
        this.mHeadTag = mHeadTag;
        this.mTag = mTag;
        this.mTextView = mTextView;
        mSwitch = 4;
    }

    @Override /* 프로세스가 실행되기 전에 실행 되는 부분 - 초기 설정 부분 */
    protected void onPreExecute()
    {
		/* Dialog 설정 구문 */
        Dialog = new ProgressDialog(mContext);
        Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); /* 원형 프로그래스 다이얼 로그 스타일로 설정 */
        Dialog.setMessage("잠시만 기다려주세요.");
        Dialog.show();

        /* Length */
        mTagLength = mTag.length; /* 배열 길이 저장 */

        /* ArrayList */
        mHashList = new ArrayList<HashMap<String, String>>(10);

        /* HashMap */
        mHash = new HashMap<String, String>(10);
     }

    @Override
    protected Integer doInBackground(Void... params)
    {
        try
        {
			/* Xml pull 파실 객체 생성 */
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

			/* 외부 사이트 연결 관련 구문 */
            URL url = new URL(mURL); /* URL 객체 생성 */
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
                    if(tagName.equals(mHeadTag)) { isItemTag = true; }
                    if(tagName.equals("item")) { mHash = new HashMap<String, String>(10); }
                }
                else if (eventType == XmlPullParser.TEXT && isItemTag)
                {
                    for(int count = 0; count < mTagLength; count++) { if(tagName.equals(mTag[count]) && !parser.getText().equals("\n")) { mHash.put(mTag[count], parser.getText()); } }
                    if(tagName.equals("totalCount") && !parser.getText().equals("\n")) { mTotalCount = Integer.parseInt(parser.getText()); }
                }
                else if (eventType == XmlPullParser.END_TAG)
                {
                    tagName = parser.getName();
                    if(tagName.equals(mHeadTag)) { isItemTag = false; }
                    if(tagName.equals("item")) { mHashList.add(mHash); }
                }
                eventType = parser.next(); /* 다음 XML 객체로 이동 */
            }
        }
        catch (Exception e)
        {
            Log.e("DaumLocalXmlParser", "Daum Local Error."); e.printStackTrace();
            Toast.makeText(mContext, "DAUM Download Error.", Toast.LENGTH_SHORT).show();
        }
        return mTotalCount;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        switch (mSwitch)
        {
            /* RecycleView */
            case (1) : {insertRecycleMethod(); break; }
            /* DAUM Maker */
            case (2) :
            {
                /* Marker for */
                for(int count = 0, mLength = mHashList.size(); count < mLength; count++)
                {
                    double mMapX = Double.valueOf(mHashList.get(count).get("latitude")).doubleValue(); /* MapX */
                    double mMapY = Double.valueOf(mHashList.get(count).get("longitude")).doubleValue(); /* MapY */
                    insertMarker(mHashList.get(count).get("title"), mMapX, mMapY); /* Marker Insert */
                } break;
            }
            /* ImageView */
            case (3) :
            {
                /* StringBuffer */
                StringBuffer mStringBuffer = new StringBuffer("https://maps.googleapis.com/maps/api/staticmap?center=");
                mStringBuffer.append(mDouble[0]); mStringBuffer.append(","); mStringBuffer.append(mDouble[1]);

                /* Marker */
                for(int count = 0, mLength = mHashList.size(); count < mLength; count++)
                { mStringBuffer.append("&markers=color:green|label:K|"); mStringBuffer.append(mHashList.get(count).get("latitude")); mStringBuffer.append(","); mStringBuffer.append(mHashList.get(count).get("longitude")); }
                mStringBuffer.append("%7C11211&sensor=false&zoom=15&size=300x300&key=");
                mStringBuffer.append(mContext.getResources().getString(R.string.GOOGLE_APIKEY)); /* APIKEY */

                Glide.with(mContext).load(mStringBuffer.toString()).override(300, 300).into(mImageView);

                /* ImageView Setting */
                mImageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Vibrate.Vibrator(mContext); /* 진동 */
                        Snackbar.make(v, (mCode == "FD6" ? "주변 음식점" : "주변 숙소"), Snackbar.LENGTH_SHORT).setAction("이동", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) { mContext.startActivity(new Intent(mContext, MapActivity.class).putExtra("Switch", 2).putExtra("HashList", mHashList)); }
                        }).show();
                    }
                }); break;
            }
            /* TextView */
            case (4) : { mTextView.setText(String.format("%d 개", mTotalCount)); break; }
        }
        Dialog.dismiss(); /* Dialog 종료 */
        super.onPostExecute(result);
    }

    /* Maker Method */
    private void insertMarker(String mTitle, double mMapX, double mMapY)
    {
        MapPOIItem mMapPOIItem = new MapPOIItem(); /* MapPOIItem 객체 생성 */
        mMapPOIItem.setItemName(mTitle); /* Maker Name */
        mMapPOIItem.setTag(1); /* Maker Tag */
        mMapPOIItem.setMapPoint(MapPoint.mapPointWithGeoCoord(mMapX, mMapY)); /* 위도와 경도 설정 */

        /* Custom Marker Setting */
        mMapPOIItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);  /* Custom Marker Setting */
        /* Custom Image Switch */
        switch (mCode)
        {
            case ("FD6") : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_food); break; } /* 식당 */
            case ("AD5") : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_hotel); break; } /* 숙소 */
            case ("PM9") : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_phar); break; } /* 숙소 */
            case ("BK9") : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_bank); break; } /* 숙소 */
        }
        mMapPOIItem.setCustomImageAutoscale(false);
        mMapPOIItem.setCustomImageAnchor(0.5f, 1.0f);

        mMapView.addPOIItem(mMapPOIItem); /* 지도에 Maker 표시 */
    }

    /* Recycler Insert Method */
    private void insertRecycleMethod()
    {
        /* ArrayList */
        ArrayList<DaumAdapter.DaumMyData> mMyDateArrayList = new ArrayList<DaumAdapter.DaumMyData>(10);

            /* String */
        String mString[] = new String[mTagLength];

            /* Double */
        double mDouble[] = new double[2];

        for(int count = 0, mLength = mHashList.size(); count < mLength; count++)
        {
            /* Double */
            mDouble[0] = Double.valueOf(mHashList.get(count).get("latitude")).doubleValue(); /* MapX */
            mDouble[1] = Double.valueOf(mHashList.get(count).get("longitude")).doubleValue(); /* MapY */
            /* String */
            mString[0] = mHashList.get(count).containsKey("title") ? mHashList.get(count).get("title") : "상호명 미제공"; /* Title */
            mString[1] = mHashList.get(count).containsKey("phone") ? mHashList.get(count).get("phone") : "0"; /* Phone */
            mString[2] = mHashList.get(count).containsKey("address") ? mHashList.get(count).get("address") : "주소 미제공"; /* Address */
            mString[3] = mHashList.get(count).containsKey("distance") ? mHashList.get(count).get("distance") : "위치 미제공"; /* Distance */
            mString[4] = mHashList.get(count).containsKey("placeUrl") ? mHashList.get(count).get("placeUrl") : "http://localhost"; /* placeUrl */
            mMyDateArrayList.add(new DaumAdapter.DaumMyData(mString[0], mDouble[0], mDouble[1], mString[1], mString[2], mString[3], mString[4]));
        } mRecyclerView.setAdapter(new DaumAdapter(mContext, mMyDateArrayList, mCode));
    }
}