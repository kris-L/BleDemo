package com.xw.ble;

import com.xw.ble.utils.MsgHandlerTools;

public class MsgTotal_match {

	/**
	 * ��ʼ��  0x0E
	 */
	private byte startCharacter = 0x0E;
	
	/**
	 * Դ��ַ   0x01:app   ;  0x02 BLEģ��
	 */
	private byte srcAddress;
	
	/**
	 * Ŀ�ĵ�ַ  0x01:app   ;  0x02 BLEģ��
	 */
	private byte dstAddress;
	
	/**
	 * ����
	 */
	private byte command;
	
	/**
	 * �������ݳ���
	 */
	private byte addDataLen;
	
	/**
	 * ��������
	 */
	private byte[] addData;
	
	/**
	 * У����
	 */
	private byte checkCode;
	
	/**
	 * ������  0x0E
	 */
	private byte endCode = 0x0F;
	
	public MsgTotal_match(){
		
	}
	
	public MsgTotal_match(byte[] bytes){
		int index = 1;
		this.srcAddress = bytes[index++];
		this.dstAddress = bytes[index++];
		this.command = bytes[index++];
		this.addDataLen = bytes[index++];
		addData = new byte[addDataLen];
		System.arraycopy(bytes, index, addData, 0, addDataLen);
		index += addDataLen;
		checkCode = bytes[index++];
	}
	
	public MsgTotal_match(byte srcAddress,byte dstAddress,byte command,byte[] addData){
		this.srcAddress = srcAddress;
		this.dstAddress = dstAddress;
		this.command = command;
		if(addData==null){
			this.addDataLen = 0;
			return;
		}
		this.addData = addData;
		this.addDataLen = (byte) addData.length;
		System.arraycopy(addData, 0, addData, 0, addData.length);
	}
	
	public byte[] getMsgBytes(){
		byte[] temp =  new byte[7+addDataLen];
		temp[0] = startCharacter;
		temp[1] = srcAddress;
		temp[2] = dstAddress;
		temp[3] = command;
		temp[4] = addDataLen;
		if(addDataLen!=0){
			System.arraycopy(addData, 0, temp, 5, addDataLen);
		}
		byte[] srcToAddData = new byte[4+addDataLen];
		System.arraycopy(temp, 1, srcToAddData, 0, srcToAddData.length);
		temp[5+addDataLen] = MsgHandlerTools.checkSum(srcToAddData);
		temp[6+addDataLen] = endCode;
		return temp;
	}
	
	
	public byte getStartCharacter() {
		return startCharacter;
	}

	public void setStartCharacter(byte startCharacter) {
		this.startCharacter = startCharacter;
	}

	public byte getSrcAddress() {
		return srcAddress;
	}

	public void setSrcAddress(byte srcAddress) {
		this.srcAddress = srcAddress;
	}

	public byte getDstAddress() {
		return dstAddress;
	}

	public void setDstAddress(byte dstAddress) {
		this.dstAddress = dstAddress;
	}

	public byte getCommand() {
		return command;
	}

	public void setCommand(byte command) {
		this.command = command;
	}

	public byte getAddDataLen() {
		return addDataLen;
	}

	public void setAddDataLen(byte addDataLen) {
		this.addDataLen = addDataLen;
	}

	public byte[] getAddData() {
		return addData;
	}

	public void setAddData(byte[] addData) {
		this.addData = addData;
	}

	public byte getCheckCode() {
		return checkCode;
	}

	public void setCheckCode(byte checkCode) {
		this.checkCode = checkCode;
	}

	public byte getEndCode() {
		return endCode;
	}

	public void setEndCode(byte endCode) {
		this.endCode = endCode;
	}

}
