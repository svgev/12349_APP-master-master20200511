package cn.deesoft.serviceplatform;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import Util.HttpUtil;

public class AdminInfoActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private TextView txtUserName;
    private TextView txtTrueName;
    private TextView txtTown;
    private TextView txtRoles;
    private TextView txtAddr;
    private TextView txtArea;
    private Button adminQuit;
    private ImageView imgPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_info);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        adminQuit=findViewById(R.id.btnAdminQuit);
        imgPhoto=findViewById(R.id.imgPhoto);
        txtUserName=findViewById(R.id.txtUserName);
        txtTrueName=findViewById(R.id.txtTrueName);
        txtRoles=findViewById(R.id.txtRole);
        txtTown=findViewById(R.id.txtArea);
        txtAddr=findViewById(R.id.txtAddr);
        txtArea=findViewById(R.id.txtArea);


        sp = this.getSharedPreferences("adminInfo", Context.MODE_PRIVATE);
        txtUserName.setText("用户名： "+sp.getString("AdminName",""));
        txtTrueName.setText(sp.getString("TrueName",""));
        txtRoles.setText(sp.getString("Roles",""));
        txtTown.setText(sp.getString("Town",""));
        txtAddr.setText(sp.getString("Addr",""));
        txtArea.setText(sp.getString("Area",""));



        Glide.with(AdminInfoActivity.this).load(R.mipmap.nophoto2).into(imgPhoto);

        adminQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HttpUtil.isFastClick()) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.commit();
                    //editor.putBoolean("AutoLogin",false);
                    Intent intent = new Intent(AdminInfoActivity.this, AdminLogActivity.class);
                    startActivity(intent);
                    AdminInfoActivity.this.finish();
                    AdminAreaActivity.adminAreaActivity.finish();

                }
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


    }
