package com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.a2dp;

import android.bluetooth.BluetoothDevice;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.www.classicbluetoothlibrary.R;
import com.jackiepenghe.www.classicbluetoothlibrary.utils.Constants;

import java.io.IOException;

import cn.almsound.www.classicblutoothlibrary.BluetoothA2dpClient;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothInterface;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothManager;

/**
 * A2DP
 *
 * @author jackie
 */
public class A2dpActivity extends BaseAppCompatActivity {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = A2dpActivity.class.getSimpleName();


    /*---------------------------成员变量---------------------------*/

    /**
     * A2DP实例
     */
    private BluetoothA2dpClient bluetoothA2DpClient;
    /**
     * 蓝牙设备
     */
    private BluetoothDevice bluetoothDevice;
    /**
     * 媒体播放器
     */
    private MediaPlayer mediaPlayer;
    /**
     * A2DP服务连接状态更改时的回调
     */
    private ClassicBluetoothInterface.OnBluetoothA2dpServiceConnectStateChangedListener onBluetoothA2dpServiceConnectStateChangedListener = new ClassicBluetoothInterface.OnBluetoothA2dpServiceConnectStateChangedListener() {
        @Override
        public void onBluetoothA2dpServiceConnected() {
            Tool.warnOut(TAG, "已连接A2DP");
            boolean b = bluetoothA2DpClient.startConnect(bluetoothDevice);
            if (b) {
                Tool.warnOut(TAG, "开始连接设备");
            } else {
                Tool.warnOut(TAG, "连接设备失败，请确认该设备已与本机配对成功");
            }

//            boolean b = bluetoothA2DpClient.startConnect(bluetoothDevice);
//            if (b) {
//                Tool.warnOut(TAG, "开始连接设备");
//            } else {
//                Tool.warnOut(TAG, "连接设备失败，请确认该设备已与本机配对成功");
//            }

        }

        @Override
        public void onBluetoothA2dpServiceDisconnected() {
            Tool.warnOut(TAG, "已断开A2DP");

        }
    };
    /**
     * 与蓝牙设备的连接状态改变时的监听
     */
    private ClassicBluetoothInterface.OnBluetoothA2dpDeviceConnectStateChangedListener onBluetoothA2dpDeviceConnectStateChangedListener = new ClassicBluetoothInterface.OnBluetoothA2dpDeviceConnectStateChangedListener() {
        @Override
        public void onBluetoothA2dpDeviceConnecting(BluetoothDevice bluetoothDevice) {
            Tool.warnOut(TAG, "正在连接：address = " + bluetoothDevice.getAddress());
        }

        @Override
        public void onBluetoothA2dpDeviceConnected(BluetoothDevice bluetoothDevice) {
            Tool.warnOut(TAG, "已连接：address = " + bluetoothDevice.getAddress());
            startPlay();
        }

        @Override
        public void onBluetoothA2dpDeviceDisconnecting(BluetoothDevice bluetoothDevice) {
            Tool.warnOut(TAG, "正在断开连接：address = " + bluetoothDevice.getAddress());

        }

        @Override
        public void onBluetoothA2dpDeviceDisconnected(BluetoothDevice bluetoothDevice) {
            Tool.warnOut(TAG, "已断开连接：address = " + bluetoothDevice.getAddress());
        }
    };

    /*---------------------------实现父类方法---------------------------*/

    /**
     * 标题栏的返回按钮被按下的时候回调此函数
     */
    @Override
    protected void titleBackClicked() {
        onBackPressed();
    }

    /**
     * 在设置布局之前需要进行的操作
     */
    @Override
    protected void doBeforeSetLayout() {
        Parcelable parcelableExtra = getIntent().getParcelableExtra(Constants.DEVICE);
        if (parcelableExtra instanceof BluetoothDevice) {
            bluetoothDevice = (BluetoothDevice) parcelableExtra;
        }
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_a2_dp;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        initBluetoothA2DPClient();
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {

    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {

    }

    /**
     * 初始化其他数据
     */
    @Override
    protected void initOtherData() {

    }

    /**
     * 初始化事件
     */
    @Override
    protected void initEvents() {

    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {
    }

    /**
     * 设置菜单
     *
     * @param menu 菜单
     * @return 只是重写 public boolean onCreateOptionsMenu(Menu menu)
     */
    @Override
    protected boolean createOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * 设置菜单监听
     *
     * @param item 菜单的item
     * @return true表示处理了监听事件
     */
    @Override
    protected boolean optionsItemSelected(MenuItem item) {
        return false;
    }

    /*---------------------------重写父类方法---------------------------*/

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopPlay();
        bluetoothA2DpClient.disconnect();
        bluetoothA2DpClient.close();
        ClassicBluetoothManager.releaseBluetoothA2DPClient();
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化A2DP客户端
     */
    private void initBluetoothA2DPClient() {
        bluetoothA2DpClient = ClassicBluetoothManager.getBluetoothA2DPClientInstance(A2dpActivity.this);
        if (bluetoothA2DpClient == null) {
            return;
        }
        boolean init = bluetoothA2DpClient.init(onBluetoothA2dpServiceConnectStateChangedListener);
        if (!init) {
            Tool.warnOut(TAG, "初始化失败");
            return;
        }
        bluetoothA2DpClient.setOnBluetoothA2dpDeviceConnectStateChangedListener(onBluetoothA2dpDeviceConnectStateChangedListener);
    }

    /**
     * 开始播放
     */
    private void startPlay() {

        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (mAudioManager != null) {
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        }
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.song1);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Tool.warnOut(TAG, "播放完成");
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    private void stopPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
