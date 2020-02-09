package com.vuducminh.nicefoodshipper.callback;

import com.vuducminh.nicefoodshipper.model.ShippingOrderModel;

import java.util.List;

public interface IShippingOrderCallbackListener {
    void onShippingOrderLoadSuccess(List<ShippingOrderModel> shippingOrderModelList);
    void onShippingOrderLoadfailed(String message);
}
