package com.net.location.mylocationhow;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import basic.GPS;
import basic.RecyclerAdapter.RouteAdapter;
import basic.Vibrate;
import daum.DaumLocalXmlParser;
import travel.TourLocalXmlParser;

/**
 * Created by Mari on 2015-08-27.
 */
public class MapActivity extends BaseActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener
{
    /* Context */
    private Context mContext = MapActivity.this;

    /* MapView */
    private MapView mMapView = null;

    /* String */
    private final String mCode[] = {"FD6", "AD5", "PM9", "BK9", "LLL"};

    /* int */
    private int mCount = 0;

    /* MapReverseGeoCoder */
    private MapReverseGeoCoder mMapReverseGeoCoder = null;

    /* MapPolyline */
    private MapPolyline mMapPolyline = null;

    /* boolean */
    private boolean mDrawing = false;

    /* ArrayList */
    private ArrayList<HashMap<String, Double>> mRouteList = new ArrayList<HashMap<String, Double>>(10);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        /* ToolBar */
        mToolbar = (Toolbar) findViewById(R.id.MapToolbar); /* ToolBar */
        /* ToolBar Title */
        mToolbar.setTitle("위치"); /* ToolBar Title Text */
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        mToolbar.setNavigationIcon(R.drawable.ic_arrow); /* Icon Setting */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* DAUM */
        createDaumMap();

        /* Intent */
        Intent mIntent = getIntent(); /* Intent */
        matchIntent(mIntent.getIntExtra("Switch", 0), mIntent);

        /* Button */
        FloatingActionButton mButton[] = {(FloatingActionButton) findViewById(R.id.MapFoodButton), (FloatingActionButton) findViewById(R.id.MapHotelButton), (FloatingActionButton) findViewById(R.id.MapPharmacyButton), (FloatingActionButton) findViewById(R.id.MapBankButton), (FloatingActionButton) findViewById(R.id.MapMyLocationButton)};
        for (int count = 0, mLength = mButton.length; count < mLength; count++) { setFloatingActionButton(mButton[count], mCode[count], count); }
    }

    /* Create DAUM Method */
    private void createDaumMap()
    {
        mMapView = new MapView(this);
        mMapView.setDaumMapApiKey(this.getResources().getString(R.string.DAUM_APIKEY)); /* APIKEY SET */

        /* Cache Set */
        mMapView.setMapTilePersistentCacheEnabled(true); /* Cache 설정 */

        /* Event */
        mMapView.setShowCurrentLocationMarker(true); /* TrackingMode Maker */
        mMapView.setMapViewEventListener(this); /* Map Event */
        mMapView.setPOIItemEventListener(this); /* Maker Event */
        mMapView.setCurrentLocationEventListener(this); /* Current Event */

        /* ViewGroup */
        ViewGroup mViewGroup = (ViewGroup) findViewById(R.id.MapDaumMap);
        mViewGroup.addView(mMapView);
    }

    /* Setting FloatingActionButton Method */
    private void setFloatingActionButton(final FloatingActionButton mButton, final String mCode, final int mButtonSwitch)
    {
        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(mContext, mButton.getTitle(), Toast.LENGTH_SHORT).show(); /* Toast */
                Vibrate.Vibrator(mContext); /* 진동 */

                if(mButtonSwitch == 4)
                {
                    switch (++mCount)
                    {
                        case (1) : { mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading); break; } /* 트래킹 모드 */
                        case (2) : { mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading); break; } /* 트래킹 나침반 모드 */
                        case (3) : { mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff); mCount = 0; break; } /* 트래킹 모드 종료 */
                    }
                }
                else
                {
                    /* String */
                    final String mTag[] = {"title", "latitude", "longitude"};

                    /* MapView */
                    mMapView.removeAllPOIItems();
                    mMapView.removeAllPolylines();
                    new DaumLocalXmlParser(mContext, createDaumLocalURL(mCode, 1), mDaumHeadTag, mTag, mCode, mMapView).execute();
                }
            }
        });
    }

    /* Match Intent Method */
    private void matchIntent(final int mSwitch, Intent mIntent)
    {
        switch (mSwitch)
        {
            /* FragmentB */
            case (1) : { createMarker(mIntent.getDoubleExtra("MapX", 0), mIntent.getDoubleExtra("MapY", 0), mIntent.getStringExtra("Title")); break; }
            /* Detail */
            case (2) :
            {
                /* ArrayList */
                final ArrayList<HashMap<String, String>> mHash = (ArrayList<HashMap<String, String>>) mIntent.getSerializableExtra("HashList");
                for (int count = 0, mLength = mHash.size(); count < mLength; count++)
                {
                    double mMapX = Double.valueOf(mHash.get(count).get("latitude")).doubleValue();
                    double mMapY = Double.valueOf(mHash.get(count).get("longitude")).doubleValue();
                    createMarker(mMapX, mMapY, mHash.get(count).get("title")); /* Create Marker Method */
                } break;
            } default: { mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(GPS.getLatitude(), GPS.getLongitude()), true); break; }
        }
    }

    /* Create TourURL Method */
    private String createTourUrl(int Type, String mCode1, String mCode2, String mCode3)
    {
        /* StringBuffer */
        StringBuffer mStringBuffer = new StringBuffer();
        mStringBuffer.append("http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?ServiceKey="); mStringBuffer.append(mContext.getResources().getString(R.string.TOUR_APIKEY)); /* API KEY */
        mStringBuffer.append("&contentTypeId=");  mStringBuffer.append(Type); /* Type */
        mStringBuffer.append("&cat1="); mStringBuffer.append(mCode1); /* Cat1 - 대분류 */
        mStringBuffer.append("&cat2="); mStringBuffer.append(mCode2); /* Cat2 - 중분류 */
        mStringBuffer.append("&cat3="); mStringBuffer.append(mCode3); /* Cat3 - 소분류 */
        mStringBuffer.append("&mapX="); mStringBuffer.append(GPS.getLongitude()); /* 경도 */
        mStringBuffer.append("&mapY="); mStringBuffer.append(GPS.getLatitude()); /* 위도 */
        mStringBuffer.append("&radius="); mStringBuffer.append(mRange); /* 범위 */
        mStringBuffer.append("&listYN=Y&MobileOS=AND&MobileApp="); mStringBuffer.append(getString(R.string.app_name)); /* APP NAME */
        mStringBuffer.append("&arrange=S&numOfRows=20&pageNo=1");

        return mStringBuffer.toString();
    }

    /* Create DaumLocal Method */
    private String createDaumLocalURL(String mCode, final int mSwitch)
    {
        /* String */
        final String APIKEY = mContext.getResources().getString(R.string.DAUM_LOCALAPIKEY);

        /* StringBuffer */
        StringBuffer mStringBuffer = null;

        switch (mSwitch)
        {
            case (1) : /* 카테고리를 이용한 검색 */
            {
                mStringBuffer = new StringBuffer("https://apis.daum.net/local/v1/search/category.xml?apikey="); mStringBuffer.append(APIKEY); /* API KEY */
                mStringBuffer.append("&code="); mStringBuffer.append(mCode); /* Code */
                mStringBuffer.append("&location="); mStringBuffer.append(GPS.getLatitude()); mStringBuffer.append(","); mStringBuffer.append(GPS.getLongitude()); /* 위도와 경도 */
                mStringBuffer.append("&radius="); mStringBuffer.append(mRange); mStringBuffer.append("&sort=2&page=1"); break;
            }
        } return mStringBuffer.toString();
    }

    /* Create MapMarker Method */
    private void createMarker(double mMapX, double mMapY, String mTitle)
    {
        MapPOIItem mMapPOIItem = new MapPOIItem(); /* MapPOIItem 객체 생성 */
        mMapPOIItem.setItemName(mTitle); /* Maker Name */
        mMapPOIItem.setTag(1); /* Maker Tag */
        mMapPOIItem.setMapPoint(MapPoint.mapPointWithGeoCoord(mMapX, mMapY)); /* 위도와 경도 설정 */

        /* Custom Marker Setting */
        mMapPOIItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);  /* Custom Marker Setting */
        mMapPOIItem.setCustomImageResourceId(R.mipmap.custom_marker_dist);
        mMapPOIItem.setCustomImageAutoscale(false);
        mMapPOIItem.setCustomImageAnchor(0.5f, 1.0f);

        mMapView.addPOIItem(mMapPOIItem); /* 지도에 Maker 표시 */
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mMapX, mMapY), true); /* Marker 위치로 화면 이동 */
    }

    /* Create Polyline Method */
    private void createPolyline(double mMapX, double mMapY)
    {
        /* MapPolyline */
        mMapPolyline.addPoint(MapPoint.mapPointWithGeoCoord(mMapX, mMapY));
        mMapView.addPolyline(mMapPolyline);

        MapPointBounds mapPointBounds = new MapPointBounds(mMapPolyline.getMapPoints());
        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, 100));
    }

    /* Create ArrayListSerialize Method */
    private void createArrayListSerialize()
    {
        /* File */
        final File mFile = new File("sdcard/MyLocationHow/Map/");

                /* String */
        final String mFileString = String.format("%s%s.love", mFile, createDate());

                /* Exists */
        if (!mFile.exists()) { mFile.mkdirs(); }

        try
        {
                    /* ObjectOutputStream */
            ObjectOutputStream mObject = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(mFileString)));
            mObject.writeObject(mRouteList); mObject.close();

                    /* ContentValues */
            ContentValues mValues = new ContentValues();
            mValues.put("title", ((TextView)findViewById(R.id.ToolBarMapText)).getText().toString());
            mValues.put("path", mFileString);
            mValues.put("date", String.format("%d년%d월%d일", mCalendar.get(mCalendar.YEAR), mCalendar.get(mCalendar.MONTH), mCalendar.get(mCalendar.DAY_OF_MONTH)));
            mDataBase.insert(mDataBaseTable[2], null, mValues);
        }
        catch (IOException e)
        {
            Toast.makeText(mContext, "경로를 파일로 저장하는 과정에서 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            Log.e("ObjectOutputStream", e.getMessage()); e.printStackTrace();
        }
        catch (SQLiteException e)
        {
            Toast.makeText(mContext, "경로를 SQLite에 저장하는 과정에서 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            Log.e("SQLite(ObjectOutput)", e.getMessage()); e.printStackTrace();
        }
    }

    /* Select Route Method */
    private void selectRoute()
    {
        /* LayoutInflater */
        final LayoutInflater mLayout = getLayoutInflater();

        /* View */
        final View mView = mLayout.inflate(R.layout.custom_map_recycler, null);

        /* AlertDialog */
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
        final DialogInterface mDialog = mBuilder.setTitle("저장 된 경로").setIcon(R.drawable.ic_navigation).setView(mView).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
        }).setCancelable(false).show();

        /* Cursor */
        final Cursor mCursor = mDataBase.rawQuery("SELECT * FROM map_db", null);

        /* RecyclerView */
        final RecyclerView mRecyclerView = (RecyclerView)mView.findViewById(R.id.CustomRouteRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        insertRecyclerView(mRecyclerView, mCursor);
    }

    /* Insert RecyclerView Method */
    private void insertRecyclerView(RecyclerView mRecyclerView, Cursor mCursor)
    {
        /* ArrayList */
        ArrayList<RouteAdapter.MyRouteData> mRouteDataArrayList = new ArrayList<RouteAdapter.MyRouteData>(10);

        while(mCursor.moveToNext()) { mRouteDataArrayList.add(new RouteAdapter.MyRouteData(mCursor.getString(1), mCursor.getString(3), mCursor.getString(2), mCursor.getInt(0))); }
        mRecyclerView.setAdapter(new RouteAdapter(mContext, mRouteDataArrayList, mMapView, mMapPolyline));
    }

    /* Option 관련 메소드 */
    @Override public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_map, menu);

        /* Switch */
        final Switch mSwitch = (Switch)menu.findItem(R.id.MapDrawing).getActionView();
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    /* 화면 꺼짐 방지 */
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if(!mMapView.isShowingCurrentLocationMarker()) { mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading); }
                    mDrawing = true; Toast.makeText(mContext, "경로 표시 ON", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    /* Write Mode */
                    if(mRouteList.size() != 0) { createArrayListSerialize(); mDrawing = false; } else { Toast.makeText(mContext, "이동한 거리가 없으므로 저장이 되지 않았습니다.", Toast.LENGTH_SHORT).show(); }
                    Toast.makeText(mContext, "경로 표시 OFF", Toast.LENGTH_SHORT).show();
                    mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                }
            }
        }); return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        /* String */
        final String mTourTag[] = {"mapx", "mapy", "title"};

        /* ActionBar Item */
        switch (item.getItemId())
        {
            /* 주변 관광지 */
            case (R.id.MapRangeCulture) : { new TourLocalXmlParser(mContext, createTourUrl(12, "", "", ""), mTourHeadTag, mTourTag, 1, mMapView).execute(); mMapView.removeAllPOIItems(); return true; }
            /* 주변 행사 */
            case (R.id.MapRangeFestival) : { new TourLocalXmlParser(mContext, createTourUrl(15, "", "", ""), mTourHeadTag, mTourTag, 2, mMapView).execute(); mMapView.removeAllPOIItems(); return true; }
            /* 해수욕장 */
            case (R.id.MapBeach) : { new TourLocalXmlParser(mContext, createTourUrl(12, "A01", "A0101", "A01011200"), mTourHeadTag, mTourTag, 3, mMapView).execute(); mMapView.removeAllPOIItems(); return true; }
            /* 수목원 */
            case (R.id.MapTree) : { new TourLocalXmlParser(mContext, createTourUrl(12, "A01", "A0101", "A01010700"), mTourHeadTag, mTourTag, 4, mMapView).execute(); mMapView.removeAllPOIItems(); return true; }
            /* 온천 */
            case (R.id.MapWater) : { new TourLocalXmlParser(mContext, createTourUrl(12, "A02", "A0202", "A02020300"), mTourHeadTag, mTourTag, 5, mMapView).execute(); mMapView.removeAllPOIItems(); return true; }
            /* 박물관 */
            case (R.id.MapMuseum) : { new TourLocalXmlParser(mContext, createTourUrl(14, "A02", "A0206", "A02060100"), mTourHeadTag, mTourTag, 6, mMapView).execute(); mMapView.removeAllPOIItems(); return true; }
            /* 경로 */
            case (R.id.MapLine) : { if(mDataBase.rawQuery("SELECT * FROM map_db", null).getCount() != 0) { selectRoute(); } else { Toast.makeText(mContext, "저장 된 경로가 없습니다.", Toast.LENGTH_SHORT).show(); } return true; }
            /* 사진확인 */
            case (R.id.MapAlbum) :
            {
                /* Cursor */
                final Cursor mCursor = mDataBase.rawQuery("SELECT * FROM photo_db", null);
                for(; mCursor.moveToNext(); createMarker(mCursor.getDouble(6), mCursor.getDouble(7), mCursor.getString(1))); mCursor.close();
                return true;
            }
        } return super.onOptionsItemSelected(item);
    }

    @Override public void onMapViewZoomLevelChanged(MapView mapView, int i) {}
    @Override public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {}
    @Override public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {}
    @Override public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {}
    @Override public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {}
    @Override public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {}
    @Override public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {}
    @Override public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {}
    @Override public void onCurrentLocationUpdateFailed(MapView mapView) {}
    @Override public void onCurrentLocationUpdateCancelled(MapView mapView) {}
    @Override public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) { }
    @Override public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {}
    @Override public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {}
    @Override public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) { ((TextView)findViewById(R.id.ToolBarMapText)).setText(s); }
    @Override public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) { Toast.makeText(mContext, "지정 된 위치에 주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show(); }

    @Override
    public void onMapViewInitialized(MapView mapView)
    {
        /* Map Setting */
        mapView.setCustomCurrentLocationMarkerImage(R.mipmap.custom_marker_now, new MapPOIItem.ImageOffset(30, 0)); /* Location Mode Image Set */
        mapView.setCustomCurrentLocationMarkerTrackingImage(R.mipmap.custom_marker_now, new MapPOIItem.ImageOffset(30, 0)); /* Track Mode Image Set */

        /* GeoCoder */
        mMapReverseGeoCoder = new MapReverseGeoCoder(mContext.getResources().getString(R.string.DAUM_APIKEY), mMapView.getMapCenterPoint(), MapActivity.this, MapActivity.this);
        mMapReverseGeoCoder.startFindingAddress(); /* 주소 찾기 */

        /* MapPolyLine */
        mMapPolyline = new MapPolyline();
        mMapPolyline.setTag(3);

        /* Line Color Match */
        int mColor = 0;
        switch (mColorValue)
        {
            case ("RED") : { mColor = Color.RED; break; }
            case ("BLACK") : { mColor = Color.BLACK; break; }
            case ("BLUE") : { mColor = Color.BLUE; break; }
            case ("YELLOW") : { mColor = Color.YELLOW; break; }
            case ("GRAY") : { mColor = Color.GRAY; break; }
        } mMapPolyline.setLineColor(mColor);
    }

    /* Daum MAP 중심 좌표가 이동 된 경우 */
    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint)
    { mMapReverseGeoCoder.startFindingAddress(); /* 주소 찾기 */ }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v)
    {
        if(mDrawing)
        {
            /* Double */
            double mLatitude = mapPoint.getMapPointGeoCoord().latitude; /* 위도 */
            double mLongitude = mapPoint.getMapPointGeoCoord().longitude; /* 경도 */

            /* HashMap */
            HashMap<String, Double> mHashMap = new HashMap<String, Double>(10);
            mHashMap.put("MapX", mLatitude); /* 위도 */
            mHashMap.put("MapY", mLongitude); /* 경도 */
            mRouteList.add(mHashMap);

            /* PolyLIne */
            createPolyline(mLatitude, mLongitude);
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(final MapView mapView, final MapPOIItem mapPOIItem)
    {
        Vibrate.Vibrator(mContext); /* 진동 */

        AlertDialog.Builder mAlert = new AlertDialog.Builder(mContext);
        mAlert.setTitle(mapPOIItem.getItemName() + " 길찾기").setIcon(R.drawable.ic_navigation).setMessage(mContext.getResources().getString(R.string.MapNavigation)).setPositiveButton("실행", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                /* StringBuffer */
                StringBuffer mStringBuffer = new StringBuffer("daummaps://route?sp=");
                mStringBuffer.append(mapView.getMapCenterPoint().getMapPointGeoCoord().latitude); /* 현재 위치 위도 */ mStringBuffer.append(",");
                mStringBuffer.append(mapView.getMapCenterPoint().getMapPointGeoCoord().longitude); /* 현재 위치 경도 */ mStringBuffer.append("&ep=");
                mStringBuffer.append(mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude); /* 마커 위치 위도 */ mStringBuffer.append(",");
                mStringBuffer.append(mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude); /* 마커 위치 경도 */
                mStringBuffer.append("&by="); mStringBuffer.append(mRouteSet);

                /* Uri */
                Uri mUri = Uri.parse(mStringBuffer.toString()); /* Uri 객체 생성 */

                /* Intent */
                final Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_VIEW);
                mIntent.setData(mUri);

                /* 다음 지도 어플 설치 유무 확인 */
                if(mIntent.resolveActivity(getPackageManager()) != null) { startActivity(mIntent); } /* 설치가 되어 있는 경우 */
                else /* 설치가 되어 있지 않는 경우 */
                {
                    AlertDialog.Builder mAlert = new AlertDialog.Builder(mContext);
                    mAlert.setTitle("DAUM MAP 설치 안내문").setIcon(R.drawable.ic_install).setMessage(mContext.getResources().getString(R.string.MapInstall)).setPositiveButton("이동", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            mIntent.setData(Uri.parse("market://details?id=net.daum.android.map"));
                            startActivity(mIntent);
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }}).show();
                }
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }
}
