package com.mahbuburrahman.weatherapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.mahbuburrahman.weatherapp.R;
import com.mahbuburrahman.weatherapp.datasource.SharedPrefsData;
import com.mahbuburrahman.weatherapp.fragments.ForeCastFragment;
import com.mahbuburrahman.weatherapp.fragments.MainFragment;
import com.mahbuburrahman.weatherapp.model.WeatherResponse;
import com.mahbuburrahman.weatherapp.services.WeatherServices;
import com.mahbuburrahman.weatherapp.utils.NetworkHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MainFragment.OnSearchTypeFinishListener {

    private static final int PERMISSION_CODE = 100;
    private ViewPager pager;
    private ImageButton settingBtn;
    private ImageButton searchBtn;
    private ImageButton leftBtn;
    private ImageButton rightBtn;
    private ImageButton homeBtn;
    public static String QueryString = null;
    public static double latitude;
    public static double longitude;
    public static MainFragment mMainFragment = null;
    public static ForeCastFragment mForeCastFragment = null;

    public static boolean celsius = false;
    private boolean hasNetwork = false;
    private View view;

    //GPS location
    private FusedLocationProviderClient mClient = null;
    private LocationCallback mCallback = null;
    private LocationRequest mLocationRequest = null;

    //API URLS
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private String API_KEY;

    private WeatherServices mServices;
    private Retrofit retrofit;

    public static SharedPrefsData defaultDataStore = null;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        //Attach the Tab bar
        // TabLayout tabLayout = findViewById(R.id.tabs);
        //  tabLayout.setupWithViewPager(pager);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please Wait....");

        settingBtn = findViewById(R.id.settings_btn);
        //searchBtn = findViewById(R.id.search_btn);
        leftBtn = findViewById(R.id.left_nav);
        rightBtn = findViewById(R.id.right_nav);
        homeBtn = findViewById(R.id.homeBtn);


        SectionPagerAdapter pagerAdapter = new SectionPagerAdapter(getSupportFragmentManager(), this);

        pager = findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    leftBtn.setVisibility(View.INVISIBLE);
                    rightBtn.setVisibility(View.VISIBLE);
                }else if(position == 1){
                    leftBtn.setVisibility(View.VISIBLE);
                    rightBtn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        settingBtn.setOnClickListener(this);
        // searchBtn.setOnClickListener(this);

        // Images left navigation
        leftBtn.setOnClickListener(this);
        leftBtn.setVisibility(View.INVISIBLE);
        // Images right navigatin
        rightBtn.setOnClickListener(this);
        //home
        homeBtn.setOnClickListener(this);


        //TODO: get previous values
        defaultDataStore = new SharedPrefsData(this);

        //TODO: checkPermission
        checkPermission();
        //get location latitude and longitude
        getLatLon();

    }

    private void showErrorMessage(String s, int duration) {
        if (view == null) {
            view = findViewById(R.id.main_view);
        }
        Snackbar.make(view, s, Snackbar.LENGTH_INDEFINITE)
                .setDuration(duration)
                .show();
    }
    private void showErrorNotification(String s, final String url) {
        if (view == null) {
            view = findViewById(R.id.main_view);
        }
        Snackbar.make(view, s, Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hasNetwork = NetworkHelper.hasNetwork(MainActivity.this);
                        //getLatLon();
                        if (hasNetwork) {

                            if (latitude == 0 && longitude == 0){
                                showErrorNotification("Please active the GPS...", url);
                            }else {
                                getData(url);
                            }
                        } else {
                            stopLocationUpdates();
                            showErrorNotification("No Network found!", url);
                        }
                    }
                }).show();
    }

    private void getData(final String url) {

        mProgressDialog.show();
        //TODO: call API
        API_KEY = getResources().getString(R.string.api_key);

        hasNetwork = NetworkHelper.hasNetwork(this);
        if (!hasNetwork) {
            showErrorNotification("No network found!", url);
            mProgressDialog.dismiss();
            return;
        }
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            mServices = retrofit.create(WeatherServices.class);
        }

        String urlString = String.format(BASE_URL + url + API_KEY);

        Log.d("data", "getData: " + urlString);

        Call<WeatherResponse> call = mServices.getCurrentWeather(urlString);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                mProgressDialog.dismiss();
                if (response.code() == 200) {
                    WeatherResponse currentWeather = response.body();
                    setWeatherDetails(currentWeather);
                    defaultDataStore.saveData(currentWeather);
                    Log.d("weather", "onResponse: " + currentWeather.getMain().getTemp());
                } else {
                    showErrorNotification(response.message(), url);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                mProgressDialog.dismiss();
                showErrorMessage("Something went wrong! Please try again.", 3000);
            }
        });
    }

    public void setWeatherDetails(WeatherResponse currentWeather) {

        if (mMainFragment != null) {
            mMainFragment.setData(currentWeather);
        }

    }


    //TODO: left and right navigation And search and settings listener
    @Override
    public void onClick(View view) {
        int id = view.getId();
        int tab = 0;
        switch (id) {
            case R.id.homeBtn:
                latitude = defaultDataStore.getLastLocationLatitude();
                longitude = defaultDataStore.getLastLocationLongitude();
                callApi();
                pager.setCurrentItem(0);
                break;
            case R.id.settings_btn:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            //TODO; left and right
            case R.id.left_nav:
                tab = pager.getCurrentItem();
                if (tab > 0) {
                    tab--;
                    pager.setCurrentItem(tab);
                } else if (tab == 0) {
                    pager.setCurrentItem(tab);
                }
                break;
            case R.id.right_nav:
                tab = pager.getCurrentItem();
                tab++;
                pager.setCurrentItem(tab);
                break;

        }
    }

    public void getLatLon() {
        //TODO: get location
        getLocation();
    }

    private void callApi() {
        String urlString = String.format("weather?lat=%f&lon=%f&appid=", latitude, longitude);
        hasNetwork = NetworkHelper.hasNetwork(MainActivity.this);
        if (hasNetwork) {
            getData(urlString);
        } else {
            Log.d("OK", "getLatLon: " + hasNetwork);
            stopLocationUpdates();
            showErrorNotification("No Network found!", urlString);
        }
    }

    @Override
    public void onSearchTypeFinished(String city) {
        //TODO; search here
        Log.d("search", "onSearchTypeFinished: " + city);
        stopLocationUpdates();
        String urlString = String.format("weather?q=%s&appid=", city);
        getData(urlString);

    }

    private class SectionPagerAdapter extends FragmentPagerAdapter {
        Context mContext;

        public SectionPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (mMainFragment == null) {
                        mMainFragment = new MainFragment();
                    }
                    return mMainFragment;
                case 1:
                    if (mForeCastFragment == null) {
                        mForeCastFragment = new ForeCastFragment();
                    }
                    return mForeCastFragment;


            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return mContext.getResources().getText(R.string.current);
                case 1:
                    return mContext.getResources().getText(R.string.forecast);

            }
            return null;
        }
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,}, PERMISSION_CODE);
            return;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void getLocation() {
        mClient = LocationServices.getFusedLocationProviderClient(this);


        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000)
                .setFastestInterval(1000);

        mCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        if (latitude == 0 && longitude == 0 && location.getLatitude() != 0 && location.getLongitude() != 0) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            defaultDataStore.saveLastLocation((float) latitude, (float) longitude);
                            Log.d("location", "onLocationResult: " + latitude + " " + longitude);
                            stopLocationUpdates();
                        }

                        Log.d("location", "onLocationResult: " + latitude + " " + longitude);
                        // return;
                    } else {
                        Log.d("location", "onLocationResult: null");
                    }
                }

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mClient.requestLocationUpdates(mLocationRequest, mCallback, null);
        Log.d("location", "getLocation: "+latitude +" "+longitude);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("location", "onStop: called");
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (mClient != null && mCallback != null) {
            mClient.removeLocationUpdates(mCallback);
        }
    }

}