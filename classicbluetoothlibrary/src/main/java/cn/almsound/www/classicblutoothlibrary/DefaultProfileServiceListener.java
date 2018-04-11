package cn.almsound.www.classicblutoothlibrary;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothProfile;

/**
 * 默认的profile监听
 *
 * @author jackie
 */
public class DefaultProfileServiceListener implements BluetoothProfile.ServiceListener {

    /*---------------------------成员变量---------------------------*/

    /**
     * 连接状态被改变时的回调
     */
    private ClassicBluetoothInterface.DefaultOnBluetoothA2dpConnectStateChangedListener defaultOnBluetoothA2DpConnectStateChangedListener;

    /**
     * a2dp实例
     */
    private BluetoothA2dp bluetoothA2dp;

    /*---------------------------实现父类方法---------------------------*/

    /**
     * Called to notify the client when the proxy object has been
     * connected to the service.
     *
     * @param profile - One of {@link #HEALTH}, {@link #HEADSET} or
     *                {@link #A2DP}
     * @param proxy   - One of {@link BluetoothHealth}, {@link BluetoothHeadset} or
     *                {@link BluetoothA2dp}
     */
    @SuppressWarnings("JavadocReference")
    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (profile == BluetoothProfile.A2DP) {
            bluetoothA2dp = (BluetoothA2dp) proxy;
            if (defaultOnBluetoothA2DpConnectStateChangedListener != null) {
                defaultOnBluetoothA2DpConnectStateChangedListener.onBluetoothA2dpConnected(bluetoothA2dp);
            }
        }
    }

    /**
     * Called to notify the client that this proxy object has been
     * disconnected from the service.
     *
     * @param profile - One of {@link #HEALTH}, {@link #HEADSET} or
     *                {@link #A2DP}
     */
    @SuppressWarnings("JavadocReference")
    @Override
    public void onServiceDisconnected(int profile) {
        if (profile == BluetoothProfile.A2DP) {
            bluetoothA2dp = null;
            if (defaultOnBluetoothA2DpConnectStateChangedListener != null) {
                defaultOnBluetoothA2DpConnectStateChangedListener.onBluetoothA2dpDisconnected();
            }
        }
    }

    /*---------------------------公开方法---------------------------*/

    /**
     * 设置连接状态被改变时的回调
     *
     * @param defaultOnBluetoothA2DpConnectStateChangedListener 连接状态被改变时的回调
     */
    void setDefaultOnBluetoothA2DpConnectStateChangedListener(ClassicBluetoothInterface.DefaultOnBluetoothA2dpConnectStateChangedListener defaultOnBluetoothA2DpConnectStateChangedListener) {
        this.defaultOnBluetoothA2DpConnectStateChangedListener = defaultOnBluetoothA2DpConnectStateChangedListener;
    }

    /**
     * 关闭
     */
    void close() {
        defaultOnBluetoothA2DpConnectStateChangedListener = null;
        bluetoothA2dp = null;
    }
}
