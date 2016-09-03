package com.eazy.brush.service.impl;

import com.eazy.brush.core.lottery.LotteryUtil;
import com.eazy.brush.core.utils.Constants;
import com.eazy.brush.core.utils.DateTimeUitl;
import com.eazy.brush.dao.entity.*;
import com.eazy.brush.dao.mapper.TaskSubMapper;
import com.eazy.brush.service.DeviceInfoService;
import com.eazy.brush.service.TaskActionService;
import com.eazy.brush.service.TaskSubService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

/**
 * 元任务相关服务
 * author : liufeng
 * create time:2016/8/28 13:02
 */
@Slf4j
@Service
public class TaskSubServiceImpl implements TaskSubService {

    private Random random = new Random();

    @Autowired
    private TaskSubMapper taskSubMapper;

    @Autowired
    private TaskActionService taskActionService;

    @Autowired
    private DeviceInfoService deviceInfoService;

    @Override
    public List<TaskSub> getUnConsumeList(long pertime, int size) {
        return taskSubMapper.getList(pertime, size);
    }

    @Override
    public void makeTaskSub(Task task) {

        List<Action> actionList = taskActionService.getActionsByTaskId(task.getId());
        List<DeviceInfo> deviceInfos = deviceInfoService.getList(0, Integer.MAX_VALUE);

        int retainDay = task.getRetainDay();//留存天数
        int upDown = random.nextInt(task.getIncrUpDown());
        upDown = random.nextInt(1) == 0 ? upDown : -upDown;
        int dayNum = task.getIncrDay() + upDown;

        //Math.pow(27,1d/3) == 27 开 3 次方
        double percent = Math.pow(task.getRetainPercent() * 1.0 / 100, 1d / retainDay);

        DateTime createDateTime = new DateTime(task.getCreateTime());
        int interDay = DateTimeUitl.getDayInter(createDateTime, DateTime.now());
        int interHour = task.getRunEndTime() - createDateTime.getHourOfDay();

        //第一天需要跑的任务占比
        double dayOnePercent = task.getRunSpeed() == 0 ?
                1.0 : interHour / (task.getRunEndTime() - task.getRunStartTime());

        int dayTaskNum = calcDayTaskNum(percent, interDay, dayOnePercent, retainDay, dayNum);
        int perNum = 0, times = 0;

        if (0 == task.getRunSpeed()) {//立即投放
            times = dayTaskNum / Constants.TASK_BATCH_UP; //需要分多少批次执行
            perNum = times > 0 ? Constants.TASK_BATCH_UP : dayTaskNum;
        } else {    //函数投放
            times = (task.getRunEndTime() - task.getRunStartTime()) * 60 / Constants.TASK_SUB_PER_MINITE;
            perNum = dayTaskNum / times;
        }

        DateTime startTime = DateTimeUitl.getStartTime(task.getRunStartTime(), interDay);

        while (times-- >= 0) {
            long perTime = Long.parseLong(startTime.toString("yyyyMMddHHmm"));
            buildTaskSubs(task, perTime, actionList, deviceInfos, perNum);
            startTime = startTime.plusMinutes(Constants.TASK_SUB_PER_MINITE);
        }
        log.info("### taskId:{},interDay:{},taskNum:{} make finished! ###", task.getId(), interDay, dayTaskNum);
    }

    @Override
    public void insertTaskSub(TaskSub taskSub) {
        taskSubMapper.insertTaskSub(taskSub);
    }

    @Override
    public void insertTaskBatch(List<TaskSub> taskSubList) {
        if (!CollectionUtils.isEmpty(taskSubList)) {
            taskSubMapper.insertTaskSubBatch(taskSubList);
        }
    }

    @Override
    public void changeTaskSubState(String ids, long callbackTime) {
        taskSubMapper.changeTaskSubState(ids, callbackTime);
    }

    /**
     * 生成任务元
     *
     * @param task
     * @param actionList
     * @param deviceInfos
     * @param taskNum
     */
    private void buildTaskSubs(Task task, long perTime, List<Action> actionList,
                               List<DeviceInfo> deviceInfos, int taskNum) {

        List<TaskSub> taskSubs = Lists.newArrayList();
        for (int num = 0; num < taskNum; num++) {
            TaskSub taskSub = new TaskSub();
            taskSub.setTaskId(task.getId());
            taskSub.setPerTime(perTime);
            taskSub.setActionId(actionList.get(random.nextInt(actionList.size())).getId());
            taskSub.setDeviceInfoId(LotteryUtil.lottery(deviceInfos).getId());
            taskSub.setRunTime(task.getRunTime());
            taskSubs.add(taskSub);
        }
        insertTaskBatch(taskSubs);
    }

    /**
     * 计算
     *
     * @param percent       每日留存百分百比
     * @param interDay      距离现在的天数
     * @param dayOnePercent 第一天需要新增的任务数占比
     * @param retainDay     留存天数
     * @param taskNum
     * @return
     */
    private int calcDayTaskNum(double percent, int interDay, double dayOnePercent, int retainDay, int taskNum) {
        int dayTaskNum = 0;
        for (int i = 0; i < interDay && i < retainDay; i++) {
            dayTaskNum += taskNum * Math.pow(percent, i);
        }
        dayTaskNum += taskNum * dayOnePercent * Math.pow(percent, interDay);
        return dayTaskNum;
    }
}
