package cc.ytdttj.leaguetimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // 定义MediaPlayer用于播放提示音
    private MediaPlayer mediaPlayer;

    // 定义5个路线的名称
    private String[] lanes = {"上路", "打野", "中路", "下路", "辅助"};

    // 定义5个路线的两个召唤师技能的spinner控件
    private Spinner[][] spellSpinners = new Spinner[5][2];

    // 定义5个路线的两个开始倒计时的按钮控件
    private Button[][] startButtons = new Button[5][2];

    // 定义5个路线的两个显示剩余时间的TextView控件
    private TextView[][] timeTextViews = new TextView[5][2];

    // 定义5个路线的两个确认是否带了星界洞悉天赋的CheckBox控件
    private CheckBox[][] insightCheckBoxes = new CheckBox[5][2];
    
    // 定义5个路线的两个确认是否带了CD鞋的CheckBox控件
    private CheckBox[][] bootsCheckBoxes = new CheckBox[5][2];

    // 定义一个数组，表示每个召唤师技能的原始冷却时间（单位：秒）
    private int[] spellCooldowns = {300, 240, 180, 210, 210, 360, 180, 90}; // 闪现、治疗、点燃、虚弱、净化、传送、屏障、惩戒

    // 定义一个二维数组，表示每个召唤师技能的倒计时器
    private CountDownTimer[][] countDownTimers = new CountDownTimer[5][2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化控件
        initViews();
        
        // 初始化按钮点击事件
        initButtonListeners();
        
        // 初始化MediaPlayer
        initMediaPlayer();
    }
    
    // 初始化控件的方法
    private void initViews() {
        // 通过id找到控件
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                spellSpinners[i][j] = findViewById(getResources().getIdentifier("spell" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
                startButtons[i][j] = findViewById(getResources().getIdentifier("start" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
                timeTextViews[i][j] = findViewById(getResources().getIdentifier("time" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
                insightCheckBoxes[i][j] = findViewById(getResources().getIdentifier("insight" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
                bootsCheckBoxes[i][j] = findViewById(getResources().getIdentifier("boots" + (i + 1) + "_" + (j + 1), "id", getPackageName()));
                
                // 设置Spinner的选择监听器，实现同一分路两个技能不能选择相同技能的逻辑
                setupSpinnerListener(i, j);
            }
        }
    }
    
    // 设置Spinner的选择监听器
    private void setupSpinnerListener(final int laneIndex, final int spellIndex) {
        spellSpinners[laneIndex][spellIndex].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 获取另一个技能的Spinner
                int otherSpellIndex = (spellIndex == 0) ? 1 : 0;
                Spinner otherSpinner = spellSpinners[laneIndex][otherSpellIndex];
                
                // 如果选择了非"未选择"的技能，则在另一个Spinner中禁用该选项
                if (position != 0) { // 0是"未选择"的索引
                    // 获取当前选中的技能名称
                    String selectedSpell = parent.getItemAtPosition(position).toString();
                    
                    // 获取原始的完整技能列表
                    String[] allSpells = getResources().getStringArray(R.array.spells);
                    
                    // 保存另一个Spinner当前选中的位置
                    int otherSelectedPosition = otherSpinner.getSelectedItemPosition();
                    String otherSelectedSpell = null;
                    if (otherSelectedPosition > 0) {
                        otherSelectedSpell = otherSpinner.getSelectedItem().toString();
                    }
                    
                    // 创建一个新的适配器
                    ArrayAdapter<String> newAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item);
                    newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    
                    // 添加所有技能，除了当前选中的技能
                    for (String spell : allSpells) {
                        if (!spell.equals(selectedSpell)) {
                            newAdapter.add(spell);
                        }
                    }
                    
                    // 设置新的适配器
                    otherSpinner.setAdapter(newAdapter);
                    
                    // 如果另一个Spinner之前有选中的技能，尝试恢复选择
                    if (otherSelectedSpell != null) {
                        for (int i = 0; i < newAdapter.getCount(); i++) {
                            if (newAdapter.getItem(i).equals(otherSelectedSpell)) {
                                otherSpinner.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不做任何操作
            }
        });
    }
    
    // 初始化按钮点击事件的方法
    private void initButtonListeners() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                final int laneIndex = i;
                final int spellIndex = j;
                
                startButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 获取选中的召唤师技能索引
                        int selectedSpellIndex = spellSpinners[laneIndex][spellIndex].getSelectedItemPosition();
                        
                        // 检查是否选择了召唤师技能（不是"未选择"）
                        if (selectedSpellIndex == 0) { // 0是"未选择"的索引
                            showWarningDialog();
                            return;
                        }
                        
                        // 计算CD时间（需要减1，因为添加了"未选择"选项）
                        int cdTime = calculateCooldown(selectedSpellIndex - 1, laneIndex, spellIndex);
                        
                        // 禁用按钮和CheckBox
                        startButtons[laneIndex][spellIndex].setEnabled(false);
                        insightCheckBoxes[laneIndex][spellIndex].setEnabled(false);
                        bootsCheckBoxes[laneIndex][spellIndex].setEnabled(false);
                        
                        // 重置文本颜色为黑色
                        timeTextViews[laneIndex][spellIndex].setTextColor(Color.BLACK);
                        
                        // 开始倒计时
                        startCountDown(laneIndex, spellIndex, cdTime);
                    }
                });
            }
        }
    }
    
    // 显示警告对话框的方法
    private void showWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告！")
               .setMessage("请选择一个召唤师技能！")
               .setPositiveButton("明白了", null)
               .show();
    }
    
    // 计算冷却时间的方法
    private int calculateCooldown(int spellIndex, int laneIndex, int spellPosition) {
        // 获取基础CD时间
        int baseCooldown = spellCooldowns[spellIndex];
        
        // 计算召唤师技能急速
        int haste = 0;
        
        // 检查是否勾选了星界洞悉
        if (insightCheckBoxes[laneIndex][spellPosition].isChecked()) {
            haste += 18;
        }
        
        // 检查是否勾选了CD鞋
        if (bootsCheckBoxes[laneIndex][spellPosition].isChecked()) {
            haste += 10;
        }
        
        // 计算最终CD时间（公式：基础CD×(100/(100+召唤师技能急速))）
        int finalCooldown = (int)(baseCooldown * (100.0 / (100 + haste)));
        
        return finalCooldown;
    }
    
    // 开始倒计时的方法
    private void startCountDown(final int laneIndex, final int spellIndex, int seconds) {
        // 如果已经有倒计时在运行，先取消它
        if (countDownTimers[laneIndex][spellIndex] != null) {
            countDownTimers[laneIndex][spellIndex].cancel();
        }
        
        // 创建新的倒计时器
        countDownTimers[laneIndex][spellIndex] = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 计算分钟和秒数
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                
                // 更新显示
                timeTextViews[laneIndex][spellIndex].setText(String.format("%02d:%02d", minutes, seconds));
            }
            
            @Override
            public void onFinish() {
                // 倒计时结束，显示"冷却完毕"并将文字变为红色
                timeTextViews[laneIndex][spellIndex].setText("冷却完毕");
                timeTextViews[laneIndex][spellIndex].setTextColor(Color.RED);
                
                // 重新启用按钮和CheckBox
                startButtons[laneIndex][spellIndex].setEnabled(true);
                insightCheckBoxes[laneIndex][spellIndex].setEnabled(true);
                bootsCheckBoxes[laneIndex][spellIndex].setEnabled(true);
                
                // 播放提示音
                playNotificationSound();
            }
        };
        
        // 开始倒计时
        countDownTimers[laneIndex][spellIndex].start();
    }
    
    // 初始化MediaPlayer的方法
    private void initMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.notification_sound);
    }
    
    // 播放提示音的方法
    private void playNotificationSound() {
        if (mediaPlayer != null) {
            // 如果MediaPlayer正在播放，先停止并重置
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(this, R.raw.notification_sound);
            }
            // 播放提示音
            mediaPlayer.start();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 取消所有倒计时器
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                if (countDownTimers[i][j] != null) {
                    countDownTimers[i][j].cancel();
                }
            }
        }
        
        // 释放MediaPlayer资源
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}