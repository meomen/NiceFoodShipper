package com.vuducminh.nicefoodshipper.callback;

import android.view.View;

//lắng nghe sự kiện của item trong bất kỳ RecyclerView nào
public interface IRecyclerClickListener {
    void onItemClickListener(View view, int pos);
}
