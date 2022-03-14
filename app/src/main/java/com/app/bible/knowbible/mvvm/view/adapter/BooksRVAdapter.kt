package com.app.bible.knowbible.mvvm.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.BookModel
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.app.bible.knowbible.mvvm.view.callback_interfaces.ISelectBibleText
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.SelectBookChapterFragment

class BooksRVAdapter(private val models: ArrayList<BookModel>) : RecyclerView.Adapter<BooksRVAdapter.MyViewHolder>() {
    interface BookInfoDialogListener {
        fun createInfoDialog(bookInfo: BookModel)
    }

    private lateinit var bookInfoDialogListener: BookInfoDialogListener
    fun setBookInfoDialogListener(bookInfoDialogListener: BookInfoDialogListener) {
        this.bookInfoDialogListener = bookInfoDialogListener
    }

    private lateinit var fragmentChanger: IChangeFragment
    fun setFragmentChangerListener(fragmentChanger: IChangeFragment) {
        this.fragmentChanger = fragmentChanger
    }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    private lateinit var selectBibleTextListener: ISelectBibleText
    fun setSelectedBibleTextListener(selectBibleTextListener: ISelectBibleText) {
        this.selectBibleTextListener = selectBibleTextListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_book, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvBookName.text = models[position].long_name
        holder.bookIcon.setImageResource(models[position].icon_res)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookIcon: ImageView = itemView.findViewById(R.id.bookIcon)
        val tvBookName: TextView = itemView.findViewById(R.id.tvBookName)
        private val btnBookInfo: ImageView = itemView.findViewById(R.id.btnBookInfo)

        init {
            themeChanger.changeItemTheme() //Смена темы для айтемов

            btnBookInfo.setOnClickListener {
                bookInfoDialogListener.createInfoDialog(models[adapterPosition]) //Отправляем данные о книге
            }

            itemView.setOnClickListener {
                selectBibleTextListener.setSelectedBibleText(models[adapterPosition].short_name + ".", true)

                val selectBookChapterFragment = SelectBookChapterFragment()
                selectBookChapterFragment.bookNumber = models[adapterPosition].book_number

                val chaptersList = ArrayList<Int>()
                var x = 0
                while (chaptersList.size < models[adapterPosition].number_of_chapters) {
                    chaptersList.add(x + 1)
                    x++
                }
                selectBookChapterFragment.chaptersList = chaptersList

                fragmentChanger.changeFragment(selectBookChapterFragment)
            }
        }
    }
}