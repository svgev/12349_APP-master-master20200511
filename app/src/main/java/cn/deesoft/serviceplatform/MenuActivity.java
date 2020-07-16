package cn.deesoft.serviceplatform;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import Util.MyConnection;
import Util.TokenData;
import cn.deesoft.serviceplatform.ServiceObjectActivity;


import com.allenliu.versionchecklib.callback.APKDownloadListener;
import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.bumptech.glide.Glide;
import com.mylhyl.circledialog.CircleDialog;
import android.support.v4.app.DialogFragment;
import com.yanzhenjie.permission.AndPermission;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import Model.ResultInfo;
import Util.ActivityManager;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.UrlData;

/**
 *
 */
public class MenuActivity extends Activity implements View.OnClickListener{
    BootCompletedReceiver MyBroadCastReciever;
    private LocationManager lm;//【位置管理】
    private Button btnService;
    private Button btnWorkOrder;
    private TextView txtName;
    private SharedPreferences sp;
    private Dialog mWeiboDialog;
    private ImageView imgPhoto;
    private DownloadBuilder builder;

    private double percentVisionCode;
    private String txtServiceButton;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        txtServiceButton="服务老人";
        Intent intentStart = getIntent();
        try{
        if(StartServiceActivity.hasStarted){
            txtServiceButton="结束服务";
            Toast.makeText(MenuActivity.this, "正在服务", Toast.LENGTH_SHORT).show();
        }else{
            txtServiceButton="服务老人";
            Toast.makeText(MenuActivity.this, "当前没有服务", Toast.LENGTH_SHORT).show();
        }}catch (Exception e){
            StartServiceActivity.hasStarted=false;
            txtServiceButton="服务老人";
        }

        //util类，存下当前页面，便于销毁
        ActivityManager.getInstance().addActivity(this);


        //控件初始化
        btnService = findViewById(R.id.btnService);
        txtName = findViewById(R.id.txtName);
        btnWorkOrder = findViewById(R.id.btnWorkOrder);
        imgPhoto = findViewById(R.id.imgPhoto);
        btnService.setText(txtServiceButton);

        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        txtName.setText(sp.getString("name", ""));
        //填充助老员头像
        Glide.with(MenuActivity.this).load(sp.getString("photo", "")).into(imgPhoto);

        percentVisionCode = getVersionCode(this);
        //Toast.makeText(this, "当前版本为" + percentVisionCode, Toast.LENGTH_LONG).show();
        //检查当前版本是否为最新版本
        checkVersion();




        try {
            //开始服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //android8.0以上通过startForegroundService启动service
                if(!UrlData.getLocationServiceStarted()){
                    Log.e("Restart LocationService","重启定位服务");
                startForegroundService(new Intent(this, LocalService.class));
                startService(new Intent(this, RomoteService.class));}else{
                    Log.e("Already started","检测到已开启定位服务");
                }
            } else {
                if(!UrlData.getLocationServiceStarted()) {
                    Log.e("Restart LocationService","重启定位服务");
                    startService(new Intent(this, LocalService.class));
                    startService(new Intent(this, RomoteService.class));
                }else{
                    Log.e("Already started","检测到已开启定位服务");
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        MyBroadCastReciever = new BootCompletedReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //为BroadcastReceiver指定action，使之用于接收同action的广播
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        //注册广播
         registerReceiver(MyBroadCastReciever, intentFilter);

        //判断是否开启gps
        lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务，就直接跳转，不用新建服务
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
//                        Toast.makeText(getActivity(), "有权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "系统检测到未开启GPS定位服务", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
        }
        btnService.setOnClickListener(this);
        imgPhoto.setOnClickListener(this);
        btnWorkOrder.setOnClickListener(this);
    }


    /**
     * 用户修改头像后修改SharedPreferences所存photo地址，重新加载头像
     */
    @Override
    protected void onRestart(){
        super.onRestart();
        Glide.with(MenuActivity.this).load(sp.getString("photo", "")).into(imgPhoto);
    }




    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.imgPhoto:
                if (HttpUtil.isFastClick()) {
                    Intent intent = new Intent(MenuActivity.this, HelperInfoActivity.class);
                    MenuActivity.this.startActivity(intent);
                    if(Build.VERSION.SDK_INT>27)
                    {
                        overridePendingTransition(0, 0);
                    }
                }
                break;
            case R.id.btnService:
                mWeiboDialog = DialogUtil.createLoadingDialog(MenuActivity.this, "加载中...");
                if (HttpUtil.isFastClick()) {
                    new Thread() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            String response;
                            String url = UrlData.getUrlYy()+"/api/AndroidApi/IsStartService?IdentityID=" + sp.getString("identityId", "");
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
                                        Log.e("GetHelperInfo",response);
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
                break;
            case R.id.btnWorkOrder:
                if (HttpUtil.isFastClick()) {
                    Intent intent = new Intent();
                    intent.setClass(MenuActivity.this, WorkOrderMonthActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            DialogUtil.closeDialog(mWeiboDialog);
            ResultInfo<LinkedHashMap> resultInfo = new ResultInfo<LinkedHashMap>();
            if (msg.what == 1) {
                try {
                    String result = msg.obj.toString();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(result);
                    resultInfo = mapper.readValue(node.toString(), resultInfo.getClass());
                    if (resultInfo.Success == true) {
                        Intent intent = new Intent(MenuActivity.this, EndServiceActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("ID", (int) resultInfo.Data.get("ID"));
                        bundle.putString("OlderName", resultInfo.Data.get("OldPeopleName").toString());
                        bundle.putString("Content", resultInfo.Data.get("WorkContent").toString());
                        bundle.putString("StartTime", resultInfo.Data.get("StartTime").toString());
                        bundle.putString("ServiceID", resultInfo.Data.get("ServiceID").toString());
                        bundle.putString("OlderID", ((LinkedHashMap) resultInfo.Data.get("OldPeople")).get("ID").toString());
                        if (((LinkedHashMap) resultInfo.Data.get("OldPeople")).get("Photo") != null) {
                            bundle.putString("Photo", ((LinkedHashMap) resultInfo.Data.get("OldPeople")).get("Photo").toString());
                        } else {
                            bundle.putString("Photo", "null");
                        }
                        bundle.putString("Birthday", ((LinkedHashMap) resultInfo.Data.get("OldPeople")).get("Birthday").toString());
                        intent.putExtras(bundle);
                        MenuActivity.this.startActivity(intent);
                    } else {
                        //如果当前没有服务则跳转服务对象列表
                        Intent intent = new Intent(MenuActivity.this, ServiceObjectActivity.class);
                        MenuActivity.this.startActivity(intent);
                    }
                } catch (Exception ex) {
                    Log.i("result2", ex.getMessage());
                }
            }
            if(msg.what==2)
            {
                Toast.makeText(MenuActivity.this,msg.obj.toString(),Toast.LENGTH_LONG).show();
            }
            if(msg.what==3)
            {
                Toast.makeText(MenuActivity.this,"未连接到网络",Toast.LENGTH_LONG).show();
            }
            if(msg.what==4)
            {
                Toast.makeText(MenuActivity.this,"验证过期",Toast.LENGTH_LONG).show();
            }
            if (msg.what == 5) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(msg.obj.toString());
                    if (node.get("Success").toString().equals("true")) {
                        String downloadUrl = node.get("Data").toString().replace("\"", "");
                        //updateUtil.ShowDialog(content,downloadUrl);

                        builder=AllenVersionChecker
                                .getInstance()
                                .downloadOnly(crateUIData(downloadUrl));
                        //builder.setSilentDownload(true);
                        builder.setShowDownloadFailDialog(true);
                        builder.setApkName(downloadUrl.substring(downloadUrl.lastIndexOf("/"),downloadUrl.lastIndexOf(".")));
                        builder.setDownloadAPKPath("/storage/emulated/0/Download");
                        builder.setApkDownloadListener(new APKDownloadListener() {
                            @Override
                            public void onDownloading(int progress) {
Log.i("ce","downloading");
                            }

                            @Override
                            public void onDownloadSuccess(File file) {
                                Log.i("ce","onDownloadSuccess");
                            }

                            @Override
                            public void onDownloadFail() {
                                Log.i("ce","onDownloadFail");
                            }
                        });
                        builder.executeMission(MenuActivity.this);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if(msg.what==6){
                Toast.makeText(MenuActivity.this,"获取版本号失败",Toast.LENGTH_LONG).show();
            }
            else{

            }

        }
    };


    private UIData crateUIData(String url) {
        UIData uiData = UIData.create();
        uiData.setTitle("提示");
        uiData.setDownloadUrl(url);
        uiData.setContent("\n" +
                "检测到最新版本，点击更新!");
        return uiData;
    }


    /**
     * 检查是否需要更新
     */
    private void checkVersion() {
        new Thread() {
            @Override
            public void run() {
                String url = UrlData.getUrlYy()+"/api/AndroidApi/GetVersionCode?PercentVersionCode=" + percentVisionCode;
                Message msg = new Message();
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    httpGet.addHeader("auth", TokenData.getTokenValue());
                    HttpResponse execute = httpClient.execute(httpGet);
                    if (execute.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = execute.getEntity();
                        String response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                        msg.what = 5;
                        msg.obj = response;
                        handler.sendMessage(msg);
                    } else {
                        msg.what = 6;
                        handler.sendMessage(msg);
                    }
                } catch (Exception ex) {
                    msg.what = 6;
                    handler.sendMessage(msg);
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    public double getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }


}
