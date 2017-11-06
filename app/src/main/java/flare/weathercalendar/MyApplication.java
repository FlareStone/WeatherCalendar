package flare.weathercalendar;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import flare.weathercalendar.adapter.DayPlansAdapter;
import flare.weathercalendar.adapter.PlansAdapter;
import flare.weathercalendar.entity.Plan;
import flare.weathercalendar.fragment.PlanFragment;

/**
 * Created by 54333 on 2017/7/19.
 */

public class MyApplication extends Application {
    private String nowSearch;   //避免显示冲突
    private SwipeRefreshLayout swipeRefreshLayout;
    private PlansAdapter plansAdapter;      //用于在日历fragment中删除时更新出行fragment
    //以下四项用于在出行fragment中删除时更新日历fragment
    private DayPlansAdapter dayPlansAdapter;
    private int calendarYear;
    private int calendarMonth;
    private int calendarDay;

    public String getNowSearch() {
        return nowSearch;
    }

    public void setNowSearch(String nowSearch) {
        this.nowSearch = nowSearch;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    public PlansAdapter getPlansAdapter() {
        return plansAdapter;
    }

    public void setPlansAdapter(PlansAdapter plansAdapter) {
        this.plansAdapter = plansAdapter;
    }

    public DayPlansAdapter getDayPlansAdapter() {
        return dayPlansAdapter;
    }

    public void setDayPlansAdapter(DayPlansAdapter dayPlansAdapter) {
        this.dayPlansAdapter = dayPlansAdapter;
    }

    public int getCalendarYear() {
        return calendarYear;
    }

    public void setCalendarYear(int calendarYear) {
        this.calendarYear = calendarYear;
    }

    public int getCalendarMonth() {
        return calendarMonth;
    }

    public void setCalendarMonth(int calendarMonth) {
        this.calendarMonth = calendarMonth;
    }

    public int getCalendarDay() {
        return calendarDay;
    }

    public void setCalendarDay(int calendarDay) {
        this.calendarDay = calendarDay;
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.isAvailable());
    }

    public static void toastForNoNetwork(Context context) {
        Toast.makeText(context, "没有网络连接，请检查设置", Toast.LENGTH_SHORT).show();
    }
}
