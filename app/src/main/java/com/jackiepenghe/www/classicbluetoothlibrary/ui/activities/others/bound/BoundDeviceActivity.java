package com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.bound;

import android.bluetooth.BluetoothDevice;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.www.classicbluetoothlibrary.R;
import com.jackiepenghe.www.classicbluetoothlibrary.utils.Constants;

import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothBounder;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothConstants;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothInterface;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothManager;

/**
 * 连接设备的界面
 *
 * @author jackie
 */
public class BoundDeviceActivity extends BaseAppCompatActivity {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = BoundDeviceActivity.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 蓝牙设备
     */
    private BluetoothDevice bluetoothDevice;

    /**
     * 绑定，解除绑定按钮
     */
    private Button boundBtn, unboundBtn;

    /**
     * 绑定状态的提示文本
     */
    private TextView textView;

    /**
     * 蓝牙绑定器
     */
    private ClassicBluetoothBounder classicBluetoothBounder;

    /**
     * 蓝牙绑定回调
     */
    private ClassicBluetoothInterface.OnDeviceBondStateChangedListener onBondStateChangedListener = new ClassicBluetoothInterface.OnDeviceBondStateChangedListener() {
        @Override
        public void onDeviceBinding() {
            textView.setText("绑定中");
            Tool.warnOut(TAG, "绑定成功");
        }

        @Override
        public void onDeviceBonded() {
            Tool.warnOut(TAG, "绑定成功");
            textView.setText("绑定成功");
        }

        @Override
        public void onDeviceBindNone() {
            textView.setText("取消绑定/绑定失败/未绑定");
            Tool.warnOut(TAG, "取消绑定/绑定失败/未绑定");
        }
    };
    /**
     * 点击事件的监听
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bound_btn:
                    bound();
                    break;
                case R.id.unbound_btn:
                    unbound();
                    break;
                default:
                    break;
            }
        }
    };
    private ClassicBluetoothInterface.OnBounderCloseCompleteListener onBounderCloseCompleteListener = new ClassicBluetoothInterface.OnBounderCloseCompleteListener() {
        @Override
        public void onBounderCloseComplete() {
            BoundDeviceActivity.super.onBackPressed();
        }
    };

    /*---------------------------实现父类方法---------------------------*/

    @Override
    protected void titleBackClicked() {
        onBackPressed();
    }

    @Override
    protected void doBeforeSetLayout() {
        Parcelable parcelableExtra = getIntent().getParcelableExtra(Constants.DEVICE);
        if (parcelableExtra instanceof BluetoothDevice) {
            bluetoothDevice = (BluetoothDevice) parcelableExtra;
        }
    }

    @Override
    protected int setLayout() {
        return R.layout.activity_bound_device;
    }

    @Override
    protected void doBeforeInitOthers() {
        initClassicBluetoothConnector();
    }

    @Override
    protected void initViews() {
        boundBtn = findViewById(R.id.bound_btn);
        unboundBtn = findViewById(R.id.unbound_btn);
        textView = findViewById(R.id.bound_status_tv);
    }

    @Override
    protected void initViewData() {

    }

    @Override
    protected void initOtherData() {

    }

    @Override
    protected void initEvents() {
        boundBtn.setOnClickListener(onClickListener);
        unboundBtn.setOnClickListener(onClickListener);
    }

    @Override
    protected void doAfterAll() {
    }

    @Override
    protected boolean createOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected boolean optionsItemSelected(MenuItem menuItem) {
        return false;
    }

    /*---------------------------重写父类方法---------------------------*/

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if (!classicBluetoothBounder.close()){
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothDevice = null;
        classicBluetoothBounder.setOnBondStateChangedListener(null);
        onBondStateChangedListener = null;
        classicBluetoothBounder = null;
        ClassicBluetoothManager.releaseClassicBluetoothConnector();
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化连接器
     */
    private void initClassicBluetoothConnector() {
        if (bluetoothDevice == null) {
            return;
        }
        classicBluetoothBounder = ClassicBluetoothManager.getClassicBluetoothBounderInstance(BoundDeviceActivity.this);
        if (classicBluetoothBounder == null) {
            return;
        }
        classicBluetoothBounder.setOnBondStateChangedListener(onBondStateChangedListener);
        classicBluetoothBounder.setOnBounderCloseCompleteListener(onBounderCloseCompleteListener);
    }

    /**
     * 发起绑定请求
     */
    private void bound() {

        int boundStatus = classicBluetoothBounder.startBound(bluetoothDevice.getAddress());

        /*
         * 调用绑定的方法（如果需要绑定)，否则请直接调用连接的方法
         * 注意：如果该设备不支持绑定，会直接回调绑定成功的回调，在绑定成功的回调中发起连接即可
         * 第一次绑定某一个设备会触发回调，之后再次绑定，可根据绑定时的函数的返回值来判断绑定状态，以进行下一步操作
         */
        switch (boundStatus) {
            case ClassicBluetoothConstants.DEVICE_BOND_START_SUCCESS:
                Tool.warnOut(TAG, "开始绑定");
                textView.setText("开始绑定");
                break;
            case ClassicBluetoothConstants.DEVICE_BOND_START_FAILED:
                textView.setText("发起绑定失败");
                Tool.warnOut(TAG, "发起绑定失败");
                break;
            case ClassicBluetoothConstants.DEVICE_BOND_BONDED:
                textView.setText("此设备已经被绑定了");
                Tool.warnOut(TAG, "此设备已经被绑定了");
                break;
            case ClassicBluetoothConstants.DEVICE_BOND_BONDING:
                textView.setText("此设备正在绑定中");
                Tool.warnOut(TAG, "此设备正在绑定中");
                break;
            case ClassicBluetoothConstants.BLUETOOTH_ADAPTER_NULL:
                textView.setText("没有蓝牙适配器存在");
                Tool.warnOut(TAG, "没有蓝牙适配器存在");
                break;
            case ClassicBluetoothConstants.BLUETOOTH_ADDRESS_INCORRECT:
                textView.setText("蓝牙地址错误");
                Tool.warnOut(TAG, "蓝牙地址错误");
                break;
            case ClassicBluetoothConstants.BLUETOOTH_MANAGER_NULL:
                textView.setText("没有蓝牙管理器存在");
                Tool.warnOut(TAG, "没有蓝牙管理器存在");
                break;
            default:
                String text = "其他错误 " + boundStatus;
                textView.setText(text);
                Tool.warnOut(TAG, "default");
                break;
        }
    }

    /**
     * 解除绑定
     */
    private void unbound() {
        boolean unbound = classicBluetoothBounder.unbound();
        if (unbound){
            Tool.toastL(BoundDeviceActivity.this,R.string.unbound_success);
        }else {
            Tool.toastL(BoundDeviceActivity.this,R.string.unbound_failed);
        }
    }
}
