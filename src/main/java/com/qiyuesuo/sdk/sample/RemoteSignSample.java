/**
 * Copyright (C) 2017,  上海亘岩网络科技有限公司 All rights reserved All rights reserved.
 */
package com.qiyuesuo.sdk.sample;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.RemoteSignService;
import com.qiyuesuo.sdk.api.SealService;
import com.qiyuesuo.sdk.api.Stamper;
import com.qiyuesuo.sdk.contract.Contract;
import com.qiyuesuo.sdk.impl.RemoteSignServiceImpl;
import com.qiyuesuo.sdk.impl.SealServiceImpl;
import com.qiyuesuo.sdk.signer.Company;
import com.qiyuesuo.sdk.signer.PaperType;
import com.qiyuesuo.sdk.signer.Person;
import com.qiyuesuo.sdk.template.Template;

/**
 * 契约锁 JAVA SDK 远程签调用示例代码
 * 本示例代码仅展示了契约锁Java SDK用法，代码中的姓名、手机、邮箱均为非真实数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * 
 * @author Ricky
 */
@SpringBootApplication
public class RemoteSignSample {

	private static final Logger logger = LoggerFactory.getLogger(RemoteSignSample.class);

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(RemoteSignSample.class, args);
		context.registerShutdownHook();
		context.start();

		SealService sealService = context.getBean(SealService.class);
		RemoteSignService remoteSignService = context.getBean(RemoteSignService.class);

		// ====================================================
		// 根据文件创建合同
		InputStream fileInput = new FileInputStream(new File("D://NoSign.pdf"));
		Long fileDocument = remoteSignService.create(fileInput, "远程签授权协议书");
		safeClose(fileInput);
		logger.info("合同创建完成,文档ID:{}", fileDocument);

		// ====================================================
		// 根据模板创建合同
		Template template = new Template(2267243393824325632L);// 创建模板对象
		// 设置模板参数
		template.addParameter("param1", "张三");
		template.addParameter("param2", "13868598888");
		template.addParameter("param3", "14000.00");

		Long templateDocument = remoteSignService.create(template, "远程签模板合同");
		logger.info("模板合同创建完成，文档ID：{}", templateDocument);

		// ================================================
		// 平台签署,带签名外观
		Long documentId = 2278885262404616192l;// 创建合同接口返回的文档ID
		Long sealId = 2201194154317316096l;// 平台印章
		Stamper stamper = new Stamper(1, 0.1f, 0.1f);// 签名位置
		// remoteSignService.sign(documentId);//无签名外观
		remoteSignService.sign(documentId, sealId, stamper);
		logger.info("平台签署完成。");

		// ==================================================
		// 个人用户签署
		Person person = new Person("丁五");
		person.setIdcard("311312195709206418");
		person.setPaperType(PaperType.IDCARD);
		person.setMobile("18601556688");

		// 个人无签名外观时调用
		// remoteSignService.sign(documentId, person);
		String sealData = sealService.generateSeal(person);// 生成个人印章数据
		// 个人签署
		Stamper personStamper = new Stamper(1, 0.2f, 0.2f);
		// 个人签署接口，有签名外观
		remoteSignService.sign(documentId, person, sealData, personStamper);
		logger.info("个人签署完成。");

		// ==============================================
		// 公司用户签署
		Company company = new Company("大唐测试科技有限公司");
		company.setRegisterNo("12323432452");
		// 公司无签名外观时调用
		//remoteSignService.sign(2278885262404616192l, company);
		Stamper companyStamper = new Stamper(1, 0.3f, 0.3f);
		String companySealData = sealService.generateSeal(company);
		remoteSignService.sign(documentId, company, companySealData, companyStamper);
		logger.info("公司签署完成。");

		// ==============================================
		// 下载合同文件
		OutputStream outputStream = new FileOutputStream(new File("D://remote-download.pdf"));
		remoteSignService.download(documentId, outputStream);
		safeClose(outputStream);
		logger.info("下载完成。");
	
		// ==============================================
		// 2.0.0版本新增功能
		//获取合同详情
		Contract contract = remoteSignService.detail(documentId);
		logger.info("获取远程签详情完成：{}",contract.getStatus());

		
		// ==============================================
		// 签署完成
		remoteSignService.complete(documentId);
		logger.info("签署完成。");

		
		// ==============================================
		// 个人用户签署页面URL
		Person signer = new Person("丁五");
		signer.setIdcard("311312195709206418");
		signer.setPaperType(PaperType.IDCARD);
		signer.setMobile("18601556688");
		//个人用户签署页面之不可见签名 
		String personSignUnvisibleUrl = remoteSignService.signUrl(documentId, signer,  "https://www.baidu.com/");
		logger.info("个人用户签署页面之不可见签名 url：{}",personSignUnvisibleUrl);
		//个人用户签署页面之可见签名
		// 生成个人印章数据，用户可自定义签名图片
		String personSealData = sealService.generateSeal(signer);// 生成个人印章数据，用户可自定义签名图片
		Stamper personSignUrlStamper = new Stamper(1, 0.2f, 0.2f);
		String personSignVisibleUrl = remoteSignService.signUrl(documentId, signer,personSealData ,personSignUrlStamper, "https://www.baidu.com/");
		logger.info("个人用户签署页面之可见签名 url：{}",personSignVisibleUrl);
		
		
		// ==============================================
		// 企业用户签署页面URL
		Company companySigner = new Company("大唐测试科技有限公司");
		companySigner.setRegisterNo("12323432452");
		//企业用户签署页面之不可见签名 
		String companySignUnvisibleUrl = remoteSignService.signUrl(documentId, companySigner, "https://www.baidu.com/");
		logger.info("企业用户签署页面之不可见签名url：{}",companySignUnvisibleUrl);
		//企业用户签署页面之可见签名 
		// 生成企业印章数据，用户可自定义印章图片
		String companySealDate = sealService.generateSeal(companySigner); 
		Stamper companySignUrlStamper = new Stamper(1, 0.3f, 0.3f);
		String companySignVisibleUrl = remoteSignService.signUrl(documentId, companySigner, companySealDate, companySignUrlStamper, "https://www.baidu.com/");
		logger.info("企业用户签署页面之可见签名url：{}",companySignVisibleUrl);
		
		
		// ==============================================
		// 浏览合同URL
		String viewUrl = remoteSignService.viewUrl(documentId);
		logger.info("浏览合同URL：{}",viewUrl);
		
	}

	@Bean
	public SDKClient sdkClient() {
		String url = "https://openapi.qiyuesuo.me";
		String accessKey = "tBYw1vOsA3";
		String accessSecret = "NOUDnkX0JN96T1VGFttLVCaVWKe1Fh";
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
