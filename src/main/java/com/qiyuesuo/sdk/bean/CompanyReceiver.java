package com.qiyuesuo.sdk.bean;

/**
 * 公司接收方信息
 */
public class CompanyReceiver {
	private String tenantName = "接收方公司"; // 接收方名称
	private String receiverContact = "18666666666"; // 经办人联系方式
	private String receiverName = "李四"; // 经办人姓名
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
	public String getReceiverName() {
		return receiverName;
	}
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

}
