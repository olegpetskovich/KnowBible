package com.app.bible.knowbible.mvvm.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import com.app.bible.knowbible.R
import com.app.bible.knowbible.data.local.NotesDBHelper
import com.app.bible.knowbible.mvvm.model.BibleTextModel
import com.app.bible.knowbible.mvvm.model.NoteModel
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.google.android.material.button.MaterialButton
import com.muddzdev.styleabletoast.StyleableToast

class AddVerseNoteDialog(private val listener: DialogListener) : AppCompatDialogFragment() {
    private lateinit var notesDBHelper: NotesDBHelper

    private lateinit var verseModel: BibleTextModel
    private lateinit var verseText: String

    private lateinit var textLay: LinearLayout

    private lateinit var tvVerseForNote: TextView
    private lateinit var mainLayout: LinearLayout
    private lateinit var editTextNote: AppCompatEditText
    private lateinit var btnSave: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_add_verse_note, null)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(requireContext()).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> ThemeManager.theme = ThemeManager.Theme.LIGHT
            ThemeModeFragment.DARK_THEME -> ThemeManager.theme = ThemeManager.Theme.DARK
            ThemeModeFragment.BOOK_THEME -> ThemeManager.theme = ThemeManager.Theme.BOOK
        }
        notesDBHelper = NotesDBHelper(requireContext())

        textLay = view.findViewById(R.id.textLay)
        tvVerseForNote = view.findViewById(R.id.tvVerseForNote)
        tvVerseForNote.text = verseText

        mainLayout = view.findViewById(R.id.mainLayout)

        editTextNote = view.findViewById(R.id.editTextNote)
//        editTextNote.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                mainLayout.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tvVerseForNote.height + editTextNote.height + btnSave.height)
//                mainLayout.requestLayout()
//            }
//        })

        btnSave = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener {
            if (editTextNote.text.toString().isNotEmpty()) {
                notesDBHelper.addNote(
                    NoteModel(
                        -1/*-1 здесь как заглушка, этот параметр нужен не при добавлении, а при получении данных, потому что там id создаётся автоматически*/,
                        true,
                        verseModel.book_number,
                        verseModel.chapter_number,
                        verseModel.verse_number,
                        verseText,
                        editTextNote.text.toString()
                    )
                )

                StyleableToast.makeText(
                    requireContext(),
                    getString(R.string.toast_note_added),
                    Toast.LENGTH_SHORT,
                    R.style.my_toast
                ).show()

                listener.dismissDialog()
            } else
                StyleableToast.makeText(
                    requireContext(),
                    getString(R.string.toast_field_cant_be_empty),
                    Toast.LENGTH_SHORT,
                    R.style.my_toast
                ).show()
        }

        //Весь этот код необходим потому, что в Книжной теме непонятно почему высота устанавливается на всю высоту экрана, даже если общая высота всех View намного меньше.
        //Из-за этого на экране получается огромный пробел.
        //Поскольку параметры View такие как высота, ширина и т.д. можно получить именно тогда, когда они уже отрисованы, необходимо их запрашивать именно в этот момент.
        //Поэтому мы используем обработчик изменения layout, которые позволяет нам получить необходимые параметры тогда, когда все View уже отрисованы.
        mainLayout.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                mainLayout.removeOnLayoutChangeListener(this) //На stackOverflow советуют удалять обработчик после первого вызова.
                //В этих двух полях собираем вместе параметры, которые дадут общую нужную высоту layout, которая будет установлена далее в mainLayout.layoutParams
                val layHeightValue = tvVerseForNote.height + textLay.height + btnSave.height
                val laysMarginsValue =
                    (tvVerseForNote.layoutParams as LinearLayout.LayoutParams).bottomMargin + (tvVerseForNote.layoutParams as LinearLayout.LayoutParams).topMargin
                +(textLay.layoutParams as LinearLayout.LayoutParams).bottomMargin + (textLay.layoutParams as LinearLayout.LayoutParams).topMargin
                +(btnSave.layoutParams as LinearLayout.LayoutParams).bottomMargin + (btnSave.layoutParams as LinearLayout.LayoutParams).topMargin
                mainLayout.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    layHeightValue + laysMarginsValue + 50
                )
                mainLayout.requestLayout()
            }
        })

        builder.setView(view)
        return builder.create()
    }

    fun setVerse(verseModel: BibleTextModel, verseText: String) {
        this.verseModel = verseModel
        this.verseText = verseText
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHideBlurBackground(View.VISIBLE) //Делаем фон размытым
        //Устанавливаем закругленные края диалогу, ещё одна обязательная строка находится перед вызовом super.onCreate(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners)
        dialog?.window?.setDimAmount(0.4f) //Устанавливаем уровень тени на фоне
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).showHideBlurBackground(View.GONE) //Убираем размытый фон
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notesDBHelper.closeDB()
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
