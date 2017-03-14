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
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.qiyuesuo.sdk.SDKClient;
import com.qiyuesuo.sdk.api.SealService;
import com.qiyuesuo.sdk.api.Stamper;
import com.qiyuesuo.sdk.api.StandardSignService;
import com.qiyuesuo.sdk.impl.SealServiceImpl;
import com.qiyuesuo.sdk.impl.StandardSignServiceImpl;
import com.qiyuesuo.sdk.standard.AuthenticationLevel;
import com.qiyuesuo.sdk.standard.Receiver;
import com.qiyuesuo.sdk.standard.request.StandardCreateByFileRequest;
import com.qiyuesuo.sdk.standard.request.StandardCreateByTemplateRequest;
import com.qiyuesuo.sdk.standard.response.StandardContract;

/**
 * 契约锁 JAVA SDK 标准签实例代码</br>
 * 本示例代码仅展示了如何使用契约锁java sdk，代码中的姓名、手机、邮箱均为虚拟数据;</br>
 * 示例中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数;</br>
 * 此Sample适用于2.0.0版本
 * @author wise
 * @version 2.0.0
 */
@SpringBootApplication
public class StandardSignSample {

	private static final Logger logger = LoggerFactory.getLogger(LocalSignSample.class);
	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext context = SpringApplication.run(StandardSignSample.class, args);
		context.registerShutdownHook();
		context.start();
		
		StandardSignService standardSignService  = context.getBean(StandardSignService.class);
		Long documentid = null;
		
		// ====================================================
		// 根据文件创建合同
		InputStream inputStream = new FileInputStream(new File("D://NoSign.pdf"));
		// 创建合同接收人信息
		Receiver receiver = new Receiver();
		receiver.setAuthLevel(AuthenticationLevel.BASIC);
		receiver.setName("测试公司");
		receiver.setMobile("12312312312");
		receiver.setAgent("张三");
		receiver.setType(Receiver.TYPE_COMPANY);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 1);
		
		StandardCreateByFileRequest request = new StandardCreateByFileRequest();
		request.setFile(inputStream);
		request.addReceiver(receiver);
		request.setDocName("员工合同文档");
		request.setExpireTime(c.getTime());
		request.setSubject("员工合同主题");
		//合同分类ID
		request.setCategoryId(null); 
		documentid = standardSignService.create(request);
		safeClose(inputStream);
		logger.info("标准签创建文件合同完成，生成文件id:{}",documentid);
		
		
		// ====================================================
		// 根据模版创建合同
		StandardCreateByTemplateRequest tempRequest = new StandardCreateByTemplateRequest();
		//模版id,可去契约锁平台【模版管理】板块获取
		tempRequest.setTemplateId(2237141379182493696l); 
		tempRequest.addTemplate("param1", "value1");
		tempRequest.addTemplate("param2", "value2");
		//receiver事例同上面的根据文件创建合同的receiver一致
		tempRequest.addReceiver(receiver);
		tempRequest.setDocName("员工合同文档");
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.MONTH, 1);
		tempRequest.setExpireTime(ca.getTime());
		tempRequest.setSubject("员工合同主题");
		tempRequest.setCategoryId(2278742364627402752L);
		documentid = standardSignService.create(request);
		logger.info("标准签创建模版合同完成，生成文件id:{}",documentid);

		
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
		StandardContract contract = standardSignService.acquireDetail(documentid);
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
		
		
		//获取签署连接
		String signUrl = standardSignService.signUrl(documentid, "http://www.baidu.com", null);
		logger.info("签署链接：{}", signUrl);
		
		
		//查看合同连接
		String viewUrl = standardSignService.viewUrl(documentid);
		logger.info("查看链接：{}", viewUrl);
		
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
