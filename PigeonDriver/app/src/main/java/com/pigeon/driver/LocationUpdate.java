package com.pigeon.driver;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Chalitta Khampachua on 17-Jul-17.
 */

public class LocationUpdate {

    private String TAG = "MyLocation";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private final static int DISTANCE_UPDATES = 0;
    private final static int TIME_UPDATES = 0;
    private LocationManager locationManager;
    public Context context;
    private LocationListener locationListener;
    private LocationCallbacks callbacks;


    public interface LocationCallbacks{
        public void onLocationDone(LocationValue locationValue);
    }

    public LocationUpdate(Context context, LocationCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        InitializeGetLocation();
    }

    public void InitializeGetLocation() {
        //Log.d(TAG, "*** Initialize Get Location ***");
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GetLocation(context, callbacks);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //Marshmallow API 23
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, locationListener);
            }
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, locationListener);
        }
    }

    public class GetLocation implements LocationListener {

        private final Context mContext;
        private LocationCallbacks mCallback;

        public GetLocation(Context mContext, LocationCallbacks mCallback) {
//            checkOpenLocation();
            this.mContext = mContext;
            this.mCallback = callbacks;
        }

        @Override
        public void onLocationChanged(android.location.Location location) {
            LocationValue locationValue = new LocationValue();
            locationValue.setLatitude(location.getLatitude());
            locationValue.setLongitude(location.getLongitude());

            //call api update server location
            //something function();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

//        public void stopGetLocation(LocationValue locationValue) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    Log.d(TAG, "Build.VERSION.SDK_INT : LOW");
//                } else {
//                    locationManager.removeUpdates(GetLocation.this);
//                }
//            }
//            else{
//                locationManager.removeUpdates(GetLocation.this);
//            }
//
//
//            if(mCallback != null){
//                mCallback.onLocationDone(locationValue);
//            }
//        }
    }

//    private void checkOpenLocation() {
//        if(!isLocationEnabled(context)) {
//            final Dialog dialog_gps = new Dialog(context);
//            dialog_gps.getWindow();
//            dialog_gps.setCanceledOnTouchOutside(false);
//            dialog_gps.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog_gps.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//            dialog_gps.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
//            dialog_gps.setContentView(R.layout.dialog_data);
//
//            TextView dialog_title = (TextView) dialog_gps.findViewById(R.id.dialog_title);
//            dialog_title.setText("GPS not enabled!");
//
//            TextView dialog_detail = (TextView) dialog_gps.findViewById(R.id.dialog_detail);
//            dialog_detail.setText("Please open your location.");
//
//
//            Button buttonOK = (Button) dialog_gps.findViewById(R.id.btnOk);
//            buttonOK.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog_gps.cancel();
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    context.startActivity(intent);
//                }
//            });
//
//            dialog_gps.show();
//        }
//    }

    private boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                Log.d(TAG, "Error check open location :" + e.getMessage());
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }

}
