package wangdaye.com.geometricweather.background.service;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.service.wallpaper.WallpaperService;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.preference.PreferenceManager;

import android.view.SurfaceHolder;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.RenderRunnable;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.DelayRotateController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.WeatherImplementorFactory;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

public class LiveWallpaperService extends WallpaperService {

    private static final int STEP_DISPLAY = 1;
    private static final int STEP_DISMISS = -1;

    @IntDef({STEP_DISPLAY, STEP_DISMISS})
    private @interface StepRule {}

    private static final int SWITCH_ANIMATION_DURATION = 150;
    protected static long DRAW_INTERVAL = 8;

    private class WeatherEngine extends Engine {

        private SurfaceHolder holder;
        @Nullable private DrawableRunnable drawableRunnable;

        @Nullable private MaterialWeatherView.WeatherAnimationImplementor implementor;
        @Nullable private MaterialWeatherView.RotateController[] rotators;

        private boolean openGravitySensor;
        @Nullable private SensorManager sensorManager;
        @Nullable private Sensor gravitySensor;

        @Size(2) int[] sizes;
        private float rotation2D;
        private float rotation3D;

        @WeatherView.WeatherKindRule private int weatherKind;
        private boolean daytime;

        private float displayRate;

        @StepRule
        private int step;
        private boolean visible;

        private class DrawableRunnable extends RenderRunnable {

            @Nullable private Canvas canvas;

            DrawableRunnable(long interval) {
                super(interval);
            }

            @Override
            protected void onRender(long interval) {
                if (implementor != null && rotators != null) {
                    rotators[0].updateRotation(rotation2D, interval);
                    rotators[1].updateRotation(rotation3D, interval);

                    implementor.updateData(
                            sizes, interval,
                            (float) rotators[0].getRotate(), (float) rotators[1].getRotate()
                    );

                    displayRate = displayRate
                            + (step == STEP_DISPLAY ? 1f : -1f) * interval / SWITCH_ANIMATION_DURATION;
                    displayRate = Math.max(0, displayRate);
                    displayRate = Math.min(1, displayRate);
                    if (displayRate == 0) {
                        setWeatherImplementor();
                    }

                    try {
                        canvas = holder.lockCanvas();
                        if (canvas != null) {
                            sizes[0] = canvas.getWidth();
                            sizes[1] = canvas.getHeight();
                            implementor.draw(
                                    sizes, canvas,
                                    displayRate, 0,
                                    (float) rotators[0].getRotate(), (float) rotators[1].getRotate()
                            );
                            holder.unlockCanvasAndPost(canvas);
                        }
                    } catch (Exception ignored) {
                        // do nothing.
                    }
                }
            }
        }

        private SensorEventListener gravityListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent ev) {
                // x : (+) fall to the left / (-) fall to the right.
                // y : (+) stand / (-) head stand.
                // z : (+) look down / (-) look up.
                // rotation2D : (+) anticlockwise / (-) clockwise.
                // rotation3D : (+) look down / (-) look up.
                if (openGravitySensor) {
                    float aX = ev.values[0];
                    float aY = ev.values[1];
                    float aZ = ev.values[2];
                    double g2D = Math.sqrt(aX * aX + aY * aY);
                    double g3D = Math.sqrt(aX * aX + aY * aY + aZ * aZ);
                    double cos2D = Math.max(Math.min(1, aY / g2D), -1);
                    double cos3D = Math.max(Math.min(1, g2D * (aY >= 0 ? 1 : -1) / g3D), -1);
                    rotation2D = (float) Math.toDegrees(Math.acos(cos2D)) * (aX >= 0 ? 1 : -1);
                    rotation3D = (float) Math.toDegrees(Math.acos(cos3D)) * (aZ >= 0 ? 1 : -1);

                    if (60 < Math.abs(rotation3D) && Math.abs(rotation3D) < 120) {
                        rotation2D *= Math.abs(Math.abs(rotation3D) - 90) / 30.0;
                    }
                } else {
                    rotation2D = 0;
                    rotation3D = 0;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                // do nothing.
            }
        };

        private void setWeather(@WeatherView.WeatherKindRule int weatherKind, boolean daytime) {
            if (this.weatherKind == weatherKind
                    && (isIgnoreDayNight(weatherKind) || this.daytime == daytime)) {
                return;
            }

            this.weatherKind = weatherKind;
            this.daytime = daytime;

            if (drawableRunnable != null && drawableRunnable.isRunning()) {
                // Set step to dismiss. The implementor will execute exit animation and call weather
                // view to reset it.
                step = STEP_DISMISS;
            }
        }

        private boolean isIgnoreDayNight(@WeatherView.WeatherKindRule int weatherKind) {
            return weatherKind == WeatherView.WEATHER_KIND_CLOUDY
                    || weatherKind == WeatherView.WEATHER_KIND_FOG
                    || weatherKind == WeatherView.WEATHER_KIND_HAZE
                    || weatherKind == WeatherView.WEATHER_KIND_THUNDERSTORM
                    || weatherKind == WeatherView.WEATHER_KIND_THUNDER
                    || weatherKind == WeatherView.WEATHER_KIND_WIND;
        }

        private void setWeatherImplementor() {
            step = STEP_DISPLAY;
            implementor = WeatherImplementorFactory.getWeatherImplementor(weatherKind, daytime, sizes);
            rotators = new MaterialWeatherView.RotateController[] {
                    new DelayRotateController(rotation2D),
                    new DelayRotateController(rotation3D)
            };
        }

        private void setOpenGravitySensor(boolean openGravitySensor) {
            this.openGravitySensor = openGravitySensor;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            this.sizes = new int[] {0, 0};

            this.holder = surfaceHolder;
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    sizes[0] = width;
                    sizes[1] = height;
                    setWeatherImplementor();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
            holder.setFormat(PixelFormat.RGBA_8888);

            this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                this.openGravitySensor = true;
                this.gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            }

            this.step = STEP_DISPLAY;
            this.visible = false;
            setWeather(WeatherView.WEATHER_KING_NULL, true);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (this.visible != visible) {
                this.visible = visible;
                if (visible) {
                    this.rotation2D = 0;
                    this.rotation3D = 0;
                    if (sensorManager != null) {
                        sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
                    }

                    Location location = DatabaseHelper.getInstance(LiveWallpaperService.this)
                            .readLocationList()
                            .get(0);
                    location.weather = DatabaseHelper.getInstance(LiveWallpaperService.this)
                            .readWeather(location);
                    if (location.weather != null) {
                        setWeather(
                                WeatherViewController.getWeatherViewWeatherKind(
                                        location.weather.realTime.weatherKind
                                ), TimeManager.isDaylight(location.weather)
                        );
                    }
                    setWeatherImplementor();
                    setOpenGravitySensor(
                            PreferenceManager.getDefaultSharedPreferences(
                                    LiveWallpaperService.this
                            ).getBoolean(getString(R.string.key_gravity_sensor_switch), true)
                    );

                    if (drawableRunnable == null || !drawableRunnable.isRunning()) {
                        drawableRunnable = new DrawableRunnable(DRAW_INTERVAL);
                        new Thread(drawableRunnable).start();
                    }
                } else {
                    if (sensorManager != null) {
                        sensorManager.unregisterListener(gravityListener, gravitySensor);
                    }
                    if (drawableRunnable != null) {
                        drawableRunnable.setRunning(false);
                        drawableRunnable = null;
                    }
                }
            }
        }

        @Override
        public void onDestroy() {
            onVisibilityChanged(false);
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new WeatherEngine();
    }
}
