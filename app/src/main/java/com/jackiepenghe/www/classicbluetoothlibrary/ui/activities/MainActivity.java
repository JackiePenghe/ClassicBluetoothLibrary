package com.jackiepenghe.www.classicbluetoothlibrary.ui.activities;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.www.classicbluetoothlibrary.R;
import com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.a2dp.A2dpDeviceListActivity;
import com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.bound.BoundDeviceListActivity;
import com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.socket_client.SocketClientDeviceListActivity;
import com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.socket_service.SocketServiceActivity;

import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothManager;

/**
 * 主界面
 *
 * @author jackie
 */
public class MainActivity extends BaseAppCompatActivity {

    /*---------------------------成员变量---------------------------*/

    /**
     * 创建服务端，连接服务端的按钮
     */
    private Button boundDeviceBtn,createServiceBtn, connectServiceBtn,a2dpBtn;
    /**
     * 点击事件的监听
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = null;
            switch (view.getId()) {
                case R.id.bound_device_btn:
                    intent = new Intent(MainActivity.this,BoundDeviceListActivity.class);
                    break;
                case R.id.create_service_btn:
                    intent  = new Intent(MainActivity.this, SocketServiceActivity.class);
                    break;
                case R.id.connect_service_btn:
                    intent = new Intent(MainActivity.this, SocketClientDeviceListActivity.class);
                    break;
                case R.id.a2dp_btn:
                    intent = new Intent(MainActivity.this, A2dpDeviceListActivity.class);
                    break;
                default:
                    break;
            }

            if (intent != null) {
                startActivity(intent);
            }
        }
    };

    /*---------------------------实现父类方法---------------------------*/

    /**
     * 标题栏的返回按钮被按下的时候回调此函数
     */
    @Override
    protected void titleBackClicked() {
        onBackPressed();
    }

    /**
     * 在设置布局之前需要进行的操作
     */
    @Override
    protected void doBeforeSetLayout() {

    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_main;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {

    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        boundDeviceBtn = findViewById(R.id.bound_device_btn);
        createServiceBtn = findViewById(R.id.create_service_btn);
        connectServiceBtn = findViewById(R.id.connect_service_btn);
        a2dpBtn = findViewById(R.id.a2dp_btn);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {

    }

    /**
     * 初始化其他数据
     */
    @Override
    protected void initOtherData() {

    }

    /**
     * 初始化事件
     */
    @Override
    protected void initEvents() {
        boundDeviceBtn.setOnClickListener(onClickListener);
        createServiceBtn.setOnClickListener(onClickListener);
        connectServiceBtn.setOnClickListener(onClickListener);
        a2dpBtn.setOnClickListener(onClickListener);
    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {

    }

    /**
     * 设置菜单
     *
     * @param menu 菜单
     * @return 只是重写 public boolean onCreateOptionsMenu(Menu menu)
     */
    @Override
    protected boolean createOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * 设置菜单监听
     *
     * @param item 菜单的item
     * @return true表示处理了监听事件
     */
    @Override
    protected boolean optionsItemSelected(MenuItem item) {
        return false;
    }

    /*---------------------------重写父类方法---------------------------*/

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ClassicBluetoothManager.releaseAll();
    }
}
