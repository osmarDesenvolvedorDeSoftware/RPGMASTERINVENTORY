package com.example.rpginventorymaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter(
    private val players: List<Player>,
    private val onPlayerClick: (Player) -> Unit,
    private val onPlayerLongClick: (Player) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewPlayerName: TextView = itemView.findViewById(R.id.textViewPlayerName)
        val textViewHP: TextView = itemView.findViewById(R.id.textViewHP)
        val textViewCA: TextView = itemView.findViewById(R.id.textViewCA)
        val textViewInitiative: TextView = itemView.findViewById(R.id.textViewInitiative)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.textViewPlayerName.text = player.name
        holder.textViewHP.text = "HP: ${player.hp}"
        holder.textViewCA.text = "CA: ${player.ca}"
        holder.textViewInitiative.text = "Iniciativa: +${player.initiative}"

        // Clique simples
        holder.itemView.setOnClickListener {
            onPlayerClick(player)
        }

        // Clique longo
        holder.itemView.setOnLongClickListener {
            onPlayerLongClick(player)
            true
        }
    }

    override fun getItemCount(): Int = players.size
}