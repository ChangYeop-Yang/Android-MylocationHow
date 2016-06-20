package travel.RecyclerAdapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.net.location.mylocationhow.MapActivity;
import com.net.location.mylocationhow.R;
import com.net.location.mylocationhow.RoadDetailActivity;

import java.util.ArrayList;

/**
 * Created by Mari on 2015-09-26.
 */
public class TourRoadAdapter extends RecyclerView.Adapter<TourRoadAdapter.ViewHolder>
{
    /* Context */
    private Context mContext = null;

    /* List<MyDate> */
    private ArrayList<TourRoadMyDate> mList = null;

    public TourRoadAdapter(Context mContext, ArrayList<TourRoadMyDate> mList)
    {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public TourRoadAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    { View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_road_cardview, parent, false); return new ViewHolder(mView); }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        /* TextView */
        holder.mTextView.setText(mList.get(position).mTitle);

        /* ImageView */
        Glide.with(mContext).load(mList.get(position).mImageRes).override(300, 150).into(holder.mImageView);

        /* Button */
        setButton(holder.mButton[0], 0, position); /* 상세보기 */
        setButton(holder.mButton[1], 1, position); /* 위치버튼 */
    }

    @Override public int getItemCount() { return mList.size(); }

    /* SetButton Method */
    private void setButton(Button mButton, final int mSwitch, final int mPosition)
    {
        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (mSwitch)
                {
                    case (0) : /* 상세보기 */
                    {
                        /* Intent */
                        Intent mIntent = new Intent(mContext, RoadDetailActivity.class);
                        mIntent.putExtra("Title", mList.get(mPosition).mTitle);
                        mIntent.putExtra("ID", mList.get(mPosition).mId);
                        mIntent.putExtra("MapX", mList.get(mPosition).mMapX);
                        mIntent.putExtra("MapY", mList.get(mPosition).mMapY);
                        mContext.startActivity(mIntent);
                        break;
                    }
                    case (1) : /* 위치 */
                    {
                        /* Intent */
                        Intent mIntent = new Intent(mContext, MapActivity.class);
                        mIntent.putExtra("Switch", 1);
                        mIntent.putExtra("MapX", mList.get(mPosition).mMapX);
                        mIntent.putExtra("MapY", mList.get(mPosition).mMapY);
                        mIntent.putExtra("Title", mList.get(mPosition).mTitle);
                        mContext.startActivity(mIntent);
                        break;
                    }
                }
            }
        });
    }

    /* RoadAdapter ViewHolder */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        /* TextView */
        TextView mTextView = null;

        /* Button */
        Button mButton[] = new Button[2];

        /* ImageView */
        ImageView mImageView = null;

        private ViewHolder(View itemView)
        {
            super(itemView);

            /* TextView */
            mTextView = (TextView)itemView.findViewById(R.id.RoadCustomTextView);
            /* Button */
            mButton[0] = (Button)itemView.findViewById(R.id.RoadCustomButton);
            mButton[1] = (Button)itemView.findViewById(R.id.RoadLocationCustomButton);
            /* ImageView */
            mImageView = (ImageView)itemView.findViewById(R.id.RoadCustomImageView);
        }
    }

    /* Custom Card Adapter */
    public static class TourRoadMyDate
    {
        /* String */
        private String mTitle = null;
        private String mImageRes = null;
        private String mId = null;
        /* Double */
        private double mMapX = 0.0;
        private double mMapY = 0.0;

        /* MyDate Adapter */
        public TourRoadMyDate(String mId, String mImageRes, String mTitle, double mMapX, double mMapY)
        {
            /* Int */
            this.mImageRes = mImageRes;
            /* String */
            this.mTitle = mTitle;
            this.mId = mId;
            /* Double */
            this.mMapX = mMapX;
            this.mMapY = mMapY;
        }
    }
}
