# ArcFaceRecognizeDemo
Free SDK demo 基于虹软ArcFaceDemo，可以在本机通过浏览器注册人脸或访问主页面下方地址注册人脸。

## 1.修改点
 1.去掉年龄、性别模块，仅保留人脸检测识别相关算法业务
 2.添加人脸方式由拍照改为网页上传姓名、图片数据，以此模拟Web后台添加注册人脸
 3.Application.decodeImage()函数加入图片最小宽高判定，小于最小尺寸会按比例缩放
 以增加寸照注册的成功率(自测时修改此处业务代码，可依据具体情况进行调整)

## 2.工程如何使用？
 1. 下载代码:    
    https://github.com/joetang1989/ArcFaceRecognizeDemo.git 或者直接下载压缩包
 2. 前往[官网](http://www.arcsoft.com.cn/ai/arcface.html)申请appid和sdkkey。    
    修改 ArcFaceRecognizeDemo\src\main\java\com\arcsoft\sdk_demo\FaceDB.java 下面的对应的值:    
   
    ```java    
    public static String appid = "xxxx"; 		
    public static String fd_key = "xxxx";    
    public static String ft_key = "xxxx";
    public static String fr_key = "xxxx";
    ```
 3. 下载sdk包之后，解压各个包里libs中的文件(*新*)到 ArcFaceRecognizeDemo\libs 下，同名so直接覆盖或直接使用工程现有so(*旧*)。
 4. Android Studio3.0(及以上)中直接打开或者导入Project,编译运行即可。    

## 3.demo如何使用?    

 1. 点击"点我本机注册"，跳转至浏览器注册页面，上传信息即可或者使用其他设备连接到
与人脸识别设备连接同一个热点，打开浏览器，地址栏输入:http://xxx:8688/register.html，
上传信息。
 *备注:xxx：人脸识别设备ip*   
 2. demo中人脸数据的保存方式?  
　注册图片保存在外置存储根目录；特征及人名信息保存在数据库中。

 

---------------
##4.FAQ
1. 参见ArcFaceDemo readme.md(https://github.com/asdfqwrasdf/ArcFaceDemo/readme.md)   
	
2. 加入虹软微信支持群@汤小泽。,说明具体即可 

##5.鸣谢

 - 虹软提供免费算法
 - GreenDao、AndServer等第三方库作者

---------------
##6.申明
最后，我只是简单搬运、整合了一下各个业务模块做了一个简单Demo；
具体业务实现需根据自己业务需求来实现。

