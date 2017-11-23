package com.xw.ble.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;

public class Hex2ByteUtil {

//	public static void main(String[] args) {
//		string2Hex();
//		Hex2File();
//		System.out.println("main test");
//
//	}
/**
 * 
* @Title Hex2Txt 
* @Description 将收到的16进制字符串写入到txt文件中
* @author 李官周
* @date 2014-8-15 上午9:46:24 
* @param hexStr
* @param fileOutPath
 */
	public static void Hex2Txt(String hexStr,String fileOutPath) {
		File out = new File(fileOutPath);
		try{
			if (!out.exists()) {
				out.createNewFile();
			}
			FileWriter fw = new FileWriter(out,true);
			fw.write(hexStr);
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
/**
	 * 
	* @Title txt2File 
	* @Description 将txt文本中的16进制字符串读出来并转化为文件
	* @author 李官周
	* @date 2014-8-15 上午9:50:02 
	* @param FileInPath
	* @param FileOutPath
	 */
	public static boolean txt2File(String FileInPath,String FileOutPath) {
		byte[] bytes = new byte[512];
		File file = new File(FileInPath);
		File out = new File(FileOutPath);
		try {
			if(!file.exists()){
				return false;
			}
			if (!out.exists()) {
				out.createNewFile();
			}
			// FileInputStream in = new FileInputStream(file);
			FileReader in = new FileReader(file);
			BufferedReader br = new BufferedReader(in);
			// FileOutputStream fileout = new FileOutputStream(out);
			// FileWriter fw = new FileWriter(out);
			FileOutputStream fout = new FileOutputStream(out);
			String result;
			while ((result = br.readLine()) != null) {
				bytes = hexStringToBytes(result);
				// fw.write(bytes);
				fout.write(bytes);
			}
			fout.close();
			in.close();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	private static void string2Hex() {
		byte[] bytes = new byte[512];
		File file = new File("C:/Users/li/Desktop/BusInfoVision.patch");
		File out = new File(
				"C:/Users/li/Desktop/BusInfoVision.patch.txt");

		try {
			if (!out.exists()) {
				out.createNewFile();
			}
			FileInputStream in = new FileInputStream(file);
			// FileOutputStream fileout = new FileOutputStream(out);
			FileWriter fw = new FileWriter(out);
			int size = 0;
			while ((size = in.read(bytes)) != -1) {
					String hex = Hex2ByteUtil.bytesToHexString(bytes,0, size);
					fw.write(hex);
			}
			fw.close();
			in.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void Hex2File() {
		byte[] bytes = new byte[512];
		File file = new File("C:/Users/Administrator/Desktop/��201.txt");
		File out = new File("C:/Users/Administrator/Desktop/��201_2.zip");

		try {
			if (!out.exists()) {
				out.createNewFile();
			}
			// FileInputStream in = new FileInputStream(file);
			FileReader in = new FileReader(file);
			BufferedReader br = new BufferedReader(in);
			// FileOutputStream fileout = new FileOutputStream(out);
			// FileWriter fw = new FileWriter(out);
			FileOutputStream fout = new FileOutputStream(out);
			String result;
			while ((result = br.readLine()) != null) {
				bytes = hexStringToBytes(result);
				// fw.write(bytes);
				fout.write(bytes);
			}
			fout.close();
			in.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String stringToHexString(String str) {
		return bytesToHexString(str.getBytes());
	}
	public static String stringToHexString(String str,String charsetName) {
		try {
			return bytesToHexString(str.getBytes(charsetName));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/** 
	* @Title bytesToHexString 
	* @Description 字节数组装16进制字符串
	* @author 李官周
	* @date 2014-9-1 上午11:07:54 
	* @param src 字节数组
	* @return 
	*/
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}
	
	/** 
	* @Title bytesToHexString 
	* @Description 字节数组装16进制字符串
	* @author 李官周
	* @date 2014-9-1 上午11:07:54 
	* @param src 字节数组
	* @return 
	*/
	public static String bytesToHexString(byte[] src,int start,int size) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || start < 0|| size <= start ) {
			return null;
		}
		int len=start+size;
		for (int i = start; i < len; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	/** 
	* @Title hexStringToByte 
	* @Description 返回一个byte
	* @author 李官周
	* @date 2015-1-20 上午10:59:16 
	* @param hexString
	* @return 
	*/
	public static byte hexStringToByte(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return 0;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		if(length>1){
			return 0;
		}
		char[] hexChars = hexString.toCharArray();
		byte d = 0;
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	/** 
     * 把中文转成Unicode�? 
     * @param str 
     * @return 
     */  
    public static String chinaToUnicode(String str){  
        String result="";  
        for (int i = 0; i < str.length(); i++){  
            int chr1 = (char) str.charAt(i);  
            if(chr1>=19968&&chr1<=171941){//汉字范围 \u4e00-\u9fa5 (中文)  
                result+="" + Integer.toHexString(chr1);  
            }else{  
                result+=str.charAt(i);  
            }  
        }  
        return result.toUpperCase();  
    }  
    
    /** 
	* @Title int2Bytes 
	* @Description ��������ת��Ϊbyte����
	* @author �º���
	* @date 2015-8-10 ����10:38:57 
	* @param integer  Ҫת������
	* @param byteLen  ת����byte���鳤�ȣ����㲹0x00
	* @return 
	*/
	public static byte[] int2Bytes(long integer,int byteLen){
		byte[] bytes = new byte[byteLen];
		try{
			int b = (byteLen - 1)* 8;
			for(int i = 0 ; i < byteLen ; i++){
				bytes[i] = (byte) (integer >> b);
				b -= 8;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return bytes;
	}
	
	/**
	 * 
	* @Title bytesToInt2 
	* @Description ��byte����ת��int  ������������(��λ�ں󣬸�λ��ǰ)��˳��
	* @author ������
	* @date 2015-9-19 ����3:02:43 
	* @param src
	* @param offset
	* @return
	 */
	 public static int bytesToInt2(byte[] ary, int offset) {  
		 int mask=0xff;  
         int temp=0;  
         int n=0;  
         for(int i=0;i<offset;i++){  
            n<<=8;  
            temp=ary[i]&mask;  
            n|=temp;  
        }  
         	return n;  
	 }  
	 
	 /***
	  * 
	 * @Title bytesToInt 
	 * @Description ��byte����ת��int  ������������(��λ�ں󣬵�λ��ǰ)��˳��
	 * @author ������
	 * @date 2015-9-25 ����8:56:54 
	 * @param ary
	 * @param offset
	 * @return
	  */
	 public static int bytesToInt(byte[] ary, int offset) {  
		 int mask=0xff;  
         int temp=0;  
         int n=0;  
         for(int i=0;i<offset;i++){  
            n<<=8;  
            temp=ary[offset-i-1]&mask;  
            n|=temp;  
        }  
         	return n;  
	 }  
	 
	 /**
	  * 
	 * @Title reverse 
	 * @Description ��byte���鷴ת
	 * @author ������
	 * @date 2015-9-25 ����9:54:57 
	 * @param myByte
	 * @return
	  */
	 public static byte[] reverseBytes(byte[] myByte) {
			byte[] newByte = new byte[myByte.length];

			for (int i = 1; i <= myByte.length; i++) {
				newByte[myByte.length - i] = myByte[i - 1];
			}

			return newByte;
		}
	 
	 /** 
		* @Title HexStringAddComma 
		* @Description �ַ���ż���ӷָ��������ţ�
		* @author �����
		* @date 2016-3-21 ����9:36:23 
		* @param src
		* @return 
		*/
		public static String HexStringAddComma(String src) {
			StringBuilder stringBuilder = new StringBuilder("");
			for (int i = 0; i < src.length(); i++) {
				if (i%2==0) {
					stringBuilder.append(",");
				}
				stringBuilder.append(src.charAt(i));
			}
			return stringBuilder.toString().toUpperCase();
		}
		public static byte[] longToByte2(long number) { 
	        long temp = number; 
	        byte[] b = new byte[2]; 
	        for (int i = 0; i < b.length; i++) { 
	            b[b.length-1-i] = new Long(temp & 0xff).byteValue();// �����λ���������λ 
	            temp = temp >> 8; // ������8λ 
	        } 
	        return b; 
	    } 
	 public static byte[] longToByte4(long number) { 
		 long temp = number; 
		 byte[] b = new byte[4]; 
		 for (int i = 0; i < b.length; i++) { 
			 b[b.length-1-i] = new Long(temp & 0xff).byteValue();// �����λ���������λ 
			 temp = temp >> 8; // ������8λ 
		 } 
		 return b; 
	 } 
	 
		/**
		* @Title decimalToHexString 
		* @Description 10����ת16�����ַ���
		* @author jw
		* @date 2015-11-23 ����3:06:56 
		* @param i
		* @return
		 */
		public static String decimalToHexString(int i) {
			String strDecimal="";
			if(i>15){
				strDecimal=Integer.toHexString(i);
			}
			else if (i <=15){
				strDecimal="0"+Integer.toHexString(i);
			}
			return strDecimal;
		}
	 
	 
}
