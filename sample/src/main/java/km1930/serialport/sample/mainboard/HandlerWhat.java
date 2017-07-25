package km1930.serialport.sample.mainboard;

/**
 * Created by djf on 2016/12/6.
 */

public class HandlerWhat {
    public final static int NET_WORK_CONNECTING = 12345678;
    public final static int HANDLER_GET_ICON = 1000;
    public final static int HANDLER_MOVE_ICON = 1001;
    public final static int HANDLER_UPDATE_CURVE = 1002;
    public final static int HANDLER_UPDATE_CURVE_LAST = 1003;

    public final static int SHOW_INVITE_MESSAGE = 1013;
    public final static int JOIN_THE_ROOM = 1014;
    public static final int HANDLER_LAYOUT_OK = 1015;
    //InviteFriendActivity
    public final static int SHOW_FRIEND_LIST = 2000;
    public final static int SHOW_HOST_INFO = 2001;
    public final static int SHOW_INVITE_RESULT = 2012;
    public final static int UPDATE_MULTI_ROOM = 2013;
    //JoinInviteRoomActivity
    public static final int UPDATE_ROOM_INFO = 3000;
    public static final int ROOM_BREAK = 3001;
    public static final int KICKED_ROOM = 3002;

    //com.ut.vrautocycling.activity.MultiConditionSelectActivity
    public static final int CREAT_ROOM = 4000;
    public static final int JOIN_OTHER_ROOM = 4001;


    public static final int REMOVE_VIEW = 5001;
    public static final int FAILED_SHOW_QRCODE_IMAGE1 = 5011;
    public static final int FAILED_SHOW_QRCODE_IMAGE2 = 5012;
    public static final int FAILED_SHOW_QRCODE_IMAGE3 = 5013;
    public static final int LOADING_QRCODE = 5014;

    public static final int INIT_BIKE_LOCATION = 5020;
    public static final int UPDATE_BIKE_LOCATION = 5021;

    public static final int SERVER_RECONNECT_SUCCESS = 5022;
    public static final int SERVER_RECONNECT_FAILED = 5024;

    public static final int COURSE_PLAY_END = 5023;
    public static final int SINGLE_PLAY_END = 5025;
    public static final int COURSE_PLAY_EXIT = 5026;

    public static final int USER_REPEAT_LOGIN = 5027;

    public static final int UPDATE_DIRECTION = 5028;

    public static final int CountDownTimer1 = 5031;
    public static final int CountDownTimer2 = 5032;
    public static final int CountDownTimer3 = 5033;
    public static final int CountDownTimerGo = 5034;

    /*
     1   处于升级状态
     2   硬件版本号正在设置    4   硬件版本号设置成功    8   硬件版本号设置失败
     16  软件版本号正在设置    32  软件版本号设置成功    64  软件版本号设置失败
     128 升级地址正在设置   256 升级地址设置成功    512 升级地址设置失败
     1024    升级文件长度正在设置    2048    升级文件长度设置成功   4096    升级文件长度设置失败
     8192   等待主控板响应    16384   主控板响应成功信息    32768   主控板响应失败信息
     65536   主控板已就绪，可以开始升级

    * */
    public static final int IS_UPDATING = 1;
    public static final int IS_UPDAT_READY = 65536;
    public static final int IS_LHV_GOT = 131072;
    public static final int IS_LSV_GOT = 262144;
    public static final int IS_UPDAT_OK = 524288;

    public static final int IS_HV_SETTING = 2;
    public static final int IS_HV_SET_OK = 4;
    public static final int IS_HV_SET_ERR = 8;

    public static final int IS_SV_SETTING = 16;
    public static final int IS_SV_SET_OK = 32;
    public static final int IS_SV_SET_ERR = 64;

    public static final int IS_ADDR_SETTING = 128;
    public static final int IS_ADDR_SET_OK = 256;
    public static final int IS_ADDR_SET_ERR = 512;

    public static final int IS_FILE_SETTING = 1024;
    public static final int IS_FILE_SET_OK = 2048;
    public static final int IS_FILE_SET_ERR = 4096;

    public static final int IS_ANS_WAITING = 8192;
    public static final int IS_ANS_OK = 16384;
    public static final int IS_ANS_ERR = 32768;

}
