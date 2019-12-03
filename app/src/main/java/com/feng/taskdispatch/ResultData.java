package com.feng.taskdispatch;

/**
 * @author Feng Zhaohao
 * Created on 2019/12/2
 */
public class ResultData {
    private String taskName;
    private int startTime;
    private int completeTime;
    private float turnTime;
    private float weightTurnTime;

    public ResultData(String taskName, int startTime,
                      int completeTime, float turnTime, float weightTurnTime) {
        this.taskName = taskName;
        this.startTime = startTime;
        this.completeTime = completeTime;
        this.turnTime = turnTime;
        this.weightTurnTime = weightTurnTime;
    }

    public float getTurnTime() {
        return turnTime;
    }

    public void setTurnTime(float turnTime) {
        this.turnTime = turnTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(int completeTime) {
        this.completeTime = completeTime;
    }


    public float getWeightTurnTime() {
        return weightTurnTime;
    }

    public void setWeightTurnTime(float weightTurnTime) {
        this.weightTurnTime = weightTurnTime;
    }
}
