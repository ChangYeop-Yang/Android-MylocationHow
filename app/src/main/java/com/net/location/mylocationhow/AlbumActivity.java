package com.net.location.mylocationhow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.File;
import java.util.ArrayList;

import basic.Vibrate;

/**
 * Created by Mari on 2015-09-17.
 */
public class AlbumActivity extends BaseActivity
{
    /* Context */
    private Context mContext = AlbumActivity.this;

    /* RecyclerAdapter */
    private RecyclerAdapter mAdapter = null;

    /* RecyclerView */
    private RecyclerView mRecyclerView = null;

    /* SearchView */
    private SearchView mSearchView = null;

    /* Integer */
    private int mSwitch = 0;
    private int mPage[] = {1, 1, 0};

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        /* ToolBar */
        mToolbar = (Toolbar) findViewById(R.id.AlbumToolbar); /* ToolBar */
        /* ToolBar Title */
        mToolbar.setTitle("앨범"); /* ToolBar Title Text */
        mToolbar.setTitleTextColor(Color.WHITE); /* ToolBar Title Text Color */
        mToolbar.setNavigationIcon(R.drawable.ic_arrow); /* Icon Setting */
        setSupportActionBar(mToolbar); /* ActionBar -> ToolBar */

        /* SQLite */
        int mSQLLength = mDataBase.rawQuery("SELECT * FROM photo_db", null).getCount();
        mToolbar.setSubtitle(String.format("%d 개", mSQLLength));
        mPage[1] = (mSQLLength % mAlbumItemCount != 0) ? (mSQLLength / mAlbumItemCount) + 1 : mSQLLength / mAlbumItemCount;

        /* Recycler View */
        mRecyclerView = (RecyclerView)findViewById(R.id.AlbumRecyclerView);

        /* RecyclerView */
        setRecyclerView(mRecyclerView, (FloatingActionsMenu)findViewById(R.id.AlbumMultipleDownMenu));
        loadDatabase(mRecyclerView);

        /* FloatingButton */
        final FloatingActionButton mFloating[] = {(FloatingActionButton)findViewById(R.id.AlbumBackButton), (FloatingActionButton)findViewById(R.id.AlbumNextButton), (FloatingActionButton)findViewById(R.id.AlbumRollBackButton)};
        for(int count = 0, mLength = mFloating.length; count < mLength; count++) { setFloatingButton(mFloating[count], count); }
    }

    /* Setting FloatingButton Method */
    private void setFloatingButton(final FloatingActionButton mFloating, final int mSwitch)
    {
        mFloating.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* 진동 */
                Vibrate.Vibrator(mContext);

                switch (mSwitch)
                {
                    /* 이전 */
                    case (0) : { if(--mPage[0] < 1) { Toast.makeText(mContext, "첫 페이지 입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = 1; } else { mPage[2] -= mAlbumItemCount; loadDatabase(mRecyclerView); } break; }
                    /* 다음 */
                    case (1) : { if(++mPage[0] > mPage[1]) { Toast.makeText(mContext, "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show(); mPage[0] = mPage[1]; } else { mPage[2] += mAlbumItemCount; loadDatabase(mRecyclerView); } break; }
                    /* 새로고침 */
                    case (2) : { loadDatabase(mRecyclerView); break; }
                }
            }
        });
    }

    /* loadDatabase Method */
    private void loadDatabase(RecyclerView mRecyclerView)
    {
        /* String */
        String mQuery = null;
        switch (mSwitch)
        {
            /* 제목정렬 */
            case (0) : { mQuery = "SELECT * FROM photo_db ORDER BY title ASC LIMIT '" + mAlbumItemCount + "' OFFSET '" + mPage[2] + "'"; break; }
            /* 주소정렬 */
            case (1) : { mQuery = "SELECT * FROM photo_db ORDER BY address ASC LIMIT '" + mAlbumItemCount + "' OFFSET '" + mPage[2] + "'"; break; }
            /* 점수정렬 */
            case (2) : { mQuery = "SELECT * FROM photo_db ORDER BY rating DESC LIMIT '" + mAlbumItemCount + "' OFFSET '" + mPage[2] + "'"; break; }
            /* 검색어 */
            case (3) : { mQuery = "SELECT * FROM photo_db WHERE title LIKE '%" + mSearchView.getQuery() + "%'"; break; }
        }

        /* Cursor */
        final Cursor mCursor = mDataBase.rawQuery(mQuery, null);

        try
        {
            /* ArrayList */
            ArrayList<MyDate> mArrayList = new ArrayList<MyDate>(10);

            while(mCursor.moveToNext())
            { mArrayList.add(new MyDate(mCursor.getString(1), mCursor.getString(2), mCursor.getString(3), mCursor.getString(4), mCursor.getString(9), mCursor.getString(10), mCursor.getString(5), mCursor.getInt(0), mCursor.getDouble(6), mCursor.getDouble(7), mCursor.getDouble(8))); }

            mRecyclerView.setAdapter(mAdapter = new RecyclerAdapter(mArrayList));
        }
        catch (SQLiteException ex)
        {
            Toast.makeText(mContext, "SQLite 불러오는 과정에서 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            ex.printStackTrace(); Log.e("Album SQLiteException", ex.getMessage());
        } finally { mCursor.close(); }
    }

    /* Optinon 관련 메소드 */
    @Override public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_album, menu);
        /* Search View */
        mSearchView = (SearchView)menu.findItem(R.id.AlbumMenuSearch).getActionView();
        mSearchView.setQueryHint("제목을 입력하세요.");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override public boolean onQueryTextSubmit(String query) { mSwitch = 3; loadDatabase(mRecyclerView); return false; }
            @Override public boolean onQueryTextChange(String newText) { return false; }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case (R.id.AlbumMenuName) : { mSwitch = 0; loadDatabase(mRecyclerView); return true; }
            case (R.id.AlbumMenuAddress) : { mSwitch = 1; loadDatabase(mRecyclerView); return true; }
            case (R.id.AlbumMenuScore) : { mSwitch = 2; loadDatabase(mRecyclerView); return true; }
        } return super.onOptionsItemSelected(item);
    }

    /* Custom Card Adapter */
    private class MyDate
    {
        /* int */
        private int mID = 0;
        /* String */
        private String mTitle = null;
        private String mAddress = null;
        private String mPath1 = null;
        private String mPath2 = null;
        private String mPath3 = null;
        private String mPath4 = null;
        private String mComment = null;
        /* Double */
        private double mMapX = 0.0; /* 위도 */
        private double mMapY = 0.0; /* 경도 */
        private double mScore = 0.0; /* 점수 */

        /* MyDate Adapter */
        private MyDate(String mTitle, String mAddress, String mPath1, String mPath2, String mPath3, String mPath4, String mComment, int mID, double mMapX, double mMapY, double mScore)
        {
            /* String */
            this.mTitle = mTitle;
            this.mAddress = mAddress;
            this.mPath1 = mPath1;
            this.mPath2 = mPath2;
            this.mPath3 = mPath3;
            this.mPath4 = mPath4;
            this.mComment = mComment;
            /* Int */
            this.mID = mID;
            /* Double */
            this.mMapX = mMapX;
            this.mMapY = mMapY;
            this.mScore = mScore;
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
    {
        /* List<MyDate> */
        private ArrayList<MyDate> mList = null;

        private RecyclerAdapter(ArrayList<MyDate> mList) { this.mList = mList; }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_album_cardview, parent, false);
            return new ViewHolder(mView);
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, final int position)
        {
            /* TextView */
            holder.mTextView[0].setText(mList.get(position).mTitle); /* 제목 */
            holder.mTextView[1].setText(mList.get(position).mAddress); /* 주소 */

            /* ImageView */
            Glide.with(mContext).load(new File(mList.get(position).mPath1)).override(500, 150).into(holder.mImageView); /* Path1 */

            /* RatingBar */
            holder.mRatingBar.setRating((float) mList.get(position).mScore);

            /* Button */
            for(int count = 0, mLength = holder.mButton.length; count < mLength; count++)
            { setButton(holder.mButton[count], count, position, mList.get(position).mID); }

            /* ImageButton */
            setImageButton(holder.mImageButton, 0, position);
        }

        @Override
        public int getItemCount() { return this.mList.size(); }

        /* setButton Method */
        private void setButton(Button mButton, final int mSwitch, final int mPosition, final int mID)
        {
            mButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    /* 진동 */
                    Vibrate.Vibrator(mContext);
                    switch (mSwitch)
                    {
                        case (0) : /* 위치 */
                        {
                            Intent mIntent = new Intent(mContext, MapActivity.class);
                            mIntent.putExtra("Switch", 1);
                            mIntent.putExtra("MapX", mList.get(mPosition).mMapX); /* 위도 */
                            mIntent.putExtra("MapY", mList.get(mPosition).mMapY); /* 경도 */
                            mIntent.putExtra("Title", mList.get(mPosition).mTitle); /* 상호명 */
                            mContext.startActivity(mIntent); break;
                        }
                        case (1) : /* 상세정보 */
                        {
                            Intent mIntent = new Intent(mContext, PhotoActivity.class);
                            mIntent.putExtra("Switch", 2);
                            mIntent.putExtra("Name", mList.get(mPosition).mTitle); /* 제목 */
                            mIntent.putExtra("Address", mList.get(mPosition).mAddress); /* 주소 */
                            mIntent.putExtra("Path1", mList.get(mPosition).mPath1); /* 경로1 */
                            mIntent.putExtra("Path2", mList.get(mPosition).mPath2); /* 경로2 */
                            mIntent.putExtra("Path3", mList.get(mPosition).mPath3); /* 경로3 */
                            mIntent.putExtra("Path4", mList.get(mPosition).mPath4); /* 경로4 */
                            mIntent.putExtra("Comment", mList.get(mPosition).mComment); /* 설명 */
                            mIntent.putExtra("Score", mList.get(mPosition).mScore); /* 점수 */
                            mIntent.putExtra("ID", mList.get(mPosition).mID); /* ID */
                            mContext.startActivity(mIntent); break;
                        }
                        case (2) : /* 삭제 */
                        {
                            /* AlertDialog */
                            AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
                            mBuilder.setTitle(mList.get(mPosition).mTitle).setMessage(String.format("%s 을 삭제하시겠습니까?\n(* 삭제 된 내용은 복구 되지 않습니다.)", mList.get(mPosition).mTitle))
                                    .setPositiveButton("삭제", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            /* DataBase */
                                            mDataBase.execSQL("DELETE FROM photo_db WHERE _id = '" + mID + "'");
                                            mToolbar.setSubtitle(String.format("%d 개", mDataBase.rawQuery("SELECT * FROM photo_db", null).getCount()));

                                            /* File */
                                            File mFile[] = {new File(mList.get(mPosition).mPath1), new File(mList.get(mPosition).mPath2), new File(mList.get(mPosition).mPath3), new File(mList.get(mPosition).mPath4)};
                                            for (int count = 0, mLength = mFile.length; count < mLength; mFile[count++].delete());

                                            /* Adapter Reroad */
                                            mList.remove(mPosition);
                                            mAdapter.notifyItemRemoved(mPosition);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                            }).show(); break;
                        }
                    }
                }
            });
        }

        /* setImageButton Method */
        private void setImageButton(ImageButton mButton, final int mSwitch, final int mPosition)
        {
            mButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    /* 진동 */
                    Vibrate.Vibrator(mContext);

                    switch (mSwitch)
                    {
                        /* 공유하기 */
                        case (0) :
                        {
                            /* String */
                            String mPathString = mAlbumShare.equals("image/*") ? mList.get(mPosition).mPath1 : mList.get(mPosition).mPath4;

                            /* Intent */
                            Intent mShare = new Intent();
                            mShare.setType(mAlbumShare);
                            mShare.setAction(Intent.ACTION_SEND);
                            mShare.putExtra(Intent.EXTRA_TITLE, mList.get(mPosition).mTitle);
                            mShare.putExtra(Intent.EXTRA_TEXT, String.format("%s\n%s\n%s\n", mList.get(mPosition).mTitle, mList.get(mPosition).mAddress, mList.get(mPosition).mComment));
                            mShare.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mPathString)));
                            mShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(mShare, "공유하기")); break;
                        }
                    }
                }
            });
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            /* TextView */
            private TextView mTextView[] = null;

            /* ImageButton */
            private ImageButton mImageButton = null;

            /* Button */
            private Button mButton[] = null;

            /* ImageVIew */
            private ImageView mImageView = null;

            /* RatingBar */
            private RatingBar mRatingBar = null;

            private ViewHolder(View itemView)
            {
                super(itemView);

                /* TextView */
                mTextView = new TextView[]{(TextView)itemView.findViewById(R.id.AlbumTitle), (TextView)itemView.findViewById(R.id.AlbumAddress)};

                /* ImageButton */
                mImageButton = (ImageButton)itemView.findViewById(R.id.AlbumShare);

                /* Button */
                mButton = new Button[]{(Button)itemView.findViewById(R.id.AlbumLocation), (Button)itemView.findViewById(R.id.AlbumDetail), (Button)itemView.findViewById(R.id.AlbumDrop)};

                /* ImageView */
                mImageView = (ImageView)itemView.findViewById(R.id.AlbumImage);

                /* RatingBar */
                mRatingBar = (RatingBar)itemView.findViewById(R.id.AlbumScore);
            }
        }
    }
}
