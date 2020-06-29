package com.vuducminh.nicefoodshipper.services;


import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vuducminh.nicefoodshipper.common.Common;
import com.vuducminh.nicefoodshipper.common.CommonAgr;

import java.util.Map;
import java.util.Random;


// Để có thể gửi thông báo cho app Server, dùng Firebase Clound Message
public class MyFCMServices extends FirebaseMessagingService {


    // Thông báo được nhận
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> dataRecv = remoteMessage.getData();
        if(dataRecv != null) {
            Common.showNotification(this, new Random().nextInt(),       // Hiện thị
                    dataRecv.get(CommonAgr.NOTI_TITLE),
                    dataRecv.get(CommonAgr.NOTI_CONTENT),
                    null);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this,s,false,true);
    }
}

