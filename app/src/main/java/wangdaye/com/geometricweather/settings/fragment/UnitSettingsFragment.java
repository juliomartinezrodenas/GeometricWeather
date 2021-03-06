package wangdaye.com.geometricweather.settings.fragment;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Unit settings fragment.
 * */

public class UnitSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_unit);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    // interface.

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_fahrenheit))) {
            // ℉
            SettingsOptionManager.getInstance(getActivity()).setFahrenheit(
                    !SettingsOptionManager.getInstance(getActivity()).isFahrenheit()
            );
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_imperial))) {
            // imperial units.
            SettingsOptionManager.getInstance(getActivity()).setImperial(
                    !SettingsOptionManager.getInstance(getActivity()).isImperial()
            );
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        }
        return super.onPreferenceTreeClick(preference);
    }
}