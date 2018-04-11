package cn.almsound.www.classicblutoothlibrary;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import java.util.UUID;

/**
 * 蓝牙管理器
 *
 * @author jackie
 */
public class ClassicBluetoothManager {

    /*---------------------------静态成员变量---------------------------*/

    /**
     * 蓝牙适配器
     */
    private static BluetoothAdapter bluetoothAdapter;

    /**
     * 经典蓝牙扫描器
     */
    private static ClassicBluetoothScanner classicBluetoothScanner;

    /**
     * 蓝牙连接器
     */
    private static ClassicBluetoothBounder classicBluetoothBounder;

    /**
     * 蓝牙Socket服务端
     */
    private static ClassicBluetoothSocketServer classicBluetoothSocketServer;

    /**
     * 蓝牙Socket客户端
     */
    private static ClassicBluetoothSocketClient classicBluetoothSocketClient;

    /**
     * A2DP客户端
     */
    private static BluetoothA2dpClient bluetoothA2DpClient;

    /*---------------------------公开静态方法---------------------------*/

    /**
     * 获取蓝牙适配器单例
     *
     * @return 蓝牙适配器单例
     */
    static BluetoothAdapter getBluetoothAdapterInstance() {
        if (bluetoothAdapter == null) {
            synchronized (ClassicBluetoothManager.class) {
                if (bluetoothAdapter == null) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
            }
        }
        return bluetoothAdapter;
    }

    /**
     * 判断设备是否支持蓝牙
     *
     * @return true表示支持
     */
    public static boolean isSupportBluetooth() {
        return getBluetoothAdapterInstance() != null;
    }

    /**
     * 释放全部资源
     */
    public static void releaseAll() {
        releaseClassicBluetoothScanner();
        releaseClassicBluetoothConnector();
        releaseBluetoothAdapter();
        releaseClassicBluetoothSocketServer();
        releaseClassicBluetoothSocketClient();
        releaseBluetoothA2DPClient();
    }

    /**
     * 释放蓝牙适配器内存
     */
    public static void releaseBluetoothAdapter() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter = null;
        }
    }

    /**
     * 打开蓝牙开关
     */
    public static void openBlueTooth(Context context) {
        if (!isSupportBluetooth()) {
            return;
        }
        if (context instanceof Activity) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(intent);
        } else {
            if (getBluetoothAdapterInstance() == null) {
                return;
            }
            getBluetoothAdapterInstance().enable();
        }
    }

    /**
     * 设置手机可被其他设备扫描
     *
     * @param context          上下文
     * @param discoverableTime 可被扫描的持续时间（1~3600秒），设为0 则会一直持续可被扫描状态，范围外的其他任意值都会默认成120秒
     */
    public static void setBluetoothDiscoverable(Context context, int discoverableTime) {
        if (!isSupportBluetooth()) {
            return;
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoverableTime);
        context.startActivity(discoverableIntent);
    }

    /**
     * 显示打印日志
     */
    public static void enableDebug() {
        Tool.setDebugFlag(true);
    }

    /**
     * 获取当前蓝牙的开关状态
     *
     * @return 蓝牙的开关状态, true表示开启，false表示关闭
     */
    public static boolean isBluetoothOpened() {
        if (!isSupportBluetooth()) {
            throw new NullPointerException("Your device not support bluetooth!");
        }
        return getBluetoothAdapterInstance().isEnabled();
    }

    /*---------------------------扫描相关---------------------------*/

    /**
     * 创建一个新的蓝牙扫描器实例
     *
     * @param context 上下文
     * @return 蓝牙扫描器实例
     */
    public static ClassicBluetoothScanner newBluetoothScanner(Context context) {
        if (!isSupportBluetooth()) {
            return null;
        }
        return new ClassicBluetoothScanner(context);
    }

    /**
     * 创建一个蓝牙扫描器实例单例
     *
     * @param context 上下文
     * @return 蓝牙扫描器实例
     */
    public static ClassicBluetoothScanner getBluetoothScannerInstance(Context context) {
        if (!isSupportBluetooth()) {
            return null;
        }
        if (classicBluetoothScanner == null) {
            synchronized (ClassicBluetoothManager.class) {
                if (classicBluetoothScanner == null) {
                    classicBluetoothScanner = new ClassicBluetoothScanner(context);
                }
            }
        }
        return classicBluetoothScanner;
    }

    /**
     * 释放经典蓝牙扫描器实例的资源
     */
    public static void releaseClassicBluetoothScanner() {
        if (classicBluetoothScanner != null) {
            classicBluetoothScanner.close();
            classicBluetoothScanner = null;
        }
    }

    /*---------------------------绑定相关---------------------------*/

    /**
     * 获取一个单例的绑定器实例
     *
     * @param context 上下文
     * @return 绑定器实例
     */
    public static ClassicBluetoothBounder getClassicBluetoothBounderInstance(Context context) {
        if (!isSupportBluetooth()) {
            return null;
        }

        if (classicBluetoothBounder == null) {
            synchronized (ClassicBluetoothManager.class) {
                if (classicBluetoothBounder == null) {
                    classicBluetoothBounder = new ClassicBluetoothBounder(context);
                }
            }
        }

        return classicBluetoothBounder;
    }

    /**
     * 释放绑定器资源
     */
    public static void releaseClassicBluetoothConnector() {
        if (classicBluetoothBounder != null) {
            classicBluetoothBounder.close();
            classicBluetoothBounder = null;
        }
    }

    /*---------------------------Socket服务端相关---------------------------*/

    /**
     * 获取Socket服务端实例
     *
     * @param context 上下文
     * @param name    Socket名称
     * @param uuid    UUID
     * @return Socket服务端实例
     */
    public static ClassicBluetoothSocketServer getClassicBluetoothSocketServerInstance(Context context, String name, UUID uuid) {
        if (!isSupportBluetooth()) {
            return null;
        }
        if (classicBluetoothSocketServer == null) {
            synchronized (ClassicBluetoothManager.class) {
                if (classicBluetoothSocketServer == null) {
                    classicBluetoothSocketServer = new ClassicBluetoothSocketServer(context, name, uuid);
                }
            }
        }
        return classicBluetoothSocketServer;
    }

    /**
     * 获取Socket服务端实例
     *
     * @param context 上下文
     * @param name    Socket名称
     * @return Socket服务端实例
     */
    public static ClassicBluetoothSocketServer getClassicBluetoothSocketServerInstance(Context context, String name) {
        return getClassicBluetoothSocketServerInstance(context, name, UUID.fromString(ClassicBluetoothConstants.SPP_UUID));
    }

    /**
     * 获取Socket服务端实例
     *
     * @param context 上下文
     * @param uuid    UUID
     * @return Socket服务端实例
     */
    public static ClassicBluetoothSocketServer getClassicBluetoothSocketServerInstance(Context context, UUID uuid) {
        return getClassicBluetoothSocketServerInstance(context, ClassicBluetoothConstants.DEFAULT_SOCKET_SERVER_NAME, uuid);
    }

    /**
     * 获取Socket服务端实例
     *
     * @param context 上下文
     * @return Socket服务端实例
     */
    public static ClassicBluetoothSocketServer getClassicBluetoothSocketServerInstance(Context context) {
        return getClassicBluetoothSocketServerInstance(context, ClassicBluetoothConstants.DEFAULT_SOCKET_SERVER_NAME, ClassicBluetoothConstants.DEFAULT_SOCKET_UUID);
    }

    /**
     * 释放Socket服务端的资源
     */
    public static void releaseClassicBluetoothSocketServer() {
        if (classicBluetoothSocketServer != null) {
            classicBluetoothSocketServer.stop();
            classicBluetoothSocketServer = null;
        }
    }

    /*---------------------------Socket客户端相关---------------------------*/

    /**
     * 获取Socket客户端单例
     *
     * @param context 上下文
     * @return Socket客户端
     */
    public static ClassicBluetoothSocketClient getClassicBluetoothSocketClientInstance(Context context) {

        if (!isSupportBluetooth()) {
            return null;
        }

        if (classicBluetoothSocketClient == null) {
            synchronized (ClassicBluetoothManager.class) {
                if (classicBluetoothSocketClient == null) {
                    classicBluetoothSocketClient = new ClassicBluetoothSocketClient(context);
                }
            }
        }

        return classicBluetoothSocketClient;
    }

    public static void releaseClassicBluetoothSocketClient() {
        if (classicBluetoothSocketClient != null) {
            classicBluetoothSocketClient.close();
            classicBluetoothSocketClient = null;
        }
    }

    /*---------------------------A2DP相关---------------------------*/

    /**
     * 获取A2DP客户端实例
     * @param context 上下文
     * @return A2DP客户端实例
     */
    public static BluetoothA2dpClient getBluetoothA2DPClientInstance(Context context) {
        if (!isSupportBluetooth()) {
            return null;
        }
        if (bluetoothA2DpClient == null) {
            synchronized (ClassicBluetoothManager.class) {
                if (bluetoothA2DpClient == null) {
                    bluetoothA2DpClient = new BluetoothA2dpClient(context);
                }
            }
        }
        return bluetoothA2DpClient;
    }

    public static void releaseBluetoothA2DPClient(){
        if (bluetoothA2DpClient != null){
            bluetoothA2DpClient.close();
            bluetoothA2DpClient = null;
        }
    }
}
