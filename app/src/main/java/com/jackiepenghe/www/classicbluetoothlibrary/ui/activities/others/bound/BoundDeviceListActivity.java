package com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.bound;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.DefaultItemDecoration;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.www.classicbluetoothlibrary.R;
import com.jackiepenghe.www.classicbluetoothlibrary.adapter.DeviceListRecyclerViewAdapter;
import com.jackiepenghe.www.classicbluetoothlibrary.utils.Constants;

import java.util.ArrayList;

import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothInterface;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothManager;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothScanner;

/**
 * 绑定设备之前，扫描到的蓝牙设备的列表
 *
 * @author jackie
 */
public class BoundDeviceListActivity extends BaseAppCompatActivity {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG= BoundDeviceListActivity.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 经典蓝牙扫描器
     */
    private ClassicBluetoothScanner classicBluetoothScanner;
    /**
     * 按钮
     */
    private Button button;
    /**
     * 记录按钮的点击次数
     */
    private int clickCount;
    /**
     * 设备列表
     */
    private RecyclerView recyclerView;

    /**
     * RecyclerView的装饰
     */
    private DefaultItemDecoration defaultItemDecoration = new DefaultItemDecoration(Color.GRAY, ViewGroup.LayoutParams.MATCH_PARENT, 1, -1);

    /**
     * 适配器的数据源
     */
    private ArrayList<BluetoothDevice> adapterList = new ArrayList<>();

    /**
     * 适配器
     */
    private DeviceListRecyclerViewAdapter deviceListRecyclerViewAdapter = new DeviceListRecyclerViewAdapter(adapterList);

    /**
     * 点击事件的监听
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.scan_btn:
                    if (classicBluetoothScanner == null) {
                        return;
                    }
                    int two = 2;
                    if (clickCount % two == 0) {
                        button.setText(R.string.stop_scan);
                        adapterList.clear();
                        deviceListRecyclerViewAdapter.notifyDataSetChanged();
                        if (classicBluetoothScanner.startScan()){
                            Tool.warnOut(TAG,"开启成功");
                        }else {
                            Tool.warnOut(TAG,"开启失败");
                        }
                    } else {
                        button.setText(R.string.start_scan);
                        if (classicBluetoothScanner.stopScan()){
                            Tool.warnOut(TAG,"停止成功");
                        }else {
                            Tool.warnOut(TAG,"停止失败");
                        }
                    }
                    clickCount ++;
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 发现一个新的设备时进行的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener = new ClassicBluetoothInterface.OnScanFindOneNewDeviceListener() {
        @Override
        public void onScanFindOneNewDevice(BluetoothDevice bluetoothDevice) {
            adapterList.add(bluetoothDevice);
            deviceListRecyclerViewAdapter.notifyItemChanged(adapterList.size() - 1);
        }
    };

    /**
     * 扫描状态被改变时的回调
     */
    private ClassicBluetoothInterface.OnScanStatusChangedListener onScanStatusChangedListener = new ClassicBluetoothInterface.OnScanStatusChangedListener() {
        @Override
        public void onScanStarted() {
            Tool.warnOut(TAG,"onScanStarted");
        }

        @Override
        public void onScanFinished() {
            Tool.warnOut(TAG,"onScanFinished");
            if (button.getText().toString().equals(getString(R.string.start_scan))){
                return;
            }
            clickCount++;
            button.setText(R.string.start_scan);
        }
    };

    /**
     * 列表中的子选项被点击的监听
     */
    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            BluetoothDevice bluetoothDevice = adapterList.get(position);
            Intent intent = new Intent(BoundDeviceListActivity.this, BoundDeviceActivity.class);
            intent.putExtra(Constants.DEVICE, bluetoothDevice);
            if (classicBluetoothScanner.isScanning()){
                classicBluetoothScanner.stopScan();
            }
            startActivity(intent);
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
        initBluetoothScanner();
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_bound_device_list;
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
        recyclerView = findViewById(R.id.recycler_view);
        button = findViewById(R.id.scan_btn);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        initRecyclerViewData();
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
        button.setOnClickListener(onClickListener);
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
        classicBluetoothScanner.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        classicBluetoothScanner.setOnScanFindOneNewDeviceListener(null);
        onScanFindOneNewDeviceListener = null;
        classicBluetoothScanner.setOnScanStatusChangedListener(null);
        onScanStatusChangedListener = null;
        classicBluetoothScanner = null;
        button.setOnClickListener(null);
        onClickListener = null;
        button = null;
        clickCount = 0;
        recyclerView.setLayoutManager(null);
        recyclerView.removeItemDecoration(defaultItemDecoration);
        defaultItemDecoration = null;
        recyclerView.setAdapter(null);
        deviceListRecyclerViewAdapter.setOnItemClickListener(null);
        onItemClickListener = null;
        deviceListRecyclerViewAdapter = null;
        recyclerView = null;
        adapterList.clear();
        adapterList = null;
        ClassicBluetoothManager.releaseClassicBluetoothScanner();
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化经典蓝牙扫描器
     */
    private void initBluetoothScanner() {
        classicBluetoothScanner = ClassicBluetoothManager.getBluetoothScannerInstance(BoundDeviceListActivity.this);
        if (classicBluetoothScanner == null) {
            return;
        }
        classicBluetoothScanner.setOnScanFindOneNewDeviceListener(onScanFindOneNewDeviceListener);
        classicBluetoothScanner.setOnScanStatusChangedListener(onScanStatusChangedListener);
        classicBluetoothScanner.init();
    }

    /**
     * 初始化列表数据
     */
    private void initRecyclerViewData() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BoundDeviceListActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(defaultItemDecoration);
        deviceListRecyclerViewAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(deviceListRecyclerViewAdapter);
    }
}
