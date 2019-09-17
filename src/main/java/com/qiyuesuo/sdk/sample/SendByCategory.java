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

public class SendByCategory {

	private static final Logger logger = LoggerFactory.getLogger(SendByCategory.class);
	private static final String PLATFORM_NAME = "大头橙橙汁公司";

	public static void main(String[] args) throws Exception {
		String url = "https://openapi.qiyuesuo.cn";
		String accessKey = "更换为您开放平台 App Token";
		String accessSecret = "更换为您开放平台App Secret";
		SdkClient client = new SdkClient(url, accessKey, accessSecret);

		/**
		 * 根据业务分类配置进行合同发起，文件、签署流程、签署公章、签署位置均在业务分类中维护
		 * 该场景模拟一个人事合同的场景，即平台方公司与员工签署合同，平台方公司先签署，员工后签
		 */
		SdkResponse<Contract> draft = draft(client);
		logger.info("创建合同草稿成功，并发起，合同ID：{}", draft.getResult().getId());
		companySealSignByCategoryConfig(client, draft.getResult().getId());
		logger.info("公章签署成功");
		/**
		 * 平台方签署完成，签署方签署可采用
		 * （1）接收短信的方式登录契约锁云平台进行签署
		 * （2）生成内嵌页面签署链接进行签署（下方生成的链接）
		 * （3）JS-SDK签署（仅支持个人）
		 */
		SdkResponse<ContractPageResult> pageResponse = gerenateSignUrl(client, draft.getResult().getId());
		logger.info("生成签署链接成功，签署地址：{}", pageResponse.getResult().getPageUrl());
	}

	/**
	 * 根据业务分类配置创建合同草稿并发起合同
	 * 
	 * @param client
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse<Contract> draft(SdkClient client) throws Exception {
		Contract draftContract = new Contract();
		draftContract.setSubject("合同主题");
		draftContract.setCategory(new Category(2584280095237849689L)); // 设置业务分类ID,套用该业务分类的配置
		draftContract.addTemplateParam(new TemplateParam("接收方2", "发起方填参")); // 若业务分类中包含参数模板，设置参数内容
		draftContract.addTemplateParam(new TemplateParam("接收方1", "发起方填参"));
		// 设置接收方详情，接收方需要与业务分类中设置的签署方流程匹配
		// 公司签署方
		Signatory companySignatory = new Signatory();
		companySignatory.setTenantName(PLATFORM_NAME);
		companySignatory.setTenantType("COMPANY");
		companySignatory.setReceiver(new User("17621699044", "MOBILE"));
		companySignatory.setSerialNo(1);
		draftContract.addSignatory(companySignatory);
		// 个人签署方
		Signatory personalSignatory = new Signatory();
		personalSignatory.setTenantType("PERSONAL");
		personalSignatory.setReceiver(new User("15021504325", "MOBILE"));
		personalSignatory.setTenantName("邓茜茜");
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
	 * 根据业务分类配置的签署位置与印章签署公章
	 * 若未配置签署位置需要设置签署位置与印章
	 * 
	 * @param client
	 * @param contractId
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse companySealSignByCategoryConfig(SdkClient client, Long contractId) throws Exception {
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
		User signUser = new User("15021504325", "MOBILE");
		String callbackPage = "https://www.qiyuesuo.com";
		ContractPageRequest request = new ContractPageRequest(contractId, signUser, callbackPage);

		String response;
		try {
			response = client.service(request);
		} catch (Exception e) {
			throw new Exception("生成签署链接请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse<ContractPageResult> sdkResponse = JSONUtils.toQysResponse(response, ContractPageResult.class);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("法人章签署失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}

}
