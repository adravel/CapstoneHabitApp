package com.fortfighter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.databinding.ItemToolBinding
import com.fortfighter.model.Tool
import com.fortfighter.ui.fragment.HouseDetailFragmentDirections
import com.fortfighter.ui.fragment.StoreFragmentDirections

class ToolAdapter(
    private val tools: MutableList<Tool>,
    private val isForParent: Boolean,
    private val childId: String,
    private val childName: String? = null
) : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

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
            val tool = tools[position]
            val toolStaticData = tool.getToolStaticData()!!

            nameText.text = toolStaticData.name
            powerText.text = toolStaticData.power.toString()
            priceText.text = context.getString(R.string.tool_price_placeholder, toolStaticData.price)

            // Display tool image
            Glide.with(context)
                .load(toolStaticData.imageResId)
                .into(toolImage)

            // Change tool power icon depending on the tool category
            if (toolStaticData.isCrushingTool) {
                powerText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_power, 0, 0, 0)
            } else {
                powerText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_clean, 0, 0, 0)
            }

            // Change button data depending on user's role
            if (isForParent && childName != null) {
                // Set button text and function for Parent in store page
                if (tool.isForSale) {
                    itemButton.isEnabled = false
                    itemButton.text = context.getString(R.string.button_label_sent)
                } else {
                    itemButton.isEnabled = true
                    itemButton.text = context.getString(R.string.button_label_send)
                }

                itemButton.setOnClickListener { view ->
                    // Display tool shipment confirmation dialog
                    val action = StoreFragmentDirections.actionStoreFragmentToToolShipmentConfirmationDialogFragment(
                        tool.id,
                        toolStaticData.name,
                        childId,
                        childName
                    )
                    view.findNavController().navigate(action)
                }
            } else {
                // Set button text and function for Child in house detail page
                itemButton.text = context.getString(R.string.button_label_use)

                itemButton.setOnClickListener { view ->
                    // Display tool purchase confirmation dialog
                    val action = HouseDetailFragmentDirections
                        .actionHouseDetailFragmentToToolPurchaseConfirmationDialogFragment(
                            tool.id,
                            toolStaticData.name,
                            tool.type.toInt()
                        )
                    view.findNavController().navigate(action)
                }
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