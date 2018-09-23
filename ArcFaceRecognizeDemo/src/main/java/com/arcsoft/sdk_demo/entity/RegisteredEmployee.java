package com.arcsoft.sdk_demo.entity;

import com.arcsoft.facerecognition.AFR_FSDKFace;

/**
 * 注册员工(由数据库中员工+AFR_FSDKFace封装,便于特征比对)
 * 因数据库中存取的是特征字节数组，而比对时需传AFR_FSDKFace字段，故而封装一层
 */
public class RegisteredEmployee extends Employee {
    AFR_FSDKFace face;

    public AFR_FSDKFace getFace() {
        return face;
    }

    public void setFace(AFR_FSDKFace face) {
        this.face = face;
    }

    public RegisteredEmployee(){}

    public RegisteredEmployee(Employee employee){
        this.setId(employee.getId());
        this.setUUId(employee.getUUId());
        this.setName(employee.getName());
        this.setJobNumber(employee.getJobNumber());
        this.setRegisterPicName(employee.getRegisterPicName());

        if (employee.getRegisterFeater()!=null){
            AFR_FSDKFace temp = new AFR_FSDKFace();
            temp.setFeatureData(employee.getRegisterFeater());
            this.setFace(temp);
        }

    }
}
