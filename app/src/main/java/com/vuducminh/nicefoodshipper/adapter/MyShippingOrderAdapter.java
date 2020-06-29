package com.vuducminh.nicefoodshipper.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.vuducminh.nicefoodshipper.R;
import com.vuducminh.nicefoodshipper.ShippingActivity;
import com.vuducminh.nicefoodshipper.common.Common;
import com.vuducminh.nicefoodshipper.common.CommonAgr;
import com.vuducminh.nicefoodshipper.model.OrderModel;
import com.vuducminh.nicefoodshipper.model.ShippingOrderModel;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.paperdb.Paper;


//  Apdate tạo item Order cần giao
//  ShippingOrderList ở HomeFragment sử dụng adapter này
public class MyShippingOrderAdapter extends RecyclerView.Adapter<MyShippingOrderAdapter.MyViewHolder>{

    private Context context;                                                // Home Activity
    private List<ShippingOrderModel> shippingOrderModelList;                // DS dữ liệu ShippingOrder
    private SimpleDateFormat simpleDateFormat;                              // định đạng thời gian

    public MyShippingOrderAdapter(Context context, List<ShippingOrderModel> shippingOrderModelList) {
        this.context = context;
        this.shippingOrderModelList = shippingOrderModelList;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");       // dạng thời gian
        Paper.init(context);
    }

    // Liên kết với giao diện(Layout)
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_order_shipper_item,parent,false);
        return new MyViewHolder(view);
    }

    // Đổ dữ liệu vào
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        OrderModel orderModel = shippingOrderModelList.get(position).getOrderModel();
        Glide.with(context)
                .load(orderModel.getCartItemList().get(0).getFoodImage())
                .into(holder.img_food);
        holder.tv_date.setText(new StringBuilder(
                simpleDateFormat.format(orderModel.getCreateDate())
        ));

        Common.setSpanStringColor("No.: ",orderModel.getKey(),holder.tv_order_number, Color.parseColor("#BA454A"));

        Common.setSpanStringColor("Address:",orderModel.getShippingAddress(),holder.tv_order_address, Color.parseColor("#BA454A"));

        Common.setSpanStringColor("Payment:",orderModel.getTransactionId(),holder.tv_payment, Color.parseColor("#BA454A"));

        // nếu đơn hàng đang được ship
        if(shippingOrderModelList.get(position).isStartTrip()) {
            holder.btn_ship_now.setEnabled(false);
        }

        holder.btn_ship_now.setOnClickListener(v -> {
            Paper.book().write(CommonAgr.SHIPPING_ORDER_DATA,new Gson().toJson(shippingOrderModelList.get(position)));              // Lưu dữ liệu Shipping order
            context.startActivity(new Intent(context, ShippingActivity.class));                 // Mở màn hình bản đồ
        });

    }

    @Override
    public int getItemCount() {
        return shippingOrderModelList.size();
    }

    // Tạo viewHolder để adapter dễ quản lý nhiều item
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;

        @BindView(R.id.tv_date)
        TextView tv_date;
        @BindView(R.id.tv_order_address)
        TextView tv_order_address;
        @BindView(R.id.tv_order_number)
        TextView tv_order_number;
        @BindView(R.id.tv_payment)
        TextView tv_payment;
        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.btn_ship_now)
        MaterialButton btn_ship_now;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
