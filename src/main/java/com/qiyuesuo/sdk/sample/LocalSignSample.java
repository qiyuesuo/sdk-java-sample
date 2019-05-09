/**
 * Copyright (C) 2017,  上海亘岩网络科技有限公司 All rights reserved All rights reserved.
 */
package com.qiyuesuo.sdk.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.LocalSignService;
import com.qiyuesuo.sdk.api.SealService;
import com.qiyuesuo.sdk.impl.LocalSignServiceImpl;
import com.qiyuesuo.sdk.impl.SealServiceImpl;
import com.qiyuesuo.sdk.sign.SignInfo;
import com.qiyuesuo.sdk.sign.Stamper;
import com.qiyuesuo.sdk.signer.Company;
import com.qiyuesuo.sdk.signer.PaperType;
import com.qiyuesuo.sdk.signer.Person;
import com.qiyuesuo.sdk.utils.JsonUtils;
import com.qiyuesuo.sdk.verify.PdfVerifyResult;

/**
 * 契约锁 JAVA SDK 本地签实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * @author GJ
 */
@SpringBootApplication
public class LocalSignSample {
	
	private static final Logger logger = LoggerFactory.getLogger(LocalSignSample.class);
	
	private static Long sealId = 2483532812670148712L; // 印章编号，在契约锁开放平台【公司印章】中获取
	
	private static File pdfFile = new File("./NoSign.pdf"); //测试文档
	private static File outFile = new File("./sign/local");   //测试输出文档路径
	static {
		if(!outFile.exists()) {
			outFile.mkdirs();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext context = SpringApplication.run(LocalSignSample.class, args);
		context.registerShutdownHook();
		context.start();
		
		LocalSignService localSignService = context.getBean(LocalSignService.class);
		SealService sealService = context.getBean(SealService.class);
		
		//========================================================================
		//平台签署无外观签名
		InputStream platformInput = new FileInputStream(pdfFile);
		OutputStream platformOut = new FileOutputStream(new File(outFile,"Local-platform.pdf"));
		localSignService.sign(platformInput, platformOut);
		logger.info("平台签署无外观签名完成.");
		
		//========================================================================
		//平台签署有外观签名
		InputStream platformStamperInput = new FileInputStream(pdfFile);
		OutputStream platformStamperOut = new FileOutputStream(new File(outFile,"Local-platform-stamper.pdf"));
		//指定签名页码和坐标，确定签名位置
		Stamper stamper = new Stamper(1,0.2f,0.2f);
		
		//根据关键字确定签名位置，若找到多个关键字则只在第一个关键字出现的位置签署，若没有出现关键字则抛出异常
		//Stamper keywordStamper = new Stamper("乙方盖章", 0.01f, -0.01f);
		//在找到的第keywordIndex个关键字上签署，keywordIndex默认为1。例：如果keywordIndex=1，则在第1个关键字上签署；如果keywordIndex=-1，则在倒数第1个关键字上签署；如果keywordIndex=0，则在所有关键字上签署。
		//keywordStamper.setKeywordIndex(-1);
		localSignService.sign(platformStamperInput, platformStamperOut,sealId ,stamper);
		logger.info("平台签署带外观签名完成.");
		
		
		//========================================================================
		//个人用户签署无外观签名
		InputStream personInput = new FileInputStream(pdfFile);
		OutputStream personOut = new FileOutputStream(new File(outFile,"Local-person.pdf"));
		//实例化个人用户信息，姓名（name）必须, 身份信息（idcard）、邮箱（email）、电话（mobile）三者必备其一，用于颁发证书
		Person person = new Person("张三");
		person.setPaper(PaperType.IDCARD,"311312183706206418");
		person.setMobile("13400000000");
		person.setEmail("san.zhang@qiyuesuo.com");
		
		localSignService.sign(personInput, personOut,person);
		logger.info("个人签署无外观签名完成.");
		
		//========================================================================
		//个人用户签署有外观签名
		InputStream personStamperInput = new FileInputStream(pdfFile);
		OutputStream personStamperOut = new FileOutputStream(new File(outFile,"Local-person-stamper.pdf"));
		//创建个人印章
		String sealData = sealService.generateSeal(person);
		//Stamper personKeywordStamper = new Stamper("运营者签字", 0, 0);
		Stamper personKeywordStamper = new Stamper(1, 0.1f, 0.1f);
		localSignService.sign(personStamperInput, personStamperOut,person,sealData,personKeywordStamper);
		logger.info("个人签署有外观签名完成.");
		
		//========================================================================
		//公司用户签署无外观签名
		InputStream companyInput = new FileInputStream(pdfFile);
		OutputStream companyOut = new FileOutputStream(new File(outFile,"Local-company.pdf"));
		//实例化公司用户信息，公司名称（name）必须, 工商注册号（社会统一信用代码）信息（registerNo）、邮箱（email）、电话（telephone）三者必备其一，用于颁发证书
		Company company = new Company("大唐科技");
		company.setTelephone("02100000000");
		company.setEmail("tang@qiyuesuo.com");
		company.setRegisterNo("123123423523");
		
		localSignService.sign(companyInput, companyOut,company);
		logger.info("公司签署无外观签名完成.");
		
		//========================================================================
		//公司用户签署有外观签名
		InputStream companyStamperInput = new FileInputStream(pdfFile);
		OutputStream companyStamperOut = new FileOutputStream(new File(outFile,"Local-company-stamper.pdf"));
		//创建个人印章
		String companySealData = sealService.generateSeal(company);
		//Stamper companyKeywordStamper = new Stamper("盖公章", 0, 0);//关键字
		Stamper companyKeywordStamper = new Stamper(1, 0.1f,  0.1f);
		companyKeywordStamper.setHeightPercent(0.2f); //公章站页面高比（默认0.14 在0~1之间）
		companyKeywordStamper.setAcrossPagePosition(0.5);
		localSignService.sign(companyStamperInput, companyStamperOut,company,companySealData,companyKeywordStamper);
		logger.info("公司签署有外观签名完成.");
		
		//========================================================================
		//批量签署接口
		InputStream batchInput = new FileInputStream(pdfFile);
		OutputStream batchOut = new FileOutputStream(new File(outFile,"Local-batch.pdf"));
		List<SignInfo> infos = new ArrayList<>();
		for(int i=0; i<2;i++){
			for(int j=0;j<2;j++){
				SignInfo signInfo = new SignInfo(new Person("张三","13400000000"), new Stamper(1, (i+1)*0.2f, (j+1)*0.2f));
				infos.add(signInfo);
			}
		}
		localSignService.batchSign(batchInput, batchOut, infos);
		logger.info("批量签署完成.");

		//========================================================================
		//完成合同签署，并对合同进行封存
		InputStream completeInput = new FileInputStream(new File(outFile,"Local-batch.pdf"));
		OutputStream completeOut = new FileOutputStream(new File(outFile,"Local-complete.pdf"));
		localSignService.complete(completeInput, completeOut);
		logger.info("合同封存完成.");
		
		//========================================================================
		// 校验合同的有效性
		InputStream verifyInput = new FileInputStream(new File(outFile,"Local-complete.pdf"));
		PdfVerifyResult verify = localSignService.verify(verifyInput);
		logger.info("校验合同的有效性完成,结果：{}",JsonUtils.toJson(verify));
	}

	
	@Bean
	public SDKClient sdkClient(){
		String url = "https://openapi.qiyuesuo.cn"; //测试环境
		String accessKey = "替换为您开放平台Access Token";
		String accessSecret = "替换为您开放平台Access Secret";
		return new SDKClient(url,accessKey,accessSecret);
	}
	
	@Bean
	public LocalSignService LocalSignService(SDKClient sdkClient){
		return new LocalSignServiceImpl(sdkClient);
	}
	
	@Bean
	public SealService sealService(SDKClient sdkClient){
		return new SealServiceImpl(sdkClient);
	}
}
