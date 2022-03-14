package com.app.bible.knowbible.mvvm.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import com.app.bible.knowbible.R
import com.app.bible.knowbible.data.local.HighlightedBibleTextInfoDBHelper
import com.app.bible.knowbible.mvvm.model.HighlightedBibleTextInfoModel
import com.app.bible.knowbible.mvvm.model.BibleTextModel
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_bible_translation.*
import kotlinx.android.synthetic.main.item_bible_translation.tvAbbreviationName
import java.lang.reflect.Field

class ColorPickerDialog(private val listener: ColorPickerDialogListener) : AppCompatDialogFragment() {
    private lateinit var versesData: ArrayList<BibleTextModel>
    private lateinit var highlightedBibleTextInfoDBHelper: HighlightedBibleTextInfoDBHelper

    private lateinit var colorPickerView: ColorPickerView
    private lateinit var manager: ColorPickerPreferenceManager

    private lateinit var saveLoadData: SaveLoadData
    private lateinit var hexColor: String
    private var point: Point = Point(250, 500) //Дефолтные значения

    interface ColorPickerDialogListener {
        fun updateItemsColor(bibleTextsForHighlighting: ArrayList<BibleTextModel>)
        fun dismissColorPickerDialog(isColorSelected: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_color_picker, null)

        saveLoadData = SaveLoadData(requireContext())
        highlightedBibleTextInfoDBHelper = HighlightedBibleTextInfoDBHelper.getInstance(context)!!

        val addBoldText: MaterialCheckBox = view.findViewById(R.id.addBoldText)
        val addUnderline: MaterialCheckBox = view.findViewById(R.id.addUnderline)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(requireContext()).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> {
                ThemeManager.theme = ThemeManager.Theme.LIGHT
                setCheckBoxColor(addBoldText,
                        ContextCompat.getColor(requireContext(), R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(requireContext(), R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(requireContext(), R.color.colorTextLightTheme))
                setCheckBoxColor(addUnderline,
                        ContextCompat.getColor(requireContext(), R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(requireContext(), R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(requireContext(), R.color.colorTextLightTheme))
            }
            ThemeModeFragment.DARK_THEME -> {
                ThemeManager.theme = ThemeManager.Theme.DARK
                setCheckBoxColor(addBoldText,
                        ContextCompat.getColor(requireContext(), R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(requireContext(), R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(requireContext(), R.color.colorTextDarkTheme))
                setCheckBoxColor(addUnderline,
                        ContextCompat.getColor(requireContext(), R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(requireContext(), R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(requireContext(), R.color.colorTextDarkTheme))
            }
            ThemeModeFragment.BOOK_THEME -> {
                ThemeManager.theme = ThemeManager.Theme.BOOK
                setCheckBoxColor(addBoldText,
                        ContextCompat.getColor(requireContext(), R.color.colorUncheckedBookTheme),
                        ContextCompat.getColor(requireContext(), R.color.colorCheckedBookTheme),
                        ContextCompat.getColor(requireContext(), R.color.colorTextBookTheme))
                setCheckBoxColor(addUnderline,
                        ContextCompat.getColor(requireContext(), R.color.colorUncheckedBookTheme),
                        ContextCompat.getColor(requireContext(), R.color.colorCheckedBookTheme),
                        ContextCompat.getColor(requireContext(), R.color.colorTextBookTheme))
            }
        }

        manager = ColorPickerPreferenceManager.getInstance(context)
        colorPickerView = view.findViewById(R.id.colorPickerView)

        //Устанавливаемый или дефолтную точку цвета, или ранее выбранную
        val x = saveLoadData.loadInt("colorPickerX")
        val y = saveLoadData.loadInt("colorPickerY")
        if (x == -1 && y == -1) {
            manager.setSelectorPosition("MyColorPicker", point) //Дефолтная точка цвета
        } else {
            manager.setSelectorPosition("MyColorPicker", Point(x, y)) //Ранее выбранная точка цвета
        }

        val selectedColorView: MaterialCardView = view.findViewById(R.id.selectedColorView)
        val tvColoredVerse: TextView = view.findViewById(R.id.tvColoredVerse)
        tvColoredVerse.movementMethod = ScrollingMovementMethod()

        var selectedText = ""
        versesData.forEachIndexed { index, selectedTextModel ->
            //Добавляем пробел перед каждым стихом, кроме первого
            selectedText += if (index == 0) selectedTextModel.text
            else " " + selectedTextModel.text

            if (selectedTextModel.isTextBold) {
                addBoldText.isChecked = selectedTextModel.isTextBold
                tvColoredVerse.setTypeface(tvColoredVerse.typeface, Typeface.BOLD)
            }
            if (selectedTextModel.isTextUnderline) {
                addUnderline.isChecked = selectedTextModel.isTextUnderline
                tvColoredVerse.paintFlags = tvColoredVerse.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }
        }

        tvColoredVerse.text = selectedText

        addBoldText.setOnCheckedChangeListener { _, isChecked ->
            //Устанавливаем и отключаем жирный шрифт именно таким образом. Установка через параметр Typeface не подходит
            if (isChecked) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    tvColoredVerse.setTextAppearance(context, R.style.TextViewStyleBold)
                } else {
                    tvColoredVerse.setTextAppearance(R.style.TextViewStyleBold)
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    tvColoredVerse.setTextAppearance(context, R.style.TextViewStyleNormal)
                } else {
                    tvColoredVerse.setTextAppearance(R.style.TextViewStyleNormal)
                }
            }
        }

        addUnderline.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) tvColoredVerse.paintFlags = tvColoredVerse.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            else tvColoredVerse.paintFlags = tvColoredVerse.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv() //Убираем подчёркивание
        }

        //Есть вероятность, что этот код понадобиться в дальнейшем если будет функция, чтобы человек мог вписывать сам код цвета
//        val inputColor: AppCompatEditText = view.findViewById(R.id.inputColor)
//        inputColor.isSingleLine = true
//        inputColor.setOnEditorActionListener(OnEditorActionListener { v: TextView?, actionId: Int, _: KeyEvent? ->
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                hideKeyboard()
//                return@OnEditorActionListener true
//            }
//            false
//        })

        colorPickerView.setColorListener(ColorEnvelopeListener { envelope, _ ->
            hexColor = java.lang.String.format("#%06X", 0xFFFFFF and envelope.color) //Параметр для сохранения и дальнейшего отображения выбранного цвета на тексте Библии
            point = colorPickerView.selectedPoint //Параметр для сохранения и дальнейшего отображения выбранного цвета на палитре цветов в этом даилоге

//            selectedColorView.setCardBackgroundColor(envelope.color)
//            tvTextColor.setTextColor(envelope.color)
            selectedColorView.setCardBackgroundColor(Color.parseColor(hexColor))
            tvColoredVerse.setTextColor(Color.parseColor(hexColor))


            //Есть вероятность, что этот код понадобиться в дальнейшем если будет функция, чтобы человек мог вписывать сам код цвета
//            setEditTextCursorAndBottomLineColor(inputColor, hexColor)
//            inputColor.setText(envelope.hexCode)
        })

        val btnCancel: TextView = view.findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener {
            listener.dismissColorPickerDialog(false)
        }

        val btnSelect: TextView = view.findViewById(R.id.btnSelect)
        btnSelect.setOnClickListener {
            //Сохраняем координаты выбранного цвета, чтобы при повторном открытии диалога палитры, отобразить ранее выбранный цвет.
            //Сохранять нужно именно координаты, потому что по другому в этой библиотеке установить цвет не получается.
            saveLoadData.saveInt("colorPickerX", point.x)
            saveLoadData.saveInt("colorPickerY", point.y)


            versesData.reverse() //Переворачиваем коллекцию, чтобы при отображении её в фрагменте Выделенные стихи (HighlightedVersesFragment) они показывались в правильном порядке
            for (selectedTextModel in versesData) {
                //Проверяем, есть ли эти данные в БД, смотря на то, какое значение в поле id.
                //Если id == -1, то данные ещё не добавлены, потому что у добавленных данных о стихе в поле id у объекта verseData будет значение id этих данных в БД.
                if (selectedTextModel.id != -1L) {                                                                                                          /*Берём обозначение перевода Библии из поля btnSelectTranslation, потому что оно уникально для каждого перевода и позволяет обозначить к какому переводу принадлежит выделяемый текст*/
                    highlightedBibleTextInfoDBHelper.updateBibleTextInfo(HighlightedBibleTextInfoModel(selectedTextModel.id, selectedTextModel.book_number, (activity as MainActivity).tvSelectTranslation.text.toString(), selectedTextModel.chapter_number, selectedTextModel.verse_number, hexColor, addBoldText.isChecked, addUnderline.isChecked))
                    selectedTextModel.textColorHex = hexColor
                    selectedTextModel.isTextBold = addBoldText.isChecked
                    selectedTextModel.isTextUnderline = addUnderline.isChecked
                } else {
                    //Сохраняем в БД hex код выбранного цвета, чтобы при загрузке текстов Библии потом отобразить текст с выбранном цветом
                    val idOfAddedData = highlightedBibleTextInfoDBHelper.addBibleTextInfo(HighlightedBibleTextInfoModel(
                            -1/*-1 здесь как заглушка, этот параметр нужен не при добавлении, а при получении данных, потому что там id создаётся автоматически*/,
                                                           /*Берём обозначение перевода Библии из поля btnSelectTranslation, потому что оно уникально для каждого перевода и позволяет обозначить к какому переводу принадлежит выделяемый текст*/
                            selectedTextModel.book_number, (activity as MainActivity).tvSelectTranslation.text.toString(), selectedTextModel.chapter_number, selectedTextModel.verse_number, hexColor, addBoldText.isChecked, addUnderline.isChecked))

                    selectedTextModel.id = idOfAddedData
                    selectedTextModel.textColorHex = hexColor
                    selectedTextModel.isTextBold = addBoldText.isChecked
                    selectedTextModel.isTextUnderline = addUnderline.isChecked
                }
            }

            listener.updateItemsColor(versesData)
            listener.dismissColorPickerDialog(true)
        }

        builder.setView(view)
        return builder.create()
    }

    //Метод для смены цвета checkBox, код из StackOverflow, не вникал в него
    private fun setCheckBoxColor(checkBox: MaterialCheckBox, uncheckedColor: Int, checkedColor: Int, textColor: Int) {
        checkBox.setTextColor(textColor)

        val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(uncheckedColor, checkedColor))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CompoundButtonCompat.setButtonTintList(checkBox, colorStateList)
        } else {
            checkBox.buttonTintList = colorStateList
        }
    }

    //В дальнейшем метод может понадобиться чтобы менять цвет курсора и нижней строки для EditText
    private fun setEditTextCursorAndBottomLineColor(view: AppCompatEditText, color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.background.colorFilter = context?.let { PorterDuffColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN) }
        }

        try {
            // Get the cursor resource id
            var field: Field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId: Int = field.getInt(view)

            // Get the editor
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor: Any = field.get(view)

            // Get the drawable and set a color filter
            val drawable = ContextCompat.getDrawable(view.context, drawableResId)
            drawable?.colorFilter = context?.let { PorterDuffColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN) }
            val drawables = arrayOf(drawable, drawable)

            // Set the drawables
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, drawables)
        } catch (ignored: Exception) {
        }
    }

    fun setVersesData(versesData: ArrayList<BibleTextModel>) {
        this.versesData = versesData
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHideBlurBackground(View.VISIBLE) //Делаем фон размытым
        // Устанавливаем закругленные края диалогу, ещё одна обязательная строка находится перед вызовом super.onCreate(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners)
        dialog?.window?.setDimAmount(0.4f) //Устанавливаем уровень тени на фоне
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.clearSavedAllData() //На свякий случай очищаем данные, которые могут невольно сохраняться
        (activity as MainActivity).showHideBlurBackground(View.GONE) //Убираем размытый фон
    }

    //Метод для связи с активити
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        listener = try {
//            context as LanguageDialogListener
//        } catch (e: ClassCastException) {
//            throw ClassCastException(context.toString() + "must implement LanguageDialogListener")
//        }
//    }
}
