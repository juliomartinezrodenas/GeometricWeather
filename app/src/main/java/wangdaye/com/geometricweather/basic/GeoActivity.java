package wangdaye.com.geometricweather.basic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Geometric weather activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {

    private List<GeoDialogFragment> dialogList;
    private boolean foreground;

    private BroadcastReceiver localeChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && intent.getAction() != null
                    && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                LanguageUtils.setLanguage(
                        GeoActivity.this,
                        SettingsOptionManager.getInstance(context).getLanguage()
                );
            }
        }
    };

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(localeChangedReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
        LanguageUtils.setLanguage(
                this,
                SettingsOptionManager.getInstance(this).getLanguage()
        );

        GeometricWeather.getInstance().addActivity(this);

        boolean darkMode = DisplayUtils.isDarkMode(this);

        DisplayUtils.setWindowTopColor(this, 0);
        DisplayUtils.setSystemBarStyle(
                getWindow(), false, false, !darkMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DisplayUtils.setNavigationBarColor(
                    this, ContextCompat.getColor(this, R.color.colorRootDark));
        }

        this.dialogList = new ArrayList<>();
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        GeometricWeather.getInstance().removeActivity(this);
        unregisterReceiver(localeChangedReceiver);
    }

    public View provideSnackbarContainer() {
        if (dialogList.size() > 0) {
            return dialogList.get(dialogList.size() - 1).getSnackbarContainer();
        } else {
            return getSnackbarContainer();
        }
    }

    public abstract View getSnackbarContainer();

    public boolean isForeground() {
        return foreground;
    }

    public List<GeoDialogFragment> getDialogList() {
        return dialogList;
    }
}
