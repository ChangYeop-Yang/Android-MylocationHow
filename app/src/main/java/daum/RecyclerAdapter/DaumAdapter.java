package daum.RecyclerAdapter;

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
import com.net.location.mylocationhow.MapActivity;
import com.net.location.mylocationhow.R;

import java.util.ArrayList;

import basic.Vibrate;

/**
 * Created by Mari on 2015-10-05.
 */
public class DaumAdapter extends RecyclerView.Adapter<DaumAdapter.ViewHolder>
{
    /* Context */
    private Context mContext = null;

    /* List<MyDate> */
    private ArrayList<DaumMyData> mList = null;
    /* String */
    private String mCode = null;

    public DaumAdapter(Context mContext, ArrayList<DaumMyData> mList, String mCode)
    {
        this.mContext = mContext;
        this.mList = mList;
        this.mCode = mCode;
    }

    @Override
    public DaumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    { View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_location_cardview, parent, false); return new ViewHolder(mView); }

    @Override
    public void onBindViewHolder(final DaumAdapter.ViewHolder holder, final int position)
    {
        /* ImageView */
        int mDrawable = 0;

        /* ImageView Image Setting */
        switch (mCode)
        {
            case ("FD6"): { mDrawable = R.drawable.ic_restorant; break; } /* 음식점 */
            case ("AD5"): { mDrawable = R.drawable.ic_airline; break; } /* 숙소 */
            case ("CE7"): { mDrawable = R.drawable.ic_cafe; break; } /* 카페 */
            case ("CS2"): { mDrawable = R.drawable.ic_convenience48; break; } /* 편의점 */
            case ("PK6"): { mDrawable = R.drawable.ic_parking48; break; } /* 주차장 */
            case ("OL7"): { mDrawable = R.drawable.ic_oil48; break; } /* 주유소 */
            case ("FIN"): { mDrawable = R.drawable.ic_heart; break; } /* 기타 */
        } Glide.with(mContext).load(mDrawable).override(48, 48).into(holder.mImageView); /* ImageView Drawable Setting */

            /* TextView */
        holder.mTextView[0].setText(mList.get(position).mTitle); /* 제목 */
        holder.mTextView[1].setText(mList.get(position).mAddress); /* 주소 */
        holder.mTextView[2].setText(mList.get(position).mDistance + " m"); /* 남은거리 */

        /* Button */
        holder.mButton[0].setOnClickListener(new View.OnClickListener() /* 전화 버튼 */
        {
            @Override
            public void onClick(View v)
            {
                Vibrate.Vibrator(mContext); /* 진동 */
                if(mList.get(position).mPhone.equals("0")) { Toast.makeText(mContext, "전화번호가 제공되지 않습니다.", Toast.LENGTH_SHORT).show(); }
                else { Snackbar.make(v.getRootView(), mList.get(position).mTitle, Snackbar.LENGTH_SHORT).setAction("전화", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + mList.get(position).mPhone))); }
                }).show(); }
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
                            /* Map Intent Move */
                        Intent mIntent = new Intent(mContext, MapActivity.class);
                        mIntent.putExtra("Switch", 1);
                        mIntent.putExtra("MapX", mList.get(position).MapX); /* 위도 */
                        mIntent.putExtra("MapY", mList.get(position).MapY); /* 경도 */
                        mIntent.putExtra("Title", mList.get(position).mTitle); /* 상호명 */
                        mContext.startActivity(mIntent);
                    }
                }).show();
            }
        });
    }

    @Override
    public int getItemCount() { return this.mList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        /* TextView */
        private TextView mTextView[] = new TextView[3];
        /* ImageView */
        private ImageView mImageView = null;
        /* Button */
        private Button mButton[] = new Button[2];

        private ViewHolder(View itemView)
        {
            super(itemView);

            /* TextView */
            mTextView[0] = (TextView)itemView.findViewById(R.id.CustomTitleText); /* 제목 */
            mTextView[1] = (TextView)itemView.findViewById(R.id.CustomAddressText); /* 주소 */
            mTextView[2] = (TextView)itemView.findViewById(R.id.CustomDistanceText); /* 거리 */

            /* ImageView */
            mImageView = (ImageView)itemView.findViewById(R.id.CustomImage); /* 이미지 */

            /* Button */
            mButton[0] = (Button)itemView.findViewById(R.id.CustomCallButton); /* 전화 버튼 */
            mButton[1] = (Button)itemView.findViewById(R.id.CustomPlaceButton); /* 이동 버튼 */
        }
    }

    /* Custom Adapter Class */
    public static class DaumMyData
    {
        /* String */
        private String mTitle = null; /* 제목 */
        private String mPhone = null; /* 전화번호 */
        private String mAddress = null; /* 주소 */
        private String mDistance = null; /* 남은거리 */
        private String mURL = null;
        /* Double */
        private double MapX = 0; /* 위도 */
        private double MapY = 0; /* 경도 */

        public DaumMyData(String mTitle, double MapX, double MapY, String mPhone, String mAddress, String mDistance, String mURL)
        {
            /* Address */
            this.mTitle = mTitle;
            this.mPhone = mPhone;
            this.mAddress = mAddress;
            this.mDistance = mDistance;
            this.mURL = mURL;
            /* Double */
            this.MapX = MapX;
            this.MapY = MapY;
        }
    }
}
