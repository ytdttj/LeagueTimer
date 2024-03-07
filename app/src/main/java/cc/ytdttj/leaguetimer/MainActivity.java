package cc.ytdttj.leaguetimer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{

    // 定义5个路线的名称
    private String[] lanes = {"上路", "打野", "中路", "下路", "辅助"};

    // 定义5个路线的spinner控件
    private Spinner[] laneSpinners = new Spinner[5];

    // 定义5个路线的两个召唤师技能的spinner控件
    private Spinner[][] spellSpinners = new Spinner[5][2];

    // 定义5个路线的两个开始倒计时的按钮控件
    private Button[][] startButtons = new Button[5][2];

    // 定义5个路线的两个显示剩余时间的TextView控件
    private TextView[][] timeTextViews = new TextView[5][2];

    // 定义5个路线的两个确认是否带了星界洞悉天赋的Switch控件
    private Switch[][] insightSwitches = new Switch[5][2];

    // 定义一个数组适配器，用来绑定召唤师技能的数据
    private ArrayAdapter<String> spellAdapter;

    // 定义一个常量，表示星界洞悉天赋的冷却时间减少百分比
    private static final double INSIGHT_REDUCTION = 0.15;

    // 定义一个二维数组，表示每个召唤师技能的原始冷却时间（单位：秒）
    private int[][] spellCooldowns = {
            {300, 240, 180, 210, 210, 360, 180, 90}, // 闪现、治疗、点燃、虚弱、净化、传送、屏障、惩戒
            {300, 240, 180, 210, 210, 360, 180, 90}, // 闪现、治疗、点燃、虚弱、净化、传送、屏障、惩戒
            {300, 240, 180, 210, 210, 360, 180, 90}, // 闪现、治疗、点燃、虚弱、净化、传送、屏障、惩戒
            {300, 240, 180, 210, 210, 360, 180, 90}, // 闪现、治疗、点燃、虚弱、净化、传送、屏障、惩戒
            {300, 240, 180, 210, 210, 360, 180, 90}  // 闪现、治疗、点燃、虚弱、净化、传送、屏障、惩戒
    };

    // 定义一个二维数组，表示每个召唤师技能的剩余冷却时间（单位：秒）
    private int[][] spellRemains = new int[5][2];

    // 定义一个二维数组，表示每个召唤师技能的倒计时是否开始
    private boolean[][] spellStarted = new boolean[5][2];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化控件
        initViews();

        // 初始化数据
        initData();

        // 初始化监听器
        initListeners();
    }
    // 初始化控件的方法
    private void initViews()
    {
        // 通过id找到控件
        for (int i = 0; i < 5; i++)
        {
            laneSpinners[i] = findViewById(getResources().getIdentifier("lane" + (i + 1), "id", getPackageName()));
            for (int j = 0; j < 2; j++)
            {
                spellSpinners[i][j] = findViewById(getResources().getIdentifier("spell" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
                startButtons[i][j] = findViewById(getResources().getIdentifier("start" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
                timeTextViews[i][j] = findViewById(getResources().getIdentifier("time" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
                insightSwitches[i][j] = findViewById(getResources().getIdentifier("insight" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
            }
        }
    }

    // 初始化数据的方法
    private void initData()
    {
        // 创建一个数组适配器，用来绑定召唤师技能的数据
        spellAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.spells));
        // 设置下拉菜单的样式
        spellAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 给每个召唤师技能的spinner设置适配器
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                spellSpinners[i][j].setAdapter(spellAdapter);
            }
        }
    }

    // 初始化监听器的方法
    private void initListeners()
    {
        // 给每个路线的spinner设置选择监听器
        for (int i = 0; i < 5; i++)
        {
            final int laneIndex = i;
            laneSpinners[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    // 当用户选择了一个路线时，显示一个提示信息
                    Toast.makeText(MainActivity.this, "你选择了" + lanes[laneIndex], Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {

                }
            });
        }

        // 给每个召唤师技能的spinner设置选择监听器
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                final int laneIndex = i;
                final int spellIndex = j;
                spellSpinners[i][j].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        // 当用户选择了一个召唤师技能时，更新该技能的原始冷却时间
                        spellCooldowns[laneIndex][spellIndex] = 300 - position * 30;
                        // 如果该技能的倒计时没有开始，就更新该技能的剩余冷却时间
                        if (!spellStarted[laneIndex][spellIndex])
                        {
                            spellRemains[laneIndex][spellIndex] = spellCooldowns[laneIndex][spellIndex];
                            // 如果该技能带了星界洞悉天赋，就根据冷却时间减少百分比更新剩余冷却时间
                            if (insightSwitches[laneIndex][spellIndex].isChecked())
                            {
                                spellRemains[laneIndex][spellIndex] = (int) (spellRemains[laneIndex][spellIndex] * (1 - INSIGHT_REDUCTION));
                            }
                            // 将剩余冷却时间格式化为分:秒的形式，并显示在TextView上
                            timeTextViews[laneIndex][spellIndex].setText(formatTime(spellRemains[laneIndex][spellIndex]));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {

                    }
                });
            }
        }

        // 给每个开始倒计时的按钮设置点击监听器
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                final int laneIndex = i;
                final int spellIndex = j;
                startButtons[i][j].setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // 当用户点击了开始倒计时的按钮时，判断该技能的倒计时是否已经开始
                        if (spellStarted[laneIndex][spellIndex])
                        {
                            // 如果已经开始，就停止倒计时，并将按钮文本改为“开始”
                            stopCountDown(laneIndex, spellIndex);
                            startButtons[laneIndex][spellIndex].setText("开始");
                        }
                        else
                        {
                            // 如果没有开始，就开始倒计时，并将按钮文本改为“停止”
                            startCountDown(laneIndex, spellIndex);
                            startButtons[laneIndex][spellIndex].setText("停止");
                        }
                    }
                });
            }
        }

        // 给每个确认是否带了星界洞悉天赋的Switch控件设置切换监听器
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                final int laneIndex = i;
                final int spellIndex = j;
                insightSwitches[i][j].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // 当用户切换了星界洞悉天赋的状态时，判断该技能的倒计时是否已经开始
                        if (spellStarted[laneIndex][spellIndex]) {
                            // 如果已经开始，就停止倒计时，并重新计算剩余冷却时间
                            stopCountDown(laneIndex, spellIndex);
                            spellRemains[laneIndex][spellIndex] = spellCooldowns[laneIndex][spellIndex];
                            // 如果该技能带了星界洞悉天赋，就根据冷却时间减少百分比更新剩余冷却时间
                            if (isChecked) {
                                spellRemains[laneIndex][spellIndex] = (int) (spellRemains[laneIndex][spellIndex] * (1 - INSIGHT_REDUCTION));
                            }
                            // 将剩余冷却时间格式化为分:秒的形式，并显示在TextView上
                            timeTextViews[laneIndex][spellIndex].setText(formatTime(spellRemains[laneIndex][spellIndex]));
                            // 重新开始倒计时
                            startCountDown(laneIndex, spellIndex);
                        } else {
                            // 如果没有开始，就只更新原始冷却时间
                            spellCooldowns[laneIndex][spellIndex] = 300 - spellSpinners[laneIndex][spellIndex].getSelectedItemPosition() * 30;
                            // 如果该技能带了星界洞悉天赋，就根据冷却时间减少百分比更新剩余冷却时间
                            if (isChecked) {
                                spellRemains[laneIndex][spellIndex] = (int) (spellCooldowns[laneIndex][spellIndex] * (1 - INSIGHT_REDUCTION));
                            } else {
                                spellRemains[laneIndex][spellIndex] = spellCooldowns[laneIndex][spellIndex];
                            }
                            // 将剩余冷却时间格式化为分:秒的形式，并显示在TextView上
                            timeTextViews[laneIndex][spellIndex].setText(formatTime(spellRemains[laneIndex][spellIndex]));
                        }
                    }
                });
            }
        }

    }

    // 开始倒计时的方法
    private void startCountDown(final int laneIndex, final int spellIndex)
    {
        // 创建一个线程，用来执行倒计时的逻辑
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // 设置该技能的倒计时状态为已开始
                spellStarted[laneIndex][spellIndex] = true;
                // 循环执行倒计时的逻辑，直到剩余冷却时间为0或者倒计时被停止
                while (spellRemains[laneIndex][spellIndex] > 0 && spellStarted[laneIndex][spellIndex])
                {
                    // 每隔一秒，将剩余冷却时间减一
                    spellRemains[laneIndex][spellIndex]--;
                    // 在主线程中更新UI，将剩余冷却时间格式化为分:秒的形式，并显示在TextView上
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            timeTextViews[laneIndex][spellIndex].setText(formatTime(spellRemains[laneIndex][spellIndex]));
                        }
                    });
                    // 休眠一秒
                    try
                    {
                        Thread.sleep(1000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                // 如果倒计时结束，就在主线程中更新UI，将按钮文本改为“开始”，并显示一个提示信息
                if (spellRemains[laneIndex][spellIndex] == 0)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            startButtons[laneIndex][spellIndex].setText("开始");
                            Toast.makeText(MainActivity.this, lanes[laneIndex] + "的召唤师技能" + spellSpinners[laneIndex][spellIndex].getSelectedItem() + "已经冷却完毕", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                // 设置该技能的倒计时状态为未开始
                spellStarted[laneIndex][spellIndex] = false;
            }
        });
        // 启动线程
        thread.start();
    }

    // 停止倒计时的方法
    private void stopCountDown(int laneIndex, int spellIndex)
    {
        // 设置该技能的倒计时状态为未开始
        spellStarted[laneIndex][spellIndex] = false;
    }

    // 格式化时间的方法，将秒数转换为分:秒的形式
    private String formatTime(int seconds)
    {
        // 计算分钟数
        int minutes = seconds / 60;
        // 计算秒数
        int secs = seconds % 60;
        // 返回格式化后的字符串，如果秒数小于10，就在前面补一个0
        return minutes + ":" + (secs < 10 ? "0" + secs : secs);
    }
}