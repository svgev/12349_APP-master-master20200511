package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.LinkedHashMap;

import Model.KeyValueInfo;
import Model.ResultInfoList;
import Util.ActivityManager;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.UrlData;

public class ServiceCategoryActivity extends AppCompatActivity {
    private Dialog mWeiboDialog;
    private Spinner spnSecondMenu;
    private Spinner spnFirstMenu;
    private ListView listService;
    private String OlderID;
    private String OlderName;
    private ResultInfoList<Object> resultInfoCategoryList=new ResultInfoList<Object>();
    private ResultInfoList<Object> resultInfoServiceList=new ResultInfoList<Object>();
    //定义一个String类型的List数组作为数据源
    private ArrayList<KeyValueInfo> firstDataList=new ArrayList<KeyValueInfo>();
    private ArrayList<KeyValueInfo> secondDataList=new ArrayList<KeyValueInfo>();
    private ArrayList<KeyValueInfo> ServiceList=new ArrayList<KeyValueInfo>();
    ArrayAdapter<KeyValueInfo> firstAdapter;
    ArrayAdapter<KeyValueInfo> secondAdapter;
    ArrayAdapter<KeyValueInfo> listAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_category);
        ActivityManager.getInstance().addActivity(this);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        spnSecondMenu = findViewById(R.id.spnSecondMenu);
        spnFirstMenu=findViewById(R.id.spnFirstMenu);
        listService=findViewById(R.id.listService);
        final Bundle bundle=this.getIntent().getExtras();
        OlderID=bundle.getString("OlderID");
        OlderName=bundle.getString("OlderName");

        spnFirstMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                secondDataList.clear();
                ServiceList.clear();

                listAdapter = new ArrayAdapter<KeyValueInfo>(ServiceCategoryActivity.this, android.R.layout.simple_spinner_dropdown_item, ServiceList);
                listService.setAdapter(listAdapter);
                String key=((KeyValueInfo) (spnFirstMenu.getSelectedItem())).getKey();
                if(resultInfoCategoryList.Data!=null) {
                    for (LinkedHashMap map : (LinkedHashMap[]) resultInfoCategoryList.Data) {
                        if (map.get("UpperID").toString().equals(key)) {
                            KeyValueInfo info = new KeyValueInfo(map.get("ID").toString(), map.get("Name").toString());
                            secondDataList.add(info);
                        }
                    }
                }
                if(secondDataList!=null) {
                    secondAdapter = new ArrayAdapter<KeyValueInfo>(ServiceCategoryActivity.this, android.R.layout.simple_spinner_dropdown_item, secondDataList);
                    spnSecondMenu.setAdapter(secondAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnSecondMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ServiceList.clear();
                String key=((KeyValueInfo) (spnSecondMenu.getSelectedItem())).getKey();
                if(resultInfoServiceList.Data!=null) {
                    for (LinkedHashMap map : (LinkedHashMap[]) resultInfoServiceList.Data) {
                        if (map.get("CategoryID").toString().equals(key)) {
                            KeyValueInfo info = new KeyValueInfo(map.get("ID").toString(), map.get("Name").toString());
                            ServiceList.add(info);
                        }
                    }
                }
                if(ServiceList!=null) {
                    listAdapter = new ArrayAdapter<KeyValueInfo>(ServiceCategoryActivity.this, android.R.layout.simple_spinner_dropdown_item, ServiceList);
                    listService.setAdapter(listAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        listService.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (HttpUtil.isFastClick()) {
                    Bundle listBundle = new Bundle();
                    listBundle.putString("OlderID", OlderID);
                    listBundle.putString("OlderName", OlderName);
                    listBundle.putString("ServiceID", ServiceList.get(i).getKey());
                    listBundle.putString("ServiceName", ((KeyValueInfo) (spnSecondMenu.getSelectedItem())).getValue());
                    Intent intent = new Intent();
                    intent.putExtras(listBundle);
                    intent.setClass(ServiceCategoryActivity.this, StartServiceActivity.class);
                    startActivity(intent);
                }
            }
        });
        mWeiboDialog = DialogUtil.createLoadingDialog(ServiceCategoryActivity.this, "加载中...");
        new Thread() {
            @Override
            public void run() {
                String url = UrlData.getUrlYy()+"/api/Default/GetServices";
                Message msg = new Message();
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse execute = httpClient.execute(httpGet);
                    if (execute.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = execute.getEntity();
                        String response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                        msg.what = 0x124;
                        msg.obj = response;
                        handler.sendMessage(msg);
                    }
                    else
                    {
                        msg.what = 0x125;
                        handler.sendMessage(msg);
                    }
                } catch (Exception ex) {
                    DialogUtil.closeDialog(mWeiboDialog);
                    ex.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                String url = UrlData.getUrlYy()+"/api/Default/GetCategories";
                Message msg = new Message();
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
                    else
                    {
                        msg.what = 0x125;
                        handler.sendMessage(msg);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
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

    @Override
    public void onStart()
    {
        super.onStart();

    }


    Handler handler=new Handler()
    {

        public void handleMessage(Message msg)
        {
            if(msg.what==291)
            {
                try {
                    DialogUtil.closeDialog(mWeiboDialog);
                    String result=msg.obj.toString();
                    ObjectMapper mapper=new ObjectMapper();
                    JsonNode node=mapper.readTree(result);
                    resultInfoCategoryList=mapper.readValue(node.toString(), new TypeReference<ResultInfoList<LinkedHashMap>>() {});

                    if(resultInfoCategoryList.Success==true)
                    {
                        for(LinkedHashMap map:(LinkedHashMap[]) resultInfoCategoryList.Data)
                        {
                            if(map.get("UpperID").toString().equals("0")) {
                                KeyValueInfo info=new KeyValueInfo(map.get("ID").toString(),map.get("Name").toString());
                                firstDataList.add(info);
                            }
                        }
                        if(firstDataList!=null) {
                            firstAdapter = new ArrayAdapter<KeyValueInfo>(ServiceCategoryActivity.this, android.R.layout.simple_spinner_dropdown_item, firstDataList);

                            //为适配器设置下拉列表下拉时的菜单样式。
                            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            //为spinner绑定我们定义好的数据适配器
                            spnFirstMenu.setAdapter(firstAdapter);
                        }
                        else
                        {
                            Toast.makeText(ServiceCategoryActivity.this,"没有服务分类",Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(ServiceCategoryActivity.this, "没有服务类别数据", Toast.LENGTH_SHORT).show();
                    }
                }
                catch(Exception ex)
                {
                    Toast.makeText(ServiceCategoryActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            else if(msg.what==292)
            {
                DialogUtil.closeDialog(mWeiboDialog);
                try {
                    String result=msg.obj.toString();
                    ObjectMapper mapper=new ObjectMapper();
                    JsonNode node=mapper.readTree(result);
                    resultInfoServiceList=mapper.readValue(node.toString(),new TypeReference<ResultInfoList<LinkedHashMap>>() {});

                    if(resultInfoServiceList.Success==true)
                    {

                    }
                    else
                    {
                        Toast.makeText(ServiceCategoryActivity.this, "没有服务数据", Toast.LENGTH_SHORT).show();
                    }
                }
                catch(Exception ex)
                {
                    Toast.makeText(ServiceCategoryActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(ServiceCategoryActivity.this,"请检查网络",Toast.LENGTH_LONG).show();
            }
        }
    };
}
