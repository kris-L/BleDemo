/**
 * @Title RecvDataListener.java
 * @Package com.gci.car.terminal.interfaces
 * @Description
 * @author 陈国宏
 * @date 2014年11月4日 上午9:49:14
 * @version V1.0
 */
package com.xw.ble;

/**
 * @ClassName RecvDataListener
 * @Description
 * @author kris lau
 * @date 2017年11月23日 
 */
public interface RecvDataListener {

	/**
	 * @Title setData
	 * @Description 处理一条完整的指令
	 * @author kris lau
	 * @param data
	 */
	public void setData(String data);
}
