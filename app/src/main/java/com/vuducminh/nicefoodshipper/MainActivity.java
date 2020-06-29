package com.vuducminh.nicefoodshipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vuducminh.nicefoodshipper.common.Common;
import com.vuducminh.nicefoodshipper.common.CommonAgr;
import com.vuducminh.nicefoodshipper.model.RestaurantModel;
import com.vuducminh.nicefoodshipper.model.ShipperUserModel;


import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
// Activity đầu tiên khi mở app
public class MainActivity extends AppCompatActivity {

    private static int API_REQUEST_CODE = 1999;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference shipperRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

//        //Delete data offline;
//        Paper.init(this);
//        Paper.book().delete(CommonAgr.TRIP_START);
//        Paper.book().delete(CommonAgr.SHIPPING_ORDER_DATA);

    }

    private void init() {

        // 2 phương thức đăng nhập: Phone và Email
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuthLocal -> {
            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if(user != null) {
                // Check user từ Firebase
                Paper.init(this);
                String jsonEncode = Paper.book().read(CommonAgr.RESTAURANT_SAVE);          // đọc dữ liệu đã lưu trong máy
                RestaurantModel restaurantModel = new Gson().fromJson(jsonEncode,
                        new TypeToken<RestaurantModel>(){}.getType());

                if(restaurantModel != null)                                                 // nếu shipper đã chọn restaurant
                    checkServerUserFromFirebase(user,restaurantModel);
                else {
                    startActivity(new Intent(MainActivity.this,RestaurantListActivity.class));   // nếu không chưa chọn, sẽ hiện màn hình danh sách
                    finish();
                }

            }
            else {
                phoneLogin();
            }
        };
    }

    // Kiểm tra tài khoản user có trong Firebase
    private void checkServerUserFromFirebase(FirebaseUser user, RestaurantModel restaurantModel) {
        dialog.show();

        shipperRef = FirebaseDatabase.getInstance().getReference(CommonAgr.RESTAURANT_REF)
                .child(restaurantModel.getUid())
                .child(CommonAgr.SHIPPER_REF);
        shipperRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            ShipperUserModel userModel = dataSnapshot.getValue(ShipperUserModel.class);
                            if(userModel.isActive())
                                gotoHomeActivity(userModel,restaurantModel);
                            else {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this,"You must be allowed from Server app",Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    // Mở HomeActivity
    private void gotoHomeActivity(ShipperUserModel userModel,RestaurantModel restaurantModel) {
        dialog.dismiss();
        Common.currentShipperUser = userModel;
        Common.currentRestaurant = restaurantModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }



    // Login bằng thư viện FirebaseUI
    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.logo_shipper)
                .setTheme(R.style.LoginTheme)
                .build(),API_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if(listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();
    }

    // Chờ kết quả login từ Firebase UI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == API_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else {
                Toast.makeText(this,"Failed to sign in",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
