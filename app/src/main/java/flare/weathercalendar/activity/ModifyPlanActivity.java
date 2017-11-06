package flare.weathercalendar.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import flare.weathercalendar.PlanSQLiteHelper;
import flare.weathercalendar.R;
import flare.weathercalendar.entity.Plan;

/**
 * Created by 54333 on 2017/7/27.
 */

public class ModifyPlanActivity extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    Plan plan;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private String title;
    private String detail;
    private boolean needRemind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(flare.weathercalendar.R.layout.activity_new_plan);

        Toolbar toolbar = (Toolbar) findViewById(flare.weathercalendar.R.id.toolbar);
        toolbar.setLogo(null);
        toolbar.setTitle("修改出行计划");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.BLACK);
        setSupportActionBar(toolbar);

        plan = PlanSQLiteHelper.queryById(this, getIntent().getIntExtra("id", 0));

        year = plan.getYear();
        month = plan.getMonth();
        day = plan.getDay();
        hour = plan.getHour();
        minute = plan.getMinute();
        title = plan.getTitle();
        detail = plan.getDetail();
        needRemind = plan.isNeedRemind();

        TextView dateView = (TextView)findViewById(flare.weathercalendar.R.id.set_date);
        dateView.setText("修改日期：" + plan.getYear() + "年" + plan.getMonth() + "月"
                + plan.getDay() +  "日");

        TextView timeView = (TextView)findViewById(flare.weathercalendar.R.id.set_time);
        timeView.setText("修改时间：" + AddPlanActivity.getTime(plan.getHour(), plan.getMinute()));

        EditText titleView = (EditText) findViewById(flare.weathercalendar.R.id.set_title);
        titleView.setText(title);

        EditText detailView = (EditText) findViewById(flare.weathercalendar.R.id.set_detail);
        detailView.setText(detail);

        Switch remindSwitch = (Switch)findViewById(flare.weathercalendar.R.id.remind);
        if (needRemind) {
            remindSwitch.setChecked(true);
        } else {
            remindSwitch.setChecked(false);
        }

        TextView dateButton = (TextView)findViewById(R.id.set_date);
        dateButton.setOnClickListener(this);


        TextView timeButton = (TextView)findViewById(R.id.set_time);
        timeButton.setOnClickListener(this);

        toolbar.setOnMenuItemClickListener(onMenuItemClick);    //必须在上一行之后
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(flare.weathercalendar.R.menu.menu_add_plan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case flare.weathercalendar.R.id.set_date:
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, this,
                        year, month - 1, day);
                datePickerDialog.show();
                break;
            case flare.weathercalendar.R.id.set_time:
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, this, hour, minute, true);
                timePickerDialog.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        TextView textView = (TextView)findViewById(flare.weathercalendar.R.id.set_date);
        this.year = year;
        this.month = month + 1;
        this.day = dayOfMonth;
        textView.setText("设置时间：" + year + "年" + (month + 1) + "月" + dayOfMonth +  "日");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        TextView textView = (TextView)findViewById(flare.weathercalendar.R.id.set_time);
        textView.setText("设置时间：" + AddPlanActivity.getTime(hourOfDay, minute));
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case flare.weathercalendar.R.id.save:
                    modifyTravelPlan(plan);
                    AddPlanActivity.addAlarm(ModifyPlanActivity.this, plan);
                    Intent intent = getIntent();
                    intent.putExtra("id", plan.getId());
                    setResult(1, intent);
                    finish();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void modifyTravelPlan(Plan plan) {
        plan.setYear(year);
        plan.setMonth(month);
        plan.setDay(day);
        plan.setHour(hour);
        plan.setMinute(minute);
        EditText titleText = (EditText)findViewById(flare.weathercalendar.R.id.set_title);
        plan.setTitle(titleText.getText().toString());
        if (plan.getTitle().equals("")) {
            plan.setTitle("未命名");
        }
        EditText detailText = (EditText)findViewById(flare.weathercalendar.R.id.set_detail);
        plan.setDetail(detailText.getText().toString());
        Switch remindSwitch = (Switch)findViewById(flare.weathercalendar.R.id.remind);
        plan.setNeedRemind(remindSwitch.isChecked());
        PlanSQLiteHelper.update(this, plan);
    }
}
