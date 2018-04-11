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
    private SocketHandler socketHandler = new SocketHandler(this);

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
        ServerThread serverThread = new ServerThread(this);
        serverThread.start();
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

    private static class ServerThread extends Thread {

        private ClassicBluetoothSocketServer classicBluetoothSocketServer;

        /**
         * Allocates a new {@code Thread} object. This constructor has the same
         * effect as {@linkplain #Thread(ThreadGroup, Runnable, String) Thread}
         * {@code (null, null, gname)}, where {@code gname} is a newly generated
         * name. Automatically generated names are of the form
         * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
         */
        @SuppressWarnings("JavadocReference")
        ServerThread(ClassicBluetoothSocketServer classicBluetoothSocketServer) {
            this.classicBluetoothSocketServer = classicBluetoothSocketServer;
            classicBluetoothSocketServer.serverSocketRun = true;
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
            while (classicBluetoothSocketServer.serverSocketRun) {
                BluetoothSocket acceptSocket = null;
                try {
                    acceptSocket = classicBluetoothSocketServer.bluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (acceptSocket == null) {
                    return;
                }
                try {
                    classicBluetoothSocketServer.outputStream = acceptSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                InputStreamListenerThread inputStreamListenerThread = new InputStreamListenerThread(classicBluetoothSocketServer, acceptSocket);
                inputStreamListenerThread.start();
                if (classicBluetoothSocketServer.onSocketServerCreateSuccessListener != null) {
                    classicBluetoothSocketServer.onSocketServerCreateSuccessListener.onSocketServerCreateSuccess(classicBluetoothSocketServer.bluetoothServerSocket);
                }
            }
        }
    }

    /**
     * 监听输入流的线程
     */
    private static class InputStreamListenerThread extends Thread {

        /*---------------------------成员变量---------------------------*/

        private ClassicBluetoothSocketServer classicBluetoothSocketServer;
        /**
         * Socket实例
         */
        private BluetoothSocket bluetoothSocket;

        /**
         * Allocates a new {@code Thread} object. This constructor has the same
         * effect as {@linkplain #Thread(ThreadGroup, Runnable, String) Thread}
         * {@code (null, null, gname)}, where {@code gname} is a newly generated
         * name. Automatically generated names are of the form
         * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
         */
        @SuppressWarnings("JavadocReference")
        InputStreamListenerThread(ClassicBluetoothSocketServer classicBluetoothSocketServer, BluetoothSocket bluetoothSocket) {
            this.classicBluetoothSocketServer = classicBluetoothSocketServer;
            this.bluetoothSocket = bluetoothSocket;
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
                classicBluetoothSocketServer.inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (bluetoothSocket.isConnected()) {
                InputStream inputStream = classicBluetoothSocketServer.inputStream;

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
                    message.what = ClassicBluetoothSocketServer.SocketHandler.WHAT_DEVICE_RECEIVE_DATA;
                    Bundle bundle = new Bundle();
                    bundle.putString(ClassicBluetoothSocketServer.SocketHandler.KEY_DEVICE_RECEIVE_DATA, text);
                    message.setData(bundle);
                    classicBluetoothSocketServer.socketHandler.sendMessage(message);
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * handler
     */
    private static class SocketHandler extends Handler {

        /*---------------------------静态常量---------------------------*/

        static final int WHAT_DEVICE_CONNECT = 1;
        static final int WHAT_DEVICE_RECEIVE_DATA = 2;

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
        SocketHandler(ClassicBluetoothSocketServer classicBluetoothSocketServer) {
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
                    if (classicBluetoothSocketServer.onSocketDeviceConnectedListener != null) {
                        classicBluetoothSocketServer.onSocketDeviceConnectedListener.onDeviceConnected(classicBluetoothSocketServer.acceptSocket);
                    }
                    break;
                case WHAT_DEVICE_RECEIVE_DATA:
                    Bundle data = msg.getData();
                    String string = data.getString(KEY_DEVICE_RECEIVE_DATA);
                    if (classicBluetoothSocketServer.onSocketServerReceiveDataListener != null) {
                        classicBluetoothSocketServer.onSocketServerReceiveDataListener.onSocketServerReceiveData(classicBluetoothSocketServer.acceptSocket.getRemoteDevice().getName(), classicBluetoothSocketServer.acceptSocket.getRemoteDevice().getAddress(), string);
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
