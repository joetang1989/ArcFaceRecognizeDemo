package com.arcsoft.sdk_demo;

import android.util.Log;

import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.sdk_demo.entity.Employee;
import com.arcsoft.sdk_demo.entity.RegisteredEmployee;
import com.guo.android_extend.java.ExtInputStream;
import com.guo.android_extend.java.ExtOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2017/7/11.
 */

public class FaceDB {
	private final String TAG = this.getClass().toString();

	public static String appid = "xxxxx";
	public static String ft_key = "xxxxx";
	public static String fd_key = "xxxxx";
	public static String fr_key = "xxxxx";

	List<RegisteredEmployee> mRegister;
	AFR_FSDKEngine mFREngine;
	AFR_FSDKVersion mFRVersion;
	boolean mUpgrade;


	public FaceDB(String path) {
		mRegister = new ArrayList<>();
		mUpgrade = false;
		mFREngine = new AFR_FSDKEngine();
		mFRVersion = new AFR_FSDKVersion();
		AFR_FSDKError error = mFREngine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
		if (error.getCode() != AFR_FSDKError.MOK) {
			Log.e(TAG, "AFR_FSDK_InitialEngine fail! error code :" + error.getCode());
		} else {
			mFREngine.AFR_FSDK_GetVersion(mFRVersion);
			Log.d(TAG, "AFR_FSDK_GetVersion=" + mFRVersion.toString());
		}
	}

	public void destroy() {
		if (mFREngine != null) {
			mFREngine.AFR_FSDK_UninitialEngine();
		}
	}

	private boolean saveInfo() {
		return true;
	}

	private boolean loadInfo() {
		if (!mRegister.isEmpty()) {
			return false;
		}

		return true;
	}

	public boolean loadFaces(){
		if (loadInfo()) {
			try {
				List<Employee> register = Application.getInstance().getDaoSession().loadAll(Employee.class);
				for (Employee employee:register){
					RegisteredEmployee registeredEmployee = new RegisteredEmployee(employee);
					mRegister.add(registeredEmployee);
				}

				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public	void addFace(String name, AFR_FSDKFace face) {
		try {
			//check if already registered.
			boolean add = true;
			for (RegisteredEmployee frface : mRegister) {
				if (frface.getName().equals(name)) {
					add = false;
					break;
				}
			}
			if (add) { // not registered.
				RegisteredEmployee frface = new RegisteredEmployee();
				frface.setName(name);
				frface.setFace(face);
				frface.setRegisterFeater(face.getFeatureData());
				mRegister.add(frface);

				//插入数据库
				Employee employee = new Employee();
				employee.setName(name);
				employee.setRegisterFeater(face.getFeatureData());
				Application.getInstance().getDaoSession().insert(employee);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean delete(String name) {
		try {
			//check if already registered.
			boolean find = false;
			for (RegisteredEmployee frface : mRegister) {
				if (frface.getName().equals(name)) {
					mRegister.remove(frface);
					find = true;
					break;
				}
			}
			//TODO:数据库移除数据

			return find;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean upgrade() {
		return false;
	}
}
