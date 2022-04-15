package com.app.bible.knowbible.mvvm.view.fragment.bible_section

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.app.bible.knowbible.App
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.BibleTranslationModel
import com.app.bible.knowbible.mvvm.model.BookModel
import com.app.bible.knowbible.mvvm.model.DataToRestoreModel
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.TapTargetSelectTestamentFragment
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabBibleNumber
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.dialog.TestamentInfoDialog
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleTranslationsFragment.Companion.TRANSLATION_DB_FILE_JSON_INFO
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.daily_verse_subsection.DailyVerseFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.notes_subsection.NotesFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.search_subsection.SearchFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utility
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.muddzdev.styleabletoast.StyleableToast
import kotlinx.android.synthetic.main.fragment_select_testament.*

open class SelectTestamentFragment : Fragment(), DialogListener {
    private val activity by lazy { requireActivity() as MainActivity }

    lateinit var myFragmentManager: FragmentManager

    private lateinit var listener: IActivityCommunicationListener
    private lateinit var saveLoadData: SaveLoadData

    private lateinit var testamentInfoDialog: TestamentInfoDialog

    private var dataToRestoreModel: DataToRestoreModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView: View = inflater.inflate(R.layout.fragment_select_testament, container, false)
        listener.setTheme(
            ThemeManager.theme,
            false
        ) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        saveLoadData = SaveLoadData(requireContext())

        //Получаем данные, которые будут использованы для восстановления стека фрагментов в случае, когда человек закрыл приложение из диспетчера задач
        dataToRestoreModel = null
        if (saveLoadData.loadString(BibleTextFragment.DATA_TO_RESTORE) != null && saveLoadData.loadString(
                BibleTextFragment.DATA_TO_RESTORE
            )!!.isNotEmpty()
        ) {
            val gson = Gson()
            dataToRestoreModel = gson.fromJson(
                saveLoadData.loadString(BibleTextFragment.DATA_TO_RESTORE),
                DataToRestoreModel::class.java
            )
        }

        //!Utility.isTranslationsDownloaded() - Проверяем, если в папке, которая содержит БД с переводами Библии, пусто, то открываем фрагмент, в котором будет представлен список переводов. Пользователь должен скачать как минмум один перевод Библии, чтобы пользоваться приложением полноценно.
        //Если же хотя бы один перевод скачан, то в последующие разы будет открываться фрагмент выбора заветов (SelectTestamentFragment).
        if (!Utility.isTranslationsDownloaded(requireContext())) {
            openBibleTranslationsFragment()
        }
        //!Utility.isSelectedTranslationDownloaded() - Проверка на то, скачан ли перевод, выбранный ранее, или же перевод удалён и в saveLoadData хранится лишь имя скачанного файла, но его самого не существует.
        //Если эту проверку не осуществлять, то в случае удаления выбранного перевода, программа будет пытаться открыть его, но не сможет,
        //потому что в действительности он будет удалён
        else if (saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO) != null
            && saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO)!!.isNotEmpty()
            && !Utility.isSelectedTranslationDownloaded(
                requireContext(),
                Gson().fromJson(
                    saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO),
                    BibleTranslationModel::class.java
                )
            )
        ) {
            openBibleTranslationsFragment()
            StyleableToast.makeText(
                requireContext(),
                requireContext().getString(R.string.toast_select_translation),
                Toast.LENGTH_SHORT,
                R.style.my_toast
            ).show()
        }
        //Код, предназначенный для восстановления стэка фрагментов то ли после поворота экрана, то ли в случае, когда человек закрыл приложение из диспетчера задача
        else if (dataToRestoreModel != null && dataToRestoreModel!!.bookNumber != -1) {
            myFragmentManager.let {
                val matthewBookNumber =
                    470 //Это код книги Евангелие Матфея в Базе Данных. С помощью этого номера будет определяться, данные какого завета возвращать по запросу.

                val transaction: FragmentTransaction = it.beginTransaction()

                val selectBibleBookFragment = SelectBibleBookFragment()
                selectBibleBookFragment.setRootFragmentManager(myFragmentManager)
                selectBibleBookFragment.bookNumber =
                    dataToRestoreModel!!.bookNumber //Устанавливаем номер книг, чтобы выбрать нужную книгу при восстановлении стэка фрагментов
                selectBibleBookFragment.chapterNumber =
                    dataToRestoreModel!!.chapterNumber //Устанавливаем номер главы, чтобы выбрать нужную главу при восстановлении стэка фрагментов
                selectBibleBookFragment.isOldTestament =
                    dataToRestoreModel!!.bookNumber < matthewBookNumber //Устанавливаем значение, чтобы загрузило список книг нужного Завета
                transaction.replace(R.id.fragment_container_bible, selectBibleBookFragment)
                transaction.addToBackStack(null)
                transaction.commit()

                dataToRestoreModel!!.bookNumber =
                    -1 //Устанавливаем значение -1, чтобы при попытке вернуться на прежний фрагмент, пользователя снова не перебрасывало на уже открытый
            }
        } else {
            myFragmentManager.let {
                //По непонятно причине при смене языка и последующей попытке открыть какой-то фрагмент в данном разделе, выбивается ошибка FragmentManager has been destroyed
                //Решение стало переопределение поля myFragmentManager, назначив ему значение статического поля BIBLE_FRAGMENT_MANAGER,
                //которое содержит в себе значение нужного фрагмент менеджера
                if (it.isDestroyed)
                    myFragmentManager = BibleRootFragment.BIBLE_FRAGMENT_MANAGER

                val transaction: FragmentTransaction = myFragmentManager.beginTransaction()
                transaction.setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )

                //Открываем БД с ранее используемым переводом Библии и в кнопку выбора переводов устанавливаем аббревиатуру перевода, который был ранее выбран пользователем
                var oldTestamentBooksList: ArrayList<BookModel>
                var newTestamentBooksList: ArrayList<BookModel>

                val jsonBibleTranslationInfo =
                    saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO)
                if (jsonBibleTranslationInfo != null && jsonBibleTranslationInfo.isNotEmpty()) {
                    val gson = Gson()
                    val bibleTranslationInfo: BibleTranslationModel =
                        gson.fromJson(jsonBibleTranslationInfo, BibleTranslationModel::class.java)

                    //Проверка на то, скачан ли перевод, выбранный ранее, или же перевод удалён и в saveLoadData хранится имя скачанного файла, но его самого не существует.
                    //Если эту проверку не осуществлять, то в случае удаления выбранного перевода, программа будет пытаться открыть его, но не сможет,
                    //потому что в действительности он будет удалён
                    if (Utility.isSelectedTranslationDownloaded(
                            requireContext(),
                            bibleTranslationInfo
                        )
                    ) {
                        val bibleDataViewModel = activity.let {
                            ViewModelProvider(requireActivity())[BibleDataViewModel::class.java]
                        }
                        //Осуществляем предазгрузку книг Библии для большей производительности
                        bibleDataViewModel
                            .getAllBooksList(BibleDataViewModel.TABLE_BOOKS)
                            .observe(viewLifecycleOwner) { list ->
                                oldTestamentBooksList = ArrayList(
                                    list.subList(
                                        0,
                                        39
                                    )
                                ) //Отрезок с 1-й по 39-ую книги - это ВЗ
                                newTestamentBooksList = ArrayList(
                                    list.subList(
                                        39,
                                        66
                                    )
                                ) //Отрезок с 1-й по 39-ую книги - это НЗ

                                val selectBibleBookFragment = SelectBibleBookFragment()
                                selectBibleBookFragment.setRootFragmentManager(myFragmentManager)

                                //В случае если перевод скачан, но не выбран, при нажатии на кнопки завета отрывается фрагмент списка переводов, чтобы пользователь всё-таки выбрал перевод.
                                val btnOldTestament: MaterialCardView =
                                    myView.findViewById(R.id.btnOldTestament)
                                btnOldTestament.setOnClickListener {
                                    selectBibleBookFragment.isOldTestament = true
                                    selectBibleBookFragment.abbreviationTranslationName =
                                        bibleTranslationInfo.abbreviationTranslationName
                                    oldTestamentBooksList.let { booksList ->
                                        selectBibleBookFragment.setBooksList(
                                            booksList
                                        )
                                    }
                                    transaction.replace(
                                        R.id.fragment_container_bible,
                                        selectBibleBookFragment
                                    )
                                    transaction.addToBackStack(null)
                                    transaction.commit()
                                }

                                val btnNewTestament: MaterialCardView =
                                    myView.findViewById(R.id.btnNewTestament)
                                btnNewTestament.setOnClickListener {
                                    selectBibleBookFragment.isOldTestament = false
                                    selectBibleBookFragment.abbreviationTranslationName =
                                        bibleTranslationInfo.abbreviationTranslationName
                                    newTestamentBooksList.let { booksList ->
                                        selectBibleBookFragment.setBooksList(
                                            booksList
                                        )
                                    }
                                    transaction.replace(
                                        R.id.fragment_container_bible,
                                        selectBibleBookFragment
                                    )
                                    transaction.addToBackStack(null)
                                    transaction.commit()
                                }

                                val btnOldTestamentInfo: MaterialButton =
                                    myView.findViewById(R.id.btnOldTestamentInfo)
                                btnOldTestamentInfo.setOnClickListener {
                                    testamentInfoDialog = TestamentInfoDialog(this)
                                    testamentInfoDialog.testamentInfoIconRes =
                                        requireActivity().applicationContext.resources?.getIdentifier(
                                            "law",
                                            "drawable",
                                            requireActivity().applicationContext?.packageName
                                        )!! //С помощью метода getIdentifier достаём нужную картинку используя string имени конкретного поля в res.drawable
                                    testamentInfoDialog.testamentInfoText =
                                        getString(R.string.old_testament_info)
                                    testamentInfoDialog.show(
                                        childFragmentManager,
                                        "Testament Info Dialog"
                                    ) //Тут должен быть именно childFragmentManager
                                }

                                val btnNewTestamentInfo: MaterialButton =
                                    myView.findViewById(R.id.btnNewTestamentInfo)
                                btnNewTestamentInfo.setOnClickListener {
                                    testamentInfoDialog = TestamentInfoDialog(this)
                                    testamentInfoDialog.testamentInfoIconRes =
                                        requireActivity().applicationContext.resources?.getIdentifier(
                                            "gospels",
                                            "drawable",
                                            requireActivity().applicationContext?.packageName
                                        )!! //С помощью метода getIdentifier достаём нужную картинку используя string имени конкретного поля в res.drawable
                                    testamentInfoDialog.testamentInfoText =
                                        getString(R.string.new_testament_info)
                                    testamentInfoDialog.show(
                                        childFragmentManager,
                                        "Testament Info Dialog"
                                    ) //Тут должен быть именно childFragmentManager
                                }
                            }
                    } else {
                        openBibleTranslationsFragment()
                        StyleableToast.makeText(
                            requireContext(),
                            requireContext().getString(R.string.toast_select_translation),
                            Toast.LENGTH_SHORT,
                            R.style.my_toast
                        ).show()
                        //Вызываем здесь return, чтобы когда пользователь не выбрал ни одного перевода и нажимает кнопку назад,
                        //ему снова открывался фрагмент выбора переводов с просьбой выбрать конкретный перевод и без продолжения выполнения кода в методе.
                        //И когда срабатывают эти строки кода в блоке else, то код прекращается вызовом return,
                        //который не позволит дальнейшее выполнение кода в методе,
                        //которое может вызвать баг несвоевременного показа подсказок при первом запуске приложения.
                        return myView
                    }
                } else {
                    //Вызываем здесь return, чтобы когда пользователь не выбрал ни одного перевода и нажимает кнопку назад,
                    //ему снова открывался фрагмент выбора переводов с просьбой выбрать конкретный перевод и без продолжения выполнения кода в методе.
                    //И когда срабатывают эти строки кода в блоке else, то код прекращается вызовом return,
                    //который не позволит дальнейшее выполнение кода в методе,
                    //которое может вызвать баг несвоевременного показа подсказок при первом запуске приложения.
                    openBibleTranslationsFragment()
                    StyleableToast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.toast_select_translation),
                        Toast.LENGTH_SHORT,
                        R.style.my_toast
                    ).show()
                    return myView
                }

                val btnNotes: MaterialButton = myView.findViewById(R.id.btnNotes)
                btnNotes.setOnClickListener {
                    val notesFragment = NotesFragment()
                    notesFragment.setRootFragmentManager(myFragmentManager)
                    transaction.replace(R.id.fragment_container_bible, notesFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

                val btnDailyVerse: MaterialButton = myView.findViewById(R.id.btnDailyVerse)
                btnDailyVerse.setOnClickListener {
                    val dailyVerseFragment = DailyVerseFragment()
                    dailyVerseFragment.setRootFragmentManager(myFragmentManager)
                    transaction.replace(R.id.fragment_container_bible, dailyVerseFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

                val btnSearch: MaterialButton = myView.findViewById(R.id.btnSearch)
                btnSearch.setOnClickListener {
                    val searchFragment = SearchFragment()
                    searchFragment.setRootFragmentManager(myFragmentManager)
                    transaction.replace(R.id.fragment_container_bible, searchFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

                //Прописываем условие, чтобы этот код срабатывал только один раз
                if (!saveLoadData.loadBoolean(TapTargetSelectTestamentFragment)) {
                    //Помещаем код в Handler, потому что только так можно получить значение параметров высоты и ширины
                    val mainHandler = Handler(requireContext().mainLooper)
                    val myRunnable =
                        Runnable {
                            TapTargetSequence(activity)
                                .targets(
                                    Utility.getTapTargetButton(
                                        btnNotes,
                                        requireContext(),
                                        R.string.btn_notes_title,
                                        R.string.btn_notes_description,
                                        Utility.convertPxToDp(
                                            btnNotes.width.toFloat(),
                                            requireContext()
                                        ).toInt()
                                    ),
                                    Utility.getTapTargetButton(
                                        btnDailyVerse,
                                        requireContext(),
                                        R.string.btn_daily_verse_title,
                                        R.string.btn_daily_verse_description,
                                        Utility.convertPxToDp(
                                            btnDailyVerse.width.toFloat(),
                                            requireContext()
                                        ).toInt()
                                    ),
                                    Utility.getTapTargetButton(
                                        btnSearch,
                                        requireContext(),
                                        R.string.btn_search_title,
                                        R.string.btn_search_title_description,
                                        Utility.convertPxToDp(
                                            btnSearch.width.toFloat(),
                                            requireContext()
                                        ).toInt()
                                    ),
                                    Utility.getTapTargetButton(
                                        btnOldTestamentInfo,
                                        requireContext(),
                                        R.string.btn_testament_information_title,
                                        R.string.btn_testament_information_description,
                                        Utility.convertPxToDp(
                                            btnOldTestamentInfo.width.toFloat(),
                                            requireContext()
                                        ).toInt()
                                    )
                                )
                                .start()
                        }
                    mainHandler.post(myRunnable)
                    saveLoadData.saveBoolean(TapTargetSelectTestamentFragment, true)
                }
                App.instance.nativeAdLoader.loadNativeAd()
                App.instance.nativeAdLoader.nativeAdLiveData.observe(
                    viewLifecycleOwner
                ) { nativeAd ->
                    if (nativeAd == null) return@observe
                    App.instance.nativeAdLoader.showNativeAd(nativeAdFrame) //Показываем нативку
                }
            }
        }
        return myView
    }

    private fun openBibleTranslationsFragment() {
        val fragment = BibleTranslationsFragment()
        fragment.setRootFragmentManager(myFragmentManager)
        val transaction: FragmentTransaction = myFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        transaction.replace(R.id.fragment_container_bible, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //После очень долгого поиска решения, удалось осуществить нормальный поворот экрана в этом фрагменте, не менять этот метод и параметр android:configChanges в манифесте.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        myFragmentManager.let {
            val fragment = SelectTestamentFragment()
            fragment.setRootFragmentManager(it)
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_bible, fragment)
            transaction.commit()
        }
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(tabBibleNumber)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(false)

        listener.setShowHideDonationLay(View.VISIBLE) //Задаём видимость кнопке Поддержать

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.GONE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)

        //Убираем текст выбранной книги Библии
        listener.setTvSelectedBibleText("", true)
    }

    override fun dismissDialog() {
        testamentInfoDialog.dismiss()
    }
}