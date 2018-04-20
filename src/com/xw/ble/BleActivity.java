package com.xw.ble;

import java.util.ArrayList;

import com.xw.ble.service.BTWorkService;
import com.xw.ble.utils.Hex2ByteUtil;
import com.xw.bledemo.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi") 
public class BleActivity extends Activity implements OnClickListener {
	private final static String TAG = BleActivity.class.getSimpleName();
	// 扫描蓝牙按钮
	private Button scan_btn, btn_sure,service_btn;
	// 蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothLeScanner scanner;
	// 蓝牙信号强度
	private ArrayList<Integer> rssis;
	// 自定义Adapter
	LeDeviceListAdapter mleDeviceListAdapter;
	// listview显示扫描到的蓝牙信息
	ListView lv;
	// 描述扫描蓝牙的状态
	private boolean mScanning;
	private boolean scan_flag;
	private Handler mHandler;
	int REQUEST_ENABLE_BT = 1;
	// 蓝牙扫描时间
	private static final long SCAN_PERIOD = 10000;

	public static SharedPreferences sp = null;
	// 创建编辑器editor,通过编辑器把数据存放到sp中
	public static Editor editor = null;

	// 蓝牙名字
	private String mDeviceName;
	// 蓝牙地址
	private String mDeviceAddress;
	public static Boolean isFirst = true;

	/**
	 * 
	 * 蓝牙扫描回调函数 实现扫描蓝牙设备，回调蓝牙BluetoothDevice，可以获取name MAC等信息
	 * 
	 * **/
	private BluetoothAdapter.LeScanCallback mLeScanCallback;

	
	private EditText et_id;

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (BTWorkService.workThread != null) {
			stopBleService();
		}
		stopBleService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ble);
		isFirst = true;
		sp = this.getSharedPreferences("soft", Context.MODE_PRIVATE);
		editor = sp.edit();
		mDeviceName = sp.getString("mDeviceName", "");
		mDeviceAddress = sp.getString("mDeviceAddress", "");
		// 初始化控件
		initWidgets();
		// 初始化蓝牙
		init_ble();
		/* 启动蓝牙service */
		startBleService();
		scan_flag = true;
		// startLogServiceService();
		mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi,
					byte[] scanRecord) {
				// TODO Auto-generated method stub

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						/* 讲扫描到设备的信息输出到listview的适配器 */
						mleDeviceListAdapter.addDevice(device, rssi);
						mleDeviceListAdapter.notifyDataSetChanged();
					}
				});
				if (device.getAddress().equals(mDeviceAddress)) {
//					Intent intent = new Intent(BleActivity.this,
//							BluetoothTest.class);
//					intent.putExtra(Ble_Activity.EXTRAS_DEVICE_NAME,
//							device.getName());
//					intent.putExtra(Ble_Activity.EXTRAS_DEVICE_ADDRESS,
//							mDeviceAddress);
//					scanLeDevice(false);
//					startActivity(intent);
				}

			}
		};
		
		
		// 自定义适配器
		mleDeviceListAdapter = new LeDeviceListAdapter();
		// 为listview指定适配器
		lv.setAdapter(mleDeviceListAdapter);

		/* listview点击函数 */
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				final BluetoothDevice device = mleDeviceListAdapter
						.getDevice(position);
				if (device == null)
					return;
				mDeviceName = device.getName();

				mDeviceAddress = device.getAddress();
				editor.putString("mDeviceName", mDeviceName);
				editor.putString("mDeviceAddress", mDeviceAddress);
				editor.commit();
				final Intent intent = new Intent(BleActivity.this,
						BluetoothTest.class);
				intent.putExtra(BluetoothTest.EXTRAS_DEVICE_NAME,
						device.getName());
				intent.putExtra(BluetoothTest.EXTRAS_DEVICE_ADDRESS,
						device.getAddress());
				intent.putExtra(BluetoothTest.EXTRAS_DEVICE_RSSI,
						rssis.get(position).toString());
				if (mScanning) {
					/* 停止扫描设备 */
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					mScanning = false;
				}

				try {
					// 启动Ble_Activity
					startActivity(intent);
					// finish();
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: handle exception
				}

			}
		});
		Log.e("test", "mDeviceName:" + mDeviceName + "mDeviceAddress:"
				+ mDeviceAddress);
		mleDeviceListAdapter = new LeDeviceListAdapter();
		lv.setAdapter(mleDeviceListAdapter);
		scanLeDevice(true);
		Log.e("hyw1", "oncreate");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mDeviceAddress = sp.getString("mDeviceAddress", "");
		Log.e("hyw1", "onresume");
	}

	private void startBleService() {
		Log.e(TAG, "startBleService");
		Intent intent = new Intent(this, BTWorkService.class);
		startService(intent);
	}

	private void stopBleService() {
		Log.e(TAG, "stopBleService");
		Intent intent = new Intent(this, BTWorkService.class);
		stopService(intent);
	}

	/**
	 * @Title: init
	 * @Description: TODO(初始化UI控件)
	 * @param 无
	 * @return void
	 * @throws
	 */
	private void initWidgets() {
		scan_btn = (Button) this.findViewById(R.id.scan_dev_btn);
		service_btn = (Button) findViewById(R.id.service_btn);
		scan_btn.setOnClickListener(this);
		service_btn.setOnClickListener(this);
		lv = (ListView) this.findViewById(R.id.lv);
		mHandler = new Handler();
		et_id = (EditText) findViewById(R.id.et_id);
		btn_sure = (Button) findViewById(R.id.btn_sure);
		btn_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String id = et_id.getText().toString();
				if (id.length() != 16) {
					Toast.makeText(getApplicationContext(), "主机id格式错误",
							Toast.LENGTH_SHORT).show();
				} else {
					byte[] iid = Hex2ByteUtil.hexStringToBytes(id);
					if (iid.length != 8) {
						Toast.makeText(getApplicationContext(), "主机id格式错误",
								Toast.LENGTH_SHORT).show();
						return;
					}
					Toast.makeText(getApplicationContext(), "主机id设置成功",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	/**
	 * 
	 * @Title init_ble
	 * @Description 初始化蓝牙
	 * @author 何勇文
	 * @date 2016-4-22 上午10:35:53
	 */
	private void init_ble() {
		// 判断手机硬件是否支持蓝牙
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
			finish();
		}
		// 获取手机本地的蓝牙适配器
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		scanner = mBluetoothAdapter.getBluetoothLeScanner();
		
		// 打开蓝牙权限
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

	}

	/*
	 * 按钮响应事件
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        switch(v.getId()){
        case R.id.scan_dev_btn:
        	if (scan_flag) {
    			mleDeviceListAdapter = new LeDeviceListAdapter();
    			lv.setAdapter(mleDeviceListAdapter);
    			scanLeDevice(true);
    		} else {
    			scanLeDevice(false);
    			scan_btn.setText("扫描设备");
    		}
        	break;
        	
        case R.id.service_btn:
			Intent intent = new Intent(this,BleServiceActivity.class);
		    startActivity(intent);
        	break;
        }
		
	}

	/**
	 * @Title: scanLeDevice
	 * @Description: TODO(扫描蓝牙设备 )
	 * @param enable
	 *            (扫描使能，true:扫描开始,false:扫描停止)
	 * @return void
	 * @throws
	 */
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					scan_flag = true;
					scan_btn.setText("扫描设备");
					Log.e("hyw", "SCAN stop.....................");
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);
			/* 开始扫描蓝牙设备，带mLeScanCallback 回调函数 */
			Log.e("hyw", "SCAN begin.....................");
			mScanning = true;
			scan_flag = false;
			scan_btn.setText("停止扫描");
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//				scanner.startScan(mScanCallback);
//		    } else {
////		    	mBluetoothAdapter.startLeScan(mLeScanCallback);
//		    }
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			Log.e("hyw", "stoping... scan.............");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			scan_flag = true;
		}
	}

	
	/**
	 * 5.0后的ble搜索回调
	 */
	private ScanCallback mScanCallback = new ScanCallback(){
		@Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BluetoothDevice device = result.getDevice();
                /* 讲扫描到设备的信息输出到listview的适配器 */
				mleDeviceListAdapter.addDevice(device, result.getRssi());
				mleDeviceListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG,"搜索失败");
        }
	};
	
	
	/**
	 * 
	 * @ClassName LeDeviceListAdapter
	 * @Description TODO<自定义适配器Adapter,作为listview的适配器>
	 * @author 何勇文
	 * @date 2016-4-22 上午10:40:04
	 */
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;

		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			rssis = new ArrayList<Integer>();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device, int rssi) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
				rssis.add(rssi);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
			rssis.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		/**
		 * 重写getview
		 * 
		 * **/
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {

			// General ListView optimization code.
			// 加载listview每一项的视图
			view = mInflator.inflate(R.layout.item_device_list, null);
			// 初始化三个textview显示蓝牙信息
			TextView deviceAddress = (TextView) view
					.findViewById(R.id.tv_deviceAddr);
			TextView deviceName = (TextView) view
					.findViewById(R.id.tv_deviceName);
			TextView rssi = (TextView) view.findViewById(R.id.tv_rssi);

			BluetoothDevice device = mLeDevices.get(i);
			deviceAddress.setText(device.getAddress());
			deviceName.setText(device.getName());
			rssi.setText("" + rssis.get(i));

			return view;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			// startActivity(new Intent(this,MainActivity.class));
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
