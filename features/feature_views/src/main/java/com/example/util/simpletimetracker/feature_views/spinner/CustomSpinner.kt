package com.example.util.simpletimetracker.feature_views.spinner

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.example.util.simpletimetracker.feature_views.R
import com.example.util.simpletimetracker.feature_views.databinding.SpinnerLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.onItemSelected
import com.example.util.simpletimetracker.feature_views.extension.setOnClick

class CustomSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    var onItemSelected: (CustomSpinnerItem) -> Unit = {}
    var onPositionSelected: (Int) -> Unit = {}

    private val binding: SpinnerLayoutBinding = SpinnerLayoutBinding
        .inflate(LayoutInflater.from(context), this, true)

    private val adapter: ArrayAdapter<String> = ArrayAdapter(context, R.layout.item_spinner_layout)
    private var items: List<CustomSpinnerItem> = emptyList()

    init {
        binding.customSpinner.adapter = adapter
        setOnClick { binding.customSpinner.performClick() }
    }

    fun setData(items: List<CustomSpinnerItem>, selectedPosition: Int) {
        this.items = items
        adapter.clear()
        adapter.addAll(items.map(CustomSpinnerItem::text))

        binding.customSpinner.onItemSelectedListener = null
        // Calling setSelection(int, boolean) because it sets selection internally and listener isn't called later.
        binding.customSpinner.setSelection(selectedPosition, false)
        binding.customSpinner.onItemSelected {
            items.getOrNull(it)?.let(onItemSelected::invoke)
            onPositionSelected(it)
        }
    }

    abstract class CustomSpinnerItem {
        abstract val text: String
    }

    data class CustomSpinnerTextItem(
        override val text: String,
    ) : CustomSpinnerItem()
}
