package com.vuducminh.nicefoodshipper.ui.home;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.common.internal.IResolveAccountCallbacks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefoodshipper.callback.IShippingOrderCallbackListener;
import com.vuducminh.nicefoodshipper.common.Common;
import com.vuducminh.nicefoodshipper.common.CommonAgr;
import com.vuducminh.nicefoodshipper.model.ShippingOrderModel;

import java.util.ArrayList;
import java.util.List;

// Class dùng để nạp dữ liệu vào HomeFrament
public class HomeViewModel extends ViewModel implements IShippingOrderCallbackListener {

    private MutableLiveData<List<ShippingOrderModel>>  mutableLiveDataShippingOrder;              // Dữ liệu các order
    private MutableLiveData<String> messageError;                                                   // Thông báo lỗi

    private IShippingOrderCallbackListener listener;

    public HomeViewModel() {
        mutableLiveDataShippingOrder = new MutableLiveData<>();
       messageError = new MutableLiveData<>();
       listener = this;
    }

    public MutableLiveData<List<ShippingOrderModel>> getMutableLiveDataShippingOrder(String shipperPhone) {

        if(shipperPhone != null && !TextUtils.isEmpty(shipperPhone))
            loadOrderByShipper(shipperPhone);
        return mutableLiveDataShippingOrder;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    // lấy dữ liệu Order mà Shipper cần giao
    private void loadOrderByShipper(String shipperPhone) {
        List<ShippingOrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentRestaurant.getUid())
                .child(CommonAgr.SHIPPING_ORDER_REF)
                .orderByChild("shipperPhone")                    // tìm theo order bằng số diện thoại shipper order đó chứa
                .equalTo(Common.currentShipperUser.getPhone());
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot orderSnapshot: dataSnapshot.getChildren()) {
                    ShippingOrderModel shippingOrderModel = orderSnapshot.getValue(ShippingOrderModel.class);
                    shippingOrderModel.setKey(orderSnapshot.getKey());
                    tempList.add(shippingOrderModel);
                }

                listener.onShippingOrderLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Lấy dữ liệu thành công
    @Override
    public void onShippingOrderLoadSuccess(List<ShippingOrderModel> shippingOrderModelList) {
        mutableLiveDataShippingOrder.setValue(shippingOrderModelList);
    }

    // Lấy dữ liệu thất bại
    @Override
    public void onShippingOrderLoadfailed(String message) {
        messageError.setValue(message);
    }
}