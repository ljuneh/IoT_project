package com.example.iot

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iot.databinding.ReservationRvItemBinding

class ReservationRvAdapter(val context: Context, val resitemList: ArrayList<resitem>, val onClickDeleteIcon: (ResItem: resitem) -> Unit) :
    RecyclerView.Adapter<ReservationRvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.reservation_rv_item, viewGroup, false)
        return ViewHolder(ReservationRvItemBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding.startDay.text= "시작일\n"+resitemList[position].startday
        viewHolder.binding.endDay.text= "종료일\n"+resitemList[position].endday
        viewHolder.binding.removeItem.setOnClickListener {
            onClickDeleteIcon.invoke(resitemList[position])
        }

    }

    override fun getItemCount() = resitemList.size



    class ViewHolder(val binding: ReservationRvItemBinding):RecyclerView.ViewHolder(binding.root)
}