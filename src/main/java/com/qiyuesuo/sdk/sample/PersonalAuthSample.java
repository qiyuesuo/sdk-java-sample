/**
 * Copyright (C) 2017,  上海亘岩网络科技有限公司 All rights reserved All rights reserved.
 */
package com.qiyuesuo.sdk.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.PersonalAuthService;
import com.qiyuesuo.sdk.auth.PersonalAuth;
import com.qiyuesuo.sdk.auth.PersonalAuthResponse;
import com.qiyuesuo.sdk.impl.PersonalAuthServiceImpl;
import com.qiyuesuo.sdk.utils.JsonUtils;

/**
 * 契约锁 JAVA SDK 个人认证接口实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * @author GJ
 */
@SpringBootApplication
public class PersonalAuthSample {

	private static final Logger logger = LoggerFactory.getLogger(PersonalAuthSample.class);

	public static void main(String[] args) throws Exception {

		ConfigurableApplicationContext context = SpringApplication.run(PersonalAuthSample.class, args);
		context.registerShutdownHook();
		context.start();

		PersonalAuthService personalAuthService = context.getBean(PersonalAuthService.class);

		// ========================================================================
		// 获取认证地址,用于进行个人实名认证
		PersonalAuth personalAuth = new PersonalAuth();
		personalAuth.setBizId("20181024");
		personalAuth.setIdCardNo("123123123123123123");
		personalAuth.setMobile("14900000000");
		personalAuth.setUsername("张三");

		String personalAuthUrl = personalAuthService.authUrl(personalAuth);
		logger.info("获取认证地址完成,结果:{}", personalAuthUrl);

		// ========================================================================
		// 查询个人认证结果
		PersonalAuthResponse response = personalAuthService.queryResult("20181024");
		logger.info("查询个人认证结果完成,结果:{}",JsonUtils.toJson(response));

	}

	@Bean
	public SDKClient sdkClient() {
		String url = "https://openapi.qiyuesuo.me"; // 测试环境
		String accessKey = "fH0pNA83NA";
		String accessSecret = "okE2PhHXiKapiWNnkPhwV4WfBjOL00";
		return new SDKClient(url, accessKey, accessSecret);
	}

	@Bean
	public PersonalAuthService personalAuthService(SDKClient sdkClient) {
		return new PersonalAuthServiceImpl(sdkClient);
	}

}
