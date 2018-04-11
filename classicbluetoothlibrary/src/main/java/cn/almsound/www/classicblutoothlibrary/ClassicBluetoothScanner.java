package cn.almsound.www.classicblutoothlibrary;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Set;

/**
 * 经典蓝牙扫描实例
 *
 * @author jackie
 */
public class ClassicBluetoothScanner {

    /*---------------------------成员变量---------------------------*/

    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * 上下文
     */
    private Context context;

    /**
     * 记录初始化的状态
     */
    private boolean initSatatus;

    /**
     * 监测扫描相关数据与状态的广播接收者
     */
    private ClassicBluetoothScanStatusBroadcastReceiver classicBluetoothScanBroadcastReceiver;

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    ClassicBluetoothScanner(Context context) {
        //获取蓝牙适配器
        bluetoothAdapter = ClassicBluetoothManager.getBluetoothAdapterInstance();

        //判空
        if (bluetoothAdapter == null) {
            Tool.toastL(context, R.string.no_bluetooth_module);
            return;
        }

        //判断蓝牙开关状态
        if (!bluetoothAdapter.isEnabled()) {
            if (context instanceof Activity) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(intent);
            } else {
                bluetoothAdapter.enable();
            }
        }

        classicBluetoothScanBroadcastReceiver = new ClassicBluetoothScanStatusBroadcastReceiver();
        this.context = context;
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 获得广播接收者需要监测的IntentFilter
     *
     * @return IntentFilter
     */
    private IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        return intentFilter;
    }

    /*---------------------------公开方法---------------------------*/

    /**
     * 默认的初始化
     *
     * @return true表示初始化成功
     */
    public boolean init() {
        return init(new ArrayList<BluetoothDevice>());
    }

    /**
     * 初始化
     *
     * @param scanResult 扫描的设备的结果集合
     * @return true表示初始化成功
     */
    public boolean init(@NonNull ArrayList<BluetoothDevice> scanResult) {
        return init(scanResult, new ArrayList<BluetoothDevice>());
    }

    /**
     * 初始化
     *
     * @param scanResult           扫描的设备的结果集合
     * @param boundedDevicesResult 存储被绑定的设备的扫描结果的集合
     * @return true表示初始化成功
     */
    public boolean init(@NonNull ArrayList<BluetoothDevice> scanResult, @NonNull ArrayList<BluetoothDevice> boundedDevicesResult) {
        return init(scanResult, boundedDevicesResult, new ArrayList<BluetoothDevice>());
    }

    /**
     * 初始化
     *
     * @param scanResult           扫描的设备的结果集合
     * @param boundedDevicesResult 存储被绑定的设备的扫描结果的集合
     * @return true表示初始化成功
     */
    public boolean init(@NonNull ArrayList<BluetoothDevice> scanResult, @NonNull ArrayList<BluetoothDevice> boundedDevicesResult, @NonNull ArrayList<BluetoothDevice> unboundDevicesResult) {
        return init(scanResult, boundedDevicesResult, unboundDevicesResult, new ArrayList<BluetoothDevice>());
    }

    /**
     * 初始化
     *
     * @param scanResult           存储设备扫描结果的集合
     * @param boundedDevicesResult 存储被绑定的设备的扫描结果的集合
     * @return true表示初始化成功
     */
    public boolean init(@NonNull ArrayList<BluetoothDevice> scanResult, @NonNull ArrayList<BluetoothDevice> boundedDevicesResult, @NonNull ArrayList<BluetoothDevice> unboundDevicesResult, @NonNull ArrayList<BluetoothDevice> boundingDevicesResult) {
        if (context == null) {
            return false;
        }
        classicBluetoothScanBroadcastReceiver.setBluetoothDevices(scanResult);
        classicBluetoothScanBroadcastReceiver.setBoundedDevices(boundedDevicesResult);
        classicBluetoothScanBroadcastReceiver.setBoundingDevices(boundingDevicesResult);
        classicBluetoothScanBroadcastReceiver.setUnboundedDevices(unboundDevicesResult);
        context.registerReceiver(classicBluetoothScanBroadcastReceiver, makeIntentFilter());
        initSatatus = true;
        return true;
    }

    /**
     * 开始扫描
     *
     * @return true表示成功
     */
    public boolean startScan() {
        if (bluetoothAdapter == null) {
            return false;
        }

        if (!initSatatus) {
            throw new IllegalStateException("please invoke method startScan() after invoke method init()" +
                    " or \n init(@NonNull ArrayList<BluetoothDevice> scanResult, @NonNull ArrayList<BluetoothDevice> boundedDevicesResult)");
        }

        return bluetoothAdapter.startDiscovery();
    }

    /**
     * 停止扫描
     *
     * @return true表示成功
     */
    public boolean stopScan() {
        return bluetoothAdapter != null && bluetoothAdapter.cancelDiscovery();
    }

    /**
     * 关闭扫描器
     */
    public void close() {
        if (bluetoothAdapter == null) {
            return;
        }
        if (!initSatatus) {
            return;
        }
        bluetoothAdapter.cancelDiscovery();
        if (context == null) {
            return;
        }
        context.unregisterReceiver(classicBluetoothScanBroadcastReceiver);
        bluetoothAdapter = null;
        initSatatus = false;
        context = null;
    }

    /**
     * 查看当前是否正在扫描
     *
     * @return true表示正在扫描
     */
    public boolean isScanning() {
        return bluetoothAdapter != null && bluetoothAdapter.isDiscovering();
    }

    /**
     * 获取已绑定
     */
    public Set<BluetoothDevice> getBoundedDevices() {
        if (bluetoothAdapter == null) {
            return null;
        }
        return bluetoothAdapter.getBondedDevices();
    }

    /**
     * 设置发现一个设备时的回调
     *
     * @param onScanFindOneDeviceListener 发现一个设备时的回调
     */
    public void setOnScanFindOneDeviceListener(ClassicBluetoothInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanFindOneDeviceListener(onScanFindOneDeviceListener);
    }

    /**
     * 设置扫描状态更改时的回调
     *
     * @param onScanStatusChangedListener 扫描状态更改时的回调
     */
    public void setOnScanStatusChangedListener(ClassicBluetoothInterface.OnScanStatusChangedListener onScanStatusChangedListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanStatusChangedListener(onScanStatusChangedListener);
    }

    /**
     * 设置发现一个新设备时的回调
     *
     * @param onScanFindOneNewDeviceListener 发现一个新设备时的回调
     */
    public void setOnScanFindOneNewDeviceListener(ClassicBluetoothInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanFindOneNewDeviceListener(onScanFindOneNewDeviceListener);
    }

    /**
     * 设置发现一个被绑定的设备时的回调
     *
     * @param onScanFindOneBoundedDeviceListener 发现一个被绑定的设备时的回调
     */
    public void setOnScanFindOneBoundedDeviceListener(ClassicBluetoothInterface.OnScanFindOneBoundedDeviceListener onScanFindOneBoundedDeviceListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanFindOneBoundedDeviceListener(onScanFindOneBoundedDeviceListener);
    }

    /**
     * 设置发现一个新的被绑定的设备时的回调
     *
     * @param onScanFindOneNewBoundedDeviceListener 发现一个新的被绑定的设备时的回调
     */
    public void setOnScanFindOneNewBoundedDeviceListener(ClassicBluetoothInterface.OnScanFindOneNewBoundedDeviceListener onScanFindOneNewBoundedDeviceListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanFindOneNewBoundedDeviceListener(onScanFindOneNewBoundedDeviceListener);
    }

    /**
     * 设置发现一个绑定中的设备时的回调
     *
     * @param onScanFindOneBoundingDeviceListener 发现一个绑定中的设备时的回调
     */
    public void setOnScanFindOneBoundingDeviceListener(ClassicBluetoothInterface.OnScanFindOneBoundingDeviceListener onScanFindOneBoundingDeviceListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanFindOneBoundingDeviceListener(onScanFindOneBoundingDeviceListener);
    }

    /**
     * 设置发现一个新的绑定中的设备时的回调
     *
     * @param onScanFindOneNewBoundingDeviceListener 发现一个新的绑定中的设备时的回调
     */
    public void setOnScanFindOneNewBoundingDeviceListener(ClassicBluetoothInterface.OnScanFindOneNewBoundingDeviceListener onScanFindOneNewBoundingDeviceListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanFindOneNewBoundingDeviceListener(onScanFindOneNewBoundingDeviceListener);
    }

    /**
     * 设置发现一个未绑定的设备时的回调
     *
     * @param onScanFindOneUnboundedDeviceListener 发现一个未绑定的设备时的回调
     */
    public void setOnScanFindOneUnboundedDeviceListener(ClassicBluetoothInterface.OnScanFindOneUnboundedDeviceListener onScanFindOneUnboundedDeviceListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanFindOneUnboundedDeviceListener(onScanFindOneUnboundedDeviceListener);
    }

    /**
     * 设置发现一个新的未绑定的设备时的回调
     *
     * @param onScanFindOneNewUnboundedDeviceListener 发现一个新的未绑定的设备时的回调
     */
    public void setOnScanFindOneNewUnboundedDeviceListener(ClassicBluetoothInterface.OnScanFindOneNewUnboundedDeviceListener onScanFindOneNewUnboundedDeviceListener) {
        classicBluetoothScanBroadcastReceiver.setOnScanFindOneNewUnboundedDeviceListener(onScanFindOneNewUnboundedDeviceListener);
    }
}
