package travel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mari on 2015-09-30.
 */
public class TourImageXmlParser extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>>
{
    /* ProgressDialog */
    private ProgressDialog mDialog = null;

    /* Context */
    private Context mContext = null;

    /* ArrayList */
    private ArrayList<HashMap<String, String>> mHashList = null;

    /* HashMap */
    private HashMap<String, String> mHash = null;

    /* String */
    private String mURL = null;
    private String mHeadTag = null;
    private String mTag[] = null;

    /* Integer */
    private int mTagLength = 0;

    public TourImageXmlParser(Context mContext, String mHeadTag, String[] mTag, String mURL)
    {
        this.mContext = mContext;
        this.mHeadTag = mHeadTag;
        this.mURL = mURL;
        this.mTag = mTag;
    }

    @Override
    protected void onPreExecute()
    {
		/* Dialog 설정 구문 */
        mDialog = new ProgressDialog(mContext);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); /* 원형 프로그래스 다이얼 로그 스타일로 설정 */
        mDialog.setMessage("잠시만 기다려주세요.");
        mDialog.show();

        /* Length */
        mTagLength = mTag.length; /* 배열 길이 저장 */

        /* HashMap */
        mHash = new HashMap<String, String>(10);

        /* ArrayList */
        mHashList = new ArrayList<HashMap<String, String>>(10);
    }

    @Override
    protected ArrayList<HashMap<String, String>> doInBackground(Void... params)
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
                { for(int count = 0; count < mTagLength; count++) { if(tagName.equals(mTag[count])) { mHash.put(mTag[count], parser.getText()); } }}
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

        return mHashList;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, String>> result)
    {
        mDialog.dismiss(); /* Dialog 종료 */
        super.onPostExecute(result);
    }
}
