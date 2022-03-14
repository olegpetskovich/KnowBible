package com.app.bible.knowbible.mvvm.view.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.app.bible.knowbible.mvvm.model.BibleTextModel
import com.app.bible.knowbible.mvvm.model.ChapterModel
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.app.bible.knowbible.mvvm.view.callback_interfaces.ISelectBibleText
import com.app.bible.knowbible.mvvm.view.dialog.AddVerseNoteDialog
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.muddzdev.styleabletoast.StyleableToast
import java.util.*

class FoundVersesListRVAdapter(
    internal val context: Context,
    private val searchedVersesList: ArrayList<BibleTextModel>,
    private val myFragmentManager: FragmentManager
) : RecyclerView.Adapter<FoundVersesListRVAdapter.MyViewHolder>() {
    private lateinit var fragmentChanger: IChangeFragment
    fun setFragmentChangerListener(fragmentChanger: IChangeFragment) {
        this.fragmentChanger = fragmentChanger
    }

    private lateinit var selectBibleTextListener: ISelectBibleText
    fun setSelectedBibleTextListener(selectBibleTextListener: ISelectBibleText) {
        this.selectBibleTextListener = selectBibleTextListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_found_verse, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val foundVerseModel = searchedVersesList[position]
        //Меняем тему айтема здесь, потому что по другому оно не меняется непонятно почему
        when (ThemeManager.theme) {
            ThemeManager.Theme.LIGHT -> {
                holder.tvVerse.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorTextLightTheme
                    )
                )
                ImageViewCompat.setImageTintList(
                    holder.btnAddNote,
                    ContextCompat.getColorStateList(context, R.color.colorIconLightTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnCopyVerse,
                    ContextCompat.getColorStateList(context, R.color.colorIconLightTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnShare,
                    ContextCompat.getColorStateList(context, R.color.colorIconLightTheme)
                )
            }
            ThemeManager.Theme.DARK -> {
                holder.tvVerse.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorTextDarkTheme
                    )
                )
                ImageViewCompat.setImageTintList(
                    holder.btnAddNote,
                    ContextCompat.getColorStateList(context, R.color.colorIconDarkTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnCopyVerse,
                    ContextCompat.getColorStateList(context, R.color.colorIconDarkTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnShare,
                    ContextCompat.getColorStateList(context, R.color.colorIconDarkTheme)
                )
            }
            ThemeManager.Theme.BOOK -> {
                holder.tvVerse.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorTextBookTheme
                    )
                )
                ImageViewCompat.setImageTintList(
                    holder.btnAddNote,
                    ContextCompat.getColorStateList(context, R.color.colorIconBookTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnCopyVerse,
                    ContextCompat.getColorStateList(context, R.color.colorIconBookTheme)
                )
                ImageViewCompat.setImageTintList(
                    holder.btnShare,
                    ContextCompat.getColorStateList(context, R.color.colorIconBookTheme)
                )
            }
        }

        //Делаем выделенным текст, помеченный тегом <b> </b>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvVerse.text =
                Html.fromHtml(foundVerseModel.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            holder.tvVerse.text = Html.fromHtml(foundVerseModel.text)
        }
    }

    override fun getItemCount(): Int {
        return searchedVersesList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), DialogListener {
        private lateinit var addVerseNoteDialog: AddVerseNoteDialog

        var tvVerse: TextView = itemView.findViewById(R.id.tvVerse)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        var btnAddNote: ImageView = itemView.findViewById(R.id.btnAddNote)
        var btnCopyVerse: ImageView = itemView.findViewById(R.id.btnCopyVerse)
        var btnShare: ImageView = itemView.findViewById(R.id.btnShare)

        init {
            btnAddNote.setOnClickListener {
                addVerseNoteDialog = AddVerseNoteDialog(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) addVerseNoteDialog.setVerse(
                    searchedVersesList[adapterPosition],
                    Html.fromHtml(
                        searchedVersesList[adapterPosition].text,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    ).toString()
                )
                else addVerseNoteDialog.setVerse(
                    searchedVersesList[adapterPosition],
                    Html.fromHtml(searchedVersesList[adapterPosition].text).toString()
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
                            searchedVersesList[adapterPosition].text,
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    else ClipData.newPlainText(
                        "label",
                        Html.fromHtml(searchedVersesList[adapterPosition].text)
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
                        searchedVersesList[adapterPosition].text,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    else Html.fromHtml(searchedVersesList[adapterPosition].text)

                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                context.startActivity(
                    Intent.createChooser(
                        myIntent,
                        context.getString(R.string.toast_share_verse)
                    )
                )
            }

            itemView.setOnClickListener {
//                selectBibleTextListener.setSelectedBibleText(searchedVersesList[adapterPosition].chapter_number.toString(), false)

                val bibleTextFragment = BibleTextFragment()
                bibleTextFragment.isBibleTextFragmentOpenedFromSearchFragment = true
                val chapterModel = ChapterModel(
                    searchedVersesList[adapterPosition].book_number,
                    searchedVersesList[adapterPosition].chapter_number,
                    searchedVersesList[adapterPosition].verse_number - 1
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
