/**
 * Copyright (C) 2017,  上海亘岩网络科技有限公司 All rights reserved All rights reserved.
 */
package com.qiyuesuo.sdk.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.StorageService;
import com.qiyuesuo.sdk.impl.StorageServiceImpl;

/**
 * 契约锁 JAVA SDK 模板接口实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数
 * @author GJ
 */
@SpringBootApplication
public class StorageSample {
	
	private static final Logger logger = LoggerFactory.getLogger(StorageSample.class);
	
	private static Long fileId = null; 
	private static File pdfFile = new File("./NoSign.pdf"); //测试文档
	private static File outFile = new File("./sign/storage");   //测试输出文档路径
	static {
		if(!outFile.exists()) {
			outFile.mkdirs();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext context = SpringApplication.run(TemplateSample.class, args);
		context.registerShutdownHook();
		context.start();
		
		StorageService storageService = context.getBean(StorageService.class);
		
		//========================================================================
		// 存储文件并公证
		FileInputStream input = new FileInputStream(pdfFile);
		String contentType = "text/plain";
		String fileName = "NoSign.pdf";
		fileId = storageService.upload(input, fileName, contentType, true);
		logger.info("存储文件并公证完成，结果{}：",fileId);

		//========================================================================
		// 下载文件
		OutputStream outputStream = new FileOutputStream(new File(outFile,"download.pdf"));
		storageService.download(fileId, outputStream);
		logger.info("下载成功");
	}

	
	@Bean
	public SDKClient sdkClient(){
		String url = "https://openapi.qiyuesuo.me"; //测试环境
		String accessKey = "fH0pNA83NA";
		String accessSecret = "okE2PhHXiKapiWNnkPhwV4WfBjOL00";
		return new SDKClient(url,accessKey,accessSecret);
	}
	
	@Bean
	public StorageService storageService(SDKClient sdkClient){
		return new StorageServiceImpl(sdkClient);
	}
	
}
