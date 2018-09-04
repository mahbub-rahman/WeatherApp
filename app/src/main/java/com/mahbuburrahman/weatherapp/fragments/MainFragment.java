package com.mahbuburrahman.weatherapp.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.mahbuburrahman.weatherapp.R;
import com.mahbuburrahman.weatherapp.activities.MainActivity;
import com.mahbuburrahman.weatherapp.model.WeatherResponse;
import com.mahbuburrahman.weatherapp.utils.NetworkHelper;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private TextView tempTv;
    private TextView cityTv;
    private TextView timeTv;
    private TextView weatherMainTv;
    private TextView weatherDescTv;
    private TextView sunriseTv;
    private TextView sunsetTv;
    private ImageView imageWeather;
    private ImageView weaTherIcon;
    private TextView dayTimeTv;
    private TextView maxTempTv;
    private TextView minTempTv;
    private TextView windTv;
    private TextView cloudPressureTV;
    private TextView humidityTv;

    private AutoCompleteTextView searchEdit;
    private ImageButton searchImgBtn;
    private ImageView weatherIcon;

    private Context mContext;
    private View view;

    ArrayList<String> cities;
    ArrayAdapter<String> adapter;

    private OnSearchTypeFinishListener mSearchTypeFinishListener;
    private final int AUTOCOMPLTE_REQUEST = 111;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mSearchTypeFinishListener = (OnSearchTypeFinishListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_main, container, false);


        WeatherResponse weatherResponse = MainActivity.defaultDataStore.getData();



        tempTv = view.findViewById(R.id.temp_tv);
        cityTv = view.findViewById(R.id.city_tv);
        timeTv = view.findViewById(R.id.current_time_tv);
        weatherMainTv = view.findViewById(R.id.main_weather_tv);
        weatherDescTv = view.findViewById(R.id.weather_desc_tv);
        sunriseTv = view.findViewById(R.id.sunrise_weather_tv);
        sunsetTv = view.findViewById(R.id.sunset_weather_tv);
        imageWeather = view.findViewById(R.id.image_weather);
        searchEdit = view.findViewById(R.id.search_edit);
        searchImgBtn = view.findViewById(R.id.searchImgbtn);
        weatherIcon = view.findViewById(R.id.weather_icon_iv);



        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int length, KeyEvent keyEvent) {
                Log.d("edit", "onEditorAction: "+length+" "+searchEdit.getText().toString());
                searchCity();
                return true;
            }
        });

        // String[] suggestions = mContext.getResources().getStringArray(R.array.cities);

        cities = MainActivity.defaultDataStore.getSuggestions();

        adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_dropdown_item_1line, cities);
        searchEdit.setAdapter(adapter);


        searchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchCity();
            }
        });

        dayTimeTv = view.findViewById(R.id.time_day_tv);
        maxTempTv = view.findViewById(R.id.max_weather_tv);
        minTempTv = view.findViewById(R.id.min_weather_tv);
        cloudPressureTV = view.findViewById(R.id.cloud_prerssure_tv);
        windTv = view.findViewById(R.id.wind_tv);
        humidityTv = view.findViewById(R.id.heumidity_tv);
        weaTherIcon = view.findViewById(R.id.weather_icon);



        if (weatherResponse !=null) {
            setData(weatherResponse);
            Log.d("default", "onCreateViewFragemet: not null");
        }else{
            Log.d("default", "onCreateViewFragemet: null");
        }



        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case AUTOCOMPLTE_REQUEST:
                if(resultCode == RESULT_OK){
                    Place place = PlaceAutocomplete.getPlace(mContext,data);
                   searchEdit.setText(place.getName().toString());
                }
                break;
        }
    }

    private void showAutocompleteWidget() {
        try {
            AutocompleteFilter filter = new AutocompleteFilter.Builder()
                    .setCountry("BD").build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(filter).build((Activity) mContext);
            startActivityForResult(intent,AUTOCOMPLTE_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void searchCity(){

        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


        String searchQuery = searchEdit.getText().toString();
        if (!TextUtils.isEmpty(searchQuery)) {

            mSearchTypeFinishListener.onSearchTypeFinished(searchQuery);

            if(!cities.contains(searchQuery)) {
                MainActivity.defaultDataStore.saveSuggetion(searchQuery);
                adapter.add(searchQuery);
                adapter.notifyDataSetChanged();
            }
            searchEdit.setText("");
            Log.d("sugg", "onClick: "+cities.size());

        }
    }

    public void setVales(String temp, String city, String time, String mainWeather,
                         String weatherDesc,String sunrise, String sunset,int imgID,
                         String day, String max, String min, String wind,
                         String cloudAndPressure,String humidity,String img) {

        if (view != null) {
           tempTv.setText(temp);
           cityTv.setText(city);
           timeTv.setText("Currently: "+time);
           weatherMainTv.setText(mainWeather);
           weatherDescTv.setText(weatherDesc);
           sunriseTv.setText("Sunrises: "+sunrise);
           sunsetTv.setText("Sunsets: "+ sunset);
           imageWeather.setImageResource(imgID);

           dayTimeTv.setText(day);
           maxTempTv.setText(max);
           minTempTv.setText(min);
           cloudPressureTV.setText(cloudAndPressure);
           windTv.setText(wind);
           humidityTv.setText(humidity);

            Uri uri = Uri.parse("http://api.openweathermap.org/img/w/" + img);
            Picasso.with(mContext).load(uri).into(weaTherIcon);
            Picasso.with(mContext).load(uri).into(weatherIcon);
                //Picasso.with(mContext).load(uri).into(imageWeather);

            Log.d("default", "setVales: called");
        }else{
            Log.d("default", "setVales: called "+(view == null));
        }

    }

    public void setData(WeatherResponse currentWeather){

        if (currentWeather != null) {

            if (NetworkHelper.hasNetwork(mContext)) {
                //Set lat lon for forecast
                if (MainActivity.mForeCastFragment != null) {
                    MainActivity.mForeCastFragment.setForecast(currentWeather.getCoord().getLat(),
                            currentWeather.getCoord().getLon());
                }
            }

            String currentDateTimeString = DateFormat.getDateTimeInstance()
                    .format(new Date(currentWeather.getSys().getSunrise() * 1000));

            List<WeatherResponse.Weather> weatherList = currentWeather.getWeather();
            DecimalFormat df = new DecimalFormat("#.#");

            String sunrise = convertTime(currentWeather.getSys().getSunrise());
            String sunset = convertTime(currentWeather.getSys().getSunset());

            double temp = currentWeather.getMain().getTemp() - 273;
            double maxTmp = currentWeather.getMain().getTempMax() - 273;
            double minTmp = currentWeather.getMain().getTempMin() - 273;


            String time = convertTime(currentWeather.getDt());
            String dayTime = convertTimeDay(currentWeather.getDt()) + "\n\tToday";

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean celsius = settings.getBoolean(mContext.getString(R.string.celsious_key), false);


            if (!celsius) {
                temp = ((9 / 5.0) * (temp)) + 32;
                maxTmp = ((9 / 5.0) * (maxTmp)) + 32;
                minTmp = ((9 / 5.0) * (minTmp)) + 32;
            }
            float onDigitsF = Float.valueOf(df.format(temp));
            float onDigitsFMax = Float.valueOf(df.format(maxTmp));
            float onDigitsFMin = Float.valueOf(df.format(minTmp));
            int resourceID = mContext.getResources().getIdentifier(getImage(weatherList.get(0).getId()), "drawable", mContext.getPackageName());




            String maxTem = "Max " + onDigitsFMax + "°" + (celsius ? "C" : "F");
            String minTem = "Min " + onDigitsFMin + "°" + (celsius ? "C" : "F");
            String wind = "Wind " + currentWeather.getWind().getSpeed() + "m/s, " + currentWeather.getWind().getDeg() + "°";
            String cloudPressuer = "Cloud " + currentWeather.getClouds().getAll() + "%,Pressure " + currentWeather.getMain().getPressure() + "hpa";
            String humidity = "Humidity " + currentWeather.getMain().getHumidity() + "%";
            String img = weatherList.get(0).getIcon() + ".png";

            setVales(
                    onDigitsF + "" + (celsius ? "°C" : "°F"),
                    currentWeather.getName(),
                    time,
                    weatherList.get(0).getMain(),
                    weatherList.get(0).getDescription(),
                    sunrise,
                    sunset,
                    resourceID,
                    dayTime,
                    maxTem,
                    minTem,
                    wind,
                    cloudPressuer,
                    humidity,
                    img

            );
        }
    }

    public interface OnSearchTypeFinishListener {
        void onSearchTypeFinished(String city);
    }

    public String convertTime(long times) {
        Date date = new Date(times * 1000);
        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        String time = sdf.format(date);

        Log.d("TIME", "convertTime: "+time);
        return time;

    }
    public String convertTimeDay(long times) {
        Date date = new Date(times * 1000);
        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM", Locale.getDefault());
        String time = sdf.format(date);

        Log.d("TIME", "day time: "+time);
        return time;

    }
    public String getImage(int condition) {
        if (condition >=0 && condition <300) {
            return "tstorm1";
        } else if (condition >= 300 && condition < 500) {
            return "light_rain";
        } else if (condition >= 500 && condition < 600) {
            return "shower3";
        } else if (condition >= 600 && condition <= 700) {
            return "snow4";
        } else if (condition >= 701 && condition <= 771) {
            return "fog";
        } else if (condition >= 772 && condition < 800) {
            return "tstorm3";
        } else if (condition == 800) {
            return "sunny";
        } else if (condition >= 801 && condition <= 804) {
            return "cloudy2";
        } else if (condition >= 900 && condition <= 902) {
            return "tstorm3";
        } else if (condition == 903) {
            return "snow5";
        } else if (condition == 904) {
            return "sunny";
        }else if (condition >= 905) {
            return "tstorm3";
        }

        return "dunno";
    }
}
