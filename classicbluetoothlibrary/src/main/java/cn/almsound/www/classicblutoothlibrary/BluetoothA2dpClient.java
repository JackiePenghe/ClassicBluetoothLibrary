package cn.almsound.www.classicblutoothlibrary;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A2dp客户端
 *
 * @author jackie
 */
public class BluetoothA2dpClient {

    /*------------------------成员变量----------------------------*/

    /**
     * 上下文
     */
    private Context context;
    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter bluetoothAdapter;
    /**
     * BluetoothA2dp实例
     */
    private BluetoothA2dp bluetoothA2dp;
    /**
     * 蓝牙设备
     */
    private BluetoothDevice bluetoothDevice;
    /**
     * Handler
     */
    private Handler handler = new Handler();
    /**
     * 监听A2DP相关的广播接收者
     */
    private BluetoothA2dpBroadCastReceiver bluetoothA2dpBroadCastReceiver = new BluetoothA2dpBroadCastReceiver();
    /**
     * ProfileService监听
     */
    private ClassicBluetoothInterface.OnBluetoothA2dpServiceConnectStateChangedListener onBluetoothA2DpServiceConnectStateChangedListener;
    /**
     * 默认的ProfileService监听
     */
    private DefaultProfileServiceListener defaultProfileServiceListener = new DefaultProfileServiceListener();
    /**
     * 与A2DP服务连接状态被改变时进行的回调
     */
    private ClassicBluetoothInterface.DefaultOnBluetoothA2dpConnectStateChangedListener defaultOnBluetoothA2DpConnectStateChangedListener = new ClassicBluetoothInterface.DefaultOnBluetoothA2dpConnectStateChangedListener() {
        @Override
        public void onBluetoothA2dpConnected(BluetoothA2dp bluetoothA2dp) {
            BluetoothA2dpClient.this.bluetoothA2dp = bluetoothA2dp;
            if (onBluetoothA2DpServiceConnectStateChangedListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onBluetoothA2DpServiceConnectStateChangedListener.onBluetoothA2dpServiceConnected();
                    }
                });
            }
        }

        @Override
        public void onBluetoothA2dpDisconnected() {
            BluetoothA2dpClient.this.bluetoothA2dp = null;
            if (onBluetoothA2DpServiceConnectStateChangedListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onBluetoothA2DpServiceConnectStateChangedListener.onBluetoothA2dpServiceDisconnected();
                    }
                });
            }
        }
    };


    /*------------------------构造方法----------------------------*/

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    BluetoothA2dpClient(@NonNull Context context) {
        this.context = context;
        bluetoothAdapter = ClassicBluetoothManager.getBluetoothAdapterInstance();
    }

    /*------------------------公开方法----------------------------*/

    /**
     * 初始化
     *
     * @return true表示初始化成功
     */
    public boolean init(ClassicBluetoothInterface.OnBluetoothA2dpServiceConnectStateChangedListener onBluetoothA2DpServiceConnectStateChangedListener) {
        this.onBluetoothA2DpServiceConnectStateChangedListener = onBluetoothA2DpServiceConnectStateChangedListener;
        defaultProfileServiceListener.setDefaultOnBluetoothA2DpConnectStateChangedListener(defaultOnBluetoothA2DpConnectStateChangedListener);
        context.registerReceiver(bluetoothA2dpBroadCastReceiver, makeIntentFilter());
        return bluetoothAdapter.getProfileProxy(context, defaultProfileServiceListener, BluetoothProfile.A2DP);
    }

    /**
     * 连接设备
     *
     * @param address 设备地址
     * @return true表示成功发起请求
     */
    @Deprecated
    public boolean startConnect(String address) {
        if (bluetoothAdapter == null) {
            return false;
        }
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        return startConnect(remoteDevice);
    }

    /**
     * 连接设备
     *
     * @param bluetoothDevice 设备
     * @return true表示成功发起请求
     */
    public boolean startConnect(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        return startConnect();
    }

    /**
     * 绑定并连接设备
     * @return true表示成功发起请求
     */
    @SuppressWarnings("WeakerAccess")
    private boolean startConnect() {

        if (bluetoothDevice == null) {
            return false;
        }

        if (bluetoothA2dp == null) {
            return false;
        }

        try {
            //noinspection JavaReflectionMemberAccess
            @SuppressLint("PrivateApi") Method connect = bluetoothA2dp.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
            connect.setAccessible(true);
            return (boolean) connect.invoke(bluetoothA2dp, bluetoothDevice);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 设置A2DP与设备的连接状态改变时的回调
     *
     * @param onBluetoothA2dpDeviceConnectStateChangedListener A2DP与设备的连接状态改变时的回调
     */
    public void setOnBluetoothA2dpDeviceConnectStateChangedListener(ClassicBluetoothInterface.OnBluetoothA2dpDeviceConnectStateChangedListener onBluetoothA2dpDeviceConnectStateChangedListener) {
        bluetoothA2dpBroadCastReceiver.setOnBluetoothA2dpDeviceConnectStateChangedListener(onBluetoothA2dpDeviceConnectStateChangedListener);
    }

    /**
     * 断开连接
     *
     * @return true表示请求成
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public boolean disconnect() {

        if (bluetoothAdapter == null) {
            return false;
        }

        if (bluetoothA2dp == null) {
            return false;
        }
        if (bluetoothDevice == null) {
            return false;
        }

        try {
            //noinspection JavaReflectionMemberAccess
            @SuppressLint("PrivateApi") Method disconnect = bluetoothA2dp.getClass().getDeclaredMethod("disconnect", BluetoothDevice.class);
            disconnect.setAccessible(true);
            return (boolean) disconnect.invoke(bluetoothA2dp, bluetoothDevice);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭
     *
     * @return true表示关闭成功
     */
    public boolean close() {
        if (bluetoothAdapter == null) {
            return false;
        }
        if (bluetoothA2dp == null) {
            return false;
        }
        if (bluetoothDevice == null) {
            return false;
        }
        disconnect();
        context.unregisterReceiver(bluetoothA2dpBroadCastReceiver);
        bluetoothA2dpBroadCastReceiver.close();
        bluetoothA2dpBroadCastReceiver = null;
        bluetoothDevice = null;
        context = null;
        handler = null;
        defaultProfileServiceListener.close();
        defaultProfileServiceListener = null;
        onBluetoothA2DpServiceConnectStateChangedListener = null;
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp);
        bluetoothAdapter = null;
        bluetoothA2dp = null;
        ClassicBluetoothManager.releaseBluetoothA2DPClient();
        return true;
    }


    /*------------------------私有方法----------------------------*/

    private IntentFilter makeIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        return filter;
    }
}
