package com.example.rpginventorymaster

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SelectItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_item)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewItems)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val playerId = intent.getIntExtra("playerId", -1)
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            db.itemDao().getItemsForPlayer(playerId).collect { items ->
                // Correção: Passar o listener de clique corretamente
                val adapter = ItemAdapter(items) { selectedItem ->
                    val resultIntent = Intent().apply {
                        putExtra("selectedItemId", selectedItem.id)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish() // Fechar a activity após seleção
                }
                recyclerView.adapter = adapter
            }
        }
    }
}