package com.vuducminh.nicefoodshipper.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;
import com.vuducminh.nicefoodshipper.R;
import com.vuducminh.nicefoodshipper.model.RestaurantModel;
import com.vuducminh.nicefoodshipper.model.ShipperUserModel;
import com.vuducminh.nicefoodshipper.model.TokenModel;

import java.util.ArrayList;
import java.util.List;

//Lưu trữ các biến, hàm có phạm vi dùng trên toàn ứng dụng
public class Common {

    //Các biến có phạm vi dùng trên toàn ứng dụng
    public static ShipperUserModel currentShipperUser;
    public static RestaurantModel currentRestaurant;

    // chỉnh font SpanString
    public static void setSpanString(String welcome, String name, TextView tv_user) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        tv_user.setText(builder, TextView.BufferType.SPANNABLE);
    }

    // chỉnh font SpanString có màu
    public static void setSpanStringColor(String welcome, String name, TextView textView, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    // <key-value> Trạng thái order
    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0: {
                return "Placed";
            }
            case 1: {
                return "Shipping";
            }
            case 2: {
                return "Shipped";
            }
            case -1: {
                return "Cancelled";
            }
            default:
                return "Unknown";
        }
    }

    // Tạo giao diện thông báo
    // Nội dung thông báo nhận từ Server
    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        String NOTIFICATION_CHANNEL_ID = "minh_vu_nice_food_java";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  //Tạo giao diện
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {                         // Nếu API Android >= 26
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Nice Food Java", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Nice Food Java");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);        //Tạo thông báo

        }

        // tạo giao diện thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_restaurant_menu_black_24dp));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);            //Nạp nội dụng thông báo
        }
        Notification notification = builder.build();
        notificationManager.notify(id, notification);           //hiện thông báo
    }
    // Cập nhận Token
    public static void updateToken(Context context, String newToken, boolean isServer, boolean isShipper) {
        if(Common.currentShipperUser != null) {
            FirebaseDatabase.getInstance()
                    .getReference(CommonAgr.TOKEN_REF)
                    .child(Common.currentShipperUser.getUid())
                    .setValue(new TokenModel(Common.currentShipperUser.getPhone(), newToken, isServer, isShipper))
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Tính góc xoay cho icon Shipper khi đang di chuyển
    // Chức năng Tracking Order( Theo dõi đơn hàng) sử dụng hàm này
    public static float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);       //Vĩ Độ
        double lng = Math.abs(begin.longitude - end.longitude);     // Kinh Độ

          /*Từ điểm xuât phát, điểm đến có 4 trường hợp:
           1: Vĩ độ cao hơn, Kinh độ cao hơn (Góc Trên-Phải)
           2: Vĩ độ thấp hơn, Kình độ cao hơn (Góc Dưới-Phải)
           3: Vĩ độ thấp hơn, Kình độ thấp hơn (Góc Dưới-Trái)
           4: Vĩ độ cao hơn, Kình độ thấp hơn (Góc Trên-Trái)

           Tương ứng với mỗi trường hợp sẽ là 4 cách tính
         */

        if(begin.latitude < end.latitude && begin.longitude < end.longitude) {
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        }
        else if(begin.latitude >= end.latitude && begin.longitude < end.longitude) {
            return (float)((90 - Math.toDegrees(Math.atan(lng/lat)))+90);
        }
        else if(begin.latitude >= end.latitude && begin.longitude >= end.longitude) {
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        }
        else if(begin.latitude < end.latitude && begin.longitude >= end.longitude) {
            return (float)((90 - Math.toDegrees(Math.atan(lng/lat)))+270);
        }
        return -1;

    }

    // Tính và tìm đoạn đường từ shipper đến điểm giao hàng
    // Tham khảo từ: https://stackoverflow.com/questions/15924834/decoding-polyline-with-new-google-maps-api
    // Chức năng Tracking Order( Theo dõi đơn hàng) sử dụng hàm này
    public static List<LatLng> decodePoly(String encode) {
        List poly = new ArrayList();
        int index = 0,len = encode.length();
        int lat = 0, lng = 0;
        while(index < len) {
            int b,shift=0,result=0;
            do{
                b = encode.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lat +=dlat;
            shift = 0;
            result = 0;
            do{
                 b = encode.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while(b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double)lat/1E5)),
                    (((double)lng/1E5)));
            poly.add(p);
        }
        return poly;
    }

    // Chuyển vị trí hiện tại sang dạng String. VD: "41.40338, 2.17403"
    public static String buildLocationString(Location location) {
        return new StringBuilder().append(location.getLatitude()).append(",")
                .append(location.getLongitude()).toString();
    }
}
