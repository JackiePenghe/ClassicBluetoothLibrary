package cn.almsound.www.classicblutoothlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Socket服务端实例
 *
 * @author jackie
 */
public class ClassicBluetoothSocketServer {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = ClassicBluetoothSocketServer.class.getSimpleName();

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
     * Socket的名字
     */
    private String name;
    /**
     * socket要监听的UUID
     */
    private UUID uuid;
    /**
     * Socket 服务端
     */
    private BluetoothServerSocket bluetoothServerSocket;
    /**
     * 初始化状态
     */
    private boolean initStatus;
    /**
     * Handler
     */
    private ServerSocketHandler serverSocketHandler = new ServerSocketHandler(ClassicBluetoothSocketServer.this);
    /**
     * 记录Socket 服务端是否已经开启
     */
    private boolean started;
    /**
     * socket服务端被连接后，返回的Socket
     */
    private BluetoothSocket acceptSocket;
    /**
     * 当有一个设备连接上了的时候，触发的回调
     */
    private ClassicBluetoothInterface.OnSocketDeviceConnectedListener onSocketDeviceConnectedListener;

    /**
     * 接收到数据时的回调
     */
    private ClassicBluetoothInterface.OnSocketServerReceiveDataListener onSocketServerReceiveDataListener;
    /**
     * 当服务端线程创建并开始运行时，回调此接口
     */
    private ClassicBluetoothInterface.OnSocketServerCreateSuccessListener onSocketServerCreateSuccessListener;
    /**
     * 保持服务继续运行的标志
     */
    private boolean serverSocketRun;
    private OutputStream outputStream;
    private InputStream inputStream;

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    ClassicBluetoothSocketServer(Context context) {
        this(context, ClassicBluetoothConstants.DEFAULT_SOCKET_SERVER_NAME, ClassicBluetoothConstants.DEFAULT_SOCKET_UUID);
    }

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param name    Socket名称
     */
    ClassicBluetoothSocketServer(Context context, String name) {
        this(context, name, ClassicBluetoothConstants.DEFAULT_SOCKET_UUID);
    }

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param uuid    UUID
     */
    ClassicBluetoothSocketServer(Context context, UUID uuid) {
        this(context, ClassicBluetoothConstants.DEFAULT_SOCKET_SERVER_NAME, uuid);
    }

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param name    Socket名称
     * @param uuid    UUID
     */
    ClassicBluetoothSocketServer(Context context, String name, UUID uuid) {
        this.context = context;
        this.name = name;
        this.uuid = uuid;
        bluetoothAdapter = ClassicBluetoothManager.getBluetoothAdapterInstance();
    }

    /*---------------------------公开方法---------------------------*/

    /**
     * 初始化
     *
     * @return true表示初始化成功
     */
    public boolean init() {
        return init(false);
    }

    /**
     * 初始化
     *
     * @param discoverable 是否可被其他设备发现
     * @return true表示初始化成功
     */
    public boolean init(boolean discoverable) {
        if (bluetoothAdapter == null) {
            return false;
        }

        if (initStatus) {
            return false;
        }

        //设置手机可被其他设备发现
        if (discoverable) {
            ClassicBluetoothManager.setBluetoothDiscoverable(context, 0);
        }

        try {
            bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
            initStatus = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 开始运行Socket服务端
     *
     * @return 状态
     */
    public int start() {

        if (!initStatus) {
            return ClassicBluetoothConstants.SOCKET_SERVER_UNINITIALIZED;
        }

        if (started) {
            return ClassicBluetoothConstants.SOCKET_SERVER_STARTED;
        }
        started = true;
        return ClassicBluetoothConstants.SOCKET_SERVER_START_SUCCESS;
    }

    /**
     * 停止
     */
    public boolean stop() {
        if (!initStatus) {
            return false;
        }
        if (bluetoothServerSocket == null) {
            return false;
        }

        if (context == null) {
            return false;
        }

        if (!serverSocketRun) {
            return false;
        }

        if (acceptSocket != null) {
            try {
                acceptSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            bluetoothServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        serverSocketRun = false;
        initStatus = false;
        context = null;
        acceptSocket = null;
        bluetoothServerSocket = null;
        return true;
    }

    /**
     * 发送数据到已连接的Socket设备
     *
     * @param data 数据
     * @return true表示发起请求成功
     */
    public boolean sendData(@NonNull String data) {
        if (acceptSocket == null) {
            return false;
        }

        try {
            if (outputStream == null) {
                outputStream = acceptSocket.getOutputStream();
            }
            outputStream.write(data.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置当有设备连接时的回调
     *
     * @param onSocketDeviceConnectedListener 有设备连接时的回调
     */
    public void setOnSocketDeviceConnectedListener(ClassicBluetoothInterface.OnSocketDeviceConnectedListener onSocketDeviceConnectedListener) {
        this.onSocketDeviceConnectedListener = onSocketDeviceConnectedListener;
    }

    /**
     * 设置当收到数据时的回调
     *
     * @param onSocketServerReceiveDataListener 当收到数据时的回调
     */
    public void setOnSocketServerReceiveDataListener(ClassicBluetoothInterface.OnSocketServerReceiveDataListener onSocketServerReceiveDataListener) {
        this.onSocketServerReceiveDataListener = onSocketServerReceiveDataListener;
    }

    public void setOnSocketServerCreateSuccessListener(ClassicBluetoothInterface.OnSocketServerCreateSuccessListener onSocketServerCreateSuccessListener) {
        this.onSocketServerCreateSuccessListener = onSocketServerCreateSuccessListener;
    }

    /*---------------------------静态内部类---------------------------*/

    private static class ServerSocketHandler extends Handler {


        /*---------------------------静态常量---------------------------*/

        public static final int WHAT_DEVICE_CONNECT = 1;
        public static final int WHAT_DEVICE_RECEIVE_DATA = 2;

        public static final String KEY_DEVICE_RECEIVE_DATA = "key_device_receive_data";

        /*---------------------------成员变量---------------------------*/

        private ClassicBluetoothSocketServer classicBluetoothSocketServer;

        /*---------------------------构造方法---------------------------*/

        /**
         * Default constructor associates this handler with the {@link Looper} for the
         * current thread.
         * <p>
         * If this thread does not have a looper, this handler won't be able to receive messages
         * so an exception is thrown.
         */
        ServerSocketHandler(ClassicBluetoothSocketServer classicBluetoothSocketServer) {
            this.classicBluetoothSocketServer = classicBluetoothSocketServer;
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
                    classicBluetoothSocketServer.onSocketDeviceConnectedListener.onDeviceConnected(classicBluetoothSocketServer.acceptSocket);
                    break;
                case WHAT_DEVICE_RECEIVE_DATA:
                    Bundle data = msg.getData();
                    String text = data.getString(ServerSocketHandler.KEY_DEVICE_RECEIVE_DATA);
                    classicBluetoothSocketServer.onSocketServerReceiveDataListener.onSocketServerReceiveData(classicBluetoothSocketServer.acceptSocket.getRemoteDevice().getName(), classicBluetoothSocketServer.acceptSocket.getRemoteDevice().getAddress(), text);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
