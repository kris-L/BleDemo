package com.example.bledemo;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.bluetooth.Ble;
import com.bluetooth.BleBean;
import com.bluetooth.BluetoothUtils;
import com.xw.bledemo.R;

public class MainActivity extends Activity {
	private Ble ble;
	private BluetoothAdapter mBtAdapter=BluetoothAdapter.getDefaultAdapter();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 if(Ble.isSurpportedBle(this)){
			ble=new Ble(this,mHandler);	    	
		 
			new Thread(new Runnable() {			
				@Override
				public void run() {
					ble.scanBleDevice(true);//��ʼɨ��ble�豸����Ҫ�������spp���ӷ�ʽ����������ʱ�������߳�ִ�иú�ʱ������ܻ�ʹ���򿨶ٺ���								
				}
			}).start(); 
		//ble.sendCmd(" ",1);����ָ�ע��Ble�����޸�uuid
	    	 }
	}

	public void BleDiscovery(){
		if(!mBtAdapter.isEnabled())
			openBlueteeh();
	
		ArrayList<BleBean> devices=ble.getDevices();
		
		/*for (BleBean bluetoothDevice : devices) {	
		  LaundryMachine machine = new LaundryMachine();
		  machine.setDevice(bluetoothDevice.getDevice());
		  machine.setRssi((short)bluetoothDevice.getRssi());
		
		 addDevice(machine);
		 LogInfo("hhh", bluetoothDevice.getDevice()+"@"+bluetoothDevice.getRssi());
		}*/
	}	
	
	private void openBlueteeh() {
		try {
			mBtAdapter.enable();
			
		} catch (Exception e) {			
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, 22);	
			  
		}
		
		
	}  
	//������д��������
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothUtils.ENABLE_BLUETOOTH:
				/*Intent intent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, 1);*/
				break;

			case BluetoothUtils.DEVICE_SCAN_STARTED:

				break;

			case BluetoothUtils.DEVICE_SCAN_STOPPED:				
				break;
			case BluetoothUtils.DEVICE_SCAN_COMPLETED:
				BleDiscovery();
				break;
			case BluetoothUtils.DEVICE_CONNECTED:
				break;
			case BluetoothUtils.DEVICE_CONNECTING:

				break;
			case BluetoothUtils.DEVICE_DISCONNECTED:

				break;

			case BluetoothUtils.DATA_SENDED:

				break;
			case BluetoothUtils.DATA_READED:

				break;

			case BluetoothUtils.CHARACTERISTIC_ACCESSIBLE:

				break;

			default:
				break;
			}
		}
	};
	
	@Override
	public void onStop() {
    super.onStop();
  if(ble!=null) ble.stopGatt();  
}
}
