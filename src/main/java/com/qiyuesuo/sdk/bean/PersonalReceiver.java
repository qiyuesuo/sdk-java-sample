package com.qiyuesuo.sdk.bean;

/**
 * 个人接收方信息
 */
public class PersonalReceiver {
	private String tenantName = "张三"; // 接收人姓名
	private String receiverContact = "18600000000"; // 接收人联系方式
	private String contactType = "MOBILE"; //联系方式类型
	private String TenantType = "PERSONAL";
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	public String getReceiverContact() {
		return receiverContact;
	}
	public void setReceiverContact(String receiverContact) {
		this.receiverContact = receiverContact;
	}
	public String getContactType() {
		return contactType;
	}
	public void setContactType(String contactType) {
		this.contactType = contactType;
	}
	public String getTenantType() {
		return TenantType;
	}
	public void setTenantType(String tenantType) {
		TenantType = tenantType;
	}

}
