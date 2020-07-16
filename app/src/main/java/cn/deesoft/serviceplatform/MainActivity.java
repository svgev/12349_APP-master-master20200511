package cn.deesoft.serviceplatform;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mylhyl.circledialog.CircleDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;

import Model.ResultInfo;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.MyConnection;
import Util.UrlData;


/*public class MainActivity extends AppCompatActivity implements BootCompletedReceiver.Message {*/
    public class MainActivity extends FragmentActivity {
    private LocationManager lm;
    private Dialog mWeiboDialog;
    private String phoneNumber;
    private String IdentityId;
    private EditText etIdentity;
    private EditText etPhoneNumber;
    private Button btnBind;
    private TextView btnAdminLog;
    private boolean AutoLogin=false;
    private SharedPreferences sp;
    private SharedPreferences spAdmin;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIdentity=findViewById(R.id.txtIdentity);
        etPhoneNumber=findViewById(R.id.txtPhoneNumber);
        btnBind=findViewById(R.id.btnBind);
        btnAdminLog=findViewById(R.id.adminLoginTxt);
//        MyConnection.getToken();
        initEvent();
        try {
            sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            spAdmin=this.getSharedPreferences("adminInfo", Context.MODE_PRIVATE);
        }
        catch (Exception ex)
        {
            Log.i("default",ex.getMessage());
        }
        if(sp.getBoolean("AutoLogin",false))
        {
            etIdentity.setText(sp.getString("identityId",""));
            etPhoneNumber.setText(sp.getString("phoneNumber",""));
            //跳转界面
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            MainActivity.this.startActivity(intent);
            finish();
        }else if(spAdmin.getBoolean("AdminAutoLogin",false)){
            Intent intentAdmin=new Intent(MainActivity.this,AdminAreaActivity.class);
            MainActivity.this.startActivity(intentAdmin);
            finish();
        }


        //绑定按钮监听方法
        btnBind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (HttpUtil.isFastClick()) {
                    IdentityId = etIdentity.getText().toString();
                    phoneNumber = etPhoneNumber.getText().toString();
                    mWeiboDialog = DialogUtil.createLoadingDialog(MainActivity.this, "登录中...");
                    new Thread() {
                        @Override
                        public void run() {
                            String url = UrlData.getUrlYy()+"/api/AndroidApi/Login?phoneNumber=" + phoneNumber + "&IdentityID=" + IdentityId;
                            Message msg = new Message();
                            String response;
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
                                    msg.obj="未连接到网络";
                                }
                            }
                            catch (Exception e) {
                                msg.what = 3;
                                msg.obj="未连接到网络";
                            }
                            handler.sendMessage(msg);
                        }
                    }.start();

                }
            }
        });

        btnAdminLog.setOnClickListener(new View.OnClickListener() {//管理员登录页面跳转
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminLogActivity.class);
                MainActivity.this.startActivity(intent);
                finish();
            }
        });

    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
        //unregisterReceiver(MyBroadCastReciever);
    }

    Handler handler=new Handler()
        {
            public void handleMessage(Message msg)
            {
                DialogUtil.closeDialog(mWeiboDialog);
                ResultInfo<LinkedHashMap>result=new ResultInfo<>();
            if(msg.what==1)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node=mapper.readTree(msg.obj.toString());
                    result=mapper.readValue(node.toString(),result.getClass());
                    if(result.Success==true)
                    {
                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        AutoLogin=true;
                        //记住用户名、密码、
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("phoneNumber", phoneNumber);
                        editor.putString("identityId", IdentityId);
                        editor.putBoolean("AutoLogin",AutoLogin);
                        editor.putString("name",result.Data.get("Name").toString());
                        editor.putString("photo",UrlData.getUrlYy()+result.Data.get("Photo").toString());
                        editor.commit();

                        Log.e("选中保存密码", "手机号：" + phoneNumber +
                                "\n" + "身份证：" + IdentityId);
                        editor.commit();
                        //跳转界面
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        MainActivity.this.startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "手机号或身份证号错误，请重新输入", Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            if(msg.what==2||msg.what==3)
            {
                Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_LONG);
            }
            if(msg.what==4)
            {//验证过期
            Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_LONG);
            }

        }
    };


    //监听textView
    private void initEvent() {
        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialog();
            }
        });
    }


    private void showDialog(){
//        AlertDialog.Builder builder=new AlertDialog.Builder(this);
//        builder.setTitle("温馨提示");
//        builder.setMessage("详情请咨询服务热线12349！");
//        builder.setPositiveButton("确定",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                    }
//                });
//        AlertDialog dialog=builder.create();
//        dialog.show();

        new CircleDialog.Builder()
                .setTitle("温馨提示")
                .setText("详情请咨询服务热线12349！")
                .setPositive("确定", null)
                .show(getSupportFragmentManager());

    }



}
