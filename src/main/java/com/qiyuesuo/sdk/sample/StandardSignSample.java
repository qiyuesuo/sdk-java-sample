/**
 * Copyright (C) 2017,  上海亘岩网络科技有限公司 All rights reserved All rights reserved.
 */
package com.qiyuesuo.sdk.sample;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.SealService;
import com.qiyuesuo.sdk.api.StandardSignService;
import com.qiyuesuo.sdk.impl.SealServiceImpl;
import com.qiyuesuo.sdk.impl.StandardSignServiceImpl;
import com.qiyuesuo.sdk.signer.AuthLevel;
import com.qiyuesuo.sdk.standard.AddDocumentByFileRequest;
import com.qiyuesuo.sdk.standard.Category;
import com.qiyuesuo.sdk.standard.CreateByFileRequest;
import com.qiyuesuo.sdk.standard.CreateContractResponse;
import com.qiyuesuo.sdk.standard.ReceiveType;
import com.qiyuesuo.sdk.standard.Receiver;
import com.qiyuesuo.sdk.standard.SendRequest;
import com.qiyuesuo.sdk.standard.SignByLegalPersonRequest;
import com.qiyuesuo.sdk.standard.SignRequest;
import com.qiyuesuo.sdk.standard.StandardContract;
import com.qiyuesuo.sdk.standard.StandardSignType;
import com.qiyuesuo.sdk.standard.StandardStamper;
import com.qiyuesuo.sdk.standard.UserType;

/**
 * 
 * 契约锁 JAVA SDK 标准签实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数;</br>
 * @author wise
 * 
 */
@SpringBootApplication
public class StandardSignSample {

	private static final Logger logger = LoggerFactory.getLogger(StandardSignSample.class);

	private static StandardSignService standardSignService  = null;
	private static Long contractId = null; // 合同ID
	private static Long documentId1 = null; // 第一个合同文件ID
	private static Long documentId2 = null; // 第二个合同文件ID
	private static Long categoryId = 2278742364627402752l;// 合同分类ID，在契约锁云平台【业务分类】获取
	private static Long templateId = 2274828609178828800L; // 模板ID，在契约锁云平台【文件模板】中获取
	private static Long sealId = 2287533979868536880L; // 印章编号，在契约锁云平台【公司印章】中获取

	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext context = SpringApplication.run(StandardSignSample.class, args);
		context.registerShutdownHook();
		context.start();
		
		standardSignService  = context.getBean(StandardSignService.class);
		// ====================================================
		// 1、创建合同：可以用文件创建合同、用html文本创建合同、用模板创建合同
		CreateContractResponse response = create();
		contractId = response.getContractId();
		documentId1 = response.getDocumentId();
		logger.info("创建合同完成：contractId:{}, documentId1:{}", contractId, documentId1);
		// ====================================================
		// 2、添加合同文件：如果存在多份合同文件，可以调用此接口为合同添加文件
        documentId2 = addDocument();
		logger.info("添加文件完成：documentId2:{}", documentId2);
		// ====================================================
		// 3、发起合同
		send();
		logger.info("发起合同完成");
		// ====================================================
		// 4、合同法人章签署，法人章签署要在公章签署之前进行
		signByLegalPerson();
		logger.info("法人章签署完成");
		// ====================================================
		// 5、合同公章签署，运营方签署完成。
		sign();
		logger.info("公章签署完成");
		
		// ====================================================
		// 获取合同详情
		StandardContract contract = standardSignService.detail(contractId);
		logger.info("根据文档id获取详情完成:{}", contract);
		
		// ====================================================
		// 合同文件下载 （ZIP）
		FileOutputStream outputZip = new FileOutputStream("D:/sign/standard/outputZip.zip");
		standardSignService.download(contractId, outputZip);
		safeClose(outputZip);
		logger.info("标准签合同下载完成");
		
		
		// ====================================================
		// 合同文件下（PDF）
		FileOutputStream outputDoc = new FileOutputStream("D:/sign/standard/outputDoc.pdf");
		standardSignService.downloadDoc(documentId1, outputDoc);
		safeClose(outputDoc);
		logger.info("标准签合同下载完成");
		
		//=====================================================
		//查询合同分类
		List<Category> categories = standardSignService.queryCategory();
		logger.info("查询合同分类完成,合同分类数量:{}",categories.size());
	}
	
	/**
	 * 创建合同：创建合同时需要指定合同名称，且必须指定一个合同文件
	 * @return
	 * @throws FileNotFoundException
	 */
	private static CreateContractResponse create() throws FileNotFoundException {
		// 用文件创建合同
		CreateByFileRequest request = new CreateByFileRequest();
		request.setDocName("file_doc1");
		request.setSubject("标准签合同");
		InputStream inputStream = new FileInputStream(new File("D:/sign/standard/demo1.pdf"));
		request.setFile(inputStream);
		CreateContractResponse response = standardSignService.create(request);
		safeClose(inputStream);
		
//		// 用html文本创建合同
//		CreateByHtmlRequest request = new CreateByHtmlRequest();
//		request.setDocName("html_doc1");
//		request.setSubject("标准签合同");
//		request.setHtml("这是用HTML创建合同的HTML文本");
//		CreateContractResponse response = standardSignService.create(request);
		
//		// 用模板创建合同
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("param1", "value1");
//		params.put("param2", "value2");
//		CreateByTemplateRequest request = new CreateByTemplateRequest();
//		request.setDocName("template_doc1");
//		request.setSubject("标准签合同");
//		request.setTemplateId(templateId);
//		request.setTemplateParams(params);
//		CreateContractResponse response = standardSignService.create(request);
		
		return response;
	}
	
	/**
	 * 添加合同文件：如果合同有多份文件，可以调用“添加合同文件”接口来添加文件
	 * @return
	 * @throws FileNotFoundException
	 */
	private static Long addDocument() throws FileNotFoundException {

		// 用文件流添加合同文件
		AddDocumentByFileRequest request = new AddDocumentByFileRequest();
		request.setContractId(contractId);
		request.setTitle("file_doc2");
		InputStream inputStream = new FileInputStream(new File("D:/sign/standard/demo2.pdf"));
		request.setFile(inputStream);
		Long documentId = standardSignService.addDocument(request);
		safeClose(inputStream);

//		// 用HTML文本添加合同文件
//		AddDocumentByHtmlRequest request = new AddDocumentByHtmlRequest();
//		request.setContractId(contractId);
//		request.setTitle("html_doc2");
//		request.setHtml("这是用HTML添加合同文件的HTML文本");
//		Long documentId = standardSignService.addDocument(request);

//		// 用合同模板添加合同文件
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("param1", "value1");
//		params.put("param2", "value2");
//		AddDocumentByTemplateRequest request = new AddDocumentByTemplateRequest();
//		request.setTemplateId(templateId);
//		request.setTemplateParams(params);
//		request.setContractId(contractId);
//		request.setTitle("template_doc2");
//		Long documentId = standardSignService.addDocument(request);

		return documentId;
	}
	
	/**
	 * 发起合同，发起合同时需要指定以下信息：
	 * 合同接收方：分为“个人”、“公司”、“运营方”三种类型；可以选择是否指定签署方的签署位置，如果指定了签署位置，则签署方必须按照此位置签署；可以选择是否需要签署法人章
	 * 合同ID: 合同创建接口的返回值
	 * 合同分类: 合同分类在契约锁云平台的【业务分类】中维护，如果不指定合同分类，则默认为“默认合同分类”
	 * 合同接收顺序: 分为“顺序接收”、“同时接收”两种，顺序接收时签署方按指定的顺序依次接收合同并签署，同时接收时签署方同时接收到合同且签署顺序不固定
	 */
	private static void send() {
		// 合同接收方：运营方
		Receiver receiver1 = new Receiver();
		receiver1.setType(UserType.PLATFORM);
		receiver1.setOrdinal(1);
		receiver1.setLegalPersonRequired(true); // 指定是否需要签署法人章

		// 合同接收方：公司用户
		Receiver receiver2 = new Receiver();
		receiver2.setAuthLevel(AuthLevel.FULL);
		receiver2.setName("接收方公司");
		receiver2.setMobile("12312312312");
		receiver2.setType(UserType.COMPANY);
		receiver2.setOrdinal(2);
		// 用关键字指定签署位置
		StandardStamper stamper1 = new StandardStamper();
		stamper1.setDocumentId(documentId1); // 指定第一份文件的文件ID
		stamper1.setType(StandardSignType.SEAL);
		stamper1.setKeyword("乙方公章签署");
		StandardStamper stamper2 = new StandardStamper();
		stamper2.setDocumentId(documentId2); // 指定第二份文件的文件ID
		stamper2.setType(StandardSignType.SEAL);
		stamper2.setKeyword("乙方公章签署");
		List<StandardStamper> stampers = new ArrayList<StandardStamper>();
		stampers.add(stamper1);
		stampers.add(stamper2);
		receiver2.setStampers(stampers);

		List<Receiver> receivers = new ArrayList<Receiver>();
		receivers.add(receiver1);
		receivers.add(receiver2);

		SendRequest request = new SendRequest();
		request.setContractId(contractId);
		request.setCategoryId(categoryId); // 为空时默认为“默认合同分类”
		request.setReceiveType(ReceiveType.SEQ); // 顺序接收
		request.setReceivers(receivers);
		// 发起合同
		standardSignService.send(request);
	}
	
	/**
	 * 签署法人章：如果合同指定了运营方签署，并且需要签署法人章，则调用此接口进行签署，且签署法人章必须在签署公章前进行
	 */
	private static void signByLegalPerson() {

		List<StandardStamper> stampers = new ArrayList<StandardStamper>();
		// 关键字指定法人章位置
		StandardStamper stamper1 = new StandardStamper();
		stamper1.setDocumentId(documentId1);
		stamper1.setType(StandardSignType.LEGAL_PERSON);
		stamper1.setKeyword("甲方法人签署");
		stampers.add(stamper1);
		// 坐标直接指定法人章位置
		StandardStamper stamper2 = new StandardStamper();
		stamper2.setDocumentId(documentId2);
		stamper2.setType(StandardSignType.LEGAL_PERSON);
		stamper2.setPage(1);
		stamper2.setOffsetX(0.1);
		stamper2.setOffsetY(0.1);
		stampers.add(stamper2);
//		// 关键字指定法人签署时间戳位置
//		StandardStamper stamper3 = new StandardStamper();
//		stamper3.setDocumentId(documentId1);
//		stamper3.setType(StandardSignType.LEGAL_PERSON_TIMESTAMP);
//		stamper3.setKeyword("甲方签署时间");
//		stampers.add(stamper3);
//		// 坐标直接指定法人签署时间戳位置
//		StandardStamper stamper4 = new StandardStamper();
//		stamper4.setDocumentId(documentId2);
//		stamper4.setType(StandardSignType.LEGAL_PERSON_TIMESTAMP);
//		stamper4.setPage(1);
//		stamper4.setOffsetX(0.3);
//		stamper4.setOffsetY(0.1);
//		stampers.add(stamper4);
		
		SignByLegalPersonRequest request = new SignByLegalPersonRequest();
		request.setContractId(contractId);
		request.setStampers(stampers);
		
		standardSignService.signByLegalPerson(request);
	}
	
	/**
	 * 签署公章：如果合同指定了运营方签署，则调用此接口签署公章；如果指定了运营方签署法人章，则签署公章必须在签署法人章之后进行
	 */
	private static void sign() {
		List<StandardStamper> stampers = new ArrayList<StandardStamper>();
		// 关键字指定公章位置
		StandardStamper stamper1 = new StandardStamper();
		stamper1.setDocumentId(documentId1);
		stamper1.setType(StandardSignType.SEAL);
		stamper1.setKeyword("甲方公章签署");
		// 坐标指定时间戳位置
		StandardStamper stamper2 = new StandardStamper();
		stamper2.setDocumentId(documentId1);
		stamper2.setType(StandardSignType.SEAL_TIMESTAMP);
		stamper2.setPage(1);
		stamper2.setOffsetX(0.1);
		stamper2.setOffsetY(0.2);
		
		stampers.add(stamper1);
		stampers.add(stamper2);
		
		SignRequest request = new SignRequest();
		request.setContractId(contractId);
		request.setSealId(sealId); // 指定印章ID
		request.setStampers(stampers);
		request.setAcrossPage(true); // 指定是否签署骑缝章
		
		standardSignService.sign(request);
	}
	
	@Bean
	public SDKClient sdkClient(){
		String url = "https://openapi.qiyuesuo.me"; // 测试环境
		String accessKey = "tBYw1vOsA3";
		String accessSecret = "NOUDnkX0JN96T1VGFttLVCaVWKe1Fh";
		return new SDKClient(url,accessKey,accessSecret);
	}
	
	@Bean
	public StandardSignService standardSignService(SDKClient sdkClient){
		return new StandardSignServiceImpl(sdkClient);
	}
	
	@Bean
	public SealService sealService(SDKClient sdkClient){
		return new SealServiceImpl(sdkClient);
	}
	
	private static void safeClose(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
}
