package com.example.rpginventorymaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val groupedItems = mutableListOf<Any>()

    sealed class ListItem {
        data class Header(val category: String) : ListItem()
        // Renomeado de Item para InventoryItem para evitar conflito com o modelo Item
        data class InventoryItem(val item: Item) : ListItem()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewItemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val textViewItemQuantity: TextView = itemView.findViewById(R.id.textViewItemQuantity)
    }

    fun updateItems(newItems: List<Item>) {
        items = newItems
        groupItems()
        notifyDataSetChanged()
    }

    private fun groupItems() {
        groupedItems.clear()
        val categories = items.groupBy { it.category }

        categories.forEach { (category, itemsInCategory) ->
            groupedItems.add(ListItem.Header(category))
            itemsInCategory.forEach { item ->
                groupedItems.add(ListItem.InventoryItem(item))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (groupedItems[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.InventoryItem -> TYPE_ITEM
            else -> throw IllegalArgumentException("Invalid type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.section_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_inventory, parent, false)
                ItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = groupedItems[position] as ListItem.Header
                holder.textViewCategory.text = when (header.category) {
                    "Armadura" -> "Armaduras"
                    "Bolsa" -> "Itens na Bolsa"
                    "Dinheiro" -> "Dinheiro"
                    else -> header.category
                }
            }
            is ItemViewHolder -> {
                val inventoryItem = groupedItems[position] as ListItem.InventoryItem
                val item = inventoryItem.item
                holder.textViewItemName.text = item.name
                holder.textViewItemQuantity.text = "Qtd: ${item.quantity}"

                holder.itemView.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun getItemCount(): Int = groupedItems.size
}
