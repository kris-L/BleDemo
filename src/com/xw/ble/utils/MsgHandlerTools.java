package com.xw.ble.utils;

import java.util.ArrayList;
import java.util.List;

public class MsgHandlerTools {

	/** 
	* @Title xorSum 
	* @Description ���
	* @author �����
	* @date 2015-9-30 ����2:03:14 
	* @param dataArray
	* @param length
	* @return 
	*/
	public static byte xorSum(byte[] dataArray, int length) {
		byte sum = (byte) (dataArray[0]^dataArray[1]);

		for(int i=2 ;i<length;i++){
			sum= (byte) (sum^dataArray[i]);
		}
		return (byte) ~sum;
	}
	
	public static byte checkSum(byte[] dataArray) {
		byte sum = 0;
		for(int i=0;i<dataArray.length;i++){
			sum += dataArray[i];
		}
		return (byte) ~sum;
	}
	
	public static List<byte[]> cutPackage(byte[] data){
		int len = data.length/20;
		int endLen = data.length%20;
		if(endLen>0){
			len +=1;
		}
		
		int index = 0;
		List<byte[]> dataList = new ArrayList<byte[]>();
		for(int i=0;i<len;i++){
			if(i==len-1&&endLen!=0){
				byte[] bytes = new byte[endLen];
				System.arraycopy(data, index, bytes, 0, bytes.length);
				dataList.add(bytes);
			}else{
				byte[] bytes = new byte[20];
				System.arraycopy(data, index, bytes, 0, bytes.length);
				index += bytes.length;
				dataList.add(bytes);
			}
			
		}
		return dataList;
	}

}
