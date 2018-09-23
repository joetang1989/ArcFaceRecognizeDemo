//package com.arcsoft.sdk_demo.tts;
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.text.TextUtils;
//import android.util.Log;
//
//import java.io.File;
//
//
///**
// * 弹出提示框，下载服务组件
// */
//public class ApkInstaller {
//    private String TAG =  "ApkInstaller";
//    private final String VOICE_APP = "VoiceNote_3.0.1120.apk";
//    private final String VOICE_APP_ZIP = "VoiceNote_3.0.1120.zip";
//    private long lastInstallTime;
//    public ApkInstaller() {}
//
//    public void installFromAssets() {
////        if (System.currentTimeMillis()-lastInstallTime >10*60*1000) {
////            ToastUtil.showToast(App.sContext,"未安装语音组件,正在启动安装...");
////            lastInstallTime = System.currentTimeMillis();
////            if (!new File(FileUtil.getDataDir(App.sContext,Config.DATA_ROOT)+File.separator+VOICE_APP).exists()) {
////                new LoadFileTask(App.sContext).execute(VOICE_APP_ZIP);
////            } else {
////                processInstall(App.sContext,FileUtil.getDataDir(App.sContext, Config.DATA_ROOT)+File.separator+VOICE_APP);
////            }
////        }
//    }
//
//    /**
//     * 如果服务组件没有安装打开语音服务组件下载页面，进行下载后安装。
//     */
//    private boolean processInstall(Context context , String filePath){
//        if (new File(filePath).exists()) {
//            Intent itent = new Intent(Intent.ACTION_VIEW);
//            itent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            itent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
//            context.startActivity(itent);
//            return true;
//        }
//       return false;
//    }
//
////    public class LoadFileTask extends AsyncTask<String, Integer, Boolean> {
////        Context context;
////
////        public LoadFileTask(Context context) {
////            this.context = context;
////        }
////
////        @Override
////        protected Boolean doInBackground(String... params) {
////            try {
////                String dataRootPath = FileUtil.getDataDir(App.sContext,Config.DATA_ROOT);
////                File file = new File(dataRootPath + File.separator);
////                if (!file.exists()) {
////                    file.mkdirs();
////                }
////                for (int i = 0; i < params.length; i++) {
////                    String fileName = params[i];
////                    if (!TextUtils.isEmpty(fileName)) {
////                        //从assets复制到应用数据根目录
////                        FileUtil.assetsDataToSD(context,fileName, dataRootPath + File.separator + fileName);
////                        //解压语音包文件
////                        UnzipFromAssetsUtil.unZipFolder(dataRootPath + File.separator + fileName, dataRootPath);
////                        File clientMode = new File(dataRootPath + File.separator + fileName);
////                        if (clientMode.exists()) {
////                            FileUtil.delFile(clientMode);//删除压缩包
////                        }
////                    } else {
////                        return false;
////                    }
////                }
////            } catch (Exception e) {
////                Log.i(TAG, "--模型语音包失败:" + e.getMessage());
////                return false;
////            }
////            return true;
////        }
////
////        @Override
////        protected void onPostExecute(Boolean result) {
////            if (result) { //拷贝文件的任务执行成功
////                processInstall(App.sContext,FileUtil.getDataDir(App.sContext,Config.DATA_ROOT)+File.separator+VOICE_APP);
////            } else {
////                ToastUtil.showToast(App.sContext,"加载语音包失败,请删除应用目录下语音组件应用文件后重试！");
////            }
////            super.onPostExecute(result);
////        }
////
////    }
//}
//
