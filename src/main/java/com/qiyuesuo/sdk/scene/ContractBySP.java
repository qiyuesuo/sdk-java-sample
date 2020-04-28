package com.qiyuesuo.sdk.scene;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qiyuesuo.sdk.bean.PersonalReceiver;
import com.qiyuesuo.sdk.bean.Sender;
import com.qiyuesuo.sdk.v2.SdkClient;
import com.qiyuesuo.sdk.v2.bean.Category;
import com.qiyuesuo.sdk.v2.bean.Contract;
import com.qiyuesuo.sdk.v2.bean.Signatory;
import com.qiyuesuo.sdk.v2.bean.TemplateParam;
import com.qiyuesuo.sdk.v2.bean.User;
import com.qiyuesuo.sdk.v2.json.JSONUtils;
import com.qiyuesuo.sdk.v2.request.ContractDraftRequest;
import com.qiyuesuo.sdk.v2.request.ContractPageRequest;
import com.qiyuesuo.sdk.v2.request.DocumentDownloadRequest;
import com.qiyuesuo.sdk.v2.response.ContractPageResult;
import com.qiyuesuo.sdk.v2.response.SdkResponse;
import com.qiyuesuo.sdk.v2.utils.IOUtils;

/**
 * 场景：对接方-个人
 * 发起方自动盖章
 * 固定模板-带参模板
 * 
 */
public class ContractBySP {
	
	private static final Logger logger = LoggerFactory.getLogger(ContractBySP.class);
	
	public static void main(String[] args) throws Exception {
		//1.初始化sdkClient：token和secret
		SdkClient client = initClient();
		//2.创建合同草稿
		Sender sender = new Sender();
		PersonalReceiver receiver = new PersonalReceiver();
		SdkResponse<Contract> draft = draft(client,"带参模板--接收方--个人",sender,receiver);
		logger.info("创建合同草稿成功，合同ID：{}", draft.getResult().getId());
		Long contractId = draft.getResult().getId();
		//3.获取个人签署页面链接
		//当前签署人
		User signUser = new User(receiver.getReceiverContact(),receiver.getContactType());
		//签署完成跳转的页面地址
		String callbackPage = "https://www.baidu.com";
		SdkResponse<ContractPageResult> pageResult = getSignUrl(client,contractId,signUser,callbackPage);
		String signURL = pageResult.getResult().getPageUrl();
		logger.info("获取签署链接成功，URL：{}", signURL);
		//4.下载合同文档
		Long documentId  = draft.getResult().getDocuments().get(0).getId();
		String path = "E:/NoSign.pdf";
		downloadDocument(client,documentId,path);
		logger.info("下载合同文档成功");	 
	}
 	
	/**
	 * 初始化client
	 * @return
	 */
	private static SdkClient initClient() {
		//token和secret信息来自开放平台控制台
		String url = "替换为开放平台请求地址";
		String accessKey = "替换成开放平台申请的token";
		String accessSecret = "替换成开放平台申请的secret";
		return new SdkClient(url, accessKey, accessSecret);
	}
	
	/**
	 * 创建合同草稿
	 * @param client
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse<Contract> draft(SdkClient client,String subject, Sender sender,PersonalReceiver receiver) throws Exception {
		Contract draftContract = new Contract();
		draftContract.setSubject(subject);
		//draftContract.setBizId("csp12345678");//对接方业务分类的唯一标识，如果没有可以不传
		draftContract.setSend(true); //创建合同草稿并发起合同
		draftContract.setCategory(new Category(sender.getCategoryId())); // 设置业务分类ID,套用该业务分类的配置
		draftContract.addTemplateParam(new TemplateParam("参数1", "参数1的值")); // 若业务分类中包含参数模板，设置参数内容
		draftContract.addTemplateParam(new TemplateParam("参数2", "参数2的值"));
		//设置创建人的账号（草稿状态的合同只有创建人可以查看）
		draftContract.setCreator(new User(sender.getCreatorContact(), sender.getContactType()));
		// 设置接收方详情，对应业务分类中预设流程（发起方->个人)
		Signatory senderSignatory = new Signatory();
		senderSignatory.setTenantName(sender.getTenantName());
		senderSignatory.setTenantType(sender.getTenantType());
		senderSignatory.setReceiver(new User(sender.getCreatorContact(), sender.getContactType()));//经办人的联系方式
		draftContract.addSignatory(senderSignatory);
		// 个人签署方
		Signatory personalSignatory = new Signatory();
		personalSignatory.setTenantType(receiver.getTenantType());
		personalSignatory.setReceiver(new User(receiver.getReceiverContact(), receiver.getContactType()));
		personalSignatory.setTenantName(receiver.getTenantName());
		draftContract.addSignatory(personalSignatory);
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
	 * 生成签署链接
	 * 
	 * @param client
	 * @param contractId
	 * @return
	 * @throws Exception
	 */
	private static SdkResponse<ContractPageResult> getSignUrl(SdkClient client, Long contractId,User signUser,String callbackPage) throws Exception {
		ContractPageRequest request = new ContractPageRequest(contractId, signUser, callbackPage);
		String response;
		try {
			response = client.service(request);
		} catch (Exception e) {
			throw new Exception("生成签署链接请求服务器失败，失败原因：" + e.getMessage());
		}
		SdkResponse<ContractPageResult> sdkResponse = JSONUtils.toQysResponse(response, ContractPageResult.class);
		if (!sdkResponse.getCode().equals(0)) {
			throw new Exception("生成签署链接失败，失败原因：" + sdkResponse.getMessage());
		}
		return sdkResponse;
	}
	
	/**
	 * 下载合同文档
	 * @throws FileNotFoundException 
	 */
	private static void downloadDocument(SdkClient client,Long documentId,String path) throws FileNotFoundException {
		 DocumentDownloadRequest request = new DocumentDownloadRequest(documentId);
		 FileOutputStream fos = new FileOutputStream(path);
		 client.download(request, fos);
		 IOUtils.safeClose(fos);

	}
}
