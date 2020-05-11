package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import Util.DialogUtil;
import Util.UrlData;
import cn.deesoft.serviceplatform.Adapter.WorkOrderListAdapter;

public class WorkOrderActivity extends AppCompatActivity {
    private Dialog mWeiboDialog;
    private ListView myListView;
    private Button btnRefresh;
    private WorkOrderListAdapter workOrderListAdapter;
    private SharedPreferences sp;
    private String month;
    private String year;
    private String olderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final Bundle bundle=this.getIntent().getExtras();
        month=bundle.getString("month");
        year=bundle.getString("year");
        olderID=bundle.getString("olderID");

        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        mWeiboDialog = DialogUtil.createLoadingDialog(WorkOrderActivity.this, "加载中...");
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();

                String url = UrlData.getUrlYy()+"/api/Default/GetWorkOrder?IdentityID=" +
                        sp.getString("identityId", "")+"&year="+year+"&month="+month+"&olderID="+olderID;
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
                    }
                    else
                        {
                            msg.what = 2;
                            handler.sendMessage(msg);
                        }
                    }
                    catch (Exception ex) {
                    DialogUtil.closeDialog(mWeiboDialog);
                }
            }
        }.start();
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
            List<Map<String, Object>> listItems=new ArrayList<>();
            Model.ResultInfoList<Object> list=new Model.ResultInfoList<Object>();
            Intent intent=new Intent();
            intent.setClass(WorkOrderActivity.this,MenuActivity.class);
            if(msg.what==1)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node=mapper.readTree(msg.obj.toString());
                    list= mapper.readValue(node.toString(),new TypeReference<ResultInfoList<LinkedHashMap>>() {});
                    if(list.Success==true)
                    {
                        for(LinkedHashMap map:(LinkedHashMap[]) list.Data)
                        {
                            Map<String, Object> item = new HashMap<>();
                            if(((HashMap)map.get("OldPeople")).get("Photo")!=null) {
                                item.put("imgPhoto", ((HashMap) map.get("OldPeople")).get("Photo").toString());
                            }
                            else
                            {
                                item.put("imgPhoto","null");
                            }
                            item.put("txtOlderName", map.get("OldPeopleName").toString());
                            item.put("txtServiceName", map.get("WorkContent").toString());
                            item.put("txtBillingStatus", map.get("BillingStatus").toString());
                            item.put("txtServiceTime", (int)Double.parseDouble(map.get("WorkInterval").toString()));
                            item.put("txtWealInterval", (int)Double.parseDouble(map.get("WealInterval").toString()));
                            item.put("txtPrice", "￥"+String.format("%.1f",Double.parseDouble(map.get("SettlementFee").toString())));
                            item.put("txtStartTime", map.get("StartTime").toString().replace("T"," "));
                            // 步骤3-3：将封装好的列表选项对象item添加到集合中
                            listItems.add(item);
                        }
                       workOrderListAdapter =new WorkOrderListAdapter(WorkOrderActivity.this,listItems);
;
                        myListView = (ListView) findViewById(R.id.myListView);
                        myListView.setAdapter(workOrderListAdapter);
                    }
                    else
                    {
                        Toast.makeText(WorkOrderActivity.this,"数据查询失败",Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception ex)
                {
                    Toast.makeText(WorkOrderActivity.this,"数据查询失败",Toast.LENGTH_LONG).show();
                }
            }
            else if(msg.what==2)
            {
                Toast.makeText(WorkOrderActivity.this,"网络不可用,请连接网络后点击刷新按钮",Toast.LENGTH_LONG).show();
                btnRefresh=findViewById(R.id.btnRefresh);
                btnRefresh.setVisibility(View.VISIBLE);
                btnRefresh.setText("刷新");
                btnRefresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnRefresh.setVisibility(View.INVISIBLE);
                        new Thread() {
                            @Override
                            public void run() {
                                Message msg = new Message();
                                String url = UrlData.getUrlYy()+"/api/Default/GetWorkOrder?IdentityID=" + sp.getString("identityId", "");
                                try {
                                    HttpClient httpClient = new DefaultHttpClient();
                                    HttpGet httpGet = new HttpGet(url);
                                    HttpResponse execute = httpClient.execute(httpGet);
                                    if (execute.getStatusLine().getStatusCode() == 200) {
                                        HttpEntity entity = execute.getEntity();
                                        String response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                                        msg.what = 0x123;
                                        msg.obj = response;
                                        handler.sendMessage(msg);
                                    }
                                } catch (Exception ex) {
                                    msg.what = 0x124;
                                    handler.sendMessage(msg);
                                }
                            }
                        }.start();
                    }
                });
            }
        }
    };



}
