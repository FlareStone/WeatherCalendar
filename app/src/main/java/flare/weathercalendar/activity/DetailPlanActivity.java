package flare.weathercalendar.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import flare.weathercalendar.PlanSQLiteHelper;
import flare.weathercalendar.R;
import flare.weathercalendar.entity.Plan;

/**
 * Created by 54333 on 2017/7/26.
 */

public class DetailPlanActivity extends AppCompatActivity {
    private Plan plan;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(null);
        toolbar.setTitle("出行计划详情");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.BLACK);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(onMenuItemClick);    //必须在上一行之后

        plan = PlanSQLiteHelper.queryById(this, getIntent().getIntExtra("id", 0));

        refresh();

        Button deleteButton = (Button)findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DetailPlanActivity.this);
                dialog.setTitle("确认删除？");
                dialog.setMessage("确认删除该项？");
                dialog.setNegativeButton("取消", null);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlanSQLiteHelper.delete(DetailPlanActivity.this, plan.getId());
                        finish();
                    }
                });
                dialog.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_plan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1) {
            plan = PlanSQLiteHelper.queryById(this, data.getIntExtra("id", 0));
            TextView dateView = (TextView)findViewById(R.id.date);
            dateView.setText("日期：" + plan.getYear() + "年" + plan.getMonth() + "月"
                    + plan.getDay() +  "日");

            TextView timeView = (TextView)findViewById(R.id.time);
            timeView.setText("时间：" + AddPlanActivity.getTime(plan.getHour(), plan.getMinute()));

            TextView titleView = (TextView)findViewById(R.id.title);
            titleView.setText("标题：" + plan.getTitle());

            TextView detailView = (TextView)findViewById(R.id.detail);
            detailView.setText(plan.getDetail());

            Switch remindSwitch = (Switch)findViewById(R.id.remind);
            if (plan.isNeedRemind()) {
                remindSwitch.setChecked(true);
            } else {
                remindSwitch.setChecked(false);
            }
            refresh();
        }
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.modify:
                    Intent intent = new Intent(DetailPlanActivity.this, ModifyPlanActivity.class);
                    intent.putExtra("id", plan.getId());
                    startActivityForResult(intent, 1);
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void refresh() {
        TextView dateView = (TextView)findViewById(R.id.date);
        dateView.setText("日期：" + plan.getYear() + "年" + plan.getMonth() + "月"
                + plan.getDay() +  "日");

        TextView timeView = (TextView)findViewById(R.id.time);
        timeView.setText("时间：" + AddPlanActivity.getTime(plan.getHour(), plan.getMinute()));

        TextView titleView = (TextView)findViewById(R.id.title);
        titleView.setText("标题：" + plan.getTitle());

        TextView detailView = (TextView)findViewById(R.id.detail);
        detailView.setText(plan.getDetail());

        Switch remindSwitch = (Switch)findViewById(R.id.remind);
        if (plan.isNeedRemind()) {
            remindSwitch.setChecked(true);
        } else {
            remindSwitch.setChecked(false);
        }
    }
}
