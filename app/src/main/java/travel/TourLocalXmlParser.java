package travel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import travel.RecyclerAdapter.TourCultureAdapter;
import travel.RecyclerAdapter.TourRoadAdapter;

/**
 * Created by Mari on 2015-08-26.
 */
public class TourLocalXmlParser extends AsyncTask<Void, Void, Integer>
{
    /* ProgressDialog */
    private ProgressDialog Dialog = null;

    /* Context */
    private Context mContext = null;

    /* String */
    private String mURL = null;
    private String mHeadTag = null;
    private String mTag[] = null;

    /* int */
    private int mTotalCount = 0;
    private int mTagLength = 0;
    private int mMakerTag = 0;
    private int mSwitch = 0;

    /* ArrayList */
    private ArrayList<HashMap<String, String>> mHashList = null;
    private HashMap<String, String> mHash = null;

    /* MapView */
    private MapView mMapView = null;

    /* RecyclerView */
    private RecyclerView mRecyclerView = null;

    /* Spinner */
    private Spinner mSpinner = null;

    /* TextView */
    private TextView mTextView = null;

    /* Map Activity */
    public TourLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], int mMakerTag, MapView mMapView) /* MapView */
    {
        this.mContext = mContext;
        this.mURL = mURL;
        this.mHeadTag = mHeadTag;
        this.mTag = mTag;
        this.mMapView = mMapView;
        this.mMakerTag = mMakerTag;
        mSwitch = 1;
    }
    public TourLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], RecyclerView mRecyclerView, int mSwitch) /* RecyclerView */
    {
        this.mContext = mContext;
        this.mURL = mURL;
        this.mHeadTag = mHeadTag;
        this.mTag = mTag;
        this.mRecyclerView = mRecyclerView;
        this.mSwitch = mSwitch;
    }
    public TourLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], Spinner mSpinner) /* Spinner */
    {
        this.mContext = mContext;
        this.mURL = mURL;
        this.mHeadTag = mHeadTag;
        this.mTag = mTag;
        this.mSpinner = mSpinner;
        mSwitch = 2;
    }
    public TourLocalXmlParser(Context mContext, String mURL, String mHeadTag, String mTag[], TextView mTextView) /* TextView */
    {
        this.mContext = mContext;
        this.mURL = mURL;
        this.mHeadTag = mHeadTag;
        this.mTag = mTag;
        this.mTextView = mTextView;
        mSwitch = 3;
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

        /* HashMap */
        mHash = new HashMap<String, String>(10);

        /* ArrayList */
        mHashList = new ArrayList<HashMap<String, String>>(10);
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
                    if(tagName.equals(mHeadTag)) { isItemTag = true; } /* XML channel 시작과 끝부분 */
                    if(tagName.equals("item")) { mHash = new HashMap<String, String>(10); }
                }
                else if (eventType == XmlPullParser.TEXT && isItemTag)
                {
                    for(int count = 0; count < mTagLength; count++) { if(tagName.equals(mTag[count])) { mHash.put(mTag[count], parser.getText()); } }
                    if(tagName.equals("totalCount")) { mTotalCount = Integer.parseInt(parser.getText()); }
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
            Log.e("TourLocalXmlParser", "Tour Local Error.");
            Toast.makeText(mContext, "Tour Download Error.", Toast.LENGTH_SHORT).show();
        }

        return mTotalCount;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        switch (mSwitch)
        {
            case (1) : /* Daum Map Maker */
            {
                for(int count = 0, mLength = mHashList.size(); count < mLength; count++)
                {
                    /* Double */
                    double mMapX = 0.0, mMapY = 0.0;
                    if(mHashList.get(count).containsKey("mapx")) { mMapX = Double.parseDouble(mHashList.get(count).get("mapx")); } else { mMapX = 0.0; }
                    if(mHashList.get(count).containsKey("mapy")) { mMapY = Double.parseDouble(mHashList.get(count).get("mapy")); } else { mMapY = 0.0; }

                    /* String */
                    String mTitle = null;
                    if(mHashList.get(count).containsKey("title")) { mTitle = mHashList.get(count).get("title"); } else { mTitle = "제목없음"; }

                    insertMarker(mTitle, mMapY, mMapX);
                } break;
            }
            case (2) :
            {
                /* ArrayList */
                ArrayList<String> mAddrList = new ArrayList<String>(10); mAddrList.add("전체조회");
                for(int count = 0, mLength = mHashList.size(); count < mLength; count++) { mAddrList.add(mHashList.get(count).get("name")); }

                ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(mContext, R.layout.custom_spinner, mAddrList);
                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(mAdapter); break;
            }
            /* TextView */
            case (3) : { if(mHashList.get(0).containsKey("overview")) { mTextView.setText(Html.fromHtml(mHashList.get(0).get("overview"))); } else { mTextView.setText("정보 없음."); } break; }
            /* Culture RecyclerView */
            case (10) : { insertCultureRecycler(); break; }
            /* Road RecyclerView */
            case (11) : { insertRoadRecycler(); break; }
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
        switch (mMakerTag)
        {
            case (1) : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_culture); break; }
            case (2) : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_fastival); break; }
            case (3) : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_beach); break; }
            case (4) : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_tree); break; }
            case (5) : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_claw); break; }
            case (6) : { mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_museum); break; }
        }
        mMapPOIItem.setCustomImageAutoscale(false);
        mMapPOIItem.setCustomImageAnchor(0.5f, 1.0f);

        mMapView.addPOIItem(mMapPOIItem); /* 지도에 Maker 표시 */
    }

    /* insert RecyclerView Method */
    private void insertCultureRecycler()
    {
        /* ArrayList */
        ArrayList<TourCultureAdapter.TourCultureMyDate> mMyDateArrayList = new ArrayList<TourCultureAdapter.TourCultureMyDate>(10);

        /* String */
        String mString[] = new String[5];

        /* Double */
        double mDouble[] = new double[2];

        for(int count = 0, mLength = mHashList.size(); count < mLength; count++)
        {
            /* Double */
            if(mHashList.get(count).containsKey("mapx")) { mDouble[0] = Double.valueOf(mHashList.get(count).get("mapx")).doubleValue(); } else { mDouble[0] = 0.0; }
            if(mHashList.get(count).containsKey("mapy")) { mDouble[1] = Double.valueOf(mHashList.get(count).get("mapy")).doubleValue(); } else { mDouble[1] = 0.0; }

            /* String */
            if(mHashList.get(count).containsKey("addr1")) { mString[0] = mHashList.get(count).get("addr1"); } else { mString[0] = null; }
            if(mHashList.get(count).containsKey("contentid")) { mString[1] = mHashList.get(count).get("contentid"); } else { mString[1] = null; }
            if(mHashList.get(count).containsKey("tel")) { mString[2] = mHashList.get(count).get("tel"); } else { mString[2] = null; }
            if(mHashList.get(count).containsKey("title")) { mString[3] = mHashList.get(count).get("title"); } else { mString[3] = null; }
            if(mHashList.get(count).containsKey("firstimage")) { mString[4] = mHashList.get(count).get("firstimage"); } else { mString[4] = null; }

            mMyDateArrayList.add(new TourCultureAdapter.TourCultureMyDate(mString[0], mString[1], mString[2], mString[3], mString[4], mDouble[1], mDouble[0]));
        } mRecyclerView.setAdapter(new TourCultureAdapter(mContext, mMyDateArrayList));
    }

    /* insert Road RecyclerView Method */
    private void insertRoadRecycler()
    {
        /* ArrayList */
        ArrayList<TourRoadAdapter.TourRoadMyDate> mMyDateArrayList = new ArrayList<TourRoadAdapter.TourRoadMyDate>(10);

        /* String */
        String mString[] = new String[3];

        /* double */
        double mDouble[] = new double[2];

        for(int count = 0, mLength = mHashList.size(); count < mLength; count++)
        {
            /* String */
            if(mHashList.get(count).containsKey("contentid")) { mString[0] = mHashList.get(count).get("contentid"); } else { mString[0] = null; }
            if(mHashList.get(count).containsKey("firstimage")) { mString[1] = mHashList.get(count).get("firstimage"); } else { mString[1] = null; }
            if(mHashList.get(count).containsKey("title")) { mString[2] = mHashList.get(count).get("title"); } else { mString[2] = null; }

            /* Double */
            if(mHashList.get(count).containsKey("mapx")) { mDouble[0] = Double.parseDouble(mHashList.get(count).get("mapx")); } else { mDouble[0] = 0.0; }
            if(mHashList.get(count).containsKey("mapy")) { mDouble[1] = Double.parseDouble(mHashList.get(count).get("mapy")); } else { mDouble[1] = 0.0; }
            mMyDateArrayList.add(new TourRoadAdapter.TourRoadMyDate(mString[0], mString[1], mString[2], mDouble[1], mDouble[0]));
        } mRecyclerView.setAdapter(new TourRoadAdapter(mContext, mMyDateArrayList));
    }
}