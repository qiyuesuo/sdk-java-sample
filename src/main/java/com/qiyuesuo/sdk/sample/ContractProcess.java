package com.qiyuesuo.sdk.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qiyuesuo.sdk.v2.SdkClient;
import com.qiyuesuo.sdk.v2.bean.Category;
import com.qiyuesuo.sdk.v2.bean.Contract;
import com.qiyuesuo.sdk.v2.bean.Signatory;
import com.qiyuesuo.sdk.v2.bean.TemplateParam;
import com.qiyuesuo.sdk.v2.bean.User;
import com.qiyuesuo.sdk.v2.json.JSONUtils;
import com.qiyuesuo.sdk.v2.param.SignParam;
import com.qiyuesuo.sdk.v2.request.ContractDraftRequest;
import com.qiyuesuo.sdk.v2.request.ContractPageRequest;
import com.qiyuesuo.sdk.v2.request.ContractSignCompanyRequest;
import com.qiyuesuo.sdk.v2.response.ContractPageResult;
import com.qiyuesuo.sdk.v2.response.SdkResponse;

/**
 * 创建合同草稿-发起合同-签署方签署-完成签署
 * @author Administrator
 *
 */
public class ContractProcess {

	private static final Logger logger = LoggerFactory.getLogger(ContractProcess.class);
	private static final String PLATFORM_NAME = "HZK有限公司";
	
	public static void main(String[] args) throws Exception {
		String url = "更换为开放平台请求地址";
		String accessKey = "更换为您开放平台 App Token";
		String accessSecret = "更换为您开放平台App Secret";
		SdkClient client = new SdkClient(url, accessKey, accessSecret);

		//根据业务分类配置进行合同发起，文件、签署流程、签署公章、签署位置均在业务分类中维护	
		SdkResponse<Contract> draft = draft(client);
		
		Long contractId = draft.getResult().getId();
		//合同发起后，按照业务分类中的设置依次签署合同
		//发起方公司签署(按照发起方预设流程签署公司公章)
		companySign(client,contractId);
		//接收方签署(登录契约锁云平台进行签署/生成内嵌页面签署链接进行签署（下方生成的链接）)
		SdkResponse<ContractPageResult> pageResponse = gerenateSignUrl(client, draft.getResult().getId());
		logger.info("生成签署链接成功，签署地址：{}", pageResponse.getResult().getPageUrl());
	}
	
	/*
	 * 根据业务分类创建合同
	 * 业务分类包括了：
	 * 		文件主题subject(自定义/按规则生成),
	 * 		签署方signatory-包含发起方及接收方流程(非预设时创建合同必传/预设时规定了签署方的数量、类型、顺序)
	 * 									(发起方有自己的审批流程-接收方预设则会使用业务分类中配置的签署流程、签署位置、印章等)
	 * 			审批流程action(发起方预设-使用预设的签署流程、签署位置、印章/非预设则必传)(常见流程：公司公章签署/法人章签署/经办人签字/个人审批)
	 * 						(接收方非预设-必传/预设-在签署方流程中)
	 * 		文件模板documentId(指定则包含该模板/未指定则主动上传)
	 * 						(本地文件及在线文件-可带参数，带参数时必传参数值)
	 * 		实名认证要求(发起方与接收方)
	 * 		签署有效期
	 */
	private static SdkResponse<Contract> draft(SdkClient client) throws Exception {
		Contract draftContract = new Contract();
		//设置合同主题subject
		draftContract.setSubject("合同主题");
		//选择业务分类(不传则取默认业务分类-根据业务分类设置来决定签署方，参数模板参数等设置)
		//以下业务分类包含发起方-企业接收方-个人接收方，文件模板中包含两个参数
		draftContract.setCategory(new Category(2635406141512749699L)); // 设置业务分类ID,套用该业务分类的配置
		
		draftContract.addTemplateParam(new TemplateParam("参数1", "这是参数1对应的值")); // 若业务分类中包含参数模板，设置参数内容
		draftContract.addTemplateParam(new TemplateParam("参数2", "这是参数2对应的值"));
		
		// 设置接收方详情，接收方需要与业务分类中设置的签署方流程匹配
		// 公司签署方
		Signatory companySignatory1 = new Signatory();
		companySignatory1.setTenantName(PLATFORM_NAME);
		companySignatory1.setTenantType("COMPANY");
		companySignatory1.setReceiver(new User("10010001001", "MOBILE"));
		companySignatory1.setSerialNo(1);
		draftContract.addSignatory(companySignatory1);
		// 公司签署方
//		Signatory companySignatory2 = new Signatory();
//		companySignatory2.setTenantName(PLATFORM_NAME);
//		companySignatory2.setTenantType("COMPANY");
//		companySignatory2.setReceiver(new User("10010001001", "MOBILE"));
//		companySignatory2.setSerialNo(2);
//		draftContract.addSignatory(companySignatory2);
		// 个人签署方
		Signatory personalSignatory = new Signatory();
		personalSignatory.setTenantType("PERSONAL");
		personalSignatory.setReceiver(new User("10010001002", "MOBILE"));
		personalSignatory.setTenantName("个人用户");
		personalSignatory.setSerialNo(3);
		draftContract.addSignatory(personalSignatory);
		
		draftContract.setSend(true); // 创建合同草稿并发起合同

		String response = null;
		try {
			response = client.service(new ContractDraftRequest(draftContract));
		} catch (Exception e) {
			throw new Exception("创建合同草稿请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse<Contract> sdkResponse = JSONUtils.toQysResponse(response, Contract.class);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("创建合同草稿失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}
	
	/**
	 * 公司公章签署
	 * @param client
	 * @param contractId
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse companySign(SdkClient client, Long contractId) throws Exception {
		SignParam param = new SignParam();
		param.setContractId(contractId);

		String response = null;
		try {
			response = client.service(new ContractSignCompanyRequest(param));
		} catch (Exception e) {
			throw new Exception("公章签署请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse sdkResponse = JSONUtils.toQysResponse(response, Object.class);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("公章签署失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}
	
	/**
	 * 生成用户的签署链接
	 * 
	 * @param client
	 * @param contractId
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse<ContractPageResult> gerenateSignUrl(SdkClient client, Long contractId) throws Exception {
		//用户信息需和发起时一致
		User signUser = new User("10010001001", "MOBILE");
		ContractPageRequest request = new ContractPageRequest(contractId, signUser, null);

		String response;
		try {
			response = client.service(request);
		} catch (Exception e) {
			throw new Exception("生成签署链接请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse<ContractPageResult> sdkResponse = JSONUtils.toQysResponse(response, ContractPageResult.class);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("签署失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}
	
	
}
