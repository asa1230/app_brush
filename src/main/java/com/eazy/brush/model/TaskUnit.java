package com.eazy.brush.model;

import lombok.Data;

/**
 * 元任务
 * author : liufeng
 * create time:2016/8/28 10:44
 */
@Data
public class TaskUnit {
    private int id;
    private int taskId;              //任务id
    private int actionId;            //动作组id
    private int deviceInfoId;        //设备信息
    private int netInfoId;           //网络信息
    private int runTime;             //任务执行时间
    private int callbackTime;        //任务执行完回调时间
}
