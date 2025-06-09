package com.tera.weather_open_meteo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tera.weather_open_meteo.R
import com.tera.weather_open_meteo.databinding.ItemDaysBinding
import com.tera.weather_open_meteo.models.DaysModel


class AdapterDays: ListAdapter<DaysModel, AdapterDays.Holder>(Comparator()) {

    class Holder(view: View, private val parent: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val binding = ItemDaysBinding.bind(view)

        fun bind(item: DaysModel) = with(binding) {
            tvDate.text = item.date
            tvWeek.text = item.nameDay
            tvMaxMin5.text = item.maxMin
            tvDescription.text= item.condition
            tvWind.text = item.windSpeed
            tvPress.text = item.pressure
            val icon = item.icon
            image.setImageResource(icon)

            val numDay = item.numDay

            val color: Int = if (numDay == 1 || numDay == 7)
                ContextCompat.getColor(parent.context, R.color.week)
            else
                ContextCompat.getColor(parent.context, R.color.white)

            tvWeek.setTextColor(color)
        }
    }


    // Сравнить элементы старые и новые.
    class Comparator() : DiffUtil.ItemCallback<DaysModel>() {
        override fun areItemsTheSame(oldItem: DaysModel, newItem: DaysModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DaysModel, newItem: DaysModel): Boolean {
            return oldItem == newItem
        }
    }

    // Создать элемент списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_days, parent, false)
        return Holder(view, parent)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }
}