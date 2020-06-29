package com.vuducminh.nicefoodshipper.evenbus;

import com.vuducminh.nicefoodshipper.model.RestaurantModel;

// Sự kiện Restaurant được chọn
public class RestaurantSelectEvent {
    private RestaurantModel restaurantModel;

    public RestaurantSelectEvent(RestaurantModel restaurantModel) {
        this.restaurantModel = restaurantModel;
    }

    public RestaurantModel getRestaurantModel() {
        return restaurantModel;
    }

    public void setRestaurantModel(RestaurantModel restaurantModel) {
        this.restaurantModel = restaurantModel;
    }
}
