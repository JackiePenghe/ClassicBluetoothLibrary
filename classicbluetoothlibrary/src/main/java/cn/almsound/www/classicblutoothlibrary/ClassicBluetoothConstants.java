package cn.almsound.www.classicblutoothlibrary;

import java.util.UUID;

/**
 * BLE常量
 *
 * @author alm
 */

public class ClassicBluetoothConstants {

    /*开放常量定义区*/

    /**
     * 设备绑定请求发起成功
     */
    public static final int DEVICE_BOND_START_SUCCESS = 0;
    /**
     * 设备绑定请求发起失败成功
     */
    public static final int DEVICE_BOND_START_FAILED = 1;
    /**
     * 没有蓝牙管理器
     */
    public static final int BLUETOOTH_MANAGER_NULL = 2;
    /**
     * 没有蓝牙适配器
     */
    public static final int BLUETOOTH_ADAPTER_NULL = 3;
    /**
     * 设备已绑定
     */
    public static final int DEVICE_BOND_BONDED = 4;
    /**
     * 设备绑定中
     */
    public static final int DEVICE_BOND_BONDING = 5;
    /**
     * 设备地址无效
     */
    public static final int BLUETOOTH_ADDRESS_INCORRECT = 6;

    /**
     * socket服务已经开启了
     */
    public static final int SOCKET_SERVER_STARTED = 7;

    /**
     * socket服务开启成功
     */
    public static final int SOCKET_SERVER_START_SUCCESS = 8;

    /**
     *  socket 服务端未初始化
     */
    public static final int SOCKET_SERVER_UNINITIALIZED = 9;

    /**
     * Socket服务线程已经在运行了
     */
    public static final int SOCKET_SERVER_THREAD_RUNNING = 10;
    /*库内ACTION定义区域*/

    /**
     * Socket通讯最经典的UUID
     */
    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * 默认的Socket服务端名称
     */
    static final String DEFAULT_SOCKET_SERVER_NAME = "default_socket_server_name";

    /**
     * 默认的Socket UUID
     */
    static final UUID DEFAULT_SOCKET_UUID = UUID.fromString(SPP_UUID);
}
