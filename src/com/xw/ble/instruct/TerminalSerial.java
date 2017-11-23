package com.xw.ble.instruct;

import com.xw.ble.utils.BtSendCommandUtils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TerminalSerial {

	private static Context mContext;
	private static Intent serialServiceIntent;
	private static final String TAG = TerminalSerial.class.getSimpleName();
	public static boolean bCV2200Test = false; // 工厂模式
	public static boolean bMCUUpdate = false; // mcu升级
	// public static PeripheralInfo mPeripheralInfo;

	public static SendPasswordListener mSendPasswordListener;
	
	
	

	/**
	 * @Title init 初始化，启动后台服务
	 * @Description
	 * @author lsz
	 * @date 2017-11-23 下午5:15:04
	 * @param context
	 *            上行文
	 * @return
	 */
	public static String init(Context context) {
		Log.v(TAG, "Terminal init");
		// XLog.TEST_MODE = true;//关闭输出日志
		try {
			bCV2200Test = false;

			mContext = context;
			// serialServiceIntent = new Intent();
			// serialServiceIntent.setAction(Constant.SerialService);
			// serialServiceIntent.setPackage(context.getPackageName());
			// context.startService(serialServiceIntent);
			//
			// mPeripheralInfo=new PeripheralInfo();
		} catch (Exception e) {
			return "err:" + e.getMessage();
		}
		return "";
	}

	public static void stop() {
		try {
			if (serialServiceIntent != null && mContext != null) {
				mContext.stopService(serialServiceIntent);
			}
		} catch (Exception e) {
		}
	}

	public static void setInstuction(BtInstructionInfo info, Object mListener) {
		if (bCV2200Test) {
			// 工厂模式程序打开，不发送指令
			return;
		}
		BtInstructionType mInstructionType = info.getMsgType();
		switch (mInstructionType) {
		case Control_SendPassword:
			TerminalSerial.mSendPasswordListener = (SendPasswordListener) mListener;
			Log.e(TAG, "info.getInputData()="+info.getInputData());
			BtSendCommandUtils.sendPassword(info.getInputData().toString());
			break;
		}
	}

}
