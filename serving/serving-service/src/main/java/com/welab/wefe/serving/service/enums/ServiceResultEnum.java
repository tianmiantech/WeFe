package com.welab.wefe.serving.service.enums;

public enum ServiceResultEnum {

	SUCCESS(0, "成功"), SERVICE_FAIL(1, "服务异常"), NO_DATA(2, "没有数据"), SERVICE_NOT_AVALIABLE(3, "服务不可用"),
	CUSTOMER_NOT_AUTHORITY(4, "服务未授权"), IP_NOT_AUTHORITY(5, "IP被限制");

	private int code;
	private String message;

	ServiceResultEnum(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public static String getValueByCode(int code) {
		String result = null;
		switch (code) {
			case 0:
				result = ServiceResultEnum.SUCCESS.message;
				break;
			case 1:
				result = ServiceResultEnum.SERVICE_FAIL.message;
				break;
			case 2:
				result = ServiceResultEnum.NO_DATA.message;
				break;
			case 3:
				result = ServiceResultEnum.SERVICE_NOT_AVALIABLE.message;
				break;
			case 4:
				result = ServiceResultEnum.CUSTOMER_NOT_AUTHORITY.message;
				break;
			case 5:
				result = ServiceResultEnum.IP_NOT_AUTHORITY.message;
				break;
			default:
				break;
		}
		return result;
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
