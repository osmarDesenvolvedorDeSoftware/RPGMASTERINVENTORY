package com.example.rpginventorymaster

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MasterPanelActivity : AppCompatActivity() {

    private lateinit var recyclerViewPlayers: RecyclerView
    private lateinit var adapter: PlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master_panel)

        recyclerViewPlayers = findViewById(R.id.recyclerViewPlayers)
        recyclerViewPlayers.layoutManager = LinearLayoutManager(this)

        // Inicializa o adapter com os dois listeners
        adapter = PlayerAdapter(
            players = emptyList(),
            onPlayerClick = { player -> navigateToPlayerInventory(player) },
            onPlayerLongClick = { player -> showPlayerOptionsDialog(player) }
        )

        recyclerViewPlayers.adapter = adapter

        findViewById<Button>(R.id.buttonAddPlayer).setOnClickListener {
            showAddPlayerDialog()
        }

        loadPlayers()
    }

    private fun loadPlayers() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.playerDao().getAllPlayers().collect { players ->
                adapter = PlayerAdapter(
                    players = players,
                    onPlayerClick = { player -> navigateToPlayerInventory(player) },
                    onPlayerLongClick = { player -> showPlayerOptionsDialog(player) }
                )
                recyclerViewPlayers.adapter = adapter
            }
        }
    }

    private fun navigateToPlayerInventory(player: Player) {
        val intent = Intent(this, MasterInventoryActivity::class.java).apply {
            putExtra("playerId", player.id)
            putExtra("playerName", player.name)
        }
        startActivity(intent)
    }

    private fun showPlayerOptionsDialog(player: Player) {
        val options = arrayOf("Editar Atributos", "Excluir Jogador")

        AlertDialog.Builder(this)
            .setTitle(player.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditAttributesDialog(player)
                    1 -> confirmDeletePlayer(player)
                }
            }
            .show()
    }

    private fun confirmDeletePlayer(player: Player) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Jogador")
            .setMessage("Tem certeza que deseja remover ${player.name}?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@MasterPanelActivity)
                        .playerDao()
                        .deletePlayer(player)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditAttributesDialog(player: Player) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_attributes, null)
        val editHP = dialogView.findViewById<EditText>(R.id.editHP)
        val editCA = dialogView.findViewById<EditText>(R.id.editCA)
        val editInitiative = dialogView.findViewById<EditText>(R.id.editInitiative)

        editHP.setText(player.hp.toString())
        editCA.setText(player.ca.toString())
        editInitiative.setText(player.initiative.toString())

        AlertDialog.Builder(this)
            .setTitle("Editar Atributos")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val updatedPlayer = player.copy(
                    hp = editHP.text.toString().toIntOrNull() ?: player.hp,
                    ca = editCA.text.toString().toIntOrNull() ?: player.ca,
                    initiative = editInitiative.text.toString().toIntOrNull() ?: player.initiative
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@MasterPanelActivity)
                        .playerDao()
                        .updatePlayer(updatedPlayer)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddPlayerDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_player, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextPlayerName)

        AlertDialog.Builder(this)
            .setTitle("Novo Jogador")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val name = editTextName.text.toString().trim()

                if (name.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getDatabase(this@MasterPanelActivity)
                            .playerDao()
                            .insertPlayer(Player(name = name, hp = 100, ca = 15, initiative = 0))
                        withContext(Dispatchers.Main) { loadPlayers() }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}