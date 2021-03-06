package com.ddutra9.sunshinenano;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ddutra9.sunshinenano.data.WeatherContract;

import java.text.SimpleDateFormat;

/**
 * Created by donato on 14/07/17.
 */

public class DetailFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>{
    private TextView detailDateTextview, dayMaxTemp, dayMinTemp, humidityText, windSpeedText,
            pressureText, mDescriptionView;
    private ImageView detailIcon;
    private static final int LOADER_DETAIL = 0;
    public static final String DETAIL_TRANSITION_ANIMATION = "DTA";

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
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
    static final int COL_WEATHER_CONDITION_ID = 9;

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    public static final String DETAIL_URI = "URI";
    private String mForecastStr;
    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private boolean mTransitionAnimation;

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
        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);

        Log.v(LOG_TAG, "onCreateView");

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        detailIcon = (ImageView) rootView.findViewById(R.id.detail_icon);
        detailDateTextview = (TextView) rootView.findViewById(R.id.detail_date_textview);
        dayMaxTemp = (TextView) rootView.findViewById(R.id.detail_high_textview);
        dayMinTemp = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityText = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windSpeedText = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureText = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
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
        if ( null != mUri ) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        ViewParent vp = getView().getParent();
        if ( vp instanceof CardView) {
            ((View)vp).setVisibility(View.INVISIBLE);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished");
        if (data == null || !data.moveToFirst()) {
            return;
        }

        ViewParent vp = getView().getParent();
        if (vp instanceof CardView) {
            ((View) vp).setVisibility(View.VISIBLE);
        }

        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String weatherDesc = data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(weatherDesc);
        mDescriptionView.setContentDescription(getString(R.string.a11y_forecast, weatherDesc));

        boolean isMetric = Utility.isMetric(getActivity());

        String max = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String min = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        mForecastStr = String.format("%s - %s - %s/%s", dateString, weatherDesc, max, min);
        detailDateTextview.setText(Utility.getFullFriendlyDayString(getContext(), data.getLong(COL_WEATHER_DATE)));

        dayMaxTemp.setText(max);
        dayMaxTemp.setContentDescription(getString(R.string.a11y_high_temp, max));
        dayMinTemp.setText(min);
        dayMinTemp.setContentDescription(getString(R.string.a11y_low_temp, min));

        Glide.with(this)
                .load(Utility.getArtUrlForWeatherCondition(getActivity(), data.getInt(COL_WEATHER_CONDITION_ID)))
                .error(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)))
                .crossFade()
                .into(detailIcon);

        detailIcon.setContentDescription(getString(R.string.a11y_forecast_icon, weatherDesc));

        windSpeedText.setText(Utility.getFormattedWind(getContext(), data.getFloat(COL_WIND_SPEED),
                data.getFloat(COL_DEGREES)));
        windSpeedText.setContentDescription(windSpeedText.getText());

        humidityText.setText(Utility.getFormattedHumidity(getContext(), data.getFloat(COL_HUMIDITY)));
        humidityText.setContentDescription(humidityText.getText());

        pressureText.setText(Utility.getFormattedPressure(getContext(), data.getFloat(COL_PRESSURE)));

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForestIntent());
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (mTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLocationChanged(String newLocation) {
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(LOADER_DETAIL, null, this);
        }
    }
}
