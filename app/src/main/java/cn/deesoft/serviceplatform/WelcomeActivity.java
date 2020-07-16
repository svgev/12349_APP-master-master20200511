package cn.deesoft.serviceplatform;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.amap.api.maps.InfoWindowAnimationManager;

import org.apache.http.impl.auth.win.WindowsCredentialsProvider;

public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window=getWindow();

        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置全屏
        int flag=WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag,flag);
        setContentView(R.layout.activity_welcome);
        handler.sendEmptyMessageDelayed(0,2000);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getHome();
            super.handleMessage(msg);
        }
    };

    public void getHome(){
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除消息
        handler.removeCallbacksAndMessages(null);
    }


    //检查是否第一次启动程序
    public class PrefManager {
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        Context _context;

        int PRIVATE_MODE = 0;

        //SharedPreferences 文件名
        private static final String PREF_NAME = "intro_slider";

        private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

        public PrefManager(Context context){
            this._context = context;
            pref = _context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
            editor = pref.edit();
        }

        public void setFirstTimeLaunch(boolean isFirstTime){
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
            editor.commit();
        }

        public boolean isFirstTimeLaunch(){
            return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
        }

    }
    //设置状态栏透明
    private void changeStatusBarColor(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
