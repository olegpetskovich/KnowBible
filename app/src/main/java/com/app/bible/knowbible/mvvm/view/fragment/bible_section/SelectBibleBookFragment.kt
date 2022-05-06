package com.app.bible.knowbible.mvvm.view.fragment.bible_section

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.BibleTranslationModel
import com.app.bible.knowbible.mvvm.model.BookModel
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.TapTargetSelectBibleBookFragment
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabBibleNumber
import com.app.bible.knowbible.mvvm.view.adapter.BooksRVAdapter
import com.app.bible.knowbible.mvvm.view.callback_interfaces.*
import com.app.bible.knowbible.mvvm.view.dialog.BookInfoDialog
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utils
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_select_testament.*

class SelectBibleBookFragment : Fragment(), IChangeFragment, IThemeChanger, ISelectBibleText, BooksRVAdapter.BookInfoDialogListener, DialogListener {
    var bookNumber: Int = -1 //Переменная, предназначенная для восстановления стэка фрагментов
    var chapterNumber: Int = -1 //Переменная, предназначенная для восстановления стэка фрагментов
    var isOldTestament: Boolean = false //Переменная, предназначенная для определения того, данные какого завета нужно предоставить. true - Ветхий Завет, false - Новый завет. На данный момент присвоено значение по умолчанию - false
    var abbreviationTranslationName: String = "" //Переменная, предназначенная для определения того, какой перевод выбран

    private var itemPosition: Int = -1

    private lateinit var saveLoadData: SaveLoadData
    private lateinit var bibleDataViewModel: BibleDataViewModel

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var myFragmentManager: FragmentManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var rvAdapter: BooksRVAdapter
    private lateinit var booksList: ArrayList<BookModel>


    private lateinit var bookInfoDialog: BookInfoDialog //Диалог краткого описания книги

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    //Интересный момент - поскольку onCreateView вызывается даже когда он просто выходит на передний план,
    //будучи до этого ниже в стэке, не нужно заботиться о том, чтобы обновить и отобразить новые данные, когда выбирается другой перевод.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        saveLoadData = SaveLoadData(requireContext())

//      Код восстановления стека фрагментов, делаем мы это специально в самом начале метода, чтобы оно запускалось без пролагов и незаметно для пользователя
        if (bookNumber != -1 && chapterNumber != -1) {
            myFragmentManager.let {
                val selectBookChapterFragment = SelectBookChapterFragment()
                selectBookChapterFragment.setRootFragmentManager(myFragmentManager)
                selectBookChapterFragment.bookNumber = bookNumber
                selectBookChapterFragment.chapterNumber = chapterNumber

                val transaction: FragmentTransaction = it.beginTransaction()
                transaction.addToBackStack(null)
                transaction.replace(R.id.fragment_container_bible, selectBookChapterFragment)
                transaction.commit()

                //Устанавливаем значение -1, чтобы при попытке вернуться на прежний фрагмент, пользователя снова не перебрасывало на только что закрытый фрагмент
                bookNumber = -1
                chapterNumber = -1
            }
        }

        val myView: View = inflater.inflate(R.layout.fragment_select_bible_info, container, false)

        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        recyclerView = myView.findViewById(R.id.recyclerView)

        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!

        val progressBar: ProgressBar = myView.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()

        val jsonBibleTranslationInfo = saveLoadData.loadString(BibleTranslationsFragment.TRANSLATION_DB_FILE_JSON_INFO)
        if (jsonBibleTranslationInfo != null && jsonBibleTranslationInfo.isNotEmpty()) {
            val gson = Gson()
            val bibleTranslationInfo: BibleTranslationModel = gson.fromJson(jsonBibleTranslationInfo, BibleTranslationModel::class.java)
            if (bibleTranslationInfo.abbreviationTranslationName != abbreviationTranslationName) {
                bibleDataViewModel
                        .getTestamentBooksList(BibleDataViewModel.TABLE_BOOKS, isOldTestament)
                        .observe(viewLifecycleOwner, Observer { list ->
                            rvAdapter = BooksRVAdapter(getListWithIcons(list))
                            rvAdapter.setFragmentChangerListener(this@SelectBibleBookFragment)
                            rvAdapter.setRecyclerViewThemeChangerListener(this@SelectBibleBookFragment) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.
                            rvAdapter.setSelectedBibleTextListener(this@SelectBibleBookFragment)
                            rvAdapter.setBookInfoDialogListener(this@SelectBibleBookFragment)

                            recyclerView.adapter = rvAdapter
                            progressBar.visibility = View.GONE

//                            Прописываем условие, чтобы этот код срабатывал только один раз
                            if (!saveLoadData.loadBoolean("TapTargetSelectBibleBookFragment")) {
                                val index = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                                val view = (recyclerView.layoutManager as LinearLayoutManager).findViewByPosition(index)
                                val bookIcon = view?.findViewById<ImageView>(R.id.bookIcon)
                                val btnBookInfo = view?.findViewById<ImageView>(R.id.btnBookInfo)

                                //Помещаем код в Handler, потому что только так можно получить значение параметров высоты и ширины
                                val mainHandler = Handler(requireContext().mainLooper)
                                val myRunnable =
                                        Runnable {
                                            TapTargetSequence(activity)
                                                    .targets(
                                                            bookIcon?.let { Utils.getTapTargetButton(it, requireContext(), R.string.btn_notes_title, R.string.btn_notes_description, Utils.convertPxToDp(btnNotes.width.toFloat(), requireContext()).toInt()) },
                                                            btnBookInfo?.let { Utils.getTapTargetButton(it, requireContext(), R.string.btn_testament_information_title, R.string.btn_testament_information_description, Utils.convertPxToDp(btnOldTestamentInfo.width.toFloat(), requireContext()).toInt()) }
                                                    )
                                                    .start()
                                        }
                                mainHandler.post(myRunnable)
                                saveLoadData.saveBoolean("TapTargetSelectBibleBookFragment", true)
                            }
                        })
            } else {
                rvAdapter = BooksRVAdapter(getListWithIcons(booksList))
                rvAdapter.setFragmentChangerListener(this@SelectBibleBookFragment)
                rvAdapter.setRecyclerViewThemeChangerListener(this@SelectBibleBookFragment) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.
                rvAdapter.setSelectedBibleTextListener(this@SelectBibleBookFragment)
                rvAdapter.setBookInfoDialogListener(this@SelectBibleBookFragment)

                recyclerView.adapter = rvAdapter
                progressBar.visibility = View.GONE

                //Прописываем условие, чтобы этот код срабатывал только один раз
                if (!saveLoadData.loadBoolean(TapTargetSelectBibleBookFragment)) {
                    //Помещаем код в Handler, потому что только так можно получить значение параметров высоты и ширины и получить View, а не null
                    val mainHandler = Handler(requireContext().mainLooper)
                    val myRunnable =
                            Runnable {
                                val index = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                                val view = (recyclerView.layoutManager as LinearLayoutManager).findViewByPosition(index)
                                val bookIcon = view?.findViewById<ImageView>(R.id.bookIcon)
                                val btnBookInfo = view?.findViewById<ImageView>(R.id.btnBookInfo)

                                TapTargetSequence(activity)
                                        .targets(
                                                Utils.getTapTargetButton(bookIcon!!, requireContext(), R.string.book_icon_title, R.string.book_icon_description, Utils.convertPxToDp(bookIcon.width.toFloat(), requireContext()).toInt()),
                                                Utils.getTapTargetButton(btnBookInfo!!, requireContext(), R.string.btn_book_info_title, R.string.btn_book_info_description, Utils.convertPxToDp(btnBookInfo.width.toFloat(), requireContext()).toInt())
                                        )
                                        .start()
                            }
                    mainHandler.post(myRunnable)
                    saveLoadData.saveBoolean(TapTargetSelectBibleBookFragment, true)
                }
            }
        }
        return myView
    }

    //В каждый элемент списка добавляем res значение, чтобы можно было в списке установить картинки книг к каждом айтему
    private fun getListWithIcons(list: ArrayList<BookModel>): ArrayList<BookModel> {
        list.forEach { model ->
            //С помощью метода getIdentifier достаём нужную картинку используя string имени конкретного поля в res.drawable
            model.icon_res = requireActivity().applicationContext.resources?.getIdentifier("book_${model.book_number}", "drawable", requireActivity().applicationContext?.packageName)!!
        }
        return list
    }

    override fun changeFragment(fragment: Fragment) {
        myFragmentManager.let {
            val myFragment = fragment as SelectBookChapterFragment
            myFragment.setRootFragmentManager(myFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container_bible, myFragment)
            transaction.commit()
        }
    }

    override fun changeItemTheme() {
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    fun setBooksList(booksList: ArrayList<BookModel>) {
        this.booksList = booksList
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onPause() {
        super.onPause()
        //Сохраняем позицию айтема, чтобы потом можно было её восстановить после обновления адаптера.
        //Обновлять адаптер нужно для того, чтобы в случае смены темы, все айтемы обновили свои цвета, если не обновлять адаптер, то с этим возникают проблемы
        itemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
    }

    override fun onResume() {
        super.onResume()
        if (itemPosition != -1) {
            rvAdapter.notifyDataSetChanged()
            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(itemPosition, 0)
        }

        listener.setTabNumber(tabBibleNumber)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setShowHideDonationLay(View.GONE) //Задаём видимость кнопке Поддержать

        listener.setBtnSelectTranslationVisibility(View.VISIBLE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)

        //Убираем текст выбранной главы Библии
        listener.setTvSelectedBibleText("", false)
    }

    override fun setSelectedBibleText(selectedText: String, isBook: Boolean) {
        listener.setTvSelectedBibleText(selectedText, isBook)
    }

    override fun createInfoDialog(bookInfo: BookModel) {
        bookInfoDialog = BookInfoDialog(this, bookInfo)
        bookInfoDialog.isCancelable = true
        bookInfoDialog.show(childFragmentManager, "Book Info Dialog") //Тут должен быть именно childFragmentManager
    }

    override fun dismissDialog() {
        bookInfoDialog.dismiss()
    }
}