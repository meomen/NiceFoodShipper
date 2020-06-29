package com.vuducminh.nicefoodshipper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//Apdate tạo item Restaurant
// RestaurantList ở SizeAddonEditActivity sử dụng
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vuducminh.nicefoodshipper.R;
import com.vuducminh.nicefoodshipper.callback.IRecyclerClickListener;
import com.vuducminh.nicefoodshipper.common.Common;
import com.vuducminh.nicefoodshipper.evenbus.RestaurantSelectEvent;
import com.vuducminh.nicefoodshipper.model.RestaurantModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

//  Apdate tạo item Restaurant
//  RestaurantList ở RestaurantActivity sử dụng adapter này
public class MyRestaurantAdapter extends RecyclerView.Adapter<MyRestaurantAdapter.MyViewHolder> {
    Context context;                                                                        // RestaurantActivity
    List<RestaurantModel> restaurantModels;                                                 // DS dữ liệu Nhà hàng

    public MyRestaurantAdapter(Context context, List<RestaurantModel> restaurantModels) {
        this.context = context;
        this.restaurantModels = restaurantModels;
    }

    // Liên kết với giao diện(Layout)
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_restaurant,parent,false);
        return new MyViewHolder(view);
    }

    // Đổ dữ liệu vào
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(restaurantModels.get(position).getImageUrl())
                .into(holder.img_restaurant);
        holder.tv_restaurant_name.setText(new StringBuilder(restaurantModels.get(position).getName()));
        holder.tv_restaurant_address.setText(new StringBuilder(restaurantModels.get(position).getAddress()));


        //Bắt sự kiện khi ấn item restaurant
        holder.setListener((view, pos) -> {
            Common.currentRestaurant = restaurantModels.get(pos);
            EventBus.getDefault().postSticky(new RestaurantSelectEvent(restaurantModels.get(pos)));
        });

    }

    @Override
    public int getItemCount() {
        return restaurantModels.size();
    }


    // Tạo viewHolder để adapter dễ quản lý nhiều item
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_restaurant_name)
        TextView tv_restaurant_name;
        @BindView(R.id.tv_restaurant_address)
        TextView tv_restaurant_address;
        @BindView(R.id.img_restaurant)
        ImageView img_restaurant;

        Unbinder unbinder;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }
}

