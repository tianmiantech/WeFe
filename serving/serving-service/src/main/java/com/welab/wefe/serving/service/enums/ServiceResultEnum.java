package com.welab.wefe.serving.service.enums;

public enum ServiceResultEnum {

	SUCCESS(1, "成功"), NO_DATA(2, "没有数据"), SERVICE_NOT_AVALIABLE(3, "服务不可用"), CUSTOMER_NOT_AUTHORITY(4, "服务未授权"),
	IP_NOT_AUTHORITY(5, "IP被限制"), SERVICE_FAIL(6, "服务异常");

	private int code;
	private String message;

	ServiceResultEnum(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
