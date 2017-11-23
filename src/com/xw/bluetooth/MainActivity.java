package com.xw.bluetooth;

import com.bluetooth.Ble;
import com.xw.ble.BleActivity;
import com.xw.bledemo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{
	
	
	private Button tradition_bt_btn;
	private Button ble_bt_btn;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		
	}
	
	private void initView(){
		tradition_bt_btn = (Button) findViewById(R.id.tradition_bt_btn);
		ble_bt_btn = (Button) findViewById(R.id.ble_bt_btn);
		
		
		tradition_bt_btn.setOnClickListener(this);
		ble_bt_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.tradition_bt_btn:
			Intent intent = new Intent(this, BluetoothActivity.class);
			startActivity(intent);
			
			break;
			
		case R.id.ble_bt_btn:
			Intent intent1 = new Intent(this, BleActivity.class);
			startActivity(intent1);
			break;
		
		}
	}
	

}
