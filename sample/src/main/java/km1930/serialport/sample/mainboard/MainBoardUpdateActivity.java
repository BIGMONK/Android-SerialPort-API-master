package km1930.serialport.sample.mainboard;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.SerialPort;
import android.serialport.sample.R;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.FileCallback;
import com.lzy.okhttputils.request.BaseRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import km1930.serialport.sample.Application;
import okhttp3.Response;

public class MainBoardUpdateActivity extends Activity {

    private String TAG = this.getClass().getSimpleName();
    @BindView(R.id.activity_main_board_update)
    RelativeLayout activity_main_board_update;
    @BindView(R.id.tv_current_hv)//当前硬件版本号
            TextView tv_current_hv;
    @BindView(R.id.tv_current_sv)//当前软件版本号
            TextView tv_current_sv;

    @BindView(R.id.btn_reset_mainboard)
    TextView btn_reset_mainboard;
    @BindView(R.id.tv_current_hvt)
    TextView tv_current_hvt;
    @BindView(R.id.tv_current_svt)
    TextView tv_current_svti;

    @BindView(R.id.tv_current_svtt)//当前软件类型
            TextView tv_current_svt;
    @BindView(R.id.rpbMsg)
    TextView rpbMsg;
    @BindView(R.id.btn_recheck)
    TextView reCheck;
    //蓝牙连接状态
    private int BleConnectStatu;
    //主控板升级标记大全
    private int updateFlag;
    private boolean isLHVGot;
    private boolean isLSVGot;
    private boolean isStartUpdate;
    private boolean isTransOK;
    private boolean isAddrSetting;
    private int addrSettingTimes;
    private boolean isAddrSetting34;
    private int softType;
    private int currentHVC;
    private int currentSVC;
    private int remoteHVC;
    private int remoteSVC;
    private boolean isFileSetting;
    private DecimalFormat dataFormat;
    private BoardRequestInfo.DataBean data;
    private AlertDialog.Builder builder;

    protected Application mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                        Log.d(TAG, "ReadThread got datas size= " + size);
                    } else {
                        Log.d(TAG, "ReadThread got no datas  ");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate: ");
        setContentView(R.layout.activity_main_board_update);
        ButterKnife.bind(this);
        dataFormat = new DecimalFormat("00.00");
        mApplication = (Application) getApplication();
        try {
//            mSerialPort = mApplication.getSerialPort();
            mSerialPort = new SerialPort(new File("dev/ttyS1/"), 115200, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            /* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }
    }


    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MainBoardUpdateActivity.this.finish();
            }
        });
        b.show();
    }

    private Runnable runnableCheckLocal;
    private boolean isLocalChecking;

    private void sendDataBySerial(byte[] bytes) {
        try {
            mOutputStream.write(bytes);
//            mOutputStream.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case HandlerWhat.IS_UPDAT_READY://蓝牙就绪，可以开始升级
                    rpbMsg.setText("设备已就绪,检测当前设备信息");
                    Log.d(TAG, "MainBoardCommand.IS_UPDAT_READY");
                    runnableCheckLocal = new Runnable() {
                        @Override
                        public void run() {
                            isLocalChecking = true;
                            while (isTransOK) {
                                //开始升级
                                sendDataBySerial(MainBoardCommand.START_UPDATE);
                                isStartUpdate = true;
                                Log.d(TAG, "MainBoardCommand.START_UPDATE");
                                isTransOK = false;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            //获取硬件版本号
                            sendDataBySerial(MainBoardCommand.GET_HARD_VERSION);
                            Log.d(TAG, "MainBoardCommand.GET_HARD_VERSION");

                            synchronized (MainBoardUpdateActivity.class) {
                                try {
                                    MainBoardUpdateActivity.class.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            //获取软件版本号
                            sendDataBySerial(MainBoardCommand.GET_SOFT_VERSION);
                            Log.d(TAG, "MainBoardCommand.GET_SOFT_VERSION");

                            synchronized (MainBoardUpdateActivity.class) {
                                try {
                                    MainBoardUpdateActivity.class.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            //写入地址
                            isAddrSetting = true;
                            handler.sendEmptyMessageDelayed(HandlerWhat.IS_ADDR_SETTING, 50);

                        }
                    };
                    Thread thread = new Thread(runnableCheckLocal);
                    thread.start();
                    break;
                case HandlerWhat.IS_ADDR_SET_ERR:
                    Log.d(TAG, "HandlerWhat.IS_ADDR_SET_ERR");
                    if (addrSettingTimes < 4) {
                        handler.sendEmptyMessageDelayed(HandlerWhat.IS_ADDR_SETTING, 50);
                    } else {
                        rpbMsg.setText("固件类型获取失败");
                    }
                    break;
                case HandlerWhat.IS_ADDR_SET_OK:
                    rpbMsg.setText("固件类型获取成功");
                    Log.d(TAG, "HandlerWhat.IS_ADDR_SET_OK");
                    if (isAddrSetting34) {
                        //响应设置3400，所以原来是9800，需要升级为A
                        softType = 1;
                        tv_current_svt.setText("B");
                    } else {
                        //响应设置9800，所以原来是3400，需要升级为B
                        softType = 2;
                        tv_current_svt.setText("A");
                    }
                    isAddrSetting = false;

                    fileCheck(currentSVC, softType, currentHVC);
//                fileCheck(0, softType, 1);

                    break;
                case HandlerWhat.IS_ADDR_SETTING:
                    Log.d(TAG, "HandlerWhat.IS_ADDR_SETTING");
                    rpbMsg.setText("获取当前设备固件类型");
                    addrSettingTimes++;
                    if (isAddrSetting) {
                        if (!isAddrSetting34) {
                            sendDataBySerial(MainBoardCommand.makeAppAddress3400());
                            isAddrSetting34 = true;
                        } else {
                            sendDataBySerial(MainBoardCommand.makeAppAddress9800());
                            isAddrSetting34 = false;
                        }
                    }
                    break;
                case HandlerWhat.IS_ANS_ERR://主控板响应
                    Log.d(TAG, "HandlerWhat.IS_ANS_ERR");
                    rpbMsg.setText("升级失败" + HandlerWhat.IS_ANS_ERR + "，请重启单车和系统");
                    break;
                case HandlerWhat.IS_ANS_OK:
                    Log.d(TAG, "HandlerWhat.IS_ANS_OK  sendCount=" + sendCount + "  " +
                            "fileSize=" + fileSize + "  " + (dataFormat.format(sendCount * 1f /
                            fileSize * 100)) + "%");
                    if (sendCount < fileSize) {
                        rpbMsg.setText("正在升级中……" + (dataFormat.format(sendCount * 1f / fileSize *
                                100)) + "%");
                        sendCount++;
                    }
                    synchronized (MainBoardUpdateActivity.class) {
                        MainBoardUpdateActivity.class.notify();
                    }
                    break;
                case HandlerWhat.IS_ANS_WAITING:
                    break;
                case HandlerWhat.IS_FILE_SET_ERR://文件发送次数
                    Log.d(TAG, "HandlerWhat.IS_FILE_SET_ERR");
                    rpbMsg.setText("升级失败" + HandlerWhat.IS_FILE_SET_ERR + "，请重启单车和系统");
                    break;
                case HandlerWhat.IS_FILE_SET_OK:
                    Log.d(TAG, "HandlerWhat.IS_FILE_SET_OK");
                    rpbMsg.setText("开始发送程序");
                    isFileSetting = false;
                    synchronized (MainBoardUpdateActivity.class) {
                        MainBoardUpdateActivity.class.notify();
                    }
                    break;
                case HandlerWhat.IS_FILE_SETTING:
                    rpbMsg.setText("准备发送程序");
                    synchronized (MainBoardUpdateActivity.class) {
                        MainBoardUpdateActivity.class.notify();
                    }
                    break;
                case HandlerWhat.IS_LHV_GOT://获取到本地硬件版本号
                    currentHVC = message.arg1;
                    rpbMsg.setText("获取当前硬件版本号成功");
                    Log.d(TAG, "HandlerWhat.IS_LHV_GOT  = " + currentHVC);
                    tv_current_hv.setText(message.arg1 + "");
                    synchronized (MainBoardUpdateActivity.class) {
                        MainBoardUpdateActivity.class.notify();
                    }
                    break;
                case HandlerWhat.IS_LSV_GOT://获取到本地软件版本号
                    rpbMsg.setText("获取当前软件版本号成功");
                    currentSVC = message.arg1;
                    Log.d(TAG, "HandlerWhat.IS_LSV_GOT  " + currentSVC);
                    tv_current_sv.setText(message.arg1 + "");
                    synchronized (MainBoardUpdateActivity.class) {
                        MainBoardUpdateActivity.class.notify();
                    }
                    break;
                case HandlerWhat.IS_HV_SET_ERR://硬件版本号设置
                    Log.d(TAG, "HandlerWhat.IS_HV_SET_ERR");
                    rpbMsg.setText("硬件版本号设置失败");
                    break;
                case HandlerWhat.IS_HV_SET_OK:
                    Log.d(TAG, "HandlerWhat.IS_HV_SET_OK");
                    isHVCSetting = false;
                    rpbMsg.setText("硬件版本号设置成功");
                    synchronized (MainBoardUpdateActivity.class) {
                        MainBoardUpdateActivity.class.notify();
                    }
                    break;
                case HandlerWhat.IS_HV_SETTING:
                    break;
                case HandlerWhat.IS_SV_SET_ERR://软件版本号设置
                    Log.d(TAG, "HandlerWhat.IS_SV_SET_ERR");
                    isSVCSetting = false;
                    rpbMsg.setText("软件版本号设置失败");
                    break;
                case HandlerWhat.IS_SV_SET_OK:
                    Log.d(TAG, "HandlerWhat.IS_SV_SET_OK");
                    isSVCSetting = false;
                    rpbMsg.setText("软件版本号设置成功");
                    synchronized (MainBoardUpdateActivity.class) {
                        MainBoardUpdateActivity.class.notify();
                    }
                    break;
                case HandlerWhat.IS_SV_SETTING:
                    break;
                case HandlerWhat.IS_UPDAT_OK:
                    rpbMsg.setText("升级成功");
                    if (softType == 1) {
                        tv_current_svt.setText("A");
                    } else if (softType == 2) {
                        tv_current_svt.setText("B");
                    }
                    tv_current_sv.setText("" + remoteSVC);
                    tv_current_hv.setText("" + remoteHVC);
                    break;
            }
        }
    };


    @OnClick({R.id.tv_current_hvt
            , R.id.tv_current_svt
            , R.id.btn_reset_mainboard
            , R.id.btn_recheck
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_recheck:
                fileCheck(currentSVC, softType, currentHVC);
                break;
            case R.id.tv_current_hvt:
//                inputSystemManager.sendData(SampleGattAttributes.MAINBOARD, HandlerWhat
//                        .GET_HARD_VERSION);
                Log.d(TAG, "onClick:GET_HARD_VERSION ");
                sendDataBySerial(MainBoardCommand.GET_HARD_VERSION);
                break;
            case R.id.tv_current_svt:
//                inputSystemManager.sendData(SampleGattAttributes.MAINBOARD, HandlerWhat
//                        .GET_SOFT_VERSION);
                Log.d(TAG, "onClick: GET_SOFT_VERSION");
                sendDataBySerial(MainBoardCommand.GET_SOFT_VERSION);
                break;
            case R.id.btn_reset_mainboard:
                sendDataBySerial(MainBoardCommand.RESET);
                isStartUpdate = false;
                tv_current_hv.setText("--");
                tv_current_sv.setText("--");
                tv_current_svt.setText("--");
                isLocalChecking = false;
                isTransOK = false;
                isAddrSetting = false;
                addrSettingTimes = 0;
                isAddrSetting34 = false;
                softType = 0;
                currentHVC = 0;
                currentSVC = 0;
                remoteHVC = 0;
                remoteSVC = 0;
                isFileSetting = false;
                break;
        }
    }

    ArrayList<Byte> getDatas = new ArrayList<>();


    protected void onDataReceived(byte[] buffer, int size) {
        for (int i = 0; i < size; i++) {
            getDatas.add(buffer[i]);
            Log.d(TAG, "onDataReceived:getDatas.add=" + buffer[i]);
            if (getDatas.size() == 9 && ((getDatas.get(0) == 85 && getDatas.get(8) == -86) ||
                    (getDatas.get(0) == -86 && getDatas.get(8) == 85))) {
                //TODO 获取到一次正确的数据
                Log.e(TAG, "onDataReceived: getDatas.size()==9&&(getDatas.get(0)==85||getDatas" +
                        ".get(0)==-86)");


                ArrayList<Byte> data = new ArrayList<Byte>();
                data.addAll(getDatas);

                onSerialOrBleDatasChanged(SampleGattAttributes.MAINBOARD, data);
                getDatas.clear();

            }
        }
        if (getDatas.size() >= 9) {
            getDatas.clear();
            Log.d(TAG, "onDataReceived: getDatas.size()>=9");
        }
    }

    @Override
    protected void onDestroy() {

        isRunableRunning = false;
        if (isStartUpdate) {
            sendDataBySerial(MainBoardCommand.RESET);
            Log.d(TAG, "onDestroy: 重置");
        }
        if (mReadThread != null) mReadThread.interrupt();
        mApplication.closeSerialPort();
        mSerialPort = null;
        OkHttpUtils.getInstance().cancelTag(this);

        super.onDestroy();
    }

    public void onSerialOrBleDatasChanged(int type, ArrayList<Byte> values) {
        if (type == SampleGattAttributes.MAINBOARD) {//主控板传输数据过来
            if (values.get(1) == (byte) 0xFF
//                        && values[7] == 1//说明文档不存在，实际数据存在
                    ) {
                if (values.get(2) == (byte) 0x4F
                        && values.get(3) == (byte) 0x4B) {  //设置指令返回成功
                    if (isAddrSetting) {//地址设置成功
                        handler.sendEmptyMessage(HandlerWhat.IS_ADDR_SET_OK);
                    } else if (isFileSetting) {//发送次数设置成功
                        handler.sendEmptyMessage(HandlerWhat.IS_FILE_SET_OK);
                    } else if (isRunableRunning && isSVCSetting) {
                        handler.sendEmptyMessageDelayed(HandlerWhat.IS_SV_SET_OK, 20);
                    } else if (isRunableRunning && isHVCSetting) {
                        handler.sendEmptyMessageDelayed(HandlerWhat.IS_HV_SET_OK, 20);
                    } else if (isRunableRunning) {//发送数据包
                        handler.sendEmptyMessageDelayed(HandlerWhat.IS_ANS_OK, 20);
                    }

                } else if (values.get(2) == (byte) 0x45
                        && values.get(3) == (byte) 0x52
                        && values.get(4) == (byte) 0x52) { //设置指令返回错误
                    if (isAddrSetting) {//地址设置错误
                        handler.sendEmptyMessage(HandlerWhat.IS_ADDR_SET_ERR);
                    } else if (isFileSetting) {//发送此时设置失败
                        handler.sendEmptyMessage(HandlerWhat.IS_FILE_SET_ERR);
                    } else if (isRunableRunning && isSVCSetting) {
                        handler.sendEmptyMessageDelayed(HandlerWhat.IS_SV_SET_ERR, 20);
                    } else if (isRunableRunning && isHVCSetting) {
                        handler.sendEmptyMessageDelayed(HandlerWhat.IS_HV_SET_ERR, 20);
                    } else if (isRunableRunning) {
                        handler.sendEmptyMessageDelayed(HandlerWhat.IS_ANS_ERR, 20);
                    }
                }
            } else if (values.get(1) == (byte) 0x81
//                        && values[7] == 8//说明文档不存在，实际数据存在
                    ) { //得到软件版本号
                int codes = (values.get(3) < 0 ? values.get(3) & 255 : values.get(3)) * 255 +
                        (values.get(2) < 0 ? values.get(2) & 255 : values.get(2));
                updateFlag = HandlerWhat.IS_LSV_GOT;
                isLSVGot = true;
                Message message = handler.obtainMessage();
                message.arg1 = codes;
                message.what = HandlerWhat.IS_LSV_GOT;
                handler.sendMessageDelayed(message, 50);
            } else if (values.get(1) == (byte) 0x82
//                        && values[7] == 8//说明文档不存在，实际数据存在
                    ) {  //得到硬件版本号
                int codes = (values.get(3) < 0 ? values.get(3) & 255 : values.get(3)) * 255 +
                        (values.get(2) < 0 ? values.get(2) & 255 : values.get(2));
                updateFlag = HandlerWhat.IS_LHV_GOT;
                isLHVGot = true;
                Message message = handler.obtainMessage();
                message.arg1 = codes;
                message.what = HandlerWhat.IS_LHV_GOT;
                handler.sendMessageDelayed(message, 50);

            } else if (values.get(1) == 1
//                        &&values[7]==8//说明文档不存在，实际数据存在
                    ) {//数据正常通讯状态

                isTransOK = true;
                if (!isLocalChecking) {
                    updateFlag = HandlerWhat.IS_UPDAT_READY;
                    handler.sendEmptyMessage(HandlerWhat.IS_UPDAT_READY);
                }
                if (sendCount > 0 && sendCount == fileSize) {
                    sendCount = 0;
                    isStartUpdate = false;
                    handler.sendEmptyMessage(HandlerWhat.IS_UPDAT_OK);
                }
            }
        }

    }

    private boolean isRemoteCheckErr;
    public static String BIKE_UPDATE = "http://115.29.198" +
            ".179/yt-c-platform/caseserver/masterVersionUpdate";

    //检测是否有可用升级文件
    public void fileCheck(final int currentSVC, final int softType, final int currentHVC) {
        rpbMsg.setText("检测是否有可用更新……");
        reCheck.setVisibility(View.GONE);
        OkHttpUtils.get(BIKE_UPDATE)//
                .tag(this)//
                .params("deviceType", SampleGattAttributes.MAINBOARD)//
                .params("hardCode", currentHVC)//
                .params("softCode", currentSVC)//
                .params("softType", softType)//
                .execute(new JsonCallback<BoardRequestInfo>(BoardRequestInfo.class) {
                    @Override
                    public void onSuccess(BoardRequestInfo boardRequestInfo, okhttp3.Call call,
                                          Response response) {
                        //TODO  解析请求返回对象
                        Log.d(TAG, "固件升级检测结果：" + boardRequestInfo.toString()
                                + "\n   request" + response.request().toString()
                        );
                        data = boardRequestInfo.getData();
                        if (boardRequestInfo.getCode() == 10000) {
                            if (data != null) {
                                if (data.getHardCode() == currentHVC
                                        && data.getSoftCode() > currentSVC
                                        && data.getSoftType() == softType) {
                                    threadShowMsg("发现更新程序……", true);
                                    remoteHVC = data.getHardCode();
                                    remoteSVC = data.getSoftCode();
                                    //TODO 测试，版本号不变
//                                    remoteHVC = 1;
//                                    remoteSVC = 1;
                                    fileDownload(data.getDownloadUrl(), data.getMd5());
                                    return;
                                }
                            }
                        } else if (boardRequestInfo.getCode() == 20301 && data != null) {//硬件版本号不合法
                            threadShowMsg("非法硬件类型" + currentHVC, true);

                            sendDataBySerial(
                                    MainBoardCommand.RESET);
                            isStartUpdate = false;
                        } else if (boardRequestInfo.getCode() == 20302 && data != null) {//软件类型不合法
                            threadShowMsg("非法软件类型" + currentSVC, true);

                            sendDataBySerial(
                                    MainBoardCommand.RESET);
                            isStartUpdate = false;
                        } else if (boardRequestInfo.getCode() == 20304 && data != null) {//已经是最新版本
                            threadShowMsg("已是最新版本……", true);

                            if (builder == null)
                                builder = new AlertDialog.Builder
                                        (MainBoardUpdateActivity.this);
                            builder.setTitle("单车固件重置：");
                            builder.setMessage("已是最新版本，若单车不能正常使用可确认重置单车，若单车正常请取消？");
                            builder.setNegativeButton("取消", new DialogInterface
                                    .OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendDataBySerial(
                                            MainBoardCommand.RESET);
                                    isStartUpdate = false;
                                    dialog.dismiss();
                                }
                            });
                            builder.setPositiveButton("确认", new DialogInterface
                                    .OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    remoteHVC = currentHVC;
                                    remoteSVC = currentSVC;
                                    fileDownload(data.getDownloadUrl(), data.getMd5());
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        } else {//
                            Toast.makeText(MainBoardUpdateActivity.this, "无可用更新", Toast
                                    .LENGTH_SHORT).show();
                            threadShowMsg("无可用更新", true);
                            sendDataBySerial(MainBoardCommand.RESET);
                            isStartUpdate = false;

                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        //TODO  请求失败
                        threadShowMsg("检测更新失败，点击重试", false);
                        isRemoteCheckErr = true;
                        reCheck.setVisibility(View.VISIBLE);
                    }
                });
    }


    private void threadShowMsg(@NonNull final String msg, final boolean isResetHide) {
        rpbMsg.post(new Runnable() {
            @Override
            public void run() {
                rpbMsg.setText(msg);
                if (isResetHide) {
                    btn_reset_mainboard.setVisibility(View.GONE);
                } else {
                    btn_reset_mainboard.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    ;

    String dataFile;

    public void fileDownload(String url, String md5) {
        dataFile = "update.bin";
        OkHttpUtils.get(url)//
                .tag(this)//
                .execute(new FileCallback(dataFile) {
                    @Override
                    public void onBefore(BaseRequest request) {
                        //("正在下载中");
                        rpbMsg.setText("准备下载程序……");
                        Log.d(TAG, "准备下载程序");

                    }

                    @Override
                    public void onSuccess(File file, okhttp3.Call call, Response response) {
                        // ("下载完成");
                        rpbMsg.setText("下载程序成功……");
                        Log.d(TAG, "下载程序成功");
                        //TODO 发送数据
                        dataFile = file.getAbsolutePath();
                        new Thread(runable).start();
                    }

                    @Override
                    public void downloadProgress(long currentSize, long totalSize, float
                            progress, long networkSpeed) {
                        Log.d(TAG, "正在下载程序-- " + totalSize + "  " +
                                currentSize + "  " + progress + "  " + networkSpeed);
                        rpbMsg.setText("正在下载程序……" + (Math.floor(progress * 100)) + "%");
                    }

                    @Override
                    public void onError(okhttp3.Call call, @Nullable Response response, @Nullable
                            Exception e) {
                        super.onError(call, response, e);
                        rpbMsg.setText("下载出错……");
                    }
                });
    }

    boolean isRunableRunning, isRunableWaiting;
    long startTime;
    int sendCount;
    int fileSize;
    private boolean isFileSetOk;
    private boolean isHVCSetting;
    private boolean isSVCSetting;
    Runnable runable = new Runnable() {
        @Override
        public void run() {
            {
                isRunableRunning = true;
                InputStream input = null;
                BufferedInputStream bis = null;
                try {
                    sendCount = 0;
                    fileSize = 0;
                    input = new FileInputStream(dataFile);
//                    String[] types = {"update/a.bin", "update/b.bin"};
//                    input = getResources().getAssets().open(types[softType]);

                    bis = new BufferedInputStream(input);
                    if (bis != null) {
                        startTime = System.currentTimeMillis();
                        fileSize = bis.available() / 2 + bis.available() % 2;
                        {

                            Log.d(TAG, "目标版本：remoteHVC=" + remoteHVC + "  remoteSVC=" +
                                    remoteSVC + "  softType" + softType);
                            isHVCSetting = true;
                            sendDataBySerial(MainBoardCommand
                                    .makeHardVersion(remoteHVC));
                            //TODO  等待设置硬件版本号成功唤醒
                            synchronized (MainBoardUpdateActivity.class) {
                                isRunableWaiting = true;
                                MainBoardUpdateActivity.class.wait();
                            }

                            isSVCSetting = true;
                            sendDataBySerial(MainBoardCommand
                                    .makeSoftVersion(remoteSVC));
                            //TODO  等待设置软件版本号成功唤醒
                            synchronized (MainBoardUpdateActivity.class) {
                                isRunableWaiting = true;
                                MainBoardUpdateActivity.class.wait();
                            }

                            isFileSetting = true;
                            //设置发送次数，当返回
                            handler.sendEmptyMessage(HandlerWhat.IS_FILE_SETTING);
                            synchronized (MainBoardUpdateActivity.class) {
                                isRunableWaiting = true;
                                MainBoardUpdateActivity.class.wait();
                            }

                            sendDataBySerial(MainBoardCommand
                                    .makeFileLength(fileSize));
                            //TODO  等待设置次数成功唤醒
                            synchronized (MainBoardUpdateActivity.class) {
                                isRunableWaiting = true;
                                MainBoardUpdateActivity.class.wait();
                            }
                        }

                        byte[] tempbytes = new byte[2];
                        int byteread = 0;
                        while (isRunableRunning && (byteread = bis.read(tempbytes)) != -1) {
                            if (byteread == 1) {
                                sendDataBySerial(MainBoardCommand.makeWriteData
                                        ((byte) (sendCount / 256), (byte) (sendCount % 256),
                                                tempbytes[0], (byte) 0));
                                Log.d(TAG, byteread + " 发送数据  data=" + (byte) (sendCount / 256) +
                                        +(byte) (sendCount % 256) + tempbytes[0] + 0);
                            } else {
                                sendDataBySerial(MainBoardCommand.makeWriteData(
                                        (byte) (sendCount / 256), (byte) (sendCount % 256),
                                        tempbytes[0], tempbytes[1]));
                                Log.d(TAG, byteread + "发送数据  data=" + (byte) (sendCount / 256) +
                                        "  " + (byte) (sendCount % 256) + "  " + tempbytes[0] + "" +
                                        "  " + tempbytes[1]);
                            }
                            //TODO  等待
                            synchronized (MainBoardUpdateActivity.class) {
                                isRunableWaiting = true;
                                MainBoardUpdateActivity.class.wait();
                            }
                            Thread.sleep(50);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bis != null)
                            bis.close();
                        if (input != null)
                            input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isRunableRunning = false;
            }
        }
    };


}
