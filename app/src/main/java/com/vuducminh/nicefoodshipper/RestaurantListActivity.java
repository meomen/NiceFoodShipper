package com.vuducminh.nicefoodshipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.vuducminh.nicefoodshipper.adapter.MyRestaurantAdapter;
import com.vuducminh.nicefoodshipper.callback.IRestaurantCallbackListener;
import com.vuducminh.nicefoodshipper.common.Common;
import com.vuducminh.nicefoodshipper.common.CommonAgr;
import com.vuducminh.nicefoodshipper.evenbus.RestaurantSelectEvent;
import com.vuducminh.nicefoodshipper.model.RestaurantModel;
import com.vuducminh.nicefoodshipper.model.ShipperUserModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;


// Màn hình hiện thị danh sách Activity
public class RestaurantListActivity extends AppCompatActivity implements IRestaurantCallbackListener {

    @BindView(R.id.recycler_restaurant)
    RecyclerView recycler_restaurant;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;         // Quản lý chuyển động item
    MyRestaurantAdapter adapter;

    DatabaseReference serverRef;
    IRestaurantCallbackListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        initViews();
        loadAllRestaurant();  // tải dữ liệu các nhà hàng
    }

    // tải dữ liệu các nhà hàng
    private void loadAllRestaurant() {
        dialog.show();
        List<RestaurantModel> restaurantModels = new ArrayList<>();
        DatabaseReference restaurantRef = FirebaseDatabase.getInstance()
                .getReference(CommonAgr.RESTAURANT_REF);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {
                        RestaurantModel restaurantModel = restaurantSnapshot.getValue(RestaurantModel.class);
                        restaurantModel.setUid(restaurantSnapshot.getKey());
                        restaurantModels.add(restaurantModel);
                    }
                    if(restaurantModels.size() > 0) {
                        listener.onRestaurantLoadSuccess(restaurantModels);
                    }
                    else
                        listener.onRestaurantLoadFailed("Restaurant List empty");
                }
                else
                    listener.onRestaurantLoadFailed("Restaurant List not found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onRestaurantLoadFailed(databaseError.getMessage());
            }
        });

    }

    private void initViews() {
        ButterKnife.bind(this);
        listener = this;

        dialog = new AlertDialog.Builder(this).setCancelable(false).setMessage("Please wait...")
                .create();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this,R.anim.layout_slide_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recycler_restaurant.setLayoutManager(layoutManager);
        recycler_restaurant.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
    }

    // nếu tải dữ liệu thành công, nếu được sẽ cho vào list view
    @Override
    public void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList) {
        dialog.dismiss();
        adapter = new MyRestaurantAdapter(this, restaurantModelList);
        recycler_restaurant.setAdapter(adapter);
        recycler_restaurant.setLayoutAnimation(layoutAnimationController);       // cài đặt chuyển động
    }

    @Override
    public void onRestaurantLoadFailed(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // Nếu Restaurant item được chọn
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onRestaurantSelectedEvent(RestaurantSelectEvent event) {
        if(event != null) {
            String jsonEncode = new Gson().toJson(event.getRestaurantModel());
            Paper.init(this);
            Paper.book().write(CommonAgr.RESTAURANT_SAVE,jsonEncode);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                checkServerUserFromFirebase(user,event.getRestaurantModel());
            }
        }
    }

    // Kiểm tra tài khoảng shipper có trong firebase ko?
    private void checkServerUserFromFirebase(FirebaseUser user, RestaurantModel restaurantModel) {
        dialog.show();

        serverRef = FirebaseDatabase.getInstance().getReference(CommonAgr.RESTAURANT_REF)
                .child(restaurantModel.getUid())
                .child(CommonAgr.SHIPPER_REF);
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            ShipperUserModel userModel = dataSnapshot.getValue(ShipperUserModel.class);
                            if(userModel.isActive())
                                gotoHomeActivity(userModel,restaurantModel);
                            else {
                                dialog.dismiss();
                                Toast.makeText(RestaurantListActivity.this,"You must be allowed from Server app",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            dialog.dismiss();
                            showRegisterDialog(user,restaurantModel.getUid());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    // chuyển đến màn hình HomeActivity
    private void gotoHomeActivity(ShipperUserModel userModel, RestaurantModel restaurantModel) {
        dialog.dismiss();

        String jsonEncode = new Gson().toJson(restaurantModel);
        Paper.init(this);
        Paper.book().write(CommonAgr.RESTAURANT_SAVE,jsonEncode);

        Common.currentShipperUser = userModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }

    // Hiện thị dialog thông tin đăng ký tài khoản
    private void showRegisterDialog(FirebaseUser user, String uid) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information \n " +
                "Admin will accept your account late");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        TextInputLayout phone_input_layout = (TextInputLayout)itemView.findViewById(R.id.phone_input_layout);
        EditText edt_name = (EditText)itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText)itemView.findViewById(R.id.edt_phone);

        //Set Data
        if(user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber())) {
            phone_input_layout.setHint("Email");
            edt_phone.setText(user.getEmail());
            edt_name.setText(user.getDisplayName());
        }
        else {
            phone_input_layout.setHint("Phone");
            edt_phone.setText(user.getPhoneNumber());
        }
        builder.setNegativeButton("CANCLE", (dialogInterface, which) -> {
            dialogInterface.dismiss();

        }).setPositiveButton("OK", (dialogInterface, which) -> {
            if(TextUtils.isEmpty(edt_name.getText().toString())) {
                Toast.makeText(RestaurantListActivity.this,"Please enter your name",Toast.LENGTH_SHORT).show();
                return;
            }

            ShipperUserModel shipperUserModel = new ShipperUserModel();
            shipperUserModel.setUid(user.getUid());
            shipperUserModel.setName(edt_name.getText().toString());
            shipperUserModel.setPhone(user.getPhoneNumber());
            shipperUserModel.setActive(false);

            dialog.show();
            serverRef = FirebaseDatabase.getInstance().getReference(CommonAgr.RESTAURANT_REF)
                    .child(uid)
                    .child(CommonAgr.SHIPPER_REF);
            serverRef.child(shipperUserModel.getUid())
                    .setValue(shipperUserModel)
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(RestaurantListActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {
                        dialog.dismiss();

                        // Chờ Quản trị viên cho phép hoạt động
                        Toast.makeText(RestaurantListActivity.this,"Congratulation ! Register success ! Admin will check and active you soon",Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
        registerDialog.show();
    }
}