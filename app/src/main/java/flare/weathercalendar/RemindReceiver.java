package flare.weathercalendar;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import flare.weathercalendar.activity.DetailPlanActivity;
import flare.weathercalendar.entity.Plan;
import flare.weathercalendar.activity.AddPlanActivity;

/**
 * Created by 54333 on 2017/7/27.
 */

public class RemindReceiver extends BroadcastReceiver
{
    Context context;
    Plan plan;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // TODO Auto-generated method stub
        plan = PlanSQLiteHelper.queryById(context, intent.getIntExtra("id", 0));
        context.startService(intent);
        this.context = context;


        Log.d("Receiver", "title:" + plan.getTitle());
        Log.d("Receiver", "id:" + plan.getId());

        if (plan.isNeedRemind() && plan != null && plan.isNeedRemind()) {
            showRemindNotification();
        }
    }

    private void showRemindNotification() {
        Intent intent = new Intent(context, DetailPlanActivity.class);
        intent.putExtra("id", plan.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, plan.getId(), intent
                , 0);
        //获取NotificationManager实例
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                //设置小图标
                .setSmallIcon(R.drawable.big0)
                //设置通知标题
                .setContentTitle(plan.getTitle())
                //呼吸灯
                .setLights(0x9932CC, 3000, 3000)
                //震动铃声
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);
        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        //.setWhen(System.currentTimeMillis());
        //通过builder.build()方法生成Notification对象,并发送通知,id=1
        notificationManager.notify(plan.getId(), builder.build());
    }

}
