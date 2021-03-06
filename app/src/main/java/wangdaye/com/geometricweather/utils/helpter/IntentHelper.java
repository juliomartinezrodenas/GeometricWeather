package wangdaye.com.geometricweather.utils.helpter;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import android.view.View;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.service.LiveWallpaperService;
import wangdaye.com.geometricweather.ui.activity.AboutActivity;
import wangdaye.com.geometricweather.ui.activity.AlertActivity;
import wangdaye.com.geometricweather.settings.activity.SelectProviderActivity;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.ui.activity.ManageActivity;
import wangdaye.com.geometricweather.ui.activity.PreviewIconActivity;
import wangdaye.com.geometricweather.ui.activity.SearcActivity;
import wangdaye.com.geometricweather.settings.activity.SettingsActivity;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Intent helper.
 * */

public class IntentHelper {

    public static void startMainActivity(Context context) {
        Intent intent = new Intent("com.wangdaye.geometricweather.Main")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static Intent buildMainActivityIntent(Context context, @Nullable Location location) {
        String locationName;
        if (location == null) {
            locationName = "";
        } else {
            locationName= location.isLocal() ? context.getString(R.string.local) : location.city;
        }
        return new Intent("com.wangdaye.geometricweather.Main")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION, locationName);
    }

    public static Intent buildMainActivityIntent(@NotNull String cityName) {
        return new Intent("com.wangdaye.geometricweather.Main")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION, cityName);
    }

    public static Intent buildAwakeUpdateActivityIntent() {
        return new Intent("com.wangdaye.geometricweather.UPDATE")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static void startAlertActivity(GeoActivity activity, Weather weather) {
        Intent intent = new Intent(activity, AlertActivity.class);
        intent.putParcelableArrayListExtra(
                AlertActivity.KEY_ALERT_ACTIVITY_ALERT_LIST,
                (ArrayList<? extends Parcelable>) weather.alertList
        );
        activity.startActivity(intent);
    }

    public static void startManageActivityForResult(GeoActivity activity) {
        activity.startActivityForResult(
                new Intent(activity, ManageActivity.class),
                MainActivity.MANAGE_ACTIVITY
        );
    }

    public static void startSearchActivityForResult(GeoActivity geoActivity, View bar) {
        Intent intent = new Intent(geoActivity, SearcActivity.class);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            geoActivity.startActivityForResult(intent, ManageActivity.SEARCH_ACTIVITY);
            geoActivity.overridePendingTransition(R.anim.activity_search_in, 0);
        } else {
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            geoActivity,
                            Pair.create(bar, geoActivity.getString(R.string.transition_activity_search_bar))
                    );
            ActivityCompat.startActivityForResult(
                    geoActivity,
                    intent,
                    ManageActivity.SEARCH_ACTIVITY,
                    options.toBundle()
            );
        }
    }

    public static void startSettingsActivityForResult(GeoActivity activity) {
        activity.startActivityForResult(
                new Intent(activity, SettingsActivity.class),
                MainActivity.SETTINGS_ACTIVITY
        );
    }

    public static void startSelectProviderActivity(Activity activity) {
        activity.startActivity(new Intent(activity, SelectProviderActivity.class));
    }

    public static void startPreviewIconActivity(Activity activity, String packageName) {
        activity.startActivity(
                new Intent(activity, PreviewIconActivity.class).putExtra(
                        PreviewIconActivity.KEY_ICON_PREVIEW_ACTIVITY_PACKAGE_NAME,
                        packageName
                )
        );
    }

    public static void startAboutActivity(GeoActivity activity) {
        activity.startActivity(new Intent(activity, AboutActivity.class));
    }

    public static void startApplicationDetailsActivity(Context context) {
        startApplicationDetailsActivity(context, context.getPackageName());
    }

    public static void startApplicationDetailsActivity(Context context, String pkgName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", pkgName, null));
        context.startActivity(intent);
    }

    public static void startLocationSettingsActivity(Context context) {
        Intent intent =  new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    public static void startLiveWallpaperActivity(Context context) {
        try {
            context.startActivity(
                    new Intent(
                            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
                    ).putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            new ComponentName(context, LiveWallpaperService.class)
                    )
            );
        } catch (ActivityNotFoundException e) {
            SnackbarUtils.showSnackbar(
                    context.getString(R.string.feedback_cannot_start_live_wallpaper_activity)
            );
        }
    }

    public static void startAppStoreDetailsActivity(Context context) {
        startAppStoreDetailsActivity(context, context.getPackageName());
    }

    public static void startAppStoreDetailsActivity(Context context, String packageName) {
        try {
            context.startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + packageName)
                    )
            );
        } catch (Exception e) {
            SnackbarUtils.showSnackbar("Unavailable AppStore.");
        }
    }

    public static void startAppStoreSearchActivity(Context context, String query) {
        try {
            context.startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://search?q=" + query)
                    )
            );
        } catch (Exception e) {
            SnackbarUtils.showSnackbar("Unavailable AppStore.");
        }
    }

    public static void startWebViewActivity(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
