package com.example.manageexpenses.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.manageexpenses.R;
import com.example.manageexpenses.activity.MainActivity;
import android.util.Log;
import com.example.manageexpenses.entity.ReminderTask;
import com.example.manageexpenses.dao.ReminderTaskDao;
import androidx.room.Room;
import java.util.Calendar;
import android.app.AlarmManager;
import android.provider.Settings;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderReceiver", "Received reminder broadcast!");
        int reminderId = intent.getIntExtra("reminderId", 0);
        String title = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");
        Log.d("ReminderReceiver", "reminderId=" + reminderId + ", title=" + title + ", desc=" + desc);
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("openReminderId", reminderId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reminderId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "reminder_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(desc)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH);
        nm.notify(reminderId, builder.build());
        Log.d("ReminderReceiver", "Notification sent for reminderId=" + reminderId);

        ReminderTaskDao dao = Room.databaseBuilder(context, com.example.manageexpenses.AppDatabase.class, "todo_manager.db")
            .allowMainThreadQueries().build().reminderTaskDao();
        ReminderTask reminder = dao.getById(reminderId >= 10 ? reminderId / 10 : reminderId);
        if (reminder != null && reminder.isEnabled) {
            scheduleNextAlarm(context, reminder, reminderId);
        }
    }
    private void scheduleNextAlarm(Context ctx, ReminderTask reminder, int firedId) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (reminder.repeatType == 0) {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.HOUR_OF_DAY, reminder.hour);
            cal.set(Calendar.MINUTE, reminder.minute);
            cal.set(Calendar.SECOND, 0);
            cal.add(Calendar.DATE, 1);
            PendingIntent pi = getReminderPendingIntent(ctx, reminder);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!am.canScheduleExactAlarms()) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                    Log.w("ReminderReceiver", "App does not have SCHEDULE_EXACT_ALARM permission.");
                    return;
                }
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            Log.d("ReminderReceiver", "Reschedule daily: id=" + reminder.id + ", time=" + cal.getTime());
        } else {

            int dayOfWeek = firedId % 10;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.HOUR_OF_DAY, reminder.hour);
            cal.set(Calendar.MINUTE, reminder.minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            PendingIntent pi = getReminderPendingIntent(ctx, reminder, dayOfWeek);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!am.canScheduleExactAlarms()) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                    Log.w("ReminderReceiver", "App does not have SCHEDULE_EXACT_ALARM permission.");
                    return;
                }
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            Log.d("ReminderReceiver", "Reschedule custom: id=" + reminder.id + ", day=" + dayOfWeek + ", time=" + cal.getTime());
        }
    }
    private PendingIntent getReminderPendingIntent(Context ctx, ReminderTask reminder) {
        Intent intent = new Intent(ctx, ReminderReceiver.class);
        intent.putExtra("reminderId", reminder.id);
        intent.putExtra("title", reminder.title);
        intent.putExtra("desc", reminder.description);
        return PendingIntent.getBroadcast(ctx, reminder.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    private PendingIntent getReminderPendingIntent(Context ctx, ReminderTask reminder, int dayOfWeek) {
        Intent intent = new Intent(ctx, ReminderReceiver.class);
        intent.putExtra("reminderId", reminder.id * 10 + dayOfWeek);
        intent.putExtra("title", reminder.title);
        intent.putExtra("desc", reminder.description);
        return PendingIntent.getBroadcast(ctx, reminder.id * 10 + dayOfWeek, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
} 