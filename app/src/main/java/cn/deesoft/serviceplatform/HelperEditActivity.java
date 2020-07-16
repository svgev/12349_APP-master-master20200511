package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mylhyl.circledialog.CircleDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Model.Helper;
import Model.ResultInfoList;
import Util.ActivityManager;
import Util.DialogUtil;
import Util.MyConnection;
import Util.UrlData;

import static Util.GetVillageList.getVillageList;

public class HelperEditActivity extends AppCompatActivity {

    private String ID;
    private String helperName;
    private String identityId;
    private String phoneNumber;
    private String town;
    private String village;
    private String addr;
    private String sex;
    private Boolean infoChecked;
    private Dialog mWeiboDialog;

    private EditText edtHelperName;
    private TextView txtSex;
    private EditText edtIdentityId;
    private EditText edtPhoneNumber;
    private TextView txtTown;
    private TextView txtVillage;
    private EditText edtAddr;
    private String[] villageList;
    private ImageView imgPhoto;
    private TextView checkId;
    private TextView existedId;
    private TextView txtPhoneNumberChecked;
    private Button btnSubmit;
    public   Date date;

    //提交信息是否符合要求

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_edit);

        infoChecked=true;

        edtHelperName = findViewById(R.id.edtName);
        txtSex=findViewById(R.id.txtSex);
        edtIdentityId = findViewById(R.id.edtIdentityId);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        txtTown = findViewById(R.id.edtTown);
        txtVillage = findViewById(R.id.edtVillage);
        edtAddr = findViewById(R.id.edtAddr);
        checkId=findViewById(R.id.checkId);
        txtPhoneNumberChecked=findViewById(R.id.phoneNumberChecked);
        existedId=findViewById(R.id.existedId);
        btnSubmit=findViewById(R.id.btnSubmit);


        Intent intent = getIntent();

        ID=intent.getStringExtra("ID");
        helperName = intent.getStringExtra("Name");
        identityId = intent.getStringExtra("IdentityId");
        phoneNumber = intent.getStringExtra("PhoneNumber");
        town = intent.getStringExtra("Town");
        village = intent.getStringExtra("Village");
        addr = intent.getStringExtra("Addr");
        sex=intent.getStringExtra("Sex");

        edtHelperName.setText(helperName);
        edtIdentityId.setText(identityId);
        edtPhoneNumber.setText(phoneNumber);
        txtTown.setText(town);
        txtVillage.setText(village);
        edtAddr.setText(addr);
        txtSex.setText(sex);
        imgPhoto = findViewById(R.id.imgPhoto);
        Glide.with(HelperEditActivity.this).load(R.mipmap.nophoto2).into(imgPhoto);

        /*ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++){
            list.add("第" + i + "项");
        }
        scrollSelector.setItemContents(list);*/

        String selectedTown = txtTown.getText().toString();
        villageList = getVillageList(selectedTown);

        txtSex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HelperEditActivity.this);
                builder.setTitle("性别");
                final String[] sexList={"男","女"};
                builder.setItems(sexList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtSex.setText(sexList[which]);
                    }
                });
                builder.show();
            }
        });

        txtTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(HelperEditActivity.this);

                builder.setTitle("选择乡镇");
                //    指定下拉列表的显示数据
                final String[] townList = {"武康街道", "阜溪街道", "舞阳街道", "乾元镇", "新市镇", "禹越镇", "洛舍镇", "新安镇", "莫干山镇", "下渚湖街道", "雷甸镇", "钟管镇"};
                //    设置一个下拉的列表选择项
                builder.setItems(townList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtTown.setText(townList[which]);
                        if (townList[which] != town) {
                            txtVillage.setText("请选择");
                            villageList = getVillageList(townList[which]);
                        }

                    }
                });
                builder.show();
            }
        });

        txtVillage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HelperEditActivity.this);

                builder.setTitle("选择街道");
                //    指定下拉列表的显示数据

                //    设置一个下拉的列表选择项
                builder.setItems(villageList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtVillage.setText(villageList[which]);

                    }
                });
                builder.show();
            }
        });

        //检查身份证
        edtIdentityId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String changedId=edtIdentityId.getText().toString();
                if (getIdChecked(changedId)) {
                    checkId.setVisibility(View.INVISIBLE);
                    infoChecked=true;
                    Toast.makeText(HelperEditActivity.this, date.toString(), Toast.LENGTH_SHORT).show();

                }else {
                    infoChecked=false;
                    checkId.setVisibility(View.VISIBLE);
                }
            }
        });

        edtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String changedPhoneNumber=edtPhoneNumber.getText().toString();
                if (!changedPhoneNumber.equals("")) {
                    txtPhoneNumberChecked.setVisibility(View.INVISIBLE);
                    infoChecked=true;
                }
                if(changedPhoneNumber.indexOf(" ")> -1){
                    txtPhoneNumberChecked.setVisibility(View.VISIBLE);
                    txtPhoneNumberChecked.setText("手机号不能有空格");
                    infoChecked=false;
                }
                if (changedPhoneNumber.equals("")){
                    infoChecked=false;
                    txtPhoneNumberChecked.setText("手机号不能为空");
                    txtPhoneNumberChecked.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helperName = edtHelperName.getText().toString();
                sex = txtSex.getText().toString();
                identityId = edtIdentityId.getText().toString();
                phoneNumber = edtPhoneNumber.getText().toString();
                town = txtTown.getText().toString();
                village = txtVillage.getText().toString();
                if(edtAddr.getText().toString().equals("")){
                addr = txtVillage.getText().toString() + edtAddr.getText().toString();}
                else {
                    addr = edtAddr.getText().toString();
                }
                if(edtPhoneNumber.getText().toString().equals("")||!getIdChecked(edtIdentityId.getText().toString())||phoneNumber.indexOf(" ") > -1){
                    infoChecked=false;
                }
                if(infoChecked){
                SubmitInfo();}

            }
        });

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }





    private void SubmitInfo(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                String url= UrlData.getUrlYy()+"/api/AndroidApi/EditHelperInfo?id="+ID+"&identityId="+identityId+"&trueName="+helperName+"&sex="+sex+"&phoneNumber="+phoneNumber+"&town="+town+"&village="+village+"&addr="+addr;
                String response="";
                Message msg = new Message();
                try{
                    response= MyConnection.setMyHttpClient(url);
                    if (response!=null) {
                        if(response.equals("请求错误")||response.equals("未授权")||response.equals("禁止访问")||response.equals("文件未找到")||response.equals("未知错误")||response.equals("未连接到网络")) {
                            msg.what = 2;
                            msg.obj = response;//返回错误原因
                            Log.e("GetHelperInfo",response);
                        }
                        if(response.equals("验证过期")){
                            //执行token过期的操作
                            msg.what=4;
                            msg.obj=response;
                            Log.e("GetHelperInfo",response);
                        }
                        else {
                            msg.what = 1;
                            msg.obj = response;//返回正常数据
                        }
                    }else {
                        msg.what = 3;
                        Log.e("GetHelperInfo","未连接网络");
                    }
                }
                catch (Exception e) {
                    msg.what = 3;
                    Log.e("GetHelperInfo","未连接网络");
                }
                handler.sendMessage(msg);
            }
        }).start();
    }



    Handler handler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            DialogUtil.closeDialog(mWeiboDialog);
            Model.ResultInfo<LinkedHashMap> list=new Model.ResultInfo<LinkedHashMap>();
            if(msg.what==1)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node=mapper.readTree(msg.obj.toString());
                    list= mapper.readValue(node.toString(),list.getClass());
                    if(list.Success==true)
                    {
                        Toast.makeText(HelperEditActivity.this, list.Msg, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(HelperEditActivity.this, HelperDetailActivity.class);
                        intent.putExtra("Name",helperName);
                        intent.putExtra("Town",town);
                        intent.putExtra("helperId","编号："+ID);
                        HelperEditActivity.this.startActivity(intent);
                        HelperEditActivity.this.finish();
                    }
                    else {
                        Toast.makeText(HelperEditActivity.this, "验证错误", Toast.LENGTH_LONG).show();
                    }

                }
                catch(Exception ex)
                {
                    Toast.makeText(HelperEditActivity.this,"出现未知异常，请联系管理员!",Toast.LENGTH_LONG).show();
                }
            }
            if(msg.what==2)
            {
                Toast.makeText(HelperEditActivity.this,msg.obj.toString(),Toast.LENGTH_LONG).show();
            }
            if(msg.what==3)
            {
                Toast.makeText(HelperEditActivity.this,"未连接到网络",Toast.LENGTH_LONG).show();
            }
            if(msg.what==4)
            {
                Toast.makeText(HelperEditActivity.this,"验证过期",Toast.LENGTH_LONG).show();
            }
        }
    };


    private Boolean getIdChecked(String changedId){
        String id=changedId;
        String Ai = "";
        if (id.length() == 18) {
            Ai = id.substring(0, 17);
            if(isDate(Ai)){
                return true;
            }else{
                return false;
            }
        } else if (id.length() == 15) {
            Ai = id.substring(0, 6) + "19" +id.substring(6, 15);
            if(isDate(Ai)){
                return true;
            }else{
                return false;
            }
        }

        //String strYear = Ai.substring(6, 10);// 年份
        //String strMonth = Ai.substring(10, 12);// 月份
        //String strDay = Ai.substring(12, 14);// 月份
        //if(isDate(strYear + "-" + strMonth + "-" + strDay) == false){
        //return  false;
        //}

        if(id.length() != 15 && id.length() != 18){
            return false;
        }
        else {
            return true;
        }


    }

    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    private  boolean isDate(String Ai) {

        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// 月份
        String time=strYear+"-"+strMonth+"-"+strDay;
        int intMonth= Integer.parseInt(strMonth);
        int intDay=Integer.parseInt(strDay);
        int intYear=Integer.parseInt(strYear);
        if(intMonth>12){
            return false;
        }

        if (isNumeric(Ai)==false){
            return false;
        }
        else {
            try{
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                this.date = format.parse(time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                if(day!=intDay){
                    return false;
                }
                else{
                    return true;
                }

            }catch (Exception e){
                return false;
            }
        }
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
    @Override
    protected void onPause() {
        super.onPause();
        HelperEditActivity.this.finish();
    }
}
