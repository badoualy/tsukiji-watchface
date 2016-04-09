package com.badoualy.tsukiji.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class KanjiWatchFaceService extends CanvasWatchFaceService {

    private static final int MSG_UPDATE_TIME = 0;

    // Update rate in interactive mode
    private static final int INTERACTIVE_UPDATE_RATE_MS = (int) TimeUnit.SECONDS.toMillis(55);

    // Delay before updating kanji is allowed
    private final long DELAY_UPDATE_KANJI = TimeUnit.SECONDS.toMillis(30);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        // device features
        boolean lowBitAmbient;

        // state
        Calendar calendar;
        boolean registeredTimeZoneReceiver;

        // graphic objects
        //Bitmap mBackgroundBitmap;
        //Bitmap backgroundScaledBitmap;
        Paint timePaint;
        Paint kanjiPaint;
        Paint translationPaint;

        // content
        Kanji kanji = Kanji.DEFAULT_KANJI;
        boolean showingTranslation = false;
        boolean showingReading = false;
        long lastChange = 0;

        // util
        Rect timeBounds = new Rect(), kanjiBounds = new Rect(), translationBounds = new Rect();

        // handler to update the time once a second in interactive mode
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        // receiver to update the time zone
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                calendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            calendar = Calendar.getInstance();

            final Resources resources = getResources();

            // configure the system UI
            setWatchFaceStyle(new WatchFaceStyle.Builder(KanjiWatchFaceService.this)
                                      .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                                      .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_PERSISTENT)
                                      .setShowSystemUiTime(false)
                                      .setAcceptsTapEvents(true)
                                      .build());

            // create graphic styles
            timePaint = new Paint();
            timePaint.setTextSize(resources.getDimensionPixelSize(R.dimen.time_size));
            timePaint.setColor(Color.WHITE);
            timePaint.setStrokeWidth(5.0f);
            timePaint.setAntiAlias(true);
            timePaint.setStrokeCap(Paint.Cap.ROUND);

            kanjiPaint = new Paint();
            kanjiPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.kanji_size));
            kanjiPaint.setColor(Color.WHITE);
            kanjiPaint.setStrokeWidth(5.0f);
            kanjiPaint.setAntiAlias(true);
            kanjiPaint.setStrokeCap(Paint.Cap.ROUND);

            translationPaint = new Paint();
            translationPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.translation_size));
            translationPaint.setColor(Color.WHITE);
            translationPaint.setStrokeWidth(5.0f);
            translationPaint.setAntiAlias(true);
            translationPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            if (System.currentTimeMillis() - lastChange >= DELAY_UPDATE_KANJI) {
                kanji = KanjiUtils.getRandomKanji(KanjiWatchFaceService.this);
                showingReading = false;
                showingTranslation = false;
                lastChange = System.currentTimeMillis();
            }
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            /* the wearable switched between modes */
//            if (lowBitAmbient) {
//                boolean antiAlias = !inAmbientMode;
//                timePaint.setAntiAlias(antiAlias);
//            }
            invalidate();
            //updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            calendar.setTimeInMillis(System.currentTimeMillis());

            int width = bounds.width();
            int height = bounds.height();
            float centerX = width / 2f;
            float centerY = height / 2f;

            // Get time
            int minutes = calendar.get(Calendar.MINUTE);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);

            // Draw
            //canvas.drawBitmap(backgroundScaledBitmap, 0, 0, null);
            canvas.drawColor(Color.BLACK);

            // Time
            //final String time = KanjiUtils.convertNumber(hours) + ":" + KanjiUtils.convertNumber(minutes);
            final String time = hours + ":" + String.format("%02d", minutes);
            timePaint.getTextBounds(time, 0, time.length(), timeBounds);
            int y = 50 + timeBounds.height();
            canvas.drawText(time, centerX - timeBounds.width() / 2, y, timePaint);

            // Kanji
            kanjiPaint.getTextBounds(kanji.kanji, 0, kanji.kanji.length(), kanjiBounds);
            y += kanjiBounds.height() + 20;
            canvas.drawText(kanji.kanji, centerX - kanjiBounds.width() / 2, y, kanjiPaint);

            if (showingReading) {
                // Translation
                translationPaint.getTextBounds(kanji.translation, 0, kanji.translation.length(), translationBounds);
                y += translationBounds.height() + 15;
                if (showingTranslation)
                    canvas.drawText(kanji.translation, centerX - translationBounds.width() / 2, y, translationPaint);

                // Reading
                y += translationBounds.height() + 15;
                final String[] kunYomiReadings = kanji.kunYomi.split(",");
                final String[] onYomiReadings = kanji.onYomi.split(",");
                for (int i = 0; i < Math.max(kunYomiReadings.length, onYomiReadings.length); i++) {
                    if (i < onYomiReadings.length)
                        canvas.drawText(onYomiReadings[i], 90, y, translationPaint);
                    if (i < kunYomiReadings.length)
                        canvas.drawText(kunYomiReadings[i], 170, y, translationPaint);
                    y += translationPaint.descent() - translationPaint.ascent();
                }
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            if (backgroundScaledBitmap == null
//                    || backgroundScaledBitmap.getWidth() != width
//                    || backgroundScaledBitmap.getHeight() != height) {
//                backgroundScaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            }
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                calendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible and
            // whether we're in ambient mode, so we may need to start or stop the timer
            //updateTimer();
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning())
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private void registerReceiver() {
            if (registeredTimeZoneReceiver)
                return;
            registeredTimeZoneReceiver = true;
            KanjiWatchFaceService.this.registerReceiver(mTimeZoneReceiver, new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
        }

        private void unregisterReceiver() {
            if (!registeredTimeZoneReceiver)
                return;
            registeredTimeZoneReceiver = false;
            KanjiWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            if (tapType == TAP_TYPE_TAP) {
                if (showingTranslation) {
                    // New kanji
                    kanji = KanjiUtils.getRandomKanji(KanjiWatchFaceService.this);
                    showingReading = false;
                    showingTranslation = false;
                    lastChange = System.currentTimeMillis();
                } else if (showingReading) {
                    showingTranslation = true;
                } else {
                    showingReading = true;
                }
                invalidate();
            }
        }
    }

}
