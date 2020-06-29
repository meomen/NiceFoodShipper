package com.vuducminh.nicefoodshipper.callback;

import com.vuducminh.nicefoodshipper.model.ShippingOrderModel;

import java.util.List;

//lắng nghe sự kiện item đơn hàng đang giao được chọn
public interface IShippingOrderCallbackListener {
    void onShippingOrderLoadSuccess(List<ShippingOrderModel> shippingOrderModelList);
    void onShippingOrderLoadfailed(String message);
}
