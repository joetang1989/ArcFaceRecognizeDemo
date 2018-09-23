package com.arcsoft.sdk_demo.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class Employee {
    /**
     * 客户端数据库生成id
     */
    @Id(autoincrement = true)
    Long id;

    /**
     * 后台传过来的ID
     */
    @Property(nameInDb = "UUId")
    @Unique
    String UUId;
    /**
     * 员工姓名
     */
    @Property(nameInDb = "name")
    @NotNull
    String name;
    /**
     * 员工工号
     */
    @Property(nameInDb = "jobNumber")
    String jobNumber;
    /**
     * 员工注册头像图片名称
     */
    @Property(nameInDb = "registerPicName")
    String registerPicName;

    /**
     * 员工注册特征值
     */
    @Property(nameInDb = "registerFeater")
    @NotNull
    byte[] registerFeater;



    @Generated(hash = 1561922569)
    public Employee(Long id, String UUId, @NotNull String name, String jobNumber,
            String registerPicName, @NotNull byte[] registerFeater) {
        this.id = id;
        this.UUId = UUId;
        this.name = name;
        this.jobNumber = jobNumber;
        this.registerPicName = registerPicName;
        this.registerFeater = registerFeater;
    }
    @Generated(hash = 202356944)
    public Employee() {
    }



    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUUId() {
        return this.UUId;
    }
    public void setUUId(String UUId) {
        this.UUId = UUId;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getJobNumber() {
        return this.jobNumber;
    }
    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }
    public String getRegisterPicName() {
        return this.registerPicName;
    }
    public void setRegisterPicName(String registerPicName) {
        this.registerPicName = registerPicName;
    }
    public byte[] getRegisterFeater() {
        return this.registerFeater;
    }
    public void setRegisterFeater(byte[] registerFeater) {
        this.registerFeater = registerFeater;
    }
}
