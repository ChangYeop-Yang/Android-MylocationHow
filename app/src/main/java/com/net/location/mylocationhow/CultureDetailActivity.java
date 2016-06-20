package com.net.location.mylocationhow;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.kakao.AppActionBuilder;
import com.kakao.KakaoLink;
import com.kakao.KakaoParameterException;
import com.kakao.KakaoTalkLinkMessageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import basic.BitmapSave;
import basic.CustomDialog.StampDialog;
import basic.GPS;
import basic.Vibrate;
import daum.DaumLocalXmlParser;
import travel.TourImageXmlParser;
import travel.TourLocalXmlParser;

/**
 * Created by Mari on 2015-09-10.
 */
public class CultureDetailActivity extends BaseActivity
{
    /* Context */
    private Context mContext = CultureDetailActivity.this;

    /* String */
    private String mDataString[] = {"Title", "Tel", "Address", "IMG", "ID"};
    private final String mTourTag[] = {"overview","homepage", "totalCount" };

    /* Double */
    private double[] mDouble = new double[2];

    /* Int */
    private int mPage[] = {0, 1};
    private int mDataStringLength = 0;

    /* TextView */
    private TextView mTextView[] = null;

    /* ArrayList */
    private ArrayList<HashMap<String, String>> mImageArr = new ArrayList<HashMap<String, String>>(10);

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culture_detail);

        /* Intent */
        Intent mIntent = getIntent();
        /* Double Intent Vlaue */
        mDouble[0] = mIntent.getDoubleExtra("MapX", 0); /* 위도 값 */
        mDouble[1] = mIntent.getDoubleExtra("MapY", 0); /* 경도 값 */
        /* String Intent Value */
        mDataStringLength = mDataString.length;
        for(int count = 0; count < mDataStringLength; count++) { mDataString[count] = mIntent.getStringExtra(mDataString[count]); }

        /* Move Layout */
        try
        {
            /* String */
            final String mImageTag[] = {"originimgurl"};
            mImageArr = new TourImageXmlParser(mContext, mTourHeadTag, mImageTag, createDetailTourURL(mDataString[(mDataStringLength - 1)], 1)).execute().get();

            /* 추가 이미지 관련 조건문 */
            if( (mPage[1] = mImageArr.size() - 1) > 0)
            {
                /* LinearLayout */
                LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.DetailMoveLayout);
                mLinearLayout.setVisibility(View.VISIBLE);

                /* Button */
                setButton(0, (Button)findViewById(R.id.DetailProveButton)); /* Prove Button Setting */
                setButton(1, (Button)findViewById(R.id.DetailNextButton)); /* Netx Button Setting */
            }
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }

        /* ToolBar Title */
        final Toolbar mToolbar = (Toolbar) findViewById(R.id.DetailToolbar); /* ToolBar */
        mToolbar.setTitle("상세정보"); /* ToolBar Title Text */
        mToolbar.setSubtitle(mDataString[0]); /* ToolBar SubTitle Setting */
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        mToolbar.setNavigationIcon(R.drawable.ic_arrow); /* Icon Setting */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* ImageView */
        final ImageView mImageView[] = new ImageView[]{(ImageView)findViewById(R.id.DetailLocationWebView), (ImageView)findViewById(R.id.DetailRangeFoodWebView), (ImageView)findViewById(R.id.DetailRangeHotelWebView)};
        /* ImageView Setting */
        int mImageLength = mImageView.length;
        for(int count = 0; count < mImageLength; count++) { setImageView(count, mImageView[count]); }

        /* TextView */
        mTextView = new TextView[]{(TextView)findViewById(R.id.DetailTitleText), (TextView)findViewById(R.id.DetailAddressText), (TextView)findViewById(R.id.DetailWebView)};
        mTextView[0].setText(mDataString[0]); mTextView[1].setText(mDataString[2]);

        /* OverView Text */
        new TourLocalXmlParser(mContext, createDetailTourURL(mDataString[mDataStringLength - 1], 0), mTourHeadTag, mTourTag, mTextView[2]).execute();

        /* ImageView Setting */
        Glide.with(mContext).load(mDataString[3]).override(500, 333).error(R.drawable.ic_nopage).into((ImageView) findViewById(R.id.DetailImageView)); /* Main ImageView */

        /* Button */
        Button mButton[] = {(Button)findViewById(R.id.DetailTelButton), (Button)findViewById(R.id.DetailLocationButton), (Button)findViewById(R.id.DetailKakaoBut), (Button)findViewById(R.id.DetailFacebookBut), (Button)findViewById(R.id.DetailFootButton)};
        for(int count = 0, mLength = mButton.length; count < mLength; count++) { setButton(count + 2, mButton[count]); }

        /* SQLite */
        setSQLite();
    }

    /* Menu Method */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.menu_detail, menu); return true; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();

        /* ActionBar Item */
        switch (item.getItemId())
        {
            case (R.id.DetailPhoto) :
            {
                /* LinearLayout */
                LinearLayout mLinear[] = {(LinearLayout)findViewById(R.id.DetailTopLayout), (LinearLayout)findViewById(R.id.DetailBottomLayout)};

                /* Save Image */
                new BitmapSave(mContext, drawLayout(mLinear), "Culture", mDataString[0]).execute();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /* Drawing Layout Method */
    private Bitmap[] drawLayout(LinearLayout mLayout[])
    {
        /* Bitmap */
        Bitmap mBitmap[] = {null, null};

        /* Array Length */
        int mLength = mLayout.length;
        for(int count = 0; count < mLength; count++)
        {
            mLayout[count].buildDrawingCache(true);
            mBitmap[count] = Bitmap.createBitmap(mLayout[count].getMeasuredWidth(), mLayout[count].getMeasuredHeight(), Bitmap.Config.ARGB_8888);

            Canvas mCanvas = new Canvas(mBitmap[count]);
            mLayout[count].draw(mCanvas);
        } return mBitmap;
    }

    /* Setting ImageView Method */
    private void setImageView(int mSwitch, ImageView mImageView)
    {
        /* String */
        final String mDaumTag[] = {"title", "latitude", "longitude", "phone", "address", "distance", "placeUrl", "totalCount"};

        /* StringBuffer */
        StringBuffer mStringBuffer = null;

        switch (mSwitch)
        {
            /* 현재 위치 */
            case (0) :
            {
                mStringBuffer = new StringBuffer("https://maps.googleapis.com/maps/api/staticmap?center=");
                mStringBuffer.append(mDouble[0]); mStringBuffer.append(","); mStringBuffer.append(mDouble[1]); /* 위도와 경도 */
                mStringBuffer.append("&markers=color:blue|label:R|"); mStringBuffer.append(mDouble[0]); mStringBuffer.append(","); mStringBuffer.append(mDouble[1]);
                mStringBuffer.append("%7C11211&sensor=false&zoom=15&size=200x200&key="); mStringBuffer.append(getResources().getString(R.string.GOOGLE_APIKEY)); /* APIKEY */

                Glide.with(mContext).load(mStringBuffer.toString()).override(200, 200).into(mImageView); /* Now Location ImageView */
                break;
            }
            /* 주변 음식점 */
            case (1) :
            {
                mStringBuffer = new StringBuffer("https://apis.daum.net/local/v1/search/category.xml?apikey="); mStringBuffer.append(getResources().getString(R.string.DAUM_LOCALAPIKEY)); /* API KEY */
                mStringBuffer.append("&code="); mStringBuffer.append("FD6"); /* Code */
                mStringBuffer.append("&location="); mStringBuffer.append(mDouble[0]); mStringBuffer.append(","); mStringBuffer.append(mDouble[1]); /* 위도와 경도 */
                mStringBuffer.append("&radius=20000&sort=2&page="); mStringBuffer.append(1);

                new DaumLocalXmlParser(mContext, mStringBuffer.toString(), mDaumHeadTag, mDaumTag, "FD6", mDouble, mImageView).execute();
                break;
            }
            /* 주변 숙소 */
            case (2) :
            {
                mStringBuffer = new StringBuffer("https://apis.daum.net/local/v1/search/category.xml?apikey="); mStringBuffer.append(getResources().getString(R.string.DAUM_LOCALAPIKEY)); /* API KEY */
                mStringBuffer.append("&code="); mStringBuffer.append("AD5"); /* Code */
                mStringBuffer.append("&location="); mStringBuffer.append(mDouble[0]); mStringBuffer.append(","); mStringBuffer.append(mDouble[1]); /* 위도와 경도 */
                mStringBuffer.append("&radius=20000&sort=2&page="); mStringBuffer.append(1);

                new DaumLocalXmlParser(mContext, mStringBuffer.toString(), mDaumHeadTag, mDaumTag, "AD5", mDouble, mImageView).execute();
                break;
            }
        }
    }

    /* Setting Button Method */
    private void setButton(final int mSwitch, Button mButton)
    {
        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Vibrate.Vibrator(mContext); /* 진동 */
                switch (mSwitch)
                {
                    /* Prove Button */
                    case (0) :
                    {
                        if(--mPage[0] < 0) { mPage[0] = 0; Toast.makeText(mContext, "첫 사진 입니다.", Toast.LENGTH_SHORT).show(); }
                        else { Glide.with(mContext).load(mImageArr.get(mPage[0]).get("originimgurl")).override(500, 333).into((ImageView) findViewById(R.id.DetailImageView)); } break;
                    }
                    /* Next Button */
                    case (1) :
                    {
                        if(++mPage[0] >= mPage[1]) { mPage[0] = mPage[1] - 1; Toast.makeText(mContext, "마지막 사진 입니다.", Toast.LENGTH_SHORT).show(); }
                        else { Glide.with(mContext).load(mImageArr.get(mPage[0]).get("originimgurl")).override(500, 333).into((ImageView) findViewById(R.id.DetailImageView)); } break;
                    }
                    /* Call Button */
                    case (2) :
                    {
                        if (mDataString[1] != null) { mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + mDataString[1]))); }
                        else { Toast.makeText(mContext, "전화번호를 제공하지 않습니다.", Toast.LENGTH_SHORT).show(); } break;
                    }
                    /* Place Button */
                    case (3) :
                    {
                        /* Map Intent Move */
                        Intent mIntent = new Intent(mContext, MapActivity.class);
                        mIntent.putExtra("Switch", 1);
                        mIntent.putExtra("MapX", mDouble[0]); /* 위도 */
                        mIntent.putExtra("MapY", mDouble[1]); /* 경도 */
                        mIntent.putExtra("Title", mDataString[0]); /* 상호명 */
                        mContext.startActivity(mIntent); break;
                    }
                    /* KakaoTalkLink Button */
                    case (4) :
                    {
                        try
                        {
                            /* KakaoLink */
                            final KakaoLink mKakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
                            final KakaoTalkLinkMessageBuilder mKakaoBuilder = mKakaoLink.createKakaoTalkLinkMessageBuilder();

                            /* KakaoTalk Message */
                            mKakaoBuilder.addText(String.format("%s\n주소 : %s\n%s", mDataString[0], mDataString[2], mTextView[2].getText().toString())); /* Culture Text */
                            mKakaoBuilder.addImage(mDataString[3], 200, 200); /* Culture Image */
                            mKakaoLink.sendMessage(mKakaoBuilder.build(), mContext); /* 메세지 전송 */
                        }
                        catch (KakaoParameterException e)
                        {
                            Toast.makeText(mContext, "KakaoTalk 메세지 전송 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        } break;
                    }
                    /* Facebook Button */
                    case (5) :
                    {
                        if(ShareDialog.canShow(ShareLinkContent.class))
                        {
                            /* FaceBook Share */
                            ShareLinkContent mShare = new ShareLinkContent.Builder()
                                    .setContentTitle(mDataString[0]) /* 관광지명 */
                                    .setContentDescription(mTextView[2].getText().toString()) /* 관광지 상세설명 */
                                    .setImageUrl(Uri.parse(mDataString[3])).build(); /* 관광지 사진 */
                            mShareDialog.show(mShare);
                        } break;
                    }
                    /* Foot Button */
                    case (6) :
                    {
                        if(GPS.calcDistance(mDouble[0], mDouble[1]) > 500) { Toast.makeText(mContext, "주변 위치에서 발도장을 눌러주세요.", Toast.LENGTH_SHORT).show(); }
                        else
                        {
                            /* DialogFragment */
                            final DialogFragment mFragment = StampDialog.newInstance();
                            /* Bundle */
                            Bundle mBundle = new Bundle();
                            mBundle.putBoolean("Switch", true);
                            mBundle.putString("DataBaseName", mDataBaseTable[3]);
                            mBundle.putString("ContentID", mDataString[4]);
                            mBundle.putDouble("MapX", mDouble[0]);
                            mBundle.putDouble("MapY", mDouble[1]);
                            mFragment.setArguments(mBundle);
                            mFragment.show(getFragmentManager(), "WriteDialog");
                        } break;
                    }
                }
            }
        });
    }

    /* CreateDetailTourURL Method */
    private String createDetailTourURL(String mCode, int mSwitch)
    {
        StringBuffer mStringBuffer = null;

        switch (mSwitch)
        {
            case (0) :
            {
                /* StringBuffer 관련 구문 */
                mStringBuffer = new StringBuffer("http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailCommon?ServiceKey="); mStringBuffer.append(getResources().getString(R.string.TOUR_APIKEY));
                mStringBuffer.append("&contentId="); mStringBuffer.append(mCode);
                mStringBuffer.append("&MobileOS=AND&MobileApp="); mStringBuffer.append(getResources().getString(R.string.app_name)); mStringBuffer.append("&overviewYN=Y"); break;
            }
            case (1) : /* 이미지 정보 조회 */
            {
                /* StringBuffer 관련 구문 */
                mStringBuffer = new StringBuffer("http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailImage?ServiceKey="); mStringBuffer.append(getResources().getString(R.string.TOUR_APIKEY)); /* API KEY */
                mStringBuffer.append("&contentId="); mStringBuffer.append(mCode); mStringBuffer.append("&imageYN=Y&MobileOS=AND&numOfRows=15&MobileApp="); mStringBuffer.append(getResources().getString(R.string.app_name)); break;
            }
        } return mStringBuffer.toString();
    }

    /* Setting SQLite Method */
    private void setSQLite()
    {
        /* Cursor */
        final Cursor mCursor = mDataBase.rawQuery("SELECT * FROM culture_db WHERE contentid = '" + mDataString[4] + "'", null);

        try
        {
            if(mCursor.moveToFirst())
            {
                /* Button */
                final Button mFootButton = (Button)findViewById(R.id.DetailFootButton);
                mFootButton.setEnabled(false); mFootButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_foot_green), null, null, null);

                /* DialogFragment */
                DialogFragment mFragment = StampDialog.newInstance();
                /* Bundle */
                Bundle mBundle = new Bundle();
                mBundle.putBoolean("Switch", false);
                mBundle.putString("DataBaseName", mDataBaseTable[3]);
                mBundle.putInt("ID", mCursor.getInt(0));
                mBundle.putStringArray("StringArray", new String[]{mCursor.getString(5), mCursor.getString(6), mCursor.getString(7)});
                mFragment.setArguments(mBundle);
                mFragment.show(getFragmentManager(), "ReadDialog");
            }
        }
        catch (SQLiteException ex)
        {
            Log.e("SQLiteException", ex.getMessage()); ex.printStackTrace();
            Toast.makeText(mContext, "SQLite 불러오는 과정에서 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
        } finally { mCursor.close(); }
    }
}
