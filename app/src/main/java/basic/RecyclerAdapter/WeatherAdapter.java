package basic.RecyclerAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.net.location.mylocationhow.R;

import java.util.ArrayList;

/**
 * Created by Mari on 2015-09-30.
 */
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder>
{
    /* ArrayList */
    private ArrayList<MyWeatherData> mWeatherList = new ArrayList<MyWeatherData>(10);

    /* Context */
    private Context mContext = null;

    /* WeatherAdapter */
    public WeatherAdapter(Context mContext, ArrayList<MyWeatherData> mWeatherList)
    { this.mContext = mContext; this.mWeatherList = mWeatherList; }

    @Override public WeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_weather_cardview, parent, false); return new ViewHolder(mView); }

    @Override
    public void onBindViewHolder(WeatherAdapter.ViewHolder holder, int position)
    {
        /* TextView */
        holder.mWeatherExceptionTime.setText(mWeatherList.get(position).mExceptionTime);
        holder.mWeatherExceptionState.setText(mWeatherList.get(position).mExceptionState);
        holder.mWeatherExceptionLowTemp.setText(mWeatherList.get(position).mExceptionLowTemp);
        holder.mWeatherExceptionHighTemp.setText(mWeatherList.get(position).mExceptionHighTemp);
        holder.mWeatherExceptionRain.setText(mWeatherList.get(position).mExceptionRain);

        /* ImageView */
        setIconWeather(mWeatherList.get(position).mExceptionState, holder.mWeatherExceptionIcon);
    }

    @Override  public int getItemCount() { return mWeatherList.size(); }

    /* WeatherAdapter ViewHolder */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        /* TextView */
        private TextView mWeatherExceptionTime = null;
        private TextView mWeatherExceptionState = null;
        private TextView mWeatherExceptionLowTemp = null;
        private TextView mWeatherExceptionHighTemp = null;
        private TextView mWeatherExceptionRain = null;

        /* ImageView */
        private ImageView mWeatherExceptionIcon = null;

        private ViewHolder(View itemView)
        {
            super(itemView);

            /* TextView */
            mWeatherExceptionTime = (TextView)itemView.findViewById(R.id.WeatherCustomTextView);
            mWeatherExceptionState = (TextView)itemView.findViewById(R.id.WeatherInformationTextView);
            mWeatherExceptionLowTemp = (TextView)itemView.findViewById(R.id.WeatherCustomRowTemp);
            mWeatherExceptionHighTemp = (TextView)itemView.findViewById(R.id.WeatherCustomHighTemp);
            mWeatherExceptionRain = (TextView)itemView.findViewById(R.id.WeatherRainTextView);

            /* ImageView */
            mWeatherExceptionIcon = (ImageView)itemView.findViewById(R.id.WeatherCustomImageView);
        }
    }

    /* IconMatch Method */
    private final void setIconWeather(String mString, ImageView mImageView)
    {
        /* Integer */
        int mIconResource = 0;

        /* Weather Icon Match Switch */
        switch (mString)
        {
            case ("맑음") : { mIconResource = R.drawable.ic_sun; break; }
            case ("구름 조금") : { mIconResource = R.drawable.ic_cloudy; break; }
            case ("구름 많음") : { mIconResource = R.drawable.ic_verycloudy; break; }
            case ("흐림") : { mIconResource = R.drawable.ic_blur; break; }
            case ("비") : { mIconResource = R.drawable.ic_rain; break; }
            case ("눈/비") : { mIconResource = R.drawable.ic_snow; break; }
            case ("눈") : { mIconResource = R.drawable.ic_snow; break; }
        }

        /* ImageView */
        Glide.with(mContext).load(mIconResource).into(mImageView);
    }

    /* Custom RecyclerView Class */
    public static class MyWeatherData
    {
        /* String */
        private String mExceptionTime = null;
        private String mExceptionState = null;
        private String mExceptionLowTemp = null;
        private String mExceptionHighTemp = null;
        private String mExceptionRain = null;

        /* MyWeatherData */
        public MyWeatherData(String mExceptionTime, String mExceptionState, String mExceptionLowTemp, String mExceptionHighTemp, String mExceptionRain)
        {
            this.mExceptionTime = mExceptionTime;
            this.mExceptionState = mExceptionState;
            this.mExceptionLowTemp = mExceptionLowTemp;
            this.mExceptionHighTemp = mExceptionHighTemp;
            this.mExceptionRain = mExceptionRain;
        }
    }
}
