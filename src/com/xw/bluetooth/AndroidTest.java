package com.xw.bluetooth;

import com.xw.bledemo.R;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

public class AndroidTest extends Activity {

	private NetWorkChangeReceiver networkChangeReceiver;
	private final static String TAG = "AndroidTest";
	private AudioManager mAudioManager;
	private int max;
	private int current;

	private String ACTION_VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION";
	private String ACTION_MEDIA_BUTTON = "android.intent.action.MEDIA_BUTTON";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ble_test);

		initView();
		initData();

	}

	private void initView() {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 获取手机当前音量值
		current = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		Log.e(TAG, "keyCode=" + keyCode);
		switch (keyCode) {
		// 音量减小
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			Toast.makeText(this, "当前音量值： " + current, Toast.LENGTH_SHORT)
					.show();
			return true;
			// 音量增大
		case KeyEvent.KEYCODE_VOLUME_UP:
			Toast.makeText(this, "当前音量值： " + current, Toast.LENGTH_SHORT)
					.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initData() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_VOLUME_CHANGED);
		intentFilter.addAction(ACTION_MEDIA_BUTTON);
		networkChangeReceiver = new NetWorkChangeReceiver();
		registerReceiver(networkChangeReceiver, intentFilter);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);// 1
		current = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		Log.e("service", "系统音量值：" + max + "-" + current);

	}

	class NetWorkChangeReceiver extends BroadcastReceiver {
		@Override
		// 接收到广播后,要执行的代码
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			 if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {  
				// 获得KeyEvent对象  
			    KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);  
			    Log.e(TAG, "Action ---->" + action + "  KeyEvent----->"+ keyEvent.toString()); 
			 }
			
			if (ACTION_VOLUME_CHANGED.equals(action)) {
				Log.e(TAG, "音量改变 ");
				current = mAudioManager
						.getStreamVolume(AudioManager.STREAM_SYSTEM);
				Log.e("service", "系统音量值：" + max + "-" + current);

				Toast.makeText(context, "音量改变 ", Toast.LENGTH_LONG).show();
			}
		}
	}

}
