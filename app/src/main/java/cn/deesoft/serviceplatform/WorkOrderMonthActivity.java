package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.nio.BufferUnderflowException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.ResultInfoList;
import Util.DialogUtil;
import Util.HttpUtil;
import Util.MyConnection;
import Util.UrlData;
import cn.deesoft.serviceplatform.Adapter.WorkOrderMonthListAdapter;

public class WorkOrderMonthActivity extends AppCompatActivity implements View.OnClickListener,OnDateSetListener {
    private Dialog mWeiboDialog;
    private ListView myListView;
    private Button btnRefresh;
    private TextView txtTime;
    private ImageView imgTime;
    private TextView txtPrice;

    private int month;
    private int year;

    List<Map<String, Object>> listItems=new ArrayList<>();
    private WorkOrderMonthListAdapter workOrderMonthListAdapter;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order_month);
        //显示toolbar中的返回按钮

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        myListView = findViewById(R.id.myListView);
        imgTime=findViewById(R.id.imgTime);
        txtPrice=findViewById(R.id.txtPrice);
        txtTime=findViewById(R.id.txtTime);
//        Calendar cal = Calendar.getInstance();
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH )+1;
//        txtTime.setText(year+"年"+month+"月");


        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        month=Calendar.getInstance().get(Calendar.MONTH)+1;
        year=Calendar.getInstance().get(Calendar.YEAR);
        getData();
        imgTime.setOnClickListener(this);

        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if(HttpUtil.isFastClick())
                        {
                            try {
                                Bundle bundle = new Bundle();
                                bundle.putString("month",month+"");
                                bundle.putString("year",year+"");
                                bundle.putString("olderID", listItems.get(position).get("OlderID").toString());
                                Intent intent = new Intent(WorkOrderMonthActivity.this,WorkOrderActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                            catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
        );
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



    //region Handler处理线程
    Handler handler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            DialogUtil.closeDialog(mWeiboDialog);
            Model.ResultInfoList<Object> list=new Model.ResultInfoList<Object>();
            Intent intent=new Intent();
            intent.setClass(WorkOrderMonthActivity.this,MenuActivity.class);
            if(msg.what==1)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node=mapper.readTree(msg.obj.toString());
                    double price=0;
                    list= mapper.readValue(node.toString(),new TypeReference<ResultInfoList<LinkedHashMap>>() {});
                    listItems.clear();
                    if(list.Success==true)
                    {
                        for(LinkedHashMap map:(LinkedHashMap[]) list.Data)
                        {
                            Map<String, Object> item = new HashMap<>();
                            if(map.get("Photo")!=null) {
                                item.put("imgPhoto", map.get("Photo").toString());
                            }
                            else
                            {
                                item.put("imgPhoto","null");
                            }
                            item.put("OlderID",map.get("OlderID").toString());
                            item.put("txtOlderName", map.get("OlderName").toString());
                            item.put("txtServiceTime", (int)Double.parseDouble(map.get("WorkInterval").toString()));
                            item.put("txtWealInterval", (int)Double.parseDouble(map.get("WealInterval").toString()));
                            item.put("txtPrice", "￥"+String.format("%.1f",Double.parseDouble(map.get("SettlementFee").toString())));
                            price+=Double.parseDouble(map.get("SettlementFee").toString());
                            // 步骤3-3：将封装好的列表选项对象item添加到集合中
                            listItems.add(item);
                        }
                        txtPrice.setText(String.format("%.1f",price));
                        workOrderMonthListAdapter =new WorkOrderMonthListAdapter(WorkOrderMonthActivity.this,listItems);
                        myListView.setAdapter(workOrderMonthListAdapter);
                    }
                    else
                    {
                        Toast.makeText(WorkOrderMonthActivity.this,"数据查询失败",Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception ex)
                {
                    Toast.makeText(WorkOrderMonthActivity.this,"数据查询失败",Toast.LENGTH_LONG).show();
                }
            }
            else if(msg.what==2||msg.what==3)
            {
                Toast.makeText(WorkOrderMonthActivity.this,"网络不可用,请连接网络后点击刷新按钮",Toast.LENGTH_LONG).show();
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
                                String response;
                                String url = UrlData.getUrlYy()+"/api/AndroidApi/GetWorkOrder?IdentityID=" + sp.getString("identityId", "");
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
                                    msg.what =5;
                                }
                                handler.sendMessage(msg);
                            }
                        }.start();
                    }
                });
            }
        }
    };
    //endregion

    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.imgTime:
                long tenYears = 10L * 365 * 1000 * 60 * 60 * 24L;
                TimePickerDialog dialogYearMonth = new TimePickerDialog.Builder()
                        .setCallBack(this)
                        .setCancelStringId("取消")
                        .setSureStringId("确定")
                        .setTitleStringId("")
                        .setYearText("年")
                        .setMonthText("月")
                        .setCyclic(false)
                        .setMinMillseconds(System.currentTimeMillis()-tenYears)
                        .setMaxMillseconds(System.currentTimeMillis() + tenYears)
                        .setCurrentMillseconds(System.currentTimeMillis())
                        .setToolBarTextColor(getResources().getColor(R.color.actionBar))
                        .setThemeColor(getResources().getColor(R.color.actionBar))
                        .setType(Type.YEAR_MONTH)
                        .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                        .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                        .setWheelItemTextSize(24)
                        .build();
                dialogYearMonth.show(getSupportFragmentManager(), "YEAR_MONTH");
                break;
        }
    }

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
        Date date = new Date(millseconds);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月");
        String time = format.format(date);
        txtTime.setText(time);
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        month=cal.get(Calendar.MONTH)+1;
        year=cal.get(Calendar.YEAR);
        getData();
    }


    /**
     * 发送http请求获取相关老人数据
     */
    private void getData()
    {
        mWeiboDialog = DialogUtil.createLoadingDialog(WorkOrderMonthActivity.this, "加载中...");

        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                String response;
                String url = UrlData.getUrlYy()+"/api/AndroidApi/GetMonthWorkOrder?identityID=" + sp.getString("identityId", "")+"&year="+year+"&month="+month;
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
                    msg.what = 5;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }
}
