package cn.deesoft.serviceplatform;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.bumptech.glide.Glide;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import Model.ResultInfo;
import Util.ActivityManager;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.MyConnection;
import Util.UrlData;

public class HelperInfoActivity extends AppCompatActivity implements BootCompletedReceiver.Message{
    BootCompletedReceiver MyBroadCastReciever;
    private Dialog mWeiboDialog;
    private SharedPreferences sp;
    private MapView mapView;
    private AMap aMap;
    private LatLng latLng;
    private Marker marker;
    private TextView txtName;
    private TextView txtPhoneNumber;
    private ImageView imgPhoto;
    private TextView txtTown;
    private TextView txtVillage;
    private Button btnQuit;
    private SimpleDateFormat df;
    private String visionName;
    private TextView txtVisionName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_info);
        ActivityManager.getInstance().addActivity(this);
        visionName=getVersionCode(this);
        mapView=findViewById(R.id.mapView);
        //重写方法（高德地图api）
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);//隐藏默认缩放控件
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.542507,119.977412),13));
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        txtVisionName=findViewById(R.id.txt_vision);
        txtVisionName.setText("当前版本："+visionName);
        txtName=findViewById(R.id.txtName);
        txtPhoneNumber=findViewById(R.id.txtPhoneNumber);
        imgPhoto=findViewById(R.id.imgPhoto);
        txtTown=findViewById(R.id.txtTown);
        txtVillage=findViewById(R.id.txtVillage);
        btnQuit=findViewById(R.id.btnQuit);
        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        MyBroadCastReciever = new BootCompletedReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //为BroadcastReceiver指定action，使之用于接收同action的广播
        intentFilter.addAction("location");
        //注册广播
        registerReceiver(MyBroadCastReciever, intentFilter);
        //因为这里需要注入Message，所以不能在AndroidManifest文件中静态注册广播接收器
        MyBroadCastReciever.setMessage(this);

        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HttpUtil.isFastClick()) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.commit();
                    Intent intent = new Intent();
                    intent.setClass(HelperInfoActivity.this, MainActivity.class);
                    startActivity(intent);
                    ActivityManager.getInstance().exit();
                }
            }
        });
        mWeiboDialog = DialogUtil.createLoadingDialog(HelperInfoActivity.this, "加载中...");
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                String url = UrlData.getUrlYy()+ "/api/AndroidApi/GetHelperInfo?IdentityID="+sp.getString("identityId","");
                String response="";
                 try{
                    response= MyConnection.setMyHttpClient(url);
                    if (response!=null) {
                        if(response.equals("请求错误")||response.equals("未授权")||response.equals("禁止访问")||response.equals("文件未找到")||response.equals("未知错误")||response.equals("未连接到网络")) {
                            msg.what = 2;
                            msg.obj = response;//返回错误原因
                            Log.e("GetHelperInfo",response);
                        }
                        if(response.equals("验证过期")){
                            //执行token过期的操作
                            msg.what=4;
                            msg.obj=response;
                            Log.e("GetHelperInfo","验证过期");
                        }
                        else {
                            msg.what = 1;
                            msg.obj = response;//返回正常数据
                        }
                    }else {
                        msg.what = 3;
                        Log.e("GetHelperInfo","未连接网络");
                    }
                }
                catch (Exception e) {
                    msg.what = 3;
                    Log.e("GetHelperInfo","未连接网络");
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    Handler handler=new Handler() {
        public void handleMessage(Message msg) {
            DialogUtil.closeDialog(mWeiboDialog);
            ResultInfo<LinkedHashMap> resultInfo = new ResultInfo<LinkedHashMap>();
            switch (msg.what) {
                case 1:
                    try {
                        String string = msg.obj.toString();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(string);
                        resultInfo = mapper.readValue(node.toString(), resultInfo.getClass());

                        txtName.setText(resultInfo.Data.get("Name").toString());
                        txtPhoneNumber.setText(resultInfo.Data.get("PhoneNumber").toString());
                        if (resultInfo.Data.get("Town") != null) {
                            txtTown.setText(resultInfo.Data.get("Town").toString());
                        }
                        if (resultInfo.Data.get("Village") != null) {
                            txtVillage.setText(resultInfo.Data.get("Village").toString());
                        }
                        if (resultInfo.Data.get("Photo") == null) {
                            Glide.with(HelperInfoActivity.this).load(UrlData.getUrlYy() + "/Images/nophoto.png").into(imgPhoto);
                        } else {
                            Glide.with(HelperInfoActivity.this).load(UrlData.getUrlYy() + resultInfo.Data.get("Photo").toString()).into(imgPhoto);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("photo", UrlData.getUrlYy() + resultInfo.Data.get("Photo").toString());
                            editor.commit();
                        }
                    } catch (Exception ex) {
                        Log.i("result2", ex.getMessage());
                    }break;
                case 2:
                    Toast.makeText(HelperInfoActivity.this,msg.obj.toString(),Toast.LENGTH_LONG);
                    break;
                case 3:
                    Toast.makeText(HelperInfoActivity.this,"未连接到网络",Toast.LENGTH_LONG);
                    break;
                case 4:
                    Toast.makeText(HelperInfoActivity.this, "验证过期", Toast.LENGTH_LONG).show();
                    break;
            }

        }
    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void getMsg(String str) {
        //通过实现MyReceiver.Message接口可以在这里对MyReceiver中的数据进行处理
        String[] st=str.split(",");
        latLng=new LatLng(Double.parseDouble(st[0]),Double.parseDouble(st[1]));
        if(marker!=null) {
            marker.remove();
        }
        marker=aMap.addMarker(new MarkerOptions().position(latLng).title("当前位置").snippet(df.format(new Date())));
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(st[0]),Double.parseDouble(st[1])),16));
    }

    private String getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        String code="";
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }


    @Override
    protected void onPause() {
        super.onPause();
        HelperInfoActivity.this.finish();
    }
}
