package com.app.bible.knowbible.mvvm.view.fragment.bible_section

import android.animation.Animator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.BibleTextModel
import com.app.bible.knowbible.mvvm.model.ChapterModel
import com.app.bible.knowbible.mvvm.model.DataToRestoreModel
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.isBackButtonClicked
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabBibleNumber
import com.app.bible.knowbible.mvvm.view.adapter.BibleTextRVAdapter
import com.app.bible.knowbible.mvvm.view.adapter.BibleTextRVAdapter.Companion.isMultiSelectionEnabled
import com.app.bible.knowbible.mvvm.view.adapter.ViewPager2Adapter
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.interpretations.BibleInterpretationFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.notes_subsection.NotesFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.search_subsection.SearchFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utils
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Field

class BibleTextFragment : Fragment(), IThemeChanger, ViewPager2Adapter.IFragmentCommunication,
    BibleTextRVAdapter.MultiSelectionPanelListener {
    companion object {
        const val DATA_TO_RESTORE = "DATA_TO_RESTORE"
        const val DATA_TO_SET = "DATA_TO_SET"
    }

    //Поле нужно в случае, когда была нажата кнопки btnHome в BottomAppBar и нам нужно очистить данные сохранённого скролла,
    //чтобы при очищении стэка фрагментов не открывался снова BibleTextFragment с ранее сохранёнными данными
    private var isBtnHomeClicked: Boolean = false
    private var isBtnNotesClicked: Boolean = false
    private var isBtnSearchClicked: Boolean = false

    private var isInterpretationOpened: Boolean = false
    private var isFullScreenInterpretationEnabled: Boolean = false
    //private var centerOfLayout: Int = 0

    private lateinit var btnCloseInterpretation: ImageView
    private lateinit var btnInterpretationFullScreen: ImageView
    private lateinit var btnExitInterpretationFullScreen: ImageView
    private lateinit var myDividerView: RelativeLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var interpretationLayout: LinearLayout
    private lateinit var coordinatorLayout: LinearLayout
    private lateinit var fragmentContainerInterpretationLay: AppBarLayout

    private lateinit var swipeListener: OnViewPagerSwipeStateListener
    private lateinit var listener: IActivityCommunicationListener

    private lateinit var saveLoadData: SaveLoadData
    private lateinit var bibleDataViewModel: BibleDataViewModel

    private lateinit var myFragmentManager: FragmentManager

    var isBibleTextFragmentOpenedFromSearchFragment: Boolean =
        false //Поле для реализации правильного взаимодействия между этим фрагментом и SearchFragment
    var isBibleTextFragmentOpenedFromAddEditNoteFragment: Boolean =
        false //Поле для реализации правильного взаимодействия между этим фрагментом и AddEditNoteFragment
    var isBibleTextFragmentOpenedFromHighlightedVersesFragment: Boolean =
        false //Поле для реализации правильного взаимодействия между этим фрагментом и HighlightedVersesFragment

    private var vpAdapter: ViewPager2Adapter? = null
    private lateinit var viewPager2: ViewPager2

    var chapterInfo: ChapterModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView: View = inflater.inflate(R.layout.fragment_bible_text, container, false)
        Utils.log("BibleTextFragment: onCreateView")
        listener.setTheme(
            ThemeManager.theme,
            false
        ) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        listener.setBibleTextFragment(this)

        saveLoadData = SaveLoadData(requireContext())

        bibleDataViewModel =
            activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!

        viewPager2 = myView.findViewById(R.id.viewPager2)

        myDividerView = myView.findViewById(R.id.myDividerView)
        progressBar = myView.findViewById(R.id.progressBar)

        interpretationLayout = myView.findViewById(R.id.interpretationLayout)
        coordinatorLayout = myView.findViewById(R.id.coordinatorLayout)
        fragmentContainerInterpretationLay =
            myView.findViewById(R.id.fragmentContainerInterpretationLay)

        btnCloseInterpretation = myView.findViewById(R.id.btnCloseInterpretation)
        btnCloseInterpretation.setOnClickListener {
            val allHeightOfLayout =
                coordinatorLayout.height - (interpretationLayout.height - 10) //Выставляем оптимальный отступ, учитывая высоту interpretationLayout
            val halfHeightOfLayout = coordinatorLayout.height / 2

            if (isFullScreenInterpretationEnabled) {
                hideInterpretationPanel(allHeightOfLayout)
            } else hideInterpretationPanel(halfHeightOfLayout)

            isInterpretationOpened = false
        }

        btnInterpretationFullScreen = myView.findViewById(R.id.btnInterpretationFullScreen)
        btnInterpretationFullScreen.setOnClickListener {
            val allHeightOfLayout =
                coordinatorLayout.height - (interpretationLayout.height - 10) //Выставляем оптимальный отступ, учитывая высоту interpretationLayout
            Utils.log("allHeightOfLayout: $allHeightOfLayout")

            val halfHeightOfLayout = coordinatorLayout.height / 2
            Utils.log("halfHeightOfLayout: $halfHeightOfLayout")

            isFullScreenInterpretationEnabled = true

            viewPager2.visibility = View.INVISIBLE
            viewPager2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))

            val animationZoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out)
            val animationZoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in)

            animationZoomOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    btnExitInterpretationFullScreen.startAnimation(animationZoomIn)
                    btnExitInterpretationFullScreen.visibility = View.VISIBLE

                    btnInterpretationFullScreen.clearAnimation()
                    btnInterpretationFullScreen.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            btnInterpretationFullScreen.startAnimation(animationZoomOut)
            Utils.slideView(
                fragmentContainerInterpretationLay,
                500,
                halfHeightOfLayout,
                allHeightOfLayout,
                true
            ).start()
        }

        btnExitInterpretationFullScreen = myView.findViewById(R.id.btnExitInterpretationFullScreen)
        btnExitInterpretationFullScreen.setOnClickListener {
            val allHeightOfLayout =
                coordinatorLayout.height - (interpretationLayout.height - 10) //Выставляем оптимальный отступ, учитывая высоту interpretationLayout
            val halfHeightOfLayout = coordinatorLayout.height / 2

            isFullScreenInterpretationEnabled = false

            viewPager2.visibility = View.VISIBLE
            viewPager2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))

            val animationZoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out)
            val animationZoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in)

            animationZoomOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    btnInterpretationFullScreen.startAnimation(animationZoomIn)
                    btnInterpretationFullScreen.visibility = View.VISIBLE

                    btnExitInterpretationFullScreen.clearAnimation()
                    btnExitInterpretationFullScreen.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            btnExitInterpretationFullScreen.startAnimation(animationZoomOut)

            Utils.slideView(
                fragmentContainerInterpretationLay,
                500,
                allHeightOfLayout,
                halfHeightOfLayout,
                true
            ).start()
        }

        return myView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnViewPagerSwipeStateListener) swipeListener = context
        else throw RuntimeException("$context must implement OnViewPagerSwipeStateListener")
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    //Поле для того, чтобы сравнивать какая тема и если тема меняется, то адаптер обновляется
    private var currentTheme = ThemeManager.theme
    override fun onResume() {
        super.onResume()
        Utils.log("BibleTextFragment: onResume")
        //Включаем свайп для листания глав Библии, отключая при этом свайп для навигации между табами
        swipeListener.setViewPagerSwipeState(false)

        //Сообщаем, что BibleTextFragment открыт, а это значит, что BottomAppBar можно показывать
        listener.setIsBibleTextFragmentOpened(true)

        //Открываем панель множественного выбора текста при возвращении в BibleTextFragment, при этом проверяя,
        //активирована ли эта панель через проверку значения поля isMultiSelectionEnabled.
        //Если она неактивирована, то и открывать не нужно (то есть задавать видимость)
        if (isMultiSelectionEnabled) openMultiSelectionPanel()

        listener.setTabNumber(tabBibleNumber)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        //Аннулируем здесь значение переменной isBackButtonClicked, потому что оно может установиться на true при нажатии кнопки "Назад" в других фрагментах
        //и при закрытии приложения через диспетчер задач данные сохраненного скролла всё равно будут стёрты,
        //потому что вызовется onPause, совершится проверка на то, была ли нажата кнопка назад и если там будет true, то данные скролла очистятся.
        //Ориентируясь на значение этой переменной, нужно очищать данные скролла только тогда,
        //когда кнопка "Назад" была нажата именно в открытом BibleTextFragment в табе "Библия", а не в каком-то другом табе(по этой причине здесь и присваивается ей значение false)
        //Потому что пользователь может нажать кнопку "Назад" в табе "Статьи", а данные сохранённого скролла Библии всё равно очистаться,
        //хотя пользователь этого естественно не хотел(Потому что этот фрагмент может быть открыт в фоне, если юзер его открыл и перешёл в другой таб).
        //Надеюсь, логика понятна... Вибачайте, якшо по молдавському
        isBackButtonClicked = false

        bibleDataViewModel
            .getBookShortName(
                BibleDataViewModel.TABLE_BOOKS,
                chapterInfo?.bookNumber!!
            )
            .observe(viewLifecycleOwner) { shortName ->
                listener.setTvSelectedBibleText("$shortName.", true)
            }

        if (vpAdapter != null && viewPager2.adapter != null && viewPager2.adapter!!.itemCount != 0) {
            vpAdapter!!.dataToRestoreData = DataToRestoreModel(
                chapterInfo?.bookNumber!!,
                chapterInfo?.chapterNumber!! - 1
            ) //Поскольку счёт в коллекции начинается с 0, а главы начинаются с 1, то нужно номер главы минусовать. Также тут отправляем данные, чтобы сравнить и выяснить, тот ли отображён текст, данные скролла которого были сохранены ранее
//            viewPager2.setCurrentItem(page, false) //Отключаем анимацию слайда именно при выборе главы, чтобы она сразу включалась без анимации,
            //потому что с анимацией в таком случае глава открывается будто бы с пролагом.
            //Анимируется только когда пользователь делает слайд влево или вправо, чтобы листать главы

            val vpPage = chapterInfo?.chapterNumber!! - 1
            //Обновляем адаптер, чтобы при смене темы все айтемы обновились и устанавливалась нужная позиция скролла
            if (currentTheme != ThemeManager.theme) {
                viewPager2.adapter!!.notifyDataSetChanged()
                currentTheme = ThemeManager.theme
                setScroll(vpPage, false)
            } else setScroll(vpPage, false)
        } else {
            //Код для преобразования текстов в БД НИ В КОЕМ СЛУЧАЕ НЕ УДАЛЯТЬ!
//            bibleDataViewModel
//                    .getBibleTextOfAllBible(BibleDataViewModel.TABLE_VERSES)
//                    .observe(viewLifecycleOwner, Observer { allBibleTexts ->
//                        val mainHandler = Handler(context!!.mainLooper)
//                        val myRunnable = Runnable {
//                            progressBar.visibility = View.VISIBLE
//
//                            allBibleTexts?.forEachIndexed { indexBible, text ->
//                                bibleDataViewModel.updateBibleTextInDB(text)
//                                Utility.log("Verse of book: " + text.book_number + ", " + text.chapter_number + ":" + text.verse_number + " is changed")
//                                if (indexBible == allBibleTexts.size - 1) {
//                                    progressBar.visibility = View.GONE
//                                    Utility.log("Verses changing of ALL BIBLE in DB is finished!")
//                                }
//                            }
//                        }
//                        mainHandler.post(myRunnable)
//                    })

            bibleDataViewModel
                .getBibleTextOfBook(BibleDataViewModel.TABLE_VERSES, chapterInfo?.bookNumber!!)
                .observe(viewLifecycleOwner, Observer { bookTexts ->
                    if (isBibleTextFragmentOpenedFromHighlightedVersesFragment) {
                        (activity as MainActivity).tabLayout.visibility = View.GONE
                        viewPager2.isUserInputEnabled = false
                    }

                    //Если панель толкования была открыта до перехода в другой фрагмент, то при возвращении на данный фрагмент из другого фрагмента мы её снова открываем
                    if (isInterpretationOpened) {
                        interpretationLayout.clearAnimation()
                        interpretationLayout.visibility =
                            View.VISIBLE //При открытии панели толкования, задаём видимость VISIBLE лейауту с кнопками

                        myFragmentManager.let {
                            val myFragment = BibleInterpretationFragment()
                            myFragment.setRootFragmentManager(myFragmentManager)

                            val transaction: FragmentTransaction = it.beginTransaction()
                            transaction.replace(R.id.fragmentContainerInterpretation, myFragment)
                            transaction.commit()
                        }

                        if (isFullScreenInterpretationEnabled) {
                            viewPager2.visibility = View.INVISIBLE

                            btnExitInterpretationFullScreen.visibility = View.VISIBLE
                            btnInterpretationFullScreen.visibility = View.GONE

                            val allHeightOfLayout =
                                coordinatorLayout.height - (interpretationLayout.height - 10) //Выставляем оптимальный отступ, учитывая высоту interpretationLayout
                            fragmentContainerInterpretationLay.layoutParams =
                                LinearLayout.LayoutParams(MATCH_PARENT, allHeightOfLayout)
                        } else {
                            val halfHeightOfLayout = coordinatorLayout.height / 2
                            fragmentContainerInterpretationLay.layoutParams =
                                LinearLayout.LayoutParams(MATCH_PARENT, halfHeightOfLayout)
                        }
                        fragmentContainerInterpretationLay.requestLayout()
                    }

                    vpAdapter = ViewPager2Adapter(requireContext(), bookTexts, myFragmentManager)
                    vpAdapter!!.setRecyclerViewThemeChangerListener(this)
                    vpAdapter!!.setIFragmentCommunicationListener(this)
                    vpAdapter!!.setMultiSelectionPanelListener(this)

                    vpAdapter!!.dataToRestoreData = DataToRestoreModel(
                        chapterInfo?.bookNumber!!,
                        chapterInfo?.chapterNumber!! - 1
                    ) //Поскольку счёт в коллекции начинается с 0, а главы начинаются с 1, то нужно номер главы минусовать. Также тут отправляем данные, чтобы сравнить и выяснить, тот ли отображён текст, данные скролла которого были сохранены ранее

                    viewPager2.adapter = vpAdapter
                    val vpPage = chapterInfo?.chapterNumber!! - 1
                    viewPager2.setCurrentItem(
                        vpPage,
                        false
                    ) //Отключаем анимацию слайда именно при выборе главы, чтобы она сразу включалась без анимации,
                    //потому что с анимацией в таком случае глава открывается будто бы с пролагом.
                    //Анимируется только когда пользователь делает слайд влево или вправо, чтобы листать главы

                    if (isBibleTextFragmentOpenedFromSearchFragment || isBibleTextFragmentOpenedFromAddEditNoteFragment || isBibleTextFragmentOpenedFromHighlightedVersesFragment) {
                        val verseNumberForScroll = chapterInfo?.verseNumber
                        setScroll(vpPage, verseNumberForScroll!!, true)
                    } else setScroll(vpPage, false)
                })
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                chapterInfo?.chapterNumber =
                    position //Сохраняем выбранную страницу, чтобы сохранить её в случае закрытия приложения
                vpAdapter!!.dataToRestoreData = DataToRestoreModel(
                    chapterInfo?.bookNumber!!,
                    chapterInfo?.chapterNumber!!
                ) //Отправляем данные, чтобы сравнить и выяснить, тот ли отображён текст, данные скролла которого были сохранены ранее
                Utils.log("BibleTextFragment: onPageSelected")

                val chapterNumber = position + 1
                chapterInfo!!.chapterNumber = chapterNumber
                listener.setTvSelectedBibleText(chapterNumber.toString(), false)

                if (isMultiSelectionEnabled) {
                    //Отключаем режим множественного выбора при переходе на другую главу
                    listener.disableMultiSelection()

                    //Обновляем recyclerView предыдущей и предстоящей главы,
                    //чтобы при отключении режима множественного выбора выбранные(выделенные) айтема обновились на не выбранные(не выделенные)
                    vpAdapter?.getRecyclerView(chapterInfo!!.chapterNumber - 2)?.adapter?.notifyDataSetChanged()
                    vpAdapter?.getRecyclerView(chapterInfo!!.chapterNumber)?.adapter?.notifyDataSetChanged()
                }
            }
        })

        //Задаём видимость кнопке только в случае включённого режима множественного выбора, потому что при включенном режиме,
        //при открытии этого фрагмента в 170 строке срабатывает метод openMultiSelectionPanel(),
        //в котором кнопке выбора переводов задаётся видимость
        if (!isMultiSelectionEnabled)
            listener.setBtnSelectTranslationVisibility(View.VISIBLE)

        listener.setShowHideDonationLay(View.GONE) //Задаём видимость кнопке Поддержать

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.VISIBLE)
    }

    private val dataToRestore: DataToRestoreModel = DataToRestoreModel(-1, -1, -1)
    override fun onPause() {
        super.onPause()
        Utils.log("BibleTextFragment: onPause")
        //Включаем свайп для навигации между табами, отключая при этом свайп для листания глав Библии
        swipeListener.setViewPagerSwipeState(true)

        (activity as MainActivity).closeMenuFromBibleTextFragment()
        //Сообщаем, что BibleTextFragment закрыт, а это значит, что BottomAppBar нельзя показывать
        listener.setIsBibleTextFragmentOpened(false)

        //Закрываем панель множественного выбора текста при переходе уходе из BibleTextFragment, при этом проверяя,
        //активирована ли эта панель через проверку значения поля isMultiSelectionEnabled.
        //Если она неактивирована, то и закрывать не нужно (то есть задавать видимость)
        if (isMultiSelectionEnabled) closeMultiSelectionPanel()

        //Если нажата кнопка btnHome, очищающая стэк фрагментов, то просто выходим из этого метода,
        //не выполняя дальнейший код, потому что данные сохранённого стэка уже очищены в методе btnHomeClicked()
        if (isBtnHomeClicked) return

        //Если BibleTextFragment открыт из SearchFragment с целью просто открыть текст с выбранным найденным в SearchFragment текстом,
        //то при возврате назад нет нужды очищать данные скролла и вообще производить какую-либо работу по сохранению данных скролла
        if (!isBibleTextFragmentOpenedFromAddEditNoteFragment && !isBibleTextFragmentOpenedFromSearchFragment && !isBibleTextFragmentOpenedFromHighlightedVersesFragment) {
            val jsonScrollData = saveLoadData.loadString(DATA_TO_RESTORE)

            val dataToRestoreJson =
                //Если кнопка "Назад" нажата, то очищаем сохранённые данные скролла
                if (isBackButtonClicked) {
                    //Очищаем сохранённые данные скролла, чтобы они не восстанавливались после того, как пользователь сам закрыл страницу текста кнопкой "Назад".
                    //Данные сохраняются только в том случае, когда человек вышел из приложения, будучи до этого в фрагменте текста Библии,
                    //чтобы потом, когда он войдёт снова, ему отобразились данные на том скролле, на котором они были до выхода приложения.
                    Gson().toJson(
                        DataToRestoreModel(
                            -1,
                            -1,
                            -1
                        )
                    )
                } else if (dataToRestore.bookNumber != -1 &&
                    dataToRestore.chapterNumber != -1 &&
                    dataToRestore.scrollPosition != -1
                ) {
                    Gson().toJson(
                        DataToRestoreModel(
                            chapterInfo!!.bookNumber,
                            chapterInfo!!.chapterNumber,
                            vpAdapter!!.getScrollPosition(chapterInfo!!.chapterNumber - 1)
                        )
                    )
                }
                //UPD: Этот блок else if взаимозаменяем с верхним блоком else if, поэтому он не нужен, но на всякий случай закомментирован, вдруг что
                //
                //Если пользователь ничего не скроллил, но экран открыт уже на какой-то ранее сохранённое позиции,
                //и пользователь захотел сразу перейти куда-то в другой фрагмент(например BibleTranslationsFragment),
                //то сохраняем ныне восстановленные данные скролла, чтобы отобразить при возврате на этот фрагмент
//                    else if (jsonScrollData != null && jsonScrollData.isNotEmpty() && Gson().fromJson(jsonScrollData, DataToRestoreModel::class.java).scrollPosition != -1) {
//                        Gson().toJson(DataToRestoreModel(
//                                chapterInfo!!.bookNumber,
//                                chapterInfo!!.chapterNumber, //Если данные скролла не меняются и всё остаётся без изменений, то увеличивать номер главы на 1 не нужно, потому что всё уже как надо.
//                                vpAdapter!!.getScrollPosition(chapterInfo!!.chapterNumber - 1))) //Получаем нынешнюю позицию скролла
//                    }
                //Если пользователь ничего не скроллил, то просто сохраняем выбранную книгу и главу, чтобы при повороте отобразить
                else {
                    Gson().toJson(
                        DataToRestoreModel(
                            chapterInfo!!.bookNumber,
                            chapterInfo!!.chapterNumber, //Если данные скролла не меняются и всё остаётся без изменений, то увеличивать номер главы на 1 не нужно, потому что всё уже как надо.
                            0
                        )
                    )
                }
            saveLoadData.saveString(
                DATA_TO_RESTORE,
                dataToRestoreJson
            )  //Сохранять данные тогда, когда фрагмент выходит из видимости
        }
    }

    fun notifyDataSetChanged() {
        vpAdapter?.getRecyclerView(chapterInfo!!.chapterNumber - 1)?.adapter?.notifyDataSetChanged()
    }

    fun getCurrentRecyclerViewAdapter(): BibleTextRVAdapter {
        return vpAdapter?.getRecyclerView(chapterInfo!!.chapterNumber - 1)?.adapter as BibleTextRVAdapter
    }

    fun notifyDataSetChangedVP() {
        vpAdapter?.notifyDataSetChanged()
        //Задержка нужна для того, чтобы скролл устанавливался после того, как сработает onBindViewHolder, чтобы onBindViewHolder не сбросил установленный скролл
        //А сам скролл мы осуществляем из-за того, что при смене размера шрифта, скролл сбивается и текст скроллится к началу главы, поэтому осуществляем скролл,
        //чтобы выставить тот номер текста, на котором находился пользователь в тот момент, когда начал менять шрифт
        GlobalScope.launch(Dispatchers.Main) {
            delay(30)
            (vpAdapter?.getRecyclerView(chapterInfo!!.chapterNumber - 1)?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                dataToRestore.scrollPosition,
                0
            )
        }
    }


    override fun onStop() {
        super.onStop()
        //Если фрагмент открыт из фрагмента HighlightedVersesFragment, то прописываем условия показа TabLayout,
        //чтобы при открытии выделенного текста нельзя было снова перейти в Таб Ещё и бесконечно открывать фрагменты с выделенными текстами.
        //Скрывая TabLayout, данная проблема решается.
        if (isBibleTextFragmentOpenedFromHighlightedVersesFragment) {
            if (isBackButtonClicked) {
                (activity as MainActivity).tabLayout.visibility = View.VISIBLE
                //Использование метода currentItem работает корректно только в Handler, иначе возникает баг - закрывается SelectTestamentFragment,
                //если он находится в стеке ниже данного открытого фрагмента, который был открыт из HighlightedVersesFragment
                Handler().post { (activity as MainActivity).viewPager.currentItem = 2 }
            } else if (isBtnHomeClicked) {
                (activity as MainActivity).tabLayout.visibility = View.VISIBLE
            }
        }

        //Если фрагмент закрывается и режим множественного выбора включён, то отключаем режим множественного выбора текстов
        //Закрытие режима множественного выбора происходит по разному в случае нажатия кнопок btnHome, btnNotes, btnSearch в BottomAppBar и в других случаях,
        //потому и вызываются 2 разных метода в разных случаях. В случае, когда нажаты кнопки btnHome, btnNotes, btnSearch,
        //вызывается метод disableMultiSelectionIfBottomAppBarBtnClicked(), а если фрагмент закрывается любым другим способом, то вызывается просто disableMultiSelection()
        //P.S. Более точный комментарий дан в самом методе disableMultiSelectionIfBottomAppBarBtnClicked
        if ((isBtnHomeClicked && isMultiSelectionEnabled) || (isBtnNotesClicked && isMultiSelectionEnabled) || (isBtnSearchClicked && isMultiSelectionEnabled)) {
            listener.disableMultiSelectionIfBottomAppBarBtnClicked()
        } else if (isMultiSelectionEnabled) listener.disableMultiSelection()

        Utils.log("BibleTextFragment: onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Utils.log("BibleTextFragment: onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.log("BibleTextFragment: onDestroy")
        isBtnHomeClicked = false
        isBtnNotesClicked = false
        isBtnSearchClicked = false
        isBackButtonClicked
    }

    override fun onDetach() {
        super.onDetach()
        Utils.log("BibleTextFragment: onDetach")
    }

    //Задержка нужна для того, чтобы скролл устанавливался после того, как сработает onBindViewHolder, чтобы onBindViewHolder не сбросил установленный скролл
    private fun setScroll(page: Int, smoothScroll: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(50)

            vpAdapter!!.scrollTo(page, smoothScroll)
        }
    }

    //Перегруженный метод, предназначенный для скролла к выбранному найденному стиху, когда BibleTextFragment был открыт из SearchFragment
    private fun setScroll(page: Int, verseNumberForScroll: Int, smoothScroll: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(70)
            vpAdapter!!.scrollTo(page, verseNumberForScroll, smoothScroll)
        }
    }

    //Уменьшаем чувствительность сенсора к свайпам. Вообще непонятный код)) , нашёл тут, в комманетариях под постом, потому что пост непонятный https://medium.com/@al.e.shevelev/how-to-reduce-scroll-sensitivity-of-viewpager2-widget-87797ad02414
    private fun reduceDragSensitivity(viewPager2: ViewPager2) {
        try {
            val ff: Field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            ff.isAccessible = true
            val recyclerView = ff.get(viewPager2) as RecyclerView
            val touchSlopField: Field = RecyclerView::class.java.getDeclaredField("mTouchSlop")
            touchSlopField.isAccessible = true
            val touchSlop = touchSlopField.get(recyclerView) as Int
            touchSlopField.set(recyclerView, touchSlop * 4)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    fun reduceDragSensitivity() {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop * 8) // "8" was obtained experimentally
    }

    interface OnViewPagerSwipeStateListener {
        fun setViewPagerSwipeState(viewPagerSwipeState: Boolean)
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun changeItemTheme() {
        listener.setTheme(
            ThemeManager.theme,
            false
        ) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение
    }

    override fun saveScrollPosition(bookNumber: Int, chapterNumber: Int, scrollPosition: Int) {
        dataToRestore.bookNumber = bookNumber
        dataToRestore.chapterNumber = chapterNumber
        dataToRestore.scrollPosition = scrollPosition
    }

    override fun getTranslationNameName(): String {
        //Берём обозначение перевода Библии из поля btnSelectTranslation,
        //потому что оно уникально для каждого перевода и позволяет обозначить к какому переводу принадлежит выделяемый текст
        return (activity as MainActivity).tvSelectTranslation.text.toString()
    }

    fun btnFABFullScreenClicked(isFullScreenEnabled: Boolean, heightForChanging: Int) {
        //Реализация высоты панели толкования при смене режима экрана на FullScreen и обратно
        //Ничего не менять, всё реализовано должным образом как нужно
        //Число 45 это примерный отступ, обеспечивающий возвращение панели при отключении режима FullScreen на место, в котором она была до включённого режима
        if (isInterpretationOpened) {
            if (isFullScreenEnabled) {
                if (isFullScreenInterpretationEnabled)
                    Utils.slideView(
                        fragmentContainerInterpretationLay,
                        300,
                        fragmentContainerInterpretationLay.height,
                        (coordinatorLayout.height + heightForChanging) - (interpretationLayout.height - 10),
                        true
                    ).start()
                else Utils.slideView(
                    fragmentContainerInterpretationLay,
                    300,
                    fragmentContainerInterpretationLay.height,
                    coordinatorLayout.height / 2 + (interpretationLayout.height + 45),
                    true
                ).start()
            } else {
                if (isFullScreenInterpretationEnabled)
                    Utils.slideView(
                        fragmentContainerInterpretationLay,
                        300,
                        fragmentContainerInterpretationLay.height,
                        (fragmentContainerInterpretationLay.height - heightForChanging) - (interpretationLayout.height - 25),
                        true
                    ).start()
                else Utils.slideView(
                    fragmentContainerInterpretationLay,
                    300,
                    fragmentContainerInterpretationLay.height,
                    coordinatorLayout.height / 2 - (interpretationLayout.height + 45),
                    true
                ).start()
            }
        }
    }

    fun btnHomeClicked() {
        isBtnHomeClicked = true

        //Очищаем сохранённые данные скролла, чтобы при очищении стэка вновь не открывался BibleTextFragment с ранее сохранёнными данными скрола
        saveLoadData.saveString(DATA_TO_RESTORE, Gson().toJson(DataToRestoreModel(-1, -1, -1)))

        myFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun btnInterpretationClicked() {
        val halfHeightOfLayout = coordinatorLayout.height / 2

        if (!isInterpretationOpened) {
            interpretationLayout.clearAnimation()
            interpretationLayout.visibility =
                View.VISIBLE //При открытии панели толкования, задаём видимость VISIBLE лейауту с кнопками

            myFragmentManager.let {
                val myFragment = BibleInterpretationFragment()
                myFragment.setRootFragmentManager(myFragmentManager)

                val transaction: FragmentTransaction = it.beginTransaction()
                transaction.replace(R.id.fragmentContainerInterpretation, myFragment)
                transaction.commit()
            }

            Utils.slideView(fragmentContainerInterpretationLay, 500, 0, halfHeightOfLayout, true)
                .start()

            isInterpretationOpened = true
        } else {
            hideInterpretationPanel(halfHeightOfLayout)

            isInterpretationOpened = false
        }
    }

    private fun hideInterpretationPanel(heightOfLayout: Int) {
        if (viewPager2.visibility == View.INVISIBLE) {
            viewPager2.visibility = View.VISIBLE
            viewPager2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
        }

        val animation =
            Utils.slideView(fragmentContainerInterpretationLay, 500, heightOfLayout, 0, false)
        animation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                //Задаём исходные данные, чтобы не происходило багов из-за оставленных ранее по разному использованных кнопок
                btnExitInterpretationFullScreen.visibility = View.GONE
                btnExitInterpretationFullScreen.clearAnimation()
                btnInterpretationFullScreen.visibility = View.VISIBLE
                btnInterpretationFullScreen.clearAnimation()
                isFullScreenInterpretationEnabled = false

                interpretationLayout.visibility =
                    View.GONE //После закрытия панели толкования, задаём видимость GONE лейауту с кнопками
                interpretationLayout.clearAnimation()

                viewPager2.clearAnimation()
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
        animation.start()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Чтобы высота в обеих ориентациях устанавливалась правильно, сначало нужно устанавливать ей 0, а потом нужную высоту
        //Если не делать это таким образом, то в горизонтальной ориентации высота будет сбиваться
        fragmentContainerInterpretationLay.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0)
        fragmentContainerInterpretationLay.requestLayout()

        //Поскольку высота лейаута меняется в зависимости от ориентации экрана, нужно задавать обновлённые данные для правильного расположения экрана толкования
        //А задержка нужна по той причине, что именно после неё мы получаем данные длины лейаута уже отображённой ориентации экрана,
        //а не той, которая была до поворота экрана, как это происходит если не делать задержку
        val mainHandler = Handler(requireContext().mainLooper)
        val myRunnable = Runnable {
            GlobalScope.launch(Dispatchers.Main) {
                delay(100)
                if (isInterpretationOpened) {
                    val heightOfLayout: Int =
                        //Выставляем оптимальный отступ, учитывая высоту interpretationLayout
                        if (isFullScreenInterpretationEnabled) coordinatorLayout.height - (interpretationLayout.height - 10)
                        else coordinatorLayout.height / 2

                    Utils.log("heightOfLayout: $heightOfLayout")
                    val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, heightOfLayout)
                    fragmentContainerInterpretationLay.layoutParams = layoutParams
                    fragmentContainerInterpretationLay.requestLayout()
                }
            }
        }
        mainHandler.post(myRunnable)
    }


    fun btnNotesClicked() {
        isBtnNotesClicked = true

        //Если BibleTextFragment открыт из AddEditNoteFragment, то при повторном нажатии на btnNotes произойдёт возврат назад на экран заметки,
        //вместо того, чтобы открывать AddEditNoteFragment по новой
        if (isBibleTextFragmentOpenedFromAddEditNoteFragment) {
            myFragmentManager.popBackStack()
            return
        }

        val transaction: FragmentTransaction = myFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )

        val notesFragment = NotesFragment()
        notesFragment.setRootFragmentManager(myFragmentManager)
        transaction.replace(R.id.fragment_container_bible, notesFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun btnSearchClicked() {
        isBtnSearchClicked = true

        //Если BibleTextFragment открыт из SearchFragment, то при повторном нажатии на btnSearch произойдёт возврат назад на экран поиска,
        //вместо того, чтобы открывать SearchFragment по новой
        if (isBibleTextFragmentOpenedFromSearchFragment) {
            myFragmentManager.popBackStack()
            return
        }

        val transaction: FragmentTransaction = myFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )

        val searchFragment = SearchFragment()
        searchFragment.setRootFragmentManager(myFragmentManager)
        transaction.replace(R.id.fragment_container_bible, searchFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun openMultiSelectionPanel() {
        listener.setShowHideMultiSelectionPanel(true)
    }

    override fun closeMultiSelectionPanel() {
        listener.setShowHideMultiSelectionPanel(false)
    }

    override fun sendDataToActivity(multiSelectedTextsList: ArrayList<BibleTextModel>) {
        listener.sendMultiSelectedTextsData(multiSelectedTextsList)
    }
}