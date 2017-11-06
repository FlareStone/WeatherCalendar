package flare.weathercalendar.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import flare.weathercalendar.R;
import flare.weathercalendar.fragment.CalendarFragment;
import flare.weathercalendar.fragment.PlanFragment;
import flare.weathercalendar.fragment.WeatherFragment;
import flare.weathercalendar.adapter.FragAdapter;

public class MainActivity extends FragmentActivity {
    private WeatherFragment weatherFragment;
    private PlanFragment planFragment;
    private CalendarFragment calendarFragment;
    private Button[] buttons;
    private final int REQUEST_READ_PHONE_STATE = 1;
    private final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    private ViewPager viewPager;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //状态栏字体颜色为黑字
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        setContentView(R.layout.activity_main);

        applyPhonePermission();

        Log.i("MainActivity", "Create");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.i("MainActivity", "Destroy");
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.i("MainActivity", "Pause");
    }


    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Log.i("MainActivity", "Restart");
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.i("MainActivity", "Resume");
    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Log.i("MainActivity", "Start");
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.i("MainActivity", "Stop");
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            for (Button button : buttons) {
                button.setEnabled(true);
            }
            buttons[position].setEnabled(false);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private void setViewPager() {
        //构造适配器
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(weatherFragment);
        fragments.add(planFragment);
        fragments.add(calendarFragment);
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);

        //设定适配器
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        buttons = new Button[3];
        buttons[0] = (Button) findViewById(R.id.tab_weather);
        buttons[1] = (Button) findViewById(R.id.tab_go_out_plan);
        buttons[2] = (Button) findViewById(R.id.tab_calendar);
        buttons[0].setEnabled(false);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setTag(i);
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem((Integer) v.getTag());
                }
            });
        }

        viewPager.setOffscreenPageLimit(2);

        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());
    }


    private void applyPhonePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE
            }, REQUEST_READ_PHONE_STATE);
        } else {
            applyCoarsePermission();
        }
    }

    private void applyCoarsePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            showUI();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    this.finish();
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    applyCoarsePermission();
                }
                break;
            case REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    this.finish();
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showUI();
                }
                break;
            default:
                break;
        }
    }

    private void showUI() {

        weatherFragment = new WeatherFragment();
        planFragment = new PlanFragment();
        calendarFragment = new CalendarFragment();

        setViewPager();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            viewPager.setCurrentItem(1);    //出行界面
        }
    }

}
