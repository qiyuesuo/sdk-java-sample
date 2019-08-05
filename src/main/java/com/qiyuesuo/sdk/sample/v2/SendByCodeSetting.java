package com.qiyuesuo.sdk.sample.v2;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.sample.v1.StandardSignSample;
import com.qiyuesuo.sdk.v2.SdkClient;
import com.qiyuesuo.sdk.v2.bean.Action;
import com.qiyuesuo.sdk.v2.bean.Contract;
import com.qiyuesuo.sdk.v2.bean.Signatory;
import com.qiyuesuo.sdk.v2.bean.Stamper;
import com.qiyuesuo.sdk.v2.bean.TemplateParam;
import com.qiyuesuo.sdk.v2.bean.User;
import com.qiyuesuo.sdk.v2.http.StreamFile;
import com.qiyuesuo.sdk.v2.json.JSONUtils;
import com.qiyuesuo.sdk.v2.param.SignParam;
import com.qiyuesuo.sdk.v2.request.ContractAuditRequest;
import com.qiyuesuo.sdk.v2.request.ContractDraftRequest;
import com.qiyuesuo.sdk.v2.request.ContractPageRequest;
import com.qiyuesuo.sdk.v2.request.ContractSendRequest;
import com.qiyuesuo.sdk.v2.request.ContractSignCompanyRequest;
import com.qiyuesuo.sdk.v2.request.ContractSignLpRequest;
import com.qiyuesuo.sdk.v2.request.DocumentAddByFileRequest;
import com.qiyuesuo.sdk.v2.request.DocumentAddByTemplateRequest;
import com.qiyuesuo.sdk.v2.response.ContractPageResult;
import com.qiyuesuo.sdk.v2.response.DocumentAddResult;
import com.qiyuesuo.sdk.v2.response.SdkResponse;

public class SendByCodeSetting {

	private static final Logger logger = LoggerFactory.getLogger(StandardSignSample.class);
	private static final String PLATFORM_NAME = "大头橙橙汁公司";

	@Bean
	public SdkClient sdkClient() {
		String url = "https://openapi.qiyuesuo.cn";
		String accessKey = "替换为开放平台申请的App Secret";
		String accessSecret = "替换为开放平台申请的App Token";
		return new SdkClient(url, accessKey, accessSecret);
	}

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(SendByCodeSetting.class, args);
		context.registerShutdownHook();
		context.start();
		SdkClient client = context.getBean(SdkClient.class);

		/**
		 * 根据代码配置进行合同发起与签署
		 * 该场景模拟一个人事合同的场景，即平台方公司与员工签署合同，平台方公司先签署，员工后签
		 */
		SdkResponse<Contract> draft = draft(client);
		logger.info("创建合同草稿成功，合同ID：{}", draft.getResult().getId());
		Long contractId = draft.getResult().getId();
		SdkResponse<DocumentAddResult> fileAddResult = addDocumentByFile(client, contractId);
		Long documentId1 = fileAddResult.getResult().getDocumentId();
		logger.info("根据文件添加合同文档成功，文档ID：{}", documentId1);
		SdkResponse<DocumentAddResult> templateAddResult = addDocumentByTemplate(client, contractId);
		Long documentId2 = templateAddResult.getResult().getDocumentId();
		logger.info("根据模板添加合同文档成功，文档ID：{}", documentId2);
		send(client, draft.getResult(), documentId1, documentId2);
		logger.info("合同发起成功");
		companySealSign(client, contractId, documentId1, documentId2);
		logger.info("公章签署成功");
		lpSign(client, contractId, documentId1, documentId2);
		logger.info("法人章签署成功");
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
	 * 配置合同草稿
	 * 
	 * @param client
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse<Contract> draft(SdkClient client) throws Exception {
		Contract draftContract = new Contract();
		draftContract.setSubject("合同主题名称");
		// 设置合同接收方
		// 公司接收方，设置签署流程：（1）合同审批，并指定审批人（2）公章签署（3）法人章签署
		Signatory platformSignatory = new Signatory();
		platformSignatory.setTenantType("COMPANY");
		platformSignatory.setTenantName(PLATFORM_NAME);
		platformSignatory.setSerialNo(1);
		platformSignatory.setReceiver(new User("17621699044", "MOBILE"));
		// 合同公章签署流程
		Action sealAction = new Action("COMPANY", 1);
		sealAction.setSealId(2490828768980361630L);
		platformSignatory.addAction(sealAction);
		// 合同法人章签署流程
		platformSignatory.addAction(new Action("LP", 2));
		draftContract.addSignatory(platformSignatory);
		// 个人签署方
		Signatory persoanlSignatory = new Signatory();
		persoanlSignatory.setTenantType("PERSONAL");
		persoanlSignatory.setTenantName("邓茜茜");
		persoanlSignatory.setSerialNo(2);
		persoanlSignatory.setReceiver(new User("15021504325", "MOBILE"));
		draftContract.addSignatory(persoanlSignatory);
		// 设置合同过期时间
		draftContract.setExpireTime("2020-07-28 23:59:59");
		draftContract.setSend(false); // 不发起合同

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
	 * 根据本地文件添加合同文档
	 * 
	 * @param client
	 * @param contractId
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse<DocumentAddResult> addDocumentByFile(SdkClient client, Long contractId)
			throws Exception {
		// 进入本地文件
		StreamFile file = new StreamFile(
				new FileInputStream(new File("C:\\Users\\Richard Cheung\\Documents\\契约锁\\测试\\AA.pdf")));

		String response = null;
		try {
			// PDF为本地文件的类型，请修改为对应的本地文件类型
			response = client.service(new DocumentAddByFileRequest(contractId, file, "pdf", "由文件创建文档"));
		} catch (Exception e) {
			throw new Exception("根据文件添加文档请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse<DocumentAddResult> sdkResponse = JSONUtils.toQysResponse(response, DocumentAddResult.class);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("根据文件添加文档失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}

	/**
	 * 根据在线模板添加合同文档
	 * 
	 * @param client
	 * @param contractId
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse<DocumentAddResult> addDocumentByTemplate(SdkClient client, Long contractId)
			throws Exception {
		// 若模板为参数模板，填充模板参数
		List<TemplateParam> templateParams = new ArrayList<TemplateParam>();
		templateParams.add(new TemplateParam("接收方1", "电子合同"));
		templateParams.add(new TemplateParam("接收方2", "契约锁"));
		Long templateId = 2492236993899110515L;
		String response = null;
		try {
			response = client
					.service(new DocumentAddByTemplateRequest(contractId, templateId, templateParams, "根据模板创建文档"));
		} catch (Exception e) {
			throw new Exception("根据模板添加文档请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse<DocumentAddResult> sdkResponse = JSONUtils.toQysResponse(response, DocumentAddResult.class);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("根据模板添加文档失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}

	/**
	 * 发起合同，并指定签署位置
	 * 
	 * @param client
	 * @param draft
	 * @param documentId1
	 * @param documentId2
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse send(SdkClient client, Contract draft, Long documentId1, Long documentId2)
			throws Exception {
		// 获取SignatoryId与ActionId，用于指定签署位置，公司签署位置需要指定ActionId,个人签署位置需要指定SignatoryId
		Long platformSignatoryId = null;
		Long personalSignatoryId = null;
		Long companySealActionId = null;
		Long lpSealActionId = null;
		for (Signatory signatory : draft.getSignatories()) {
			// 获取平台方SignatoryId，以及对应的公章签署ActionId和法人章签署ActionId
			if (signatory.getTenantName().equals(PLATFORM_NAME) && signatory.getTenantType().equals("COMPANY")) {
				platformSignatoryId = signatory.getId();
				for (Action action : signatory.getActions()) {
					if (action.getType().equals("COMPANY")) {
						companySealActionId = action.getId();
					}
					if (action.getType().equals("LP")) {
						lpSealActionId = action.getId();
					}
				}
			}
			// 获取个人签署方（本例只有一个个人签署方）
			if (signatory.getTenantType().equals("PERSONAL")) {
				personalSignatoryId = signatory.getId();
			}
		}
		// 指定平台方签署位置
		// 公章签署位置
		Stamper sealStamper = new Stamper();
		sealStamper.setType("COMPANY");
		sealStamper.setActionId(companySealActionId);
		sealStamper.setDocumentId(documentId1);
		sealStamper.setOffsetX(0.2);
		sealStamper.setOffsetY(0.3);
		sealStamper.setPage(1);

		// 法人章签署位置
		Stamper lpStamper = new Stamper();
		lpStamper.setType("LP");
		lpStamper.setActionId(lpSealActionId);
		lpStamper.setDocumentId(documentId1);
		lpStamper.setOffsetX(0.7);
		lpStamper.setOffsetY(0.1);
		lpStamper.setPage(1);

		// 公章时间戳签署位置
		Stamper timeStamper = new Stamper();
		timeStamper.setType("TIMESTAMP");
		timeStamper.setActionId(companySealActionId);
		timeStamper.setDocumentId(documentId1);
		timeStamper.setOffsetX(0.4);
		timeStamper.setOffsetY(0.7);
		timeStamper.setPage(1);

		// 个人签名位置
		Stamper personalStamper = new Stamper();
		personalStamper.setType("PERSONAL");
		personalStamper.setSignatoryId(personalSignatoryId);
		personalStamper.setDocumentId(documentId1);
		personalStamper.setOffsetX(0.6);
		personalStamper.setOffsetY(0.6);
		personalStamper.setPage(1);

		String response = null;
		try {
			response = client.service(new ContractSendRequest(draft.getId(),
					Arrays.asList(sealStamper, lpStamper, timeStamper, personalStamper)));
		} catch (Exception e) {
			throw new Exception("发起合同请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse sdkResponse = JSONUtils.toQysResponse(response);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("发起合同失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}

	/**
	 * 合同审批，若不通过审批则合同会退回
	 * 
	 * @param client
	 * @param contractId
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse audit(SdkClient client, Long contractId) throws Exception {
		String response = null;
		try {
			response = client.service(
					new ContractAuditRequest(contractId, true, "符合要求，审批通过"));
		} catch (Exception e) {
			throw new Exception("审批合同请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse sdkResponse = JSONUtils.toQysResponse(response);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("审批合同失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}

	/**
	 * 公司公章签署
	 * 若指定了公章签署位置，则使用指定的配置
	 * 若未指定签署位置，需要在签署时指定签署位置
	 * 
	 * @param client
	 * @param contractId
	 * @param documentId1
	 * @param documentId2
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse companySealSign(SdkClient client, Long contractId, Long documentId1, Long documentId2)
			throws Exception {
		SignParam param = new SignParam();
		param.setContractId(contractId);
		/*
		 * //公章签署位置
		 * Stamper sealStamper=new Stamper();
		 * sealStamper.setType("COMPANY");
		 * sealStamper.setDocumentId(documentId1);
		 * sealStamper.setSealId(2490828768980361630L);
		 * sealStamper.setOffsetX(0.2);
		 * sealStamper.setOffsetY(0.4);
		 * sealStamper.setPage(1);
		 * //时间戳签署位置
		 * Stamper timeStamper=new Stamper();
		 * timeStamper.setType("TIMESTAMP");
		 * timeStamper.setDocumentId(documentId1);
		 * timeStamper.setOffsetX(0.3);
		 * timeStamper.setOffsetY(0.2);
		 * timeStamper.setPage(1);
		 * //骑缝章签署位置
		 * Stamper acrossStamper=new Stamper();
		 * acrossStamper.setType("ACROSS_PAGE");
		 * acrossStamper.setDocumentId(documentId1);
		 * acrossStamper.setSealId(2490828768980361630L);
		 * acrossStamper.setOffsetY(0.4);
		 * param.setStampers(Arrays.asList(sealStamper,timeStamper,acrossStamper));
		 */
		String response = null;
		try {
			response = client.service(new ContractSignCompanyRequest(param));
		} catch (Exception e) {
			throw new Exception("公章签署请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse sdkResponse = JSONUtils.toQysResponse(response);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("公章签署失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}

	/**
	 * 公司法人章签署
	 * 若指定了法人章章签署位置，则使用指定的配置
	 * 若未指定签署位置，需要在签署时指定签署位置
	 * 
	 * @param client
	 * @param contractId
	 * @param documentId1
	 * @param documentId2
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse lpSign(SdkClient client, Long contractId, Long documentId1, Long documentId2)
			throws Exception {
		SignParam param = new SignParam();
		param.setContractId(contractId);
		/*
		 * //法人章签署位置
		 * Stamper lpStamper=new Stamper();
		 * lpStamper.setType("LP");
		 * lpStamper.setDocumentId(documentId1);
		 * lpStamper.setOffsetX(0.8);
		 * lpStamper.setOffsetY(0.2);
		 * lpStamper.setPage(1);
		 * //时间戳签署位置
		 * Stamper timeStamper=new Stamper();
		 * timeStamper.setType("TIMESTAMP");
		 * timeStamper.setDocumentId(documentId1);
		 * timeStamper.setOffsetX(0.9);
		 * timeStamper.setOffsetY(0.5);
		 * timeStamper.setPage(1);
		 * param.setStampers(Arrays.asList(lpStamper,timeStamper));
		 */
		String response = null;
		try {
			response = client.service(new ContractSignLpRequest(param));
		} catch (Exception e) {
			throw new Exception("法人章签署请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse sdkResponse = JSONUtils.toQysResponse(response);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("法人章签署失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}

	/**
	 * 生成签署链接
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
