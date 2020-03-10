package com.qiyuesuo.sdk.bean;

public class Sender {
	private Long categoryId = 2591576924778139892L; // 业务分类ID
	private Long sealId = 2620527506041606739L; // 印章ID
	private String tenantName = "发起方公司名称"; // 发起方公司名称
	private String TenantType = "COMPANY";
	private String creatorContact = "11000000000"; // 合同发起人联系方式
	private String creatorName = "张三"; // 合同发起人姓名
	private String contactType = "MOBILE";//联系方式类型
	
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	public Long getSealId() {
		return sealId;
	}
	public void setSealId(Long sealId) {
		this.sealId = sealId;
	}
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	
	public String getTenantType() {
		return TenantType;
	}
	public void setTenantType(String tenantType) {
		TenantType = tenantType;
	}
	public String getCreatorContact() {
		return creatorContact;
	}
	public void setCreatorContact(String creatorContact) {
		this.creatorContact = creatorContact;
	}
	public String getCreatorName() {
		return creatorName;
	}
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	public String getContactType() {
		return contactType;
	}
	public void setContactType(String contactType) {
		this.contactType = contactType;
	}
	
}
