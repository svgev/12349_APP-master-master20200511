package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mylhyl.circledialog.CircleDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import Model.KeyValueInfo;
import Model.ResultInfoList;
import Util.ActivityManager;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.UrlData;
import cn.deesoft.serviceplatform.Adapter.GridviewAdapter;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.bumptech.glide.Glide;


public class StartServiceActivity extends AppCompatActivity {
    private Dialog mWeiboDialog;
    private SharedPreferences sp;
    private SharedPreferences objectSp;

    private TextView txtOlderName;
    private Button btnStartService;
    private GridView gridView1;
    private TextView txtAge;
    private ImageView imgPhoto;
    private TextView txtNavigation;
    private TextView txtIsLiving;
    private View navigation;

    private String ServiceID;
    private String OlderID;
    private String ServiceName;
    private String Town;
    private String beStarted;

    private GridviewAdapter mAdapter;
    private ArrayList<KeyValueInfo> list = new ArrayList<KeyValueInfo>();
    private List<String> selectName = new ArrayList<String>();
    private String result;
    private int clickTimes;

    private String guideAddress;
    private String dlngX;
    private String dlatY;

    private String trueName;
    private String olderTown;
    private String olderVillage;
    private String olderAddress;
    public static Boolean isLiving;
    private Boolean apkExist;

    public static Boolean hasStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_service);
        ActivityManager.getInstance().addActivity(this);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        apkExist = checkApkExist(StartServiceActivity.this, "com.autonavi.minimap");

        hasStarted=false;
        isLiving=true;

        clickTimes=0;
        imgPhoto=findViewById(R.id.imgPhoto);
        txtAge=findViewById(R.id.txtAge);
        txtOlderName=findViewById(R.id.txtServiceObject);
        txtNavigation=findViewById(R.id.txtNavigation);
        txtIsLiving=findViewById(R.id.txtIsLiving);
        navigation=findViewById(R.id.navigation);
        btnStartService=findViewById(R.id.btnStartService);
        gridView1=findViewById(R.id.gridView1);

        final Bundle bundle=this.getIntent().getExtras();
        isLiving=bundle.getBoolean("IsLiving");
        OlderID=bundle.getString("OlderID");
        trueName=bundle.getString("OlderName");
        txtOlderName.setText(bundle.getString("OlderName"));
        txtAge.setText(bundle.getString("Age")+"岁");
        Town=bundle.getString("Town");

        String photoUrl=bundle.getString("Photo");
        beStarted="false";




        GetOlderPoint();

        if(photoUrl.equals("null"))
        {
            if(isLiving)
            Glide.with(StartServiceActivity.this).load(R.mipmap.nophoto).into(imgPhoto);
            else {
                txtIsLiving.setText("(过世)");
                txtIsLiving.setVisibility(View.VISIBLE);
                Glide.with(StartServiceActivity.this).load(R.mipmap.nophoto_black).into(imgPhoto);

            }
        }
        else
        {

            Glide.with(StartServiceActivity.this).load(UrlData.getUrlYy()+photoUrl).into(imgPhoto);
        }



        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        String[] checkboxText = new String[] { "洗衣", "保洁",
                "心灵慰藉", "代购物品","理发","剪指甲","洗头","干家务","修理" };
        try {
            for(int i=0;i<checkboxText.length;i++)
            {
                KeyValueInfo keyValueInfo=new KeyValueInfo();
                keyValueInfo.setKey(i+"");
                keyValueInfo.setValue(checkboxText[i]);
                list.add(keyValueInfo);
            }
            mAdapter=new GridviewAdapter(list,this);
            gridView1.setAdapter(mAdapter);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        mWeiboDialog = DialogUtil.createLoadingDialog(StartServiceActivity.this, "加载中...");
        new Thread() {
            @Override
            public void run() {
                String url = UrlData.getUrlYy()+"/api/Default/GetService";
                Message msg = new Message();
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse execute = httpClient.execute(httpGet);
                    if (execute.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = execute.getEntity();
                        String response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                        msg.what = 2;
                        msg.obj = response;
                        handler.sendMessage(msg);
                    }
                    else
                    {
                        msg.what = 3;
                        handler.sendMessage(msg);
                    }
                } catch (Exception ex) {
                    DialogUtil.closeDialog(mWeiboDialog);
                    ex.printStackTrace();
                }
            }
        }.start();
        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GridviewAdapter.ViewHolder holder = (GridviewAdapter.ViewHolder) view.getTag();
                holder.cb.toggle();
                GridviewAdapter.getIsSelected().put(i, holder.cb.isChecked());

            }
        });

        //点击头像跳转老人详细页
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("ID",OlderID);
                intent.putExtra("TrueName",trueName);
                intent.setClass(StartServiceActivity.this,OlderDetailActivity.class);
                StartServiceActivity.this.startActivity(intent);
            }
        });
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickTimes=clickTimes+1;
                if (HttpUtil.isFastClick()) {
                    selectName.clear();
                    for (int i = 0; i < mAdapter.getIsSelected().size(); i++) {
                        if (mAdapter.getIsSelected().get(i)) {
                            selectName.add(list.get(i).getValue());
                        }
                    }
                    if (selectName.size() == 0) {
                        Toast.makeText(StartServiceActivity.this,"至少选择一项服务！",Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < selectName.size(); i++) {

                            sb.append(","+selectName.get(i));
                        }
                        result=sb.delete(0,1).toString();
                    }


                    mWeiboDialog = DialogUtil.createLoadingDialog(StartServiceActivity.this, "加载中...");
                    new Thread() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            if(clickTimes==4){
                                beStarted="true";
                                clickTimes=0;
                            }else{
                                beStarted="false";
                            }
                            String url = UrlData.getUrlYy()+"/api/Default/StartService?identityID=" + sp.getString("identityId", "")+
                                    "&phoneNumber="+sp.getString("phoneNumber", "")+ "&oldPeopleID=" + OlderID + "&serviceID=" +
                                    ServiceID + "&serviceName=" + ServiceName+"&content="+result+"&town="+Town;
                            String urlMy = UrlData.getUrlYy()+"/api/Default/StartService?beStarted="+beStarted+"&identityID=" + sp.getString("identityId", "")+
                                    "&phoneNumber="+sp.getString("phoneNumber", "")+ "&oldPeopleID=" + OlderID + "&serviceID=" +
                                    ServiceID + "&serviceName=" + ServiceName+"&content="+result+"&town="+Town;
                            try {
                                HttpClient httpClient = new DefaultHttpClient();
                                HttpGet httpGet = new HttpGet(url);
                                HttpResponse execute = httpClient.execute(httpGet);
                                if (execute.getStatusLine().getStatusCode() == 200||clickTimes>3) {
                                    HttpEntity entity = execute.getEntity();
                                    String response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                                    msg.what = 101;
                                    msg.obj = response;
                                    handler.sendMessage(msg);
                                    beStarted="false";
                                } else {
                                    msg.what = 3;
                                    handler.sendMessage(msg);
                                    beStarted="false";
                                }
                            } catch (Exception ex) {
                                DialogUtil.closeDialog(mWeiboDialog);
                                ex.printStackTrace();
                                beStarted="false";
                            }
                        }
                    }.start();
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    Handler handler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            DialogUtil.closeDialog(mWeiboDialog);
            Model.ResultInfo<LinkedHashMap> list=new Model.ResultInfo<LinkedHashMap>();
            if(msg.what==101)
            {
                Intent intent=new Intent();
                intent.setClass(StartServiceActivity.this,MenuActivity.class);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node=mapper.readTree(msg.obj.toString());
                    list= mapper.readValue(node.toString(),list.getClass());
                    if(list.Success==true||clickTimes>3)
                    {
                        Toast.makeText(StartServiceActivity.this,"开始服务成功",Toast.LENGTH_LONG).show();
                        intent.putExtra("clickTimes",clickTimes);
                        hasStarted=true;
                        startActivity(intent);
                        ActivityManager.getInstance().exit();
                        clickTimes=0;
                    }
                    else
                    {
                        if(list.Msg.equals("没有定位信息")) {
                            new CircleDialog.Builder()
                                    .setTitle("提示")
                                    .setText("GPS信号弱，请移至空旷位置后点击开始服务按钮！")
                                    .setPositive("确定", null)
                                    .show(getSupportFragmentManager());
                        }
                        else {
                            Toast.makeText(StartServiceActivity.this, list.Msg, Toast.LENGTH_LONG).show();
                        }
                    }

                }
                catch(Exception ex)
                {
                    Toast.makeText(StartServiceActivity.this,"出现未知异常，请联系管理员!",Toast.LENGTH_LONG).show();
                }
            }
            else if(msg.what==2)
            {
                try{
                    ObjectMapper mapper=new ObjectMapper();
                    JsonNode node=mapper.readTree(msg.obj.toString());
                    ServiceID=node.get("Data").get("ID").toString();
                    ServiceName=node.get("Data").get("Name").toString().replace("\"","");
                }
                catch(Exception ex)
                {
                    Toast.makeText(StartServiceActivity.this,"出现未知异常，请联系管理员!！",Toast.LENGTH_LONG).show();

                }
            }
            else
            {
                Toast.makeText(StartServiceActivity.this,"出现未知异常，请联系管理员!",Toast.LENGTH_LONG).show();
            }


        }
    };


    private void GetOlderPoint(){

        ResultInfoList<Object> list = new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                String ID=OlderID;

                String url = UrlData.getUrlYy() + "/api/Default/GetOlderById?olderId="+ID;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse execute = httpClient.execute(httpGet);
                    if (execute.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = execute.getEntity();
                        String response = EntityUtils.toString(entity);   //将entity当中的数据转换为字符串
                        msg.what = 5;
                        msg.obj = response;
                        pointHandler.sendMessage(msg);
                    } else {
                        msg.what = 6;
                        pointHandler.sendMessage(msg);
                    }
                } catch (Exception ex) {
                    DialogUtil.closeDialog(mWeiboDialog);
                }
            }
        }.start();
    }

    Handler pointHandler=new Handler()
    {
        ResultInfoList<Object> pointList = new ResultInfoList<Object>();

        @Override
        public void handleMessage(Message msg)
        {
            DialogUtil.closeDialog(mWeiboDialog);
            Model.ResultInfo<LinkedHashMap> list=new Model.ResultInfo<LinkedHashMap>();
            if(msg.what==5)
            {
                try {
                    DialogUtil.closeDialog(mWeiboDialog);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(msg.obj.toString());
                    pointList = mapper.readValue(node.toString(), new TypeReference<ResultInfoList<LinkedHashMap>>() {
                    });
                    for (LinkedHashMap map : (LinkedHashMap[]) pointList.Data) {

                        olderTown = map.get("Town").toString();
                        olderVillage = map.get("Village").toString();
                        olderAddress=map.get("Addr").toString();

                        if((!map.get("Longitude").toString().equals("0.0"))&&(!map.get("Latitude").toString().equals("0.0"))){
                            //如果传回有效经纬度，直接标点
                            dlngX=map.get("Latitude").toString();
                            dlatY=map.get("Longitude").toString();
                            //Toast.makeText(StartServiceActivity.this, "有经纬度", Toast.LENGTH_SHORT).show();

                            if (apkExist) {
                                //Toast.makeText(StartServiceActivity.this, "检测到高德地图", Toast.LENGTH_SHORT).show();
                            } else {
                                //Toast.makeText(StartServiceActivity.this, "未检测到高德地图", Toast.LENGTH_SHORT).show();
                            }
                            navigation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(apkExist){
                                        openAppToGuide();
                                    }else {
                                        openBrowserToGuide();
                                    }
                                }
                            });
                            navigation.setVisibility(View.VISIBLE);


                        }
                        if(map.get("Longitude").toString().equals("0.0")){
                            //Toast.makeText(StartServiceActivity.this, "无经纬度", Toast.LENGTH_SHORT).show();

                            //没有经纬度的情况
                            if(map.get("Addr")==null){
                                navigation.setVisibility(View.INVISIBLE);
                                //Toast.makeText(StartServiceActivity.this, "地址不存在", Toast.LENGTH_SHORT).show();

                            }
                            if(map.get("Addr")!=null){
                                //Toast.makeText(StartServiceActivity.this, "有地址", Toast.LENGTH_SHORT).show();
                                navigation.setVisibility(View.VISIBLE);
                                olderAddress = map.get("Addr").toString()+" ?";
                                guideAddress=map.get("Addr").toString();
                                ResultInfoList<Object> addrList = new ResultInfoList<Object>();
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
                                            navigation.setVisibility(View.VISIBLE);
                                            navigation.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(apkExist){
                                                        openAppToGuide();
                                                    }else {
                                                        openBrowserToGuide();
                                                    }
                                                }
                                            });

                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }.start();}
                            else{
                                navigation.setVisibility(View.INVISIBLE);
                            }
                        }

                        //没有详细地址不显示导航
                    }


                }catch (Exception  ex){

                }

            }
            else
            {
                navigation.setVisibility(View.GONE);
            }

        }
    };

    private boolean checkApkExist(Context context, String packageName) { //判断是否已安装高德地图
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

    private void openAppToGuide() { //打开高德App进行导航
        try {
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

}
