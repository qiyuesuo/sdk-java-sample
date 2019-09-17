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
import com.qiyuesuo.sdk.api.AuthService;
import com.qiyuesuo.sdk.auth.AuthFileType;
import com.qiyuesuo.sdk.auth.AuthUrlResponse;
import com.qiyuesuo.sdk.auth.CompanyAuth;
import com.qiyuesuo.sdk.auth.CompanyAuthDetailResponse;
import com.qiyuesuo.sdk.impl.AuthServiceImpl;
import com.qiyuesuo.sdk.utils.JsonUtils;

/**
 * 契约锁 JAVA SDK 公司认证接口实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱、图片均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * @author GJ
 */
@SpringBootApplication
public class CompanyAuthSample {

	private static final Logger logger = LoggerFactory.getLogger(CompanyAuthSample.class);
	
	private static File pictureFile = new File("./license.png"); //测试文档
	private static File outFile = new File("./sign/auth");   //测试输出文档路径
	static {
		if(!outFile.exists()) {
			outFile.mkdirs();
		}
	}
	
	private static Long applyBasicId =  2492557572920755143L;

	public static void main(String[] args) throws Exception {

		ConfigurableApplicationContext context = SpringApplication.run(CompanyAuthSample.class, args);
		context.registerShutdownHook();
		context.start();

		AuthService authService = context.getBean(AuthService.class);

		// ========================================================================
		// 申请基本认证
		CompanyAuth basicAuth = new CompanyAuth();
		basicAuth.setName("jdk-sample测试公司");
		basicAuth.setRegisterNo("330100400015575");
		basicAuth.setProvince("上海");
		basicAuth.setCity("上海");
		basicAuth.setLegalPerson("张三");
		basicAuth.setContact("李四");
		basicAuth.setContactPhone("12312312312");
		InputStream license = new FileInputStream(pictureFile);
		InputStream operAuthorization = new FileInputStream(pictureFile);
		basicAuth.setLicense(license);
		basicAuth.setOperAuthorization(operAuthorization);
		applyBasicId = authService.applyBasic(basicAuth);
		logger.info("申请基本认证完成，结果:{}",applyBasicId);
		
		// ========================================================================
		//申请加强认证；申请加强认证需要认证企业向契约锁账户打款（10元以下）来认证
		CompanyAuth fullAuth = new CompanyAuth();
		fullAuth.setName("jdk-sample测试公司");
		fullAuth.setRegisterNo("330100400015575");
		fullAuth.setProvince("上海");
		fullAuth.setCity("上海");
		fullAuth.setLegalPerson("张三");
		fullAuth.setContact("李四");
		fullAuth.setContactPhone("12312312312");
		InputStream fullLicense = new FileInputStream(pictureFile);
		InputStream fullOperAuthorization = new FileInputStream(pictureFile);
		fullAuth.setLicense(fullLicense);
		fullAuth.setOperAuthorization(fullOperAuthorization);
		Long applyFull = authService.applyFull(fullAuth);
		logger.info("申请加强认证完成，结果:{}",applyFull);
		
		// ========================================================================
		//查询认证状态
		CompanyAuthDetailResponse response = authService.detail("jdk-sample测试公司");
		logger.info("查询认证状态完成，结果:{}",JsonUtils.toJson(response));
		
		// ========================================================================
		//下载认证文件（营业执照、授权委托书）
		OutputStream fos = new FileOutputStream(new File(outFile,"LICENSE.png"));
		authService.downloadFile(applyBasicId, AuthFileType.LICENSE, fos);
		logger.info("下载认证文件完成");
		
		// ========================================================================
		//初级、加强认证页面地址
		AuthUrlResponse authUrl = authService.basicUrl("jdk-sample测试公司", "330100400015575","http://www.baidu.com");
		logger.info("初级认证页面地址完成，结果:{}",JsonUtils.toJson(authUrl));
		AuthUrlResponse fullUrl = authService.fullUrl("jdk-sample测试公司", "330100400015575","http://www.baidu.com");
		logger.info("加强认证页面地址完成，结果:{}",JsonUtils.toJson(fullUrl));
		
		// ========================================================================
		//认证查看地址
		AuthUrlResponse viewUrl = authService.viewUrl("jdk-sample测试公司");
		logger.info("认证查看地址完成，结果:{}",JsonUtils.toJson(viewUrl));
	}

	@Bean
	public SDKClient sdkClient() {
		String url = "https://openapi.qiyuesuo.cn"; // 测试环境
		String accessKey = "替换为您开放平台Access Token";
		String accessSecret = "替换为您开放平台Access Secret";
		return new SDKClient(url, accessKey, accessSecret);
	}

	@Bean
	public AuthService authService(SDKClient sdkClient) {
		return new AuthServiceImpl(sdkClient);
	}

}
