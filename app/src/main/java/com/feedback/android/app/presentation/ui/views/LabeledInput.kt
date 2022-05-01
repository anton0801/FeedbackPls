package com.feedback.android.app.presentation.ui.views

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.feedback.android.app.R
import com.feedback.android.app.common.getAllMonthsNames
import com.feedback.android.app.common.getCurrentYear

class LabeledInput : LinearLayoutCompat {

    private var labelText: String = ""
    private var canModify: Boolean = true
    private var fixedInputValue: String = ""
    private var inputValueColor: Int = 0
    private var inputValueTextMode: Int = 0
    private var inputHint: String = ""
    private var inputBackground: Int = 0
    private var labelTextSize: Float = 0f
    private var isActive: Boolean = true

    private lateinit var labelTextView: AppCompatTextView
    private lateinit var fixedInputTextView: AppCompatTextView
    private lateinit var input: AppCompatEditText
    private lateinit var spinnerDay: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner

    var hasError: Boolean = false
        private set

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        obtainValues(attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        obtainValues(attrs)
        init()
    }

    private fun init() {
        inflate(context, R.layout.labeled_input, this)
        labelTextView = findViewById(R.id.label)
        input = findViewById(R.id.input)
        fixedInputTextView = findViewById(R.id.input_value)
        spinnerDay = findViewById(R.id.spinner_day)
        spinnerMonth = findViewById(R.id.spinner_month)
        spinnerYear = findViewById(R.id.spinner_year)

        input.hint = inputHint
        input.setBackgroundResource(inputBackground)
        labelTextView.text = labelText
        labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, labelTextSize)

        input.isVisible = canModify && inputValueTextMode != 2
        fixedInputTextView.isVisible = !canModify && inputValueTextMode != 2

        if (inputValueTextMode == 2) {
            findViewById<LinearLayoutCompat>(R.id.birthday_select_container).isVisible = true

            val daysArrayAdapter =
                ArrayAdapter(
                    context,
                    R.layout.dropdown_item,
                    mutableListOf("--").apply {
                        addAll((1..31).map { it.toString() }.toList())
                    }
                )
            val monthsArrayAdapter =
                ArrayAdapter(
                    context,
                    R.layout.dropdown_item,
                    mutableListOf("--------").apply {
                        addAll(getAllMonthsNames())
                    }
                )
            val yearsArrayAdapter =
                ArrayAdapter(
                    context,
                    R.layout.dropdown_item,
                    mutableListOf("-----").apply {
                        val currentYear = getCurrentYear()
                        addAll(((currentYear - 100)..getCurrentYear()).map { it.toString() }
                            .toList().reversed())
                    }
                )
            spinnerDay.adapter = daysArrayAdapter
            spinnerMonth.adapter = monthsArrayAdapter
            spinnerYear.adapter = yearsArrayAdapter

            setSelectListenerToSpinner(spinnerDay)
            setSelectListenerToSpinner(spinnerMonth)
            setSelectListenerToSpinner(spinnerYear)
        }

        applyFixedValues()
    }

    private fun applyFixedValues() {
        if (fixedInputValue.isNotBlank()) {
            fixedInputTextView.text = fixedInputValue
            if (inputValueColor != 0 && isActive) {
                fixedInputTextView.setTextColor(inputValueColor)
            }
            if (inputValueTextMode == 1) {
                val content = SpannableString(fixedInputValue)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                fixedInputTextView.text = content
            }
        }
    }

    private fun obtainValues(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LabeledInput)
        labelText = a.getString(R.styleable.LabeledInput_labelText) ?: ""
        fixedInputValue = a.getString(R.styleable.LabeledInput_inputValue) ?: ""
        canModify = a.getBoolean(R.styleable.LabeledInput_canModify, true)
        inputValueColor = a.getColor(R.styleable.LabeledInput_inputValueColor, 0)
        inputValueTextMode = a.getInt(R.styleable.LabeledInput_inputValueTextModification, 0)
        inputHint = a.getString(R.styleable.LabeledInput_inputHint) ?: ""
        inputBackground = a.getResourceId(
            R.styleable.LabeledInput_backgroundResource,
            R.drawable.bottom_input_underline
        )
        labelTextSize = a.getDimension(
            R.styleable.LabeledInput_labelTextSize,
            resources.getDimension(R.dimen._4ssp)
        )
        a.recycle()
    }

    fun enableErrorVisibility(isVisible: Boolean) {
        labelTextView.setTextColor(
            if (isVisible) {
                resources.getColor(R.color.colorError)
            } else {
                resources.getColor(R.color.black)
            }
        )
        hasError = isVisible
    }

    fun setInputValue(text: String) {
        if (canModify) {
            input.setText(text)
        } else {
            if (inputValueTextMode == 2) {
                val date = text.split(".")
                val day = date[0]
                val month = date[1].replaceFirstChar { it.uppercase() }
                val year = date[2]
                val allMonths = getAllItemsFromSpinner(spinnerMonth)
                spinnerDay.setSelection(getAllItemsFromSpinner(spinnerDay).indexOf(day))
                spinnerMonth.setSelection(
                    allMonths.indexOf(allMonths.find { it == month })
                )
                spinnerYear.setSelection(getAllItemsFromSpinner(spinnerYear).indexOf(year))
            } else {
                fixedInputValue = text
                applyFixedValues()
            }
        }
    }

    private fun getAllItemsFromSpinner(spinner: Spinner): List<String> {
        val adapter = spinner.adapter
        val n = adapter.count
        return (0 until n).map { adapter.getItem(it).toString() }.toList()
    }

    fun getInputField() = input

    fun getInputValue() = if (inputValueTextMode == 2) {
        "${spinnerDay.selectedItem}.${spinnerMonth.selectedItem}.${spinnerYear.selectedItem}"
    } else {
        input.text.toString().trim()
    }

    fun setInputValueActive(isActive: Boolean) {
        this.isActive = isActive
        fixedInputTextView.isEnabled = isActive
        fixedInputTextView.setTextColor(
            if (!isActive)
                ContextCompat.getColor(context, R.color.colorInactive)
            else
                inputValueColor
        )
    }

    private fun setSelectListenerToSpinner(spinner: Spinner) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (parent?.getChildAt(0) as? TextView)?.textSize =
                    resources.getDimension(R.dimen._3ssp)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

}