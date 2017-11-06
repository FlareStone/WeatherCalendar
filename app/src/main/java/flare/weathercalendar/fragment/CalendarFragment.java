package flare.weathercalendar.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nanchen.calendarview.ClickDataListener;
import com.nanchen.calendarview.MyCalendarView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import flare.weathercalendar.MyApplication;
import flare.weathercalendar.PlanSQLiteHelper;
import flare.weathercalendar.R;
import flare.weathercalendar.activity.AddPlanActivity;
import flare.weathercalendar.activity.DetailPlanActivity;
import flare.weathercalendar.adapter.DayPlansAdapter;
import flare.weathercalendar.entity.Plan;

/**
 * Created by 54333 on 2017/7/27.
 */

public class CalendarFragment extends Fragment implements View.OnClickListener{
    private View view;
    private int year;
    private int month;
    private int day;
    private DayPlansAdapter dayPlansAdapter;
    private ListView listView;
    private int childPosition;
    private MyApplication myApplication;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calendar, container, false);

        FloatingActionButton addPlanButton = (FloatingActionButton)view.findViewById(R.id.add_plan);
        addPlanButton.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        myApplication = (MyApplication)getActivity().getApplication();
        myApplication.setCalendarYear(year);
        myApplication.setCalendarMonth(month);
        myApplication.setCalendarDay(day);

        List<Plan> listItems = PlanSQLiteHelper.queryByDate(getActivity(), year, month, day);
        dayPlansAdapter = new DayPlansAdapter(getContext(), listItems);
        myApplication.setDayPlansAdapter(dayPlansAdapter);

        listView = (ListView) view.findViewById(R.id.day_plan_list);
        listView.setAdapter(dayPlansAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), DetailPlanActivity.class);
                intent.putExtra("id", dayPlansAdapter.getItem(position).getId());
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //为menu准备参数
                childPosition = position;
                return false;
            }
        });

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0,3,0,"删除");
            }
        });

        MyCalendarView calendarView = (MyCalendarView) view.findViewById(R.id.calendarView);
        calendarView.setClickDataListener(new ClickDataListener() {
            @Override
            public void clickData(int year, int month, int day) {
                CalendarFragment.this.year = year;
                CalendarFragment.this.month = month;
                CalendarFragment.this.day = day;
                dayPlansAdapter.refresh(PlanSQLiteHelper.queryByDate(getActivity(), year, month, day));
                dayPlansAdapter.notifyDataSetChanged();
                myApplication.setCalendarYear(year);
                myApplication.setCalendarMonth(month);
                myApplication.setCalendarDay(day);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        dayPlansAdapter.refresh(PlanSQLiteHelper.queryByDate(getActivity(), year, month, day));
        dayPlansAdapter.notifyDataSetChanged();
        Log.d("PlanFragment", "onResume");
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //info.id得到listview中选择的条目绑定的id
        switch (item.getItemId()) {
            case 3:
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("确认删除？");
                dialog.setMessage("确认删除该项？");
                dialog.setNegativeButton("取消", null);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlanSQLiteHelper.delete(getActivity(), dayPlansAdapter.getItem(childPosition).getId());
                        dayPlansAdapter.refresh(PlanSQLiteHelper.queryByDate(getActivity(), year, month, day));
                        dayPlansAdapter.notifyDataSetChanged();
                        myApplication.getPlansAdapter().refresh(PlanSQLiteHelper.queryAll(getActivity()));
                        myApplication.getPlansAdapter().notifyDataSetChanged();
                    }
                });
                dialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_plan:
                Intent intent = new Intent(getActivity(), AddPlanActivity.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                startActivity(intent);
                break;
        }
    }
}
