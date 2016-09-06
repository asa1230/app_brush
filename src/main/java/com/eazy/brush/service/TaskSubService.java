package com.eazy.brush.service;

import com.eazy.brush.dao.entity.Task;
import com.eazy.brush.dao.entity.TaskSub;

import java.util.List;

/**
 * 元任务相关服务
 * author : liufeng
 * create time:2016/8/28 12:57
 */
public interface TaskSubService {


    /**
     * 获取未消费的任务集合
     *
     * @param pertime
     * @param size
     * @return
     */
    List<TaskSub> getUnConsumeList(long pertime, int size);

    /**
     * 生成每日新增TaskSub
     *
     * @param task
     */
    void makeIncrDayTaskSub(Task task);

    /**
     * 生成每日留存TaskSub
     *
     * @param task
     */
    void makeRetainDayTaskSub(Task task);

    void insertTaskSub(TaskSub taskSub);

    void insertTaskBatch(List<TaskSub> taskSubList);

    /**
     * 修改子任务运行状态
     *
     * @param ids
     * @param callbackTime
     */
    void changeTaskSubState(String ids, long callbackTime);

    /**
     * 随机获取 size 条 taskSub
     * @param createDay
     * @param offset
     * @param size
     * @return
     */
    List<TaskSub> getRandList(int createDay, int offset, int size);
}
