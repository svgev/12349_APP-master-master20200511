package cn.deesoft.serviceplatform;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;

import java.util.ArrayList;
import java.util.List;

public class AdminAreaActivity extends AppCompatActivity {

    public static AdminAreaActivity adminAreaActivity;
    private SharedPreferences sp;
    String adminArea;
    private String adminName;
    private String[] areaList;
    private LinearLayout mBtnListLayout = null;
    private ImageView imgPhoto;
    private TextView txtAdminName;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_area);
        adminAreaActivity=this;
        sp = this.getSharedPreferences("adminInfo", Context.MODE_PRIVATE);
        //adminArea=sp.getString("Area","");
        adminName=sp.getString("AdminName","");
        adminArea=sp.getString("Area", "");
        //adminArea="新市镇,雷甸镇,舞阳街道";
        areaList=adminArea.split("[,]");
        scrollView = (ScrollView) findViewById(R.id.myScrollView);
        mBtnListLayout =findViewById(R.id.btnListLayout);

        imgPhoto = findViewById(R.id.imgPhoto);
        txtAdminName=findViewById(R.id.txtAdminName);
        txtAdminName.setText(adminName);
        Glide.with(AdminAreaActivity.this).load(R.mipmap.nophoto2).into(imgPhoto);


        int index = 0;
        for( String btnContent : areaList ){
            Button codeBtn = new Button( this );
            setBtnAttribute( codeBtn, btnContent, index, Color.TRANSPARENT, Color.BLACK, 24 );
            mBtnListLayout.addView( codeBtn );
            index++;
        }

        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(AdminAreaActivity.this,AdminInfoActivity.class);
                AdminAreaActivity.this.startActivity(intent);
                AdminAreaActivity.this.finish();
            }
        });

    }

    private void setBtnAttribute(final Button codeBtn, String btnContent, int id, int backGroundColor, int textColor, int textSize ){
        if( null == codeBtn ){
            return;
        }

        codeBtn.setBackgroundColor(0xFFFFC0CB);
        codeBtn.setTextColor( ( textColor >= 0 )?textColor:Color.WHITE);
        codeBtn.setTextSize( ( textSize > 16 )?textSize:24 );
        codeBtn.setId( id );
        codeBtn.setText( btnContent );
        codeBtn.setTextSize(19);


        codeBtn.setBackgroundResource(R.drawable.button_area);
        codeBtn.setWidth(560);
        codeBtn.setHeight(80);
        codeBtn.setPadding(0,20,0,4);

        codeBtn.setGravity(Gravity.CENTER_HORIZONTAL);
        codeBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // btn click process
            }
        });

        MarginLayoutParams mp = new MarginLayoutParams(650,125);
        mp.setMargins(0, 43, 0, 0);

        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(mp);


        codeBtn.setLayoutParams( rlp );

        codeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectArea=codeBtn.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("Area", codeBtn.getText());
                intent.setClass(AdminAreaActivity.this, AdminMenuActivity.class);
                AdminAreaActivity.this.startActivity(intent);
            }
        });

    }


}