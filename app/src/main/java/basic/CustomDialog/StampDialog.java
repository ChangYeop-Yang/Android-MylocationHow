package basic.CustomDialog;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.net.location.mylocationhow.BaseActivity;
import com.net.location.mylocationhow.R;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import basic.BitmapSave;

public class StampDialog extends DialogFragment
{
    /* AppComPat EditText */
    private AppCompatEditText mTitleEditText = null;
    private AppCompatEditText mCommentEditText = null;

    /* ImageView */
    private ImageView mImageView = null;

    /* Integer */
    private static final int CAMERA_CAPTURE = 100;

    /* String */
    private String mDataBaseName = null;

    /* Context */
    private Context mContext = null;

    /* View */
    private View mView = null;

    /* Instance */
    public static StampDialog newInstance() { return new StampDialog(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        /* View */
        mView = inflater.inflate(R.layout.custom_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        /* Context */
        mContext = mView.getContext();

        /* AppComPat EditText */
        mTitleEditText = (AppCompatEditText)mView.findViewById(R.id.CustomDialogTitleEditText);
        mCommentEditText = (AppCompatEditText)mView.findViewById(R.id.CustomDialogCommentEditText);

        /* ImageView */
        mImageView = (ImageView)mView.findViewById(R.id.CustomDialogImageView);

        /* Bundle */
        Bundle mBundle = getArguments();
        getBundle(mBundle.getBoolean("Switch"), mBundle);

        return mView;
    }

    /* getBundle Method */
    private void getBundle(Boolean mBoolean, final Bundle mBundle)
    {
        /* String */
        mDataBaseName = mBundle.getString("DataBaseName");

        /* Write Mode */
        if(mBoolean)
        {
            /* LinearLayout */
            ((LinearLayout) mView.findViewById(R.id.CustomDialogWriteLayout)).setVisibility(View.VISIBLE);

            /* ImageView */
            setImageView(mImageView);

            /* Button */
            ((Button) mView.findViewById(R.id.CustomDialogSaveButton)).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    /* String */
                    final String mString[] = { mTitleEditText.getText().toString(), mCommentEditText.getText().toString() };
                    if(mString[0].equals("")) { Toast.makeText(mContext, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show(); return; }
                    if(mString[1].equals("")) { Toast.makeText(mContext, "설명을 입력해주세요.", Toast.LENGTH_SHORT).show(); return; }

                    /* Bitmap */
                    String mPath = null;
                    try { mPath = new BitmapSave(mContext, mImageView, "Photo/stamp/", String.format("%s%s", createDate(), mString[0])).execute().get(); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                    catch (ExecutionException e) { e.printStackTrace(); }

                    /* SQLite */
                    try
                    {
                        BaseActivity.dbHelper mHelper = new BaseActivity.dbHelper(mContext);
                        SQLiteDatabase mDataBase = mHelper.getWritableDatabase();
                        Log.e("SQLite Pass.", mHelper.getDatabaseName());

                        /* ContentValues */
                        ContentValues mValues = new ContentValues();
                        mValues.put("contentid", mBundle.getString("ContentID"));
                        mValues.put("latitude", mBundle.getDouble("MapX"));
                        mValues.put("longitude", mBundle.getDouble("MapY"));
                        mValues.put("clear", 1);
                        mValues.put("title", mString[0]);
                        mValues.put("comment", mString[1]);
                        mValues.put("path", mPath);
                        mDataBase.insert(mDataBaseName, null, mValues);
                    }
                    catch(SQLiteException ex)
                    {
                        Toast.makeText(mContext, "SQLite 문제가 발생 하였습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("SQLiteException", ex.getMessage()); ex.printStackTrace();
                    } finally { dismiss(); }
                }
            });
            ((Button)mView.findViewById(R.id.CustomDialogCancelButton)).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                { dismiss(); }
            });
        }
        /* Read Mode */
        else
        {
            /* LinearLayout */
            ((LinearLayout) mView.findViewById(R.id.CustomDialogReadLayout)).setVisibility(View.VISIBLE);

            /* String */
            final String mBundleString[] = mBundle.getStringArray("StringArray");

            /* AppComPat EditText */
            mTitleEditText.setText(mBundleString[0]); mTitleEditText.setFocusableInTouchMode(false);
            mCommentEditText.setText(mBundleString[1]); mCommentEditText.setFocusableInTouchMode(false);

            /* ImageView */
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mContext).load(mBundleString[2]).error(R.drawable.ic_nopage).into(mImageView);

            /* Button */
            ((Button) mView.findViewById(R.id.CustomDialogDeleteButton)).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try
                    {
                        BaseActivity.dbHelper mHelper = new BaseActivity.dbHelper(mContext);
                        SQLiteDatabase mDataBase = mHelper.getWritableDatabase();
                        mDataBase.execSQL("DELETE FROM " + mDataBaseName + " WHERE _id = '" + mBundle.getInt("ID") + "'");

                        /* File */
                        new File(mBundleString[2]).delete();
                    }
                    catch(SQLiteException ex)
                    {
                        Toast.makeText(mContext, "SQLite 문제가 발생 하였습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("SQLiteException", ex.getMessage()); ex.printStackTrace();
                    } finally { dismiss(); }
                }
            });
            ((Button) mView.findViewById(R.id.CustomDialogShareButton)).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    /* Intent */
                    Intent mShare = new Intent();
                    mShare.setType("image/*");
                    mShare.setAction(Intent.ACTION_SEND);
                    mShare.putExtra(Intent.EXTRA_TITLE, mBundleString[0]);
                    mShare.putExtra(Intent.EXTRA_TEXT, mBundleString[1]);
                    mShare.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mBundleString[2])));
                    mShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(mShare, "공유하기"));
                }
            });
        }
    }

    /* Create Date Method */
    private String createDate()
    {
        /* Calendar */
        Calendar mCalendar = Calendar.getInstance();
        return String.format("%d%d%d%d%d%d", mCalendar.get(mCalendar.YEAR), mCalendar.get(mCalendar.MONTH), mCalendar.get(mCalendar.DAY_OF_MONTH), mCalendar.get(mCalendar.HOUR), mCalendar.get(mCalendar.MINUTE), mCalendar.get(mCalendar.SECOND));
    }

    /* Setting ImageView Method */
    private void setImageView(ImageView mImageView)
    {
        mImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) { startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_CAPTURE); }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == CAMERA_CAPTURE && resultCode == getActivity().RESULT_OK)
        {
            /* ImageView */
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(getActivity()).load(data.getData()).error(R.drawable.ic_nopage).into(mImageView);
        }
    }
}
