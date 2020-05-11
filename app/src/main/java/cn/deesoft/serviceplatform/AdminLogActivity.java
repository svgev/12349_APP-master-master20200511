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

import java.util.LinkedHashMap;

import Model.ResultInfo;
import Util.DialogUtil;
import Util.HttpUtil;

import java.util.LinkedHashMap;

import Model.ResultInfo;
import Util.DialogUtil;
import Util.UrlData;


public class AdminLogActivity extends FragmentActivity {

    private Button btnLog;
    private String adminName;
    private String password;
    private String trueName;
    private String county;
    private String town;
    private String village;
    private String addr;
    private String roles;
    private String area;
    private String addTime;
    private Dialog mWeiboDialog;
    private EditText etAdminName;
    private EditText etPassword;
    private TextView helperAdminBtn;
    private SharedPreferences sp;
    private boolean AdminAutoLogin=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_log);


        etAdminName=findViewById(R.id.txtAdminName);
        etPassword=findViewById(R.id.txtAdminPassword);
        btnLog=findViewById(R.id.btnAdmin);
        helperAdminBtn=findViewById(R.id.helperLoginTxt);
        initEvent();

        //判断用户登录历史纪录 若已经登陆 直接跳转用户界面
        try {
            sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        }
        catch (Exception ex)
        {
            Log.i("default",ex.getMessage());
        }
        if(sp.getBoolean("AutoLogin",false))
        {
            //跳转界面
            Intent intent = new Intent(AdminLogActivity.this, MenuActivity.class);
            AdminLogActivity.this.startActivity(intent);
            finish();
        }


        try {
            sp = this.getSharedPreferences("adminInfo", Context.MODE_PRIVATE);
        } catch (Exception ex) {
            Log.i("default", ex.getMessage());
        }
        if (sp.getBoolean("AdminAutoLogin", false)) {
            etAdminName.setText(sp.getString("AdminName", ""));
            etPassword.setText(sp.getString("Password", ""));
            //跳转界
        }

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminName=etAdminName.getText().toString();
                password=etPassword.getText().toString();
                mWeiboDialog = DialogUtil.createLoadingDialog(AdminLogActivity.this, "登录中...");
                new Thread() {
                    @Override
                    public void run() {
                        String url = UrlData.getUrl()+"/api/Default/AdminLogin?adminName=" + adminName + "&password=" + password;
                        Message msg = new Message();
                        try {
                            HttpClient httpClient = new DefaultHttpClient();
                            HttpGet httpGet = new HttpGet(url);
                            HttpResponse execute = httpClient.execute(httpGet);
                            if (execute.getStatusLine().getStatusCode() == 200) {
                                HttpEntity entity = execute.getEntity();
                                String response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                                msg.what = 1010;
                                msg.obj = response;
                                handler.sendMessage(msg);
                            } else {
                                msg.what = 113;
                                handler.sendMessage(msg);
                            }
                        } catch (Exception ex) {
                            DialogUtil.closeDialog(mWeiboDialog);
                            msg.what = 113;
                            handler.sendMessage(msg);
                            ex.printStackTrace();
                        }
                    }
                }.start();


            }
        });
        helperAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLogActivity.this, MainActivity.class);
                AdminLogActivity.this.startActivity(intent);
                finish();
            }
        });
    }

    Handler handler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            DialogUtil.closeDialog(mWeiboDialog);
            ResultInfo<LinkedHashMap> result=new ResultInfo<>();
            if(msg.what==1010)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node=mapper.readTree(msg.obj.toString());
                    result=mapper.readValue(node.toString(),result.getClass());
                    if(result.Success==true)
                    {
                        Toast.makeText(AdminLogActivity.this, "管理员登录成功", Toast.LENGTH_SHORT).show();

                        AdminAutoLogin=true;
                        trueName=result.Data.get("TrueName").toString();
                        if(result.Data.get("Town")!=(null))
                        town=result.Data.get("Town").toString();
                        roles=result.Data.get("Roles").toString();
                        if(result.Data.get("Area")!=(null))
                        area=result.Data.get("Area").toString();
                        if(result.Data.get("AddDate")!=(null))
                        addTime=result.Data.get("AddDate").toString();
                        if(result.Data.get("County")!=(null))
                        county=result.Data.get("County").toString();
                        if(result.Data.get("Village")!=(null))
                        village=result.Data.get("Village").toString();
                        addr=county+town+village;


                        //记住用户名、密码、管辖区域
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("AdminName", adminName);
                        editor.putString("Password", password);
                        editor.putBoolean("AdminAutoLogin",AdminAutoLogin);
                        editor.putString("TrueName", trueName);
                        editor.putString("Town", town);
                        editor.putString("Roles", roles);
                        editor.putString("Area",area);
                        editor.putString("Addr",addr);
                        editor.putString("addTime",addTime);

                        editor.commit();
                        //Toast.makeText(AdminLogActivity.this, "存储完毕", Toast.LENGTH_LONG).show();

                        //Log.e("选中保存密码", "密码：" + adminName +"\n" + "身份证：" + password);
                        editor.commit();
                        //跳转界面
                        Intent intent = new Intent(AdminLogActivity.this, AdminAreaActivity.class);

                        AdminLogActivity.this.startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(AdminLogActivity.this, "密码错误，请重新输入", Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            else if(msg.what==113)
            {
                Toast.makeText(AdminLogActivity.this,"未连接到网络！",Toast.LENGTH_LONG).show();
            }
        }
    };

    private void initEvent() {
        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }
    private void showDialog(){

        new CircleDialog.Builder()
                .setTitle("温馨提示")
                .setText("详情请咨询服务热线12349！")
                .setPositive("确定", null)
                .show(getSupportFragmentManager());

    }
}
