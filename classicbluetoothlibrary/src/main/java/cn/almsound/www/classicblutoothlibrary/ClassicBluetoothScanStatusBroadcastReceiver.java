package cn.almsound.www.classicblutoothlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;

/**
 * @author jackie
 */
class ClassicBluetoothScanStatusBroadcastReceiver extends BroadcastReceiver {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = ClassicBluetoothScanStatusBroadcastReceiver.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 发现一个设备时的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener;

    /**
     * 扫描状态改变时的回调
     */
    private ClassicBluetoothInterface.OnScanStatusChangedListener onScanStatusChangedListener;

    /**
     * 发现一个新的设备时的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener;

    /**
     * 发现一个被绑定的设备时的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneBoundedDeviceListener onScanFindOneBoundedDeviceListener;

    /**
     * 发现一个新的被绑定的设备时的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneNewBoundedDeviceListener onScanFindOneNewBoundedDeviceListener;

    /**
     * 发现一个绑定中设备时的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneBoundingDeviceListener onScanFindOneBoundingDeviceListener;

    /**
     * 发现一个新的绑定中设备时的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneNewBoundingDeviceListener onScanFindOneNewBoundingDeviceListener;

    /**
     * 发现一个未绑定设备时的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneUnboundedDeviceListener onScanFindOneUnboundedDeviceListener;

    /**
     * 发现一个新的未绑定设备时的回调
     */
    private ClassicBluetoothInterface.OnScanFindOneNewUnboundedDeviceListener onScanFindOneNewUnboundedDeviceListener;

    /**
     * 记录扫描到的设备集合
     */
    private ArrayList<BluetoothDevice> bluetoothDevices;

    /**
     * 已绑定的设备的列表
     */
    private ArrayList<BluetoothDevice> boundedDevices;

    /**
     * 未绑定的设备的列表
     */
    private ArrayList<BluetoothDevice> unboundedDevices;

    /**
     * 绑定中的设备的列表
     */
    private ArrayList<BluetoothDevice> boundingDevices;

    /**
     * Handler
     */
    private Handler handler = new Handler();

    /**
     * 线程创建工具类
     */
    private ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r);
        }
    };

    /*---------------------------实现父类方法---------------------------*/

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * {@link Context#registerReceiver(BroadcastReceiver, * IntentFilter, String, Handler)}. When it runs on the main
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     * <p>
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b> This means you should not perform any operations that
     * return a result to you asynchronously. If you need to perform any follow up
     * background work, schedule a {@link JobService} with
     * {@link JobScheduler}.
     * <p>
     * If you wish to interact with a service that is already running and previously
     * bound using {@link Context#bindService(Intent, ServiceConnection, int) bindService()},
     * you can use {@link #peekService}.
     * <p>
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @SuppressWarnings({"JavadocReference", "JavaDoc"})
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                startThreadToDoWhileFoundDevice(device);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                bluetoothDevices.clear();
                boundedDevices.clear();
                boundingDevices.clear();
                unboundedDevices.clear();
                if (onScanStatusChangedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onScanStatusChangedListener.onScanStarted();
                        }
                    });
                }
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                if (onScanStatusChangedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onScanStatusChangedListener.onScanFinished();
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 当发现一个蓝牙设备时，启动一个线程去执行操作
     *
     * @param device 蓝牙设备
     */
    private void startThreadToDoWhileFoundDevice(final BluetoothDevice device) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (onScanFindOneDeviceListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onScanFindOneDeviceListener.onScanFindADevice(device);
                        }
                    });
                }

                if (!bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device);
                    if (onScanFindOneNewDeviceListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onScanFindOneNewDeviceListener.onScanFindOneNewDevice(device);
                            }
                        });
                    }
                }

                int bondState = device.getBondState();
                switch (bondState) {
                    case BluetoothDevice.BOND_NONE:
                        if (onScanFindOneUnboundedDeviceListener != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onScanFindOneUnboundedDeviceListener.onScanFindOneUnboundedDevice(device);
                                }
                            });
                        }

                        if (!unboundedDevices.contains(device)) {
                            unboundedDevices.add(device);
                            if (onScanFindOneNewUnboundedDeviceListener != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onScanFindOneNewUnboundedDeviceListener.onScanFindOneNewUnboundedDevice(device);
                                    }
                                });
                            }
                        }
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        if (onScanFindOneBoundingDeviceListener != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onScanFindOneBoundingDeviceListener.onScanFindOneBounding(device);
                                }
                            });
                        }

                        if (!boundingDevices.contains(device)) {
                            boundingDevices.add(device);
                            if (onScanFindOneNewBoundingDeviceListener != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onScanFindOneNewBoundingDeviceListener.onScanFindOneNewBounding(device);
                                    }
                                });
                            }
                        }
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        if (onScanFindOneBoundedDeviceListener != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onScanFindOneBoundedDeviceListener.onScanFindOneBoundedDevice(device);
                                }
                            });
                        }

                        if (!boundedDevices.contains(device)) {
                            boundedDevices.add(device);
                            if (onScanFindOneNewBoundedDeviceListener != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onScanFindOneNewBoundedDeviceListener.onScanFindOneNewBoundedDevice(device);
                                    }
                                });
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        Thread thread = threadFactory.newThread(runnable);
        thread.start();
    }

    /*---------------------------库内方法---------------------------*/

    /**
     * 设置蓝牙扫描的结果存放的集合
     *
     * @param bluetoothDevices 蓝牙扫描的结果存放的集合
     */
    void setBluetoothDevices(@NonNull ArrayList<BluetoothDevice> bluetoothDevices) {
        this.bluetoothDevices = bluetoothDevices;
    }

    /**
     * 设置被绑定的蓝牙扫描的结果存放集合
     *
     * @param boundedDevices 被绑定的蓝牙扫描的结果存放集合
     */
    void setBoundedDevices(@NonNull ArrayList<BluetoothDevice> boundedDevices) {
        this.boundedDevices = boundedDevices;
    }

    /**
     * 设置未绑定的蓝牙扫描的结果存放集合
     *
     * @param unboundedDevices 未绑定的蓝牙扫描的结果存放集合
     */
    public void setUnboundedDevices(ArrayList<BluetoothDevice> unboundedDevices) {
        this.unboundedDevices = unboundedDevices;
    }

    /**
     * 设置绑定中的蓝牙扫描的结果存放集合
     *
     * @param boundingDevices 绑定中的蓝牙扫描的结果存放集合
     */
    public void setBoundingDevices(ArrayList<BluetoothDevice> boundingDevices) {
        this.boundingDevices = boundingDevices;
    }

    /**
     * 设置发现一个设备时的回调
     *
     * @param onScanFindOneDeviceListener 发现一个设备时的回调
     */
    void setOnScanFindOneDeviceListener(ClassicBluetoothInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener) {
        this.onScanFindOneDeviceListener = onScanFindOneDeviceListener;
    }

    /**
     * 设置扫描状态更改时的回调
     *
     * @param onScanStatusChangedListener 扫描状态更改时的回调
     */
    void setOnScanStatusChangedListener(ClassicBluetoothInterface.OnScanStatusChangedListener onScanStatusChangedListener) {
        this.onScanStatusChangedListener = onScanStatusChangedListener;
    }

    /**
     * 设置发现一个新设备时的回调
     *
     * @param onScanFindOneNewDeviceListener 发现一个新设备时的回调
     */
    void setOnScanFindOneNewDeviceListener(ClassicBluetoothInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener) {
        this.onScanFindOneNewDeviceListener = onScanFindOneNewDeviceListener;
    }

    /**
     * 设置发现一个被绑定的设备时的回调
     *
     * @param onScanFindOneBoundedDeviceListener 发现一个被绑定的设备时的回调
     */
    void setOnScanFindOneBoundedDeviceListener(ClassicBluetoothInterface.OnScanFindOneBoundedDeviceListener onScanFindOneBoundedDeviceListener) {
        this.onScanFindOneBoundedDeviceListener = onScanFindOneBoundedDeviceListener;
    }

    /**
     * 设置发现一个新的被绑定的设备时的回调
     *
     * @param onScanFindOneNewBoundedDeviceListener 发现一个新的被绑定的设备时的回调
     */
    void setOnScanFindOneNewBoundedDeviceListener(ClassicBluetoothInterface.OnScanFindOneNewBoundedDeviceListener onScanFindOneNewBoundedDeviceListener) {
        this.onScanFindOneNewBoundedDeviceListener = onScanFindOneNewBoundedDeviceListener;
    }

    /**
     * 设置发现一个绑定中的设备时的回调
     *
     * @param onScanFindOneBoundingDeviceListener 发现一个绑定中的设备时的回调
     */
    void setOnScanFindOneBoundingDeviceListener(ClassicBluetoothInterface.OnScanFindOneBoundingDeviceListener onScanFindOneBoundingDeviceListener) {
        this.onScanFindOneBoundingDeviceListener = onScanFindOneBoundingDeviceListener;
    }

    /**
     * 设置发现一个新的绑定中的设备时的回调
     *
     * @param onScanFindOneNewBoundingDeviceListener 发现一个新的绑定中的设备时的回调
     */
    void setOnScanFindOneNewBoundingDeviceListener(ClassicBluetoothInterface.OnScanFindOneNewBoundingDeviceListener onScanFindOneNewBoundingDeviceListener) {
        this.onScanFindOneNewBoundingDeviceListener = onScanFindOneNewBoundingDeviceListener;
    }

    /**
     * 设置发现一个未绑定的设备时的回调
     *
     * @param onScanFindOneUnboundedDeviceListener 发现一个未绑定的设备时的回调
     */
    void setOnScanFindOneUnboundedDeviceListener(ClassicBluetoothInterface.OnScanFindOneUnboundedDeviceListener onScanFindOneUnboundedDeviceListener) {
        this.onScanFindOneUnboundedDeviceListener = onScanFindOneUnboundedDeviceListener;
    }

    /**
     * 设置发现一个新的未绑定的设备时的回调
     *
     * @param onScanFindOneNewUnboundedDeviceListener 发现一个新的未绑定的设备时的回调
     */
    void setOnScanFindOneNewUnboundedDeviceListener(ClassicBluetoothInterface.OnScanFindOneNewUnboundedDeviceListener onScanFindOneNewUnboundedDeviceListener) {
        this.onScanFindOneNewUnboundedDeviceListener = onScanFindOneNewUnboundedDeviceListener;
    }
}
