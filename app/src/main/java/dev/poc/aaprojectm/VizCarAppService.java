package dev.poc.aaprojectm;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.AppManager;
import androidx.car.app.CarAppService;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.hardware.CarHardwareManager;
import androidx.car.app.hardware.common.CarValue;
import androidx.car.app.hardware.common.OnCarDataAvailableListener;
import androidx.car.app.hardware.info.CarInfo;
import androidx.car.app.hardware.info.Speed;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.Template;
import androidx.car.app.navigation.model.NavigationTemplate;
import androidx.car.app.validation.HostValidator;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public final class VizCarAppService extends CarAppService {
    @NonNull @Override public HostValidator createHostValidator() {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR; // sideload POC only, never ship this
    }

    @NonNull @Override public Session onCreateSession() {
        return new Session() {
            @NonNull @Override public Screen onCreateScreen(@NonNull Intent intent) {
                return new VizScreen(getCarContext());
            }
        };
    }

    static final class VizScreen extends Screen implements DefaultLifecycleObserver {
        private final VizRenderer renderer;
        private CarInfo carInfo;
        private final OnCarDataAvailableListener<Speed> speedListener = this::onSpeed;

        private void onSpeed(Speed s) {
            CarValue<Float> v = s.getRawSpeedMetersPerSecond();
            // Safety guard: blank the visuals whenever the vehicle reports motion.
            renderer.paused = v.getStatus() == CarValue.STATUS_SUCCESS
                    && v.getValue() != null && Math.abs(v.getValue()) > 0.5f;
        }

        VizScreen(CarContext ctx) {
            super(ctx);
            renderer = new VizRenderer(ctx.getAssets());
            ctx.getCarService(AppManager.class).setSurfaceCallback(renderer);
            getLifecycle().addObserver(this);
        }

        @Override public void onCreate(@NonNull LifecycleOwner o) {
            try {
                carInfo = getCarContext().getCarService(CarHardwareManager.class).getCarInfo();
                carInfo.addSpeedListener(ContextCompat.getMainExecutor(getCarContext()), speedListener);
            } catch (SecurityException | IllegalStateException e) {
                // ponytail: fails open (keeps rendering) when speed is unavailable, e.g. DHU or CAR_SPEED denied.
                // Fail closed here before any demo in a car that can move.
                carInfo = null;
                Log.w("viz", "speed guard unavailable", e);
            }
            if (getCarContext().checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                CarToast.makeText(getCarContext(), "Open AA projectM on the phone to allow audio", CarToast.LENGTH_LONG).show();
            }
            VisualizerSource.start(getCarContext());
        }

        @Override public void onDestroy(@NonNull LifecycleOwner o) {
            if (carInfo != null) carInfo.removeSpeedListener(speedListener);
            renderer.stop();
            VisualizerSource.stop(getCarContext());
        }

        @NonNull @Override public Template onGetTemplate() {
            ActionStrip strip = new ActionStrip.Builder()
                    .addAction(new Action.Builder().setTitle("Next").setOnClickListener(renderer::next).build())
                    .build();
            return new NavigationTemplate.Builder().setActionStrip(strip).build();
        }
    }
}
