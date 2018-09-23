package com.arcsoft.sdk_demo.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.arcsoft.sdk_demo.Application;

public class ServerManager extends BroadcastReceiver {

    private static final String ACTION = "com.arcsoft.sdk_demo.server.receiver";

    private static final String CMD_KEY = "CMD_KEY";
    private static final String MESSAGE_KEY = "MESSAGE_KEY";

    private static final int CMD_VALUE_START = 1;
    private static final int CMD_VALUE_ERROR = 2;
    private static final int CMD_VALUE_STOP = 4;

    public static String serverIp;

    /**
     * Notify serverStart.
     *
     * @param context context.
     */
    public static void serverStart(Context context, String hostAddress) {
        sendBroadcast(context, CMD_VALUE_START, hostAddress);
    }

    /**
     * Notify serverStop.
     *
     * @param context context.
     */
    public static void serverError(Context context, String error) {
        sendBroadcast(context, CMD_VALUE_ERROR, error);
    }

    /**
     * Notify serverStop.
     *
     * @param context context.
     */
    public static void serverStop(Context context) {
        sendBroadcast(context, CMD_VALUE_STOP);
    }

    private static void sendBroadcast(Context context, int cmd) {
        sendBroadcast(context, cmd, null);
    }

    private static void sendBroadcast(Context context, int cmd, String message) {
        Intent broadcast = new Intent(ACTION);
        broadcast.putExtra(CMD_KEY, cmd);
        broadcast.putExtra(MESSAGE_KEY, message);
        context.sendBroadcast(broadcast);
    }

    private Intent mService;

    public ServerManager() {
        mService = new Intent(Application.getInstance(), CoreService.class);
    }

    /**
     * Register broadcast.
     */
    public void register() {
        IntentFilter filter = new IntentFilter(ACTION);
        Application.getInstance().registerReceiver(this, filter);
    }

    public void startService() {
        Application.getInstance().startService(mService);
    }

    public void stopService() {
        Application.getInstance().stopService(mService);
    }

    /**
     * UnRegister broadcast.
     */
    public void unRegister() {
        Application.getInstance().unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION.equals(action)) {
            int cmd = intent.getIntExtra(CMD_KEY, 0);
            switch (cmd) {
                case CMD_VALUE_START: {
                    String ip = intent.getStringExtra(MESSAGE_KEY);
                    Log.d("DDDD","----server ip:"+ip);
                    serverIp = ip;
                    break;
                }
                case CMD_VALUE_ERROR: {
                    String error = intent.getStringExtra(MESSAGE_KEY);
                    Log.d("DDDD","----server error:"+error);
                    break;
                }
                case CMD_VALUE_STOP: {
                    Log.d("DDDD","----server stop");
                    break;
                }
            }
        }
    }

}
