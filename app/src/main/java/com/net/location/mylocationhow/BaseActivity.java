package com.net.location.mylocationhow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.Sharer;
import com.facebook.share.widget.ShareDialog;
import com.tsengvn.typekit.Typekit;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.Calendar;

import basic.Sound;

/**
 * Created by Mari on 2015-09-05.
 */
public class BaseActivity extends AppCompatActivity implements DrawerLayout.DrawerListener, NavigationView.OnNavigationItemSelectedListener
{
    /* Calendar */
    protected Calendar mCalendar = null;

    /* ActionBarToggle */
    protected static ActionBarDrawerToggle mToggle = null;

    /* int */
    private int scrolledDistance  = 0;
    protected int ScrollItemCount = 15;
    protected int mRange = 20000;
    protected int mAlbumItemCount = 3;
    protected int mPhotoVideoTime = 12000;

    /* Boolean */
    private boolean controlsVisible  = true;

    /* String */
    protected final String mDataBaseTable[] = {"photo_db", "road_db", "map_db", "culture_db"};
    protected String mDaumHeadTag = "channel";
    protected String mTourHeadTag = "response";
    protected String mAppName = null;
    protected String mColorValue = null;
    protected String mRouteSet = null;
    protected String mAlbumShare = null;

    /* SQLite 관련 변수 */
    private dbHelper mHelper = null;
    protected SQLiteDatabase mDataBase = null;

    /* ToolBar */
    protected Toolbar mToolbar = null;

    /* TabLayout */
    protected TabLayout mTabLayout = null;

    /* NavigationView */
    protected NavigationView mNavigationView = null;

    /* Drawer */
    protected DrawerLayout mDrawerLayout = null;

    /* FaceBook CallbackManager */
    protected CallbackManager mCallBack = null;
    protected ShareDialog mShareDialog = null;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Display */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* Custom Setting Text */
        Typekit.getInstance().addNormal(Typekit.createFromAsset(this, "fonts/BMJUA_ttf.ttf"));

        /* Calendar */
        mCalendar = Calendar.getInstance();

        /* String */
        mAppName = getResources().getString(R.string.app_name);

        /* SQLite */
        try /* 읽기/쓰기 모드로 데이터베이스를 오픈 */
        {
            mHelper = new dbHelper(this);
            mDataBase = mHelper.getWritableDatabase();
        }
        catch(SQLiteException ex) /* 읽기 전용 모드로 데이터베이스를 오픈 */
        {
            mDataBase = mHelper.getReadableDatabase();
            Toast.makeText(getApplicationContext(), "SQLite 문제가 발생 하였습니다.", Toast.LENGTH_SHORT).show();
        }

        /* SharedPreferences */
        setSharedPreferences();

        /* FaceBook */
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallBack = CallbackManager.Factory.create();

        mShareDialog = new ShareDialog(this);
        mShareDialog.registerCallback(mCallBack, new FacebookCallback<Sharer.Result>()
        {
            @Override public void onSuccess(Sharer.Result result) { Toast.makeText(getApplicationContext(), "담벼락이 정상적으로 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show(); }
            @Override public void onCancel() { Toast.makeText(getApplicationContext(), "담벼락 등록이 취소되었습니다.", Toast.LENGTH_SHORT).show(); }
            @Override public void onError(FacebookException e)
            {
                Toast.makeText(getApplicationContext(), "담벼락을 등록 과정 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace(); Log.e("FaceBook onError.", e.getMessage());
            }
        });
    }

    /* Setting SharedPreferences Method */
    private final void setSharedPreferences()
    {
        /* Shared */
        final SharedPreferences mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
        mColorValue = mSharedPre.getString("Line", "RED");
        mRouteSet = mSharedPre.getString("Navigation", "CAR");
        mRange = Integer.parseInt(mSharedPre.getString("MapRange", "20000"));
        mAlbumItemCount = Integer.parseInt(mSharedPre.getString("Album", "3"));
        mAlbumShare = mSharedPre.getString("AlbumShare", "image/*");
        mPhotoVideoTime = Integer.parseInt(mSharedPre.getString("PhotoTime", "12000"));
    }

    /* Setting RecyclerView Method */
    protected final void setRecyclerView(RecyclerView mRecyclerView, final View mView)
    {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                /* Scroll Bottom Move */
                if (scrolledDistance > ScrollItemCount && controlsVisible)
                {
                    mView.setVisibility(View.GONE);
                    controlsVisible = false;
                    scrolledDistance = 0;
                }
                /* Scroll Top Move */
                else if (scrolledDistance < -ScrollItemCount && !controlsVisible)
                {
                    mView.setVisibility(View.VISIBLE);
                    controlsVisible = true;
                    scrolledDistance = 0;
                }
                if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                    scrolledDistance += dy;
                }
            }
        });
    }

    /* createDate Method */
    protected final String createDate()
    { return String.format("%d%d%d%d%d%d", mCalendar.get(mCalendar.YEAR), mCalendar.get(mCalendar.MONTH), mCalendar.get(mCalendar.DAY_OF_MONTH), mCalendar.get(mCalendar.HOUR), mCalendar.get(mCalendar.MINUTE), mCalendar.get(mCalendar.SECOND)); }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(TypekitContextWrapper.wrap(newBase)); }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        /* ActionBar Item */
        switch (item.getItemId())
        {
            case (R.id.toolSetting): { startActivity(new Intent(getApplicationContext(), SettingActivity.class)); return true; } /* 설정화면 */
            case (android.R.id.home) : { finish(); return true; } /* 뒤로가기 */
        }
        return super.onOptionsItemSelected(item);
    }

    /* FaceBook */
    @Override protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }
    @Override protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }
    @Override protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBack.onActivityResult(requestCode, resultCode, data);
    }

    /* Drawer Listener */
    @Override public void onDrawerSlide(View drawerView, float slideOffset) { }
    @Override public void onDrawerStateChanged(int newState) { }
    @Override public void onDrawerOpened(View drawerView) { new Sound(getApplicationContext(), R.raw.bookpage).PlaySound(); }
    @Override public void onDrawerClosed(View drawerView) { new Sound(getApplicationContext(), R.raw.bookpage).PlaySound(); }

    /* Drawer Method */
    @Override public void onConfigurationChanged(Configuration newConfig) { mToggle.onConfigurationChanged(newConfig); super.onConfigurationChanged(newConfig); }
    @Override protected void onPostCreate(Bundle savedInstanceState) { super.onPostCreate(savedInstanceState); mToggle.syncState(); }

    /* Navigation Listener */
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        new Sound(getApplicationContext(), R.raw.openbutton).PlaySound(); /* Sound Play */

        /* MenuItem Switch */
        switch (menuItem.getItemId())
        {
            /* 사진 */
            case (R.id.Camara) : { startActivity(new Intent(getApplicationContext(), PhotoActivity.class).putExtra("Switch", 1)); return true; }
            /* 앨범 */
            case (R.id.Album) : { startActivity(new Intent(getApplicationContext(), AlbumActivity.class)); return true; }
            /* 문화 */
            case(R.id.Culture) : { startActivity(new Intent(getApplicationContext(), CultureActivity.class)); return true; }
            /* 주변 */
            case(R.id.Range) : { startActivity(new Intent(getApplicationContext(), RangeActivity.class)); return true; }
            /* 지도 */
            case(R.id.Map) : { startActivity(new Intent(getApplicationContext(), MapActivity.class)); return true; }
            /* 코스 */
            case(R.id.Local) : { startActivity(new Intent(getApplicationContext(), RoadActivity.class)); return true; }
            /* 종료 */
            case (R.id.Exit) : { System.exit(0); return true; }
        }
        return false;
    }

    /* SQLite Class */
    public static class dbHelper extends SQLiteOpenHelper
    {
        /* SQLite 관련 변수 */
        public static final String DATABASE_NAME = "locationhow.db"; /* DataBase NAME */
        public static final int DATABASE_VERSION = 1; /* DataBase VERSION */

        /* TODO Auto-generated method stub */
        public dbHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); } /* SQLiteOpenHelper 생성자 함수 */

        @Override /* TODO Auto-generated method stub */
        public void onCreate(SQLiteDatabase db)
        {
            /* photo_db Create Table */
            db.execSQL("CREATE TABLE photo_db ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "title TEXT, address TEXT, path1 TEXT, path2 TEXT, comment TEXT, latitude DOUBLE, longitude DOUBLE, rating DOUBLE, path3 TEXT, path4 TEXT);");
            /* road_db Create Table */
            db.execSQL("CREATE TABLE road_db ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "contentid TEXT, latitude DOUBLE, longitude DOUBLE, clear INTEGER, title TEXT, comment TEXT, path TEXT);");
            /* culture_db Create Table */
            db.execSQL("CREATE TABLE culture_db ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "contentid TEXT, latitude DOUBLE, longitude DOUBLE, clear INTEGER, title TEXT, comment TEXT, path TEXT);");
            /* map_db Create Table */
            db.execSQL("CREATE TABLE map_db ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "title TEXT, path TEXT, date TEXT);");
        }

        @Override /* TODO Auto-generated method stub */
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        { db.execSQL("DROP TABLE IF EXISTS locationhow.db"); /* (구 버전은 삭제 후 새 버전으로 생성) -------> */ onCreate(db); }
    }
}
