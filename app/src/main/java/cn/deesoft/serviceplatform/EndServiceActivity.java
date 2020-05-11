package cn.deesoft.serviceplatform;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.mylhyl.circledialog.CircleDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.xc.DomElementJsonSerializer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import Model.KeyValueInfo;
import Model.ResultInfo;
import Model.ResultInfoList;
import Util.ActivityManager;
import Util.DateUtil;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.PhotoUtils;
import Util.UploadUtil;
import Util.UrlData;
import cn.deesoft.serviceplatform.Adapter.GridviewAdapter;

public class EndServiceActivity extends BaseActivity {
    private int clickTimes;
    private Dialog mWeiboDialog;
    private TextView txtOlderName;
    private TextView txtStartTime;
    private TextView txtServiceTime;
    private Button btnEndService;
    private ImageView imgPhoto;
    private GridView gridView;
    private TextView txtAge;
    private ImageView takePhoto;
    private EditText txtPhoneNumber;
    private EditText txtHealthy;
    private TextView txtNavigation;
    private TextView txtIsLiving;
    private View navigation;

    private SharedPreferences sp;
    private SharedPreferences objectSp;
    private String result;
    private GridviewAdapter mAdapter;
    private ArrayList<KeyValueInfo> list = new ArrayList<KeyValueInfo>();
    private int ID;
    private String Content;
    private List<String> selectName = new ArrayList<String>();
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private File fileUri = new File(Environment.getExternalStorageDirectory().getPath() + "/photo.jpg");
    private Uri imageUri;


    //用于定位导航
    private String guideAddress;
    private String dlngX;
    private String dlatY;

    private String trueName;
    private String olderTown;
    private String olderVillage;
    private String olderAddress;
    private String olderId;
    private Boolean isLiving;
    private Boolean apkExist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_service);
        ActivityManager.getInstance().addActivity(this);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        apkExist = checkApkExist(EndServiceActivity.this, "com.autonavi.minimap");




            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //版本判断
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 1);
                }
            }
            clickTimes=0;
        takePhoto=findViewById(R.id.takePhoto);
        txtAge=findViewById(R.id.txtAge);
        txtOlderName = findViewById(R.id.txtOlderName);
        txtServiceTime = findViewById(R.id.txtServiceTime);
        txtStartTime = findViewById(R.id.txtStartTime);
        btnEndService = findViewById(R.id.btnEndService);
        imgPhoto=findViewById(R.id.imgPhoto);
        gridView=findViewById(R.id.gridView);
        txtPhoneNumber=findViewById(R.id.txtPhoneNumber);
        txtHealthy=findViewById(R.id.txtHealthy);
        txtIsLiving=findViewById(R.id.txtIsLiving);


        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final Bundle bundle = this.getIntent().getExtras();
        ID = bundle.getInt("ID");
        String birthday=bundle.getString("Birthday");
        birthday=birthday.substring(0,birthday.indexOf("T"));
        int age= DateUtil.getAgeFromBirthTime(birthday);
        txtAge.setText(age+"岁");
        String photoUrl=bundle.getString("Photo");

        txtNavigation=findViewById(R.id.txtNavigation);
        navigation=findViewById(R.id.navigation);
        olderId=bundle.getString("OlderID");
        trueName=bundle.getString("OlderName");
        GetOlderPoint();

        if(photoUrl.equals("null"))
        {
                Glide.with(EndServiceActivity.this).load(R.mipmap.nophoto).into(imgPhoto);
        }
        else
        {
            Glide.with(EndServiceActivity.this).load(UrlData.getUrlYy()+photoUrl).into(imgPhoto);
        }
        Content=bundle.getString("Content");
        txtOlderName.setText(bundle.getString("OlderName"));

        String[] content=Content.split(",");
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
            mAdapter=new GridviewAdapter(list,content,this);
            gridView.setAdapter(mAdapter);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            txtStartTime.setText(bundle.getString("StartTime").replace("T", " "));
            double minutes = (int) ((new Date()).getTime() - sdf.parse(bundle.getString("StartTime").replace("T", " ")).getTime()) / (1000 * 60);
            txtServiceTime.setText(minutes + "分钟");
        } catch (Exception ex) {
            Toast.makeText(EndServiceActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                GridviewAdapter.ViewHolder holder = (GridviewAdapter.ViewHolder) view.getTag();
//                holder.cb.toggle();
//                GridviewAdapter.getIsSelected().put(i, holder.cb.isChecked());
//
//            }
//        });

        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("ID",olderId);
                intent.putExtra("TrueName",trueName);
                intent.setClass(EndServiceActivity.this,OlderDetailActivity.class);
                EndServiceActivity.this.startActivity(intent);
            }
        });

        btnEndService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    selectName.clear();
                    for (int i = 0; i < mAdapter.getIsSelected().size(); i++) {
                        if (mAdapter.getIsSelected().get(i)) {
                            selectName.add(list.get(i).getValue());
                        }
                    }

                    if (selectName.size() == 0) {
                        Toast.makeText(EndServiceActivity.this, "至少选择一项服务！", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < selectName.size(); i++) {

                            sb.append("," + selectName.get(i));
                        }
                        result = sb.delete(0, 1).toString();
                        showMyDialog();
                    }


            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GridviewAdapter.ViewHolder holder = (GridviewAdapter.ViewHolder) view.getTag();
                holder.cb.toggle();
                GridviewAdapter.getIsSelected().put(i, holder.cb.isChecked());

            }
        });
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions(EndServiceActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, new RequestPermissionCallBack() {
                    @Override
                    public void granted() {
                        if (hasSdcard()) {
                            imageUri = Uri.fromFile(fileUri);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                //通过FileProvider创建一个content类型的Uri
                                imageUri = FileProvider.getUriForFile(EndServiceActivity.this, "cn.deesoft.serviceplatform.fileprovider", fileUri);
                            PhotoUtils.takePicture(EndServiceActivity.this, imageUri, REQUEST_IMAGE_CAPTURE);
                        } else {
                            Toast.makeText(EndServiceActivity.this, "设备没有SD卡！", Toast.LENGTH_SHORT).show();
                            Log.e("asd", "设备没有SD卡");
                        }
                    }

                    @Override
                    public void denied() {
                        Toast.makeText(EndServiceActivity.this, "部分权限获取失败，正常功能受到影响", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }


    private String compressImage;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {

                try {
                     compressImage = PhotoUtils.compressImage(Environment.getExternalStorageDirectory().getPath() + "/photo.jpg", Environment.getExternalStorageDirectory()+"/picture.jpg", 90);
                }
                catch (Exception ex)
                {}
                mWeiboDialog = DialogUtil.createLoadingDialog(EndServiceActivity.this, "照片上传中...");
                new Thread() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/photo.jpg");
                        String url = UrlData.getUrlYy()+"/api/Default/TakePhoto?WorkOrderID=" +ID;
                        if(file!=null) {
                            try {
                                String re = UploadUtil.uploadFile(new File(compressImage), url);
                                msg.what = 3;
                                msg.obj = re;
                            } catch (Exception ex) {
                                msg.what = 4;
                            }
                            handler.sendMessage(msg);
                        }else {
                            Toast.makeText(EndServiceActivity.this, "找不到上传图片", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.start();
            } catch (Exception  e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
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
                        Toast.makeText(EndServiceActivity.this, "结束服务成功!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EndServiceActivity.this, MenuActivity.class);
                        intent.putExtra("clickTimes",clickTimes);
                        StartServiceActivity.hasStarted=false;
                        clickTimes=0;
                        EndServiceActivity.this.startActivity(intent);
                        ActivityManager.getInstance().exit();
                    } else {
                        if(resultInfo.Msg.equals("没有定位信息")) {
                            new CircleDialog.Builder()
                                    .setTitle("提示")
                                    .setText("GPS信号弱，请移至空旷位置后点击开始服务按钮！")
                                    .setPositive("确定", null)
                                    .show(getSupportFragmentManager());
                        }
                        else
                        {
                            Toast.makeText(EndServiceActivity.this, "结束服务出现异常，请联系管理员!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception ex) {
                    Log.i("result2", ex.getMessage());
                }
            }
            else if(msg.what==2)
            {
                Toast.makeText(EndServiceActivity.this, "结束服务出现异常，请检查网络！", Toast.LENGTH_SHORT).show();
            }
            if (msg.what == 3) {
                try {
                    String result = msg.obj.toString();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(result);
                    resultInfo = mapper.readValue(node.toString(), resultInfo.getClass());
                    if (resultInfo.Success == true&&resultInfo.Msg.equals("Photo beyond")) {
                        Toast.makeText(EndServiceActivity.this, "服务最多只能上传四张照片！", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EndServiceActivity.this, MenuActivity.class);
                        EndServiceActivity.this.startActivity(intent);
                        ActivityManager.getInstance().exit();
                    }
                    else if (resultInfo.Success == true) {
                        Toast.makeText(EndServiceActivity.this, "上传照片成功!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EndServiceActivity.this, "服务器出错!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EndServiceActivity.this, MenuActivity.class);
                        EndServiceActivity.this.startActivity(intent);
                        ActivityManager.getInstance().exit();
                    }
                } catch (Exception ex) {
                    Log.i("result2", ex.getMessage());
                }
            }
            else if(msg.what==4)
            {
                Toast.makeText(EndServiceActivity.this, "上传照片出现异常，请检查网络！", Toast.LENGTH_SHORT).show();
            }

        }
    };
    /**
     * 检查设备是否存在SDCard的工具方法
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
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


    private void GetOlderPoint(){

        ResultInfoList<Object> list = new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();

                String url = UrlData.getUrlYy() + "/api/Default/GetOlderById?olderId="+olderId;
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
                        //Toast.makeText(EndServiceActivity.this, "地址为"+olderAddress, Toast.LENGTH_SHORT).show();

                        if((!map.get("Longitude").toString().equals("0.0"))&&(!map.get("Latitude").toString().equals("0.0"))){
                            //如果传回有效经纬度，直接标点
                            dlngX=map.get("Latitude").toString();
                            dlatY=map.get("Longitude").toString();
                            //Toast.makeText(EndServiceActivity.this, "有经纬度", Toast.LENGTH_SHORT).show();

                            if (apkExist) {
                                //Toast.makeText(EndServiceActivity.this, "检测到高德地图", Toast.LENGTH_SHORT).show();
                            } else {
                                //Toast.makeText(EndServiceActivity.this, "未检测到高德地图", Toast.LENGTH_SHORT).show();
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


                        }
                        if(map.get("Longitude").toString().equals("0.0")){
                            //Toast.makeText(EndServiceActivity.this, "无经纬度", Toast.LENGTH_SHORT).show();

                            //没有经纬度的情况
                            if(map.get("Addr").toString().equals(null)){
                                navigation.setVisibility(View.INVISIBLE);
                                Toast.makeText(EndServiceActivity.this, "地址不存在", Toast.LENGTH_SHORT).show();

                            }
                            if(!map.get("Addr").toString().equals(null)){
                                //Toast.makeText(EndServiceActivity.this, "有地址", Toast.LENGTH_SHORT).show();

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
                                navigation.setVisibility(View.GONE);
                            }
                        }

                        //没有详细地址不显示导航
                    }


                }catch (Exception  ex){

                }

            }
            else
            {
                Toast.makeText(EndServiceActivity.this, "出现未知异常", Toast.LENGTH_SHORT).show();


                txtNavigation.setVisibility(View.GONE);
            }


        }
    };


    //判断高德地图安装
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


    private void showMyDialog() {
        // 创建退出对话框
        AlertDialog.Builder isExit = new AlertDialog.Builder(this);
        // 设置对话框标题
        //isExit.setTitle("提示");
        // 设置对话框消息
        isExit.setMessage("确定要结束服务吗？");
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
                    clickTimes=clickTimes+1;
                    if (HttpUtil.isFastClick()) {

                        mWeiboDialog = DialogUtil.createLoadingDialog(EndServiceActivity.this, "加载中...");
                        new Thread() {
                            @Override
                            public void run() {
                                Message msg = new Message();
                                String url = UrlData.getUrlYy()+ "/api/Default/EndService?WorkOrderID=" +ID+"&Content="+result+"&phoneNumber="+sp.getString("phoneNumber", "")+
                                        "&oldPeoplePhoneNumber="+txtPhoneNumber.getText().toString()+"&healthy="+txtHealthy.getText().toString();
                                String urlMy = UrlData.getUrlYy()+ "/api/Default/EndService?beEnded=true&WorkOrderID=" +ID+"&Content="+result+"&phoneNumber="+sp.getString("phoneNumber", "")+
                                        "&oldPeoplePhoneNumber="+txtPhoneNumber.getText().toString()+"&healthy="+txtHealthy.getText().toString();
                                try {
                                    HttpClient httpClient = new DefaultHttpClient();
                                    HttpPost httpPost = new HttpPost(url);
                                    HttpResponse execute = httpClient.execute(httpPost);
                                    if (execute.getStatusLine().getStatusCode() == 200||clickTimes>3) {
                                        HttpEntity entity = execute.getEntity();
                                        String response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                                        msg.what = 1;
                                        msg.obj = response;
                                    }
                                } catch (Exception ex) {
                                    msg.what = 2;
                                }
                                handler.sendMessage(msg);
                            }
                        }.start();
                    }
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

}
