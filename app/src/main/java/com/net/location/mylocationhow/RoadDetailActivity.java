package com.net.location.mylocationhow;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import basic.CustomDialog.StampDialog;
import basic.GPS;
import travel.TourImageXmlParser;
import travel.TourLocalXmlParser;

/**
 * Created by Mari on 2015-09-26.
 */
public class RoadDetailActivity extends BaseActivity
{
    /* Context */
    private Context mContext = RoadDetailActivity.this;

    /* ArrayList */
    private ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>(10);

    /* ImageView */
    private ImageView mImageView = null;

    /* TextView */
    private TextView mTextView[] = null;

    /* Button */
    private Button mButton[] = null;

    /* int */
    private int mPage[] = {0, 1};
    private static final int CAMERA_CAPTURE = 100;

    /* String */
    private String mCode = null;

    /* Double */
    private double mMapX = 0.0;
    private double mMapY = 0.0;

    /* LayoutInflater */
    private LayoutInflater mLayout = null;

    /* View */
    private View mView = null;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_detail);

        /* ToolBar */
        mToolbar = (Toolbar) findViewById(R.id.RoadToolbar); /* ToolBar */
        /* ToolBar Title */
        mToolbar.setTitle("상세정보"); /* ToolBar Title Text */
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        mToolbar.setNavigationIcon(R.drawable.ic_arrow); /* Icon Setting */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* LayoutInflater */
        mLayout = getLayoutInflater();

        /* View */
        mView = mLayout.inflate(R.layout.custom_dialog, null);

        /* Button */
        mButton = new Button[]{(Button)findViewById(R.id.RoadProveButton), (Button)findViewById(R.id.RoadNextButton), (Button)findViewById(R.id.RoadMakerButton)};
        for(int count = 0, mLength = mButton.length; count < mLength; count++) { setButton(mButton[count], count); }

        /* Intent */
        Intent mIntent = getIntent();
        getIntent(mIntent);
    }

    /* getIntent Method */
    private void getIntent(Intent mIntent)
    {
        /* String */
        final String mTag[][] = { {"overview"}, {"subdetailimg", "subdetailoverview", "subname"} };
        final String mTitle = mIntent.getStringExtra("Title");
        mCode = mIntent.getStringExtra("ID");
        String mURLString[] = new String[2];

        /* double */
        mMapX = mIntent.getDoubleExtra("MapX", 0.0);
        mMapY = mIntent.getDoubleExtra("MapY", 0.0);

        /* Toolbar */
        mToolbar.setSubtitle(mTitle);

         /* 공통정보 */
        mURLString[0] = createTourURL(mCode, 0);
        new TourLocalXmlParser(mContext, mURLString[0], mTourHeadTag, mTag[0], (TextView)findViewById(R.id.RoadOverViewTextView)).execute();

        try
        {
            /* 코스정보 */
            mURLString[1] = createTourURL(mCode, 1);
            mList = new TourImageXmlParser(mContext, mTourHeadTag, mTag[1], createTourURL(mCode, 1)).execute().get();
            mPage[1] = mList.size() - 1;
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }

         /* TextView */
        mTextView = new TextView[]{(TextView)findViewById(R.id.RoadInformationTextView), (TextView)findViewById(R.id.RoadDetailTextView), (TextView)findViewById(R.id.RoadTitleTextView)};
        if(mList.get(0).containsKey("subname")) { mTextView[0].setText(Html.fromHtml(mList.get(0).get("subname"))); } else { mTextView[0].setText("정보없음."); }
        if(mList.get(0).containsKey("subdetailoverview")) { mTextView[1].setText(Html.fromHtml(mList.get(0).get("subdetailoverview"))); } else { mTextView[1].setText("정보없음."); }
        mTextView[2].setText(mTitle);

        /* ImageView */
        mImageView = (ImageView)findViewById(R.id.RoadInformationImageView);
        Glide.with(mContext).load(mList.get(0).get("subdetailimg")).error(R.drawable.ic_nopage).override(500, 250).into(mImageView);
        Glide.with(mContext).load(createMapURL(mMapX, mMapY)).error(R.drawable.ic_nopage).into((ImageView) findViewById(R.id.RoadLocationImageView));

        /* SQL */
        checkSQL();
    }

    /* CreateTourURL Method */
    private String createTourURL(String mCode, int mSwitch)
    {
        /* StringBuffer */
        StringBuffer mStringBuffer = null;

        switch (mSwitch)
        {
            case (0) :
            {
                mStringBuffer = new StringBuffer("http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailCommon?ServiceKey="); mStringBuffer.append(getResources().getString(R.string.TOUR_APIKEY));
                mStringBuffer.append("&contentTypeId=25&contentId="); mStringBuffer.append(mCode); /* ContentID */
                mStringBuffer.append("&MobileOS=AND&MobileApp="); mStringBuffer.append(getResources().getString(R.string.app_name)); /* APP NAME */
                mStringBuffer.append("&overviewYN=Y"); break;
            }
            case (1) :
            {
                mStringBuffer = new StringBuffer("http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailInfo?ServiceKey="); mStringBuffer.append(getResources().getString(R.string.TOUR_APIKEY));
                mStringBuffer.append("&contentTypeId=25&contentId="); mStringBuffer.append(mCode); /* Content ID */
                mStringBuffer.append("&MobileOS=AND&MobileApp="); mStringBuffer.append(getResources().getString(R.string.app_name)); /* APP NAME */
                mStringBuffer.append("&listYN=Y"); break;
            }
        } return mStringBuffer.toString();
    }

    /* CreateMapURL Method */
    private String createMapURL(double mMapX, double mMapY)
    {
        /* StringBuffer */
        StringBuffer mStringBuffer = new StringBuffer("https://maps.googleapis.com/maps/api/staticmap?center=");
        mStringBuffer.append(mMapX); mStringBuffer.append(","); mStringBuffer.append(mMapY); /* 위도와 경도 */
        mStringBuffer.append("&markers=color:blue|label:R|"); mStringBuffer.append(mMapX); mStringBuffer.append(","); mStringBuffer.append(mMapY);
        mStringBuffer.append("%7C11211&sensor=false&zoom=15&size="); mStringBuffer.append(300); mStringBuffer.append("x"); mStringBuffer.append(150);
        mStringBuffer.append("&key="); mStringBuffer.append(mContext.getResources().getString(R.string.GOOGLE_APIKEY)); /* APIKEY */

        return  mStringBuffer.toString();
    }

    /* setButton Method */
    private void setButton(Button mButton, final int mSwitch)
    {
        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (mSwitch)
                {
                    /* 이전 */
                    case (0) :
                    {
                        if(--mPage[0] < 0) { Toast.makeText(mContext, "첫 코스입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = 0; }
                        else
                        {
                            /* TextView */
                            if(mList.get(mPage[0]).containsKey("subname")) { mTextView[0].setText(mList.get(mPage[0]).get("subname")); } else { mTextView[0].setText("정보없음."); }
                            if(mList.get(mPage[0]).containsKey("subdetailoverview")) { mTextView[1].setText(Html.fromHtml(mList.get(mPage[0]).get("subdetailoverview"))); } else { mTextView[1].setText("정보없음."); }

                            /* ImageView */
                            Glide.with(mContext).load(mList.get(mPage[0]).get("subdetailimg")).error(R.drawable.ic_nopage).override(500, 250).into(mImageView);
                        }  break;
                    }
                    /* 다음 */
                    case (1) :
                    {
                        if(++mPage[0] >= mPage[1]) { Toast.makeText(mContext, "마지막 코스입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = mPage[1] - 1; }
                        else
                        {
                            /* TextView */
                            if(mList.get(mPage[0]).containsKey("subname")) { mTextView[0].setText(mList.get(mPage[0]).get("subname")); } else { mTextView[0].setText("정보없음."); }
                            if(mList.get(mPage[0]).containsKey("subdetailoverview")) { mTextView[1].setText(Html.fromHtml(mList.get(mPage[0]).get("subdetailoverview"))); } else { mTextView[1].setText("정보없음."); }

                            /* ImageView */
                            Glide.with(mContext).load(mList.get(mPage[0]).get("subdetailimg")).error(R.drawable.ic_nopage).override(500, 250).into(mImageView);
                        }   break;
                    }
                    /* 발도장 */
                    case (2) :
                    {
                        if(GPS.calcDistance(mMapX, mMapY) > 500) { Toast.makeText(mContext, "주변 위치에서 발도장을 눌러주세요.", Toast.LENGTH_SHORT).show(); }
                        else
                        {
                            /* DialogFragment */
                            final DialogFragment mFragment = StampDialog.newInstance();
                            /* Bundle */
                            Bundle mBundle = new Bundle();
                            mBundle.putBoolean("Switch", true);
                            mBundle.putString("DataBaseName", mDataBaseTable[3]);
                            mBundle.putString("ContentID", mCode);
                            mBundle.putDouble("MapX", mMapX);
                            mBundle.putDouble("MapY", mMapY);
                            mFragment.setArguments(mBundle);
                            mFragment.show(getFragmentManager(), "WriteDialog");
                        } break;
                    }
                }
            }
        });
    }

    /* CheckSQL Method */
    private void checkSQL()
    {
        /* Cursor */
        final Cursor mCursor = mDataBase.rawQuery("SELECT * FROM road_db WHERE contentid = '" + mCode + "'", null);

        try
        {
            if(mCursor.moveToFirst())
            {
                /* Button */
                mButton[2].setEnabled(false);
                mButton[2].setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_foot_green), null, null, null);

                /* DialogFragment */
                final DialogFragment mFragment = StampDialog.newInstance();
                /* Bundle */
                final Bundle mBundle = new Bundle();
                mBundle.putBoolean("Switch", false);
                mBundle.putString("DataBaseName", mDataBaseTable[3]);
                mBundle.putInt("ID", mCursor.getInt(0));
                mBundle.putStringArray("StringArray", new String[]{mCursor.getString(5), mCursor.getString(6), mCursor.getString(7)});
                mFragment.setArguments(mBundle);
                mFragment.show(getFragmentManager(), "ReadDialog");
            }
        }
        catch (SQLiteException ex) { Toast.makeText(mContext, "SQLite 불러오는 과정에서 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show(); ex.printStackTrace(); }
        finally { mCursor.close(); }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == CAMERA_CAPTURE && resultCode == RESULT_OK)
        {
            /* ImageView */
            final ImageView mImageView = (ImageView)mView.findViewById(R.id.CustomDialogImageView);
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mView.getContext()).load(data.getData()).into(mImageView);
        }
    }
}
