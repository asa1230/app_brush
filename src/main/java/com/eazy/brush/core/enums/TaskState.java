package com.eazy.brush.core.enums;

/**
 * author : liufeng
 * create time:2016/9/4 10:52
 */
public enum TaskState {
    confirm_ing("审核中", -1), confirm_passed("审核通过", 0), confirm_failed("审核失败", 1),
    running("运行中", 2), stoped("已停止", 3);

    private String name;
    private int code;

    TaskState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static TaskState valueOf(int code) {
        for (TaskState t : TaskState.values()) {
            if (t.getCode() == code) {
                return t;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
