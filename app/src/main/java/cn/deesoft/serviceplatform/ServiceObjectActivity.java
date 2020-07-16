package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.ResultInfoList;
import Util.ActivityManager;
import Util.DateUtil;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.MyConnection;
import Util.UrlData;
import cn.deesoft.serviceplatform.Adapter.SelectObjectListAdapter;

public class ServiceObjectActivity extends AppCompatActivity {
  private Dialog mWeiboDialog;
  private ListView listServiceOlder;
  private SelectObjectListAdapter selectObjectListAdapter;
  private SharedPreferences sp;
  private Boolean isLiving;
  List<Map<String, Object>> listItems=new ArrayList<>();
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_service_object);
    ActivityManager.getInstance().addActivity(this);
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if(actionBar != null){
      actionBar.setHomeButtonEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    sp=this.getSharedPreferences("userInfo",Context.MODE_PRIVATE);

    listServiceOlder = findViewById(R.id.listServiceOlder);
    mWeiboDialog = DialogUtil.createLoadingDialog(ServiceObjectActivity.this, "加载中...");

    new Thread() {
      @Override
      public void run() { Message msg = new Message();
      String response="";
        String url = UrlData.getUrlYy()+"/api/AndroidApi/GetOlders?identityID="+sp.getString("identityId","");
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
        handler.sendMessage(msg);
//        DialogUtil.closeDialog(mWeiboDialog);
      }
    }.start();
    listServiceOlder.setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (HttpUtil.isFastClick()) {
                  Bundle bundle = new Bundle();
                  bundle.putString("OlderID", listItems.get(position).get("ID").toString());
                  bundle.putString("OlderName", listItems.get(position).get("txtName").toString());
                  bundle.putString("Age",listItems.get(position).get("txtAge").toString());
                  bundle.putString("Town",listItems.get(position).get("Town").toString());
                  bundle.putBoolean("IsLiving",Boolean.valueOf(listItems.get(position).get("IsLiving").toString()));
                  bundle.putString("RemainTime",listItems.get(position).get("RemainTime").toString());
                  if(listItems.get(position).get("photo")!=null) {
                    bundle.putString("Photo",listItems.get(position).get("photo").toString());
                  }
                  else
                  {
                    bundle.putString("Photo","null");
                  }
                  Intent intent = new Intent();
                  intent.putExtras(bundle);
                  intent.setClass(ServiceObjectActivity.this, StartServiceActivity.class);
                  startActivity(intent);
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
      ResultInfoList<Object> list=new ResultInfoList<Object>();
      if(msg.what==1)
      {
        try {
          ObjectMapper mapper = new ObjectMapper();
          JsonNode node=mapper.readTree(msg.obj.toString());
          list= mapper.readValue(node.toString(),new TypeReference<ResultInfoList<LinkedHashMap>>() {});
          for(LinkedHashMap map:(LinkedHashMap[]) list.Data)
          {
            Map<String, Object> item = new HashMap<>();
            item.put("ID",map.get("ID").toString());
            item.put("txtName",map.get("TrueName").toString());
            String birthday=map.get("BirthDay").toString();
            birthday=birthday.substring(0,birthday.indexOf("T"));
            int age= DateUtil.getAgeFromBirthTime(birthday);
            item.put("txtAge",age+"");
            isLiving=Boolean.valueOf(map.get("IsLiving").toString());
            item.put("IsLiving",isLiving);
            item.put("photo",map.get("Photo"));
            item.put("Town",map.get("Town"));
            item.put("RemainTime",map.get("RemainTime").toString());
            item.put("WorkOrderCount",map.get("WorkOrderCount").toString());
            listItems.add(item);
          }
          selectObjectListAdapter =new SelectObjectListAdapter(ServiceObjectActivity.this,listItems);

          listServiceOlder.setAdapter(selectObjectListAdapter);
        }
        catch(Exception ex)
        {
          ex.printStackTrace();
          Toast.makeText(ServiceObjectActivity.this,"出现未知异常，请联系管理员!！",Toast.LENGTH_LONG).show();
        }
      }
      if(msg.what==2) {
        Toast.makeText(ServiceObjectActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
      }
      if(msg.what==3) {
        Toast.makeText(ServiceObjectActivity.this, "未连接到网络", Toast.LENGTH_LONG).show();
      }
      if(msg.what==4) {
        Toast.makeText(ServiceObjectActivity.this, "验证过期", Toast.LENGTH_LONG).show();
      }else {
        Log.e("网络不好","无返回对象");
      }
    }
  };

}
