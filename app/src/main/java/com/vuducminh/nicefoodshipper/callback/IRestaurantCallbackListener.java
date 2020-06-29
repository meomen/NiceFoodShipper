package com.vuducminh.nicefoodshipper.callback;

import com.vuducminh.nicefoodshipper.model.RestaurantModel;

import java.util.List;

//lắng nghe sự kiện item nhà hàng (restaurant) được chọn
public interface IRestaurantCallbackListener {
    void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList);
    void onRestaurantLoadFailed(String message);
}
