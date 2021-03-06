package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.os.Build;
import android.service.quicksettings.Tile;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.weather.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Tile helper.
 * */

public class TileHelper {
    // data
    private static final String PREFERENCE_NAME = "geometric_weather_tile";
    private static final String KEY_ENABLE = "enable";

    /** <br> data */

    public static void setEnable(Context context, boolean enable) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ENABLE, enable)
                .apply();

        BackgroundManager.resetNormalBackgroundTask(context, true);
    }

    public static boolean isEnable(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLE, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void refreshTile(Context context, Tile tile) {
        if (tile == null) {
            return;
        }
        Location location = DatabaseHelper.getInstance(context).readLocationList().get(0);
        location.weather = DatabaseHelper.getInstance(context).readWeather(location);
        if (location.weather != null) {
            boolean f = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.key_fahrenheit), false);
            tile.setIcon(
                    WeatherHelper.getMinimalIcon(
                            ResourcesProviderFactory.getNewInstance(),
                            location.weather.realTime.weatherKind,
                            TimeManager.getInstance(context).isDayTime()
                    )
            );
            tile.setLabel(
                    ValueUtils.buildCurrentTemp(
                            location.weather.realTime.temp, false, f
                    )
            );
            tile.updateTile();
        }
    }
}
