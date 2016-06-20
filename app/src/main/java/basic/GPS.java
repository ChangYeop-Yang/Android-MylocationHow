package basic;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.net.location.mylocationhow.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by Mari on 2015-08-28.
 */
public class GPS
{
    /* Double */
    private static double Latitude = 0.0; /* 위도 */
    private static double Longitude = 0.0; /* 경도 */

    /* Context */
    private Context mContext = null;

    public GPS(Context mContext) { this.mContext = mContext; }

    /* getMethod */
    public static final double getLatitude() { return Latitude; }
    public static final double getLongitude() { return Longitude; }

    public final void startGPS()
    {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener()
        {

            /* GPS의 좌표가 바뀔 경우 호출 되는 함수 */
            public void onLocationChanged(Location location)
            {
                // Called when a new location is found by the network location provider
                Latitude = location.getLatitude();
                Longitude = location.getLongitude();
            }
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            public void onProviderEnabled(String provider) { }
            public void onProviderDisabled(String provider) { }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    /* Create Map Address Method */
    public static final void createMapAddress(Context mContext, TextView mTextView)
    {
        /* GeoCoding */
        final Geocoder mGeocoder = new Geocoder(mContext);

        /* ArrayList */
        List<Address> mAddressList = null;

        /* Address Network Exception */
        try { mAddressList = mGeocoder.getFromLocation(GPS.getLatitude(), GPS.getLongitude(), 1); }
        catch (NumberFormatException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }

        /* TextView Setting */
        if( (mAddressList != null) && (mAddressList.size()>0) )
        {
            Address mAddress = mAddressList.get(0);
            /* String Format */
            String mAddressFormat = String.format("%s %s %s %s", mAddress.getAdminArea(), mAddress.getLocality(), mAddress.getThoroughfare(), mAddress.getFeatureName());
            mTextView.setText(mAddressFormat);
        } else { mTextView.setText("현재 주소를 찾을 수 없습니다."); }
    }

    /* Distance Method */
    public static final int calcDistance(double lat2, double lon2)
    {
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * Latitude;
        radLat2 = Rad * lat2;
        radDist = Rad * (Longitude - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        return (int)ret;
    }

    /* Create Now Location Image */
    public static final void createNowLocationMap(Context mContext, ImageView mImageView)
    {
        /* StringBuffer */
        StringBuffer mStringBuffer = new StringBuffer("https://maps.googleapis.com/maps/api/staticmap?center=");
        mStringBuffer.append(Latitude); mStringBuffer.append(","); mStringBuffer.append(Longitude); /* 위도와 경도 */
        mStringBuffer.append("&markers=color:blue|label:R|"); mStringBuffer.append(GPS.getLatitude()); mStringBuffer.append(","); mStringBuffer.append(GPS.getLongitude());
        mStringBuffer.append("%7C11211&sensor=false&zoom=15&size="); mStringBuffer.append(300); mStringBuffer.append("x"); mStringBuffer.append(300);
        mStringBuffer.append("&key="); mStringBuffer.append(mContext.getResources().getString(R.string.GOOGLE_APIKEY)); /* APIKEY */

        Glide.with(mContext).load(mStringBuffer.toString()).error(R.drawable.ic_nopage).into(mImageView);
    }
}
