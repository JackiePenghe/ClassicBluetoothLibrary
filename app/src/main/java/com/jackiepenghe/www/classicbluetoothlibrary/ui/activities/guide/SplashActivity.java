package com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.guide;

import android.content.Intent;

import com.jackiepenghe.baselibrary.BaseSplashActivity;

/**
 * 防止启动黑白屏的界面
 *
 * @author jackie
 */
public class SplashActivity extends BaseSplashActivity {
    /**
     * 在本界面第一次启动时执行的操作
     */
    @Override
    protected void onCreate() {
        Intent intent = new Intent(SplashActivity.this,WelcomeActivity.class);
        startActivity(intent);
        onBackPressed();
    }
}
