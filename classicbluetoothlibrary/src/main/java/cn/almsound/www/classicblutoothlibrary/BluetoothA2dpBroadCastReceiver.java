package cn.almsound.www.classicblutoothlibrary;

import android.annotation.SuppressLint;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 监听A2DP相关的广播接收者
 *
 * @author jackie
 */
class BluetoothA2dpBroadCastReceiver extends BroadcastReceiver {


    private static final Pattern PATTERN = Pattern.compile("[0-9]*");

    private static final String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";

    private static final String TAG = BluetoothA2dpBroadCastReceiver.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * A2DP与设备的连接状态改变时的回调
     */
    private ClassicBluetoothInterface.OnBluetoothA2dpDeviceConnectStateChangedListener onBluetoothA2dpDeviceConnectStateChangedListener;

    /**
     * Handler
     */
    private Handler handler = new Handler();
    private boolean autoBound;
    private String pin;
    private BluetoothDevice remoteDevice;

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
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device == null) {
            return;
        }
        if (handler == null) {
            return;
        }
        switch (action) {
            //A2DP设备的连接状态被改变
            case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                int bluetoothA2dpConnectionState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
                processConnectStateChanged(device, bluetoothA2dpConnectionState);
                break;
            case BluetoothDevice.ACTION_PAIRING_REQUEST:
                if (processAutoBound(intent)) {
                    abortBroadcast();
                    if (onBluetoothA2dpDeviceConnectStateChangedListener != null){
                        onBluetoothA2dpDeviceConnectStateChangedListener.onBluetoothA2dpDeviceAutoBound(device);
                    }
                }else{
                    if (onBluetoothA2dpDeviceConnectStateChangedListener != null){
                        onBluetoothA2dpDeviceConnectStateChangedListener.onBluetoothA2dpDeviceRequestBound(device);
                    }
                }
                break;
            default:
                break;

        }

    }

    private boolean processAutoBound(Intent intent) {
        int mType;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
        } else {
            mType = intent.getIntExtra(EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
        }
        boolean result;
        if (autoBound && pin != null && pin.length() >= 4 && isNumeric(pin) && remoteDevice != null) {
            switch (mType) {
                case BluetoothDevice.PAIRING_VARIANT_PIN:
                    result = autoBoundByPin();
                    break;
                case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                    result = autoBoundByPassword();
                    break;
                default:
                    result = false;
                    break;
            }
        } else {
            result = false;
        }
        return result;
    }

    private boolean autoBoundByPassword() {
        try {
            @SuppressWarnings("JavaReflectionMemberAccess") @SuppressLint("PrivateApi") Method setPasskey = remoteDevice.getClass().getDeclaredMethod("setPasskey", int.class);
            setPasskey.setAccessible(true);
            return (boolean) setPasskey.invoke(remoteDevice, Integer.valueOf(pin));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean autoBoundByPin() {
        boolean result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            result = remoteDevice.setPin(pin.getBytes());
        } else {
            result = setDevicePin(pin);
        }
        return result;
    }

    private void processConnectStateChanged(final BluetoothDevice device, int bluetoothA2dpConnectionState) {
        switch (bluetoothA2dpConnectionState) {
            case BluetoothA2dp.STATE_CONNECTING:
                if (handler == null) {
                    return;
                }
                if (onBluetoothA2dpDeviceConnectStateChangedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onBluetoothA2dpDeviceConnectStateChangedListener.onBluetoothA2dpDeviceConnecting(device);
                        }
                    });
                }
                break;
            case BluetoothA2dp.STATE_CONNECTED:
                if (handler == null) {
                    return;
                }
                if (onBluetoothA2dpDeviceConnectStateChangedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onBluetoothA2dpDeviceConnectStateChangedListener.onBluetoothA2dpDeviceConnected(device);
                        }
                    });
                }
                break;
            case BluetoothA2dp.STATE_DISCONNECTING:
                if (handler == null) {
                    return;
                }
                if (onBluetoothA2dpDeviceConnectStateChangedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onBluetoothA2dpDeviceConnectStateChangedListener.onBluetoothA2dpDeviceDisconnecting(device);
                        }
                    });
                }
                break;
            case BluetoothA2dp.STATE_DISCONNECTED:
                if (handler == null) {
                    return;
                }
                if (onBluetoothA2dpDeviceConnectStateChangedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onBluetoothA2dpDeviceConnectStateChangedListener.onBluetoothA2dpDeviceDisconnected(device);
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置A2DP与设备的连接状态改变时的回调
     *
     * @param onBluetoothA2dpDeviceConnectStateChangedListener A2DP与设备的连接状态改变时的回调
     */
    void setOnBluetoothA2dpDeviceConnectStateChangedListener(ClassicBluetoothInterface.OnBluetoothA2dpDeviceConnectStateChangedListener onBluetoothA2dpDeviceConnectStateChangedListener) {
        this.onBluetoothA2dpDeviceConnectStateChangedListener = onBluetoothA2dpDeviceConnectStateChangedListener;
    }

    /**
     * 关闭
     */
    void close() {
        onBluetoothA2dpDeviceConnectStateChangedListener = null;
        handler = null;
    }

    public static boolean isNumeric(String str) {
        return PATTERN.matcher(str).matches();

    }

    void setRemoteDevice(BluetoothDevice remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    void setPin(String pin) {
        this.pin = pin;
    }

    void setAutoBound(boolean autoBound) {
        this.autoBound = autoBound;
    }

    private boolean setDevicePin(String pin) {
        if (remoteDevice == null) {
            return false;
        }
        try {
            Method setPin = remoteDevice.getClass().getDeclaredMethod("setPin", byte[].class);
            return (boolean) setPin.invoke(remoteDevice, (Object) pin.getBytes());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
}
