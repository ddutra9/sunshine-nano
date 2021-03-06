package com.ddutra9.sunshinenano;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;

import static com.ddutra9.sunshinenano.SettingsActivity.PLACE_PICKER_REQUEST;

/**
 * Created by donato on 18/09/17.
 */

public class LocationEditTextPreferecnce extends EditTextPreference {

    static final private int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;
    private int minLength;

    public LocationEditTextPreferecnce(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreferecnce,
                0, 0);

        try {
            minLength = a.getInteger(R.styleable.LocationEditTextPreferecnce_minLength, DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (resultCode == ConnectionResult.SUCCESS) {
            // Add the get current location widget to our location preference
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();

                // Launch the Place Picker so that the user can specify their location, and then
                // return the result to SettingsActivity.
                // TODO(student): Create a PlacePicker.IntentBuilder object here.
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();


                // We are in a view right now, not an activity. So we need to get ourselves
                // an activity that we can use to start our Place Picker intent. By using
                // SettingsActivity in this way, we can ensure the result of the Place Picker
                // intent comes to the right place for us to process it.
                Activity settingsActivity = (SettingsActivity) context;
                try {
                    // TODO(student): Launch the intent using your settingsActivity object to access
                    // startActivityForResult(). You'll need to build your builder object and use
                    // the request code we declared in SettingsActivity.
                    settingsActivity.startActivityForResult(builder.build(settingsActivity), PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                    // What did you do?? This is why we check Google Play services in onResume!!!
                    // The difference in these exception types is the difference between pausing
                    // for a moment to prompt the user to update/install/enable Play services vs
                    // complete and utter failure.
                    // If you prefer to manage Google Play services dynamically, then you can do so
                    // by responding to these exceptions in the right moment. But I prefer a cleaner
                    // user experience, which is why you check all of this when the app resumes,
                    // and then disable/enable features based on that availability.
                }
            }
        });

        return view;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog d = getDialog();
                if(d instanceof AlertDialog){
                    AlertDialog dialog = (AlertDialog) d;
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    if(s.length() >= minLength){
                        positiveButton.setEnabled(true);
                    } else {
                        positiveButton.setEnabled(false);
                    }
                }
            }
        });
    }
}
