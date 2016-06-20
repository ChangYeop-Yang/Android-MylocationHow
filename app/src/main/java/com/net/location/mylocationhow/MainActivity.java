package com.net.location.mylocationhow;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;

import basic.GPS;

public class MainActivity extends BaseActivity
{
    /* Context */
    private Context mContext = MainActivity.this;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkInternet();

        /* 이전 상태에서 복구가 되는 경우 프래그먼트를 새로 생성하지 않는 조건문 */
        if (findViewById(R.id.container) != null) { if (savedInstanceState != null) { return; } }

        /* ToolBar */
        mToolbar = (Toolbar) findViewById(R.id.toolbar); /* ToolBar */
        /* ToolBar Title */
        mToolbar.setTitle("홈"); /* ToolBar Title Text */
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        /* ToolBar SubTitle */
        mToolbar.setSubtitle("날씨"); /* ToolBar SubTitle Text Set */
        mToolbar.setSubtitleTextColor(Color.WHITE); /* ToolBar SubTitle Text Color */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* Drawer */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); /* Drawer */
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mToggle);

        /* NavigationView */
        mNavigationView = (NavigationView) findViewById(R.id.NavigatinoView);
        mNavigationView.setNavigationItemSelectedListener(this);

        /* TabLayout */
        mTabLayout = (TabLayout) findViewById(R.id.Tab);
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.ic_weather)); /* Weather Tab */
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.ic_location)); /* Location Tab */
        setTabSelected(mTabLayout);
    }

    /* Check GPS Method */
    private final void checkGPS()
    {
        /* LocationManager */
        final LocationManager mLocation = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if(!mLocation.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            mBuilder.setTitle("위치 서비스 설정").setMessage(getResources().getString(R.string.MainGPSCheck)).setCancelable(false)
                    .setPositiveButton("설정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); System.exit(0); }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { dialog.cancel(); System.exit(0); }
            }).show();
        }
        else
        {
            /* GPS 사용 동의 확인창 */
            AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
            mAlertDialog.setTitle("위치정보 수집 및 이용 동의").setIcon(R.drawable.ic_healing).setMessage(R.string.MainGPSComment).setPositiveButton("동의", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new GPS(mContext).startGPS(); /* GPS Start */
                    checkGPSDialog();
                }
            }).setNegativeButton("거절", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                    dialog.cancel();
                }
            }).show();
        }
    }

    /* Check Internet Method */
    private final void checkInternet()
    {
        /* ConnectivityManager */
        ConnectivityManager mConnectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfoMobile = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo mNetworkInfoWifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        /* Internet Setting Check */
        if(mNetworkInfoMobile.isConnected() || mNetworkInfoWifi.isConnected()) { checkGPS(); }
        else
        {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setTitle("데이터 네트워크 설정").setMessage("데이터 네트워크가 활성화 되어 있지 않습니다. 설정화면으로 이동하시겠습니까?")
                    .setPositiveButton("설정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); finish(); }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
            }).show();
        }
    }

    /* Check GPS */
    private void checkGPSDialog()
    {
        final ProgressDialog mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); /* Style Setting */
        mProgressDialog.setTitle("GPS와 수신중 입니다."); /* Title */
        mProgressDialog.setMessage("잠시만 기다려 주세요."); /* Message */
        mProgressDialog.setIcon(R.drawable.ic_healing); /* Icon */
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                while(GPS.getLatitude() == 0)
                {
                    try { Thread.sleep(10); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }

                /* Fragment */
                Fragment_Weather mFragmentA = new Fragment_Weather();
                mFragmentA.setArguments(getIntent().getExtras());
                getFragmentManager().beginTransaction().add(R.id.container, mFragmentA).commit();
                mProgressDialog.dismiss();
            }
        }).start();
    }

    /* Setting TabSelected Method */
    private void setTabSelected(TabLayout mTab)
    {
        mTab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                /* Fragment */
                android.app.Fragment mFragment = null;

                    /* Tab */
                switch (tab.getPosition())
                {
                    /* Weather Tab */
                    case (0) :
                    {
                        Toast.makeText(mContext, "날씨", Toast.LENGTH_SHORT).show();
                        mToolbar.setSubtitle("날씨"); /* ToolBar SubTitle Text Set */
                        mFragment = new Fragment_Weather(); break;
                    }
                    /* Location Tab */
                    case (1) :
                    {
                        Toast.makeText(mContext, "위치", Toast.LENGTH_SHORT).show();
                        mToolbar.setSubtitle("위치"); /* ToolBar SubTitle Text Set */
                        mFragment = new Fragment_Location(); break;
                    }
                }

                /* FragmentManager */
                FragmentManager mFragmentManager = getFragmentManager();
                FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.container, mFragment);
                mFragmentTransaction.addToBackStack(null);
                mFragmentTransaction.commit();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /* Optinon 관련 메소드 */
    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.menu_main, menu); return true; }
}