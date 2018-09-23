package com.arcsoft.sdk_demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import com.arcsoft.sdk_demo.database.DaoMaster;
import com.arcsoft.sdk_demo.database.DaoSession;
import com.arcsoft.sdk_demo.server.ServerManager;

/**
 * Created by gqj3375 on 2017/4/28.
 */

public class Application extends android.app.Application {
	private static final String TAG = "FaceDemo";

	static final int MIN_REGISTE_IMG_WIDTH = 500;
	static final int MIN_REGISTE_IMG_HEIGHT = 800;

	static Application sInstance;
	static final String DB_NAME = "greendao_app.db";
	DaoSession mDaoSession = null;
	ServerManager mServerManager;
	FaceDB mFaceDB;
	Uri mImage;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;

		mDaoSession = new DaoMaster(new DaoMaster.DevOpenHelper(this,DB_NAME).getWritableDb()).newSession();
		mFaceDB = new FaceDB(this.getExternalCacheDir().getPath());
		mImage = null;
		mServerManager = new ServerManager();
		mServerManager.register();
		mServerManager.startService();
	}



	public void setCaptureImage(Uri uri) {
		mImage = uri;
	}

	public Uri getCaptureImage() {
		return mImage;
	}

	/**
	 * 解码图片(加入图片最小宽高设置,小于最小宽高时进行放大,以提高寸照注册成功率)
	 * @param path
	 * @return
	 */
	public static Bitmap decodeImage(String path) {
		Bitmap res;
		try {
			ExifInterface exif = new ExifInterface(path);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			BitmapFactory.Options op = new BitmapFactory.Options();
			op.inSampleSize = 1;
			op.inJustDecodeBounds = false;
			//op.inMutable = true;
			res = BitmapFactory.decodeFile(path, op);
			//rotate and scale.
			Matrix matrix = new Matrix();

			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				matrix.postRotate(90);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				matrix.postRotate(180);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				matrix.postRotate(270);
			}

			//对小尺寸图进行放大
			if (res.getWidth() < MIN_REGISTE_IMG_WIDTH || res.getHeight()< MIN_REGISTE_IMG_HEIGHT){
				float scaleWidth = ((float) MIN_REGISTE_IMG_WIDTH) / res.getWidth();
				float scaleHeight = ((float) MIN_REGISTE_IMG_HEIGHT) / res.getHeight();
				float scale = Math.max(scaleWidth,scaleHeight);
				Log.d(TAG,"图片"+path.substring(path.lastIndexOf('/')+1)+" 缩放系数:"+scale);
				matrix.postScale(scale,scale);
			}
			Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
			Log.d("com.arcsoft", "check target Image:" + temp.getWidth() + "X" + temp.getHeight());

			if (!temp.equals(res)) {
				res.recycle();
			}
			return temp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public DaoSession getDaoSession() {
		return mDaoSession;
	}

	public FaceDB getFaceDB() {
		return mFaceDB;
	}

	public static Application getInstance(){
		return sInstance;
	}
}
