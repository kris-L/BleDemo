package com.xw.ble;


import com.xw.ble.utils.BtSendCommandUtils;
import com.xw.ble.utils.Hex2ByteUtil;
import com.xw.bledemo.R;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class InstructInputView extends LinearLayout{
	
	private Context context;
	private EditText send_receive_et;
	private EditText instruct_parameter_et;
	private EditText data_et;
	
	public InstructInputView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public InstructInputView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public InstructInputView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.context = context;
		initData();
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		View.inflate(getContext(), R.layout.instruct_input_view, this);
		send_receive_et = (EditText) findViewById(R.id.send_receive_et);
		instruct_parameter_et = (EditText) findViewById(R.id.instruct_parameter_et);
		data_et = (EditText) findViewById(R.id.data_et);
	}
	
	/**
	 * 初始化数据
	 */
	private void initData()
	{
		
	}
	
	public boolean sendViewInstruct()
	{
		String sendReceiveStr = send_receive_et.getText().toString().trim();
		String instructParameterStr = instruct_parameter_et.getText().toString().trim();
		String dataStr = data_et.getText().toString().trim();
		
		if (TextUtils.isEmpty(sendReceiveStr) || sendReceiveStr.length() < 4) {
			Toast.makeText(context, "请输入发送和接收地", Toast.LENGTH_SHORT).show();
		}else if (TextUtils.isEmpty(instructParameterStr)|| instructParameterStr.length() < 4) {
			Toast.makeText(context, "请输入指令和参数", Toast.LENGTH_SHORT).show();
		}else if (TextUtils.isEmpty(dataStr)) {
			Toast.makeText(context, "请输入数据", Toast.LENGTH_SHORT).show();
		}else{
			byte bSource = Hex2ByteUtil.hexStringToBytes(sendReceiveStr.substring(0, 2))[0];
			byte bDestination = Hex2ByteUtil.hexStringToBytes(sendReceiveStr.substring(2, 4))[0];
			byte bInstruction = Hex2ByteUtil.hexStringToBytes(instructParameterStr.substring(0, 2))[0];
			byte bParameter = Hex2ByteUtil.hexStringToBytes(instructParameterStr.substring(2, 4))[0];
			
			byte[] bData = {};
			if (!TextUtils.isEmpty(dataStr)) {
				bData = Hex2ByteUtil.hexStringToBytes(dataStr);
			}
			byte[] bDataNumber = BtSendCommandUtils.getDataNumber(bData.length);
			
			CommunicationInstruction versionInstruction = new CommunicationInstruction(
					bSource, bDestination, bInstruction, bParameter, bDataNumber,
					bData);
			BtSendCommandUtils.sendInstruction(versionInstruction);
		}
		return true;
	}
	
}
