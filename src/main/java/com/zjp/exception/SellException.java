package com.zjp.exception;

public class SellException extends Exception {

	/**
	 * 错误编码
	 */
	private int errorCode;

	/**
	 * 消息是否为属性文件中的Key
	 */
	private boolean propertiesKey = true;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public boolean isPropertiesKey() {
		return propertiesKey;
	}

	public void setPropertiesKey(boolean propertiesKey) {
		this.propertiesKey = propertiesKey;
	}

	public SellException(int errorCode, String message) {
		this(errorCode, message, true);
	}

	public SellException(int errorCode, String message, boolean propertiesKey) {
		super(message);
		this.setErrorCode(errorCode);
		this.setPropertiesKey(propertiesKey);
	}
}
