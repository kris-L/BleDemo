package com.xw.ble;

import java.io.IOException; 
import java.util.ArrayList;
import java.util.UUID;

import com.xw.ble.utils.Hex2ByteUtil;
import com.xw.ble.utils.MyQueue;
import com.xw.bledemo.R;
import com.xw.bluetooth.ChatActivity;
import com.xw.bluetooth.DeviceBean;
import com.xw.bluetooth.DeviceListAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class BleServiceActivity extends Activity {

	private final static String TAG = "BleServiceActivity";
	private static final int STATUS_CONNECT = 0x11;
	private ListView mListView;
	private ArrayList<DeviceBean> mDatas;
	private DeviceListAdapter mDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private EditText mEtMsg;
	private Button mBtnSend;// 发送按钮
	private Button mBtnDisconn;// 断开连接

	private BluetoothGattServer mGattServer;
	private BluetoothLeAdvertiser mLeAdvertiser;
	private BluetoothAdapter mAdapter;
	private BluetoothManager mManager;

	private final String UUID_DATA_SERVICE = "0003cdd0-0000-1000-8000-00805f9b0131";
	private final String UUID_READ = "0003cdd1-0000-1000-8000-00805f9b0131";
	private final String UUID_WRITE = "0003cdd2-0000-1000-8000-00805f9b0131";
	private final static String UUID_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	// --线程类-----------------
	private ServerThread mServerThread;
	public MyQueue myQueue;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		initViews();
		initDatas();
	}
	


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}



	private void initDatas() {
		mDatas = new ArrayList<DeviceBean>();
		mDeviceListAdapter = new DeviceListAdapter(this, mDatas);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mListView.setAdapter(mDeviceListAdapter);
		mListView.setFastScrollEnabled(true);
		
		if(null == mManager) {
			mManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE); 
		}
//        if(false == getPackageManager().  
//            hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))  
        if(null == mAdapter) {
        	mAdapter = mManager.getAdapter(); 
        }
            
        //判断支持不支持BLE Peripheral
        if(false == mAdapter.isMultipleAdvertisementSupported()){
        	Log.e(TAG,"不支持BLE Peripheral");
        }
        
		myQueue = new MyQueue();
		myQueue.setmRecvDataListener(mRecvDataListener);
        
        startAdvertise();
		 
	}

	private RecvDataListener mRecvDataListener = new RecvDataListener() {
		
		@Override
		public void setData(String data) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void initViews() {
		mListView = (ListView) findViewById(R.id.list);
		

		mEtMsg = (EditText) findViewById(R.id.MessageText);
		mEtMsg.clearFocus();

		mBtnSend = (Button) findViewById(R.id.btn_msg_send);
		mBtnDisconn = (Button) findViewById(R.id.btn_disconnect);
	}

	// 开启服务器
	private class ServerThread extends Thread {
		public void run() {
			Log.e(TAG,"启动服务监听");
			startAdvertise();
		}
	};
	
	/**
	 * 信息处理
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String info = (String) msg.obj;
			switch (msg.what) {
			case STATUS_CONNECT:
				Toast.makeText(BleServiceActivity.this, info, 0).show();
				break;
			}
			
			if (msg.what == 1) {
				mDatas.add(new DeviceBean(info, true));
				mDeviceListAdapter.notifyDataSetChanged();
				mListView.setSelection(mDatas.size() - 1);
			}else {
				mDatas.add(new DeviceBean(info, false));
				mDeviceListAdapter.notifyDataSetChanged();
				mListView.setSelection(mDatas.size() - 1);
			}
		}
	};
	
	

	@SuppressLint("NewApi")
	private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {

		@Override
		public void onStartFailure(int errorCode) {
			Log.e(TAG, "onStartFailure");
			Message msg = new Message();
			msg = new Message();
			msg.obj = "服务端已断开";
			msg.what = STATUS_CONNECT;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			Log.e(TAG, "onStartSuccess");
			Message msg = new Message();
			msg = new Message();
			msg.obj = "服务端已监听";
			msg.what = STATUS_CONNECT;
			mHandler.sendMessage(msg);
		};
	};

	@SuppressLint("NewApi")
	private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

		@Override
		public void onConnectionStateChange(BluetoothDevice device, int status,
				int newState) {
//			Log.e(TAG,
//					"Our gatt server connection state changed, new state ");
//			Log.e("GattServer", Integer.toString(newState));
			super.onConnectionStateChange(device, status, newState);
			Log.e(TAG, "Connected to GATT server,status:" + status
					+ "  newState:" + newState);
			if (newState == BluetoothProfile.STATE_CONNECTED)// 连接成功
			{
				try {
					Message msg = new Message();
					msg = new Message();
					msg.obj = "蓝牙:"+device.getAddress()+"已连接";
					msg.what = STATUS_CONNECT;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					// TODO: handle exception
					Log.e(TAG,
							" mBluetoothGatt.requestMtu(41) error:"
									+ e.getMessage());
				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED)// 连接失败
			{
				// mBluetoothGatt.requestMtu(20);
				if(device != null){
					Message msg = new Message();
					msg = new Message();
					msg.obj = "蓝牙:"+device.getAddress()+"已连接";
					msg.what = STATUS_CONNECT;
					mHandler.sendMessage(msg);
				}
			}
		}
		
		
		@Override
		public void onServiceAdded(int status, BluetoothGattService service) {
			Log.d(TAG, "Our gatt server service was added.");
			super.onServiceAdded(status, service);
		}

		@Override
		public void onCharacteristicReadRequest(BluetoothDevice device,
				int requestId, int offset,
				BluetoothGattCharacteristic characteristic) {
			Log.e(TAG, "Our gatt characteristic was read.");
			super.onCharacteristicReadRequest(device, requestId, offset,
					characteristic);
			mGattServer.sendResponse(device, requestId,
					BluetoothGatt.GATT_SUCCESS, offset,
					characteristic.getValue());
			
			broadcastUpdateData(characteristic);
			
		}
		
		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device,
				int requestId, BluetoothGattCharacteristic characteristic,
				boolean preparedWrite, boolean responseNeeded, int offset,
				byte[] value) {
			Log.e(TAG,
					"We have received a write request for one of our hosted characteristics");
			Log.e(TAG, "data = " + value.toString());
			super.onCharacteristicWriteRequest(device, requestId,
					characteristic, preparedWrite, responseNeeded, offset,
					value);
		}

		@Override
		public void onNotificationSent(BluetoothDevice device, int status) {
			Log.e(TAG, "onNotificationSent");
			super.onNotificationSent(device, status);
		}

		@Override
		public void onDescriptorReadRequest(BluetoothDevice device,
				int requestId, int offset, BluetoothGattDescriptor descriptor) {
			Log.e(TAG, "Our gatt server descriptor was read.");
			super.onDescriptorReadRequest(device, requestId, offset, descriptor);

		}

		@Override
		public void onDescriptorWriteRequest(BluetoothDevice device,
				int requestId, BluetoothGattDescriptor descriptor,
				boolean preparedWrite, boolean responseNeeded, int offset,
				byte[] value) {
			Log.e(TAG, "Our gatt server descriptor was written.");
			super.onDescriptorWriteRequest(device, requestId, descriptor,
					preparedWrite, responseNeeded, offset, value);
		}

		@Override
		public void onExecuteWrite(BluetoothDevice device, int requestId,
				boolean execute) {
			Log.e(TAG, "Our gatt server on execute write.");
			super.onExecuteWrite(device, requestId, execute);
		}

	};

	/* 接收的数据 */
	public void broadcastUpdateData(final BluetoothGattCharacteristic characteristic) {
		Log.e(TAG, "broadcastUpdateData=");
		// 从特征值获取数据
		final byte[] sourceData = characteristic.getValue();
		if (myQueue != null && sourceData != null) {
			myQueue.RecvData(sourceData);
		}
		String data = Hex2ByteUtil.bytesToHexString(characteristic.getValue());
		
		Log.e(TAG, "接收数据data="+data);
	}
	
	

	@SuppressLint("NewApi")
	public void startAdvertise() {
		if (null == mAdapter)
			return;

		if (null == mLeAdvertiser) {
			mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
		}

		if (null == mLeAdvertiser)
			return;

		AdvertiseSettings.Builder settingBuilder;

		settingBuilder = new AdvertiseSettings.Builder();
		settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
		settingBuilder.setConnectable(true);
		settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

		AdvertiseData.Builder advBuilder;

		advBuilder = new AdvertiseData.Builder();

		mAdapter.setName("HUD"); // 8 characters works, 9+ fails
		Log.e(TAG, "mAdapter.getAddress()="+mAdapter.getAddress());
		advBuilder.setIncludeDeviceName(true);

		mGattServer = mManager.openGattServer(this, mGattServerCallback);

		// addDeviceInfoService(mGattServer);

		BluetoothGattCharacteristic read2Characteristic = new BluetoothGattCharacteristic(
				UUID.fromString(UUID_READ),
				BluetoothGattCharacteristic.PROPERTY_READ,
				BluetoothGattCharacteristic.PERMISSION_READ);

		BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
				UUID.fromString(UUID_WRITE),
				BluetoothGattCharacteristic.PROPERTY_WRITE,
				BluetoothGattCharacteristic.PERMISSION_WRITE);
	
		BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
				UUID.fromString(UUID_CONFIG),
				BluetoothGattCharacteristic.PROPERTY_NOTIFY);
		read2Characteristic.addDescriptor(descriptor);
		

		BluetoothGattService AService = new BluetoothGattService(
				UUID.fromString(UUID_DATA_SERVICE),
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		
		AService.addCharacteristic(read2Characteristic);
		AService.addCharacteristic(writeCharacteristic);
		
		// Add notify characteristic here !!!
		mGattServer.addService(AService);

		mLeAdvertiser.startAdvertising(settingBuilder.build(),
				advBuilder.build(), mAdvCallback);
		
	}

	@SuppressLint("NewApi")
	public void stopAdvertise() {
		if (null != mLeAdvertiser)
			mLeAdvertiser.stopAdvertising(mAdvCallback);

		mLeAdvertiser = null;
	}

}
