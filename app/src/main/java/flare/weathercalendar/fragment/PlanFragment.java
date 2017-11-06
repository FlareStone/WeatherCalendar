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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flare.weathercalendar.MyApplication;
import flare.weathercalendar.PlanSQLiteHelper;
import flare.weathercalendar.R;
import flare.weathercalendar.activity.AddPlanActivity;
import flare.weathercalendar.activity.DetailPlanActivity;
import flare.weathercalendar.adapter.CitiesAdapter;
import flare.weathercalendar.adapter.DayPlansAdapter;
import flare.weathercalendar.adapter.PlansAdapter;
import flare.weathercalendar.entity.Plan;


/**
 * Created by 54333 on 2017/7/26.
 */

public class PlanFragment extends Fragment implements View.OnClickListener{
    private View view;
    private ListView listView;
    private PlansAdapter plansAdapter;
    int childPosition;
    MyApplication myApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_plan, container, false);
        FloatingActionButton addPlanButton = (FloatingActionButton)view.findViewById(R.id.add_plan);
        addPlanButton.setOnClickListener(this);


        List<Plan> listItems = PlanSQLiteHelper.queryAll(getActivity());

        for (Plan plan : listItems) {
            AddPlanActivity.addAlarm(getActivity(), plan);
        }


        plansAdapter = new PlansAdapter(getContext(), listItems);
        myApplication = (MyApplication)getActivity().getApplication();
        myApplication.setPlansAdapter(plansAdapter);

        listView = (ListView) view.findViewById(R.id.plan_list);
        listView.setAdapter(plansAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), DetailPlanActivity.class);
                intent.putExtra("id", plansAdapter.getItem(position).getId());
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
                menu.add(0,0,0,"删除");
                menu.add(0,1,0, "删除所有");
                menu.add(0,2,0, "删除所有过期项");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        plansAdapter.refresh(PlanSQLiteHelper.queryAll(getActivity()));
        plansAdapter.notifyDataSetChanged();
        Log.d("PlanFragment", "onResume");
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 2) { //从详情返回
//            plansAdapter.refresh(PlanSQLiteHelper.queryAll(getActivity()));
//            plansAdapter.notifyDataSetChanged();
//        }
//    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //info.id得到listview中选择的条目绑定的id
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        switch (item.getItemId()) {
            case 0:
                int id = plansAdapter.getItem(childPosition).getId();
                dialog.setTitle("确认删除？");
                dialog.setMessage("确认删除该项？");
                dialog.setNegativeButton("取消", null);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlanSQLiteHelper.delete(getActivity(), plansAdapter.getItem(childPosition).getId());
                        plansAdapter.refresh(PlanSQLiteHelper.queryAll(getActivity()));
                        plansAdapter.notifyDataSetChanged();
                        synchronizeCalendarFragment();
                    }
                });
                dialog.show();
                return true;
            case 1:
                dialog.setTitle("确认删除所有？");
                dialog.setMessage("确认删除所有项？");
                dialog.setNegativeButton("取消", null);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlanSQLiteHelper.deleteAll(getActivity());
                        plansAdapter.deleteAllItem();
                        plansAdapter.notifyDataSetChanged();
                        synchronizeCalendarFragment();
                    }
                });
                dialog.show();
                return true;
            case 2:
                dialog.setTitle("确认删除所有过期项？");
                dialog.setMessage("确认删除所有过期项？");
                dialog.setNegativeButton("取消", null);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlanSQLiteHelper.deleteOld(getActivity());
                        synchronizeCalendarFragment();
                    }
                });
                dialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void synchronizeCalendarFragment() {
        int calendarYear = myApplication.getCalendarYear();
        int calendarMonth = myApplication.getCalendarMonth();
        int calendarDay = myApplication.getCalendarDay();
        myApplication.getDayPlansAdapter().refresh(PlanSQLiteHelper.
                queryByDate(getActivity(), calendarYear, calendarMonth,
                        calendarDay));
        myApplication.getDayPlansAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_plan:
                Intent intent = new Intent(getActivity(), AddPlanActivity.class);
                startActivity(intent);
                break;
        }
    }
}
