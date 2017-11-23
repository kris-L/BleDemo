/**   
 * @Title SendCommandUtils.java 
 * @Package com.businfovision.util 
 * @Description  
 * @author 陈志刚
 * @date 2014-7-31 下午6:04:31 
 * @version V1.0   
 */
package com.xw.ble.utils;

import java.text.SimpleDateFormat;  
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xw.ble.CommunicationInstruction;
import com.xw.ble.MsgTotal_match;
import com.xw.ble.service.BTWorkService;

import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * @ClassName BtSendCommandUtils
 * @Description 蓝牙发送指令到主机的工具类
 * @author 刘思政
 * @date 2017-08-28
 */
public class BtSendCommandUtils {
	public static final String TAG = BtSendCommandUtils.class.getSimpleName();

	private static byte[] bFirstAppend = { (byte) 0x41, (byte) 0x54,
			(byte) 0x2B };
	private static byte[] bLastAppend = { (byte) 0x0D, (byte) 0x0A };

	private static byte[] bDATAnumb = {};

	public static byte checkSum(byte[] dataArray, int length) {
		byte sum = 0;
		for (int i = 0; i < length; i++) {
			sum += dataArray[i];
		}
		return sum;
	}

	/**
	 * @Title testInstructionInput
	 * @Description 输入指令字符串
	 * @author lsz
	 * @date 2017-11-23 下午3:19:27
	 * @param mInstruction
	 * @return
	 */
	private static String testInstructionInput(
			CommunicationInstruction mInstruction) {
		ArrayList<Byte> appendByte = new ArrayList<Byte>();
		appendByte.add(mInstruction.getSource());
		appendByte.add(mInstruction.getDestination());
		appendByte.add(mInstruction.getCommand());
		appendByte.add(mInstruction.getParameter());

		// appendByte.add(mInstruction.getDataNumber()[0]);
		appendByte.add(mInstruction.getDataNumber()[1]);
		if (mInstruction.getData() != null
				&& mInstruction.getData().length >= 1) {
			for (int i = 0; i <= mInstruction.getData().length - 1; i++) {
				appendByte.add(mInstruction.getData()[i]);
			}
		}
		byte[] content = new byte[appendByte.size()];
		for (int i = 0; i <= appendByte.size() - 1; i++) {
			content[i] = appendByte.get(i);
		}

		byte[] checkSum = { checkSum(content, content.length) };
		byte[] data = new byte[content.length + checkSum.length
				+ bFirstAppend.length + bLastAppend.length];
		System.arraycopy(bFirstAppend, 0, data, 0, bFirstAppend.length);
		System.arraycopy(content, 0, data, bFirstAppend.length, content.length);

		System.arraycopy(checkSum, 0, data, bFirstAppend.length
				+ content.length, checkSum.length);
		System.arraycopy(bLastAppend, 0, data, bFirstAppend.length
				+ content.length + checkSum.length, bLastAppend.length);

		String dataStr = Hex2ByteUtil.bytesToHexString(data);
		return dataStr;
	}

	public static boolean sendInstruction(CommunicationInstruction mInstruction) {
		ArrayList<Byte> appendByte = new ArrayList<Byte>();
		appendByte.add(mInstruction.getSource());
		appendByte.add(mInstruction.getDestination());
		appendByte.add(mInstruction.getCommand());
		appendByte.add(mInstruction.getParameter());

		// appendByte.add(mInstruction.getDataNumber()[0]);
		appendByte.add(mInstruction.getDataNumber()[1]);
		if (mInstruction.getData() != null
				&& mInstruction.getData().length >= 1) {
			for (int i = 0; i <= mInstruction.getData().length - 1; i++) {
				appendByte.add(mInstruction.getData()[i]);
			}
		}
		byte[] content = new byte[appendByte.size()];
		for (int i = 0; i <= appendByte.size() - 1; i++) {
			content[i] = appendByte.get(i);
		}

		byte[] checkSum = { checkSum(content, content.length) };
		byte[] data = new byte[content.length + checkSum.length
				+ bFirstAppend.length + bLastAppend.length];
		System.arraycopy(bFirstAppend, 0, data, 0, bFirstAppend.length);
		System.arraycopy(content, 0, data, bFirstAppend.length, content.length);
		System.arraycopy(checkSum, 0, data, bFirstAppend.length
				+ content.length, checkSum.length);
		System.arraycopy(bLastAppend, 0, data, bFirstAppend.length
				+ content.length + checkSum.length, bLastAppend.length);
		if (BTWorkService.workThread != null) {
			BTWorkService.workThread.writeData(data);
		}
		String dataStr = Hex2ByteUtil.bytesToHexString(data);
		if (!dataStr.contains("41542B00101001")) {
			Log.v("sendInstruction:" + TAG, dataStr);
		}
		return true;
	}

	/**
	 * 
	 * @Title sendPassword
	 * @Description 发送密码
	 * @author kris
	 * @date 2016-4-22 上午11:31:28
	 * @param password
	 */
	public static void sendPassword(String passwordStr) {
		Log.e(TAG, "sendPassword:" + passwordStr);
		char[] password = passwordStr.toCharArray();
		String pas = "";
		for (int i = 0; i < password.length; i++) {
			pas += "0";
			pas += password[i];
		}

		MsgTotal_match msgTotal = new MsgTotal_match((byte) 0x01, (byte) 0x02,
				(byte) 0x01, Hex2ByteUtil.hexStringToBytes(pas));
		byte[] data = msgTotal.getMsgBytes();
	
		if (BTWorkService.workThread != null) {
			BTWorkService.workThread.writeData(data);
		}
	}
	
	
	
	/**
	 * 
	 * @param msg
	 * @param length 校验和位数
	 * @return
	 */
	private static byte[] SumCheck(byte[] msg, int length) {
		long mSum = 0;
		byte[] mByte = new byte[length];

		/** 逐Byte添加位数和 */
		for (byte byteMsg : msg) {
			long mNum = ((long) byteMsg >= 0) ? (long) byteMsg
					: ((long) byteMsg + 256);
			mSum += mNum;
		}
		/** end of for (byte byteMsg : msg) */

		/** 位数和转化为Byte数组 */
		for (int liv_Count = 0; liv_Count < length; liv_Count++) {
			mByte[length - liv_Count - 1] = (byte) (mSum >> (liv_Count * 8) & 0xff);
		}
		/** end of for (int liv_Count = 0; liv_Count < length; liv_Count++) */

		return mByte;
	}


	
	
	/**
	 * @Title sendInputInstruct
	 * @Description 发送输入指令
	 * @author 刘思政
	 * @date 2017-9-01 上午10:00:10
	 * @param Data
	 * @return
	 */
	public static boolean sendInputInstruct(String sendInstruct) {
		byte[] data = Hex2ByteUtil.hexStringToBytes(sendInstruct);

		if (BTWorkService.workThread != null) {
			BTWorkService.workThread.writeData(data);
		}
		// String dataStr=Hex2ByteUtil.bytesToHexString(data);
		// Log.e(TAG,"sendInputInstruct:"+ dataStr);
		return true;
	}



	/**
	 * @Title getTimeData
	 * @Description 获取时间和数据相加
	 * @author jw
	 * @date 2015-12-1 下午4:44:01
	 * @param bOnlyData
	 * @return
	 */
	private static byte[] getTimeData(byte[] bOnlyData) {
		byte[] bData = null;
		if (bOnlyData == null) {
			return bData;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		String sNowDate = sdf.format(new Date());
		Pattern p = Pattern.compile("(\\w{2})");
		Matcher m = p.matcher(sNowDate);
		String sNowDateStr = "";
		while (m.find()) {
			sNowDateStr = sNowDateStr
					+ Hex2ByteUtil.decimalToHexString(Integer.parseInt(m
							.group())) + "2D"; // ascii码表 45代表-
		}
		sNowDateStr = sNowDateStr.substring(0, sNowDateStr.length() - 2);
		byte[] bNowDate = Hex2ByteUtil.hexStringToBytes(sNowDateStr);
		int iCount = bNowDate.length + bOnlyData.length;
		bData = new byte[iCount];
		System.arraycopy(bNowDate, 0, bData, 0, bNowDate.length);
		System.arraycopy(bOnlyData, 0, bData, bNowDate.length, bOnlyData.length);
		return bData;
	}

	/**
	 * @Title int2Bytes
	 * @Description 将正整数转换为byte数组
	 * @author jiangwei
	 * @date 2015-8-10 上午10:38:57
	 * @param integer
	 *            要转换的数
	 * @param byteLen
	 *            转换的byte数组长度，不足补0x00
	 * @return
	 */
	public static byte[] getDataNumber(int integer) {
		byte[] bytes = new byte[2];
		try {
			int b = (2 - 1) * 8;
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) (integer >> b);
				b -= 8;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return bytes;
	}

	/**
	 * @Title concat
	 * @Description 两个数组的合并
	 * @author djm
	 * @date 2017-2-6
	 * @return
	 */
	public static byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	/**
	 * @Title setDataEncryption
	 * @Description 数据加密
	 * @author djm
	 * @param byte[] a 要加密的数据
	 * @date 2017-2-6
	 * @return
	 */

	public static byte[] setDataEncryption(byte[] a) {
		int i, c;
		byte[] b = new byte[a.length];
		for (i = 0; i < a.length; i++) {
			c = i % 8;
			switch (c) {
			case 0:
				b[i] = (byte) ~(a[i] ^ (byte) 0x35);
				break;
			case 1:
				b[i] = (byte) ~(a[i] ^ (byte) 0x26);
				break;
			case 2:
				b[i] = (byte) ~(a[i] ^ (byte) 0x55);
				break;
			case 3:
				b[i] = (byte) ~(a[i] ^ (byte) 0x8A);
				break;
			case 4:
				b[i] = (byte) ~(a[i] ^ (byte) 0x93);
				break;
			case 5:
				b[i] = (byte) ~(a[i] ^ (byte) 0x3B);
				break;
			case 6:
				b[i] = (byte) ~(a[i] ^ (byte) 0x48);
				break;
			case 7:
				b[i] = (byte) ~(a[i] ^ (byte) 0x33);
				break;
			}
		}
		return b;
	}
}
