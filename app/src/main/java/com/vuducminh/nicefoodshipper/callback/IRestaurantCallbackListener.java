package com.vuducminh.nicefoodshipper.callback;

import com.vuducminh.nicefoodshipper.model.RestaurantModel;

import java.util.List;

public interface IRestaurantCallbackListener {
    void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList);
    void onRestaurantLoadFailed(String message);
}
