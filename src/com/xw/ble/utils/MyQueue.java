package com.xw.ble.utils;

import java.util.regex.Matcher; 
import java.util.regex.Pattern;

import com.xw.ble.RecvDataListener;


import android.util.Log;


public class MyQueue {
	private static final String TAG = "MyQueue";
	private int MAXLEN = 4192;
	private int front;
	private int rear;
	private byte quebuffer[];
	private int num = 0;
    private RecvDataListener mRecvDataListener;
    
    
	public MyQueue() {
		quebuffer = new byte[MAXLEN];
		front = 0;
		rear = 0;
		num = 0;
	}

	public RecvDataListener getmRecvDataListener() {
		return mRecvDataListener;
	}

	public void setmRecvDataListener(RecvDataListener mRecvDataListener) {
		this.mRecvDataListener = mRecvDataListener;
	}




	/** 判空函数 */
	public boolean isEmpty() {
		if (front == rear) {
			return true;
		}
		return false;
	}

	/** 判断队列是否已满 */
	public boolean isFull() {
		if (((rear + 1) % MAXLEN) == front) {
			return true;
		} else {
			return false;
		}
	}

	/** 入列 */
	public void enQueue(byte[] data) {
		int datalen = data.length;
		num = (MAXLEN - front + rear) % MAXLEN;
		// if (num < MAXLEN) {// 第二阶段：
		if (num + datalen >= MAXLEN) {// 将满
			if (rear + datalen >= MAXLEN) {// 跨越数组尾部
				System.arraycopy(data, 0, quebuffer, rear, (MAXLEN - rear));
				System.arraycopy(data, 0 + (MAXLEN - rear), quebuffer, 0,
						datalen - (MAXLEN - rear));
				rear = 0 + datalen - (MAXLEN - rear);
			} else {
				System.arraycopy(data, 0, quebuffer, rear, datalen);
				rear += datalen;
			}
			front = rear;
		} else {// 未满
			if (rear + datalen >= MAXLEN) {// 跨越数组尾部
				System.arraycopy(data, 0, quebuffer, rear, (MAXLEN - rear));
				System.arraycopy(data, 0 + (MAXLEN - rear), quebuffer, 0,
						datalen - (MAXLEN - rear));
				rear = 0 + datalen - (MAXLEN - rear);
			} else {
				System.arraycopy(data, 0, quebuffer, rear, datalen);
				rear += datalen;
			}
		}
	}

	// 出列
	byte[] outQueue() {
		if (front < rear) {
			byte[] out = new byte[rear - front];
			System.arraycopy(quebuffer, front, out, 0, rear - front);
			return out;
		} else {
			byte[] out = new byte[MAXLEN - front + rear];
			System.arraycopy(quebuffer, front, out, 0, MAXLEN - front);
			System.arraycopy(quebuffer, 0, out, MAXLEN - front, rear);
			return out;
		}
	}

	Pattern pattern = Pattern
	// AT+...../R/N
			.compile("41542B(.{2})*?(..0D0A){1,2}");
	Pattern pattern2 = Pattern.compile("0E(.{2})*?0F");
	private String outStr;

	/**
	 * @Title RecvData
	 * @Description 指令提取
	 * @author 刘思政
	 * @date 2017-9-07 上午15:22:32
	 * @param source
	 * @return
	 */
	public synchronized String RecvData(byte[] source) {
        String instructStr = "";
		enQueue(source);
		try {
			outStr = Hex2ByteUtil.bytesToHexString(outQueue());
		} catch (Exception e) {
			outStr = "";
			e.printStackTrace();
		}
//		if (!"41542B030110010131470D0A".equals(outStr)) {
//			Log.e(TAG,"outStr:"+outStr);
//		}
		//匹配0E***0F连接指令
		if (outStr != null && outStr.length() > 0) {
			// if(!TextUtils.isEmpty(outStr)){
			Matcher matcher2 = pattern2.matcher(outStr);
			Matcher matcher = pattern.matcher(outStr);
			int frontTemp = front;
			int end = -1;
			while (matcher2.find()) {
				end = matcher2.end() / 2;
				String hexCmd = matcher2.group().trim();
				// 一个指令包中重复出现的41542B，取最后部分
				String[] hexCmds = hexCmd.split("0E");
				if (hexCmds.length > 2) {
					hexCmd = hexCmds[hexCmds.length-1];
					hexCmd = "0E" + hexCmd;
				}
				int endTag = hexCmd.indexOf("0F");
				while (endTag % 2 == 1 && endTag > 0) {
					endTag = hexCmd.indexOf("0F", endTag + 1);
				}
				if (endTag < 0) {
					continue;
				}
				int oldCheckSum = Hex2ByteUtil.hexStringToByte(hexCmd
						.substring(endTag - 2, endTag));
				byte[] tempByte = Hex2ByteUtil.hexStringToBytes(hexCmd
						.substring(2, endTag - 2));
				int dataCheckSum = checkSum(tempByte);
				if (oldCheckSum == dataCheckSum) {
					instructStr = hexCmd;
					if (mRecvDataListener != null) {
						mRecvDataListener.setData(instructStr);
					}
				} else {
					 Log.e(TAG,"oldCheckSum:"+oldCheckSum+" ,dataCheckSum:"
					 +dataCheckSum +",source:"+hexCmd);
				}
				front = (frontTemp + end) % MAXLEN;
				// return end;
			}
			
			//匹配41542B***0D0A通讯指令
			while (matcher.find()) {
				end = matcher.end() / 2;
				String hexCmd = matcher.group().trim();
				// 一个指令包中重复出现的41542B，取最后部分
				String[] hexCmds = hexCmd.split("41542B");
				if (hexCmds.length > 3) {
					hexCmd = hexCmds[hexCmds.length-1];
					hexCmd = "41542B" + hexCmd;
				}
				int endTag = hexCmd.indexOf("0D0A");
				while (endTag % 2 == 1 && endTag > 0) {
					endTag = hexCmd.indexOf("0D0A", endTag + 1);
				}
				String[] stern = hexCmd.split("0D0A");
				if (stern.length > 1) {
					endTag = hexCmd.indexOf("0D0A", endTag + 1);
				}
				
				if (endTag < 0) {
					continue;
				}
				int oldCheckSum = Hex2ByteUtil.hexStringToByte(hexCmd
						.substring(endTag - 2, endTag));
				byte[] tempByte = Hex2ByteUtil.hexStringToBytes(hexCmd
						.substring(6, endTag - 2));
				int dataCheckSum = checkSum(tempByte);
				if (oldCheckSum == dataCheckSum) {
					// System.out.println("group:" + matcher.group());
					instructStr = hexCmd;
					if (mRecvDataListener != null) {
						mRecvDataListener.setData(instructStr);
					}
					front = (frontTemp + end) % MAXLEN;
//					if (!"41542B030110010131470D0A".equals(instructStr)) {
//					Log.e(TAG,"instructStr:"+instructStr);
//				}
				} else {
					 Log.e(TAG,"oldCheckSum:"+oldCheckSum+" ,dataCheckSum:"
					 +dataCheckSum +",source:"+hexCmd);
				}
				// return end;
			}
		}
		return instructStr;
	}

	
	public byte checkSum(byte[] dataArray) {
		if(dataArray == null){
			return (Byte) null;
		}
		byte sum = 0;
		int lengthInt = dataArray.length;
		for (int i = 0; i < lengthInt; i++) {
			sum += dataArray[i];
		}
		return (byte) ~sum;
	}


}
