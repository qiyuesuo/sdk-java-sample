# 契约锁Java SDK示例

[契约锁](http://www.qiyuesuo.com) 成立于2015年，是新一代数字签名服务领域的领军企业。依托雄厚的企业管理软件服务经验，致力为全国的企业及个人用户提供最具可用性、稳定性及前瞻性的电子签署、数据存证及数据交互服务。 契约锁深耕企业级市场，产品线涵盖电子签署、合同管理、公文签发、数据存证等企业内签署场景，并提供本地签、远程签、标准签等多种API调用方式接入企业内部管理系统。目前主要为教育、旅游、互联网金融、政府事业单位、集团企业、B2B电商、地产中介、O2O等企业及个人提供签署、存证服务。 契约锁严格遵守《中华人民共和国电子签名法》，并联合公安部公民网络身份识别系统（eID）、工商相关身份识别系统、权威CA机构、公证处及律师事务所，确保在契约锁上签署的每一份合同真实且具有法律效力。 契约锁平台由上海亘岩网络科技有限公司运营开发，核心团队具有丰富的企业管理软件、金融支付行业、数字证书行业从业经验，致力于通过技术手段让企业合同签署及管理业务更简单、更便捷。

了解更多契约锁详情请访问 [www.qiyuesuo.com](http://www.qiyuesuo.com).


Requirements
============
Java 1.6 or later.  

Installation
============

前往 [契约锁开放平台](http://open.qiyuesuo.com/download)下载Java SDK及依赖包，并添加到项目中。

### Maven users

将下载的Java SDK上传至Maven私有仓库，或本地仓库，并在项目POM文件中添加:

```xml
<dependency>
	<groupId>com.qiyuesuo.sdk</groupId>
	<artifactId>sdk-java</artifactId>
	<version>1.0.0</version>
</dependency>
<dependency>
	<groupId>org.bouncycastle</groupId>
	<artifactId>bcprov-jdk15on</artifactId>
	<version>1.49</version>
</dependency>
<dependency>
	<groupId>org.bouncycastle</groupId>
	<artifactId>bcpkix-jdk15on</artifactId>
	<version>1.49</version>
</dependency>
```

Usage
=====

#### 本地签
文件无需上传到契约锁云平台，在本地服务器上进行PDF文件的hash运算，再调用云平台的签署接口完成签署。

详情请参考： [LocalSignSample.java](https://github.com/qiyuesuo/sdk-java-sample/blob/master/src/main/java/com/qiyuesuo/sdk/sample/LocalSignSample.java).

#### 远程签
将文件上传的云平台进行签署，或者使用云平台的模板进行签署。

详情请参考： [RemoteSignSample.java](https://github.com/qiyuesuo/sdk-java-sample/blob/master/src/main/java/com/qiyuesuo/sdk/sample/RemoteSignSample.java).

Notes
=======
示例代码中的参数均为测试环境参数，实际运行时需要将相关参数修改为生产环境参数，或沙箱测试环境参数。

扫码关注契约锁公众号,了解契约锁最新动态。

![契约锁公众号](qrcode.png)