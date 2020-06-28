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

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        firebaseAuth = FirebaseAuth.getInstance();
//        shipperRef = FirebaseDatabase.getInstance().getReference(CommonAgr.SHIPPER_REF);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuthLocal -> {
            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if(user != null) {
                // Check user from Firebase
                Paper.init(this);
                String jsonEncode = Paper.book().read(CommonAgr.RESTAURANT_SAVE);
                RestaurantModel restaurantModel = new Gson().fromJson(jsonEncode,
                        new TypeToken<RestaurantModel>(){}.getType());

                if(restaurantModel != null)
                    checkServerUserFromFirebase(user,restaurantModel);
                else {
                    startActivity(new Intent(MainActivity.this,RestaurantListActivity.class));
                    finish();
                }

            }
            else {
                phoneLogin();
            }
        };
    }

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

    private void gotoHomeActivity(ShipperUserModel userModel,RestaurantModel restaurantModel) {
        dialog.dismiss();
        Common.currentShipperUser = userModel;
        Common.currentRestaurant = restaurantModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }


//    private void checkUserServerFromFirebase(FirebaseUser user) {
//        dialog.show();
//        shipperRef.child(user.getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.exists()) {
//                            ShipperUserModel userModel = dataSnapshot.getValue(ShipperUserModel.class);
//                            if(userModel.isActive()) {
//                                gotoHomeActivity(userModel);
//                            }
//                            else {
//                                dialog.dismiss();
//                                Toast.makeText(MainActivity.this,"You must be allowed from Admin to access this app",Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        else{
//                            // Usernot exists in database
//                            dialog.dismiss();
//                            showRegisterDialog(user);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        Toast.makeText(MainActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//    private void showRegisterDialog(FirebaseUser user) {
//        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
//        builder.setTitle("Register");
//        builder.setMessage("Please fill information \n " +
//                "Admin will accept your account late");
//
//        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
//        TextInputLayout phone_input_layout = (TextInputLayout)itemView.findViewById(R.id.phone_input_layout);
//        EditText edt_name = (EditText)itemView.findViewById(R.id.edt_name);
//        EditText edt_phone = (EditText)itemView.findViewById(R.id.edt_phone);
//
//        //Set Data
//        if(user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber())) {
//            phone_input_layout.setHint("Email");
//            edt_phone.setText(user.getEmail());
//            edt_name.setText(user.getDisplayName());
//        }
//        else {
//            phone_input_layout.setHint("Phone");
//            edt_phone.setText(user.getPhoneNumber());
//        }
//        builder.setNegativeButton("CANCLE", (dialogInterface, which) -> {
//            dialogInterface.dismiss();
//
//        }).setPositiveButton("OK", (dialogInterface, which) -> {
//            if(TextUtils.isEmpty(edt_name.getText().toString())) {
//                Toast.makeText(MainActivity.this,"Please enter your name",Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            ShipperUserModel shipperUserModel = new ShipperUserModel();
//            shipperUserModel.setUid(user.getUid());
//            shipperUserModel.setName(edt_name.getText().toString());
//            shipperUserModel.setPhone(user.getPhoneNumber());
//            shipperUserModel.setActive(false);
//
//            dialog.show();
//
//            shipperRef.child(shipperUserModel.getUid())
//                    .setValue(shipperUserModel)
//                    .addOnFailureListener(e -> {
//                        dialog.dismiss();
//                        Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnCompleteListener(task -> {
//                        dialog.dismiss();
//                        Toast.makeText(MainActivity.this,"Congratulation ! Register success ! Admin will check and active you soon",Toast.LENGTH_SHORT).show();
////                        gotoHomeActivity(serverUserModel);
//                    });
//        });
//
//        builder.setView(itemView);
//        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
//        registerDialog.show();
//    }

//    private void gotoHomeActivity(ShipperUserModel shipperUserModel) {
//
//        dialog.dismiss();
//        Common.currentShipperUser = shipperUserModel;
//        startActivity(new Intent(this,HomeActivity.class));
//        finish();
//    }


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
