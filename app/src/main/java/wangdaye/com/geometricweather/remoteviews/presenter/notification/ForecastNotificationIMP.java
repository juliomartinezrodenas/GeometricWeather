package wangdaye.com.geometricweather.remoteviews.presenter.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.presenter.AbstractRemoteViewsPresenter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Forecast notification utils.
 * */

public class ForecastNotificationIMP extends AbstractRemoteViewsPresenter {

    public static void buildForecastAndSendIt(Context context, Weather weather, boolean today) {
        if (weather == null) {
            return;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        LanguageUtils.setLanguage(
                context,
                SettingsOptionManager.getInstance(context).getLanguage()
        );

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = sharedPreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false);

        // create channel.
        NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                    new NotificationChannel(
                            GeometricWeather.NOTIFICATION_CHANNEL_ID_FORECAST,
                            GeometricWeather.getNotificationChannelName(
                                    context,
                                    GeometricWeather.NOTIFICATION_CHANNEL_ID_FORECAST
                            ), NotificationManager.IMPORTANCE_DEFAULT
                    )
            );
        }

        // get builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, GeometricWeather.NOTIFICATION_CHANNEL_ID_FORECAST);

        // set notification level.
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        // set notification visibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }

        String weatherKind;
        boolean daytime;
        if (today) {
            daytime = TimeManager.isDaylight(weather);
            weatherKind = weather.dailyList.get(0).weatherKinds[daytime ? 0 : 1];
        } else {
            daytime = true;
            weatherKind = weather.dailyList.get(1).weatherKinds[0];
        }

        // set small icon.
        builder.setSmallIcon(
                WeatherHelper.getDefaultMinimalXmlIconId(weatherKind, daytime));

        // large icon.
        builder.setLargeIcon(
                drawableToBitmap(
                        WeatherHelper.getWeatherIcon(provider, weatherKind, daytime)
                )
        );

        // sub text.
        if (today) {
            builder.setSubText(context.getString(R.string.today));
        } else {
            builder.setSubText(context.getString(R.string.tomorrow));
        }

        // title and content.
        if (today) {
            builder.setContentTitle(
                    context.getString(R.string.day)
                            + " " + weather.dailyList.get(0).weathers[0]
                            + " " + ValueUtils.buildCurrentTemp(
                                    weather.dailyList.get(0).temps[0], false, fahrenheit
                            )
            ).setContentText(
                    context.getString(R.string.night)
                            + " " + weather.dailyList.get(0).weathers[1]
                            + " " + ValueUtils.buildCurrentTemp(
                                    weather.dailyList.get(0).temps[1], false, fahrenheit
                            )
            );
        } else {
            builder.setContentTitle(
                    context.getString(R.string.day)
                            + " " + weather.dailyList.get(1).weathers[0]
                            + " " + ValueUtils.buildCurrentTemp(
                                    weather.dailyList.get(1).temps[0], false, fahrenheit
                            )
            ).setContentText(
                    context.getString(R.string.night)
                            + " " + weather.dailyList.get(1).weathers[1]
                            + " " + ValueUtils.buildCurrentTemp(
                                    weather.dailyList.get(1).temps[1], false, fahrenheit
                            )
            );
        }

        // set intent.
        builder.setContentIntent(getWeatherPendingIntent(context, null, 0));

        // set sound & vibrate.
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(true);

        // set badge.
        builder.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                notification.getClass()
                        .getMethod("setSmallIcon", Icon.class)
                        .invoke(
                                notification,
                                WeatherHelper.getMinimalIcon(
                                        provider, weather.realTime.weatherKind, daytime)
                        );
            } catch (Exception ignore) {
                // do nothing.
            }
        }

        // commit.
        manager.notify(
                today
                        ? GeometricWeather.NOTIFICATION_ID_TODAY_FORECAST
                        : GeometricWeather.NOTIFICATION_ID_TOMORROW_FORECAST,
                notification
        );
    }

    public static boolean isEnable(Context context, boolean today) {
        if (today) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.key_forecast_today), false);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.key_forecast_tomorrow), false);
        }
    }
}
