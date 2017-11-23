package com.xw.ble.instruct;

/**
* @ClassName InstructionType 
* @Description 指令类型
* @author kris
* @date 2017-08-28 上午 10:00:04
 */
public enum BtInstructionType {
	/**
	 * 读取外围设备版本号,00-00
	 */
	Peripheral_Version,
	/**
	 * 心跳包,10-01
	 */
	MCUMTK_MSG,
	
	/**
	 * 输入输出设备读取,10-10
	 */
	InputOutputDevice_Read,
	/**
	 * 读取安全带输入状态,10-30
	 */
	Safty_Belt_Status,
	/**
	 * 读取ch输入状态,10-31
	 */
	CH_Status,
	/**
	 * 读取脚刹车输入状态,10-32
	 */
	Foot_Brake_Status,
	/**
	 * 读取Door负输入状态,10-33
	 */
	Minus_Door_Status,
	/**
	 * 读取Brake输入状态,10-34
	 */
	Brake_Status,
	/**
	 * 读取大灯的输入状态输入状态,10-35
	 */
	Headlight_State,
	/**
	 * 读取F_Light(远光灯)输入状态,10-36
	 */
	F_Light_Status,
	/**
	 * 读取L_Light(左转向灯)输入状态,10-37
	 */
	L_Light_Status,
	/**
	 * 读取R_Light(右转向灯)输入状态,10-38
	 */
	R_Light_Status,
	/**
	 * 读取Speaker输入状态,10-39
	 */
	Speaker_Status,
	/**
	 * 读取ACC输入状态,10-3A
	 */
	ACC_Status,
	/**
	 * 读取Reverse(倒车)输入状态,10-3B
	 */
	Reverse_Status,
	/**
	 * 读取def的输入状态,10-3C
	 */
	DEF_Status,
	/**
	 * 读取door正输入状态,10-3D
	 */
	Plus_DOOR_Status,
	/**
	 * 读取hand输入状态,10-3E
	 */
	Hand_State,
	/**
	 * 读取负触发输入状态,10-3F
	 */
	Negative_Trigger,
	/**
	 * 油料的数量百分比,10-61
	 */
	Gasoline_Percent,
	/**
	 * MCU执行指纹比对,10-62
	 */
	Contrast_Fingerprint,
	/**
	 * MCU给安卓主机提示卡插入或拔出,10-63
	 */
	Prompt_Card_Status,
	/**
	 * MCU提示读取IC卡故障代码,10-64
	 */
	IC_Card_ErrorCode,
	/**
	 * MCU读取喊话器和耳机状态,10-65
	 */
	MegaphoneHeadset_Status,
	/**
	 * MCU给安卓主机提示卡插入或拔出  10-66
	 */
	IC_Card_Status,
	/**
	 * 非接触式IC卡读指令 10-70
	 */
	Control_ReadNoncontactIC,
	/**
	 * 接触式IC卡读指令 10-71
	 */
	Control_ReadContactIC,
	/**
	 * 安卓主机控制功放电源输出,20-4A
	 */
	Control_AmplifierPower,
	/**
	 * 安卓主机控制高电压Lock输出,20-51
	 */
	Control_HighVoltageLock,
	/**
	 * 安卓主机控制高电压UnLock输出,20-52
	 */
	Control_HighVoltageUnlock,
	/**
	 * 安卓主机控制CTRL_ELE输出,20-53
	 */
	Control_CTRL_ELE,
	/**
	 * 安卓主机控制CuteOFF_L输出,20-54
	 */
	Control_CuteOFF_L,
	/**
	 * 安卓主机控制CuteOFF_H输出,20-55
	 */
	Control_CuteOFF_H,
	/**
	 * 安卓主机控制内置电池的供电输出,20-56
	 */
	Control_CellIn,
	/**
	 * 安卓主机控制UART3电源输出,20-57
	 */
	Control_PowerUART3,
	/**
	 * 安卓主机控制USB电源输出,20-58
	 */
	Control_PowerUSB,
	/**
	 * 安卓主机设置指纹比对等级,20-59
	 */
	Control_FingerprintLevel,
	/**
	 * 安卓主机控制音频功放,20-5A
	 */
	Control_Audio,
	/**
	 * 安卓主机控制,开启/关闭指纹设备,20-5B
	 */
	Control_FingerprintDevice,
	/**
	 * 安卓主机控制MCU外围IO输出,20-5F
	 */
	Control_IOOutputVoltage,
	/**
	 *安卓主机要求读取户的指纹信息,20-61
	 */
	Control_FingerprintAsk,
	/**
	 * 安卓主机查询喊话器和耳机选择状态,20-62
	 */
	Control_MegaphoneHeadset,
	/**
	 * 安卓主机查询MCU外围输入接口的状态,20-63
	 */
	ALL_Above,
	/**
	 * 安卓主机查询引擎转速RPM,20-64
	 */
	Control_EngineRPM,
	/**
	 * 安卓主机要求MCU把采集的指纹数据上传,20-65
	 */
	Control_FingerprintUpload,
	/**
	 * 安卓主机下发已有的指纹信息给MCU做比对应用,20-66
	 */
	Control_FingerprintCompare,
	/**
	 * 安卓主机下发指令（不含指纹信息数）给MCU做比对应用,20-67
	 */
	Control_FingerprintCompareNo,
	/**
	 * 非接触式IC写指令    20-70
	 */
	Control_WriteNoncontactIC,
	/**
	 *  接触式IC写指令   20-71
	 */
	Control_WritecontactIC,
	/**
	 * MCU外围串口1波特率设置,20-80
	 */
	Control_SerialPort1,
	/**
	 * MCU外围串口2波特率设置,20-81
	 */
	Control_SerialPort2,
	/**
	 * MCU外围串口3波特率设置,20-82
	 */
	Control_SerialPort3,
	/**
	 * MCU外围CAN总线波特率设置,20-83
	 */
	Control_SerialPortCAN,
	/**
	 * MCU外围RS485波特率设置,20-84
	 */
	Control_SerialPortRS485,
	/**
	 *  输入数据到RS485
	 */
	RS485_Input,
	/**
	 * MCU外围串口1输出数据,30-20
	 */
	Control_SendSerialPort1,
	/**
	 * MCU外围串口2输出数据,30-2
	 */
	Control_SendSerialPort2,
	/**
	 * MCU外围串口3输出数据,30-2
	 */
	Control_SendSerialPort3,
	/**
	 * MCU外围串口RS485输出数据,30-2
	 */
	Control_SendSerialPortRS485,
	/**
	 * 发送配对ID
	 */
	Control_SendPassword,
	/**
	 * 控制小信号负输出,20-5C
	 */
	Control_Signal_Negative_Output
}
