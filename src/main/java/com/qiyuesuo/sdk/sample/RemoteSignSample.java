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
import com.qiyuesuo.sdk.seal.Seal;
import com.qiyuesuo.sdk.sign.SignType;
import com.qiyuesuo.sdk.sign.SignUrlResponse;
import com.qiyuesuo.sdk.sign.Stamper;
import com.qiyuesuo.sdk.sign.ViewUrlResponse;
import com.qiyuesuo.sdk.signer.Company;
import com.qiyuesuo.sdk.signer.PaperType;
import com.qiyuesuo.sdk.signer.Person;
import com.qiyuesuo.sdk.template.Template;

/**
 * 契约锁 JAVA SDK 远程签调用示例代码
 * 本示例代码仅展示了契约锁Java SDK用法，代码中的姓名、手机、邮箱均为非真实数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
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
		Long documentId = 2279623303930839040l;// 创建合同接口返回的文档ID
		// ====================================================
		// 根据文件创建合同
		InputStream fileInput = new FileInputStream(new File("D:/test/NoSign.pdf"));
		documentId = remoteSignService.create(fileInput, "远程签授权协议书");
		safeClose(fileInput);
		logger.info("合同创建完成,文档ID:{}", documentId);

		// ====================================================
		// 根据模板创建合同
		Template template = new Template(2291848536332750874l);// 创建模板对象
		// 设置模板参数
		template.addParameter("name", "张三");
		template.addParameter("age", "11");

		documentId = remoteSignService.create(template, "远程签模板合同");
		logger.info("模板合同创建完成，文档ID：{}", documentId);

		// ================================================
		// 平台签署,带签名外观
		Long sealId = 2249772556456296448l;// 平台印章
		// Stamper stamper = new Stamper(1, 0.1f, 0.1f);// 签名位置，直接确定印章坐标
		Stamper stamper = new Stamper("乙方签名",0,-0.01f);// 签名位置，用关键字确定印章坐标
		stamper.setKeywordIndex(-1); // 设置关键字索引，-1表示在找到的最后一个关键字处签名
		remoteSignService.sign(documentId, sealId, stamper); // 有签名外观签署
		// remoteSignService.sign(documentId);//无签名外观签署
		logger.info("平台签署完成。");

		// ==================================================
		// 个人用户签署
		Person person = new Person("丁五");
		person.setIdcard("311312195709206418");
		person.setPaperType(PaperType.IDCARD);
		person.setMobile("134****1093");

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
		OutputStream outputStream = new FileOutputStream(new File("D:/test/remote-download.pdf"));
		remoteSignService.download(documentId, outputStream);
		safeClose(outputStream);
		logger.info("下载完成。");
	
		// ==============================================
		//获取合同详情
		Contract contract = remoteSignService.detail(documentId);
		logger.info("获取远程签详情完成：{}",contract.getStatus());

		//==============================================
		//个人用户签署页面URL
		Person signer = new Person("丁六");
		signer.setIdcard("311312195709206418");
		signer.setPaperType(PaperType.IDCARD);
		signer.setMobile("13412341093");//SignType.SIGNWITHPIN时必填
		//个人用户签署页面之不可见签名 
		//SignType:SIGN（直接签署），SIGNWITHPIN（手机验证码签署）
		SignUrlResponse personSignUnvisibleResponse = remoteSignService.signUrl(documentId,SignType.SIGN, signer,  "https://www.baidu.com/", null);
		logger.info("个人用户签署页面之不可见签名 url：{}",personSignUnvisibleResponse.getSignUrl());
		//个人用户签署页面之可见签名
		//生成个人印章数据，用户可自定义签名图片
		String personSealData = sealService.generateSeal(signer);// 生成个人印章数据，用户可自定义签名图片
		Stamper personSignUrlStamper = null;
		//Stamper personSignUrlStamper = new Stamper(1, 0.3F, 0.3F);
		SignUrlResponse personSignVisibleResponse = remoteSignService.signUrl(documentId,SignType.SIGN, signer,personSealData ,personSignUrlStamper, "https://www.baidu.com/", null);
		logger.info("个人用户签署页面之可见签名 url：{}",personSignVisibleResponse.getSignUrl());
		
		
		// ==============================================
		// 企业用户签署页面URL
		Company companySigner = new Company("哈治理测试科技有限公司");
		companySigner.setRegisterNo("12323432452");
		companySigner.setTelephone("13411111093");//SignType.SIGNWITHPIN时必填
		//企业用户签署页面之不可见签名 
		SignUrlResponse companySignUnvisibleResponse = remoteSignService.signUrl(documentId, SignType.SIGNWITHPIN,companySigner, "https://www.baidu.com/", null);
		logger.info("企业用户签署页面之不可见签名url：{}",companySignUnvisibleResponse.getSignUrl());
		//企业用户签署页面之可见签名 
		// 生成企业印章数据，用户可自定义印章图片
		String companySealDate = sealService.generateSeal(companySigner); 
		//Stamper companySignUrlStamper = null;
		Stamper companySignUrlStamper = new Stamper(1, 0.3F, 0.3F);
		SignUrlResponse companySignVisibleResponse = remoteSignService.signUrl(documentId,SignType.SIGNWITHPIN ,companySigner, companySealDate, companySignUrlStamper, "https://www.baidu.com/", null);
		logger.info("企业用户签署页面之可见签名url：{}",companySignVisibleResponse.getSignUrl());
		
	
		// ==============================================
		// 浏览合同URL
		ViewUrlResponse viewUrlResponse = remoteSignService.viewUrl(documentId);
		logger.info("浏览合同URL：{}",viewUrlResponse.getViewUrl());

		
		// ==============================================
		// 签署完成
		remoteSignService.complete(documentId);
		logger.info("签署完成。");
		
		// ==============================================
		//根据html创建合同,不带有效时间
		String html = "<html><body><p>title</p><p>在线第三方电子合同平台。企业及个人用户可通过本平台与签约方快速完成合同签署，安全、合法、有效。</p></body></html>";
		documentId = remoteSignService.create(html, "测试html创建合同一");
		logger.info("根据html创建合同 documentId：{}",documentId);
		//根据html创建合同,带有效时间
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 1);
		documentId = remoteSignService.create(html, "测试html创建合同二",calendar.getTime());
		logger.info("根据html创建合同 documentId：{}",documentId);
		
		// =============================================
		//获取签章列表
		List<Seal> list =  sealService.sealList();
		logger.info("获取签章列表,{}",list);
	}

	@Bean
	public SDKClient sdkClient() {
		String url = "https://openapi.qiyuesuo.me"; // 测试环境
		String accessKey = "7EswyQzhBe";
		String accessSecret = "lSTLQLZlnCGkdy6MiOhAzIvfbOYlpU";
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
