package com.xw.ble;

import java.io.File; 
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.xw.ble.instruct.BtInstructionInfo;
import com.xw.ble.instruct.BtInstructionType;
import com.xw.ble.instruct.SendPasswordListener;
import com.xw.ble.instruct.TerminalSerial;
import com.xw.ble.service.BTWorkService;
import com.xw.ble.service.BTWorkThread;
import com.xw.ble.utils.BtSendCommandUtils;
import com.xw.ble.utils.Hex2ByteUtil;
import com.xw.bledemo.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

@SuppressLint("NewApi")
public class BluetoothTest extends Activity implements OnClickListener {

	private final static String TAG = BluetoothTest.class.getSimpleName();
	// 蓝牙4.0的UUID,其中0000ffe1-0000-1000-8000-00805f9b34fb是08蓝牙模块的UUID

	public final static String UUID_SERVICE_MAIN = "0003cdd0-0000-1000-8000-00805f9b0131";
	public final static String UUID_DATA_READ = "0003cdd1-0000-1000-8000-00805f9b0131";
	public final static String UUID_DATA_WRITE = "0003cdd2-0000-1000-8000-00805f9b0131";

	private BluetoothGattService bluetoothGattService;
	public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";;

	public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static String EXTRAS_DEVICE_RSSI = "RSSI";

	public static int BT_PAIR_RESULT = 1;
	public static int START_CONNECT = 101;
	// 蓝牙配对状态
	private boolean isPair = false;
	// 蓝牙连接状态
	private boolean mConnected = false;
	private String status = "disconnected";
	// 蓝牙名字
	private String mDeviceName;
	// 蓝牙地址
	public static String mDeviceAddress;
	// 蓝牙信号值
	private String mRssi;
	private Bundle b;
	private String rev_str = "";
	// 文本框，显示接受的内容
	private TextView rev_tv, connect_state, tv_clear;
	// 发送按钮
	private Button send_btn;
	// 文本编辑框
	private EditText send_et;
	// 指令编辑框
	private InstructInputView send_instruct_iiv;
	private ScrollView rev_sv;
	private TextView tv_device_address, tv_device_name, tv_heart;
	LinearLayout ll_register, ll_info, ll_bottom;
	public static boolean isnotify = false;
	// 定义
	public static boolean isFirst = true;
	private byte plateform = 1;
	String password;
	String driverInfo = "";
	String carInfo = "";

	Button btn_password, btn_send, btn_notify, switch_btn, btn_clear, test_btn,
			clear_btn;
	EditText et_password, et_car, et_driver;
	private Spinner spinner;
	private int passwordNum = 3;
	public Handler handler;
	private boolean hasLogin;
	Intent gattServiceIntent = null;
	private static Boolean isSendingHeat = false;
	private static Boolean sendHeat = false;
	private static Boolean reConnect = true;
	public static int reConnectNum = 0;
	

	private ToggleButton mTogBtn;
	// 定义
	public boolean isThredRun = true;

	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	// 蓝牙特征值

	private static BluetoothGattCharacteristic character_read = null;
	private static BluetoothGattCharacteristic character_write = null;
	private static BluetoothGattCharacteristic character_notify = null;

	private Animation anim_in_down, anim_out_down;
	static SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd-HH:mm:ss");
	static SimpleDateFormat df1 = new SimpleDateFormat("yyMMddHHmmss");
	static SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String fileName = Environment.getExternalStorageDirectory()
			+ File.separator + "ble";
	String txt = File.separator + df1.format(new Date()) + ".txt";

	File file = new File(fileName);
	File textFile;

	private DrawerLayout mDrawerLayout;
	private ListView mLv;
	private String[] str;
	private TextView tv_time, tv_longitude, tv_latitude, tv_speed,
			tv_direction, tv_satellite_num, tv_tatal_mileage, tv_acc,
			tv_safety_belt, tv_left_turn_signal_lamp,
			tv_right_trun_signal_lamp, tv_head_lamp, tv_foot_brake,
			tv_hand_brake, tv_reversing, tv_horn, tv_standby_trigger;

	private EditText time_num_et;

	static class MyHandler extends Handler {
		private final WeakReference<BluetoothTest> mActivity;

		public MyHandler(BluetoothTest activity) {
			mActivity = new WeakReference<BluetoothTest>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mActivity.get() != null) {
				if (msg.what == BT_PAIR_RESULT) {
					String outputData = msg.getData().toString();
					Log.e(TAG, "handleMessage outputData="+outputData);
				}else if(msg.what == START_CONNECT){
					if(reConnectNum > 0){
						reConnectNum--;
					}
					BTWorkService.workThread.disconnect();
					BTWorkService.workThread.close();
					BTWorkService.workThread.connect(mDeviceAddress);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ble_test);
		handler = new MyHandler(this);
		b = getIntent().getExtras();
		// 从意图获取显示的蓝牙信息
		mDeviceName = b.getString(EXTRAS_DEVICE_NAME);
		mDeviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);
		mRssi = b.getString(EXTRAS_DEVICE_RSSI);

		/* 启动蓝牙service */
		startBleService();

		init();
		initAnims();
		tv_device_name.setText(mDeviceName);
		tv_device_address.setText(mDeviceAddress);

//		File file = new File(fileName);
//		if (!file.exists()) {
//			boolean dd = file.mkdir();
//			Log.d("hyw1", "boolean:" + dd);
//		}
//		textFile = new File(fileName + txt);

	}

	// 初始化动作
	private void initAnims() {
		anim_in_down = AnimationUtils.loadAnimation(this, R.anim.slide_in_dowm);
		anim_out_down = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_down);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isThredRun = true;
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}
		if (BTWorkService.workThread != null) {
			Log.d(TAG, "关闭蓝牙连接");
			BTWorkService.workThread.disconnect();
			BTWorkService.workThread.close();
		}
		// 解除广播接收器
		unregisterReceiver(mGattUpdateReceiver);
		// 停止发送心跳包
		sendHeat = false;
//		if (mBluetoothLeService != null) {
//			mBluetoothLeService.disconnect();
//		}
		reConnect = false;

	}

	// Activity出来时候，绑定广播接收器，监听蓝牙连接服务传过来的事件
	@Override
	protected void onResume() {
		super.onResume();
		isnotify = false;
		Log.e(TAG, "onResume");
		// 绑定广播接收器
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (BTWorkService.workThread != null) {
			BTWorkService.workThread.connect(mDeviceAddress);
			Log.e(TAG, "onResume connect mDeviceAddress:" + mDeviceAddress);
		} else {
			Log.e(TAG, "BTWorkService.workThread = null:" + mDeviceAddress);
		}
//		if (mBluetoothLeService != null) {
//			// 根据蓝牙地址，建立连接
//			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//			Log.d(TAG, "Connect request result=" + result);
//		}
	}

	/**
	 * @Title: init
	 * @Description: TODO(初始化UI控件)
	 * @param 无
	 * @return void
	 * @throws
	 */
	private void init() {
		time_num_et = (EditText) findViewById(R.id.time_num_et);
		test_btn = (Button) findViewById(R.id.test_btn);
		test_btn.setOnClickListener(this);
		clear_btn = (Button) findViewById(R.id.clear_btn);
		clear_btn.setOnClickListener(this);

		ll_register = (LinearLayout) findViewById(R.id.ll_register);
		ll_info = (LinearLayout) findViewById(R.id.ll_info);
		ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
		rev_sv = (ScrollView) this.findViewById(R.id.rev_sv);
		rev_tv = (TextView) this.findViewById(R.id.rev_tv);
		tv_clear = (TextView) this.findViewById(R.id.tv_clear);
		tv_clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				rev_tv.setText("");
			}
		});
		connect_state = (TextView) this.findViewById(R.id.connect_state);

		send_et = (EditText) this.findViewById(R.id.send_et);
		send_instruct_iiv = (InstructInputView) this
				.findViewById(R.id.send_instruct_iiv);
		connect_state.setText(status);
		send_btn = (Button) this.findViewById(R.id.send_btn);
		send_btn.setOnClickListener(this);

		tv_device_address = (TextView) findViewById(R.id.tv_device_address);
		tv_device_name = (TextView) findViewById(R.id.tv_device_name);
		btn_clear = (Button) findViewById(R.id.btn_clear);
		btn_clear.setOnClickListener(this);
		spinner = (Spinner) findViewById(R.id.spinner);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				plateform = (byte) (position + 1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		btn_password = (Button) findViewById(R.id.btn_password);
		btn_password.setOnClickListener(this);
		btn_send = (Button) findViewById(R.id.btn_send);
		btn_send.setOnClickListener(this);

		et_password = (EditText) findViewById(R.id.et_password);
		et_driver = (EditText) findViewById(R.id.et_driver);
		et_car = (EditText) findViewById(R.id.et_car);

		btn_notify = (Button) findViewById(R.id.btn_notify);
		btn_notify.setOnClickListener(this);
		switch_btn = (Button) findViewById(R.id.switch_btn);
		switch_btn.setOnClickListener(this);

		tv_heart = (TextView) findViewById(R.id.tv_heart);
		mTogBtn = (ToggleButton) findViewById(R.id.mTogBtn); // 获取到控件
		mTogBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					sendHeat = true;
					// 选中
				} else {
					// 未选中
					sendHeat = false;
				}
			}
		});// 添加监听事件


		isThredRun = true;
		new Thread(new ThreadShow()).start();

	}

	private String sendInstruct;
	// handler类接收数据
	Handler handlerTime = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				if (!TextUtils.isEmpty(sendInstruct) && isPair) {
					if (send_et.getVisibility() == View.VISIBLE) {
						sendInstruct = send_et.getText().toString().trim();
						sendInstruct = sendInstruct.replaceAll(" ", "");
						BtSendCommandUtils.sendInputInstruct(sendInstruct);
					} else {
						send_instruct_iiv.sendViewInstruct();
					}
				}
			}
		};
	};
	private String sleepTimeStr = "1000";
	private long sleepTimeLon = 1000;

	// 线程类
	class ThreadShow implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (isThredRun) {
				if (sendHeat) {
					try {
						Message msg = new Message();
						msg.what = 1;
						handlerTime.sendMessage(msg);
						sleepTimeStr = time_num_et.getText().toString().trim();
						if (!TextUtils.isEmpty(sleepTimeStr)) {
							sleepTimeLon = Long.parseLong(sleepTimeStr);
						}
						Thread.sleep(sleepTimeLon);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("thread error...");
					}
				}
			}
		}
	}

	/**
	 * 广播接收器，负责接收BluetoothLeService类发送的数据
	 */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BTWorkThread.ACTION_GATT_CONNECTED.equals(action))// Gatt连接成功
			{
				Log.e(TAG, "connected");
				mConnected = true;
				status = "connected";
				// 更新连接状态
				connect_state.setText(status);
				passwordNum = 3;
				
				if (isPair) {

				} else {
					isPair = true;
					ll_register.setVisibility(View.GONE);
					ll_register.startAnimation(anim_out_down);
					ll_bottom.startAnimation(anim_in_down);
					ll_bottom.setVisibility(View.VISIBLE);
					
					//密码验证逻辑，测试省去
//					ll_register.setVisibility(View.VISIBLE);
//					ll_register.startAnimation(anim_in_down);
				}
			} else if (BTWorkThread.ACTION_GATT_DISCONNECTED// Gatt连接失败
					.equals(action)) {
				Log.e(TAG, "Gatt连接失败");
				mConnected = false;
				isPair = false;
				status = "disconnected";
				ll_bottom.startAnimation(anim_out_down);
				ll_bottom.setVisibility(View.GONE);
				if(ll_register.getVisibility() == View.VISIBLE){
					ll_register.setVisibility(View.GONE);
				}
				// 更新连接状态
				connect_state.setText(status);
				reConnect = true;

				if (BTWorkService.workThread != null && reConnectNum == 0) {
					reConnectNum++;
					handler.sendEmptyMessageDelayed(START_CONNECT, 1000);
//					BTWorkService.workThread.connect(mDeviceAddress);
				}
			} else if (BTWorkThread.ACTION_GATT_SERVICES_DISCOVERED// 发现GATT服务器
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				// 获取设备的所有蓝牙服务
				// displayGattServices(mBluetoothLeService
				// .getSupportedGattServices());
				// displayGattServices(BTWorkService.workThread.getSupportedGattServices());

			} else if (BTWorkThread.ACTION_DATA_AVAILABLE.equals(action))// 有效数据
			{
				// 处理发送过来的数据
				String receiveData = intent
						.getStringExtra(BTWorkThread.EXTRA_DATA);
				String hexData = intent.getStringExtra(
						BTWorkThread.EXTRA_DATA).toUpperCase();
				if (!"41542B030110010131470D0A".equals(receiveData)) {
					System.out.println("Receiver onData:" + receiveData);
					displayData(hexData);
				}
				byte[] valueTemp = Hex2ByteUtil.hexStringToBytes(hexData);
				// showToast("收到消息："+hexData);
				
			}
		}
	};

	/* 意图过滤器 */
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BTWorkThread.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BTWorkThread.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BTWorkThread.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BTWorkThread.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	/**
	 * @Title: displayData
	 * @Description: TODO(接收到的数据在scrollview上显示)
	 * @param @param rev_string(接受的数据)
	 * @return void
	 * @throws
	 */
	private void displayData(final String rev_string) {
		rev_str += rev_string;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// rev_tv.setText(rev_str + "\n");
				rev_tv.append(rev_string + "\r\n");
				if (rev_tv.getMeasuredHeight() > 100) {
					rev_str = "";
				}
				rev_sv.scrollTo(0, rev_tv.getMeasuredHeight());
				// System.out.println("rev:" + rev_str);
			}
		});
	}


	@Override
	public void onClick(View v) {
		// // TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_btn:
			if (send_et.getVisibility() == View.VISIBLE) {
				send_et.setVisibility(View.GONE);
				send_instruct_iiv.setVisibility(View.VISIBLE);
			} else {
				send_et.setVisibility(View.VISIBLE);
				send_instruct_iiv.setVisibility(View.GONE);
			}

			break;

		case R.id.send_btn:
			if (isPair) {
				if (send_et.getVisibility() == View.VISIBLE) {
					if ("".equals(send_et.getText().toString())) {
						showToast("信息不能为空");
						return;
					}
					sendInstruct = send_et.getText().toString().trim();
					sendInstruct = sendInstruct.replaceAll(" ", "");
					BtSendCommandUtils.sendInputInstruct(sendInstruct);
				} else {
					send_instruct_iiv.sendViewInstruct();
				}
			}
			break;
		case R.id.btn_password:
			if ("".equals(et_password.getText().toString())) {
				Toast.makeText(getApplicationContext(), "密码不能为空",
						Toast.LENGTH_SHORT).show();
				return;
			}
			password = et_password.getText().toString();
			// LoginByPassword(password.toCharArray());
			BtInstructionInfo info_serndPassword = new BtInstructionInfo(
					BtInstructionType.Control_SendPassword, password);
			TerminalSerial.setInstuction(info_serndPassword,
					new SendPasswordListener() {
						@Override
						public void getInstructionInput(String Input) {
							Log.d(TAG, "ID输入Input:" + Input);
						}

						@Override
						public void getInstructionOutput(final String Output) {
							Log.e(TAG, "输出:" + Output);
							BluetoothTest.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Log.e(TAG, "runOnUiThread");
									CommunicationInstruction instructionInfo = new CommunicationInstruction(
											Output, 2);
									if (instructionInfo.getData() != null) {

										for (int i = 0; i < instructionInfo.getData().length; i++) {
											Log.e(TAG,
													"数据i:"
															+ instructionInfo.getData()[i]);
										}
										if (instructionInfo.getData()[0] == (byte) 0x00) {
											Log.e(TAG, "密码正确:");
											isPair = true;
											showToast("密码正确");
											ll_register.setVisibility(View.GONE);
											ll_register.startAnimation(anim_out_down);
											handler.postDelayed(new Runnable() {

												@Override
												public void run() {
													ll_bottom.startAnimation(anim_in_down);
													ll_bottom.setVisibility(View.VISIBLE);
												}
											}, 1000);
										} else {
											passwordNum--;
											if (passwordNum == 0) {
												passwordNum = 3;
												// mBluetoothLeService.disconnect();
//												finish();
											} else if (passwordNum > 0) {
												showToast("密码错误，还有" + passwordNum
														+ "次机会");
											} else {

											}
										}
									}
								}
							});
						}

						@Override
						public void getInstructionData(final String Data) {
							BluetoothTest.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {

								}
							});
						}
					});

			break;
		case R.id.btn_send:
			if ("".equals(et_driver.getText().toString())
					|| "".equals(et_car.getText().toString())) {
				Toast.makeText(getApplicationContext(), "信息不能为空",
						Toast.LENGTH_SHORT).show();
				return;
			}
			driverInfo = et_driver.getText().toString();
			carInfo = et_car.getText().toString();
			break;

		case R.id.btn_clear:
	
			break;

		case R.id.clear_btn:
			rev_str = "";
			rev_tv.setText("");
			break;

		case R.id.test_btn:
//			if (mConnected && isPair) {
//				Intent intent = new Intent(this, InstructTestActivity.class);
//				startActivity(intent);
//			} else {
//				Toast.makeText(getApplicationContext(), "请先连接配对",
//						Toast.LENGTH_SHORT).show();
//			}
			break;
		}
	}



	private void showToast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

	private void startBleService() {
		Log.e(TAG, "startBleService");
		gattServiceIntent = new Intent(this, BTWorkService.class);
		startService(gattServiceIntent);
	}
	
	
	private void stopBleService() {
		Log.e(TAG, "stopBleService");
		Intent intent = new Intent(this, BTWorkService.class);
		stopService(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			finish();
			// BluetoothLeService.mBluetoothGatt = null;
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}






}
