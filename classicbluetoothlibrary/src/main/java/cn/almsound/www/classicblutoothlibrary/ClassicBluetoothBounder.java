package cn.almsound.www.classicblutoothlibrary;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * BLE连接器
 *
 * @author alm
 */

public class ClassicBluetoothBounder {

    /*-------------------------成员变量-------------------------*/

    private static final String TAG = ClassicBluetoothBounder.class.getSimpleName();

    /*-------------------------成员变量-------------------------*/

    /**
     * 上下文引用
     */
    private Context context;

    /**
     * BLE绑定的广播接收者
     */
    private BoundStatusBroadcastReceiver boundStatusBroadcastReceiver;
    /**
     * 绑定的设备的地址
     */
    private String bondAddress;
    /**
     * 记录当前的关闭状态
     */
    private boolean closed;

    /**
     * 绑定器被成功关闭时的回调
     */
    private ClassicBluetoothInterface.OnBounderCloseCompleteListener onBounderCloseCompleteListener;
    /**
     * Handler,处理回调，将回调post到主线程中进行
     */
    private Handler handler = new Handler();

    /*-------------------------构造函数-------------------------*/

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    ClassicBluetoothBounder(Context context) {
        this.context = context;
        boundStatusBroadcastReceiver = new BoundStatusBroadcastReceiver();
    }

    /*-------------------------私有函数-------------------------*/

    /**
     * 通过设备地址直接解绑某个设备
     *
     * @param context 上下文
     * @param address 设备地址
     * @return true表示成功解绑
     */
    public static boolean unbound(Context context, String address) {

        if (context == null) {
            return false;
        }

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            return false;
        }

        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);

        if (remoteDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            return false;
        }

        Method removeBondMethod;
        boolean result = false;
        try {
            //noinspection JavaReflectionMemberAccess
            removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            result = (boolean) removeBondMethod.invoke(remoteDevice);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 广播接收者Action过滤器
     *
     * @return 接收者Action过滤器
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private IntentFilter makeBoundStatusIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);
        return intentFilter;
    }

    /**
     * 检查关闭状况（用于调用回调）
     */
    private void checkCloseStatus() {
        closed = true;
        if (onBounderCloseCompleteListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onBounderCloseCompleteListener.onBounderCloseComplete();
                }
            });
        }
    }

    /*-------------------------公开函数-------------------------*/

    /**
     * 发起设备绑定
     *
     * @param address 设备地址
     * @return BleConstants中定义的常量
     * {@link ClassicBluetoothConstants#BLUETOOTH_ADDRESS_INCORRECT} 设备地址错误
     * {@link ClassicBluetoothConstants#BLUETOOTH_MANAGER_NULL} 没有蓝牙管理器
     * {@link ClassicBluetoothConstants#BLUETOOTH_ADAPTER_NULL} 没有蓝牙适配器
     * {@link ClassicBluetoothConstants#DEVICE_BOND_BONDED} 该设备已被绑定
     * {@link ClassicBluetoothConstants#DEVICE_BOND_BONDING} 该设备正在进行绑定（或正在向该设备发起绑定）
     * {@link ClassicBluetoothConstants#DEVICE_BOND_START_SUCCESS} 成功发起绑定请求
     * {@link ClassicBluetoothConstants#DEVICE_BOND_START_FAILED} 发起绑定请求失败
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public int startBound(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return ClassicBluetoothConstants.BLUETOOTH_ADDRESS_INCORRECT;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            return ClassicBluetoothConstants.BLUETOOTH_MANAGER_NULL;
        }

        BluetoothAdapter bluetoothAdapter =ClassicBluetoothManager.getBluetoothAdapterInstance();

        if (bluetoothAdapter == null) {
            return ClassicBluetoothConstants.BLUETOOTH_ADAPTER_NULL;
        }


        bondAddress = address;
        closed = false;

        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        switch (remoteDevice.getBondState()) {
            case BluetoothDevice.BOND_BONDED:
                return ClassicBluetoothConstants.DEVICE_BOND_BONDED;
            case BluetoothDevice.BOND_BONDING:
                return ClassicBluetoothConstants.DEVICE_BOND_BONDING;
            default:
                Tool.warnOut(TAG, "设备未绑定，开始绑定");
                break;
        }

        //注册绑定BLE的广播接收者
        context.registerReceiver(boundStatusBroadcastReceiver, makeBoundStatusIntentFilter());

        //发起绑定
        if (remoteDevice.createBond()) {
            return ClassicBluetoothConstants.DEVICE_BOND_START_SUCCESS;
        } else {
            return ClassicBluetoothConstants.DEVICE_BOND_START_FAILED;
        }
    }

    /**
     * 设置绑定状态改变时的回调
     *
     * @param onDeviceBondStateChangedListener 绑定状态改变时的回调
     */
    public void setOnBondStateChangedListener(ClassicBluetoothInterface.OnDeviceBondStateChangedListener onDeviceBondStateChangedListener) {
        boundStatusBroadcastReceiver.setOnDeviceBondStateChangedListener(onDeviceBondStateChangedListener);
    }

    /**
     * 解除之前前发起绑定的设备之间的配对
     *
     * @return true代表成功
     */
    public boolean unbound() {
        return unbound(context, bondAddress);
    }

    /**
     * 关闭绑定器
     *
     * @return true表示关闭成功
     */
    public boolean close() {

        if (closed) {
            return false;
        }

        try {
            bondAddress = null;
            context.unregisterReceiver(boundStatusBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkCloseStatus();
        context = null;
        return true;
    }

    /**
     * 设置绑定器被关闭完成时的回调
     *
     * @param onBounderCloseCompleteListener 绑定器被关闭完成时的回调
     */
    public void setOnBounderCloseCompleteListener(ClassicBluetoothInterface.OnBounderCloseCompleteListener onBounderCloseCompleteListener) {
        this.onBounderCloseCompleteListener = onBounderCloseCompleteListener;
    }
}
