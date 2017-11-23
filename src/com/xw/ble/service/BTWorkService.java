package com.xw.ble.service;

import java.io.UnsupportedEncodingException; 
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xw.ble.Const;
import com.xw.ble.RecvDataListener;
import com.xw.ble.utils.Hex2ByteUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * 观察者模式
 * 
 * @author Administrator
 * 
 */
public class BTWorkService extends Service implements RecvDataListener {

	private static final String TAG = "BTWorkService";
	// Service和workThread通信用mHandler
	public static BTWorkThread workThread = null;
	private static Handler mHandler = null;
	private static List<Handler> targetsHandler = new ArrayList<Handler>(5);

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Log.e(TAG, "onCreate");
		mHandler = new MHandler(this);
		workThread = new BTWorkThread(this, mHandler, this);
		workThread.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "onStartCommand");
		Message msg = Message.obtain();
		msg.what = Const.MSG_ALLTHREAD_READY;
		notifyHandlers(msg);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		workThread.unregisterBroadcast();
		workThread.disconnect();
		workThread.close();
		workThread.quit();
		workThread = null;
		Log.v("DrawerService", "onDestroy");
	}

	static class MHandler extends Handler {

		WeakReference<BTWorkService> mService;

		MHandler(BTWorkService service) {
			mService = new WeakReference<BTWorkService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			notifyHandlers(msg);
		}
	}

	/**
	 * 
	 * @param handler
	 */
	public static void addHandler(Handler handler) {
		if (!targetsHandler.contains(handler)) {
			targetsHandler.add(handler);
		}
	}

	/**
	 * 
	 * @param handler
	 */
	public static void delHandler(Handler handler) {
		if (targetsHandler.contains(handler)) {
			targetsHandler.remove(handler);
		}
	}

	/**
	 * 
	 * @param msg
	 */
	public static void notifyHandlers(Message msg) {
		for (int i = 0; i < targetsHandler.size(); i++) {
			Message message = Message.obtain(msg);
			targetsHandler.get(i).sendMessage(message);
		}
	}

	@Override
	public void setData(String hexStr) {
		if (!"41542B030110010131470D0A".equals(hexStr)) {
			Log.d(TAG, "setData="+hexStr);
		}
		// TODO Auto-generated method stub
		// hexStr 格式：AT+, Source, destination, command, parameter number,parameter, data number, data, checksum,\r\n
		// 具体参考 ：蓝牙通信协议



	}

	/**
	 * @Title getByteArrayOnlyData
	 * @Description 返回数据，不返回时间(数据为gbk转码)
	 * @author jw
	 * @date 2015-12-1 上午9:27:40
	 * @param bData
	 * @return
	 */
	private String getByteArrayOnlyData(byte[] bData) {
		String sData = "";

		int iCount = getCountD2(bData);
		if (iCount >= 5) {
			byte[] bOnlyData = new byte[bData.length - 11];
			System.arraycopy(bData, 11, bOnlyData, 0, bData.length - 11);
			try {
				sData = new String(bOnlyData, "GBK");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			try {
				sData = new String(bData, "GBK");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return sData;
	}

	/**
	 * @Title getByteArrayHexOnlyData
	 * @Description 返回数据，不返回时间(数据为16进制字符串)
	 * @author jw
	 * @date 2015-12-2 上午9:44:36
	 * @param bData
	 * @return
	 */
	private String getByteArrayHexOnlyData(byte[] bData) {
		String sData = "";

		int iCount = getCountD2(bData);
		if (iCount >= 5) {
			byte[] bOnlyData = new byte[bData.length - 11];
			System.arraycopy(bData, 11, bOnlyData, 0, bData.length - 11);
			sData = Hex2ByteUtil.bytesToHexString(bOnlyData);

		} else {
			sData = Hex2ByteUtil.bytesToHexString(bData);
		}

		return sData;
	}

	private int getCountD2(byte[] bData) {
		int i = 0;
		String str = Hex2ByteUtil.bytesToHexString(bData);
		Pattern p = Pattern.compile("(\\w{2})");
		Matcher m = p.matcher(str);
		while (m.find()) {
			if (m.group().equals("2D")) {
				i = i + 1;
			}
		}
		return i;
	}

}
