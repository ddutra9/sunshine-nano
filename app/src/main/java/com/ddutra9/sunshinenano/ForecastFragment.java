package com.ddutra9.sunshinenano;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ddutra9.sunshinenano.data.WeatherContract;
import com.ddutra9.sunshinenano.service.SunshineService;
import com.ddutra9.sunshinenano.sync.SunshineSyncAdapter;

import static android.R.attr.data;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    //    private ArrayAdapter<String> mForecastAdapter;
    private static final String TAG = ForecastFragment.class.getSimpleName();
    private ForecastAdapter mForecastAdapter;
    private static final int MY_LOADER_ID = 0;
    private static final int REQUEST_PERMISSION_INTERNT = 1;
    private static final String SELECTED_KEY = "selected_position";

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    private RecyclerView recycleViewForecast;
    private View emptyView;
    private int mPosition = RecyclerView.NO_POSITION;
    private boolean useTodayLayout, mAutoSelectView;
    private int mChoiceMode;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }



    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        List<String> weekForecast = new ArrayList<String>();
//        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
//                R.id.list_item_forecast_textview, weekForecast);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        emptyView = rootView.findViewById(R.id.recycleview_forecast_empty);
        recycleViewForecast = (RecyclerView) rootView.findViewById(R.id.recycleview_forecast);

        mForecastAdapter = new ForecastAdapter(getActivity(), new ForecastAdapter.ForecastAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, ForecastAdapter.ForecastAdapterViewHolder vh) {
                String locationSetting = Utility.getPreferredLocation(getActivity());
                ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, date));
                mPosition = vh.getAdapterPosition();
            }
        }, emptyView, mChoiceMode);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), OrientationHelper.VERTICAL, false);

        recycleViewForecast.setLayoutManager(layoutManager);
        recycleViewForecast.setAdapter(mForecastAdapter);
        recycleViewForecast.setHasFixedSize(true);

//        recycleViewForecast.setEmptyView(emptyView);

//        listViewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String forecast = mForecastAdapter.getItem(position);
//                Intent intent = new Intent(getActivity(), DetailActivity.class)
//                        .putExtra(Intent.EXTRA_TEXT, forecast);
//                startActivity(intent);
//            }
//        });

//        recycleViewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
//                // CursorAdapter returns a cursor at the correct position for getItem(), or null
//                // if it cannot seek to that position.
//                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
//                if (cursor != null) {
//                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    ((Callback) getActivity())
//                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//
//                    mPosition = position;
//                }
//            }
//        });


        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(SELECTED_KEY)) {
                // The Recycler View probably hasn't even been populated yet.  Actually perform the
                // swapout in onLoadFinished.
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }

            mForecastAdapter.onRestoreInstanceState(savedInstanceState);
        }

        mForecastAdapter.setUseTodayLayout(useTodayLayout);

        return rootView;
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.ForecastFragment,
                +0, 0);
        mChoiceMode = a.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        a.recycle();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:

                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_PERMISSION_INTERNT);
                } else {
                    Log.v("ForecastFragment", "esta executando executeFetchWeatherTask");
                    updateWeather();
                }

                return true;
            case R.id.action_map: {
                openPreferredLocationInMap();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_INTERNT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateWeather();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    void onLocationChanged( ) {
        updateWeather();
        getLoaderManager().restartLoader(MY_LOADER_ID, null, this);
    }

    private void updateWeather() {
//        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
//        String location = Utility.getPreferredLocation(getActivity());
//        weatherTask.execute(location);
        Log.d(TAG, "updateWeather");
//        Intent alarmIntent = new Intent(getContext(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
//
//        PendingIntent pi = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager alarmMgr = (AlarmManager)getContext().getSystemService(getContext().ALARM_SERVICE);
//        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, pi);

        SunshineSyncAdapter.syncImmediately(getContext());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);

        if (mPosition != RecyclerView.NO_POSITION) {
            recycleViewForecast.smoothScrollToPosition(mPosition);
        }

        updateEmpityView();
        if (data.getCount() > 0) {
            recycleViewForecast.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (recycleViewForecast.getChildCount() > 0) {
                        recycleViewForecast.getViewTreeObserver().removeOnPreDrawListener(this);
                        int itemPosition = mForecastAdapter.getSelectedItemPosition();
                        if (RecyclerView.NO_POSITION == itemPosition) itemPosition = 0;
                        RecyclerView.ViewHolder vh = recycleViewForecast.findViewHolderForAdapterPosition(itemPosition);
                        if (null != vh && mAutoSelectView) {
                            mForecastAdapter.selectView(vh);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void updateEmpityView(){
        int message = R.string.empty_forecast_list;
        @SunshineSyncAdapter.NavigationMode int navegationMode = Utility.getNavegationMode(getActivity());
        switch (navegationMode){
            case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:{
                message = R.string.empty_forecast_list_server_down;
                break;
            }
            case SunshineSyncAdapter.LOCATION_STATUS_INVALID:{
                message = R.string.empty_forecast_list_invalid_location;
                break;
            }
            case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:{
                message = R.string.empty_forecast_list_server_error;
                break;
            }
        }

        if(!Utility.isNetworkAvaliable(getActivity())){
            message = R.string.no_network_avaliable;
        }

        ((TextView)emptyView).setText(getString(message));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateEmpityView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        mForecastAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
        if(mForecastAdapter != null){
            mForecastAdapter.setUseTodayLayout(this.useTodayLayout);
        }
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != mForecastAdapter ) {
            Cursor c = mForecastAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
