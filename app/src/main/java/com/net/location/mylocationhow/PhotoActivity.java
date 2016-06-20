package com.net.location.mylocationhow;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import basic.BitmapSave;
import basic.GPS;

/**
 * Created by Mari on 2015-09-16.
 */
public class PhotoActivity extends BaseActivity
{
    /* Context */
    private Context mContext = PhotoActivity.this;

    /* EditText */
    private AppCompatEditText mEditText[] = null;

    /* ImageView */
    private ImageView mImageView[] = null;

    /* int */
    private static final int CAMERA_CAPTURE = 100;
    private static final int REQUEST_CODE_IMAGE= 100;
    private int mID = 0;
    private int mImageViewSwitch = 0;

    /* String */
    private String mDateString = null;

    /* double */
    private double mScore = 0.0;

    /* Boolean */
    private Boolean mRecording = false;
    private Boolean mSelectSwitch[] = {false, false, false, false};

    /* Surface */
    private SurfaceHolder mSurfaceHolder = null;
    private SurfaceView mSurfaceView = null;

    /* MediaRecorder */
    private MediaRecorder mMediaRecorder = null;

    /* MediaPlayer */
    private MediaPlayer mMediaPlayer = null;

    /* Camera */
    private Camera mCamera = null;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        /* ToolBar */
        mToolbar = (Toolbar) findViewById(R.id.PhotoToolbar); /* ToolBar */
        /* ToolBar Title */
        mToolbar.setTitle("사진"); /* ToolBar Title Text */
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        mToolbar.setNavigationIcon(R.drawable.ic_arrow); /* Icon Setting */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* EditText */
        mEditText = new AppCompatEditText[]{(AppCompatEditText)findViewById(R.id.PhotoTitleEditText), (AppCompatEditText)findViewById(R.id.PhotoCommentEditText)};

        /* ImageView */
        mImageView = new ImageView[]{(ImageView)findViewById(R.id.PhotoImageView), (ImageView)findViewById(R.id.PhotoReserveImage), (ImageView)findViewById(R.id.PhotoMapImageView)};

        /* RatingBar */
        RatingBar mRatingBar = (RatingBar)findViewById(R.id.PhotoRatingBar);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) { Toast.makeText(mContext, "" + (mScore = rating), Toast.LENGTH_SHORT).show(); /* 현재 점수 출력 */ } });

        /* SurfaceView */
        mSurfaceView = (SurfaceView)findViewById(R.id.PhotoSurface);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /* String */
        mDateString = createDate();

        /* Intent */
        Intent mIntent = getIntent();
        setIntent(mIntent.getIntExtra("Switch", 0), mIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	/* 카메라의 인텐트 결과를 받아 들려 Bitmap 쪼갠 후 받아서 image view 출력해주는 함수 */
        if(requestCode == CAMERA_CAPTURE && resultCode == RESULT_OK)
        {
            mImageView[mImageViewSwitch].setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mContext).load(data.getData()).override(500, 200).error(R.drawable.ic_nopage).into(mImageView[mImageViewSwitch]);
        }
    }

    /* Optinon 관련 메소드 */
    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.menu_photo, menu); return true; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /* ContentValues */
        ContentValues mValues = new ContentValues();

        /* ActionBar Item */
        switch (item.getItemId())
        {
            case (R.id.PhotoSave) : /* 저장 */
            {
                /* TextView */
                final TextView mTextView = (TextView)findViewById(R.id.PhotoMapTextView);

                /* String */
                final String mEditString[] = {mEditText[0].getText().toString(), mEditText[1].getText().toString(), mTextView.getText().toString()};

                if(mSelectSwitch[3]) /* 수정 모드 */
                {
                    mValues.put("title", mEditString[0]); /* 제목 */
                    mValues.put("comment", mEditString[1]); /* 설명 */
                    mValues.put("rating", mScore); /* 점수 */
                    mDataBase.update(mDataBaseTable[0], mValues, "_id = " + mID, null); /* Update */
                }
                /* 쓰기 모드 */
                else
                {
                    /* String */
                    String mPathString[] = new String[4];

                    /* 여행 일기 제목 */
                    if(mEditString[0].equals("")) { Toast.makeText(mContext, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show(); return false; }
                    /* 여행 상세정보 */
                    if(mEditString[1].equals("")) { Toast.makeText(mContext, "상세정보를 입력해주세요.", Toast.LENGTH_SHORT).show(); return false; }
                    /* 여행 메인사진 */
                    if(!mSelectSwitch[0]) { Toast.makeText(mContext, "사진을 촬영해주세요.", Toast.LENGTH_SHORT).show(); return false; }

                    try
                    {
                        /* String */
                        final String mStringFormat = String.format("%s%s", mDateString, mEditString[0]);

                        mPathString[0] = new BitmapSave(mContext, mImageView[0], "Photo/photos", mStringFormat).execute().get(); /* 사진 이미지 저장 */
                        mPathString[1] = new BitmapSave(mContext, mImageView[2], "Photo/maps", mStringFormat).execute().get(); /* 지도 이미지 저장 */

                        /* 예비 이미지 저장 */
                        if (mSelectSwitch[1]) { mPathString[2] = new BitmapSave(mContext, mImageView[1], "Photo/reserve", mStringFormat).execute().get(); } else { mPathString[2] = "Not Path."; }
                        /* 동영상 저장 */
                        if (mSelectSwitch[2]) { mPathString[3] = "/sdcard/MyLocationHow/Photo/videos/" + String.format("%s.mp4", mStringFormat); } else { mPathString[3] = "Not Path."; }

                        /* Value */
                        mValues.put("title", mEditString[0]); /* 제목 */
                        mValues.put("comment", mEditString[1]); /* 설명 */
                        mValues.put("address", mEditString[2]); /* 주소 */
                        mValues.put("path1", mPathString[0]); /* 경로1 */
                        mValues.put("path2", mPathString[1]); /* 경로2 */
                        mValues.put("path3", mPathString[2]); /* 경로3 */
                        mValues.put("path4", mPathString[3]); /* 경로4 */
                        mValues.put("latitude", GPS.getLatitude()); /* 위도 */
                        mValues.put("longitude", GPS.getLongitude()); /* 경도 */
                        mValues.put("rating", mScore); /* 점수 */
                        mDataBase.insert(mDataBaseTable[0], null, mValues);
                    }
                    catch (InterruptedException e) { e.printStackTrace(); }
                    catch (ExecutionException e) { e.printStackTrace(); }
                }
            } finish(); return true;
        } return super.onOptionsItemSelected(item);
    }

    /* SetButton Method */
    private void setButton(final Button mButton, final int mSwitch)
    {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mSwitch)
                {
                    case (0): /* 녹화 시작 버튼 */
                    {
                        if (!mEditText[0].getText().toString().equals(""))
                        {
                            /* SD CARD Path Check */
                            final File mFile = new File("/sdcard/MyLocationHow/Photo/videos/");
                            if (!mFile.exists()) { mFile.mkdirs(); }

                            if (mRecording) /* 녹화 중 */
                            {
                                mButton.setText("녹화시작");
                                mRecording = false;
                                mMediaRecorder.stop();
                                mMediaRecorder.release();
                                mMediaRecorder = null;
                            }
                            else
                            {
                                /* Camera */
                                mCamera = Camera.open();
                                mCamera.setDisplayOrientation(90); /* Camera 회전각 설정 */
                                mCamera.unlock();

                                /* MediaRecorder */
                                mSelectSwitch[2] = true;
                                mMediaRecorder = new MediaRecorder();
                                mMediaRecorder.setCamera(mCamera); /* 카메라 설정 */
                                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                                /* 오디오 및 동영상 파일 형식 지정 */
                                mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                                mMediaRecorder.setMaxDuration(mPhotoVideoTime); /* 최대 시간 2분 */
                                /* 파일 저장 경로 설정 */
                                mMediaRecorder.setOutputFile("/sdcard/MyLocationHow/Photo/videos/" + String.format("%s%s.mp4", mDateString, mEditText[0].getText()));
                                mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                                mMediaRecorder.setOrientationHint(90); /* SurfaceView 회전각 설정 */

                                if (mMediaRecorder != null) {
                                    /* 녹화 준비 */
                                    try
                                    {
                                        mMediaRecorder.prepare();
                                        Toast.makeText(mContext, "녹화가 시작 되었습니다.", Toast.LENGTH_SHORT).show();

                                        /* Timer */
                                        new CountDownTimer(mPhotoVideoTime, 1000)
                                        {
                                            @Override public void onTick(long millisUntilFinished) {}
                                            @Override public void onFinish()
                                            {
                                                if(mRecording != false)
                                                {
                                                    /* MediaRecorder */
                                                    mMediaRecorder.stop(); mMediaRecorder.release();
                                                    mButton.setText("녹화시작"); mRecording = false;
                                                    Toast.makeText(mContext, "녹화가 종료 되었습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }.start();
                                    }
                                    catch (IllegalStateException e)
                                    {
                                        Toast.makeText(mContext, "카메라 장치에 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                        Log.e("MediaRecorder IllegalStateException", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    catch (IOException e)
                                    {
                                        Toast.makeText(mContext, "카메라 장치에 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                        Log.e("MediaRecorder IOException", e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                                /* 녹화 시작 */
                                mMediaRecorder.start();
                                mButton.setText("녹화중단");
                                mRecording = true;
                            }
                        } else { Toast.makeText(mContext, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show(); } break;
                    }
                }
            }
        });
    }

    /* setImageView */
    private void setImageView(final ImageView mImageView, final int mImageSwitch)
    {
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                switch (mImageSwitch)
                {
                    /* 메인 이미지 */
                    case (0) :
                    {
                        mImageViewSwitch = 0; mSelectSwitch[0] = true;
                        createAlertDialog(); break;
                    }
                    /* 확장 이미지 */
                    case (1):
                    {
                        mImageViewSwitch = 1; mSelectSwitch[1] = true;
                        createAlertDialog(); break;
                    }
                }
            }
        });
    }

    /* Create Dialog Method */
    private void createAlertDialog()
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setPositiveButton("촬영하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, "촬영하기", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_CAPTURE);
            }
        }).setNegativeButton("가져오기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, "앨범에서 가져오기", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_CODE_IMAGE);
            }
        }).setNeutralButton("취소", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
        }).show();
    }

    /* setIntent Method */
    private void setIntent(int mSwitch, Intent mIntent)
    {
        switch (mSwitch)
        {
            case (1) : /* 쓰기 모드 */
            {
                mToolbar.setSubtitle("쓰기");
                /* Map ImageView */
                GPS.createNowLocationMap(mContext, mImageView[2]);
                for(int count = 0, mImageLength = mImageView.length; count < (mImageLength - 1); count++) { setImageView(mImageView[count], count); }

                /* TextView */
                GPS.createMapAddress(mContext, (TextView) findViewById(R.id.PhotoMapTextView));

                /* Button */
                setButton((Button) findViewById(R.id.PhotoRecButton), 0); /* 녹화 버튼 */ break;
            }
            case (2) : /* 읽기 모드 */
            {
                mToolbar.setSubtitle("수정");
                mID = mIntent.getIntExtra("ID", 0);
                mSelectSwitch[3] = true; /* 읽기 모드 */

                /* String */
                final String mPathString[] = {mIntent.getStringExtra("Path1"), mIntent.getStringExtra("Path2"), mIntent.getStringExtra("Path3"), mIntent.getStringExtra("Path4")};

                /* CardView */
                CardView mCardView[] = {(CardView)findViewById(R.id.PhotoReserveCardView), (CardView)findViewById(R.id.PhotoMovieCardView)};
                if(mPathString[2].equals("Not Path.")) { mCardView[0].setVisibility(View.GONE); }
                if(mPathString[3].equals("Not Path.")) { mCardView[1].setVisibility(View.GONE); }

                /* ImageView */
                ImageView mImageView[] = {(ImageView)findViewById(R.id.PhotoImageView), (ImageView)findViewById(R.id.PhotoMapImageView), (ImageView)findViewById(R.id.PhotoReserveImage)};
                for(int count = 0, mLength = mImageView.length; count < mLength; count++) { mImageView[count].setScaleType(ImageView.ScaleType.FIT_XY); Glide.with(mContext).load(mPathString[count]).override(500, 200).into(mImageView[count]); }

                /* TextView */
                TextView mTextVIew = (TextView)findViewById(R.id.PhotoMapTextView);
                mTextVIew.setText(mIntent.getStringExtra("Address")); /* 주소 설정 */

                /* EditText */
                EditText mEditText[] = {(EditText)findViewById(R.id.PhotoTitleEditText), (EditText)findViewById(R.id.PhotoCommentEditText)};
                mEditText[0].setText(mIntent.getStringExtra("Name")); /* 제목 설정 */
                mEditText[1].setText(mIntent.getStringExtra("Comment")); /* 설명 설정 */

                /* Rating Bar */
                RatingBar mRatingBar = (RatingBar)findViewById(R.id.PhotoRatingBar);
                mRatingBar.setRating((float) mIntent.getDoubleExtra("Score", 0.0));

                /* Button */
                final Button mButton = (Button)findViewById(R.id.PhotoRecButton);
                mButton.setText("재생");
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        /* MediaPlayer */
                        if(mRecording) /* 멈춤 */
                        {
                            /* MediaPlayer */
                            mButton.setText("재생");
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                            mMediaPlayer = null;
                            mRecording = false;
                        }
                        else /* 재생 */
                        {
                            mRecording = true;
                            mButton.setText("중지");

                            /* MediaPlayer */
                            mMediaPlayer = new MediaPlayer();
                            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp)
                                {
                                    mButton.setText("재생"); Toast.makeText(mContext, "동영상이 종료 되었습니다.", Toast.LENGTH_SHORT).show();
                                    mp.release(); mp = null;
                                    mRecording = false;
                                }
                            });

                            try
                            {
                                mMediaPlayer.setDataSource(mPathString[3]);
                                mMediaPlayer.setDisplay(mSurfaceHolder);
                                /* 재생 준비 및 시작 */
                                mMediaPlayer.prepare();
                                mMediaPlayer.start();
                            } catch (IOException e) { e.printStackTrace(); }
                        }
                    }
                }); break;
            }
        }
    }
}