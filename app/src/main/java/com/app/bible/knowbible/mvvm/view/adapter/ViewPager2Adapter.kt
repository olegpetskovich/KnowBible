package com.app.bible.knowbible.mvvm.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.app.bible.knowbible.R
import com.app.bible.knowbible.data.local.HighlightedBibleTextInfoDBHelper
import com.app.bible.knowbible.mvvm.model.BibleTextModel
import com.app.bible.knowbible.mvvm.model.DataToRestoreModel
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment.Companion.DATA_TO_RESTORE
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utils
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ViewPager2Adapter(private val context: Context, private val chaptersTextList: ArrayList<ArrayList<BibleTextModel>>, private val myFragmentManager: FragmentManager) : RecyclerView.Adapter<ViewPager2Adapter.PagerVH>() {
    lateinit var dataToRestoreData: DataToRestoreModel //Поле для определения и сохранения скролла в Ресайклере

    private val saveLoadData = SaveLoadData(context)

    private lateinit var rvAdapter: BibleTextRVAdapter

    private var mapRV = hashMapOf<Int, RecyclerView>()

    interface IFragmentCommunication {
        fun saveScrollPosition(bookNumber: Int, chapterNumber: Int, scrollPosition: Int)
        fun getTranslationNameName(): String
    }

    private lateinit var fragmentCommunication: IFragmentCommunication
    fun setIFragmentCommunicationListener(fragmentCommunication: IFragmentCommunication) {
        this.fragmentCommunication = fragmentCommunication
    }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    private lateinit var multiSelectionListener: BibleTextRVAdapter.MultiSelectionPanelListener
    fun setMultiSelectionPanelListener(multiSelectionListener: BibleTextRVAdapter.MultiSelectionPanelListener) {
        this.multiSelectionListener = multiSelectionListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager_two, parent, false)
        return PagerVH(view)
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH =
//            PagerVH(LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager_two, parent, false))

    override fun getItemCount(): Int = chaptersTextList.size

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: PagerVH, position: Int) {
        if (!mapRV.containsKey(position))
            mapRV[position] = holder.recyclerView

        val bibleTextInfoDBHelper = HighlightedBibleTextInfoDBHelper.getInstance(context)!!
        bibleTextInfoDBHelper
//                .loadBibleTextInfo(dataToRestoreData.bookNumber, models[0].chapter) //Просто берём данные из самого первого элемента в коллекции. Неважно из какого элемента брать, главное, что каждый из них хранит значение book_number и chapter
                .loadBibleTextInfo(dataToRestoreData.bookNumber, fragmentCommunication.getTranslationNameName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { highlightedTextsInfoList ->
                    val textList = chaptersTextList[position]

                    if (highlightedTextsInfoList.size != 0) {
                        for (textInfoItem in highlightedTextsInfoList) {
                            for (item in textList) {
                                if (item.text.isEmpty())
                                    item.text = "-" //Нужно устанавливать значение в некоторые строки, потому что в переводе UMT есть пустые стихи, их нужно заполнять, чтобы в адаптере им не присвоился другой текст при обновление айтема во время скрола

                                if (textInfoItem.bookNumber == item.book_number && textInfoItem.chapterNumber == item.chapter_number && textInfoItem.verseNumber == item.verse_number) {
                                    item.id = textInfoItem.id //id этого значения в БД. Это поле нужно для того, чтобы удалять данные из БД.
                                    item.textColorHex = textInfoItem.textColorHex
                                    item.isTextBold = textInfoItem.isTextBold
                                    item.isTextUnderline = textInfoItem.isTextUnderline
                                }
                            }
                        }
                    } else {
                        //В случае, если нет ни одного выделенного текста, мы в любом случае проходимся по списку всех текстов, чтобы найти и установить значения в пустые строки, чтобы не возникало ошибок в переводе UMT
                        for (item in textList) {
                            if (item.text.isEmpty())
                                item.text = "-" //Нужно устанавливать значение в некоторые строки, потому что в переводе UMT есть пустые стихи, их нужно заполнять, чтобы в адаптере им не присвоился другой текст при обновление айтема во время скрола
                        }
                    }

                    Utils.log("BibleTextFragment: onBindViewHolder")
                    rvAdapter = BibleTextRVAdapter(context, textList, myFragmentManager)
                    rvAdapter.setRecyclerViewThemeChangerListener(themeChanger) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.
                    rvAdapter.setMultiSelectionPanelListener(multiSelectionListener)
                    val rvItem = holder.recyclerView

                    rvItem.adapter = rvAdapter

                    rvItem.layoutManager = LinearLayoutManager(context)
                    rvItem.itemAnimator = DefaultItemAnimator()

                    rvItem.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            fragmentCommunication.saveScrollPosition(dataToRestoreData.bookNumber, dataToRestoreData.chapterNumber, (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                        }
                    })
                }
    }

    fun getScrollPosition(posPage: Int): Int {
        return if (mapRV[posPage]?.layoutManager != null) {
            val linearLayoutManager = mapRV[posPage]?.layoutManager as LinearLayoutManager
            linearLayoutManager.findFirstVisibleItemPosition()
        } else {
            0
        }
    }

    fun getRecyclerView(posPage: Int): RecyclerView? {
        return if (mapRV.contains(posPage) && mapRV[posPage]?.layoutManager != null) mapRV[posPage]
        else null
    }

    //Ничего не менять
    fun scrollTo(posPage: Int, smoothScroll: Boolean) {
        //Тут выставляется позиция скролла для RecyclerView, которая была сохранена для восстновления текста, на котором ранее остановился пользователь
        //__________________________________________________________________________________________
        val jsonScrollData = saveLoadData.loadString(DATA_TO_RESTORE)
        if (jsonScrollData != null && jsonScrollData.isNotEmpty()) {
            val dataToRestoreModel: DataToRestoreModel = Gson().fromJson(jsonScrollData, DataToRestoreModel::class.java)
            Utils.log("dataToRestoreModel.bookNumber: " + dataToRestoreModel.bookNumber + ", dataToRestoreData.bookNumber: " + dataToRestoreData.bookNumber)
            Utils.log("dataToRestoreModel.chapterNumber: " + (dataToRestoreModel.chapterNumber - 1) + ", dataToRestoreData.chapterNumber: " + dataToRestoreData.chapterNumber)
            Utils.log("dataToRestoreModel.scrollPosition: " + dataToRestoreModel.scrollPosition)

            if (dataToRestoreModel.scrollPosition != -1 //Проверяем, сохранена ли какая-то позиция ранее или же значение равно -1 (то есть ничего не было сохранено)
            ) {
                val linearLayoutManager = mapRV[posPage]!!.layoutManager as LinearLayoutManager
                //Плавный скролл
                if (smoothScroll) {
                    val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }
                    smoothScroller.targetPosition = dataToRestoreModel.scrollPosition
                    linearLayoutManager.startSmoothScroll(smoothScroller)
                    //Резкий скролл
                } else linearLayoutManager.scrollToPositionWithOffset(dataToRestoreModel.scrollPosition, 0)
            }
        }
        //__________________________________________________________________________________________
    }

    //Ничего не менять, это перегруженный метод предназначенный просто для установления нужной позиции скролла. Используется при открытии найденного текста в SearchFragment
    fun scrollTo(posPage: Int, posVerse: Int, smoothScroll: Boolean) {
        //Тут выставляется позиция скролла для RecyclerView
        //__________________________________________________________________________________________
        val linearLayoutManager = mapRV[posPage]!!.layoutManager as LinearLayoutManager
        //Плавный скролл
        if (smoothScroll) {
            val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }
            smoothScroller.targetPosition = posVerse
            linearLayoutManager.startSmoothScroll(smoothScroller)
        } else linearLayoutManager.scrollToPositionWithOffset(posVerse, 0) //Резкий скролл

        //__________________________________________________________________________________________
    }

    inner class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewVP2)
    }
}

