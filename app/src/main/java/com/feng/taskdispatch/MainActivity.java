package com.feng.taskdispatch;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    private static final int METHOD_PF = 0;     // 优先权优先
    private static final int METHOD_TC = 1;     // 时间片轮转

    private EditText mProcessNameEt;
    private EditText mServiceTimeEt;
    private EditText mCommitTimeEt;
    private EditText mPriorityEt;
    private Button mCommitBtn;
    private Button mClearBtn;
    private Spinner mMethodSp;
    private Button mRunBtn;
    private RecyclerView mProcessListRv;
    private RecyclerView mDispatchResultRv;
    private TextView mAverTurnTimeTv;         // 平均周转时间
    private TextView mWeightAverTurnTimeTv;   // 带权平均周转时间

    private int mMethod = 0;    // 执行的算法
    private ProcessListAdapter mProcessListAdapter;
    private List<ProcessData> mProcessDataList = new ArrayList<>();
    private DispatchResultAdapter mDispatchResultAdapter;
    private List<ResultData> mResultDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);   //隐藏标题栏
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mProcessNameEt = findViewById(R.id.et_main_task_name);
        mServiceTimeEt = findViewById(R.id.et_main_service_time);
        mCommitTimeEt = findViewById(R.id.et_main_commit_time);
        mPriorityEt = findViewById(R.id.et_main_priority);

        mCommitBtn = findViewById(R.id.btn_main_commit_task);
        mCommitBtn.setOnClickListener(this);
        mClearBtn = findViewById(R.id.btn_main_clear_task);
        mClearBtn.setOnClickListener(this);
        mRunBtn = findViewById(R.id.btn_main_run);
        mRunBtn.setOnClickListener(this);

        mMethodSp = findViewById(R.id.sp_main_method_list);
        mMethodSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // 优先权优先
                        mMethod = METHOD_PF;
                        break;
                    case 1:
                        // 时间片轮转
                        mMethod = METHOD_TC;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mProcessListRv = findViewById(R.id.rv_main_task_list);
        mProcessListRv.setLayoutManager(new LinearLayoutManager(this));
        mDispatchResultRv = findViewById(R.id.rv_main_dispatch_result);
        mDispatchResultRv.setLayoutManager(new LinearLayoutManager(this));

        mAverTurnTimeTv = findViewById(R.id.tv_main_dispatch_result_average_turn_time);
        mWeightAverTurnTimeTv = findViewById(R.id.tv_main_dispatch_result_weight_average_turn_time);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_commit_task:
                // 提交作业
                if (!checkCommit()) {
                    break;
                }
                ProcessData processData = new ProcessData(mProcessNameEt.getText().toString().trim(),
                        Integer.parseInt(mCommitTimeEt.getText().toString()),
                        Integer.parseInt(mServiceTimeEt.getText().toString()),
                        Integer.parseInt(mPriorityEt.getText().toString()));
                mProcessDataList.add(processData);
                // 按照到达时间排序
                Collections.sort(mProcessDataList, new Comparator<ProcessData>() {
                    @Override
                    public int compare(ProcessData o1, ProcessData o2) {
                        return o1.getCommitTime() - o2.getCommitTime();
                    }
                });
                // 显示信息
                if (mProcessListAdapter == null) {
                    mProcessListAdapter = new ProcessListAdapter(MainActivity.this, mProcessDataList);
                    mProcessListRv.setAdapter(mProcessListAdapter);
                } else {
                    mProcessListAdapter.notifyDataSetChanged();
                }
                // 清空输入
                mServiceTimeEt.setText("");
                mProcessNameEt.setText("");
                mCommitTimeEt.setText("");
                mPriorityEt.setText("");
                break;
            case R.id.btn_main_clear_task:
                // 清空提交的进程
                mProcessDataList.clear();
                if (mProcessListAdapter != null) {
                    mProcessListAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.btn_main_run:
                if (!checkRun()) {
                    break;
                }
                // 隐藏软键盘
                hideSoftKeyboard(this);
                // 先重置结果
                mResultDataList.clear();
                // 根据不同的算法对进程进行调度
                switch (mMethod) {
                    case METHOD_PF:
                        // 执行优先权优先调度算法
                        doPF();
                        break;
                    case METHOD_TC:
                        // 执行时间轮转法调度
                        doTC();
                        break;
                    default:
                        break;
                }
                // 显示结果
                // 1. 计算平均周转时间和带权平均周转时间
                float turnTimeSum = 0f;
                float weightTurnTimeSum = 0f;
                for (int i = 0; i < mResultDataList.size(); i++) {
                    turnTimeSum += mResultDataList.get(i).getTurnTime();
                    weightTurnTimeSum += mResultDataList.get(i).getWeightTurnTime();
                }
                float averTurnTime = turnTimeSum / mResultDataList.size();
                float weightAverTurnTime = weightTurnTimeSum / mResultDataList.size();
                mAverTurnTimeTv.setText(String.format("%.2f",averTurnTime));
                mWeightAverTurnTimeTv.setText(String.format("%.2f",weightAverTurnTime));
                // 2. 更新列表
                if (mDispatchResultAdapter == null) {
                    mDispatchResultAdapter = new DispatchResultAdapter(MainActivity.this, mResultDataList);
                    mDispatchResultRv.setAdapter(mDispatchResultAdapter);
                } else {
                    mDispatchResultAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    private boolean checkCommit() {
        if (mProcessNameEt.getText().toString().trim().equals("") ||
                mServiceTimeEt.getText().toString().equals("") ||
                mCommitTimeEt.getText().toString().equals("") ||
                mPriorityEt.getText().toString().equals("")) {
            showShortToast("请先填写完整进程名称、到达时间、服务时间和优先权");
            return false;
        }
        if (Integer.parseInt(mServiceTimeEt.getText().toString()) == 0) {
            showShortToast("服务时间不能为 0");
            return false;
        }

        return true;
    }

    private boolean checkRun() {
        if (mProcessDataList.isEmpty()) {
            showShortToast("请先添加进程");
            return false;
        }

        return true;
    }

    private void showShortToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 隐藏软键盘
     */
    private static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 执行优先权优先调度（非抢占式，数字越小，优先权越高）
     */
    private void doPF() {
        int currTime = mProcessDataList.get(0).getCommitTime();
        boolean[] hasCompleted = new boolean[mProcessDataList.size()];  // 当前作业是否执行完毕
        int finishNum = 0;  // 完成任务的作业数
        while (finishNum < mProcessDataList.size()) {
            int index = -1; // 当前需要执行的作业
            int maxPriority = Integer.MAX_VALUE;
            boolean hasFound = false;
            for (int i = 0; i < mProcessDataList.size(); i++) {
                if (hasCompleted[i] || mProcessDataList.get(i).getCommitTime() > currTime) {
                    continue;
                }
                hasFound = true;
                if (mProcessDataList.get(i).getPriority() < maxPriority) {
                    maxPriority = mProcessDataList.get(i).getPriority();
                    index = i;
                }
            }
            if (!hasFound) {
                // 更新 currTime
                int temp = Integer.MAX_VALUE;
                for (int i = 0; i < mProcessDataList.size(); i++) {
                    int currCommitTime = mProcessDataList.get(i).getCommitTime();
                    if (currCommitTime > currTime) {
                        temp = Math.min(temp, currCommitTime);
                    }
                }
                currTime = temp;
                continue;
            }
            // 执行当前作业
            hasCompleted[index] = true;
            finishNum++;
            int completeTime = currTime + mProcessDataList.get(index).getServiceTime();
            float turnTime = completeTime - mProcessDataList.get(index).getCommitTime();
            float weightTurnTime = turnTime / mProcessDataList.get(index).getServiceTime();
            mResultDataList.add(new ResultData(mProcessDataList.get(index).getTaskName(),
                    currTime, completeTime,turnTime, weightTurnTime));
            currTime = completeTime;
        }
    }

    /**
     * 执行时间片轮转法（时间片为 1）
     */
    private void doTC() {
        int currTime = mProcessDataList.get(0).getCommitTime();
        int[] serviceTimes = new int[mProcessDataList.size()];  // 记录作业剩余的服务时间
        int[] startTimes = new int[mProcessDataList.size()];    // 记录作业开始运行的时间
        for (int i = 0; i < mProcessDataList.size(); i++) {
            serviceTimes[i] = mProcessDataList.get(i).getServiceTime();
            startTimes[i] = -1;
        }
        int finishNum = 0;  // 完成任务的作业数
        LinkedList<Integer> queue = new LinkedList<>(); // 就绪队列
        while (finishNum < mProcessDataList.size()) {
            // 判断是否有需要加入队列的作业
            for (int i = 0; i < mProcessDataList.size(); i++) {
                if (mProcessDataList.get(i).getCommitTime() == currTime) {
                    queue.add(i);
                }
            }
            if (queue.isEmpty()) {
                // 更新 currTime
                int temp = Integer.MAX_VALUE;
                for (int i = 0; i < mProcessDataList.size(); i++) {
                    int currCommitTime = mProcessDataList.get(i).getCommitTime();
                    if (currCommitTime > currTime) {
                        temp = Math.min(temp, currCommitTime);
                    }
                }
                currTime = temp;
                continue;
            }
            int currIndex = queue.remove(); // 当前需要执行的作业
            // 执行当前作业
            if (startTimes[currIndex] == -1) {
                startTimes[currIndex] = currTime;
            }
            serviceTimes[currIndex]--;
            if (serviceTimes[currIndex] == 0) { // 当前作业执行完毕
                int completeTime = currTime + 1;
                float turnTime = completeTime - mProcessDataList.get(currIndex).getCommitTime();
                float weightTurnTime = turnTime / mProcessDataList.get(currIndex).getServiceTime();
                mResultDataList.add(new ResultData(mProcessDataList.get(currIndex).getTaskName(),
                        startTimes[currIndex], completeTime,turnTime, weightTurnTime));
                finishNum++;
            } else {
                queue.add(currIndex);   // 还没有执行完毕，添加到就绪队列末端
            }
            currTime++;
        }
    }

}
