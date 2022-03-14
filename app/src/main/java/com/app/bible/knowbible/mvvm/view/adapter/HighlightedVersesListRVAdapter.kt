package com.app.bible.knowbible.mvvm.view.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.app.bible.knowbible.R
import com.app.bible.knowbible.data.local.HighlightedBibleTextInfoDBHelper
import com.app.bible.knowbible.mvvm.model.BibleTextModel
import com.app.bible.knowbible.mvvm.model.ChapterModel
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.app.bible.knowbible.mvvm.view.dialog.AddVerseNoteDialog
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.muddzdev.styleabletoast.StyleableToast
import java.util.*

class HighlightedVersesListRVAdapter(
    internal val context: Context,
    private val highlightedVersesList: ArrayList<BibleTextModel>,
    private val myFragmentManager: FragmentManager
) : RecyclerView.Adapter<HighlightedVersesListRVAdapter.MyViewHolder>() {
    private lateinit var fragmentChanger: IChangeFragment
    fun setFragmentChangerListener(fragmentChanger: IChangeFragment) {
        this.fragmentChanger = fragmentChanger
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_highlighted_verse, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val highlightedVerseModel = highlightedVersesList[position]
        //Меняем тему айтема здесь, потому по другому оно не меняется непонятно почему
        when (ThemeManager.theme) {
            ThemeManager.Theme.LIGHT -> {
                ImageViewCompat.setImageTintList(
                    holder.btnAddNote,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedLightTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnCopyVerse,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedLightTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnShare,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedLightTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnDelete,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedLightTheme)
                )
            }
            ThemeManager.Theme.DARK -> {
                ImageViewCompat.setImageTintList(
                    holder.btnAddNote,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedDarkTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnCopyVerse,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedDarkTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnShare,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedDarkTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnDelete,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedDarkTheme)
                )
            }
            ThemeManager.Theme.BOOK -> {
                ImageViewCompat.setImageTintList(
                    holder.btnAddNote,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedBookTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnCopyVerse,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedBookTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnShare,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedBookTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnDelete,
                    ContextCompat.getColorStateList(context, R.color.colorIconHighlightedBookTheme)
                )
            }
        }

        holder.tvVerse.setTextColor(Color.parseColor(highlightedVerseModel.textColorHex))

        //Делаем выделенным текст, помеченный тегом <b> </b>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvVerse.text =
                Html.fromHtml(highlightedVerseModel.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            holder.tvVerse.text = Html.fromHtml(highlightedVerseModel.text)
        }

        if (highlightedVerseModel.isTextUnderline) holder.tvVerse.paintFlags =
            holder.tvVerse.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        else holder.tvVerse.paintFlags =
            holder.tvVerse.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv() //Убираем подчёркивание
    }

    override fun getItemCount(): Int {
        return highlightedVersesList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), DialogListener {
        private lateinit var addVerseNoteDialog: AddVerseNoteDialog

        var tvVerse: TextView = itemView.findViewById(R.id.tvVerse)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        var btnAddNote: ImageView = itemView.findViewById(R.id.btnAddNote)
        var btnCopyVerse: ImageView = itemView.findViewById(R.id.btnCopyVerse)
        var btnShare: ImageView = itemView.findViewById(R.id.btnShare)
        var btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        init {
            btnAddNote.setOnClickListener {
                addVerseNoteDialog = AddVerseNoteDialog(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) addVerseNoteDialog.setVerse(
                    highlightedVersesList[adapterPosition],
                    Html.fromHtml(
                        highlightedVersesList[adapterPosition].text,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    ).toString()
                )
                else addVerseNoteDialog.setVerse(
                    highlightedVersesList[adapterPosition],
                    Html.fromHtml(highlightedVersesList[adapterPosition].text).toString()
                )

                addVerseNoteDialog.show(
                    myFragmentManager,
                    "Add Note Dialog"
                ) //По непонятной причине открыть диалог вызываемым здесь childFragmentManager-ом не получается, поэтому приходится использовать переданный объект fragmentManager из другого класса
            }

            btnCopyVerse.setOnClickListener {
                //Код для копирования текста
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ClipData.newPlainText(
                        "label",
                        Html.fromHtml(
                            highlightedVersesList[adapterPosition].text,
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    else ClipData.newPlainText(
                        "label",
                        Html.fromHtml(highlightedVersesList[adapterPosition].text)
                    )

                clipboard.setPrimaryClip(clip)

                StyleableToast.makeText(
                    context,
                    context.getString(R.string.verse_copied),
                    Toast.LENGTH_SHORT,
                    R.style.my_toast
                ).show()
            }

            btnShare.setOnClickListener {
                val myIntent = Intent(Intent.ACTION_SEND)
                myIntent.type = "text/plain"
                val shareBody =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(
                        highlightedVersesList[adapterPosition].text,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    else Html.fromHtml(highlightedVersesList[adapterPosition].text)

                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                context.startActivity(
                    Intent.createChooser(
                        myIntent,
                        context.getString(R.string.toast_share_verse)
                    )
                )
            }

            btnDelete.setOnClickListener {
                HighlightedBibleTextInfoDBHelper.getInstance(context)
                    ?.deleteBibleTextInfo(highlightedVersesList[adapterPosition].id) //Удаляем данные о выделенном тексте из БД
                highlightedVersesList.remove(highlightedVersesList[adapterPosition]) //Удаляем объект выделенного текста из коллекции
                notifyItemRemoved(adapterPosition) //Оповещаем адаптер о том, что объект из коллекции удалён, а значит нужно обновить список, чтобы отобразить его без удалённого элемента
            }

            itemView.setOnClickListener {
//                selectBibleTextListener.setSelectedBibleText(searchedVersesList[adapterPosition].chapter_number.toString(), false)

                val bibleTextFragment = BibleTextFragment()
                bibleTextFragment.isBibleTextFragmentOpenedFromHighlightedVersesFragment = true
                val chapterModel = ChapterModel(
                    highlightedVersesList[adapterPosition].book_number,
                    highlightedVersesList[adapterPosition].chapter_number,
                    highlightedVersesList[adapterPosition].verse_number - 1
                ) //Пишем - 1, чтобы проскроллить к нужному айтему
                bibleTextFragment.chapterInfo = chapterModel
                fragmentChanger.changeFragment(bibleTextFragment)
            }
        }

        override fun dismissDialog() {
            addVerseNoteDialog.dismiss()
        }
    }
}
