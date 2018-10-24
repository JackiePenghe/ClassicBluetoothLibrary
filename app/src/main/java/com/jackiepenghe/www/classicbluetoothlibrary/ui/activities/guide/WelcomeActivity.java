package com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.guide;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.jackiepenghe.baselibrary.BaseWelcomeActivity;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.www.classicbluetoothlibrary.R;
import com.jackiepenghe.www.classicbluetoothlibrary.ui.activities.MainActivity;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.Setting;

import java.util.List;

import cn.almsound.www.classicblutoothlibrary.ClassicBluetoothManager;

/**
 * 欢迎页
 *
 * @author jackie
 */
public class WelcomeActivity extends BaseWelcomeActivity {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 当用户拒绝了某一个权限，但是程序运行有必须这个权限时，就需要使用Rationale
     */
    private Rationale<List<String>> rationale = new Rationale<List<String>>() {
        @Override
        public void showRationale(Context context, List<String> permissions, final RequestExecutor executor) {
            List<String> permissionNames = Permission.transformText(context, permissions);
            String message = context.getString(R.string.message_permission_rationale, TextUtils.join("\n", permissionNames));

            new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle(R.string.title_dialog)
                    .setMessage(message)
                    .setPositiveButton(R.string.resume, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.execute();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.cancel();
                        }
                    })
                    .show();
        }
    };

    /**
     * 当权限请求全部通过时，执行的动作
     */
    private Action<List<String>> grantedAction = new Action<List<String>>() {
        @Override
        public void onAction(List<String> permissions) {
            toNext();
        }
    };
    private Setting.Action onComeback = new Setting.Action() {
        @Override
        public void onAction() {
            doAfterAnimation();
        }
    };

    /**
     * 当权限请求失败的时候，执行的动作
     */
    private Action<List<String>> deniedAction = new Action<List<String>>() {
        @Override
        public void onAction(List<String> permissions) {

            for (int i = 0; i < permissions.size(); i++) {
                String permission = permissions.get(i);
                Tool.warnOut(TAG, "permission = " + permission);
            }

            if (AndPermission.hasAlwaysDeniedPermission(WelcomeActivity.this, permissions)) {
                List<String> permissionNames = Permission.transformText(WelcomeActivity.this, permissions);
                String message = WelcomeActivity.this.getString(R.string.message_permission_always_failed, TextUtils.join("\n", permissionNames));
                new AlertDialog.Builder(WelcomeActivity.this)
                        .setCancelable(false)
                        .setTitle(R.string.title_dialog)
                        .setMessage(message)
                        .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AndPermission.with(WelcomeActivity.this)
                                        .runtime()
                                        .setting()
                                        .onComeback(onComeback)
                                        .start();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        }
    };


    /*---------------------------实现父类方法---------------------------*/

    /**
     * 当动画执行完成后调用这个函数
     */
    @Override
    protected void doAfterAnimation() {

        if (!ClassicBluetoothManager.isSupportBluetooth()) {
            Tool.toastL(WelcomeActivity.this, R.string.no_bluetooth_module);
            onBackPressed();
            return;
        }

        if (!ClassicBluetoothManager.isBluetoothOpened()) {
            ClassicBluetoothManager.openBlueTooth(WelcomeActivity.this);
        }

        AndPermission.with(WelcomeActivity.this)
                .runtime()
                .permission(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)
                .rationale(rationale)
                .onGranted(grantedAction)
                .onDenied(deniedAction)
                .start();
    }

    /**
     * 设置ImageView的图片资源
     *
     * @return 图片资源ID
     */
    @Override
    protected int setImageViewSource() {
        return 0;
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 跳转到下一个界面
     */
    private void toNext() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        onBackPressed();
    }
}
