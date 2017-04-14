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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.SealService;
import com.qiyuesuo.sdk.api.StandardSignService;
import com.qiyuesuo.sdk.impl.SealServiceImpl;
import com.qiyuesuo.sdk.impl.StandardSignServiceImpl;
import com.qiyuesuo.sdk.sign.Stamper;
import com.qiyuesuo.sdk.signer.AuthLevel;
import com.qiyuesuo.sdk.standard.Receiver;
import com.qiyuesuo.sdk.standard.StandardContract;

/**
 * 
 * 契约锁 JAVA SDK 标准签实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数;</br>
 * @author wise
 * 
 */
@SpringBootApplication
public class StandardSignSample {

	private static final Logger logger = LoggerFactory.getLogger(StandardSignSample.class);
	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext context = SpringApplication.run(StandardSignSample.class, args);
		context.registerShutdownHook();
		context.start();
		
		StandardSignService standardSignService  = context.getBean(StandardSignService.class);
		Long documentid = null;
		Long categoryId = 2232065308875554816l;//合同分类ID，需到契约锁云平台【分类管理】获取
		// ====================================================
		// 根据文件创建合同
		InputStream inputStream = new FileInputStream(new File("D://NoSign.pdf"));
		// 创建合同接收人信息
		Receiver receiver = new Receiver();
		receiver.setAuthLevel(AuthLevel.BASIC);
		receiver.setName("老丁");
		receiver.setMobile("134****1093");
		receiver.setType(Receiver.TYPE_PERSONAL);

		List<Receiver> receivers = new ArrayList<Receiver>();
		receivers.add(receiver);
		//使用默认合同分类创建合同
		documentid = standardSignService.create(inputStream,receivers,"测试API合同");
		logger.info("标准签使用默认合同分类创建文件合同完成，生成文件id:{}",documentid);
		//使用指定合同分类创建模版合同
		inputStream = new FileInputStream(new File("D://NoSign.pdf"));
		documentid = standardSignService.create(inputStream,receivers,"测试分类管理合同",categoryId);
		logger.info("标准签使用指定合同分类创建文件合同完成，生成文件id:{}",documentid);
		safeClose(inputStream);
		
		// ====================================================
		// 根据模版创建合同
		//模版id,可去契约锁平台【模版管理】板块获取
		Long templateId = 2284249661201973248l;
		//模版参数
		Map<String, String> templateParams = new HashMap<String, String>();
		templateParams.put("name", "laoding");
		templateParams.put("age", "11");
		//使用默认合同分类创建模版合同
		documentid = standardSignService.create(templateId,templateParams,receivers,"测试模版合同");
		logger.info("标准签使用默认合同分类创建模版合同完成，生成文件id:{}",documentid);
		//使用指定合同分类创建模版合同
		//合同分类ID，需到契约锁云平台【分类管理】获取
		Long tempCategoryId = 2232065308875554816l;
		documentid = standardSignService.create(templateId,templateParams,receivers,"测试模版合同",tempCategoryId);
		logger.info("标准签使用指定合同分类创建模版合同完成，生成文件id:{}",documentid);
	
		// ====================================================
		// 合同签署
		// 签署页码和位置
		Stamper stamper = new Stamper(1, 0.5f, 0.5f);
		//公章编号，需前往契约锁平台【公章管理】获取
		Long sealId = 2201194154317316096l;
		standardSignService.sign(documentid, sealId, stamper);
		logger.info("标准签合同签署完成");
		
		// ====================================================
		// 获取合同详情
		StandardContract contract = standardSignService.detail(documentid);
		logger.info("根据文档id获取详情完成:{}", contract);
		
		
		// ====================================================
		// 合同文件下载 （ZIP）
		FileOutputStream outputZip = new FileOutputStream("D:/outputZip.zip");
		standardSignService.download(documentid, outputZip);
		safeClose(outputZip);
		logger.info("标准签合同下载完成");
		
		
		// ====================================================
		// 合同文件下（PDF）
		FileOutputStream outputDoc = new FileOutputStream("D:/outputDoc.pdf");
		standardSignService.downloadDoc(documentid, outputDoc);
		safeClose(outputDoc);
		logger.info("标准签合同下载完成");
		
		//=====================================================
		//根据html使用默认合同分类创建模版合同
		String html = "<html><body><p>title</p><p>在线第三方电子合同平台。企业及个人用户可通过本平台与签约方快速完成合同签署，安全、合法、有效。</p></body></html>";
		documentid = standardSignService.create(html, receivers, "标准签之根据html创建合同");
		logger.info("根据html使用默认合同分类创建模版合同完成,documentId:{}",documentid);
		
		//根据html使用指定合同分类创建模版合同
		documentid = standardSignService.create(html, receivers, "标准签之根据html创建合同",categoryId);
		logger.info("根据html使用默认合同分类创建模版合同完成,documentId:{}",documentid);
	}
	
	@Bean
	public SDKClient sdkClient(){
		String url = "http://openapi.qiyuesuo.net";
		String accessKey = "VLd3gWPAA6";
		String accessSecret = "XDKr9cpVuaeieERaUl8GempbLYaFCK";
		return new SDKClient(url,accessKey,accessSecret);
	}
	
	@Bean
	public StandardSignService standardSignService(SDKClient sdkClient){
		return new StandardSignServiceImpl(sdkClient);
	}
	
	@Bean
	public SealService sealService(SDKClient sdkClient){
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
