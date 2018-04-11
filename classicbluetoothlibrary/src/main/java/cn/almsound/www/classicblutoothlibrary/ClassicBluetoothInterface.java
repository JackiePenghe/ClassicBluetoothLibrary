package cn.almsound.www.classicblutoothlibrary;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

/**
 * 定义接口的专用类
 *
 * @author jackie
 */
public class ClassicBluetoothInterface {

    /*-------------------------扫描部分-------------------------*/

    /**
     * 发现一个蓝牙设备时，进行的回调
     */
    public interface OnScanFindOneDeviceListener {
        /**
         * 发现一个蓝牙设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onScanFindADevice(BluetoothDevice bluetoothDevice);
    }

    /**
     * 发现一个新设备时，进行的回调
     */
    public interface OnScanFindOneNewDeviceListener {
        /**
         * 发现一个新蓝牙设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onScanFindOneNewDevice(BluetoothDevice bluetoothDevice);
    }

    /**
     * 发现一个已绑定的设备时，进行的回调
     */
    public interface OnScanFindOneBoundedDeviceListener {
        /**
         * 发现一个已绑定的蓝牙设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onScanFindOneBoundedDevice(BluetoothDevice bluetoothDevice);
    }

    /**
     * 发现一个新的已绑定的设备时，进行的回调
     */
    public interface OnScanFindOneNewBoundedDeviceListener {
        /**
         * 发现一个新的已绑定的蓝牙设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onScanFindOneNewBoundedDevice(BluetoothDevice bluetoothDevice);
    }

    /**
     * 发现一个未被绑定的设备时，进行的回调
     */
    public interface OnScanFindOneUnboundedDeviceListener {
        /**
         * 发现一个未被绑定的设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onScanFindOneUnboundedDevice(BluetoothDevice bluetoothDevice);
    }

    /**
     * 发现一个新的未被绑定的设备时，进行的回调
     */
    public interface OnScanFindOneNewUnboundedDeviceListener {
        /**
         * 发现一个新的未被绑定的设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onScanFindOneNewUnboundedDevice(BluetoothDevice bluetoothDevice);
    }

    /**
     * 发现一个绑定中的设备时，进行的回调
     */
    public interface OnScanFindOneBoundingDeviceListener {
        /**
         * 发现一个绑定中的设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onScanFindOneBounding(BluetoothDevice bluetoothDevice);
    }

    /**
     * 发现一个新的绑定中的设备时，进行的回调
     */
    public interface OnScanFindOneNewBoundingDeviceListener {
        /**
         * 发现一个新的绑定中的设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onScanFindOneNewBounding(BluetoothDevice bluetoothDevice);
    }

    /**
     * 当扫描状态改变时，进行的回调
     */
    public interface OnScanStatusChangedListener {
        /**
         * 开始扫描
         */
        void onScanStarted();

        /**
         * 扫描完成
         */
        void onScanFinished();
    }

    /*-------------------------绑定部分-------------------------*/

    /**
     * BLE蓝牙设备绑定状态改变的回调
     */
    public interface OnDeviceBondStateChangedListener {
        /**
         * 设备正在绑定
         */
        void onDeviceBinding();

        /**
         * 设备已经绑定过了
         */
        void onDeviceBonded();

        /**
         * 取消绑定或者绑定失败
         */
        void onDeviceBindNone();
    }

    /**
     * ble连接工具关闭完成的回调
     */
    public interface OnBounderCloseCompleteListener {
        /**
         * ble连接工具关闭完成的时候回调此函数
         */
        void onBounderCloseComplete();
    }

    /*-------------------------Socket服务端-------------------------*/

    /**
     * 有设备连接时回调此函数
     */
    public interface OnSocketDeviceConnectedListener {

        /**
         * 有设备连接了
         *
         * @param bluetoothSocket Socket实例
         */
        void onDeviceConnected(BluetoothSocket bluetoothSocket);
    }

    /**
     * Socket服务端创建成功的接口
     */
    public interface OnSocketServerCreateSuccessListener{
        /**
         * Socket服务端创建成功
         * @param bluetoothServerSocket
         */
        void  onSocketServerCreateSuccess(BluetoothServerSocket bluetoothServerSocket);
    }

    /**
     * 当socket服务端收到数据时回调此接口
     */
    public interface OnSocketServerReceiveDataListener {

        /**
         * socket服务端收到数据了
         *
         * @param deviceName    数据来源设备名
         * @param deviceAddress 数据来源设备地址
         * @param data          数据
         */
        void onSocketServerReceiveData(String deviceName, String deviceAddress, String data);
    }

    /*-------------------------Socket客户端-------------------------*/

    /**
     * 当Socket客户端连接到服务端成功时回调此接口
     */
    public interface OnConnectSocketServerSuccessListener {

        /**
         * 连接到服务端成功
         *
         * @param bluetoothSocket Socket实例
         */
        void onConnectSocketServerSuccess(BluetoothSocket bluetoothSocket);
    }

    /**
     * 当socket客户端端收到数据时回调此接口
     */
    public interface OnReceiveSocketServerDataListener {

        /**
         * socket客户端端收到数据了
         *
         * @param deviceName    数据来源设备名称
         * @param deviceAddress 数据来源设备地址
         * @param data          数据
         */
        void onReceiveSocketServerData(String deviceName, String deviceAddress, String data);
    }

    /*-------------------------A2DP-------------------------*/

    /**
     * API默认的与A2DP服务的连接状态改变时回调此接口
     */
    interface DefaultOnBluetoothA2dpConnectStateChangedListener {
        /**
         * A2DP服务已连接
         *
         * @param bluetoothA2dp BluetoothA2dp
         */
        void onBluetoothA2dpConnected(BluetoothA2dp bluetoothA2dp);

        /**
         * A2DP服务已断开连接
         */
        void onBluetoothA2dpDisconnected();
    }

    /**
     * 与A2DP服务的连接状态改变时回调此接口
     */
    public interface OnBluetoothA2dpServiceConnectStateChangedListener {
        /**
         * A2DP服务已连接
         */
        void onBluetoothA2dpServiceConnected();

        /**
         * A2DP服务已断开连接
         */
        void onBluetoothA2dpServiceDisconnected();
    }

    /**
     * 当A2DP与设备的连接状态改变时回调此接口
     */
    public interface OnBluetoothA2dpDeviceConnectStateChangedListener {
        /**
         * 正在连接设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onBluetoothA2dpDeviceConnecting(BluetoothDevice bluetoothDevice);

        /**
         * 已连接设备
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onBluetoothA2dpDeviceConnected(BluetoothDevice bluetoothDevice);

        /**
         * 正在断开连接
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onBluetoothA2dpDeviceDisconnecting(BluetoothDevice bluetoothDevice);

        /**
         * 连接已断开
         *
         * @param bluetoothDevice 蓝牙设备
         */
        void onBluetoothA2dpDeviceDisconnected(BluetoothDevice bluetoothDevice);
    }
}
