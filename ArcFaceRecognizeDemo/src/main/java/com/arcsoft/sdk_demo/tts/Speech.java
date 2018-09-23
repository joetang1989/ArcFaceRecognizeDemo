//package com.arcsoft.sdk_demo.tts;
//
//import android.os.Bundle;
//import android.util.Log;
//import com.arcsoft.sdk_demo.Application;
//import com.iflytek.cloud.ErrorCode;
//import com.iflytek.cloud.InitListener;
//import com.iflytek.cloud.SpeechConstant;
//import com.iflytek.cloud.SpeechError;
//import com.iflytek.cloud.SpeechSynthesizer;
//import com.iflytek.cloud.SpeechUtility;
//import com.iflytek.cloud.SynthesizerListener;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class Speech {
//    private static final String TAG = "TTS-Speech";
//    private ApkInstaller      mInstaller ;
//    private SpeechSynthesizer mTts;
//    private List<String>      speechTextList;
//    public static final int MAX_TEXTLIST_SIZE = 4;   // 最大未播记录，超过就清空
//    private boolean mSpeechSuccess;
//	private static Speech SPEECH_INSTANCE;
//    private String  strSpeachText    ="";
//    private boolean mTtsSetParameter = true;
//    private boolean mTtsInstalltionCalled = false;
//    //static Reference<Activity> activityRef;
//    public static Speech getSpeechInstance(){
//        if (SPEECH_INSTANCE ==null) {
//            SPEECH_INSTANCE = new Speech(3);
//        }
//        return SPEECH_INSTANCE;
//    }
//
//    private Speech(int maxSize){
//            mTtsSetParameter = true;
//            strSpeachText ="";
//            SpeechUtility.createUtility(Application.getInstance(), "appid=575e54e5");
//            mTts = SpeechSynthesizer.createSynthesizer(Application.getInstance(), mTtsInitListener);
//            speechTextList = new ArrayList<String>();
//            mInstaller = new  ApkInstaller();
//    }
//
//    private InitListener mTtsInitListener = new InitListener() {
//        @Override
//        public void onInit(int code) {
//            if (code != ErrorCode.SUCCESS) {
//                mSpeechSuccess = false;
//                Log.d(TAG,"初始化失败,错误码："+code);
//            } else {
//                // 初始化成功，之后可以调用startSpeaking方法
//                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
//                // 正确的做法是将onCreate中的startSpeaking调用移至这里
//                mSpeechSuccess = true;
//            }
//        }
//    };
//
//    public void ttsPlay(String text) {
//        if (SpeechUtility.getUtility() != null && !SpeechUtility.getUtility().checkServiceInstalled() && !mTtsInstalltionCalled) {
//            mTtsInstalltionCalled = true;
//            mInstaller.installFromAssets();
//        }
//        if (SpeechUtility.getUtility() == null || !mSpeechSuccess) {
//            Log.e("VOICE","mSpeechSuccess="+mSpeechSuccess+" return back,no tts play.");
//            return;
//        }
//        synchronized (this) {
//            if (mTts.isSpeaking() && speechTextList != null) {
//                if(strSpeachText.length() > 0){
//                    if( strSpeachText.equals(text) ){
//                        return;
//                    }
//                }
//
//                for (String temp :speechTextList) {
//                    if (temp.equals(text)) {
//                        return; //待播报列表中已有记录
//                    }
//                }
//
//                if ( speechTextList.size() >= MAX_TEXTLIST_SIZE) {
//                    speechTextList.remove(speechTextList.size() - 1);
//                }
//
//                speechTextList.add(0, text);
//                return;
//            }
//            toPlay(text);
//        }
//    }
//
//    private  void toPlay(String text){
//        if(mTts == null || mTtsListener == null ) {
//            return;
//        }
//
//        try {
//            if(mTtsSetParameter) {
//                //设置发音人
//                mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
//                mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
//                mTts.setParameter(SpeechConstant.PITCH, "50");
//                mTts.setParameter(SpeechConstant.VOLUME, "100");
//                mTtsSetParameter = false;
//            }
//
//            int code = mTts.startSpeaking(text,mTtsListener);
//            strSpeachText = text;
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.d(TAG,"语音合成异常--"+e.getMessage());
//        }
//    }
//
//    public void ttsPause(){
//        mTts.pauseSpeaking();
//    }
//
//    private void  ttsSpeechNext(){
//        String text = null;
//        // 播放下一句
//        synchronized (this){
//            if(speechTextList != null && speechTextList.size() > 0){
//                text = speechTextList.remove(speechTextList.size() -1 );
//            }
//            if(text != null && text.length() > 0) {
//                toPlay(text);
//            }
//        }
//    }
//
//    private SynthesizerListener mTtsListener = new SynthesizerListener() {
//        @Override
//        public void onSpeakBegin() {
//        }
//        @Override
//        public void onSpeakPaused() {
//            mTts.resumeSpeaking();//fix bug:0002960.语音播报无声音.
//        }
//        @Override
//        public void onSpeakResumed() {
//        }
//        @Override
//        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
//        }
//        @Override
//        public void onSpeakProgress(int percent, int beginPos, int endPos) {
//        }
//
//        @Override
//        public void onCompleted(SpeechError error) {
//            strSpeachText = "";
//
//            if (error == null) {
//                ttsSpeechNext();
//            } else if (error != null) {
//                Log.d(TAG, "SpeechError ---"+error.getMessage());
//            }
//        }
//
//        @Override
//        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
//           // Log.i(TAG,"TtsListener onEvent()出错:beventType="+eventType+"arg1= "+arg1+" arg2= "+arg2+" bundle= "+(obj ==null?"obj为空":obj));
//            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
//            // 若使用本地能力，会话id为null
//            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
//            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
//            //		Log.d(TAG, "session id =" + sid);
//            //	}
//        }
//    };
//}
