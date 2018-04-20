package com.xw.bluetooth;

import java.io.IOException; 
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;


import com.xw.bledemo.R;
import com.xw.bluetooth.BluetoothActivity.Type;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 会话界面
 * 
 * @Package com.xw.bluetooth
 * @author kris lau
 * @version 1.0
 * @Date 2017年11月22日
 */
public class ChatActivity extends Activity implements OnItemClickListener, OnClickListener {
	private static final int STATUS_CONNECT = 0x11;

	private ListView mListView;
	private ArrayList<DeviceBean> mDatas;
	private Button mBtnSend;// 发送按钮
	private Button mBtnDisconn;// 断开连接
	private EditText mEtMsg;
	private DeviceListAdapter mAdapter;

	/* 一些常量，代表服务器的名称 */
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
	public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";

	// 蓝牙服务端socket
	private BluetoothServerSocket mServerSocket;
	private BluetoothServerSocket mServerSocket2;
	
	// 蓝牙客户端socket
	private BluetoothSocket mSocket;
	// 蓝牙客户端socket
	private BluetoothSocket mSocket2;
	
	// 设备
	private BluetoothDevice mDevice;
	private BluetoothAdapter mBluetoothAdapter;

	// --线程类-----------------
	private ServerThread mServerThread;
	private ClientThread mClientThread;
	private ReadThread mReadThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		initDatas();
		initViews();
		initEvents();
	}

	private void initEvents() {
		mListView.setOnItemClickListener(this);

		// 发送信息
		mBtnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String text = mEtMsg.getText().toString();
				if (!TextUtils.isEmpty(text)) {
					// 发送信息
					sendMessageHandle(text);

					mEtMsg.setText("");
					mEtMsg.clearFocus();
					// 隐藏软键盘
					InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					manager.hideSoftInputFromWindow(mEtMsg.getWindowToken(), 0);
				} else
					Toast.makeText(ChatActivity.this, "发送内容不能为空！", Toast.LENGTH_SHORT).show();
			}
		});

		// 关闭会话
		mBtnDisconn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (BluetoothActivity.mType == Type.CILENT) {
					shutdownClient();
				} else if (BluetoothActivity.mType == Type.SERVICE) {
					shutdownServer();
				}
				BluetoothActivity.isOpen = false;
				BluetoothActivity.mType = Type.NONE;
				Toast.makeText(ChatActivity.this, "已断开连接！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void initViews() {
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setFastScrollEnabled(true);

		mEtMsg = (EditText) findViewById(R.id.MessageText);
		mEtMsg.clearFocus();

		mBtnSend = (Button) findViewById(R.id.btn_msg_send);
		mBtnDisconn = (Button) findViewById(R.id.btn_disconnect);
	}

	private void initDatas() {
		mDatas = new ArrayList<DeviceBean>();
		mAdapter = new DeviceListAdapter(this, mDatas);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	/**
	 * 信息处理
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String info = (String) msg.obj;
			switch (msg.what) {
			case STATUS_CONNECT:
				Toast.makeText(ChatActivity.this, info, 0).show();
				break;
			}
			
			if (msg.what == 1) {
				mDatas.add(new DeviceBean(info, true));
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(mDatas.size() - 1);
			}else {
				mDatas.add(new DeviceBean(info, false));
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(mDatas.size() - 1);
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		if (BluetoothActivity.isOpen) {
			Toast.makeText(this, "连接已经打开，可以通信。如果要再建立连接，请先断开", Toast.LENGTH_SHORT).show();
			return;
		}
		if (BluetoothActivity.mType == Type.CILENT) {
			String address = BluetoothActivity.BlueToothAddress;
			if (!"".equals(address)) {
				mBluetoothAdapter.cancelDiscovery();
				mDevice = mBluetoothAdapter.getRemoteDevice(address);
				mClientThread = new ClientThread();
				mClientThread.start();
				BluetoothActivity.isOpen = true;
			} else {
				Toast.makeText(this, "address is null !", Toast.LENGTH_SHORT).show();
			}
		} else if (BluetoothActivity.mType == Type.SERVICE) {
			mServerThread = new ServerThread();
			mServerThread.start();
			BluetoothActivity.isOpen = true;
		}
	}

	// 客户端线程
	private class ClientThread extends Thread {
		public void run() {
			try {
				mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//				mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00000003-0000-1000-8000-00805F9B34FB"));
	
//				mSocket =(BluetoothSocket) mDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mDevice,1);
//				mSocket.connect();
	
				Message msg = new Message();
				msg.obj = "请稍候，正在连接服务器:" + BluetoothActivity.BlueToothAddress;
				msg.what = STATUS_CONNECT;
				mHandler.sendMessage(msg);

//				mSocket.connect();

				msg = new Message();
				msg.obj = "已经连接上服务端！可以发送信息。";
				msg.what = STATUS_CONNECT;
				mHandler.sendMessage(msg);
				// 启动接受数据
				mReadThread = new ReadThread();
				mReadThread.start();
			} catch (IOException e) {
				Log.e("lsz", "error="+e);
				Message msg = new Message();
				msg.obj = "连接服务端异常！断开连接重新试一试。";
				msg.what = STATUS_CONNECT;
				mHandler.sendMessage(msg);
			}
		}
	};

	// 开启服务器
	private class ServerThread extends Thread {
		public void run() {
			try {
				// 创建一个蓝牙服务器 参数分别：服务器名称、UUID
				mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
						UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				mServerSocket2 = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
						UUID.fromString("00001101-0000-1000-8000-00805F9B34FF"));
				Message msg = new Message();
				msg.obj = "请稍候，正在等待客户端的连接...";
				msg.what = STATUS_CONNECT;
				mHandler.sendMessage(msg);

				/* 接受客户端的连接请求 */
				mSocket = mServerSocket.accept();
			

				msg = new Message();
				msg.obj = "客户端已经连接上！可以发送信息。";
				msg.what = STATUS_CONNECT;
				mHandler.sendMessage(msg);	
				// 启动接受数据
				mReadThread = new ReadThread();
				mReadThread.start();
				
				mServerSocket.close();
				if(mSocket != null){
					mSocket2 = mServerSocket2.accept();
				}
				
				ReadThread2 mReadThread2 = new ReadThread2();
				mReadThread2.start();
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	/* 停止服务器 */
	private void shutdownServer() {
		new Thread() {
			public void run() {
				if (mServerThread != null) {
					mServerThread.interrupt();
					mServerThread = null;
				}
				if (mReadThread != null) {
					mReadThread.interrupt();
					mReadThread = null;
				}
				try {
					if (mSocket != null) {
						mSocket.close();
						mSocket = null;
					}
					if (mServerSocket != null) {
						mServerSocket.close();
						mServerSocket = null;
					}
				} catch (IOException e) {
					Log.e("server", "mserverSocket.close()", e);
				}
			};
		}.start();
	}

	/* ͣ停止客户端连接 */
	private void shutdownClient() {
		new Thread() {
			public void run() {
				if (mClientThread != null) {
					mClientThread.interrupt();
					mClientThread = null;
				}
				if (mReadThread != null) {
					mReadThread.interrupt();
					mReadThread = null;
				}
				if (mSocket != null) {
					try {
						mSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mSocket = null;
				}
			};
		}.start();
	}

	// 发送数据
	private void sendMessageHandle(String msg) {
		if (mSocket == null) {
			Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			OutputStream os = mSocket.getOutputStream();
			os.write(msg.getBytes());

			mDatas.add(new DeviceBean(msg, false));
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(mDatas.size() - 1);
			
			if(mSocket2 != null){
				OutputStream os2 = mSocket2.getOutputStream();
				os2.write(msg.getBytes());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 读取数据
	private class ReadThread extends Thread {
		public void run() {
			byte[] buffer = new byte[1024];
			int bytes;
			InputStream is = null;
			try {
				is = mSocket.getInputStream();
				while (true) {
					if ((bytes = is.read(buffer)) > 0) {
						byte[] buf_data = new byte[bytes];
						for (int i = 0; i < bytes; i++) {
							buf_data[i] = buffer[i];
						}
						String s = new String(buf_data);
						Message msg = new Message();
						msg.obj = s;
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	// 读取数据2
	private class ReadThread2 extends Thread {
		public void run() {
			byte[] buffer = new byte[1024];
			int bytes;
			InputStream is = null;
			try {
				is = mSocket2.getInputStream();
				while (true) {
					if ((bytes = is.read(buffer)) > 0) {
						byte[] buf_data = new byte[bytes];
						for (int i = 0; i < bytes; i++) {
							buf_data[i] = buffer[i];
						}
						String s = new String(buf_data);
						Message msg = new Message();
						msg.obj = s;
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public void onClick(View view) {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (BluetoothActivity.mType == Type.CILENT) {
			shutdownClient();
		} else if (BluetoothActivity.mType == Type.SERVICE) {
			shutdownServer();
		}
		BluetoothActivity.isOpen = false;
		BluetoothActivity.mType = Type.NONE;
	}

}