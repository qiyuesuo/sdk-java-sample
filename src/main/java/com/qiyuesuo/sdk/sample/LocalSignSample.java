/**
 * Copyright (C) 2017,  上海亘岩网络科技有限公司 All rights reserved All rights reserved.
 */
package com.qiyuesuo.sdk.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
import com.qiyuesuo.sdk.sign.Stamper;
import com.qiyuesuo.sdk.signer.Company;
import com.qiyuesuo.sdk.signer.PaperType;
import com.qiyuesuo.sdk.signer.Person;

/**
 * 契约锁 JAVA SDK 本地签实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * @author Ricky
 */
@SpringBootApplication
public class LocalSignSample {
	
	private static final Logger logger = LoggerFactory.getLogger(LocalSignSample.class);
	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext context = SpringApplication.run(LocalSignSample.class, args);
		context.registerShutdownHook();
		context.start();
		
		LocalSignService localSignService = context.getBean(LocalSignService.class);
		SealService sealService = context.getBean(SealService.class);
		
		//========================================================================
		//平台签署无外观签名
		InputStream platformInput = new FileInputStream(new File("D://NoSign.pdf"));
		OutputStream platformOut = new FileOutputStream(new File("D://Local-platform.pdf"));
		localSignService.sign(platformInput, platformOut);
		logger.info("平台签署无外观签名完成.");
		
		//========================================================================
		//平台签署有外观签名
		InputStream platformStamperInput = new FileInputStream(new File("D://NoSign.pdf"));
		OutputStream platformStamperOut = new FileOutputStream(new File("D://Local-platform-stamper.pdf"));
		//指定签名页码和坐标，确定签名位置
		//Stamper stamper = new Stamper(1, 0.1f, 0.1f);
		//根据关键字确定签名位置，若找到多个关键字则只在第一个关键字出现的位置签署，若没有出现关键字则抛出异常
		Stamper keywordStamper = new Stamper(1, 0.1f, 0.1f);
		Long sealId = 2201194154317316096l;//印章ID，在云平台维护
		localSignService.sign(platformStamperInput, platformStamperOut,sealId ,keywordStamper);
		logger.info("平台签署带外观签名完成.");
		
		
		//========================================================================
		//个人用户签署无外观签名
		InputStream personInput = new FileInputStream(new File("D://NoSign.pdf"));
		OutputStream personOut = new FileOutputStream(new File("D://Local-person.pdf"));
		//实例化个人用户信息，姓名（name）必须, 身份信息（idcard）、邮箱（email）、电话（mobile）三者必备其一，用于颁发证书
		Person person = new Person("张三");
		person.setPaper(PaperType.IDCARD,"311312183706206418");
		person.setMobile("134****1093");
		person.setEmail("san.zhang@qiyuesuo.com");
		
		localSignService.sign(personInput, personOut,person);
		logger.info("个人签署无外观签名完成.");
		
		//========================================================================
		//个人用户签署有外观签名
		InputStream personStamperInput = new FileInputStream(new File("D://NoSign.pdf"));
		OutputStream personStamperOut = new FileOutputStream(new File("D://Local-person-stamper.pdf"));
		//创建个人印章
		String sealData = sealService.generateSeal(person);
		//Stamper personKeywordStamper = new Stamper("运营者签字", 0, 0);
		Stamper personKeywordStamper = new Stamper(1, 0.1f, 0.1f);
		localSignService.sign(personStamperInput, personStamperOut,person,sealData,personKeywordStamper);
		logger.info("个人签署有外观签名完成.");
		
		//========================================================================
		//公司用户签署无外观签名
		InputStream companyInput = new FileInputStream(new File("D://NoSign.pdf"));
		OutputStream companyOut = new FileOutputStream(new File("D://Local-company.pdf"));
		//实例化公司用户信息，公司名称（name）必须, 工商注册号（社会统一信用代码）信息（registerNo）、邮箱（email）、电话（telephone）三者必备其一，用于颁发证书
		Company company = new Company("大唐科技");
		company.setTelephone("02150****88");
		company.setEmail("tang@qiyuesuo.com");
		
		localSignService.sign(companyInput, companyOut,company);
		logger.info("公司签署无外观签名完成.");
		
		//========================================================================
		//公司用户签署有外观签名
		InputStream companyStamperInput = new FileInputStream(new File("D://NoSign.pdf"));
		OutputStream companyStamperOut = new FileOutputStream(new File("D://Local-company-stamper.pdf"));
		//创建个人印章
		String companySealData = sealService.generateSeal(company);
		//Stamper companyKeywordStamper = new Stamper("盖公章", 0, 0);//关键字
		Stamper companyKeywordStamper = new Stamper(1, 0.1f,  0.1f);
		localSignService.sign(companyStamperInput, companyStamperOut,company,companySealData,companyKeywordStamper);
		logger.info("公司签署有外观签名完成.");

	}

	
	@Bean
	public SDKClient sdkClient(){
		String url = "http://openapi.qiyuesuo.net";
		String accessKey = "VLd3gWPAA6";
		String accessSecret = "XDKr9cpVuaeieERaUl8GempbLYaFCK";
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
