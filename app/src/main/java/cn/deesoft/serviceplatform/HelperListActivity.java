package cn.deesoft.serviceplatform;

import android.app.Dialog;
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

import Model.Helper;
import Model.ResultInfoList;
import Util.DialogUtil;
import Util.MyConnection;
import Util.TokenData;
import Util.UrlData;
import cn.deesoft.serviceplatform.Adapter.HelperAdapter;

public class HelperListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    //搜索框组件
    private ImageView deleteText2;
    private EditText searchText2;
    private TextView searchButton2;
    public String searchContent2;

    private LoadHelperList listView;
    HelperAdapter helperAdapter;
    private ArrayList<Helper> helperList=new ArrayList<>();

    private Dialog mWeiboDialog;
    private int pageNum;
    private String town;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_list);

        town="";
        key="";
        Intent intent = getIntent();
        town = intent.getStringExtra("Area");

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(town+" - 助老员");
        }

        helperAdapter=new HelperAdapter(HelperListActivity.this,R.layout.helper_listitem,helperList);
        initHelpers();
        listView=findViewById(R.id.helper_list_view);
        listView.setAdapter(helperAdapter);
        listView.setOnItemClickListener(this);//添加item点击
        //搜索框
        deleteText2=(ImageView)findViewById(R.id.bt_delete_text);
        searchText2=findViewById(R.id.search_text);
        searchButton2=findViewById(R.id.search_button_1);

        mWeiboDialog = DialogUtil.createLoadingDialog(HelperListActivity.this, "加载中...");

        pageNum=1;
        myOnclick();
        initClick();
    }

    private Handler firstPageHandler=new Handler(){
        ResultInfoList<Object> list=new ResultInfoList<Object>();
        @Override
        public void handleMessage(Message msg){

            DialogUtil.closeDialog(mWeiboDialog);
            switch (msg.what) {
                case 1:
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(msg.obj.toString());
                        list = mapper.readValue(node.toString(), new TypeReference<ResultInfoList<LinkedHashMap>>() {
                        });
                        for (LinkedHashMap map : (LinkedHashMap[]) list.Data) {
                            String name = map.get("Name").toString();
                            String helperTown;
                            String helperId=map.get("ID").toString();
                            String helperVillage;
                            String helperMobile="暂无";
                            if (map.get("PhoneNumber")!=null&&map.get("PhoneNumber")!="") {
                            helperMobile=map.get("PhoneNumber").toString();}

                            if(map.get("Town")!=null) {helperTown=map.get("Town").toString();}else{helperTown=null;}
                            if(map.get("Village")!=null) {helperVillage=map.get("Village").toString();}else{helperVillage="";}

                            Helper helper = new Helper(name,helperId, R.mipmap.nophoto2, helperTown,helperVillage,helperMobile);
                            helperList.add(helper);
                        }
                    } catch (Exception ex) {
                    }

//          通知适配器数据已经改变


                    helperAdapter.notifyDataSetChanged();
//          加载完成
                    listView.loadComplete();
                    break;
                case 2:
                    Toast.makeText(HelperListActivity.this,msg.obj.toString(),Toast.LENGTH_LONG);
                    break;
                case 3:
                    Toast.makeText(HelperListActivity.this,"未连接到网络",Toast.LENGTH_LONG);
                    break;
                case 4:
                    Toast.makeText(HelperListActivity.this,"验证过期",Toast.LENGTH_LONG);
                    Log.e("sssssss","ssssss");
                    break;
            }
        }
    };



    private void initHelpers() {//发送请求获得第一页数据

        ResultInfoList<Object> list=new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                String response="";
                String url= UrlData.getUrlYy()+"/api/AndroidApi/GetHelperList?pageNum="+pageNum+"&town="+town+"&key="+key;
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



    private void myOnclick() {//获取更多滑动加载数据
//      加载数据
        listView.setInterface(new LoadHelperList.ILoadHelperListener() {
            @Override
            public void onLoad() {
                pageNum=pageNum+1;
                initHelpers();
            }
        });

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

    //搜索框事件
    private void initClick() {
        deleteText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText2.setText("");
                key="";
                helperList.clear();
                pageNum=1;
                initHelpers();
            }
        });

        searchButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchContent2=searchText2.getText().toString();
                listView.footer.setVisibility(View.GONE);
                key=searchContent2;
                helperList.clear();
                pageNum=1;
                initHelpers();

            }
        });
    }





    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //通过view获取其内部的组件，进而进行操作
        try {
            Intent intent = new Intent();
            if ((String) ((TextView) view.findViewById(R.id.helperName)).getText() != null) {
                intent.putExtra("Name", (String) ((TextView) view.findViewById(R.id.helperName)).getText());
                intent.putExtra("Town", (String) ((TextView) view.findViewById(R.id.helperTown)).getText());
                intent.putExtra("helperId", (String) ((TextView) view.findViewById(R.id.helperId)).getText());
                intent.setClass(HelperListActivity.this, HelperDetailActivity.class);
                HelperListActivity.this.startActivity(intent);
            }
        }catch (Exception e){
            Toast.makeText(this,"请稍等",Toast.LENGTH_LONG).show();
        }
    }
}