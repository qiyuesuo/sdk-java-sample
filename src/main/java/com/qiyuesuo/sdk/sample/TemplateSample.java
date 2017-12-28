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
import com.qiyuesuo.sdk.api.TemplateService;
import com.qiyuesuo.sdk.impl.TemplateServiceImpl;
import com.qiyuesuo.sdk.template.TemplateInfo;

/**
 * 契约锁 JAVA SDK 模板接口实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * @author sgf
 */
@SpringBootApplication
public class TemplateSample {
	
	private static final Logger logger = LoggerFactory.getLogger(TemplateSample.class);
	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext context = SpringApplication.run(TemplateSample.class, args);
		context.registerShutdownHook();
		context.start();
		
		TemplateService templateService = context.getBean(TemplateService.class);
		
		//========================================================================
		// 查询合同模板
		List<TemplateInfo> templateInfos = templateService.queryTemplate();
		logger.info("查询合同模板完成,合同模板数量:{}",templateInfos.size());

		//========================================================================
		// 查询合同模板详情
		TemplateInfo templateInfo = templateService.queryDetail(2291848536332750874L);
		logger.info("查询合同模板详情完成,模板名称:{}",templateInfo.getTitle());

	}

	
	@Bean
	public SDKClient sdkClient(){
		String url = "https://openapi.qiyuesuo.me"; // 测试环境
		String accessKey = "7EswyQzhBe";
		String accessSecret = "lSTLQLZlnCGkdy6MiOhAzIvfbOYlpU";
		return new SDKClient(url,accessKey,accessSecret);
	}
	
	@Bean
	public TemplateService templateService(SDKClient sdkClient){
		return new TemplateServiceImpl(sdkClient);
	}
	
}
