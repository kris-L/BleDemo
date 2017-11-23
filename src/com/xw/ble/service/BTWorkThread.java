package com.xw.ble.service;

import java.util.ArrayList; 
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.xw.ble.RecvDataListener;
import com.xw.ble.utils.Hex2ByteUtil;
import com.xw.ble.utils.MsgHandlerTools;
import com.xw.ble.utils.MyQueue;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi") public class BTWorkThread extends Thread {

	private static final String TAG = "WorkThread";
	private static Looper mLooper = null;
	public static Handler targetHandler = null;
	// private static boolean threadInitOK = false;
	private static boolean isConnecting = false;
	public boolean isConnect = false;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;
	public final static String ACTION_GATT_CONNECTED = "com.hc_ble.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.hc_ble.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.hc_ble.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.hc_ble.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.hc_ble.bluetooth.le.EXTRA_DATA";

	public final static String UUID_SERVICE_MAIN = "0003cdd0-0000-1000-8000-00805f9b0131";
	public final static String UUID_DATA_READ = "0003cdd1-0000-1000-8000-00805f9b0131";
	public final static String UUID_DATA_WRITE = "0003cdd2-0000-1000-8000-00805f9b0131";
	private static BluetoothGattCharacteristic character_read = null;
	private static BluetoothGattCharacteristic character_write = null;
	private static BluetoothGattCharacteristic character_notify = null;

	private Context mContext;
	private BluetoothManager mBluetoothManager;
	public BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	public static BluetoothGatt mBluetoothGatt;
	private BluetoothGattService bluetoothGattService;

	private Boolean isSet = false;
	private int mConnectionState = STATE_DISCONNECTED;
	private long lastTime = 0;
	private String bufferData = "";
	private boolean isLastData = false;
	private int length = 0;
	public static boolean isnotify = false;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	// 定义
	public static boolean isFirst = true;
	public RecvDataListener mRecvDataListener;
	public MyQueue myQueue;

	public BTWorkThread(Context mContext, Handler handler) {
		// threadInitOK = false;
		this.mContext = mContext;
		targetHandler = handler;

		initialize();
		registerBroadcast();
	}

	public BTWorkThread(Context mContext, Handler handler,
			RecvDataListener mRecvDataListener) {
		// threadInitOK = false;
		this.mContext = mContext;
		targetHandler = handler;
		this.mRecvDataListener = mRecvDataListener;

		initialize();
		registerBroadcast();
	}

	@Override
	public void start() {
		super.start();
		// while (!threadInitOK)
		// ;
	}

	/* service 中蓝牙初始化 */
	public boolean initialize() {
		if (mBluetoothManager == null) { // 获取系统的蓝牙管理器
			mBluetoothManager = (BluetoothManager) mContext
					.getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}
		Log.e(TAG, "bluetooth initialize successful.");

		myQueue = new MyQueue();
		myQueue.setmRecvDataListener(mRecvDataListener);
		return true;
	}

	// 连接远程蓝牙
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.e(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.e(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			isSet = false;
			if (mBluetoothGatt.connect())// 连接蓝牙，其实就是调用BluetoothGatt的连接方法
			{
				mConnectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}
		/* 获取远端的蓝牙设备 */
		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.e(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		/* 调用device中的connectGatt连接到远程设备 */
		mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
		Log.e(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		isSet = true;
		// Boolean flg = mBluetoothGatt.requestMtu(40);
		// final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
		// intent.putExtra(EXTRA_DATA,
		// "mBluetoothGatt.requestMtu(38) is success?:"+flg);
		// sendBroadcast(intent);
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	/*
	 * 取消连接
	 */
	/**
	 * @Title: disconnect
	 * @Description: TODO(取消蓝牙连接)
	 * @param 无
	 * @return void
	 * @throws
	 */
	public void disconnect() {
		Log.e(TAG, "BluetoothGatt disconnect");
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.d(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	/**
	 * @Title: close
	 * @Description: TODO(关闭所有蓝牙连接)
	 * @param 无
	 * @return void
	 * @throws
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * @Title: readCharacteristic
	 * @Description: TODO(读取特征值)
	 * @param @param characteristic（要读的特征值）
	 * @return void 返回类型
	 * @throws
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.e("hyw", "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	// 写入特征值
	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.e(TAG, "BluetoothAdapter not initialized");
			return;
		}
		Log.e(TAG,
				"write data:"
						+ Hex2ByteUtil.bytesToHexString(characteristic
								.getValue()));
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * @Title: setCharacteristicNotification
	 * @Description: TODO(设置特征值通变化通知)
	 * @param @param characteristic（特征值）
	 * @param @param enabled （使能）
	 * @return void
	 * @throws
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.e("hyw", "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		BluetoothGattDescriptor clientConfig = characteristic
				.getDescriptor(UUID
						.fromString("00002902-0000-1000-8000-00805f9b34fb"));

		if (enabled) {
			clientConfig
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		} else {
			clientConfig
					.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		}
		mBluetoothGatt.writeDescriptor(clientConfig);
	}

	/**
	 * @Title: getSupportedGattServices
	 * @Description: TODO(得到蓝牙的所有服务)
	 * @param @return 无
	 * @return List<BluetoothGattService>
	 * @throws
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;
		return mBluetoothGatt.getServices();
	}

	/* 连接远程设备的回调函数 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			isnotify = false;
			isFirst = true;	
			if (newState == BluetoothProfile.STATE_CONNECTED)// 连接成功
			{
				Log.e(TAG, "Connected to GATT server,status:" + status
						+ "  newState:" + newState);
				try {
					isSet = false;
					mBluetoothGatt.discoverServices();
					intentAction = ACTION_GATT_CONNECTED;
					mConnectionState = STATE_CONNECTED;
					isConnect = true;
					/* 通过广播更新连接状态 */
					broadcastUpdate(intentAction);

				} catch (Exception e) {
					// TODO: handle exception
					Log.e(TAG,
							" mBluetoothGatt.requestMtu(41) error:"
									+ e.getMessage());
				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED)// 连接失败
			{
				// mBluetoothGatt.requestMtu(20);
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				isConnect = false;
				disconnect();
				Log.e(TAG, "STATE_DISCONNECTED,status:" + status
						+ "  newState:" + newState);
				broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
			super.onMtuChanged(gatt, mtu, status);

			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.e(TAG, "onMtuChanged:" + mtu + "  status:" + status);
			} else {
				Log.e(TAG, "mtu:" + mtu + "   status:" + status);
			}
			mBluetoothGatt.discoverServices();
			final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
			// 从特征值获取数据
			intent.putExtra(EXTRA_DATA, "onMtuChanged:" + "mtu:" + mtu
					+ "  status:" + status);
			mContext.sendBroadcast(intent);
		}

		/*
		 * 重写onServicesDiscovered，发现蓝牙服务
		 */
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS)// 发现到服务
			{
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
				Log.d(TAG, "onServicesDiscoveredSuccess:发现gatt服务" + status);
			} else {
				Log.d(TAG, "onServicesDiscoveredFail:" + status);
			}
		}

		/*
		 * 特征值的读写
		 */
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// 将数据通过广播到Ble_Activity
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
				Log.e(TAG, "onCharacteristicRead:  ,status:" + status);
			}
		}

		/*
		 * 特征值的改变
		 */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			Log.e("hyw", "onCharacteristicChanged:");
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}
	};

	// 广播意图
	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		mContext.sendBroadcast(intent);
	}

	/* 广播远程发送过来的数据 */
	public void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		// 从特征值获取数据
		final byte[] sourceData = characteristic.getValue();
		if (myQueue != null && sourceData != null) {
			myQueue.RecvData(sourceData);
		}
		String data = Hex2ByteUtil.bytesToHexString(characteristic.getValue());
		final Intent intent = new Intent(action);
		if (data == null) {
			return;
		}
		intent.putExtra(EXTRA_DATA, data);
		if (!"41542B030110010131470D0A".equals(data)) {
			Log.e(TAG, "收到数据:" + data);
		}

		mContext.sendBroadcast(intent);
		// mRecvDataListener.setData(data);

	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_GATT_CONNECTED);
		filter.addAction(ACTION_GATT_DISCONNECTED);
		filter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
		filter.addAction(ACTION_DATA_AVAILABLE);
		mContext.registerReceiver(mGattUpdateReceiver, filter);
	}

	public void unregisterBroadcast() {
		mContext.unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	public void run() {
		Looper.prepare();
		mLooper = Looper.myLooper();
		if (null == mLooper)
			Log.v(TAG, "mLooper is null pointer");
		else
			Log.v(TAG, "mLooper is valid");

		// threadInitOK = true;
		Looper.loop();
	}

	public void quit() {
		try {
			if (null != mLooper) {
				mLooper.quit();
				mLooper = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isConnecting() {
		return isConnecting;
	}

	public boolean isConnect() {
		return isConnect;
	}

	public void setConnect(boolean isConnect) {
		this.isConnect = isConnect;
	}

	public int getConnectionState() {
		return mConnectionState;
	}

	public void writeData(byte[] data) {
		if (character_write == null) {
			Toast.makeText(mContext, "无相应服务，不能进行写操作", Toast.LENGTH_SHORT)
					.show();
			if (mBluetoothGatt != null) {
				BluetoothGattService service = mBluetoothGatt
						.getService(UUID.fromString(UUID_SERVICE_MAIN));
				character_read = service.getCharacteristic(UUID
						.fromString(UUID_DATA_READ));
				character_notify = service.getCharacteristic(UUID
						.fromString(UUID_DATA_READ));
				readCharacteristic(character_read);
				setCharacteristicNotification(character_notify, true);
				Log.e("hyw", "再次初始化相关服务");
				Toast.makeText(mContext, "服务已重新打开，请重试", Toast.LENGTH_SHORT)
						.show();
			} else {
				disconnect();
				close();
				Log.e("hyw", "蓝牙初始化失败，已关闭所有连接");
				Toast.makeText(mContext, "蓝牙初始化失败，已关闭所有连接", Toast.LENGTH_SHORT)
						.show();
			}
			return;
		}

		if (data.length > 20) {
			List<byte[]> dataList = MsgHandlerTools.cutPackage(data);
			for (int i = 0; i < dataList.size(); i++) {
				character_write.setValue(dataList.get(i));
				writeCharacteristic(character_write);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return;
		} else {
			try {
				Log.e("messon", "数据包：" + Hex2ByteUtil.bytesToHexString(data));
				character_write.setValue(data);
				writeCharacteristic(character_write);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

	/**
	 * 合并数组
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	/**
	 * @Title: displayGattServices
	 * @Description: TODO(处理蓝牙服务)
	 * @param 无
	 * @return void
	 * @throws
	 */
	@SuppressLint("NewApi")
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		Log.d(TAG, "displayGattServices gattServices.size:" + gattServices.size());
		if (gattServices == null)
		{
			Log.e(TAG, "gattServices==null");
			return;
		}
			

		// 部分层次，所有特征值集合
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		int j = 0;
		for (BluetoothGattService gattService : gattServices) {
			j++;
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			// 对于当前循环所指向的服务中的每一个特征值
			int i = 0;
			
			Log.d(TAG, "service_uuid:" + gattService.getUuid());
			if (gattService.getUuid().equals(UUID_SERVICE_MAIN)) {
				bluetoothGattService = gattService;

			}
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				i++;
				Log.d(TAG, "UUID:" + gattCharacteristic.getUuid().toString());
				for (BluetoothGattDescriptor gsc : gattCharacteristic
						.getDescriptors()) {
					Log.d(TAG, "getDescriptors:" + gsc.getUuid().toString()
							+ "  per" + gsc.getPermissions());
				}
				if (gattCharacteristic.getUuid().toString()
						.equals(UUID_DATA_READ)) {
					// 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
					if (gattCharacteristic != null) {
						if (!isnotify) {
							character_read = gattCharacteristic;
							readCharacteristic(character_read);
							character_notify = gattCharacteristic;
							setCharacteristicNotification(character_notify,
									true);
							isnotify = true;
						}
					}
					for (BluetoothGattDescriptor gsc : gattCharacteristic
							.getDescriptors()) {
						Log.e(TAG, j + "DescriptorUuid:" + i + "   "
								+ gsc.getUuid().toString());
					}
				}
				if (gattCharacteristic.getUuid().toString()
						.equals(UUID_DATA_WRITE)) {
					character_write = gattCharacteristic;
				}
			}
		}
	}

	private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (ACTION_GATT_CONNECTED.equals(action)) {

			} else if (ACTION_GATT_SERVICES_DISCOVERED// 发现GATT服务器
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				// 获取设备的所有蓝牙服务
				displayGattServices(getSupportedGattServices());

			}
		}
	};
}
