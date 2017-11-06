package flare.weathercalendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import flare.weathercalendar.entity.Plan;

import static flare.weathercalendar.activity.AddPlanActivity.getTimestamp;

/**
 * Created by 54333 on 2017/7/26.
 */

public class PlanSQLiteHelper extends SQLiteOpenHelper {
    public PlanSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table plan(id integer primary key autoincrement, title text,detail text, year int, " +
                "month int, day int, hour int, minute int, needRemind boolean)";
        //execSQL函数用于执行SQL语句
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static long insert(Plan plan, Context context) {
        PlanSQLiteHelper dbHelper = new PlanSQLiteHelper(context, "plan", null, 1);
        //得到一个可写的数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //生成ContentValues对象 //key:列名，value:想插入的值
        ContentValues cv = new ContentValues();
        //往ContentValues对象存放数据，键-值对模式
        cv.put("title", plan.getTitle());
        cv.put("detail", plan.getDetail());
        cv.put("year", plan.getYear());
        cv.put("month", plan.getMonth());
        cv.put("day", plan.getDay());
        cv.put("hour", plan.getHour());
        cv.put("minute", plan.getMinute());
        cv.put("needRemind", plan.isNeedRemind());
        //调用insert方法，将数据插入数据库
        long id = db.insert("plan", null, cv);
        //关闭数据库
        db.close();
        return id;
    }

    public static List<Plan> queryAll(Context context) {  //返回未过期的plan
        List<Plan> planList = new ArrayList<>();
        PlanSQLiteHelper dbHelper = new PlanSQLiteHelper(context, "plan", null, 1);
        //得到一个可写的数据库
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("plan", new String[]{"id", "title", "detail", "year", "month", "day",
                "hour", "minute", "needRemind"}, null, null, null, null, "year ASC,month ASC,day ASC,hour ASC,minute ASC");
        while (cursor.moveToNext()) {
            Plan plan = new Plan();
            plan.setId(cursor.getInt(cursor.getColumnIndex("id")));
            plan.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            plan.setDetail(cursor.getString(cursor.getColumnIndex("detail")));
            plan.setYear(cursor.getInt(cursor.getColumnIndex("year")));
            plan.setMonth(cursor.getInt(cursor.getColumnIndex("month")));
            plan.setDay(cursor.getInt(cursor.getColumnIndex("day")));
            plan.setHour(cursor.getInt(cursor.getColumnIndex("hour")));
            plan.setMinute(cursor.getInt(cursor.getColumnIndex("minute")));
            if (cursor.getInt(cursor.getColumnIndex("needRemind")) == 1) {
                plan.setNeedRemind(true);
            } else {
                plan.setNeedRemind(false);
            }
            if (getTimestamp(plan.getYear() + "年" + plan.getMonth() + "月" +
                    plan.getDay() + "日" + plan.getHour() + "时" + plan.getMinute() + "分")
                    > System.currentTimeMillis()) {
                planList.add(plan);
            }
        }
        //关闭数据库
        db.close();
        return planList;
    }

    public static Plan queryById(Context context, int id) {
        Plan plan = new Plan();
        PlanSQLiteHelper dbHelper = new PlanSQLiteHelper(context, "plan", null, 1);
        //得到一个可写的数据库
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("plan", new String[]{"id", "title", "detail", "year", "month", "day",
                "hour", "minute", "needRemind"}, "id=?", new String[]{Integer.toString(id)}, null, null, null);
        while (cursor.moveToNext()) {
            plan.setId(cursor.getInt(cursor.getColumnIndex("id")));
            plan.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            plan.setDetail(cursor.getString(cursor.getColumnIndex("detail")));
            plan.setYear(cursor.getInt(cursor.getColumnIndex("year")));
            plan.setMonth(cursor.getInt(cursor.getColumnIndex("month")));
            plan.setDay(cursor.getInt(cursor.getColumnIndex("day")));
            plan.setHour(cursor.getInt(cursor.getColumnIndex("hour")));
            plan.setMinute(cursor.getInt(cursor.getColumnIndex("minute")));
            if (cursor.getInt(cursor.getColumnIndex("needRemind")) == 1) {
                plan.setNeedRemind(true);
            } else {
                plan.setNeedRemind(false);
            }
        }
        //关闭数据库
        db.close();
        return plan;
    }

    public static List<Plan> queryByDate(Context context, int year, int month, int day) {
        List<Plan> planList = new ArrayList<>();
        PlanSQLiteHelper dbHelper = new PlanSQLiteHelper(context, "plan", null, 1);
        //得到一个可写的数据库
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("plan", new String[]{"id", "title", "detail", "year", "month", "day",
                "hour", "minute", "needRemind"}, "year=? and month=? and day=?",
                new String[]{Integer.toString(year), Integer.toString(month),
                        Integer.toString(day)}, null, null, "hour ASC,minute ASC");
        while (cursor.moveToNext()) {
            Plan plan = new Plan();
            plan.setId(cursor.getInt(cursor.getColumnIndex("id")));
            plan.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            plan.setDetail(cursor.getString(cursor.getColumnIndex("detail")));
            plan.setYear(cursor.getInt(cursor.getColumnIndex("year")));
            plan.setMonth(cursor.getInt(cursor.getColumnIndex("month")));
            plan.setDay(cursor.getInt(cursor.getColumnIndex("day")));
            plan.setHour(cursor.getInt(cursor.getColumnIndex("hour")));
            plan.setMinute(cursor.getInt(cursor.getColumnIndex("minute")));
            if (cursor.getInt(cursor.getColumnIndex("needRemind")) == 1) {
                plan.setNeedRemind(true);
            } else {
                plan.setNeedRemind(false);
            }
            planList.add(plan);
        }
        //关闭数据库
        db.close();
        return planList;
    }

    public static void delete(Context context, int id) {
        PlanSQLiteHelper dbHelper = new PlanSQLiteHelper(context, "plan", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClauses = "id=?";
        String [] whereArgs = {String.valueOf(id)};
        //调用delete方法，删除数据
        db.delete("plan", whereClauses, whereArgs);
        db.close();
    }

    public static void deleteAll(Context context) {
        PlanSQLiteHelper dbHelper = new PlanSQLiteHelper(context, "plan", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //调用delete方法，删除数据
        db.delete("plan", null, null);
        db.close();
    }

    public static void deleteOld(Context context) {
        PlanSQLiteHelper dbHelper = new PlanSQLiteHelper(context, "plan", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //调用delete方法，删除数据
        Calendar calendar = Calendar.getInstance();
        String year = Integer.toString(calendar.get(Calendar.YEAR));
        String month = Integer.toString(calendar.get(Calendar.MONTH) + 1);
        String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = Integer.toString(calendar.get(Calendar.MINUTE));
        db.delete("plan", "year<? or (year=? and month<?) or (year=? and month=? and day<?) or " +
                "(year=? and month=? and day=? and hour<?) or " +
                "(year=? and month=? and day=? and hour=? and minute<?)" ,
                new String[]{year,year,month,year,month,day,year,month,day,hour,year,month,day,
                        hour,minute});
        db.close();
    }

    public static void update(Context context, Plan plan) {
        PlanSQLiteHelper dbHelper = new PlanSQLiteHelper(context, "plan", null, 1);
        //得到一个可写的数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("year", plan.getYear());
        cv.put("month", plan.getMonth());
        cv.put("day", plan.getDay());
        cv.put("hour", plan.getHour());
        cv.put("minute", plan.getMinute());
        cv.put("title", plan.getTitle());
        cv.put("detail", plan.getDetail());
        cv.put("needRemind", plan.isNeedRemind());
        //where 子句 "?"是占位符号，对应后面的"1",
        String whereClause = "id=?";
        String[] whereArgs = {String.valueOf(plan.getId())};
        //参数1 是要更新的表名
        //参数2 是一个ContentValeus对象
        //参数3 是where子句
        db.update("plan", cv, whereClause, whereArgs);
        db.close();
    }
}
