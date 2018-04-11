package com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.others.socket_service;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
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

import java.util.ArrayList;

import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothConstants;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothInterface;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothManager;
import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothSocketServer;

/**
 * @author jackie
 */
public class SocketServiceActivity extends BaseAppCompatActivity {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = SocketServiceActivity.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 经典蓝牙Socket服务端实例
     */
    private ClassicBluetoothSocketServer classicBluetoothSocketServer;

    /**
     * 按钮，点击此按钮创建开始服务端
     */
    private Button createButton;

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
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.create_socket_server_btn:
                    startSocket();
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
     * 当有设备连接上时进行的回调
     */
    private ClassicBluetoothInterface.OnSocketDeviceConnectedListener onSocketDeviceConnectedListener = new ClassicBluetoothInterface.OnSocketDeviceConnectedListener() {
        @Override
        public void onDeviceConnected(BluetoothSocket bluetoothSocket) {
            Tool.warnOut(TAG, "设备已连接");
        }
    };
    /**
     * 当收到数据时的回调
     */
    private ClassicBluetoothInterface.OnSocketServerReceiveDataListener onSocketServerReceiveDataListener = new ClassicBluetoothInterface.OnSocketServerReceiveDataListener() {
        @Override
        public void onSocketServerReceiveData(String deviceName, String deviceAddress, String data) {
            Tool.warnOut(TAG, "收到数据：data = " + data);
            String message = deviceName + "：" + data;
            strings.add(message);
            messageRecyclerViewAdapter.notifyItemChanged(strings.size() - 1);
            recyclerView.smoothScrollToPosition(strings.size() - 1);
        }
    };
    /**
     * 服务创建并开始运行时，回调此函数
     */
    private ClassicBluetoothInterface.OnSocketServerCreateSuccessListener onSocketServerCreateSuccessListener = new ClassicBluetoothInterface.OnSocketServerCreateSuccessListener() {
        @Override
        public void onSocketServerCreateSuccess(BluetoothServerSocket bluetoothServerSocket) {
            Tool.warnOut(TAG, "服务创建成功并开始运行时");
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
        initClassicBluetoothSocketServer();
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_socket_service;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {

    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        createButton = findViewById(R.id.create_socket_server_btn);
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
        createButton.setOnClickListener(onClickListener);
        sendButton.setOnClickListener(onClickListener);
    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {
        ClassicBluetoothManager.setBluetoothDiscoverable(SocketServiceActivity.this, 0);
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
        classicBluetoothSocketServer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClassicBluetoothManager.releaseClassicBluetoothSocketServer();
    }
    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化经典蓝牙Socket服务端
     */
    private void initClassicBluetoothSocketServer() {
        classicBluetoothSocketServer = ClassicBluetoothManager.getClassicBluetoothSocketServerInstance(SocketServiceActivity.this);
        classicBluetoothSocketServer.setOnSocketDeviceConnectedListener(onSocketDeviceConnectedListener);
        classicBluetoothSocketServer.setOnSocketServerReceiveDataListener(onSocketServerReceiveDataListener);
        classicBluetoothSocketServer.setOnSocketServerCreateSuccessListener(onSocketServerCreateSuccessListener);
    }

    /**
     * 开启socket服务端
     */
    private void startSocket() {
        boolean init = classicBluetoothSocketServer.init();
        if (!init) {
            Tool.warnOut(TAG, "初始化失败");
            return;
        }

        Tool.warnOut(TAG, "初始化成功");
        int start = classicBluetoothSocketServer.start();
        switch (start) {
            case ClassicBluetoothConstants.SOCKET_SERVER_START_SUCCESS:
                Tool.warnOut(TAG, "开启成功");
                break;
            case ClassicBluetoothConstants.SOCKET_SERVER_STARTED:
                Tool.warnOut(TAG, "服务已经开启了");
                break;
            case ClassicBluetoothConstants.SOCKET_SERVER_UNINITIALIZED:
                Tool.warnOut(TAG, "服务未初始化");
                break;
            default:
                break;
        }
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
            Tool.toastL(SocketServiceActivity.this, R.string.null_input);
            return;
        }

        if (!classicBluetoothSocketServer.sendData(text)) {
            Tool.toastL(SocketServiceActivity.this, R.string.send_failed);
            return;
        }
        Tool.toastL(SocketServiceActivity.this, R.string.send_success);
        editText.setText("");
        String message = "我：" + text;
        strings.add(message);
        messageRecyclerViewAdapter.notifyItemChanged(strings.size() - 1);
        recyclerView.smoothScrollToPosition(strings.size() - 1);
    }
}
