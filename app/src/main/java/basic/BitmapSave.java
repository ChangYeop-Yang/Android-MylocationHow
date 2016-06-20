package basic;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Mari on 2015-08-31.
 */
public class BitmapSave extends AsyncTask<Void, Void, String>
{
    /* Context */
    private Context mContext = null;

    /* ProgressDialog */
    private ProgressDialog mProgressDialog = null;

    /* Bitmap */
    private Bitmap mBitmap[] = new Bitmap[2];

    /* String */
    private String mFileName = null;
    private String mFileException = null;

    /* StringBuffer */
    private StringBuffer mURL = new StringBuffer("sdcard/MyLocationHow/");

    /* int */
    private int mSwitch = 0;

    /* BitmapSave */
    public BitmapSave(Context mContext, Bitmap mBitmap[], String mURI, String mFileName)
    {
        this.mContext = mContext;
        this.mBitmap = mBitmap;
        this.mURL.append(mURI).append("/");
        this.mFileName = mFileName;
        mSwitch = 1;
    }
    public BitmapSave(Context mContext, ImageView mImageView, String mURI, String mFileName)
    {
        this.mContext = mContext;
        this.mBitmap[0] = createConvertBitmap(mImageView);
        this.mURL.append(mURI).append("/");
        this.mFileName = mFileName;
        mSwitch = 2;
    }

    @Override /* 프로세스가 실행되기 전에 실행 되는 부분 - 초기 설정 부분 */
    protected void onPreExecute()
    {
        /* Dialog 설정 구문 */
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); /* 원형 프로그래스 다이얼 로그 스타일로 설정 */
        mProgressDialog.setMessage("잠시만 기다려 주세요.");
        mProgressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params)
    {
        /* File */
        File mFile[] = { (new File(mURL.toString())), null };

        /* SD CARD Check */
        if(isExternalStorageCheck() == false) { Toast.makeText(mContext, "외장메모리 읽기/쓰기가 불가능합니다.", Toast.LENGTH_SHORT).show(); }
        else
        {
            /* SD CARD Path Check */
            if (!mFile[0].exists()) { mFile[0].mkdirs(); }

            mFile[1] = new File(mURL.append(mFileName).append(".JPG").toString());
            mFile[1].getAbsolutePath();

            try
            {
                /* OutPutStream */
                OutputStream mOutput = null;

                mFile[1].createNewFile();
                mOutput = new FileOutputStream(mFile[1]);

                if(mSwitch == 1) { combineImage(mBitmap[0], mBitmap[1], true).compress(Bitmap.CompressFormat.JPEG, 100, mOutput); }
                else { mBitmap[0].compress(Bitmap.CompressFormat.JPEG, 100, mOutput); }

                /* Close */
                mOutput.close();
            }
            catch (FileNotFoundException e)
            {
                Toast.makeText(mContext, "파일 생성 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                Log.e("BitmapSave", "FileNotFoundException" + e.getMessage());
                e.printStackTrace();
            }
            catch (IOException e)
            {
                Toast.makeText(mContext, "입/출력 장치에 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                Log.e("BitmapSave", "IOException" + e.getMessage());
                e.printStackTrace();
            }
        }
        return mFile[1].getPath().toString();
    }

    @Override
    protected void onPostExecute(String result)
    {
        Toast.makeText(mContext, "정상적으로 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
        mProgressDialog.dismiss();
        super.onPostExecute(result);
    }

    /* 2개 이상의 Bitmap을 합쳐 주는 함수  */
    @SuppressWarnings("deprecation")
    private final Bitmap combineImage(Bitmap first, Bitmap secod, boolean isVerticalMode)
    {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inDither  = true;
        option.inPurgeable = true;

        Bitmap bitmap = null;
        if(isVerticalMode) { bitmap = Bitmap.createScaledBitmap(first, first.getWidth(), first.getHeight()+secod.getHeight(), true); }
        else { bitmap = Bitmap.createScaledBitmap(first, first.getWidth()+secod.getWidth(), first.getHeight(), true); }

			/* isVerticalMode 값이 true 일 경우 세로, isVerticalMode 값이 false 일 경우 가로 */
        Paint p = new Paint();
        p.setDither(true);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(bitmap);
        c.drawBitmap(first, 0, 0, null);
        if(isVerticalMode) { c.drawBitmap(secod, 0, first.getHeight(), null); }
        else { c.drawBitmap(secod, first.getWidth(), 0, null); }

        first.recycle(); secod.recycle();

        return bitmap;
    }

    /* External Storage Check */
    private boolean isExternalStorageCheck()
    {
        String mState = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(mState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mState)) { return true; }
        else { return false; }
    }

    /* Create Bitmap Convert Method */
    private Bitmap createConvertBitmap(ImageView mImageView)
    {
        /* Bitmap */
        Bitmap mBitmap = null;

        mImageView.buildDrawingCache(true);
        mBitmap = Bitmap.createBitmap(mImageView.getMeasuredWidth(), mImageView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas mCanvas = new Canvas(mBitmap);
        mImageView.draw(mCanvas);
        mImageView.buildDrawingCache(false);
        return mBitmap;
    }
}
