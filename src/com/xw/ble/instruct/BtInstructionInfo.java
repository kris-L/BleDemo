package com.xw.ble.instruct;



public class BtInstructionInfo {
	private int MsgOperate;
	private BtInstructionType MsgType;
	private int MsgTime;
	private Object InputData;
	
	public int getMsgOperate(){
		return MsgOperate;
	}
	
	public void setMsgOperate(int iMsgOperate){
		this.MsgOperate=iMsgOperate;
	}
	
	public BtInstructionType getMsgType(){
		return MsgType;
	}
	
	public void setMsgType(BtInstructionType iType){
		this.MsgType=iType;
	}
	
	public int getMsgTime(){
		return MsgTime;
	}
	
	public void setMsgTime(int iTime){
		this.MsgTime=iTime;
	}
	
	public Object getInputData(){
		return InputData;
	}
	
	public void setInputData(Object oInputData){
		this.InputData=oInputData;
	}
	
	public BtInstructionInfo(){
		
	}
	
	public BtInstructionInfo(BtInstructionType iType){
		this.MsgType=iType;
	}
	
	public BtInstructionInfo(BtInstructionType iType,int iOperate){
		this.MsgType=iType;
		this.MsgOperate=iOperate;	
	}
	
	public BtInstructionInfo(BtInstructionType iType,Object oInputData){
		this.MsgType=iType;
		this.InputData=oInputData;	
	}
	
	public BtInstructionInfo(BtInstructionType iType,int iOperate,int iTime){
		this.MsgType=iType;
		this.MsgOperate=iOperate;
		this.MsgTime=iTime;
	}
}
