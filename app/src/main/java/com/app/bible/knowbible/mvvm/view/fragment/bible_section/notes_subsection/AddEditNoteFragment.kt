package com.app.bible.knowbible.mvvm.view.fragment.bible_section.notes_subsection

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.app.bible.knowbible.R
import com.app.bible.knowbible.data.local.NotesDBHelper
import com.app.bible.knowbible.mvvm.model.NoteModel
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabBibleNumber
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utils
import com.google.android.material.button.MaterialButton
import com.muddzdev.styleabletoast.StyleableToast

class AddEditNoteFragment : Fragment() {
    private lateinit var listener: IActivityCommunicationListener
    private lateinit var myFragmentManager: FragmentManager
    private lateinit var notesDBHelper: NotesDBHelper

    var isNoteToAdd: Boolean = false //Значение этого поля определяет, пользователь открыл фрагмент для создания заметки, или же для её редактирования
    private var isBtnSaveClicked: Boolean = false

    private var noteData: NoteModel? = null

    private lateinit var tvVerseForNote: TextView
    private lateinit var editTextNote: AppCompatEditText
    private lateinit var btnSave: MaterialButton

    private lateinit var saveLoadData: SaveLoadData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_add_edit_note, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(requireContext())
        notesDBHelper = NotesDBHelper(requireContext())

        if (noteData != null && noteData!!.verseText.isNotEmpty()) {
            tvVerseForNote = myView.findViewById(R.id.tvVerseForNote)
            //Пока что нажатие будет отключено из-за расхождений в порядке глав и текстов в разных переводах Библии.
            //Потому что пользователь может нажать на стих, сохранённый в одном из Русских переводов,
            //а Библия может быть включена в одном из англ переводов и при открытии конкретного текста, может произойти расхождение
//            tvVerseForNote.setOnClickListener {
//                myFragmentManager.let {
//                    val myFragment = BibleTextFragment()
//                    myFragment.isBibleTextFragmentOpenedFromAddEditNoteFragment = true
//                    myFragment.chapterInfo = ChapterModel(noteData!!.bookNumber, noteData!!.chapterNumber, noteData!!.verseNumber - 1) //Пишем - 1, чтобы проскроллить к нужному айтему
//                    myFragment.setRootFragmentManager(myFragmentManager)
//
//                    val transaction: FragmentTransaction = it.beginTransaction()
//                    transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
//                    transaction.addToBackStack(null)
//                    transaction.replace(R.id.fragment_container_bible, myFragment)
//                    transaction.commit()
//                }
//            }
            tvVerseForNote.visibility = View.VISIBLE
            tvVerseForNote.text = noteData!!.verseText
        }

        editTextNote = myView.findViewById(R.id.editTextNote)
        editTextNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


        if (!isNoteToAdd) editTextNote.setText(noteData?.text)

        btnSave = myView.findViewById(R.id.btnSave)
        btnSave.setOnClickListener {
            if (editTextNote.text.toString().isNotEmpty()) {
                isBtnSaveClicked = true
                saveOrUpdateNote()
                myFragmentManager.popBackStack() //Закрываем фрагмент
            } else
                StyleableToast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.toast_field_cant_be_empty),
                    Toast.LENGTH_SHORT,
                    R.style.my_toast
                ).show()
        }

        return myView
    }

    private fun saveOrUpdateNote() {
        if (isNoteToAdd) {
            notesDBHelper.addNote(NoteModel(
                    -1/*-1 здесь как заглушка, этот параметр нужен не при добавлении, а при получении данных, потому что там id создаётся автоматически*/,
                    false,
                    -1,
                    -1,
                    -1,
                    "",
                    editTextNote.text.toString()))

            StyleableToast.makeText(
                requireContext(),
                requireContext().getString(R.string.toast_note_added),
                Toast.LENGTH_SHORT,
                R.style.my_toast
            ).show()
        } else {
            //Обновляем только если текст заметки отличается от ранее написанного
            if (editTextNote.text.toString() != noteData?.text) {
                notesDBHelper.updateNote(NoteModel(
                        noteData!!.id,
                        noteData!!.isNoteForVerse,
                        noteData!!.bookNumber,
                        noteData!!.chapterNumber,
                        noteData!!.verseNumber,
                        noteData!!.verseText,
                        editTextNote.text.toString()))

                StyleableToast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.toast_note_edited),
                    Toast.LENGTH_SHORT,
                    R.style.my_toast
                ).show()
            }
        }
    }

    fun setNoteData(noteData: NoteModel) {
        this.noteData = noteData
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun onStop() {
        super.onStop()
        Utils.log("onStop work")

        //Эта проверка нужна для того, чтобы в случае нажатия кнопки "сохранить" не сохранять заметку дважды. Если кнопка "Сохранить" нажата, то весь этот вызов срабатывать не будет
        if (!isBtnSaveClicked) {
            if (editTextNote.text.toString().isNotEmpty()) saveOrUpdateNote()
        }
    }

    override fun onPause() {
        super.onPause()
        Utils.log("onPause work")
        listener.setShowHideNoteButtons(View.GONE)
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(tabBibleNumber)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setShowHideDonationLay(View.GONE) //Задаём видимость кнопке Поддержать

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)

        //Если заметка только добавляется, то не отображать иконку удаления и поделиться, если же открыт для редактирования, то отображать
        if (isNoteToAdd) listener.setShowHideNoteButtons(View.GONE)
        else {
            listener.setShowHideNoteButtons(View.VISIBLE)
            noteData?.let { listener.setNoteData(it) } //Присваиваем данные заметки для удаления и функции поделиться
        }
    }


    //Метод для связи с активити
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notesDBHelper.closeDB()
    }
}
