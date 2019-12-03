package com.feng.taskdispatch;

/**
 * @author Feng Zhaohao
 * Created on 2019/12/2
 */
public class ProcessData {
    private String taskName;
    private int commitTime;
    private int serviceTime;
    private int priority;

    public ProcessData(String taskName, int commitTime, int serviceTime, int priority) {
        this.taskName = taskName;
        this.commitTime = commitTime;
        this.serviceTime = serviceTime;
        this.priority = priority;
    }

    public int getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(int commitTime) {
        this.commitTime = commitTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
