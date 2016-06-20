package travel.RecyclerAdapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.net.location.mylocationhow.CultureDetailActivity;
import com.net.location.mylocationhow.MapActivity;
import com.net.location.mylocationhow.R;

import java.util.ArrayList;

import basic.Vibrate;

/**
 * Created by Mari on 2015-09-26.
 */
public class TourCultureAdapter extends RecyclerView.Adapter<TourCultureAdapter.ViewHolder>
{
    /* Context */
    private Context mContext = null;

    /* List<MyDate> */
    private ArrayList<TourCultureMyDate> mList = null;

    public TourCultureAdapter(Context mContext, ArrayList<TourCultureMyDate> mList)
    {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public TourCultureAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_culture_cardview, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(final TourCultureAdapter.ViewHolder holder, final int position)
    {
        /* ImageView */
        Glide.with(mContext).load(mList.get(position).mImageRes).override(250, 100).error(R.drawable.ic_nopage).into(holder.mImageView);

        /* TextView */
        holder.mTextView[0].setText(mList.get(position).mTitle); /* 제목 */
        holder.mTextView[1].setText(mList.get(position).mAddress); /* 주소 */

        /* Button */
        holder.mButton[0].setOnClickListener(new View.OnClickListener() /* 전화 버튼 */
        {
            @Override
            public void onClick(View v)
            {
                Vibrate.Vibrator(mContext); /* 진동 */
                Snackbar.make(v.getRootView(), mList.get(position).mTitle, Snackbar.LENGTH_SHORT).setAction("전화", new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if (mList.get(position).mTel != null) { mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + mList.get(position).mTel))); }
                        else { Toast.makeText(mContext, "전화번호를 제공하지 않습니다.", Toast.LENGTH_SHORT).show(); }
                    }
                }).show();
            }
        });
        holder.mButton[1].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Vibrate.Vibrator(mContext); /* 진동 */
                Snackbar.make(v, mList.get(position).mTitle, Snackbar.LENGTH_SHORT).setAction("이동", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(mList.get(position).mMapX != 0.0)
                        {
                            /* Map Intent Move */
                            Intent mIntent = new Intent(mContext, MapActivity.class);
                            mIntent.putExtra("Switch", 1);
                            mIntent.putExtra("MapX", mList.get(position).mMapX); /* 위도 */
                            mIntent.putExtra("MapY", mList.get(position).mMapY); /* 경도 */
                            mIntent.putExtra("Title", mList.get(position).mTitle); /* 상호명 */
                            mContext.startActivity(mIntent);
                        } else { Toast.makeText(mContext, "장소를 제공하지 않습니다.", Toast.LENGTH_SHORT).show(); }
                    }
                }).show();
            }
        });
        holder.mButton[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                /* Intext */
                Intent mIntent = new Intent(mContext, CultureDetailActivity.class);
                mIntent.putExtra("ID", mList.get(position).mId);
                mIntent.putExtra("MapX", mList.get(position).mMapX);
                mIntent.putExtra("MapY", mList.get(position).mMapY);
                mIntent.putExtra("Title", mList.get(position).mTitle);
                mIntent.putExtra("Tel", mList.get(position).mTel);
                mIntent.putExtra("Address", mList.get(position).mAddress);
                mIntent.putExtra("IMG", mList.get(position).mImageRes);
                mContext.startActivity(mIntent);
            }
        });
    }

    @Override
    public int getItemCount() { return this.mList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        /* TextView */
        private TextView mTextView[] = new TextView[2];
        /* ImageView */
        private ImageView mImageView = null;
        /* Button */
        private Button mButton[] = new Button[3];

        private ViewHolder(View itemView)
        {
            super(itemView);

                /* TextView */
            mTextView[0] = (TextView)itemView.findViewById(R.id.CustomCultureCardTitle); /* 제목 */
            mTextView[1] = (TextView)itemView.findViewById(R.id.CustomCardAddress); /* 주소 */

                /* ImageView */
            mImageView = (ImageView)itemView.findViewById(R.id.CustomCultureCardImage); /* 이미지 */

                /* Button */
            mButton[0] = (Button)itemView.findViewById(R.id.CustomCultureCallButton); /* 전화 버튼 */
            mButton[1] = (Button)itemView.findViewById(R.id.CustomCultureMapButton); /* 이동 버튼 */
            mButton[2] = (Button)itemView.findViewById(R.id.CustomCultureDetailedButton); /* 상세보기 버튼 */
        }
    }

    /* Custom Card Adapter */
    public static class TourCultureMyDate
    {
        /* String */
        private String mTitle = null;
        private String mAddress = null;
        private String mTel = null;
        private String mImageRes = null;
        private String mId = null;
        /* Double */
        private double mMapX = 0;
        private double mMapY = 0;

        /* MyDate Adapter */
        public TourCultureMyDate(String mAddress, String mId, String mTel, String mTitle, String mImageRes, double mMapX, double mMapY)
        {
            /* Int */
            this.mImageRes = mImageRes;
            /* String */
            this.mTitle = mTitle;
            this.mAddress = mAddress;
            this.mTel = mTel;
            this.mId = mId;
            /* Double */
            this.mMapX = mMapX;
            this.mMapY = mMapY;
        }
    }
}