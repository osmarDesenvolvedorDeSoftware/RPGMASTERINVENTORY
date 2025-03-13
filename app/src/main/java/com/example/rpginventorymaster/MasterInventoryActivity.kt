package com.example.rpginventorymaster

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first // Import necessário para usar first()
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MasterInventoryActivity : AppCompatActivity(), DiceRollerDialog.OnRollResultListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var textViewPlayerName: TextView
    private lateinit var buttonAddItem: Button
    private lateinit var buttonTransferItem: Button
    private lateinit var buttonRemoveItem: Button
    private lateinit var db: AppDatabase

    private var playerId: Int = -1

    companion object {
        private const val REQUEST_SELECT_ITEM = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master_inventory)

        db = AppDatabase.getDatabase(this)
        val playerName = intent.getStringExtra("playerName") ?: "Jogador"
        playerId = intent.getIntExtra("playerId", -1)

        textViewPlayerName = findViewById(R.id.textViewPlayerName)
        textViewPlayerName.text = "Inventário de $playerName"

        recyclerView = findViewById(R.id.recyclerViewMasterInventory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(emptyList()) { showItemOptionsDialog(it) }
        recyclerView.adapter = adapter

        buttonAddItem = findViewById(R.id.buttonAddItem)
        buttonTransferItem = findViewById(R.id.buttonTransferItem)
        buttonRemoveItem = findViewById(R.id.buttonRemoveItem)

        loadInventory()

        buttonAddItem.setOnClickListener { showAddItemDialog() }
        buttonRemoveItem.setOnClickListener { showRemoveItemDialog() }
        buttonTransferItem.setOnClickListener { showTransferItemDialog() }
        buttonTransferItem.visibility = View.GONE
        buttonRemoveItem.visibility = View.GONE
    }

    private fun loadInventory() {
        lifecycleScope.launch {
            db.itemDao().getItemsForPlayer(playerId).collect { items ->
                adapter.updateItems(items)
            }
        }
    }

    private fun showItemOptionsDialog(item: Item) {
        val options = arrayOf("Editar", "Excluir", "Roubar Item")
        AlertDialog.Builder(this)
            .setTitle("Opções para ${item.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditItemDialog(item)
                    1 -> confirmDeleteItem(item)
                    2 -> showStealItemDialog(item)
                }
            }
            .show()
    }

    private fun showEditItemDialog(item: Item) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_item, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextItemDescription)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextItemQuantity)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        spinnerCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Armadura", "Bolsa", "Dinheiro")
        )

        editTextName.setText(item.name)
        editTextDescription.setText(item.description)
        editTextQuantity.setText(item.quantity.toString())
        spinnerCategory.setSelection(
            when (item.category) {
                "Armadura" -> 0
                "Bolsa" -> 1
                "Dinheiro" -> 2
                else -> 1
            }
        )

        AlertDialog.Builder(this)
            .setTitle("Editar Item")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val newName = editTextName.text.toString().trim()
                val newDescription = editTextDescription.text.toString().trim()
                val newQuantity = editTextQuantity.text.toString().toIntOrNull() ?: 0
                val newCategory = when (spinnerCategory.selectedItemPosition) {
                    0 -> "Armadura"
                    1 -> "Bolsa"
                    2 -> "Dinheiro"
                    else -> "Bolsa"
                }

                if (newName.isNotEmpty() && newQuantity > 0) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.itemDao().updateItem(
                            item.copy(
                                name = newName,
                                description = newDescription,
                                quantity = newQuantity,
                                category = newCategory
                            )
                        )
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onPerceptionCheckResult(success: Boolean, targetPlayerId: Int, stolenItem: Item) {
        if (success) {
            lifecycleScope.launch(Dispatchers.IO) {
                db.itemDao().updateItem(stolenItem.copy(playerId = targetPlayerId))

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MasterInventoryActivity, "${stolenItem.name} foi roubado com sucesso!", Toast.LENGTH_SHORT).show()
                    loadInventory()
                }
            }
        } else {
            Toast.makeText(this, "O roubo falhou!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteItem(item: Item) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Item")
            .setMessage("Deseja remover ${item.name}?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.itemDao().deleteItem(item)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showStealItemDialog(item: Item) {
        lifecycleScope.launch {
            val players = db.playerDao().getAllPlayersOnce().filter { it.id != item.playerId }
            val playerNames = players.map { it.name }.toTypedArray()

            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@MasterInventoryActivity)
                    .setTitle("Roubar para qual jogador?")
                    .setItems(playerNames) { _, which ->
                        val targetPlayer = players[which]
                        DiceRollerDialog.newInstance(targetPlayer.id, item)
                            .show(supportFragmentManager, "DiceRollerDialog")
                    }
                    .show()
            }
        }
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_item, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextItemDescription)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextItemQuantity)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        spinnerCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Armadura", "Bolsa", "Dinheiro")
        )

        AlertDialog.Builder(this)
            .setTitle("Adicionar Item")
            .setView(dialogView)
            .setPositiveButton("Adicionar") { _, _ ->
                val name = editTextName.text.toString().trim()
                val description = editTextDescription.text.toString().trim()
                val quantity = editTextQuantity.text.toString().toIntOrNull() ?: 0
                val category = when (spinnerCategory.selectedItemPosition) {
                    0 -> "Armadura"
                    1 -> "Bolsa"
                    2 -> "Dinheiro"
                    else -> "Bolsa"
                }

                if (name.isNotEmpty() && quantity > 0 && playerId != -1) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.itemDao().insertItem(
                            Item(
                                name = name,
                                description = description,
                                quantity = quantity,
                                playerId = playerId,
                                category = category
                            )
                        )
                        withContext(Dispatchers.Main) {
                            loadInventory() // Recarrega a lista após a inserção
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRemoveItemDialog() {
        if (adapter.itemCount > 0) {
            AlertDialog.Builder(this)
                .setMessage("Selecione um item para remover")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showTransferItemDialog() {
        if (adapter.itemCount > 0) {
            val intent = Intent(this, SelectItemActivity::class.java).apply {
                putExtra("playerId", playerId)
            }
            startActivityForResult(intent, REQUEST_SELECT_ITEM)
        } else {
            AlertDialog.Builder(this)
                .setMessage("Nenhum item para transferir.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_ITEM && resultCode == Activity.RESULT_OK) {
            val itemId = data?.getIntExtra("selectedItemId", -1) ?: -1
            lifecycleScope.launch {
                val item = db.itemDao().getItemById(itemId)
                item?.let { showPlayerSelectionDialog(it) }
            }
        }
    }

    private fun showPlayerSelectionDialog(item: Item) {
        lifecycleScope.launch {
            val players = db.playerDao().getAllPlayers().first()
            val playerNames = players.map { it.name }.toTypedArray()

            AlertDialog.Builder(this@MasterInventoryActivity)
                .setTitle("Transferir ${item.name} para:")
                .setItems(playerNames) { _, which ->
                    transferItem(item, players[which].id)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun transferItem(item: Item, targetPlayerId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.itemDao().updateItem(item.copy(playerId = targetPlayerId))
        }
    }
}