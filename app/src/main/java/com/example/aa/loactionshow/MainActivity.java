package com.example.aa.loactionshow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LocationClient locationClient;
    private TextView text;
    private MapView mv;
    private BaiduMap map;
    boolean fisrtLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());


        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        mv = (MapView) findViewById(R.id.mapview);
        map = mv.getMap();
        map.setMyLocationEnabled(true);//显示当前位置图标,要使用这个功能必须要开启
        List<String> list = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!list.isEmpty()) {
            String[] permissons = list.toArray(new String[list.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissons, 1);
        } else {
            requestLocation();
        }
    }

    private void requestLocation() {
        initlocation();
        locationClient.start();
    }

    private void initlocation() {
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setScanSpan(5000);
        locationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationClientOption.setIsNeedAddress(true);
        locationClient.setLocOption(locationClientOption);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mv.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mv.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        mv.onDestroy();
        map.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length != 0) {
                    for (int per : grantResults) {
                        if (per != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "必要的权限还是给我啊！", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(MainActivity.this, "出现未知错误！", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("经度： ").append(bdLocation.getLatitude()).append("\n").append("维度 ： ").append(bdLocation.getLongitude()).append("\n")
                    .append("定位方式：  ");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                stringBuilder.append("GPS ");

            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                stringBuilder.append("网络");
            }
            stringBuilder.append(bdLocation.getCountry()).append(bdLocation.getProvince()).append(bdLocation.getCity()).append(bdLocation.getDistrict()).append(bdLocation.getStreet());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.setText(stringBuilder);
                }
            });
            navigateTo(bdLocation);
        }

        private void navigateTo(BDLocation bdLocation) {
            if (fisrtLocation) {
                LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
                map.animateMapStatus(mapStatusUpdate);
                mapStatusUpdate = MapStatusUpdateFactory.zoomTo(16f);
                map.animateMapStatus(mapStatusUpdate);
                fisrtLocation = false;
            }
//            显示当前自己位置的图标
            MyLocationData.Builder builder = new MyLocationData.Builder();
            builder.latitude(bdLocation.getLatitude());
            builder.longitude(bdLocation.getLongitude());
            map.setMyLocationData(builder.build());

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
}
