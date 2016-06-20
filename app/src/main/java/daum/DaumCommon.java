package daum;

import android.content.Context;

import com.net.location.mylocationhow.R;

import basic.GPS;

/**
 * Created by Mari on 2015-09-05.
 */
public class DaumCommon
{
    /* Create Daum URL Method */
    public static final String createDaumURL(String mCode, Context mContext, int mSwitch, int mPage)
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
                mStringBuffer.append("&radius=20000&sort=2&page="); mStringBuffer.append(mPage); break;
            }
            case (2) : /* 키워드를 이용한 검색 */
            {
                mStringBuffer = new StringBuffer("https://apis.daum.net/local/v1/search/keyword.xml?apikey="); mStringBuffer.append(APIKEY); /* API KEY */
                mStringBuffer.append("&query="); /* Query */ mStringBuffer.append(mCode);
                mStringBuffer.append("&location="); mStringBuffer.append(GPS.getLatitude()); mStringBuffer.append(","); mStringBuffer.append(GPS.getLongitude()); /* 위도와 경도 */
                mStringBuffer.append("&radius=20000&sort=2&page="); mStringBuffer.append(mPage); break;
            }
        }
        return mStringBuffer.toString();
    }
}
