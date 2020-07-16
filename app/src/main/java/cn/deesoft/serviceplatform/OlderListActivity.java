package cn.deesoft.serviceplatform;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import Model.Older;
import Model.ResultInfoList;
import Util.DateUtil;
import Util.DialogUtil;
import Util.MyConnection;
import Util.UrlData;
import cn.deesoft.serviceplatform.Adapter.OlderAdapter;

public class OlderListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    //搜索框组件

    private ImageView deleteText;
    private EditText searchText;
    private TextView searchButton;
    public String searchContent;


    private LoadOlderList listView;
    OlderAdapter olderAdapter;
    private ArrayList<Older> olderList=new ArrayList<>();//用于展示老人列表


    private ArrayList<Older> olderForSearch=new ArrayList<>();//用于代替老人信息数据库
    private Dialog mWeiboDialog;
    private int pageNum;
    private String town;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_older_list);
        key="";
        town="武康街道";
        Intent intent = getIntent();
        town = intent.getStringExtra("Area");
        
        initOlders();
        olderAdapter=new OlderAdapter(OlderListActivity.this,R.layout.older_listitem,olderList);
        listView= (LoadOlderList) findViewById(R.id.older_list_view);
        listView.setAdapter(olderAdapter);
        listView.setOnItemClickListener(this);
        deleteText=(ImageView)findViewById(R.id.bt_delete_text);
        searchText=findViewById(R.id.search_text);
        searchButton=findViewById(R.id.search_button_1);

        mWeiboDialog = DialogUtil.createLoadingDialog(OlderListActivity.this, "加载中...");


        initClick();//设置搜索组件动作监听
        myOnclick();
        pageNum=1;


        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(town+" - 老人");
        }
    }

    private Handler firstPageHandler=new Handler(){
        //将返回的老人加入老人列表
        ResultInfoList<Object> list=new ResultInfoList<Object>();
        @Override
        public void handleMessage(Message msg){

            switch (msg.what) {
                case 1:
                    try {
                        DialogUtil.closeDialog(mWeiboDialog);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(msg.obj.toString());
                        list = mapper.readValue(node.toString(), new TypeReference<ResultInfoList<LinkedHashMap>>() {
                        });
                        for (LinkedHashMap map : (LinkedHashMap[]) list.Data) {
                            String name="";
                            name = map.get("TrueName").toString();
                            String birthday="";
                            birthday = map.get("Birthday").toString();
                            birthday = birthday.substring(0, birthday.indexOf("T"));
                            String identityID="";
                            identityID=map.get("IdentityID").toString();
                            String village="";
                            if(map.get("Village")!=(null)){
                            village=map.get("Village").toString();}
                            int age = DateUtil.getAgeFromBirthTime(birthday);
                            String ID=map.get("ID").toString();
                            Boolean isLiving=Boolean.valueOf(map.get("IsLiving").toString());
                            Older older1 = new Older(name, R.mipmap.nophoto2, age,identityID,ID,village);
                            if(!isLiving){
                                older1.setImage(R.mipmap.nophoto_black);
                                older1.setIsLiving(false);
                            }
                            olderList.add(older1);
                        }
                    } catch (Exception ex) {
                    }
//          通知适配器数据已经改变
                    olderAdapter.notifyDataSetChanged();
//          加载完成
                    listView.loadComplete();
                    break;
                case 2:
                    Toast.makeText(OlderListActivity.this,msg.obj.toString(),Toast.LENGTH_LONG);
                    break;
                case 3:
                    Toast.makeText(OlderListActivity.this,"未连接到网络",Toast.LENGTH_LONG);
                    break;
                case 4:
                    Toast.makeText(OlderListActivity.this,msg.obj.toString(),Toast.LENGTH_LONG);
                    Log.e("sssssss","ssssss");
                    break;
            }
        }
    };
    private void initOlders(){//获取第一页数据
        ResultInfoList<Object> list=new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                String response="";
                String url = UrlData.getUrl()+"/api/AndroidApi/GetOlderList?pageNum="+pageNum+"&town="+town+"&key="+key;
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
        firstPageHandler.sendEmptyMessageDelayed(1,100);
    }

    //将entity当中的数据转换为字符串


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent=new Intent(OlderListActivity.this,AdminMenuActivity.class);
                intent.putExtra("Area", town);
                OlderListActivity.this.startActivity(intent);
                this.finish();  // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void myOnclick() {
//      加载数据
        listView.setInterface(new LoadOlderList.ILoadListener() {
            @Override
            public void onLoad() {
                pageNum=pageNum+1;
                initOlders();
            }

        });
    }
    //搜索框功能
    private void initClick() {
        deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setText("");
                key="";
                olderList.clear();
                pageNum=1;
                initOlders();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchContent=searchText.getText().toString();
                listView.footer.setVisibility(View.GONE);
                key=searchContent;
                olderList.clear();
                pageNum=1;
                initOlders();

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //通过view获取其内部的组件，进而进行操作
        try {
            Intent intent = new Intent();
            if ((String) ((TextView) view.findViewById(R.id.olderName)).getText() != null) {
                intent.putExtra("TrueName", (String) ((TextView) view.findViewById(R.id.olderName)).getText());
                intent.putExtra("Age", (String) ((TextView) view.findViewById(R.id.olderAge)).getText());
                intent.putExtra("IdentityId", (String) ((TextView) view.findViewById(R.id.olderIdentityID)).getText());
                String iii= (String) ((TextView) view.findViewById(R.id.olderId)).getText();
                //Toast.makeText(OlderListActivity.this, iii, Toast.LENGTH_LONG).show();
                intent.putExtra("CurrentArea", town);
                intent.putExtra("ID", (String) ((TextView) view.findViewById(R.id.olderId)).getText());
                intent.setClass(OlderListActivity.this, OlderDetailActivity.class);
                OlderListActivity.this.startActivity(intent);
            }
        }catch (Exception e){
            Toast.makeText(this,"请稍等",Toast.LENGTH_LONG).show();
        }
    }

}