package com.google.sample.cloudvision.alarmFunction;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.google.sample.cloudvision.MainActivity;
import com.google.sample.cloudvision.R;
import com.google.sample.cloudvision.login.LoginActivity;


public class AlarmReceiver extends BroadcastReceiver {

    public AlarmReceiver(){ System.out.println("AlarmReceiver 생성자 동작!"); }

    NotificationManager manager;
    NotificationCompat.Builder builder;

    //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
    private static String CHANNEL_ID = "channel_loc";
    private static String CHANNEL_NAME = "Channel_location";

    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("푸쉬 알람 동작!");

        builder = null;
        manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        //알림창 클릭 시 activity 화면 부름
        Intent intent2 = new Intent(context, LoginActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(context,101,intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context,
                    0, intent2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(context,
                    0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        //알림창 제목
        builder.setContentTitle("유효기간이 7일 남은 기프티콘이 있습니다.. 기프티콘을 사용해 주세요");
        //알림창 아이콘
        builder.setSmallIcon(R.mipmap.kgc);
        //알림창 터치시 자동 삭제
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setContentText("곧 보유하신 기프티콘이 사라집니다.");
        //builder.setPriority(NotificationCompat.PRIORITY_MAX);
        Notification notification = builder.build();
        manager.notify(1,notification);

    }

}