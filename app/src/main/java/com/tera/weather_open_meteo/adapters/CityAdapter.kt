package com.tera.weather_open_meteo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tera.weather_open_meteo.R
import com.tera.weather_open_meteo.databinding.ItemCityBinding
import com.tera.weather_open_meteo.models.CityModel


interface ItemClickListener {
    fun onItemClick(context: Context, position: Int)
    fun onItemClickDelete(list: ArrayList<Int>)
}

class CityAdapter(
    private val list: ArrayList<CityModel>,
    private var keyCheckOut: Boolean,
    private val listener: ItemClickListener
) : RecyclerView.Adapter<CityAdapter.Holder>() {

    private val listSelect = ArrayList<Int>()
    private val listBoolean = ArrayList<Boolean>()
    private var keyVisible = false
    private var keyChecked = false

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemCityBinding.bind(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context

        holder.binding.apply {
            if (position == 0) {
                val img = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_location_grey, null)
                val size = context.resources.getDimension(R.dimen.icon_size).toInt()
                img?.setBounds(0, 0, size, size)
                tvCity.setCompoundDrawables(img, null, null, null)
            }
            tvCity.text = item.name
            tvRegion.text = item.region
            tvDate.text = item.date
            tvTemp.text = item.temp
            imIcon.setImageResource(item.icon)

            // Передать в активность позицию щелчка
            card.setOnClickListener {
                listener.onItemClick(context, position)
            }

            // Внешнее выключение CheckBox через keyCheckOut
            if (keyCheckOut) {
                checkBox.isVisible = false
                checkBox.isChecked = false
                keyCheckOut = false
            } else {
                if (position != 0) {
                    checkBox.isVisible = keyVisible
                    checkBox.isChecked = keyChecked // Выключить, если список пуст
                }
                //if (position == pos)
                //checkBox.isChecked = true
            }

            // Изменить видимость CheckBox
            card.setOnLongClickListener {

                keyVisible = !keyVisible
                setListBoolean()

                // Если все CheckBox не выбраны, передать в активность чистый список
                if (!keyVisible) {
                    listSelect.clear()
                    keyChecked = false
                    listener.onItemClickDelete(listSelect)
                }
                update()
                true
            }

            // Передать в активность список выбранных позиций
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                checkBox.isChecked = isChecked

                // Заполнить список Boolean значениями CheckBox (кроме первого)
                if (position != 0)
                    listBoolean[position] = isChecked

                listSelect.clear()

                // Добавить в список позиций выбранные позиции
                for (i in list.indices) {
                    val key = listBoolean[i]
                    if (key)
                        listSelect.add(i)
                }
                listener.onItemClickDelete(listSelect)
            }
        }
    }

    private fun update() {
        for (i in list.indices) {
            notifyItemChanged(i)
        }
    }

    private fun setListBoolean() {
        listBoolean.clear()
        for (i in list.indices) {
            listBoolean.add(false)
        }
    }

}