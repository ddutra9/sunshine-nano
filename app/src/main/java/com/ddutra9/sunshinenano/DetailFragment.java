package com.ddutra9.sunshinenano;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddutra9.sunshinenano.data.WeatherContract;

import java.text.SimpleDateFormat;

/**
 * Created by donato on 14/07/17.
 */

public class DetailFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>{
    private TextView dayWeekText, dayMonthText, dayMaxTemp, dayMinTemp, humidityText, windSpeedText,
            pressureText, mDescriptionView;
    private static final int LOADER_DETAIL = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_HUMIDITY = 5;
    static final int COL_WIND_SPEED = 6;
    static final int COL_PRESSURE = 7;
    static final int COL_DEGREES = 8;

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecastStr;
    private ShareActionProvider mShareActionProvider;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(menuItem);

        if(mForecastStr != null){
            mShareActionProvider.setShareIntent(createShareForestIntent());
        } else{
            Log.d(LOG_TAG, "ShareAction provider is null!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Log.v(LOG_TAG, "onCreateView");

        dayWeekText = (TextView) rootView.findViewById(R.id.day_week_text);
        dayMonthText = (TextView) rootView.findViewById(R.id.day_month_text);
        dayMaxTemp = (TextView) rootView.findViewById(R.id.max_temp_text);
        dayMinTemp = (TextView) rootView.findViewById(R.id.min_temp_text);
        humidityText = (TextView) rootView.findViewById(R.id.humidity_text);
        windSpeedText = (TextView) rootView.findViewById(R.id.wind_text);
        pressureText = (TextView) rootView.findViewById(R.id.pressure_text);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_DETAIL, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private Intent createShareForestIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader");
        Intent intent = getActivity().getIntent();

        if(intent == null){
            return  null;
        }

        return new CursorLoader(getActivity(), intent.getData(), FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished");
        if(!data.moveToFirst()) {
            return;
        }
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("MMM dd");

        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String weatherDesc = data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(weatherDesc);

        boolean isMetric = Utility.isMetric(getActivity());

        String max = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String min = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        mForecastStr = String.format("%s - %s - %s/%s", dateString, weatherDesc, max, min);
        dayWeekText.setText(Utility.getDayName(getContext(), data.getLong(COL_WEATHER_DATE)));
        dayMonthText.setText(shortenedDateFormat.format(data.getLong(COL_WEATHER_DATE)));

        dayMaxTemp.setText(max);
        dayMinTemp.setText(min);

        windSpeedText.setText(Utility.getFormattedWind(getContext(), data.getFloat(COL_WIND_SPEED),
                data.getFloat(COL_DEGREES)));

        humidityText.setText(Utility.getFormattedHumidity(getContext(), data.getFloat(COL_HUMIDITY)));

        pressureText.setText(Utility.getFormattedPressure(getContext(), data.getFloat(COL_PRESSURE)));

        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForestIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
