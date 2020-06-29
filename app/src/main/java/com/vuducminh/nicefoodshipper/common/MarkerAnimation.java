package com.vuducminh.nicefoodshipper.common;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

// Tạo hoạt hình di chuyển của shipper khi shipper di chuyển
public class MarkerAnimation {
    public static void animateMarkerToGB(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterpolator latLngInterpolator)
    {
        LatLng startPosition = marker.getPosition();
        Handler handler = new Handler();
        long start = SystemClock.uptimeMillis();
        Interpolator interpolator = new AccelerateDecelerateInterpolator();    // Khi shipper đến góc cua, xoay hình chậm
        float durationInMs = 3000;        // 3s

        handler.post(new Runnable() {
            long elapsed;
            float t,v;
            @Override
            public void run() {
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v=  interpolator.getInterpolation(t);           //xoay hình

                marker.setPosition(latLngInterpolator.interpolate(v,startPosition,finalPosition));       //hiện thị Shipper

                //Lặp lại cho đến khi tiến trình hoàn tất
                if(t<1) {
                    // hiện thị lại sau 16 giây
                    handler.postDelayed(this,16);
                }
            }
        });
    }

    // Đối với Android 3.0
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void animateMarkerToHC(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterpolator latLngInterpolator){
        LatLng startLocation = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            float v= valueAnimator.getAnimatedFraction();
            LatLng newPosition = latLngInterpolator.interpolate(v,startLocation,finalPosition);
            marker.setPosition(newPosition);
        });
        valueAnimator.setFloatValues(0,1);
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }

    //Đối với Android 4.0
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void animateMarkerToICS(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterpolator latLngInterpolator){
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction,startValue,endValue);
            }
        };
        Property<Marker,LatLng> property = Property.of(Marker.class,LatLng.class,"position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker,property,typeEvaluator,finalPosition);
        animator.setDuration(3000);
        animator.start();
    }
}
