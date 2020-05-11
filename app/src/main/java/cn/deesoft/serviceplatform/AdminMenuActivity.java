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
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class AdminMenuActivity extends AppCompatActivity {

  private Button adminOlder, adminHelper, adminOrder;
  private TextView txtUserName;
  private ImageView imgPhoto;
  private SharedPreferences sp;
  private TextView txtArea;
  private String userName;
  private String adminArea;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin_menu);
    adminOlder = (Button) findViewById(R.id.bt_admin_older);
    adminOrder = (Button) findViewById(R.id.bt_admin_order);
    adminHelper = (Button) findViewById(R.id.bt_admin_helper);
    txtUserName = findViewById(R.id.txtName);
    imgPhoto = findViewById(R.id.imgPhoto);
    //txtArea=findViewById(R.id.txtArea);

    Intent intent = getIntent();
    adminArea = intent.getStringExtra("Area");
    //txtArea.setText(adminArea);


    sp = this.getSharedPreferences("adminInfo", Context.MODE_PRIVATE);
    Glide.with(AdminMenuActivity.this).load(R.mipmap.nophoto2).into(imgPhoto);

    userName = sp.getString("AdminName", "");
    txtUserName.setText(adminArea);

    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if(actionBar!=null){
      actionBar.setHomeButtonEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("当前片区： "+adminArea);
    }


    imgPhoto.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(AdminMenuActivity.this, AdminInfoActivity.class);
        intent.putExtra("Area", adminArea);
        startActivity(intent);
      }
    });

    adminOlder.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(AdminMenuActivity.this, OlderListActivity.class);
        intent.putExtra("Area", adminArea);
        startActivity(intent);
      }
    });
    adminHelper.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(AdminMenuActivity.this, HelperListActivity.class);
        intent.putExtra("Area", adminArea);
        startActivity(intent);
      }
    });

    adminOrder.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(AdminMenuActivity.this, OrderListActivity.class);
        intent.putExtra("Area", adminArea);
        startActivity(intent);
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

