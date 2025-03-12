package com.example.rpginventorymaster

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PlayerInventoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private var playerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_inventory)

        playerId = intent.getIntExtra("playerId", -1)
        val playerName = intent.getStringExtra("playerName") ?: "Jogador"
        findViewById<TextView>(R.id.textViewInventoryTitle).text = "Inventário de $playerName"

        recyclerView = findViewById(R.id.recyclerViewItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(emptyList()) { /* Sem ações */ }
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddItem).hide()
        loadItems()
    }

    private fun loadItems() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.itemDao().getItemsForPlayer(playerId).collect { items ->
                adapter.updateItems(items)
            }
        }
    }
}