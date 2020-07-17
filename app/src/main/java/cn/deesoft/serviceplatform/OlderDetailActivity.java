package cn.deesoft.serviceplatform;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import org.codehaus.jackson.type.TypeReference;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import Model.Older;
import Model.ResultInfoList;
import Util.DateUtil;
import Util.DialogUtil;
import Util.MyConnection;
import Util.UrlData;

public class OlderDetailActivity extends AppCompatActivity {

    private Dialog mWeiboDialog;
    private String currentArea;

    private String olderName;
    private int age;
    private String olderAge;
    private String sex;
    private String olderIdentityId;
    private String olderMobile;
    private String olderTown;
    private String olderVillage;
    private String olderAddress;
    private String contactNumber;
    private String olderContactName;
    private String contactRelationship;
    private String olderLongiitude;
    private String olderLatitude;
    private String diseaseHistory;
    private String ID;
    private String guideAddress;
    private String isDisability;
    private String isPoor;
    private String isLonely;
    private String isOldAge;
    private String isEmpty;
    private String isLiving;
    private String enable;
    private String living;
    private String strState;


    private String dlngX;
    private String dlatY;

    private MapView mapView;
    private AMap aMap;
    private TextView txtName;
    private TextView txtAge;
    private TextView txtSex;
    private LatLng latLng;
    private Marker marker;
    private TextView txtOlderID;
    private TextView txtIdentityId;
    private TextView txtMobile;
    private TextView txtTown;
    private TextView txtVillage;
    private TextView txtAddress;
    private TextView txtLongitude;
    private TextView txtLatitude;
    private TextView txtContactNumber;
    private TextView txtContactName;
    private TextView txtContactRelationship;
    private TextView txtDiseaseHistory;
    private Button btnEditInfo;
    private MapContainer map_container;

    private TextView txtIsLiving;
    private TextView txtIsEnable;
    private TextView txtState;
    private ImageView imgPhoto;
    private TextView btnUpdateLocation;
    private SimpleDateFormat df;
    private ScrollView scrollView;
    Boolean apkExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_older_detail);

        mWeiboDialog = DialogUtil.createLoadingDialog(OlderDetailActivity.this, "加载中...");

        Intent intent = getIntent();
        olderName = intent.getStringExtra("TrueName");
        currentArea=intent.getStringExtra("CurrentArea");
        ID = intent.getStringExtra("ID");
        olderMobile = null;
        olderAddress=null;
        strState="";
        living="true";
        enable="true";
        btnEditInfo=findViewById(R.id.btnEditInfo);
        txtLongitude=findViewById(R.id.txtLongitude);
        txtLatitude=findViewById(R.id.txtLatitude);
        txtOlderID=findViewById(R.id.txtNumber);
        txtOlderID.setText(ID);
        txtState=findViewById(R.id.txtState);

        initOlder();

        //检测是否安装高德地图
        apkExist = checkApkExist(OlderDetailActivity.this, "com.autonavi.minimap");


        mapView = findViewById(R.id.mapView);
        //重写方法（高德地图api）
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);//隐藏默认缩放控件
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.542507, 119.977412), 13));

        latLng = new LatLng(30.542507, 119.977412);


        scrollView = (ScrollView) findViewById(R.id.myScrollView);
        map_container = (MapContainer) findViewById(R.id.map_container);
        btnUpdateLocation=findViewById(R.id.btnLocation);
        map_container.setScrollView(scrollView);

//        imgPhoto = findViewById(R.id.imgPhoto);
//        Glide.with(OlderDetailActivity.this).load(R.mipmap.nophoto2).into(imgPhoto);
//


        btnUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("ID",ID);
                intent.setClass(OlderDetailActivity.this,UpdateLocationActivity.class);
                OlderDetailActivity.this.startActivity(intent);
            }
        });
        btnEditInfo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("CurrentArea", currentArea);
                intent.putExtra("ID",ID);
                intent.putExtra("Name",olderName);
                intent.putExtra("IdentityId",olderIdentityId);
                intent.putExtra("PhoneNumber",olderMobile);
                intent.putExtra("Town",olderTown);
                intent.putExtra("Village",olderVillage);
                intent.putExtra("Addr",olderAddress);
                intent.putExtra("Sex",sex);
                intent.putExtra("Contact",olderContactName);
                intent.putExtra("ContactRelationship", contactRelationship);
                intent.putExtra("ContactNumber",contactNumber);
                intent.putExtra("DiseaseHistory",diseaseHistory);
                intent.putExtra("IsDisability",isDisability);
                intent.putExtra("IsEmpty",isEmpty);
                intent.putExtra("IsEnable",enable);
                intent.putExtra("IsLiving",living);
                intent.putExtra("IsLonely",isLonely);
                intent.putExtra("IsOldAge",isOldAge);
                intent.putExtra("IsPoor",isPoor);

                intent.setClass(OlderDetailActivity.this,OlderEditActivity.class);
                OlderDetailActivity.this.startActivity(intent);
            }
        });


        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }


    private void initOlder() {//获取第一页数据
        ResultInfoList<Object> list = new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                String response;
                String url = UrlData.getUrl() + "/api/AndroidApi/GetOlderById?olderId=" + ID;
                try{
                    response= MyConnection.setMyHttpClient(url);
                    if (response!=null) {
                        if(response.equals("请求错误")||response.equals("未授权")||response.equals("禁止访问")||response.equals("文件未找到")||response.equals("未知错误")||response.equals("未连接到网络")) {
                            msg.what = 2;
                            msg.obj = response;//返回错误原因
                        }
                        if(response.equals("验证过期")){
                            //执行token过期的操作
                            msg.what=4;
                            msg.obj=response;
                            Log.e("验证失败",response);
                        }
                        else {
                            msg.what = 1;
                            msg.obj = response;//返回正常数据
                        }
                    }else {
                        msg.what = 3;
                    }
                }
                catch (Exception e) {
                    msg.what = 3;
                }
                firstPageHandler.sendMessage(msg);
            }
        }.start();
    }

    private Handler firstPageHandler = new Handler() {
        ResultInfoList<Object> list = new ResultInfoList<Object>();

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    try {
                        DialogUtil.closeDialog(mWeiboDialog);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(msg.obj.toString());
                        list = mapper.readValue(node.toString(), new TypeReference<ResultInfoList<LinkedHashMap>>() {
                        });
                        for (LinkedHashMap map : (LinkedHashMap[]) list.Data) {
                            olderTown = map.get("Town").toString();
                            if(map.get("Village")!=(null)){
                            olderVillage = map.get("Village").toString();}
                            sex=map.get("Sex").toString();
                            olderIdentityId=map.get("IdentityID").toString();
                            isLiving=map.get("IsLiving").toString();
                            isDisability=map.get("IsDisability").toString();
                            isEmpty=map.get("IsEmpty").toString();
                            isLonely=map.get("IsLonely").toString();
                            isOldAge=map.get("IsOldAge").toString();
                            isPoor=map.get("IsPoor").toString();
                            if(isDisability.equals("true")){
                                strState=strState+"/残疾";
                            }
                            if(isPoor.equals("true")){
                                strState=strState+"/贫困";
                            }
                            if(isLonely.equals("true")){
                                strState=strState+"/孤寡";
                            }
                            if(isOldAge.equals("true")){
                                strState=strState+"/高龄";
                            }
                            if(isEmpty.equals("true")){
                                strState=strState+"/空巢";
                            }

                            String birthday = map.get("Birthday").toString();
                            birthday = birthday.substring(0, birthday.indexOf("T"));
                            age = DateUtil.getAgeFromBirthTime(birthday);

                            if(map.get("Addr")!=(null)){
                            olderAddress = map.get("Addr").toString();
                            guideAddress=map.get("Addr").toString();}else {
                                olderAddress="";
                            }
                            if(map.get("Mobile")!=(null)){
                                olderMobile = map.get("Mobile").toString();}else {
                                olderMobile="暂无";
                            }

                            if(map.get("Longitude").toString().equals("0.0")||map.get("Longitude").toString().equals("0.0")){
                                //如果没有有效经纬度传回，先根据地址查找经纬度再标点
                                getLocationFromApi();
                                txtLongitude.setText(dlngX);
                                txtLatitude.setText(dlatY);
                            }
                            if(map.get("Longtitude")==null||map.get("Latitude")==null){
                                getLocationFromApi();
                                txtLongitude.setText(dlngX);
                                txtLatitude.setText(dlatY);
                            }
                            if((!map.get("Longitude").toString().equals("0.0"))&&(!map.get("Latitude").toString().equals("0.0"))&&map.get("Longitude")!=null&&map.get("Latitude")!=null){
                                //如果传回有效经纬度，直接标点
                                dlngX=map.get("Longitude").toString();
                                dlatY=map.get("Latitude").toString();
                                setMyMarker(dlngX,dlatY);
                                //显示经纬度数值
                                txtLongitude.setText(dlngX);
                                txtLatitude.setText(dlatY);
                            }

                            //判断在世和是否退出服务
                            Boolean isEnable=Boolean.valueOf(map.get("IsEnable").toString());
                            Boolean isLiving=Boolean.valueOf(map.get("IsLiving").toString());
                            if(!isLiving){
                                Glide.with(OlderDetailActivity.this).load(R.mipmap.nophoto_black).into(imgPhoto);
                            }
                            if(map.get("ContactNumber")!=(null)){
                                contactNumber = map.get("ContactNumber").toString();}else {
                                contactNumber="";
                            }
                            if(map.get("ContactName")!=(null)){
                                olderContactName = map.get("ContactName").toString();}else {
                                olderContactName="";
                            }
                            if(map.get("ContactRelationship")!=(null)){
                                contactRelationship =map.get("ContactRelationship").toString();}else {
                                contactRelationship="";
                            }
                            if(map.get("DiseaseHistory")!=(null)){
                                diseaseHistory = map.get("DiseaseHistory").toString();}else {
                                diseaseHistory="";
                            }

                            txtName = findViewById(R.id.txtName);
                            txtAge = findViewById(R.id.txtAge);
                            txtSex=findViewById(R.id.txtSex);
                            txtMobile = findViewById(R.id.txtMobile);
                            txtIdentityId = findViewById(R.id.txtIdentityId);
                            txtTown = findViewById(R.id.txtTown);
                            txtVillage = findViewById(R.id.txtVillage);
                            txtAddress = findViewById(R.id.txtAddress);
                            txtIsEnable=findViewById(R.id.txtBeServiced);
                            txtIsLiving=findViewById(R.id.txtIsLiving);
                            txtContactName=findViewById(R.id.txtContact);
                            txtContactNumber=findViewById(R.id.txtContactMobile);
                            txtDiseaseHistory=findViewById(R.id.txtDiseaseHistory);

                            txtState.setText(strState);
                            txtName.setText(olderName);
                            txtAge.setText(String.valueOf(age)+"岁");
                            txtSex.setText(sex);
                            txtMobile.setText(olderMobile);
                            txtIdentityId.setText(olderIdentityId);
                            txtTown.setText( olderTown);
                            txtVillage.setText(olderVillage);
                            txtAddress.setText(olderAddress+" ➯");
                            txtContactName.setText(olderContactName+"("+contactRelationship+")");
                            txtContactNumber.setText(contactNumber);
                            txtDiseaseHistory.setText(diseaseHistory);
                            txtAddress.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(apkExist){
                                        openAppToGuide();
                                    }else {
                                        openBrowserToGuide();
                                    }
                                }
                            });

                            if(!isLiving){
                                txtIsLiving.setVisibility(View.VISIBLE);
                                txtIsLiving.setText("(过世)");
                                living="false";
                            }
                            if(!isEnable){
                                txtIsEnable.setText("退出服务");
                                enable="false";
                            }
                        }
                    } catch (Exception ex) {
                    }
                case 2:
                    Toast.makeText(OlderDetailActivity.this,msg.obj.toString(),Toast.LENGTH_LONG);
                    break;
                case 3:
                    Toast.makeText(OlderDetailActivity.this,"未连接到网络",Toast.LENGTH_LONG);
                    break;
                case 4:
                    Toast.makeText(OlderDetailActivity.this,"验证过期",Toast.LENGTH_LONG);
                    break;
            }
        }
    };


    private void openAppToGuide() { //打开高德App进行导航
        try {
            Log.e("x", dlngX);

            Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("amapuri://route/plan/?did=BGVIS2&dlat="+dlatY+"&dlon="+dlngX+"&dname=目的地&dev=0&t=0"));
            intent.setPackage("com.autonavi.minimap");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openBrowserToGuide() { //打开网页导航
        String url = "http://uri.amap.com/navigation?to="+dlngX+","+dlatY+"," +
                "目的地"+ "&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=0";
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


    private void getLocationFromApi() { //根据地址查询经纬度
        ResultInfoList<Object> list = new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();

                String totalAddress = "浙江省湖州市德清县" + olderTown + olderVillage + guideAddress;
                String url = "http://restapi.amap.com/v3/geocode/geo?key=389880a06e3f893ea46036f030c94700&s=rsv3&city=35&address=" + totalAddress;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse execute = httpClient.execute(httpGet);
                    HttpEntity entity = execute.getEntity();
                    String response = EntityUtils.toString(entity);
                    com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSONObject.parseObject(response);
                    JSONArray geocodes = object.getJSONArray("geocodes");
                    JSONObject trueAddress = geocodes.getJSONObject(0);
                    String location = trueAddress.getString("location");
                    dlngX = location.split(",")[0];
                    dlatY = location.split(",")[1];
                    setMyMarker(dlngX,dlatY);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();

    }

    private void setMyMarker(String dlngX, String dlatY){ //根据经纬度标点
        Double lngX = Double.parseDouble(dlngX);
        Double latY = Double.parseDouble(dlatY);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latY, lngX));
        latLng = new LatLng(latY, lngX);
        if (marker != null) {
            marker.remove();
        }
        marker = aMap.addMarker(new MarkerOptions().position(latLng).title("当前位置"));
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latY, lngX), 16));

    }


    public boolean checkApkExist(Context context, String packageName) { //判断是否已安装高德地图
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(OlderDetailActivity.this, OlderListActivity.class);
                intent.putExtra("Area",currentArea);
                OlderDetailActivity.this.startActivity(intent);
                OlderDetailActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}