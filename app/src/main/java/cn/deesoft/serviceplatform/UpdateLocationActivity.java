package cn.deesoft.serviceplatform;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import net.coobird.thumbnailator.resizers.AbstractResizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import Model.ResultInfo;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.UrlData;

public class UpdateLocationActivity extends AppCompatActivity implements  LocationSource, AMapLocationListener
    {
        private TextView txtLongitude;
        private TextView txtLatitude;
        private Button btnUpdateLocation;
        private String ID;
        private Double longitude;
        private Double latitude;
    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器
    private MapView mapView;
    private AMap aMap;
   // private MapContainer map_container;
    private LatLng latLng;
    private Marker marker;
   // private ScrollView scrollView;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_location);
        Intent intent = getIntent();
        ID = intent.getStringExtra("ID");
        txtLatitude=findViewById(R.id.txtLatitude);
        txtLongitude=findViewById(R.id.txtLongitude);
        mapView = findViewById(R.id.mapView);
        btnUpdateLocation=findViewById(R.id.btnUseLocation);
        //重写方法（高德地图api）
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        //map_container = (MapContainer) findViewById(R.id.map_container);
        //scrollView = (ScrollView) findViewById(R.id.myScrollView);
        //map_container.setScrollView(scrollView);
        aMap.getUiSettings().setZoomControlsEnabled(false);//隐藏默认缩放控件
        //设置默认中心点
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.542507, 119.977412), 13));
        // 设置定位监听
        aMap.setLocationSource((LocationSource) this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        initLoc();
        btnUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyDialog();
            }
        });

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            mapView.onSaveInstanceState(outState);
        }

    private void initLoc(){
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener((AMapLocationListener) this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }


    //定位回调函数
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
                    //获取经纬度
                    longitude=amapLocation.getLongitude();
                    latitude=amapLocation.getLatitude();
                    //显示经纬度
                    txtLongitude.setText(Double.toString(amapLocation.getLongitude()));
                    txtLatitude.setText(Double.toString(amapLocation.getLatitude()));
                    //添加图钉
                    setMyMarker(amapLocation.getLatitude(),amapLocation.getLongitude());
                    //获取定位信息
//                    StringBuffer buffer = new StringBuffer();
//                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
//                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }
        private void setMyMarker(double dlngX, double dlatY){ //根据经纬度标点
            Double lngX =dlngX;
            Double latY = dlatY;
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(latY, lngX));
            latLng = new LatLng(latY, lngX);
            if (marker != null) {
                marker.remove();
            }
            marker = aMap.addMarker(new MarkerOptions().position(latLng).title("当前位置"));
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latY, lngX), 16));
        }
        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            mListener = onLocationChangedListener;
        }
        @Override
        public void deactivate() {
            mListener = null;
        }

        private void showMyDialog() {
            // 创建退出对话框
            AlertDialog.Builder isExit = new AlertDialog.Builder(this);
            // 设置对话框标题
            //isExit.setTitle("提示");
            // 设置对话框消息
            isExit.setMessage("确定要使用当前位置吗？");
            // 添加选择按钮并注册监听
            isExit.setPositiveButton("确定", listener);
            isExit.setNegativeButton("取消", listener);
            // 显示对话框
            isExit.show();
        }


        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                        new Thread() {
                            @Override
                            public void run() {
                                String url = UrlData.getUrl()+"/api/Default/LocationUpdate?ID=" + ID + "&Latitude=" +Double.toString(latitude)+"&Longitude="+Double.toString(longitude) ;
                                Message msg = new Message();
                                try {
                                    HttpClient httpClient = new DefaultHttpClient();
                                    HttpGet httpGet = new HttpGet(url);
                                    HttpResponse execute = httpClient.execute(httpGet);
                                    if (execute.getStatusLine().getStatusCode() == 200) {
                                        HttpEntity entity = execute.getEntity();
                                        String response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                                        msg.what = 1;
                                        msg.obj = response;
                                        handler.sendMessage(msg);
                                    } else {
                                        msg.what = 2;
                                        handler.sendMessage(msg);
                                    }
                                } catch (Exception ex) {
                                    msg.what = 3;
                                    handler.sendMessage(msg);
                                    ex.printStackTrace();
                                }
                            }
                        }.start();
//                        Intent intent=new Intent();
//                        Bundle bundle=new Bundle();
//                        bundle.putString("Longitude",longitude);
//                        bundle.putString("Latitude",latitude);
//                        intent.putExtras(bundle);
//                        setResult(1,intent);
//                        finish();
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                        break;
                    default:
                        break;
                }
            }
        };



        Handler handler=new Handler()
        {
            public void handleMessage(Message msg)
            {
                ResultInfo<LinkedHashMap> result=new ResultInfo<>();
                if(msg.what==1)
                {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node=mapper.readTree(msg.obj.toString());
                        result=mapper.readValue(node.toString(),result.getClass());
                        if(result.Success==true)
                        {
                            Toast.makeText(UpdateLocationActivity.this, "定位更新成功", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(UpdateLocationActivity.this, result.Msg, Toast.LENGTH_LONG).show();
                        }
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                        Toast.makeText(UpdateLocationActivity.this, "未知错误", Toast.LENGTH_LONG).show();
                    }
                }
                else if(msg.what==3)
                {
                    Toast.makeText(UpdateLocationActivity.this,"未连接到网络！",Toast.LENGTH_LONG).show();
                }
            }
        };


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    this.finish();  // back button
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
