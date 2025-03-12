package com.example.rpginventorymaster

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class DiceRollerDialog : DialogFragment() {

    interface OnRollResultListener {
        fun onPerceptionCheckResult(success: Boolean, targetPlayerId: Int, stolenItem: Item)
    }

    private var listener: OnRollResultListener? = null
    private var targetPlayerId: Int = -1
    private lateinit var stolenItem: Item

    companion object {
        private const val ARG_TARGET_PLAYER = "target_player"
        private const val ARG_STOLEN_ITEM = "stolen_item"

        fun newInstance(targetPlayerId: Int, item: Item): DiceRollerDialog {
            val args = Bundle().apply {
                putInt(ARG_TARGET_PLAYER, targetPlayerId)
                putParcelable(ARG_STOLEN_ITEM, item)
            }
            return DiceRollerDialog().apply { arguments = args }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            targetPlayerId = it.getInt(ARG_TARGET_PLAYER)
            stolenItem = it.getParcelable(ARG_STOLEN_ITEM)!!
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRollResultListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnRollResultListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dice_roller_dialog, container, false)

        val spinnerDiceType = view.findViewById<Spinner>(R.id.spinnerDiceType)
        val spinnerDiceCount = view.findViewById<Spinner>(R.id.spinnerDiceCount)
        val editModifier = view.findViewById<EditText>(R.id.editModifier)
        val editCD = view.findViewById<EditText>(R.id.editCD)
        val textResult = view.findViewById<TextView>(R.id.textResult)
        val buttonRoll = view.findViewById<Button>(R.id.buttonRoll)
        val buttonConfirm = view.findViewById<Button>(R.id.buttonConfirm)

        // Configurar Spinners
        spinnerDiceType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            DiceRoller.DiceType.values().map { it.name }
        )

        spinnerDiceCount.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            (1..5).map { it.toString() }
        )

        buttonRoll.setOnClickListener {
            val diceType = DiceRoller.DiceType.values()[spinnerDiceType.selectedItemPosition]
            val diceCount = spinnerDiceCount.selectedItem.toString().toInt()
            val modifier = editModifier.text.toString().toIntOrNull() ?: 0
            val cd = editCD.text.toString().toIntOrNull() ?: 0

            val results = DiceRoller.rollMultiple(diceCount, diceType, modifier)
            val total = results.sum()

            val success = total >= cd
            textResult.text = DiceRoller.formatResult(results, modifier, cd)

            buttonRoll.isEnabled = false
            buttonConfirm.visibility = View.VISIBLE

            buttonConfirm.setOnClickListener {
                listener?.onPerceptionCheckResult(success, targetPlayerId, stolenItem)
                dismiss()
            }
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle("Teste de Percepção")
        return dialog
    }
}