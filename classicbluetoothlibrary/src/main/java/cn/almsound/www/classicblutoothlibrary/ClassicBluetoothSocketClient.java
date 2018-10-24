package cn.almsound.www.classicblutoothlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Socket客户端实例
 *
 * @author jackie
 */
public class ClassicBluetoothSocketClient {

    /*---------------------------成员变量---------------------------*/

    private static final String TAG = ClassicBluetoothSocketClient.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 上下文
     */
    private Context context;

    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * 要连接的设备的地址
     */
    private String address;

    /**
     * 记录初始化的状态
     */
    private boolean initStatus;

    /**
     * 连接后的Socket实例
     */
    private BluetoothSocket rfcommSocketToServiceRecord;

    /**
     * Handler
     */
    private SocketHandler socketHandler = new SocketHandler(ClassicBluetoothSocketClient.this);

    /**
     * 成功连接到服务端时回调此函数
     */
    private ClassicBluetoothInterface.OnConnectSocketServerSuccessListener onConnectSocketServerSuccessListener;

    /**
     * 当收到服务端的数据时回调此函数
     */
    private ClassicBluetoothInterface.OnReceiveSocketServerDataListener onReceiveSocketServerDataListener;
    private InputStream inputStream;
    private OutputStream outputStream;

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    ClassicBluetoothSocketClient(Context context) {
        this.context = context;
        bluetoothAdapter = ClassicBluetoothManager.getBluetoothAdapterInstance();
    }

    /*---------------------------公开方法---------------------------*/

    /**
     * 初始化
     *
     * @param address 要连接的设备的地址
     * @return true表示初始化成功
     */
    public boolean init(String address) {
        if (bluetoothAdapter == null) {
            return false;
        }
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        this.address = address;
        initStatus = true;
        return true;
    }

    /**
     * 发起连接
     *
     * @return 开始连接
     */
    public boolean startConnect() {
        return startConnect(ClassicBluetoothConstants.DEFAULT_SOCKET_UUID);
    }

    /**
     * 发起连接
     *
     * @param uuid uuid
     * @return true表示成功发起连接
     */
    @SuppressWarnings("WeakerAccess")
    public boolean startConnect(UUID uuid) {
        if (!initStatus) {
            return false;
        }
        if (address == null) {
            return false;
        }

        ConnectThread connectThread = new ConnectThread(ClassicBluetoothSocketClient.this, uuid);
        connectThread.start();
        return true;
    }

    /**
     * 关闭
     * @return true表示关闭成功
     */
    public boolean close() {
        if (!initStatus) {
            return false;
        }
        if (address == null) {
            return false;
        }
        if (context == null) {
            return false;
        }

        if (rfcommSocketToServiceRecord != null) {
            try {
                rfcommSocketToServiceRecord.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        initStatus = false;
        address = null;
        context = null;
        rfcommSocketToServiceRecord = null;
        return true;
    }

    /**
     * 发送数据到已连接的Socket设备
     *
     * @param data 数据
     * @return true表示发起请求成功
     */
    public boolean sendData(@NonNull String data) {
        if (rfcommSocketToServiceRecord == null) {
            return false;
        }

        try {
            if (outputStream == null) {
                outputStream = rfcommSocketToServiceRecord.getOutputStream();
            }
            outputStream.write(data.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置连接成功的回调
     *
     * @param onConnectSocketServerSuccessListener 成功的回调
     */
    public void setOnConnectSocketServerSuccessListener(ClassicBluetoothInterface.OnConnectSocketServerSuccessListener onConnectSocketServerSuccessListener) {
        this.onConnectSocketServerSuccessListener = onConnectSocketServerSuccessListener;
    }

    /**
     * 设置收到数据的回调
     *
     * @param onReceiveSocketServerDataListener 收到数据的回调
     */
    public void setOnReceiveSocketServerDataListener(ClassicBluetoothInterface.OnReceiveSocketServerDataListener onReceiveSocketServerDataListener) {
        this.onReceiveSocketServerDataListener = onReceiveSocketServerDataListener;
    }

    /*---------------------------静态内部类方法---------------------------*/

    /**
     * handler
     */
    private static class SocketHandler extends Handler {

        /*---------------------------静态常量---------------------------*/

        static final int WHAT_DEVICE_CONNECT = 1;
        static final int WHAT_DEVICE_RECEIVE_DATA = 2;

        static final String KEY_DEVICE_RECEIVE_DATA = "key_device_receive_data";

        /*---------------------------成员变量---------------------------*/


        private ClassicBluetoothSocketClient classicBluetoothSocketClient;

        /*---------------------------构造方法---------------------------*/

        /**
         * Default constructor associates this handler with the {@link Looper} for the
         * current thread.
         * <p>
         * If this thread does not have a looper, this handler won't be able to receive messages
         * so an exception is thrown.
         */
        SocketHandler(ClassicBluetoothSocketClient classicBluetoothSocketClient) {
            this.classicBluetoothSocketClient = classicBluetoothSocketClient;
        }

        /*---------------------------重写父类方法---------------------------*/

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg Message
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DEVICE_CONNECT:
                    classicBluetoothSocketClient.onConnectSocketServerSuccessListener.onConnectSocketServerSuccess(classicBluetoothSocketClient.rfcommSocketToServiceRecord);
                    break;
                case WHAT_DEVICE_RECEIVE_DATA:
                    Bundle data = msg.getData();
                    String string = data.getString(KEY_DEVICE_RECEIVE_DATA);
                    classicBluetoothSocketClient.onReceiveSocketServerDataListener.onReceiveSocketServerData(classicBluetoothSocketClient.rfcommSocketToServiceRecord.getRemoteDevice().getName(), classicBluetoothSocketClient.rfcommSocketToServiceRecord.getRemoteDevice().getAddress(), string);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    /**
     * 用于连接的线程
     */
    private static class ConnectThread extends Thread {

        /*---------------------------成员变量---------------------------*/

        private static final String TAG = ConnectThread.class.getSimpleName();

        /*---------------------------成员变量---------------------------*/

        /**
         * 外部类的引用
         */
        private ClassicBluetoothSocketClient classicBluetoothSocketClient;

        /**
         * 蓝牙适配器
         */
        private BluetoothAdapter bluetoothAdapter;

        /**
         * 地址
         */
        private String address;

        /**
         * UUID
         */
        private UUID uuid;

        /*---------------------------构造方法---------------------------*/

        /**
         * 构造方法
         *
         * @param classicBluetoothSocketClient 外部类的引用
         * @param uuid                         UUID
         */
        ConnectThread(ClassicBluetoothSocketClient classicBluetoothSocketClient, UUID uuid) {
            this.classicBluetoothSocketClient = classicBluetoothSocketClient;
            this.uuid = uuid;
            bluetoothAdapter = classicBluetoothSocketClient.bluetoothAdapter;
            address = classicBluetoothSocketClient.address;
        }

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see #start()
         * @see #stop()
         * @see #Thread(ThreadGroup, Runnable, String)
         */
        @SuppressWarnings("JavadocReference")
        @Override
        public void run() {
            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket rfcommSocketToServiceRecord;
            try {
                Tool.warnOut(TAG, "开始准备连接");
                rfcommSocketToServiceRecord = remoteDevice.createRfcommSocketToServiceRecord(uuid);
                classicBluetoothSocketClient.rfcommSocketToServiceRecord = rfcommSocketToServiceRecord;
                rfcommSocketToServiceRecord.connect();
                Tool.warnOut(TAG, "连接成功");
                classicBluetoothSocketClient.socketHandler.sendEmptyMessage(SocketHandler.WHAT_DEVICE_CONNECT);
                InputStreamListenerThread inputStreamListenerThread = new InputStreamListenerThread(rfcommSocketToServiceRecord, ConnectThread.this);
                inputStreamListenerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
                Tool.warnOut(TAG, "连接失败");
            }
        }

        /*---------------------------静态内部类---------------------------*/

        /**
         * 监听输入流的线程
         */
        private static class InputStreamListenerThread extends Thread {

            /*---------------------------成员变量---------------------------*/

            /**
             * Socket实例
             */
            private BluetoothSocket bluetoothSocket;

            /**
             * 外部类实例
             */
            private ClassicBluetoothSocketClient.ConnectThread connectThread;

            /**
             * Allocates a new {@code Thread} object. This constructor has the same
             * effect as {@linkplain #Thread(ThreadGroup, Runnable, String) Thread}
             * {@code (null, null, gname)}, where {@code gname} is a newly generated
             * name. Automatically generated names are of the form
             * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
             */
            @SuppressWarnings("JavadocReference")
            InputStreamListenerThread(BluetoothSocket bluetoothSocket, ClassicBluetoothSocketClient.ConnectThread connectThread) {
                this.bluetoothSocket = bluetoothSocket;
                this.connectThread = connectThread;
            }

            /**
             * If this thread was constructed using a separate
             * <code>Runnable</code> run object, then that
             * <code>Runnable</code> object's <code>run</code> method is called;
             * otherwise, this method does nothing and returns.
             * <p>
             * Subclasses of <code>Thread</code> should override this method.
             *
             * @see #start()
             * @see #stop()
             * @see #Thread(ThreadGroup, Runnable, String)
             */
            @SuppressWarnings("JavadocReference")
            @Override
            public void run() {
                if (bluetoothSocket == null) {
                    return;
                }

                try {
                    connectThread.classicBluetoothSocketClient.inputStream = bluetoothSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (bluetoothSocket.isConnected()) {
                    InputStream inputStream = connectThread.classicBluetoothSocketClient.inputStream;

                    if (inputStream == null) {
                        Tool.warnOut(TAG, "输入流为空 返回");
                        continue;
                    }
                    try {

                        byte[] bytes = new byte[1024];
                        int length = inputStream.read(bytes);
                        if (length == -1) {
                            return;
                        }
                        byte[] cacheBytes = new byte[length];
                        System.arraycopy(bytes, 0, cacheBytes, 0, length);
                        final String text = new String(cacheBytes);
                        Tool.warnOut(TAG, "inputStream text = " + text);
                        Message message = new Message();
                        message.what = SocketHandler.WHAT_DEVICE_RECEIVE_DATA;
                        Bundle bundle = new Bundle();
                        bundle.putString(SocketHandler.KEY_DEVICE_RECEIVE_DATA, text);
                        message.setData(bundle);
                        connectThread.classicBluetoothSocketClient.socketHandler.sendMessage(message);
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
