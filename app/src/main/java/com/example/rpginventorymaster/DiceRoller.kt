package com.example.rpginventorymaster

import kotlin.random.Random

object DiceRoller {

    // Tipos de dados suportados
    enum class DiceType(val faces: Int) {
        D4(4), D6(6), D8(8), D10(10), D12(12), D20(20)
    }

    // Rolagem única com modificador
    fun roll(diceType: DiceType, modifier: Int = 0): Int {
        return Random.nextInt(1, diceType.faces + 1) + modifier
    }

    // Rolagem múltipla (ex: 2d6 + 3)
    fun rollMultiple(diceCount: Int, diceType: DiceType, modifier: Int = 0): List<Int> {
        return (1..diceCount).map { roll(diceType) + modifier }
    }
    object DiceRoller {

        enum class DiceType(val faces: Int) {
            D4(4), D6(6), D8(8), D10(10), D12(12), D20(20)
        }

        fun roll(diceType: DiceType, modifier: Int = 0): Int {
            return Random.nextInt(1, diceType.faces + 1) + modifier
        }

        fun rollMultiple(diceCount: Int, diceType: DiceType, modifier: Int = 0): List<Int> {
            return (1..diceCount).map { roll(diceType) + modifier }
        }

        fun formatResult(results: List<Int>, modifier: Int = 0, cd: Int = 0): String {
            val total = results.sum()
            return buildString {
                append("Rolagem: ")
                if (results.size > 1) append("(${results.joinToString(" + ")})") else append(results.first())
                if (modifier != 0) append(" + $modifier")
                append(" = $total")
                if (cd != 0) append("\nCD: $cd → ${if (total >= cd) "Sucesso!" else "Falha!"}")
            }
        }
    }

    // Formatar resultado para exibição
    fun formatResult(results: List<Int>, modifier: Int = 0, cd: Int): String {
        val total = results.sum() + modifier
        return if (results.size > 1) {
            "${results.joinToString(" + ")} + $modifier = $total"
        } else {
            "${results.first()} + $modifier = $total"
        }
    }
}