package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
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

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

import Model.Helper;
import Model.ResultInfoList;
import Util.DialogUtil;
import Util.UrlData;

public class HelperDetailActivity extends AppCompatActivity {

    private Dialog mWeiboDialog;


    private String helperName;
    private String helperId;
    private String sex;
    private int ID;
    private String identityId;
    private String phoneNumber;
    private String town;
    private String village;
    private String addr;
    private String addrLocation;
    private String[] idList;

    private MapView mapView;
    private AMap aMap;
    private LatLng latLng;
    private Marker marker;
    private TextView txtHelperName;
    private TextView txtSex;
    private TextView txtHelperId;
    private TextView txtIdentityId;
    private TextView txtPhoneNumber;
    private TextView txtTown;
    private TextView txtVillage;
    private TextView txtAddr;
    private Button editButton;
    private ImageView imgPhoto;
    private SimpleDateFormat df;

    private MapContainer map_container;
    private ScrollView scrollView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_detail);

        mWeiboDialog = DialogUtil.createLoadingDialog(HelperDetailActivity.this, "加载中...");


        Intent intent = getIntent();
        phoneNumber="";
        sex="";
        identityId="";
        addr="";
        helperId = intent.getStringExtra("helperId");
        idList=helperId.split("[：]");
        ID = Integer.parseInt(idList[1]);
        imgPhoto = findViewById(R.id.imgPhoto);
        Glide.with(HelperDetailActivity.this).load(R.mipmap.nophoto2).into(imgPhoto);
        mapView = findViewById(R.id.mapView);

        //重写方法（高德地图api）
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);//隐藏默认缩放控件
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.542507, 119.977412), 13));
        latLng = new LatLng(30.542507, 119.977412);



        editButton=findViewById(R.id.edit_button);



        initHelper();

        /*
        mapView=findViewById(R.id.mapView);
        //重写方法（高德地图api）
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);//隐藏默认缩放控件
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.542507,119.977412),13));

        imgPhoto = findViewById(R.id.imgPhoto);
        Glide.with(HelperDetailActivity.this).load(R.mipmap.nophoto).into(imgPhoto);*/


        scrollView = (ScrollView) findViewById(R.id.myScrollView);
        map_container = (MapContainer) findViewById(R.id.map_container);
        map_container.setScrollView(scrollView);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelperDetailActivity.this, HelperEditActivity.class);
                intent.putExtra("ID",idList[1]);
                intent.putExtra("Name",helperName);
                intent.putExtra("IdentityId",identityId);
                intent.putExtra("PhoneNumber",phoneNumber);
                intent.putExtra("Town",town);
                intent.putExtra("Village",village);
                intent.putExtra("Addr",addr);
                intent.putExtra("Sex",sex);
                HelperDetailActivity.this.startActivity(intent);
            }
        });
    }

    private void initHelper() {//获取第一页数据
        ResultInfoList<Object> list = new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();

                String url = UrlData.getUrl() + "/api/Default/GetHelperById?helperId=" + ID;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse execute = httpClient.execute(httpGet);
                    if (execute.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = execute.getEntity();
                        String response = EntityUtils.toString(entity);   //将entity当中的数据转换为字符串
                        msg.what = 1;
                        msg.obj = response;
                        handler.sendMessage(msg);
                    } else {
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                } catch (Exception ex) {
                    DialogUtil.closeDialog(mWeiboDialog);
                }
            }
        }.start();
        handler.sendEmptyMessageDelayed(1, 100);
    }

    private Handler handler = new Handler() {
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

                            //txtSex=findViewById(R.id.txtSex);
                            txtHelperName = findViewById(R.id.txtHelperName);
                            txtHelperId = findViewById(R.id.txtHelperId);
                            txtPhoneNumber = findViewById(R.id.txtPhoneNumber);
                            txtTown = findViewById(R.id.txtTown);
                            txtVillage = findViewById(R.id.txtVillage);
                            txtIdentityId = findViewById(R.id.txtIdentityId);
                            txtAddr = findViewById(R.id.txtAddr);

                            if (map.get("Name") != null) {
                                helperName = map.get("Name").toString();
                            }
                            if (map.get("Town") != null) {
                                town = map.get("Town").toString();
                            }else {
                                town="";
                            }
                            if (map.get("Village") != null) {
                                village = map.get("Village").toString();
                            }else{
                                village="";
                            }
                            if (map.get("PhoneNumber")!=null) {
                                phoneNumber = map.get("PhoneNumber").toString();
                            }
                            if (map.get("IdentityID") != null) {
                                identityId = map.get("IdentityID").toString();
                            }
                            if (map.get("Addr") != null) {
                                addr = map.get("Addr").toString();
                            }else {
                                addr = "";
                            }
                            if (map.get("Sex") != null) {
                                sex = map.get("Sex").toString();
                            }


                            txtHelperName.setText(helperName);
                            txtHelperId.setText(helperId);
                            //txtSex.setText("性别： "+sex);
                            txtPhoneNumber.setText("联系电话： " + phoneNumber);
                            txtTown.setText("乡镇： " + town);
                            txtVillage.setText("街道： " + village);
                            txtAddr.setText("地址：" + addr);
                            txtIdentityId.setText("身份证号：" + identityId);
                            getLocationFromApi();

                        }
                    } catch (Exception ex) {
                    }

            }
        }
    };



    private void getLocationFromApi(){
        ResultInfoList<Object> list = new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();

                String totalAddress="浙江省湖州市德清县"+town+village+addr;
                String url = "http://restapi.amap.com/v3/geocode/geo?key=389880a06e3f893ea46036f030c94700&s=rsv3&city=35&address="+totalAddress;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse execute = httpClient.execute(httpGet);
                    HttpEntity entity = execute.getEntity();
                    String response = EntityUtils.toString(entity);
                    com.alibaba.fastjson.JSONObject object= com.alibaba.fastjson.JSONObject.parseObject(response);
                    JSONArray geocodes = object.getJSONArray("geocodes");
                    JSONObject trueAddress = geocodes.getJSONObject(0);
                    String location = trueAddress.getString("location");
                    String slngX = location.split(",")[0];
                    String slatY = location.split(",")[1];
                    Double lngX=Double.parseDouble(slngX);
                    Double latY=Double.parseDouble(slatY);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(latY, lngX));
                    latLng = new LatLng(latY, lngX);
                    if (marker != null) {
                        marker.remove();
                    }
                    marker = aMap.addMarker(new MarkerOptions().position(latLng).title("当前位置"));
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latY, lngX), 16));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
        handler.sendEmptyMessageDelayed(1, 100);
    }


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