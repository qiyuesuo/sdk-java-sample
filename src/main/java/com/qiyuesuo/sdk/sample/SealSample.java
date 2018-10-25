/**
 * Copyright (C) 2017,  上海亘岩网络科技有限公司 All rights reserved All rights reserved.
 */
package com.qiyuesuo.sdk.sample;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.SealService;
import com.qiyuesuo.sdk.impl.SealServiceImpl;
import com.qiyuesuo.sdk.seal.Seal;
import com.qiyuesuo.sdk.signer.Company;
import com.qiyuesuo.sdk.signer.PaperType;
import com.qiyuesuo.sdk.signer.Person;
import com.qiyuesuo.sdk.utils.JsonUtils;

/**
 * 契约锁 JAVA SDK 印章接口实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * @author GJ
 */
@SpringBootApplication
public class SealSample {
	
	private static final Logger logger = LoggerFactory.getLogger(SealSample.class);
	
	private static Long sealId = 2492283309478498330L; // 印章编号，在契约锁云平台【公司印章】中获取
	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext context = SpringApplication.run(SealSample.class, args);
		context.registerShutdownHook();
		context.start();
		
		SealService sealService = context.getBean(SealService.class);
		
		//========================================================================
		//生成个人电子印章
		Person person = new Person("张三");
		person.setPaper(PaperType.IDCARD,"311312183706206418");
		person.setMobile("13400000000");
		person.setEmail("san.zhang@qiyuesuo.com");
		String personSeal = sealService.generateSeal(person);
		logger.info("生成个人电子印章完成,结果:{}",personSeal);
		
		//========================================================================
		//生成企业电子印章
		Company company = new Company("API测试公司");
		company.setRegisterNo("123123423523");
		company.setAddress("上海");
		String companySeal = sealService.generateSeal(company);
		logger.info("生成企业电子印章完成,结果:{}",companySeal);
		
		//========================================================================
		//获取所有可用的平台印章
		List<Seal> sealList = sealService.sealList();
		logger.info("获取所有可用的平台印章完成,结果:{}",JsonUtils.toJson(sealList));
		
		//========================================================================
		//查找印章
		String seal = sealService.findSeal(sealId);
		logger.info("获查找印章完成,结果:{}",seal);

	}

	
	@Bean
	public SDKClient sdkClient(){
		String url = "https://openapi.qiyuesuo.me"; //测试环境
		String accessKey = "fH0pNA83NA";
		String accessSecret = "okE2PhHXiKapiWNnkPhwV4WfBjOL00";
		return new SDKClient(url,accessKey,accessSecret);
	}
	
	
	@Bean
	public SealService sealService(SDKClient sdkClient) {
		return new SealServiceImpl(sdkClient);
	}
	
}
