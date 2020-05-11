package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.jna.platform.win32.WinUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;

import Model.ResultInfoList;
import Util.DialogUtil;
import Util.UrlData;

public class OrderDetailActivity extends AppCompatActivity {

    private Dialog mWeiboDialog;
    String orderId;//通过向请求服务器GetWorkOrderById方法，获得工单详情
    int ID;
    ImageView helperImage;

    TextView txtHelperName;
    TextView txtOlderName;
    TextView txtOrderId;
    TextView txtWorkStatus;
    TextView txtBillingStatus;
    TextView txtAddDate;
    TextView txtServiceName;
    TextView txtWorkContent;
    TextView txtStartTime;
    TextView txtEndTime;
    TextView txtWorkInterval;
    TextView txtWorkFee;

    String helperName;
    String olderName;
    String serviceName;
    String workStatus;
    String billingStatus;
    String addDate;
    String startTime;
    String endTime;
    String workContent;
    String workInterval;
    String workFee;
    Double interval;

    private ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        Intent intent=getIntent();
        orderId=intent.getStringExtra("ID");
        ID=Integer.parseInt(orderId);
        helperImage=findViewById(R.id.helperImage);
        helperImage.setImageResource(R.mipmap.nophoto2);


        Toast.makeText(OrderDetailActivity.this,"当前订单号"+ID,Toast.LENGTH_SHORT).show();
        initOrder();



        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    private void initOrder(){
        ResultInfoList<Object> list=new ResultInfoList<Object>();
        new Thread(){
            @Override
            public void run() {
                Message msg = new Message();
                String url = UrlData.getUrl() + "/api/Default/GetWorkOrderById?orderId=" + ID;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse execute = httpClient.execute(httpGet);
                    if (execute.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = execute.getEntity();
                        String response = EntityUtils.toString(entity);   //将entity当中的数据转换为字符串
                        msg.what = 1;
                        msg.obj = response;
                        handler.sendMessage(msg);
                    } else {
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {

                }
            }
        }.start();
        handler.sendEmptyMessageDelayed(1,100);
    }

    private Handler handler=new Handler(){
        ResultInfoList<Object> list=new ResultInfoList<Object>();
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    try{
                        DialogUtil.closeDialog(mWeiboDialog);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(msg.obj.toString());
                        list = mapper.readValue(node.toString(), new TypeReference<ResultInfoList<LinkedHashMap>>() {
                        });
                        for (LinkedHashMap map : (LinkedHashMap[]) list.Data) {
                            helperName = map.get("HelperName").toString();
                            olderName=map.get("OldPeopleName").toString();
                            serviceName=map.get("ServiceName").toString();
                            workStatus=map.get("WorkStatus").toString();
                            billingStatus=map.get("BillingStatus").toString();
                            String addDate1=map.get("AddDate").toString();
                            addDate=addDate1.replace("T"," ");
                            String startTime1=map.get("StartTime").toString();
                            startTime=startTime1.replace("T"," ");
                            String endTime1=map.get("EndTime").toString();
                            endTime=endTime1.replace("T"," ");
                            workContent=map.get("WorkContent").toString();
                            workInterval=map.get("WorkInterval").toString();
                            workFee=map.get("WorkFee").toString();
                            interval = Double.parseDouble(workInterval);
                            double v = new BigDecimal(interval).setScale(2, RoundingMode.DOWN).doubleValue();

                            txtHelperName=findViewById(R.id.txtHelperName);
                            txtOrderId=findViewById(R.id.txtOrderId);
                            txtOlderName=findViewById(R.id.txtOlderName);
                            txtWorkStatus=findViewById(R.id.txtWorkStatus);
                            txtAddDate=findViewById(R.id.txtAddDate);

                            txtBillingStatus=findViewById(R.id.txtBillingStatus);
                            txtServiceName=findViewById(R.id.txtServiceName);
                            txtWorkContent=findViewById(R.id.txtWorkContent);
                            txtStartTime=findViewById(R.id.txtStartTime);
                            txtEndTime=findViewById(R.id.txtEndTime);
                            txtWorkInterval=findViewById(R.id.txtWorkInterval);
                            txtWorkFee=findViewById(R.id.txtWorkFee);


                            txtHelperName.setText(helperName);
                            txtOrderId.setText(orderId);
                            txtOlderName.setText(olderName);
                            txtWorkStatus.setText(workStatus);
                            txtAddDate.setText(addDate);

                            txtBillingStatus.setText(billingStatus);
                            txtServiceName.setText(serviceName);
                            txtWorkContent.setText(workContent);
                            txtStartTime.setText(startTime);
                            txtEndTime.setText(endTime);
                            txtWorkInterval.setText("分钟 "+String.valueOf(v));
                            txtWorkFee.setText("￥ "+workFee);

                            Toast.makeText(OrderDetailActivity.this,"助老员姓名："+helperName,Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){

                    }
            }
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}