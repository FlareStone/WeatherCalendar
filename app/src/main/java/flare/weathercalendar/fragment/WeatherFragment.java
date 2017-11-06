package flare.weathercalendar.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import flare.weathercalendar.MyApplication;
import flare.weathercalendar.R;
import flare.weathercalendar.activity.AddPlanActivity;
import flare.weathercalendar.asyncTask.WeatherAsyncTask;
import flare.weathercalendar.activity.SearchCityActivity;
import flare.weathercalendar.entity.City;
import flare.weathercalendar.entity.Weather;

/**
 * Created by 54333 on 2017/7/19.
 */

public class WeatherFragment extends Fragment implements View.OnClickListener{
    private SearchView searchView;
    private View view;
    private Context context;
    private Weather weather;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("WeatherFragment", "CreateView");
        view = inflater.inflate(R.layout.fragment_weather, container, false);
        context = view.getContext();

        searchView = (SearchView) view.findViewById(R.id.search_view);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("北京");
        final SearchView searchView = (SearchView) view.findViewById(R.id.search_view);
        setSearchViewOnClickListener(searchView, this); //无法监听搜索按钮。
        view.findViewById(R.id.city_name).setOnClickListener(this);


        //下拉刷新
        final SwipeRefreshLayout swipeRefreshLayout =
                (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SharedPreferences sharedPreferences = context.getSharedPreferences("city",
                        Context.MODE_PRIVATE);
                String cityId = sharedPreferences.getString("cityId", "GPS");
                if (!MyApplication.isNetworkAvailable(context)) {
                    MyApplication.toastForNoNetwork(context);
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    downloadWeather(cityId);
                    MyApplication application = (MyApplication) getActivity().getApplication(); //全局变量用于结束刷新
                    application.setSwipeRefreshLayout(swipeRefreshLayout);
                }
            }
        });

        ImageView shareView = (ImageView)view.findViewById(R.id.share);
        shareView.setOnClickListener(this);

        ImageView addPlanView = (ImageView)view.findViewById(R.id.add_plan);
        addPlanView.setOnClickListener(this);

        setWeatherFromCache();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share:
                shareWeather();
                break;
            case R.id.city_name:
                searchCity();
                break;
            case R.id.add_plan:
                Intent intent = new Intent(getActivity(), AddPlanActivity.class);
                startActivityForResult(intent, 2);  //当天的计划
                break;
            default:
                searchCity();   //其它的只有搜索按钮绑定了。
                break;
        }
    }




    public static void setSearchViewOnClickListener(View v, View.OnClickListener listener) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = group.getChildAt(i);
                if (child instanceof LinearLayout || child instanceof RelativeLayout) {
                    setSearchViewOnClickListener(child, listener);
                }

                if (child instanceof TextView) {
                    TextView text = (TextView) child;
                    text.setFocusable(false);
                }
                child.setOnClickListener(listener);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {
            if (!MyApplication.isNetworkAvailable(context)) {
                MyApplication.toastForNoNetwork(context);
                return;
            }
            Bundle bundle = data.getExtras();
            downloadWeather(bundle.getString("cityId"));
        }
    }

    private void setWeatherFromCache() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("city",
                Context.MODE_PRIVATE);
        String cityId = sharedPreferences.getString("cityId", "");
        if (cityId.equals("")) {    //没有缓存，自动定位
            downloadWeather("GPS");
        } else {
            weather = loadWeather();
            putWeatherOnUI(view, weather);
            if (!MyApplication.isNetworkAvailable(context)) {
                MyApplication.toastForNoNetwork(context);
            } else {
                downloadWeather(cityId);
            }
        }
    }

    public static void putWeatherOnUI(View view, Weather weather) {
        TextView textView = (TextView) view.findViewById(R.id.city_name);
        textView.setText(weather.getCity().getName());
        ImageView imageView = (ImageView) view.findViewById(R.id.weather_code);
        imageView.setImageResource(view.getResources().getIdentifier("big" + weather.getCode(),
                "drawable", view.getContext().getPackageName()));
        textView = (TextView) view.findViewById(R.id.weather_text);
        textView.setText(weather.getText());
        textView = (TextView) view.findViewById(R.id.date);
        textView.setText(weather.getDate());
        textView = (TextView) view.findViewById(R.id.temperature);
        textView.setText(weather.getTemperature() + "℃");
        textView = (TextView) view.findViewById(R.id.low_and_high_temperature);
        textView.setText(weather.getLowTemperature() + " ~ " + weather.getHighTemperature() + "℃");
        textView = (TextView) view.findViewById(R.id.wind_direction);
        textView.setText("风向：" + weather.getWindDirection());
        textView = (TextView) view.findViewById(R.id.wind_scale);
        textView.setText("风力等级：" + weather.getWindScale());
        textView = (TextView) view.findViewById(R.id.uv);
        textView.setText("紫外线强度：" + weather.getUv());
        textView = (TextView) view.findViewById(R.id.life1);
        textView.setText("舒适度：" + weather.getComfort() + "\n穿衣：" + weather.getDressing() +
                "\n洗车：" + weather.getCarWashing() + "\n交通：" + weather.getTraffic());
        textView = (TextView) view.findViewById(R.id.life2);
        textView.setText("晨练：" + weather.getMorningSport() + "\n运动：" + weather.getSport() +
                "\n雨伞：" + weather.getUmbrella() + "\n购物：" + weather.getShopping());
        imageView = (ImageView) view.findViewById(R.id.tomorrow_weather_code);
        imageView.setImageResource(view.getResources().getIdentifier(
                "small" + weather.getTomorrowCode(),
                "drawable", view.getContext().getPackageName()));
        textView = (TextView) view.findViewById(R.id.tomorrow_weather_text);
        textView.setText(weather.getTomorrowText());
        textView = (TextView) view.findViewById(R.id.tomorrow_low_and_high_temperature);
        textView.setText(weather.getTomorrowLowTemperature() + " ~ " +
                weather.getTomorrowHighTemperature() + "℃");
        textView = (TextView) view.findViewById(R.id.tomorrow_wind_direction);
        textView.setText("风向：" + weather.getTomorrowWindDirection());
        textView = (TextView) view.findViewById(R.id.tomorrow_wind_scale);
        textView.setText("风力等级：" + weather.getTomorrowWindScale());
        imageView = (ImageView) view.findViewById(R.id.after_tomorrow_weather_code);
        imageView.setImageResource(view.getResources().getIdentifier(
                "small" + weather.getAfterTomorrowCode(),
                "drawable", view.getContext().getPackageName()));
        textView = (TextView) view.findViewById(R.id.after_tomorrow_weather_text);
        textView.setText(weather.getAfterTomorrowText());
        textView = (TextView) view.findViewById(R.id.after_tomorrow_low_and_high_temperature);
        textView.setText(weather.getAfterTomorrowLowTemperature() + " ~ " +
                weather.getAfterTomorrowHighTemperature() + "℃");
        textView = (TextView) view.findViewById(R.id.after_tomorrow_wind_direction);
        textView.setText("风向：" + weather.getAfterTomorrowWindDirection());
        textView = (TextView) view.findViewById(R.id.after_tomorrow_wind_scale);
        textView.setText("风力等级：" + weather.getAfterTomorrowWindScale());
    }

    private Weather loadWeather() {
        weather = new Weather();
        SharedPreferences sharedPreferences = context.getSharedPreferences("city",
                Context.MODE_PRIVATE);
        City city = new City();
        city.setId(sharedPreferences.getString("cityId", ""));
        city.setPath(sharedPreferences.getString("cityPath",""));
        city.setName(sharedPreferences.getString("cityName", ""));
        weather.setCity(city);
        weather.setText(sharedPreferences.getString("weatherText", ""));
        weather.setCode(sharedPreferences.getString("weatherCode", ""));
        weather.setDate(sharedPreferences.getString("date", ""));
        weather.setTemperature(sharedPreferences.getString("temperature", ""));
        weather.setHighTemperature(sharedPreferences.getString("highTemperature", ""));
        weather.setLowTemperature(sharedPreferences.getString("lowTemperature", ""));
        weather.setWindDirection(sharedPreferences.getString("windDirection", ""));
        weather.setWindScale(sharedPreferences.getString("windScale", ""));
        weather.setUv(sharedPreferences.getString("uv", ""));
        weather.setComfort(sharedPreferences.getString("comfort", ""));
        weather.setCarWashing(sharedPreferences.getString("carWashing", ""));
        weather.setDressing(sharedPreferences.getString("dressing", ""));
        weather.setTraffic(sharedPreferences.getString("traffic", ""));
        weather.setMorningSport(sharedPreferences.getString("morningSport", ""));
        weather.setSport(sharedPreferences.getString("sport", ""));
        weather.setUmbrella(sharedPreferences.getString("umbrella", ""));
        weather.setShopping(sharedPreferences.getString("shopping", ""));
        weather.setTomorrowText(sharedPreferences.getString("tomorrowText", ""));
        weather.setTomorrowCode(sharedPreferences.getString("tomorrowCode", ""));
        weather.setTomorrowHighTemperature(
                sharedPreferences.getString("tomorrowHighTemperature", ""));
        weather.setTomorrowLowTemperature(
                sharedPreferences.getString("tomorrowLowTemperature", ""));
        weather.setTomorrowWindDirection(sharedPreferences.getString("tomorrowWindDirection", ""));
        weather.setTomorrowWindScale(sharedPreferences.getString("tomorrowWindScale", ""));
        weather.setAfterTomorrowText(sharedPreferences.getString("afterTomorrowText", ""));
        weather.setAfterTomorrowCode(sharedPreferences.getString("afterTomorrowCode", ""));
        weather.setAfterTomorrowHighTemperature(sharedPreferences.getString(
                "afterTomorrowHighTemperature", ""));
        weather.setAfterTomorrowLowTemperature(sharedPreferences.getString(
                "afterTomorrowLowTemperature", ""));
        weather.setAfterTomorrowWindDirection(sharedPreferences.getString(
                "afterTomorrowWindDirection", ""));
        weather.setAfterTomorrowWindScale(sharedPreferences.getString(
                "afterTomorrowWindScale", ""));
        return weather;
    }

    private void downloadWeather(String cityId) {
        WeatherAsyncTask weatherAsyncTask = new WeatherAsyncTask(view, getActivity());
        weatherAsyncTask.execute(cityId);
    }

    private void shareWeather() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String weatherText = getWeatherText();
        if (weatherText == null) {
            return;
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, weatherText);
        shareIntent.setType("text/plain");

        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private void searchCity() {
        Intent intent = new Intent(context, SearchCityActivity.class);
        startActivityForResult(intent, 1);
    }

    public String getWeatherText() {
        String weatherText = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences("city",
                Context.MODE_PRIVATE);
        String cityId = sharedPreferences.getString("cityId", "");
        if (cityId.equals("")) {    //没有缓存，自动定位
            Toast.makeText(getActivity(), "请刷新后重试", Toast.LENGTH_SHORT).show();
        } else {
            weatherText = "#天气预报#" + sharedPreferences.getString("cityPath", "") + "：\n" +
                    "【今天】" + sharedPreferences.getString("weatherText","") + "，" +
                    sharedPreferences.getString("lowTemperature","") + "~" +
                    sharedPreferences.getString("highTemperature","") + "℃，" +
                    "风向：" + sharedPreferences.getString("windDirection","") +
                    "，风力：" + sharedPreferences.getString("windScale","") +
                    "；\n【明天】"  + sharedPreferences.getString("tomorrowWeatherText","") + "，" +
                    sharedPreferences.getString("tomorrowLowTemperature","") + "~" +
                    sharedPreferences.getString("tomorrowHighTemperature","") + "℃，" +
                    "风向：" + sharedPreferences.getString("tomorrowWindDirection","") +
                    "，风力：" + sharedPreferences.getString("tomorrowWindScale","") +
                    "；\n【后天】"  + sharedPreferences.getString("afterTomorrowWeatherText","") + "，" +
                    sharedPreferences.getString("afterTomorrowLowTemperature","") + "~" +
                    sharedPreferences.getString("afterTomorrowHighTemperature","") + "℃，" +
                    "风向：" + sharedPreferences.getString("afterTomorrowWindDirection","") +
                    "，风力：" + sharedPreferences.getString("afterTomorrowWindScale","") + "。";
        }
        return weatherText;
    }
}
