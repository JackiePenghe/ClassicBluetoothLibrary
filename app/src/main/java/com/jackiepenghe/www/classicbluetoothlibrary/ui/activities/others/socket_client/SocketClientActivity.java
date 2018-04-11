package com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.socket_client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.DefaultItemDecoration;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.www.classicbluetoothlibrary.R;
import com.jackiepenghe.www.classicbluetoothlibrary.adapter.MessageRecyclerViewAdapter;
import com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.socket_service.SocketServiceActivity;
import com.jackiepenghe.www.classicbluetoothlibrary.utils.Constants;

import java.util.ArrayList;

import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothInterface;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothManager;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothSocketClient;

/**
 * 连接设备的界面
 *
 * @author jackie
 */
public class SocketClientActivity extends BaseAppCompatActivity {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = SocketClientActivity.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 经典蓝牙Socket客户端
     */
    private ClassicBluetoothSocketClient classicBluetoothSocketClient;

    /**
     * 蓝牙设备
     */
    private BluetoothDevice bluetoothDevice;

    /**
     * 按钮
     */
    private Button button;

    /**
     * 发送信息按钮
     */
    private Button sendButton;

    /**
     * 显示数据内容的列表
     */
    private RecyclerView recyclerView;

    /**
     * 输入数据用以发送文本框
     */
    private EditText editText;

    /**
     * 适配器的数据源
     */
    private ArrayList<String> strings = new ArrayList<>();

    /**
     * 适配器
     */
    private MessageRecyclerViewAdapter messageRecyclerViewAdapter = new MessageRecyclerViewAdapter(strings);

    /**
     * 点击事件的监听
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.connect_btn:
                    boolean b = classicBluetoothSocketClient.startConnect();
                    if (b) {
                        Tool.warnOut(TAG, "成功发起连接");
                    } else {
                        Tool.warnOut(TAG, "发起连接失败");
                    }
                    break;
                case R.id.send_btn:
                    sendMessage();
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 连接成功的回调
     */
    private ClassicBluetoothInterface.OnConnectSocketServerSuccessListener onConnectSocketServerSuccessListener = new ClassicBluetoothInterface.OnConnectSocketServerSuccessListener() {
        @Override
        public void onConnectSocketServerSuccess(BluetoothSocket bluetoothSocket) {
            Tool.warnOut(TAG, " 连接成功");
        }
    };
    /**
     * 收到数据的回调
     */
    private ClassicBluetoothInterface.OnReceiveSocketServerDataListener onReceiveSocketServerDataListener = new ClassicBluetoothInterface.OnReceiveSocketServerDataListener() {
        @Override
        public void onReceiveSocketServerData(String deviceName, String deviceAddress, String data) {
            Tool.warnOut(TAG, " 收到数据：data = " + data);
            String message = deviceName + "：" + data;
            strings.add(message);
            messageRecyclerViewAdapter.notifyItemChanged(strings.size() - 1);
            recyclerView.smoothScrollToPosition(strings.size() - 1);
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
        return R.layout.activity_socket_client;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        initClassicBluetoothSocketClient();
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        button = findViewById(R.id.connect_btn);
        sendButton = findViewById(R.id.send_btn);
        recyclerView = findViewById(R.id.message_rv);
        editText = findViewById(R.id.message_et);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        initRecyclerViewData();
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
        button.setOnClickListener(onClickListener);
        sendButton.setOnClickListener(onClickListener);
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
        classicBluetoothSocketClient.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClassicBluetoothManager.releaseClassicBluetoothSocketClient();
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化Socket客户端
     */
    private void initClassicBluetoothSocketClient() {
        classicBluetoothSocketClient = ClassicBluetoothManager.getClassicBluetoothSocketClientInstance(SocketClientActivity.this);
        if (classicBluetoothSocketClient == null) {
            return;
        }
        if (bluetoothDevice == null) {
            return;
        }
        classicBluetoothSocketClient.init(bluetoothDevice.getAddress());
        classicBluetoothSocketClient.setOnConnectSocketServerSuccessListener(onConnectSocketServerSuccessListener);
        classicBluetoothSocketClient.setOnReceiveSocketServerDataListener(onReceiveSocketServerDataListener);
    }

    /**
     * 初始化RecyclerView数据
     */
    private void initRecyclerViewData() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DefaultItemDecoration defaultItemDecoration = new DefaultItemDecoration(Color.GRAY, ViewGroup.LayoutParams.MATCH_PARENT, 1, -1);
        recyclerView.addItemDecoration(defaultItemDecoration);
        recyclerView.setAdapter(messageRecyclerViewAdapter);
    }

    /**
     * 发送输入的内容
     */
    private void sendMessage() {
        String text = editText.getText().toString();
        if ("".equals(text)) {
            Tool.toastL(SocketClientActivity.this, R.string.null_input);
            return;
        }

        if (!classicBluetoothSocketClient.sendData(text)) {
            Tool.toastL(SocketClientActivity.this, R.string.send_failed);
            return;
        }
        Tool.toastL(SocketClientActivity.this, R.string.send_success);
        editText.setText("");
        String message = "我：" + text;
        strings.add(message);
        messageRecyclerViewAdapter.notifyItemChanged(strings.size() - 1);
        recyclerView.smoothScrollToPosition(strings.size() - 1);
    }
}
