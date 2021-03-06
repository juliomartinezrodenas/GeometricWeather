package wangdaye.com.geometricweather.remoteviews.presenter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetClockDayVerticalProvider;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Widget clock day vertical utils.
 */

public class ClockDayVerticalWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void refreshWidgetView(Context context, Location location, @Nullable Weather weather) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_clock_day_vertical_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location, weather,
                config.viewStyle, config.cardStyle, config.cardAlpha, config.textColor,
                config.hideSubtitle, config.subtitleData, config.clockFont
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetClockDayVerticalProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context, Location location, @Nullable Weather weather,
                                             String viewStyle, String cardStyle, int cardAlpha, String textColor,
                                             boolean hideSubtitle, String subtitleData, String clockFont) {
        boolean dayTime = TimeManager.isDaylight(weather);

        SharedPreferences defaultSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false
        );
        boolean minimalIcon = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_widget_minimal_icon),
                false
        );
        boolean touchToRefresh = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh),
                false
        );

        WidgetColor color = new WidgetColor(context, dayTime, cardStyle, textColor);

        int textColorInt;
        if (color.darkText) {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        RemoteViews views = buildWidgetViewDayPart(
                context, weather,
                dayTime, textColorInt, fahrenheit,
                minimalIcon, color.darkText,
                clockFont, viewStyle,
                hideSubtitle, subtitleData
        );
        if (weather == null) {
            return views;
        }

        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_clock_day_card,
                    getCardBackgroundId(context, color.darkCard, cardAlpha)
            );
            views.setViewVisibility(R.id.widget_clock_day_card, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_clock_day_card, View.GONE);
        }

        setOnClickPendingIntent(context, views, location, subtitleData, touchToRefresh);

        return views;
    }

    private static RemoteViews buildWidgetViewDayPart(Context context, @Nullable Weather weather,
                                                      boolean dayTime, int textColor, boolean fahrenheit,
                                                      boolean minimalIcon, boolean blackText,
                                                      String clockFont, String viewStyle,
                                                      boolean hideSubtitle, String subtitleData) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_symmetry);
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_rectangle);
                break;

            case "symmetry":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_symmetry);
                break;

            case "tile":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_tile);
                break;

            case "mini":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_mini);
                break;

            case "vertical":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_vertical);
                break;
        }
        if (weather == null) {
            return views;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        views.setImageViewUri(
                R.id.widget_clock_day_icon,
                WeatherHelper.getWidgetNotificationIconUri(
                        provider,
                        weather.realTime.weatherKind,
                        dayTime,
                        minimalIcon,
                        blackText
                )
        );
        views.setTextViewText(
                R.id.widget_clock_day_title,
                getTitleText(weather, viewStyle, fahrenheit)
        );
        views.setTextViewText(
                R.id.widget_clock_day_subtitle,
                getSubtitleText(weather, viewStyle, fahrenheit)
        );
        views.setTextViewText(
                R.id.widget_clock_day_time,
                getTimeText(context, weather, viewStyle, subtitleData)
        );

        views.setTextColor(R.id.widget_clock_day_clock_light, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_normal, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_black, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_aa_light, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_aa_normal, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_aa_black, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_1_light, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_1_normal, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_1_black, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_2_light, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_2_normal, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_2_black, textColor);
        views.setTextColor(R.id.widget_clock_day_title, textColor);
        views.setTextColor(R.id.widget_clock_day_subtitle, textColor);
        views.setTextColor(R.id.widget_clock_day_time, textColor);

        views.setViewVisibility(R.id.widget_clock_day_time, hideSubtitle ? View.GONE : View.VISIBLE);

        if (clockFont == null) {
            clockFont = "light";
        }
        switch (clockFont) {
            case "light":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE);
                break;

            case "normal":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE);
                break;

            case "black":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE);
                break;

            case "analog":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                views.setViewVisibility(
                        R.id.widget_clock_day_clock_analogContainer_light,
                        blackText ? View.GONE : View.VISIBLE
                );
                views.setViewVisibility(
                        R.id.widget_clock_day_clock_analogContainer_dark,
                        blackText ? View.VISIBLE : View.GONE
                );
                break;
        }

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(
                        new ComponentName(context, WidgetClockDayVerticalProvider.class)
                );
        return widgetIds != null && widgetIds.length > 0;
    }

    private static String getTitleText(Weather weather, String viewStyle, boolean fahrenheit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit)[0];

            case "symmetry":
                return weather.base.city
                        + "\n"
                        + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, fahrenheit);

            case "tile":
                return weather.realTime.weather
                        + " "
                        + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);

            case "mini":
                return weather.realTime.weather;

            case "vertical":
                return weather.realTime.weather
                        + " "
                        + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);
        }
        return "";
    }

    private static String getSubtitleText(Weather weather, String viewStyle, boolean fahrenheit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit)[1];

            case "symmetry":
                return weather.realTime.weather
                        + "\n"
                        + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);

            case "tile":
                return ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);

            case "mini":
                return ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);
        }
        return "";
    }

    private static String getTimeText(Context context, Weather weather, String viewStyle, String subtitleData) {
        switch (subtitleData) {
            case "time":
                switch (viewStyle) {
                    case "rectangle":
                        return weather.base.city + " " + weather.base.time;

                    case "symmetry":
                        return WidgetUtils.getWeek(context) + " " + weather.base.time;

                    case "tile":
                    case "vertical":
                        return weather.base.city
                                + " " + WidgetUtils.getWeek(context)
                                + " " + weather.base.time;
                }
                break;

            case "aqi":
                if (weather.aqi != null) {
                    return weather.aqi.quality + " (" + weather.aqi.aqi + ")";
                }
                break;

            case "wind":
                return weather.realTime.windLevel
                        + " (" + weather.realTime.windDir + weather.realTime.windSpeed + ")";

            case "lunar":
                switch (viewStyle) {
                    case "rectangle":
                        return weather.base.city
                                + " "
                                + LunarHelper.getLunarDate(Calendar.getInstance());

                    case "symmetry":
                        return WidgetUtils.getWeek(context)
                                + " "
                                + LunarHelper.getLunarDate(Calendar.getInstance());

                    case "tile":
                    case "vertical":
                        return weather.base.city
                                + " " + WidgetUtils.getWeek(context)
                                + " " + LunarHelper.getLunarDate(Calendar.getInstance());
                }
                break;

            default:
                return context.getString(R.string.feels_like) + " "
                        + ValueUtils.buildAbbreviatedCurrentTemp(
                                weather.realTime.sensibleTemp,
                                SettingsOptionManager.getInstance(context).isFahrenheit()
                        );
        }
        return "";
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                String subtitleData, boolean touchToRefresh) {
        // weather.
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_weather,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_REFRESH
                    )
            );
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_weather,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER
                    )
            );
        }

        // clock.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_light,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_normal,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_black,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_BLACK
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_light,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_normal,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_black,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_BLACK
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_light,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_normal,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_black,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_BLACK
                )
        );

        // time.
        if (subtitleData.equals("lunar")) {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_time,
                    getCalendarPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR
                    )
            );
        } else if (!touchToRefresh && subtitleData.equals("time")) {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_time,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_PENDING_INTENT_CODE_REFRESH
                    )
            );
        }
    }
}
