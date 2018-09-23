package com.arcsoft.sdk_demo.server;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.sdk_demo.Application;
import com.arcsoft.sdk_demo.FaceDB;
import com.guo.android_extend.image.ImageConverter;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.upload.HttpFileUpload;
import com.yanzhenjie.andserver.upload.HttpUploadContext;
import com.yanzhenjie.andserver.util.HttpRequestParser;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.httpcore.HttpEntityEnclosingRequest;
import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册Handler
 */
public class RegisterHandler implements RequestHandler {
    static String TAG = "RegisterHandler";
    static final int REQUEST_OK = 200;
    static final int REQUEST_INVALID = 400;

    public interface EXTRA_FEATURE{
        /*信息不正确*/
        int INFO_INVALID = -1;
        /*特征提取成功*/
        int OK = 1;
        /*没有检测到人脸*/
        int NO_FACE = -2;
        /*特征提取失败*/
        int FEATURE_INVALID = -3;
        /*其他错误*/
        int OTHER_ERROR = -4;
    }

    /**
     * 对应表单key
     */
    static final String KEY_USER_NAME = "username";
    static final String KEY_REGISTER_IMG = "registerimg";

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!HttpRequestParser.isMultipartContentRequest(request)) { // Is POST and upload.
            StringEntity stringEntity = new StringEntity("需要上传注册照片.", "utf-8");
            response.setStatusCode(REQUEST_INVALID);
            response.setEntity(stringEntity);
        } else {
            String fileUploadDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            FileItemFactory factory = new DiskFileItemFactory(1024 * 1024, new File(fileUploadDir));
            HttpFileUpload fileUpload = new HttpFileUpload(factory);

            try{
                List<FileItem> fileItems = fileUpload.parseRequest(new HttpUploadContext((HttpEntityEnclosingRequest) request));
                String fileName = null;
                File uploadFile = null;
                HashMap<String,String> params = new HashMap<>();
                for (FileItem fileItem : fileItems) {
                    if (!fileItem.isFormField()) { // File param.
                        Log.w(TAG,"file name:"+fileItem.getName());
                        fileName = fileItem.getName();
                        //文件后缀名
                        String fileSubName = fileName.substring(fileName.lastIndexOf(".")+1);
                        //通过文件后缀名判断是否为图片
                        if (fileSubName.equalsIgnoreCase("jpeg") ||fileSubName.equalsIgnoreCase("jpg")
                                ||fileSubName.equalsIgnoreCase("png")){
                            //保存图片文件
                            uploadFile = new File(fileUploadDir,fileName);
                            fileItem.write(uploadFile);
                            params.put(KEY_REGISTER_IMG,uploadFile.getAbsolutePath());
                        }else{
                            StringEntity stringEntity = new StringEntity("需上传图片.", "utf-8");
                            response.setStatusCode(REQUEST_INVALID);
                            response.setEntity(stringEntity);
                            return;
                        }
                    } else { // General param.
                        String key = fileItem.getFieldName();
                        String value = new String(fileItem.getString("UTF-8"));
                        Log.d(TAG,"key:"+key+"  vlaue:"+value);
                        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)){
                            params.put(key,value);
                        }

                    }

                }


                if (params.containsKey(KEY_USER_NAME) && params.containsKey(KEY_REGISTER_IMG)){
                    //同步提取人脸特征
                    int result = registerSync(params);
                    //hard code
                    StringEntity stringEntity = null;
                    switch (result){
                        case EXTRA_FEATURE.OK:
                            stringEntity = new StringEntity("**注册信息填写成功**", "utf-8");
                            response.setStatusCode(REQUEST_OK);
                            response.setEntity(stringEntity);
                            break;
                        case EXTRA_FEATURE.FEATURE_INVALID:
                            stringEntity = new StringEntity("提取特征失败,请更换照片.", "utf-8");
                            response.setStatusCode(REQUEST_INVALID);
                            response.setEntity(stringEntity);
                            break;
                        case EXTRA_FEATURE.NO_FACE:
                            stringEntity = new StringEntity("没有检测到人脸,请更换照片..", "utf-8");
                            response.setStatusCode(REQUEST_INVALID);
                            response.setEntity(stringEntity);
                            break;
                        default:
                            stringEntity = new StringEntity("请正确填写信息.", "utf-8");
                            response.setStatusCode(REQUEST_INVALID);
                            response.setEntity(stringEntity);
                            break;
                    }

                }else{
                    StringEntity stringEntity = new StringEntity("请正确填写信息.", "utf-8");
                    response.setStatusCode(REQUEST_INVALID);
                    response.setEntity(stringEntity);
                }

            }catch(Exception e){
                StringEntity stringEntity = new StringEntity("请正确填写信息."+e.getMessage(), "utf-8");
                response.setStatusCode(REQUEST_INVALID);
                response.setEntity(stringEntity);
            }

        }
    }

    /**
     * 同步提取特征
     * @param params
     * @return
     */
    private int registerSync(final Map<String, String> params) {
        int registerResult = -1;
        if (params.containsKey(KEY_USER_NAME) && params.containsKey(KEY_REGISTER_IMG)) {
            final String extFileDir = params.get(KEY_REGISTER_IMG);
            final File registerFile = new File(extFileDir);
            if (registerFile.exists() && registerFile.canRead()) {
                Bitmap mBitmap;
                Rect src = new Rect();
                String filePath = registerFile.getAbsolutePath();
                final String registerName = params.get(KEY_USER_NAME);

                mBitmap = Application.decodeImage(filePath);
                src.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
                byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight() * 3 / 2];
                try {
                    ImageConverter convert = new ImageConverter();
                    convert.initial(mBitmap.getWidth(), mBitmap.getHeight(), ImageConverter.CP_PAF_NV21);
                    if (convert.convert(mBitmap, data)) {
                        Log.d(TAG, "convert ok!");
                    }

                    AFD_FSDKEngine engine = new AFD_FSDKEngine();
                    List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
                    AFD_FSDKError err = engine.AFD_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.fd_key, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
                    Log.d(TAG, "AFD_FSDK_InitialFaceEngine = " + err.getCode());

                    Log.d(TAG, "AFD_FSDK_StillImageFaceDetection =" + err.getCode() + "<" + result.size());

                    AFR_FSDKEngine engine1 = new AFR_FSDKEngine();
                    AFR_FSDKFace result1 = new AFR_FSDKFace();
                    AFR_FSDKError error1 = engine1.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
                    Log.d("com.arcsoft", "AFR_FSDK_InitialEngine = " + error1.getCode());

                    err = engine.AFD_FSDK_StillImageFaceDetection(data, mBitmap.getWidth(), mBitmap.getHeight(), AFD_FSDKEngine.CP_PAF_NV21, result);
                    Log.d(TAG, "AFD_FSDK_StillImageFaceDetection =" + err.getCode() + "<" + result.size());

                    if (!result.isEmpty()) {
                        //提取特征
                        error1 = engine1.AFR_FSDK_ExtractFRFeature(data, mBitmap.getWidth(), mBitmap.getHeight(), AFR_FSDKEngine.CP_PAF_NV21, new Rect(result.get(0).getRect()), result.get(0).getDegree(), result1);
                        Log.d("com.arcsoft", "Face=" + result1.getFeatureData()[0] + "," + result1.getFeatureData()[1] + "," + result1.getFeatureData()[2] + "," + error1.getCode());
                        if (error1.getCode() == AFR_FSDKError.MOK) {
                            //信息保存到数据库中
                            Application.getInstance().getFaceDB().addFace(registerName, result1.clone());
                            registerResult = EXTRA_FEATURE.OK;
                        } else {
                            Log.d(TAG, "-----特征提取失败 " + registerName);
                            registerResult =  EXTRA_FEATURE.FEATURE_INVALID;
                        }
                    } else {
                        Log.d(TAG, "-----没有检测到人脸 " + registerName);
                        registerResult = EXTRA_FEATURE.NO_FACE;
                    }

                    err = engine.AFD_FSDK_UninitialFaceEngine();
                    Log.d(TAG, "AFD_FSDK_UninitialFaceEngine =" + err.getCode());
                    convert.destroy();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "-----其他错误:" + e.getMessage());
                    registerResult =  EXTRA_FEATURE.OTHER_ERROR;
                }
            }
        } else {
            Log.d(TAG, "-----上传的数据不完整");
            registerResult = EXTRA_FEATURE.INFO_INVALID;
        }
        return registerResult;
    }

}
