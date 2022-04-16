package com.example.capstonehabitapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.ItemToolBinding
import com.example.capstonehabitapp.model.Tool
import com.example.capstonehabitapp.ui.ShopFragmentDirections

class ToolAdapter(
    private val tools: MutableList<Tool>,
    private val childId: String,
    private val childName: String)
    : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

    inner class ToolViewHolder(val itemBinding: ItemToolBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ItemToolBinding.inflate(from, parent, false)
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        // Get ViewHolder's ItemView context
        val context = holder.itemView.context

        // Bind the data to RecyclerView item's Views
        holder.itemBinding.apply {
            nameText.text = tools[position].name
            powerText.text = tools[position].power.toString()
            priceText.text = tools[position].price.toString()

            if (tools[position].sold) {
                sellButton.isEnabled = false
                sellButton.text = context.getString(R.string.button_label_sold)
            } else {
                sellButton.isEnabled = true
                sellButton.text = context.getString(R.string.button_label_sell)
            }

            sellButton.setOnClickListener { view ->
                val action = ShopFragmentDirections.actionShopFragmentToToolSaleConfirmationDialogFragment(
                    tools[position].name,
                    childName
                )
                view.findNavController().navigate(action)
            }
        }
    }

    override fun getItemCount(): Int {
        return tools.size
    }

    fun updateList(newList: List<Tool>) {
        tools.clear()
        tools.addAll(newList)
        notifyDataSetChanged()
    }
}