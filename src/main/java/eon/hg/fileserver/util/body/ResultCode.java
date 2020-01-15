package eon.hg.fileserver.util.body;

/**
 * 前端返回代码定义
 * @author eonook
 */
public enum ResultCode {

	SUCCESS(0, "处理成功"),
	FAILED(500, "处理失败"),
	USER_LOGIN_ERROR(500201, "登录失败，用户名或密码出错"),
	IDENTITY_FAIL(500401, "身份验证失败"),
	AUTHENTICATION_FAIL(500403, "未被授权的请求资源"),
	REQUEST_TIME_OUT(500408,"由于长时间未操作,空闲会话已超时"),
	FDFS_UNAVAILABLE(500510,"无法获取文件存储服务连接资源"),
	FDFS_IO_ERROR(500511,"客户端连接服务端出现了io异常"),
	FDFS_SERVER_ERROR(500512,"文件存储服务返回异常"),
	FILE_IO_ERROR(500410,"文件IO异常"),
	CUSTOMIZE_FAIL(500999, "自定义错误");

	private Integer code;
	
	private String msg;
	
	ResultCode(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
