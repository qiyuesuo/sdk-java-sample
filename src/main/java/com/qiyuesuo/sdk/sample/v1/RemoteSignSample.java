/**
 * Copyright (C) 2017,  上海亘岩网络科技有限公司 All rights reserved All rights reserved.
 */
package com.qiyuesuo.sdk.sample.v1;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.RemoteSignService;
import com.qiyuesuo.sdk.api.SealService;
import com.qiyuesuo.sdk.contract.Contract;
import com.qiyuesuo.sdk.impl.RemoteSignServiceImpl;
import com.qiyuesuo.sdk.impl.SealServiceImpl;
import com.qiyuesuo.sdk.mutisigner.MutiCompany;
import com.qiyuesuo.sdk.mutisigner.MutiPerson;
import com.qiyuesuo.sdk.mutisigner.MutiPlatform;
import com.qiyuesuo.sdk.mutisigner.RemoteSealType;
import com.qiyuesuo.sdk.mutisigner.RemoteStamper;
import com.qiyuesuo.sdk.sign.SignType;
import com.qiyuesuo.sdk.sign.SignUrlRequest;
import com.qiyuesuo.sdk.sign.SignUrlResponse;
import com.qiyuesuo.sdk.sign.Stamper;
import com.qiyuesuo.sdk.sign.ViewUrlResponse;
import com.qiyuesuo.sdk.signer.Company;
import com.qiyuesuo.sdk.signer.PaperType;
import com.qiyuesuo.sdk.signer.Person;
import com.qiyuesuo.sdk.template.Template;
import com.qiyuesuo.sdk.utils.JsonUtils;

/**
 * 契约锁 JAVA SDK 远程签调用示例代码
 * 本示例代码仅展示了契约锁Java SDK用法，代码中的姓名、手机、邮箱均为非真实数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * @author GJ
 */
@SpringBootApplication
public class RemoteSignSample {

	private static final Logger logger = LoggerFactory.getLogger(RemoteSignSample.class);
	
	private static Long templateId = 2475125988735381915L; // 模板ID，在契约锁开放平台【模板管理】中获取
	private static Long sealId = 2492283309478498330L; // 印章编号，在契约锁开放平台【公司印章】中获取
	
	private static String base64Seal="";
	
	private static File pdfFile = new File("./NoSign.pdf"); //测试文档
	private static File outFile = new File("./sign/remote");   //测试输出文档路径
	static {
		if(!outFile.exists()) {
			outFile.mkdirs();
		}
	}

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(RemoteSignSample.class, args);
		context.registerShutdownHook();
		context.start();

		SealService sealService = context.getBean(SealService.class);
		RemoteSignService remoteSignService = context.getBean(RemoteSignService.class);
		Long documentId = null;// 创建合同接口返回的文档ID
		// ====================================================
		// 根据文件创建合同
		createContractByFile(remoteSignService);
		// ==============================================
		//根据html创建合同,不带有效时间
		documentId=createContractByHtml(remoteSignService);
		// ====================================================
		// 根据模板创建合同
		createContractByTemplate(remoteSignService);
		// ================================================
		// 平台签署,带签名外观
		platformSign(documentId, remoteSignService);
		// ==================================================
		personSign(documentId,remoteSignService,sealService);
		// ==============================================
		// 公司用户签署
		companySign(documentId,remoteSignService,sealService);
		// ==============================================
		// 下载合同文件
		contractDownload(documentId,remoteSignService);
		// ==============================================
		//获取合同详情
		Contract contract = remoteSignService.detail(documentId);
		logger.info("获取远程签详情完成：{}",JsonUtils.toJson(contract));
		//==============================================
		//个人用户签署页面URL
		SignUrlResponse response=gerenatePersonSignUrlWithoutApperance(documentId,remoteSignService);
		//个人用户签署页面之可见签名
		//生成个人印章数据，用户可自定义签名图片
		response=gerenatePersonSignUrlWithApperance(documentId,remoteSignService,sealService);
		// ==============================================
		// 企业用户签署页面URL
		response=gerenateCompanySignUrlWithoutApperance(documentId, remoteSignService);
		//企业用户签署页面之可见签名 
		// 生成企业印章数据，用户可自定义印章图片
		response=gerenatePersonSignUrlWithApperance(documentId,remoteSignService,sealService);
		// ==============================================
		// 浏览合同URL
		ViewUrlResponse viewUrlResponse = remoteSignService.viewUrl(documentId);
		logger.info("浏览合同URL：{}",viewUrlResponse.getViewUrl());
		// ==============================================
		// 平台方多章、时间戳签署
		platformMutiSign(documentId, remoteSignService);
		// ==============================================
		// 公司用户多章、时间戳签署
		companyMutiSign(documentId, remoteSignService);
		// ==============================================
		// 公司用户多章、时间戳签署
		personMutiSign(documentId, remoteSignService);
		// ==============================================
		// 签署完成
		remoteSignService.complete(documentId);
		logger.info("签署完成。");
	}

	private static Long createContractByFile(RemoteSignService remoteSignService) throws FileNotFoundException {
		InputStream fileInput = new FileInputStream(pdfFile);
		Long documentId = remoteSignService.create(fileInput, "远程签授权协议书");
		safeClose(fileInput);
		logger.info("合同创建完成,文档ID:{}", documentId);
		return documentId;
	}
	
	
	private static Long createContractByHtml(RemoteSignService remoteSignService) {
		String html = "<html><body><p>title</p><p>在线第三方电子合同平台。企业及个人用户可通过本平台与签约方快速完成合同签署，安全、合法、有效。</p></body></html>";
		Long documentId = remoteSignService.create(html, "测试html创建合同一");
		logger.info("根据html创建合同 documentId：{}",documentId);
		//根据html创建合同,带有效时间
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 1);
		documentId = remoteSignService.create(html, "测试html创建合同",calendar.getTime());
		logger.info("根据html创建合同 documentId：{}",documentId);
		return documentId;
	}
	
	
	private static Long createContractByTemplate(RemoteSignService remoteSignService) {
		Template template = new Template(templateId);// 创建模板对象 
		// 设置模板参数
		template.addParameter("甲方", "张三");
		template.addParameter("乙方", "李四");
		Long documentId = remoteSignService.create(template, "远程签模板合同");
		logger.info("模板合同创建完成，文档ID：{}", documentId);
		return documentId;
	}
	
	
	private static void platformSign(Long documentId,RemoteSignService remoteSignService) {
//		Stamper stamper = new Stamper(1, 0.1f, 0.1f);// 签名位置，直接确定印章坐标
		Stamper stamper = new Stamper("乙方签名",0,-0.01f);// 签名位置，用关键字确定印章坐标
		stamper.setKeywordIndex(-1); // 设置关键字索引，-1表示在找到的最后一个关键字处签名
		remoteSignService.sign(documentId, sealId, stamper); // 有签名外观签署
//		remoteSignService.sign(documentId);//无签名外观签署
		logger.info("平台签署完成。");
	}
	
	private static void personSign(Long documentId,RemoteSignService remoteSignService,SealService sealService) {
		// 个人用户签署
		Person person = new Person("李四");
		person.setIdcard("311312195709206418");
		person.setPaperType(PaperType.IDCARD);
		person.setMobile("13400001111");
		// 个人无签名外观时调用
		// remoteSignService.sign(documentId, person);
		String sealData = sealService.generateSeal(person);// 生成个人印章数据
		// 个人签署
		Stamper personStamper = new Stamper(1, 0.2f, 0.2f);
		// 个人签署接口，有签名外观
		remoteSignService.sign(documentId, person, sealData, personStamper);
		logger.info("个人签署完成。");	
	}
	
	private static void companySign(Long documentId,RemoteSignService remoteSignService,SealService sealService) {
		Company company = new Company("大唐测试科技有限公司");
		company.setRegisterNo("12323432452");
		// 公司无签名外观时调用
//		remoteSignService.sign(documentId, company);
		Stamper companyStamper = new Stamper(1, 0.3f, 0.3f);
		String companySealData = sealService.generateSeal(company);
		remoteSignService.sign(documentId, company, companySealData, companyStamper);
		logger.info("公司签署完成。");
	}
	
	private static void contractDownload(Long documentId,RemoteSignService remoteSignService) throws FileNotFoundException {
		OutputStream outputStream = new FileOutputStream(new File(outFile,"remote-download.pdf"));
		remoteSignService.download(documentId, outputStream);
		safeClose(outputStream);
		logger.info("下载完成。");
	}
	
	
	private static SignUrlResponse gerenatePersonSignUrlWithoutApperance(Long documentId,RemoteSignService remoteSignService) {
		Person signer = new Person("张三");
		signer.setIdcard("311312195709206418");
		signer.setPaperType(PaperType.IDCARD);
		signer.setMobile("13400000000");//SignType.SIGNWITHPIN时必填
		//个人用户签署页面之不可见签名 
		//SignType:SIGN（直接签署），SIGNWITHPIN（手机验证码签署）
		SignUrlRequest personSignUnvisibleRequest = new SignUrlRequest();
		personSignUnvisibleRequest.setDocumentId(documentId);
		personSignUnvisibleRequest.setSignType(SignType.SIGN);
		personSignUnvisibleRequest.setSigner(signer);
		personSignUnvisibleRequest.setCallbackUrl("https://www.baidu.com/");
		personSignUnvisibleRequest.setSuccessUrl("https://www.baidu.com/");
		SignUrlResponse personSignUnvisibleResponse = remoteSignService.signUrl(personSignUnvisibleRequest);
		logger.info("个人用户签署页面之不可见签名 url：{}",personSignUnvisibleResponse.getSignUrl());
		return personSignUnvisibleResponse;
	}
	
	
	private static SignUrlResponse gerenatePersonSignUrlWithApperance(Long documentId,RemoteSignService remoteSignService,SealService sealService) {
		Person signer = new Person("张三");
		signer.setIdcard("311312195709206418");
		signer.setPaperType(PaperType.IDCARD);
		signer.setMobile("13400000000");//SignType.SIGNWITHPIN时必填
		String personSealData = sealService.generateSeal(signer);// 生成个人印章数据，用户可自定义签名图片
		SignUrlRequest personSignVisibleRequest = new SignUrlRequest();
		personSignVisibleRequest.setDocumentId(documentId);
		personSignVisibleRequest.setSignType(SignType.SIGN);
		personSignVisibleRequest.setSigner(signer);
		personSignVisibleRequest.setCallbackUrl("https://www.baidu.com/");
		personSignVisibleRequest.setSuccessUrl("https://www.baidu.com/");
		personSignVisibleRequest.setSealData(personSealData);
		List<Stamper> personSignUrlStampers = new ArrayList<>();
		Stamper personSignUrlStamper1 = new Stamper(1, 0.3F, 0.3F);
		personSignUrlStampers.add(personSignUrlStamper1);
		Stamper personSignUrlStamper2 = new Stamper(1, 0.5F, 0.6F);
		personSignUrlStampers.add(personSignUrlStamper2);
		personSignVisibleRequest.setStampers(personSignUrlStampers);
		SignUrlResponse personSignVisibleResponse = remoteSignService.signUrl(personSignVisibleRequest);
		logger.info("个人用户签署页面之可见签名 url：{}",personSignVisibleResponse.getSignUrl());
		return personSignVisibleResponse;
	}
	
	
	private static SignUrlResponse gerenateCompanySignUrlWithoutApperance(Long documentId,RemoteSignService remoteSignService) {
		Company companySigner = new Company("哈治理测试科技有限公司");
		companySigner.setRegisterNo("12323432452");
		companySigner.setTelephone("13411111093");//SignType.SIGNWITHPIN时必填
		//企业用户签署页面之不可见签名 
		SignUrlRequest companySignUnvisibleRequest = new SignUrlRequest();
		companySignUnvisibleRequest.setDocumentId(documentId);
		companySignUnvisibleRequest.setSignType(SignType.SIGN);
		companySignUnvisibleRequest.setSigner(companySigner);
		companySignUnvisibleRequest.setCallbackUrl("https://www.baidu.com/");
		companySignUnvisibleRequest.setSuccessUrl("https://www.baidu.com/");
		SignUrlResponse companySignUnvisibleResponse = remoteSignService.signUrl(companySignUnvisibleRequest);
		logger.info("企业用户签署页面之不可见签名url：{}",companySignUnvisibleResponse.getSignUrl());
		return companySignUnvisibleResponse;
	}
	
	private static SignUrlResponse gerenateCompanySignUrlWithApperance(Long documentId,RemoteSignService remoteSignService,SealService sealService) {
		Company companySigner = new Company("哈治理测试科技有限公司");
		companySigner.setRegisterNo("12323432452");
		companySigner.setTelephone("13411111093");//SignType.SIGNWITHPIN时必填
		String companySealDate = sealService.generateSeal(companySigner); 
		SignUrlRequest companySignVisibleRequest = new SignUrlRequest();
		companySignVisibleRequest.setDocumentId(documentId);
		companySignVisibleRequest.setSignType(SignType.SIGN);
		companySignVisibleRequest.setSigner(companySigner);
		companySignVisibleRequest.setCallbackUrl("https://www.baidu.com/");
		companySignVisibleRequest.setSuccessUrl("https://www.baidu.com/");
		companySignVisibleRequest.setSealData(companySealDate);
		List<Stamper> companySignUrlStampers = new ArrayList<>();
		Stamper companySignUrlStamper = new Stamper(1, 0.4F, 0.2F);
		companySignUrlStampers.add(companySignUrlStamper);
		companySignVisibleRequest.setStampers(companySignUrlStampers);
		SignUrlResponse companySignVisibleResponse = remoteSignService.signUrl(companySignVisibleRequest);
		logger.info("企业用户签署页面之可见签名url：{}",companySignVisibleResponse.getSignUrl());
		return companySignVisibleResponse;
	}
	
	private static void platformMutiSign(Long documentId,RemoteSignService remoteSignService) {
		MutiPlatform mutiPlatformSign=new MutiPlatform();
		
		List<RemoteStamper> stampers=new ArrayList<>();
		RemoteStamper remoteStamper1=new RemoteStamper();
		remoteStamper1.setDocumentId(documentId);
		remoteStamper1.setSealType(RemoteSealType.SEAL);
		remoteStamper1.setOffsetX(0.1);
		remoteStamper1.setOffsetY(0.2);
		remoteStamper1.setPage(1);
		remoteStamper1.setSealId(sealId);
		stampers.add(remoteStamper1);
		
		RemoteStamper remoteStamper2=new RemoteStamper();
		remoteStamper2.setDocumentId(documentId);
		remoteStamper2.setSealType(RemoteSealType.TIMESTAMP);
		remoteStamper2.setOffsetX(0.1);
		remoteStamper2.setOffsetY(0.2);
		remoteStamper2.setPage(2);
		stampers.add(remoteStamper2);
		
		RemoteStamper remoteStamper3=new RemoteStamper();
		remoteStamper3.setDocumentId(documentId);
		remoteStamper3.setSealType(RemoteSealType.ACROSS_PAGE);
		remoteStamper3.setOffsetY(0.2);
		stampers.add(remoteStamper3);
		mutiPlatformSign.setStampers(stampers);
		remoteSignService.mutiSign(mutiPlatformSign);
		
	}
	
	private static void companyMutiSign(Long documentId,RemoteSignService remoteSignService) {
		Company companySigner = new Company("哈治理测试科技有限公司");
		companySigner.setRegisterNo("12323432452");
		MutiCompany companyMutiSign=new MutiCompany(companySigner);
		
		List<RemoteStamper> stampers=new ArrayList<>();
		RemoteStamper remoteStamper1=new RemoteStamper();
		remoteStamper1.setDocumentId(documentId);
		remoteStamper1.setSealType(RemoteSealType.SEAL);
		remoteStamper1.setOffsetX(0.1);
		remoteStamper1.setOffsetY(0.2);
		remoteStamper1.setPage(1);
		remoteStamper1.setSealImageBase64(base64Seal);
		stampers.add(remoteStamper1);
		
		RemoteStamper remoteStamper2=new RemoteStamper();
		remoteStamper2.setDocumentId(documentId);
		remoteStamper2.setSealType(RemoteSealType.TIMESTAMP);
		remoteStamper2.setOffsetX(0.1);
		remoteStamper2.setOffsetY(0.2);
		remoteStamper2.setPage(2);
		stampers.add(remoteStamper2);
		
		RemoteStamper remoteStamper3=new RemoteStamper();
		remoteStamper3.setDocumentId(documentId);
		remoteStamper3.setSealType(RemoteSealType.ACROSS_PAGE);
		remoteStamper3.setOffsetY(0.2);
		stampers.add(remoteStamper3);
		companyMutiSign.setStampers(stampers);
		remoteSignService.mutiSign(companyMutiSign);
		
	}
	
	
	private static void personMutiSign(Long documentId,RemoteSignService remoteSignService) {
		Person personSigner = new Person("签署人");
		personSigner.setIdcard("311312195709206418");
		personSigner.setPaperType(PaperType.IDCARD);
		personSigner.setMobile("13400001111");
		MutiPerson personMutiSign=new MutiPerson(personSigner);
		
		List<RemoteStamper> stampers=new ArrayList<>();
		RemoteStamper remoteStamper1=new RemoteStamper();
		remoteStamper1.setDocumentId(documentId);
		remoteStamper1.setSealType(RemoteSealType.SEAL);
		remoteStamper1.setOffsetX(0.1);
		remoteStamper1.setOffsetY(0.2);
		remoteStamper1.setPage(1);
		remoteStamper1.setSealImageBase64(base64Seal);
		stampers.add(remoteStamper1);
		
		RemoteStamper remoteStamper2=new RemoteStamper();
		remoteStamper2.setDocumentId(documentId);
		remoteStamper2.setSealType(RemoteSealType.TIMESTAMP);
		remoteStamper2.setOffsetX(0.1);
		remoteStamper2.setOffsetY(0.2);
		remoteStamper2.setPage(2);
		stampers.add(remoteStamper2);
		
		personMutiSign.setStampers(stampers);
		remoteSignService.mutiSign(personMutiSign);
		
	}
	
	@Bean
	public SDKClient sdkClient() {
		String url = "https://openapi.qiyuesuo.cn"; //测试环境
		String accessKey = "替换为您开放平台Access Token";
		String accessSecret = "替换为您开放平台Access Secret";
		return new SDKClient(url, accessKey, accessSecret);
	}

	@Bean
	public RemoteSignService remoteSignService(SDKClient sdkClient) {
		return new RemoteSignServiceImpl(sdkClient);
	}

	@Bean
	public SealService sealService(SDKClient sdkClient) {
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