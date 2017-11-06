package flare.weathercalendar.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import flare.weathercalendar.MyApplication;
import flare.weathercalendar.PlanSQLiteHelper;
import flare.weathercalendar.R;
import flare.weathercalendar.entity.Plan;

/**
 * Created by 54333 on 2017/7/26.
 */

public class AddPlanActivity extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private Plan plan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_plan);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(null);
        toolbar.setTitle("新增出行计划");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.BLACK);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(onMenuItemClick);    //必须在上一行之后

        Calendar calendar = Calendar.getInstance();
        year = getIntent().getIntExtra("year", calendar.get(Calendar.YEAR));
        month = getIntent().getIntExtra("month", calendar.get(Calendar.MONTH) + 1);
        day = getIntent().getIntExtra("day", calendar.get(Calendar.DAY_OF_MONTH));
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE) + 1;
        if (year != calendar.get(Calendar.YEAR) || month != calendar.get(Calendar.MONTH) + 1
                || day != calendar.get(Calendar.DAY_OF_MONTH)) {
            hour = 0;
            minute = 0;
        }
        TextView dateView = (TextView) findViewById(R.id.set_date);
        dateView.setText("设置日期：" + year + "年" + month + "月"
                + day + "日");

        TextView textView = (TextView) findViewById(R.id.set_time);
        textView.setText("设置时间：" + getTime(hour, minute));

        TextView dateButton = (TextView) findViewById(R.id.set_date);
        dateButton.setOnClickListener(this);


        TextView timeButton = (TextView) findViewById(R.id.set_time);
        timeButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_plan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_date:
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, this,
                        year, month - 1, day);
                datePickerDialog.show();
                break;
            case R.id.set_time:
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, this, hour, minute, true);
                timePickerDialog.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        TextView textView = (TextView) findViewById(R.id.set_date);
        this.year = year;
        this.month = month + 1;
        this.day = dayOfMonth;
        textView.setText("设置时间：" + year + "年" + (month + 1) + "月" + dayOfMonth + "日");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        TextView textView = (TextView) findViewById(R.id.set_time);
        textView.setText("设置时间：" + getTime(hourOfDay, minute));
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.save:
                    plan = new Plan();
                    plan.setId(addTravelPlan());
                    addAlarm(AddPlanActivity.this, plan);
//                    MyApplication myApplication = (MyApplication) getApplication();
//                    myApplication.getPlanFragment().getPlansAdapter().
//                            refresh(PlanSQLiteHelper.queryAll(AddPlanActivity.this));
                    Intent intent = getIntent();
                    setResult(2, intent);
                    finish();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private int addTravelPlan() {
        plan.setYear(year);
        plan.setMonth(month);
        plan.setDay(day);
        plan.setHour(hour);
        plan.setMinute(minute);
        EditText titleText = (EditText) findViewById(R.id.set_title);
        plan.setTitle(titleText.getText().toString());
        if (plan.getTitle().equals("")) {
            plan.setTitle("未命名");
        }
        EditText detailText = (EditText) findViewById(R.id.set_detail);
        plan.setDetail(detailText.getText().toString());
        Switch remindSwitch = (Switch) findViewById(R.id.remind);
        plan.setNeedRemind(remindSwitch.isChecked());

        return (int) PlanSQLiteHelper.insert(plan, this);
    }

    public static void addAlarm(Activity activity, Plan plan) {
        if (!plan.isNeedRemind()) {
            return;
        }
        Intent intent = new Intent("REMIND");
        intent.putExtra("title", plan.getTitle());
        intent.putExtra("detail", plan.getDetail());
        intent.putExtra("id", plan.getId());
        intent.putExtra("year", plan.getYear());
        intent.putExtra("month", plan.getMonth());
        intent.putExtra("day", plan.getDay());
        intent.putExtra("hour", plan.getHour());
        intent.putExtra("minute", plan.getMinute());
        intent.putExtra("needRemind", plan.isNeedRemind());



        Log.d("Broadcast", "title" + plan.getTitle());
        PendingIntent pi = PendingIntent.getBroadcast(activity, plan.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) activity.getSystemService(ALARM_SERVICE);

        long setTime = getTimestamp(plan.getYear() + "年" + plan.getMonth() + "月" +
                plan.getDay() + "日" + plan.getHour() + "时" + plan.getMinute() + "分");

        if (setTime < System.currentTimeMillis()) {
            return;
        }

        am.set(AlarmManager.RTC_WAKEUP, setTime, pi);
        Log.d("Time", new Date(setTime).toString());
    }

    public static long getTimestamp(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy年MM月dd日HH时mm分",
                Locale.CHINA);
        Date date;
        String times = null;
        try {
            date = sdr.parse(time);
            long l = date.getTime();
            String stf = String.valueOf(l);
            times = stf.substring(0, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Long.parseLong(times) * 1000;
    }


    public static String getTime(int hour, int minute) {
        String sHour = Integer.toString(hour);
        String sMinute = Integer.toString(minute);
        if (sHour.length() == 1) {
            sHour = "0" + sHour;
        }
        if (sMinute.length() == 1) {
            sMinute = "0" + sMinute;
        }
        return sHour + ":" + sMinute;
    }
}
