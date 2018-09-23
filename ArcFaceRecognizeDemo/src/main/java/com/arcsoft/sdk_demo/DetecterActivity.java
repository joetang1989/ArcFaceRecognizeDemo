package com.arcsoft.sdk_demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.arcsoft.sdk_demo.entity.RegisteredEmployee;
import com.arcsoft.sdk_demo.server.ServerManager;
import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.tools.CameraHelper;
import com.guo.android_extend.widget.CameraFrameData;
import com.guo.android_extend.widget.CameraGLSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView.OnCameraListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2017/4/28.
 */

public class DetecterActivity extends Activity implements OnCameraListener, View.OnTouchListener, Camera.AutoFocusCallback, View.OnClickListener {
	private final String TAG = this.getClass().getSimpleName();

	private int mWidth, mHeight, mFormat;
	private CameraSurfaceView mSurfaceView;
	private CameraGLSurfaceView mGLSurfaceView;
	private Camera mCamera;

	AFT_FSDKVersion version = new AFT_FSDKVersion();
	AFT_FSDKEngine engine = new AFT_FSDKEngine();
	List<AFT_FSDKFace> result = new ArrayList<>();
	List<RecognizedFace> recognizedList = new ArrayList<>();
	int mCameraID;
	int mCameraRotate;
	boolean mCameraMirror;
	byte[] mImageNV21 = null;
	FRAbsLoop mFRAbsLoop = null;
	AFT_FSDKFace mAFT_FSDKFace = null;
	Handler mHandler;
	boolean isPostted = false;

	/**
	 * 识别特征脸
	 */
	class RecognizedFace{
		private long recognizeTime;
		private AFR_FSDKFace recognizedFace;

		public RecognizedFace(long recognizeTime, AFR_FSDKFace recognizedFace) {
			this.recognizeTime = recognizeTime;
			this.recognizedFace = recognizedFace;
		}

		public long getRecognizeTime() {
			return recognizeTime;
		}

		public void setRecognizeTime(long recognizeTime) {
			this.recognizeTime = recognizeTime;
		}

		public AFR_FSDKFace getRecognizedFace() {
			return recognizedFace;
		}

		public void setRecognizedFace(AFR_FSDKFace recognizedFace) {
			this.recognizedFace = recognizedFace;
		}
	}


	Runnable hide = new Runnable() {
		@Override
		public void run() {
			mTextView.setAlpha(0.5f);
			mImageView.setImageAlpha(128);
			isPostted = false;
		}
	};


	/**
	 * 识别线程
	 */
	class FRAbsLoop extends AbsLoop {

		AFR_FSDKVersion version = new AFR_FSDKVersion();
		//识别引擎
		AFR_FSDKEngine engine = new AFR_FSDKEngine();
		AFR_FSDKFace result = new AFR_FSDKFace();
		List<RegisteredEmployee> mResgist = ((Application)DetecterActivity.this.getApplicationContext()).mFaceDB.mRegister;

		@Override
		public void setup() {
			AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
			Log.d(TAG, "AFR_FSDK_InitialEngine = " + error.getCode());
			error = engine.AFR_FSDK_GetVersion(version);
			Log.d(TAG, "FR=" + version.toString() + "," + error.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
		}

		@Override
		public void loop() {
			if (mImageNV21 != null) {
				final int rotate = mCameraRotate;

				long time = System.currentTimeMillis();
				//根据人脸检测返回人脸坐标裁剪人脸进行特征提取
				AFR_FSDKError error = engine.AFR_FSDK_ExtractFRFeature(mImageNV21, mWidth, mHeight,
						AFR_FSDKEngine.CP_PAF_NV21, mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree(), result);
				Log.d(TAG, "AFR_FSDK_ExtractFRFeature cost :" + (System.currentTimeMillis() - time) + "ms");
				Log.d("DDDD", "AFR_FSDK_ExtractFRFeature cost :" + (System.currentTimeMillis() - time) + "ms");
				Log.d(TAG, "Face=" + result.getFeatureData()[0] + "," + result.getFeatureData()[1] + "," + result.getFeatureData()[2] + "," + error.getCode());
				AFR_FSDKMatching score = new AFR_FSDKMatching();
				float max = 0.0f;
				String name = null;

				//处理短时间类同一个在画面问题
//				if (!recognizedList.isEmpty()){
//					RecognizedFace temp =null;
//					for (RecognizedFace fr : recognizedList) {
//						error = engine.AFR_FSDK_FacePairMatching(result, fr.getRecognizedFace(), score);
//						Log.d(TAG,  "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + error.getCode());
//						if (max < score.getScore()) {
//							max = score.getScore();
//							temp = fr;
//						}
//					}
//					if (max > 0.6f && temp!=null && (System.currentTimeMillis()-temp.recognizeTime < 120*1000)){
//						temp.setRecognizeTime(System.currentTimeMillis());
//						return;
//					}
//				}



				for (RegisteredEmployee fr : mResgist) {
						error = engine.AFR_FSDK_FacePairMatching(result, fr.getFace(), score);
						Log.d(TAG,  "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + error.getCode());
						if (max < score.getScore()) {
							max = score.getScore();
							name = fr.getName();
						}
				}

				//crop
				byte[] data = mImageNV21;
				YuvImage yuv = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
				ExtByteArrayOutputStream ops = new ExtByteArrayOutputStream();
				//FIXMI:存在native内存泄露。参见https://blog.csdn.net/q979713444/article/details/80446404
				yuv.compressToJpeg(mAFT_FSDKFace.getRect(), 80, ops);
				final Bitmap bmp = BitmapFactory.decodeByteArray(ops.getByteArray(), 0, ops.getByteArray().length);
				try {
					ops.close();
				} catch (IOException e) {
					e.printStackTrace();
				}


				if (max > 0.6f) {
					//fr success.
					final float max_score = max;
					Log.d(TAG, "fit Score:" + max + ", NAME:" + name);
					final String mNameShow = name;
					mHandler.removeCallbacks(hide);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mTextView.setAlpha(1.0f);
							mTextView.setText(mNameShow);
							mTextView.setTextColor(Color.RED);
							mTextView1.setVisibility(View.VISIBLE);
							mTextView1.setText("置信度：" + (float)((int)(max_score * 1000)) / 1000.0);
							mTextView1.setTextColor(Color.RED);
							mImageView.setRotation(rotate);
							if (mCameraMirror) {
								mImageView.setScaleY(-1);
							}
							mImageView.setImageAlpha(255);
							mImageView.setImageBitmap(bmp);
//							Speech.getSpeechInstance().ttsPlay("您好 "+mNameShow);
						}
					});
					if (recognizedList.size() > 4){
						recognizedList.remove(0);
					}
					recognizedList.add(new RecognizedFace(System.currentTimeMillis(),result));

				} else {
//					final String mNameShow = "未识别";
//					DetecterActivity.this.runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							mTextView.setAlpha(1.0f);
//							mTextView1.setVisibility(View.VISIBLE);
//							mTextView1.setTextColor(Color.RED);
//							mTextView.setText(mNameShow);
//							mTextView.setTextColor(Color.RED);
//							mImageView.setImageAlpha(255);
//							mImageView.setRotation(rotate);
//							if (mCameraMirror) {
//								mImageView.setScaleY(-1);
//							}
//							mImageView.setImageBitmap(bmp);
//						}
//					});
				}
				mImageNV21 = null;
			}

		}

		@Override
		public void over() {
			AFR_FSDKError error = engine.AFR_FSDK_UninitialEngine();
			Log.d(TAG, "AFR_FSDK_UninitialEngine : " + error.getCode());
		}
	}

	private TextView mTextView;
	private TextView mTextView1;
	private ImageView mImageView;
	private ImageButton mImageButton;

	private TextView mTVRegister;
	private Button mRegisterButton;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

//		mCameraID = getIntent().getIntExtra("Camera", 0) == 0 ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
		mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
//		mCameraRotate = getIntent().getIntExtra("Camera", 0) == 0 ? 90 : 270;
		mCameraRotate = 270;
//		mCameraMirror = getIntent().getIntExtra("Camera", 0) == 0 ? false : true;
		mCameraMirror = false;
		mWidth = 1280;
		mHeight = 960;
		mFormat = ImageFormat.NV21;
		mHandler = new Handler();

		setContentView(R.layout.activity_camera);
		mGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.glsurfaceView);
		mGLSurfaceView.setOnTouchListener(this);
		mSurfaceView = (CameraSurfaceView) findViewById(R.id.surfaceView);
		mSurfaceView.setOnCameraListener(this);
		mSurfaceView.setupGLSurafceView(mGLSurfaceView, true, mCameraMirror, mCameraRotate);
		mSurfaceView.debug_print_fps(true, false);

		//snap
		mTextView = (TextView) findViewById(R.id.textView);
		mTextView.setText("");
		mTextView1 = (TextView) findViewById(R.id.textView1);
		mTextView1.setText("");

		mTVRegister = (TextView) findViewById(R.id.tv_register_info);
		mRegisterButton = (Button)findViewById(R.id.btn_register);
		mImageView = (ImageView) findViewById(R.id.imageView);
		mImageButton = (ImageButton) findViewById(R.id.imageButton);
		mImageButton.setOnClickListener(this);

		AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16/*预览帧传入算法后图像缩放系数*/, 5/*单帧人脸检测最大个数*/);
		Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + err.getCode());
		err = engine.AFT_FSDK_GetVersion(version);
		Log.d(TAG, "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());


		mFRAbsLoop = new FRAbsLoop();
		mFRAbsLoop.start();



		mRegisterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!TextUtils.isEmpty(ServerManager.serverIp)) {
					Intent intent = new Intent();
					intent.setAction("android.intent.action.VIEW");
					intent.setData(Uri.parse(getString(R.string.register_addr, ServerManager.serverIp)));
					startActivity(intent);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTVRegister.setText("注册地址:"+getString(R.string.register_addr, ServerManager.serverIp));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mFRAbsLoop.shutdown();
		AFT_FSDKError err = engine.AFT_FSDK_UninitialFaceEngine();
		Log.d(TAG, "AFT_FSDK_UninitialFaceEngine =" + err.getCode());

	//		ASAE_FSDKError err1 = mAgeEngine.ASAE_FSDK_UninitAgeEngine();
	//		Log.d(TAG, "ASAE_FSDK_UninitAgeEngine =" + err1.getCode());
	//
	//		ASGE_FSDKError err2 = mGenderEngine.ASGE_FSDK_UninitGenderEngine();
	//		Log.d(TAG, "ASGE_FSDK_UninitGenderEngine =" + err2.getCode());
	}

	@Override
	public Camera setupCamera() {
		// TODO Auto-generated method stub
		mCamera = Camera.open(mCameraID);
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mWidth, mHeight);
			parameters.setPreviewFormat(mFormat);

			for( Camera.Size size : parameters.getSupportedPreviewSizes()) {
				Log.d(TAG, "SIZE:" + size.width + "x" + size.height);
			}
			for( Integer format : parameters.getSupportedPreviewFormats()) {
				Log.d(TAG, "FORMAT:" + format);
			}

			List<int[]> fps = parameters.getSupportedPreviewFpsRange();
			for(int[] count : fps) {
				Log.d(TAG, "T:");
				for (int data : count) {
					Log.d(TAG, "V=" + data);
				}
			}
			//parameters.setPreviewFpsRange(15000, 30000);
			//parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
			//parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			//parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
			//parmeters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			//parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
			//parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mCamera != null) {
			mWidth = mCamera.getParameters().getPreviewSize().width;
			mHeight = mCamera.getParameters().getPreviewSize().height;
		}
		return mCamera;
	}

	@Override
	public void setupChanged(int format, int width, int height) {

	}

	@Override
	public boolean startPreviewImmediately() {
		return true;
	}

	long mDetectTime = 0;
	@Override
	public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {
		//检测人脸
		mDetectTime = System.currentTimeMillis();
		AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
		long now  = System.currentTimeMillis();
		Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
		Log.d(TAG, "Face=" + result.size());
		for (AFT_FSDKFace face : result) {
			Log.d(TAG, "Face:" + face.toString());
		}
		if (mImageNV21 == null) {
			if (!result.isEmpty()) {
				mAFT_FSDKFace = result.get(0).clone();
				mImageNV21 = data.clone();
			} else {
				if (!isPostted) {
					mHandler.removeCallbacks(hide);
					mHandler.postDelayed(hide, 2000);
					isPostted = true;
				}
			}
		}
		//copy rects
		Rect[] rects = new Rect[result.size()];
		for (int i = 0; i < result.size(); i++) {
			rects[i] = new Rect(result.get(i).getRect());
		}
		//clear result.
		result.clear();
		//return the rects for render.
		return rects;
	}

	@Override
	public void onBeforeRender(CameraFrameData data) {

	}

	@Override
	public void onAfterRender(CameraFrameData data) {
		//画人脸框
		mGLSurfaceView.getGLES2Render().draw_rect((Rect[])data.getParams(), Color.GREEN, 2);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		CameraHelper.touchFocus(mCamera, event, v, this);
		return false;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (success) {
			Log.d(TAG, "Camera Focus SUCCESS!");
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.imageButton) {
			if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
				mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
				mCameraRotate = 270;
				mCameraMirror = true;
			} else {
				mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
				mCameraRotate = 90;
				mCameraMirror = false;
			}
			mSurfaceView.resetCamera();
			mGLSurfaceView.setRenderConfig(mCameraRotate, mCameraMirror);
			mGLSurfaceView.getGLES2Render().setViewAngle(mCameraMirror, mCameraRotate);
		}
	}

}
