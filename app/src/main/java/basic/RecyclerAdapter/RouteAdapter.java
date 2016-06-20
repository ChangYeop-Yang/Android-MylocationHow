package basic.RecyclerAdapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.net.location.mylocationhow.BaseActivity;
import com.net.location.mylocationhow.R;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import basic.Vibrate;

/**
 * Created by Mari on 2015-10-08.
 */
public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder>
{
    /* Context */
    private Context mContext = null;

    /* ArrayList */
    private ArrayList<MyRouteData> mRouteDataArrayList = new ArrayList<MyRouteData>(10);

    /* MapView */
    private MapView mMapView = null;

    /* MapPolyLine */
    private MapPolyline mMapPolyline = null;

    /* RouteAdapter */
    private RouteAdapter mRouteAdapter = this;

    /* RouteAdapter */
    public RouteAdapter(Context mContext, ArrayList<MyRouteData> mRouteDataArrayList, MapView mMapView, MapPolyline mMapPolyline)
    { this.mContext = mContext; this.mRouteDataArrayList = mRouteDataArrayList; this.mMapView = mMapView; this.mMapPolyline = mMapPolyline; }

    @Override public int getItemCount() { return mRouteDataArrayList.size(); }
    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_route_cardview, parent, false); return new ViewHolder(mView); }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        /* TextView */
        holder.mAddressTextView.setText(mRouteDataArrayList.get(position).mAddress);
        holder.mDateTextView.setText(mRouteDataArrayList.get(position).mData);

        /* ImageButton */
        setImageButton(holder.mDrawingButton, 0, position);
        setImageButton(holder.mDeleteButton, 1, position);
    }

    /* Setting ImageButton Method */
    private final void setImageButton(ImageButton mImageButton, final int mSwitch, final int mPosition)
    {
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 진동 */
                Vibrate.Vibrator(mContext);

                switch (mSwitch) {
                    /* Drawing Button */
                    case (0): { createArrayListSerialize(mRouteDataArrayList.get(mPosition).mPath); break; }
                    /* Delete Button */
                    case (1):
                    {
                         /* SQLite 관련 변수 */
                        BaseActivity.dbHelper mHelper = null;
                        SQLiteDatabase mDataBase = null;

                        /* SQLite */
                        try /* 읽기/쓰기 모드로 데이터베이스를 오픈 */
                        {
                            mHelper = new BaseActivity.dbHelper(mContext);
                            mDataBase = mHelper.getWritableDatabase();
                        }
                        catch(SQLiteException ex) /* 읽기 전용 모드로 데이터베이스를 오픈 */
                        {
                            mDataBase = mHelper.getReadableDatabase();
                            Toast.makeText(mContext, "SQLite 문제가 발생 하였습니다.", Toast.LENGTH_SHORT).show();
                        }

                        /* DataBase */
                        mDataBase.execSQL("DELETE FROM map_db WHERE _id = '" + mRouteDataArrayList.get(mPosition).mID + "'");
                        new File(mRouteDataArrayList.get(mPosition).mPath).delete();

                        /* Adapter Reroad */
                        mRouteDataArrayList.remove(mPosition);
                        mRouteAdapter.notifyItemRemoved(mPosition);
                        mRouteAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
    }

    /* Create ArrayListSerialize Method */
    private void createArrayListSerialize(String mPath)
    {
        /* File */
        final File mFile = new File("sdcard/MyLocationHow/Map/");

        /* Exists */
        if (!mFile.exists()) { Toast.makeText(mContext, "해당 폴더가 존재하지 않습니다.", Toast.LENGTH_SHORT).show(); return; }

        /* File */
        final File mPathFile = new File(mPath);

        try
        {
                    /* ObjectInputStream */
            ObjectInputStream mObjectInput = new ObjectInputStream(new BufferedInputStream(new FileInputStream(mPathFile)));

                    /* ArrayList */
            ArrayList<HashMap<String, Double>> mTemp = (ArrayList<HashMap<String, Double>>)mObjectInput.readObject();

            /* PolyLine */
            int mLength = mTemp.size();
            for(int count = 0; count < mLength; count++) { createPolyline(mTemp.get(count).get("MapX"), mTemp.get(count).get("MapY")); }

            /* MapMarker */
            createMarker(mTemp.get(0).get("MapX"), mTemp.get(0).get("MapY"), "출발지점", 0);
            createMarker(mTemp.get(mLength - 1).get("MapX"), mTemp.get(mLength - 1).get("MapY"), "도착지점", 1);
            mObjectInput.close();
        }
        catch (IOException e)
        {
            Toast.makeText(mContext, "경로를 파일로 읽어오는 과정에서 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            Log.e("ObjectInputStream", e.getMessage()); e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            Toast.makeText(mContext, "Serialize Error!", Toast.LENGTH_SHORT).show();
            Log.e("Serialize", e.getMessage()); e.printStackTrace();
        }
    }

    /* Create Polyline Method */
    private void createPolyline(double mMapX, double mMapY)
    {
        /* MapPolyline */
        mMapPolyline.addPoint(MapPoint.mapPointWithGeoCoord(mMapX, mMapY));
        mMapView.addPolyline(mMapPolyline);

        MapPointBounds mapPointBounds = new MapPointBounds(mMapPolyline.getMapPoints());
        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, 100));
    }

    /* Create MapMarker Method */
    private void createMarker(double mMapX, double mMapY, String mTitle, int mSwitch)
    {
        MapPOIItem mMapPOIItem = new MapPOIItem(); /* MapPOIItem 객체 생성 */
        mMapPOIItem.setItemName(mTitle); /* Maker Name */
        mMapPOIItem.setTag(1); /* Maker Tag */
        mMapPOIItem.setMapPoint(MapPoint.mapPointWithGeoCoord(mMapX, mMapY)); /* 위도와 경도 설정 */

        switch (mSwitch)
        {
            /* Blue Marker */
            case (0) : { mMapPOIItem.setMarkerType(MapPOIItem.MarkerType.BluePin); break; }
            /* Red Marker */
            case (1) : { mMapPOIItem.setMarkerType(MapPOIItem.MarkerType.RedPin); break; }
        }

        mMapView.addPOIItem(mMapPOIItem); /* 지도에 Maker 표시 */
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mMapX, mMapY), true); /* Marker 위치로 화면 이동 */
    }

    /* RouteAdapter ViewHolder */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        /* TextView */
        private TextView mAddressTextView = null;
        private TextView mDateTextView = null;

        /* ImageButton */
        private ImageButton mDeleteButton = null;
        private ImageButton mDrawingButton = null;

        private ViewHolder(View itemView)
        {
            super(itemView);

            /* TextView */
            mAddressTextView = (TextView)itemView.findViewById(R.id.CustomRouteAddress);
            mDateTextView = (TextView)itemView.findViewById(R.id.CustomRouteDate);

            /* ImageButton */
            mDeleteButton = (ImageButton)itemView.findViewById(R.id.CustomRouteDeleteButton);
            mDrawingButton = (ImageButton)itemView.findViewById(R.id.CustomRouteDrawingButton);
        }
    }

    /* MyRouteData */
    public static class MyRouteData
    {
        /* String */
        private String mAddress = null;
        private String mData = null;
        private String mPath = null;
        /* Integer */
        private int mID = 0;

        /* MyRouteData */
        public MyRouteData(String mAddress, String mData, String mPath, int mID)
        {
            this.mAddress = mAddress;
            this.mData = mData;
            this.mPath = mPath;
            this.mID = mID;
        }
    }
}