package com.xw.ble;

import com.xw.ble.utils.Hex2ByteUtil;

import android.util.Log; 


/**
 * 
* @ClassName CommunicationInstruction 
* @Description 通讯指令实体类
* @author kris lau
* @date 2017-11-23 下午4:49:34
 */
public class CommunicationInstruction {
	private byte Source;  //发送源
	private byte Destination;  //目的地
	private byte Command;  //命令
	private byte Parameter;  //参数
	private byte[] dataNumber;  //数据数量
	private byte[] Data;  //数据
	private byte checkSum;  //校验和
	
	public byte getSource(){
		return Source;
	}
	
	public void setSource(byte bSource){
		this.Source=bSource;
	}
	
	public byte getDestination(){
		return Destination;
	}
	
	public void setDestination(byte bDestination){
		this.Destination=bDestination;
	}
	
	public byte getCommand(){
		return Command;
	}
	
	public void setCommand(byte bCommand){
		this.Command=bCommand;
	}
	
	public byte getParameter(){
		return Parameter;
	}
	
	public void setParameter(byte bParameter){
		this.Parameter=bParameter;
	}
	
	public byte[] getDataNumber(){
		return dataNumber;
	}
	
	public void setDataNumber(byte[] bDataNumber){
		this.dataNumber=bDataNumber;
	}
	
	public byte[] getData(){
		return Data;
	}
	
	public void setData(byte[] bData){
		this.Data=bData;
	}
	
	public byte getCheckSum(){
		return checkSum;
	}
	
	public void setCheckSum(byte bCheckSum){
		this.checkSum=bCheckSum;
	}
	
	public CommunicationInstruction(){
		
	}
	
	/**
	 * 实例化通讯实体类
	 * @param bSource
	 * @param bDestination
	 * @param bCommand
	 * @param bParameterNumber
	 * @param bParameter
	 * @param bDataNumber
	 * @param bData
	 * @param bCheckSum
	 */
	public CommunicationInstruction(byte bSource,byte bDestination,byte bCommand,byte bParameter,byte[] bDataNumber,byte[] bData){
		this.Source=bSource;
		this.Destination=bDestination;
		this.Command=bCommand;
		this.Parameter=bParameter;
		this.dataNumber=bDataNumber;
		this.Data=bData;
	}
	
	public CommunicationInstruction(String hexStr) {
		if(hexStr.length()<9*2){
			return ;
		}
		this.Source  = Hex2ByteUtil.hexStringToByte(hexStr.substring(3*2,4*2));
		this.Destination  = Hex2ByteUtil.hexStringToByte(hexStr.substring(4*2,5*2));
		this.Command  = Hex2ByteUtil.hexStringToByte(hexStr.substring(5*2,6*2));
		this.Parameter  = Hex2ByteUtil.hexStringToByte(hexStr.substring(6*2,7*2));
		this.dataNumber  = Hex2ByteUtil.hexStringToBytes(hexStr.substring(7*2,8*2));
		int iDataNumber=0;
		try{
			iDataNumber=Integer.parseInt(hexStr.substring(7*2,8*2), 16);
		}
		catch(Exception e){}
		if(iDataNumber>=1 && (8+iDataNumber)*2 <= hexStr.length()){
			this.Data = Hex2ByteUtil.hexStringToBytes(hexStr.substring(8*2, (8+iDataNumber)*2));
		}
	}
	
	public CommunicationInstruction(String hexStr,int type) {
		if (type == 2) {
			if(hexStr.length()<5*2){
				return ;
			}
			this.Source  = Hex2ByteUtil.hexStringToByte(hexStr.substring(1*2,2*2));
			this.Destination  = Hex2ByteUtil.hexStringToByte(hexStr.substring(2*2,3*2));
			this.Command  = Hex2ByteUtil.hexStringToByte(hexStr.substring(3*2,4*2));
			this.Parameter  = (byte)0x00;
			this.dataNumber  = Hex2ByteUtil.hexStringToBytes(hexStr.substring(4*2,5*2));
			int iDataNumber=0;
			try{
				iDataNumber=Integer.parseInt(hexStr.substring(4*2,5*2), 16);
			}
			catch(Exception e){}
			if(iDataNumber>=1 && (5+iDataNumber)*2 <= hexStr.length()){
				this.Data = Hex2ByteUtil.hexStringToBytes(hexStr.substring(5*2, (5+iDataNumber)*2));
			}
		}
		
	}
	
}
