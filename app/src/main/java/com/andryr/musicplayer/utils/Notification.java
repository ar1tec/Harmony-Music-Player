/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.PlaybackService;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.images.ArtworkCache;
import com.andryr.musicplayer.images.BitmapCache;

/**
 * Created by Andry on 27/01/16.
 */
public class Notification {
    private static int NOTIFY_ID = 32;

    public static void updateNotification(final PlaybackService playbackService) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            updateSupportNotification(playbackService);
            return;
        }
        RemoteViews contentViews = new RemoteViews(playbackService.getPackageName(),
                R.layout.notification);
        contentViews.setTextViewText(R.id.song_title, playbackService.getSongTitle());
        contentViews.setTextViewText(R.id.song_artist, playbackService.getArtistName());

        // ArtworkHelper.loadArtworkAsync(this, getAlbumId(), contentViews, R.id.album_artwork);
        PendingIntent togglePlayIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_TOGGLE), 0);
        contentViews.setOnClickPendingIntent(R.id.quick_play_pause_toggle,
                togglePlayIntent);

        PendingIntent nextIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class).setAction(PlaybackService.ACTION_NEXT),
                0);
        contentViews.setOnClickPendingIntent(R.id.quick_next, nextIntent);

        PendingIntent previousIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_PREVIOUS), 0);
        contentViews.setOnClickPendingIntent(R.id.quick_prev, previousIntent);

        PendingIntent stopIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class).setAction(PlaybackService.ACTION_STOP),
                0);
        contentViews.setOnClickPendingIntent(R.id.close, stopIntent);

        if (playbackService.isPlaying()) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_pause);
            } else {
                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_pause_black);
            }
            // contentView.setContentDescription(R.id.play_pause_toggle,
            // getString(R.string.pause));
        } else {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_play_small);
            } else {
                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_play_black);
            }
            // contentView.setContentDescription(R.id.play_pause_toggle,
            // getString(R.string.play));

        }


        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                playbackService);

        Intent intent = new Intent(playbackService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(playbackService, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt)
                .setOngoing(true).setContent(contentViews);

        builder.setSmallIcon(R.drawable.ic_stat_note);

        Resources res = playbackService.getResources();

        final int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        final int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

        ArtworkCache artworkCache = ArtworkCache.getInstance();
        Bitmap b = artworkCache.getCachedBitmap(playbackService.getAlbumId(), width, height);
        if(b != null) {
            builder.setLargeIcon(b);
            playbackService.startForeground(NOTIFY_ID, builder.build());

        }
        else {
            ArtworkCache.getInstance().loadBitmap(playbackService.getAlbumId(), width, height, new BitmapCache.Callback() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap) {
                    setBitmap(playbackService, builder, bitmap);
                    playbackService.startForeground(NOTIFY_ID, builder.build());

                }
            });
        }


    }

    private static void setBitmap(Context context, NotificationCompat.Builder builder, Bitmap bitmap) {
        if (bitmap != null) {

            //bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

            builder.setLargeIcon(bitmap);
        } else {
            BitmapDrawable d = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_stat_note));
            builder.setLargeIcon(d.getBitmap());
        }
    }

    private static void updateSupportNotification(PlaybackService playbackService) {


        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                playbackService);

        Intent intent = new Intent(playbackService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(playbackService, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt)
                .setOngoing(true)
                .setContentTitle(playbackService.getSongTitle())
                .setContentText(playbackService.getArtistName());


        builder.setSmallIcon(R.drawable.ic_stat_note);


        playbackService.startForeground(NOTIFY_ID, builder.build());
    }
}
