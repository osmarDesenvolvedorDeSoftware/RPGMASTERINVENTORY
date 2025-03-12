package com.example.rpginventorymaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referências dos elementos da tela
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val radioGroupPerfil = findViewById<RadioGroup>(R.id.radioGroupPerfil)
        val buttonEntrar = findViewById<Button>(R.id.buttonEntrar)

        buttonEntrar.setOnClickListener {
            // Validação básica
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha usuário e senha!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar perfil selecionado
            val selectedId = radioGroupPerfil.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Selecione um perfil!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Navegar para a tela correta
            val isMestre = (selectedId == R.id.radioMestre)
            if (isMestre) {
                startActivity(Intent(this, MasterPanelActivity::class.java))
            } else {
                // Supondo que o jogador tem ID fixo por enquanto (ajuste depois)
                val intent = Intent(this, PlayerInventoryActivity::class.java).apply {
                    putExtra("playerId", 1)
                    putExtra("playerName", username)
                }
                startActivity(intent)
            }
        }
    }
}