package com.app.bible.knowbible.mvvm.view.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.app.bible.knowbible.App
import com.app.bible.knowbible.R
import com.app.bible.knowbible.data.local.HighlightedBibleTextInfoDBHelper
import com.app.bible.knowbible.data.local.NotesDBHelper
import com.app.bible.knowbible.mvvm.model.BibleTextModel
import com.app.bible.knowbible.mvvm.model.BibleTranslationModel
import com.app.bible.knowbible.mvvm.model.NoteModel
import com.app.bible.knowbible.mvvm.view.adapter.BibleTextRVAdapter.Companion.isMultiSelectionEnabled
import com.app.bible.knowbible.mvvm.view.adapter.BibleTranslationsRVAdapter
import com.app.bible.knowbible.mvvm.view.adapter.ViewPagerAdapter
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.dialog.AddVerseNoteDialog
import com.app.bible.knowbible.mvvm.view.dialog.ArticlesInfoDialog
import com.app.bible.knowbible.mvvm.view.dialog.ColorPickerDialog
import com.app.bible.knowbible.mvvm.view.fragment.articles_section.ArticlesRootFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleRootFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleTranslationsFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleTranslationsFragment.Companion.TRANSLATION_DB_FILE_JSON_INFO
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.notes_subsection.AddEditNoteFragment
import com.app.bible.knowbible.mvvm.view.fragment.more_section.AppLanguageFragment
import com.app.bible.knowbible.mvvm.view.fragment.more_section.MoreRootFragment
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.BOOK_THEME
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.DARK_THEME
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.LIGHT_THEME
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.THEME_NAME_KEY
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.app.bible.knowbible.push.PushCreator
import com.app.bible.knowbible.push.UnlockBR
import com.app.bible.knowbible.push.accessory.PUSH_ID_ARTICLES
import com.app.bible.knowbible.push.accessory.PUSH_KEY
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utils
import com.app.bible.knowbible.utility.Utils.Companion.convertDpToPx
import com.app.bible.knowbible.utility.Utils.Companion.getCurrentTime
import com.app.bible.knowbible.utility.Utils.Companion.viewAnimatorX
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.muddzdev.styleabletoast.StyleableToast
import eightbitlab.com.blurview.RenderScriptBlur
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.abs

class MainActivity : AppCompatActivity(), BibleTextFragment.OnViewPagerSwipeStateListener,
    IActivityCommunicationListener, AppLanguageFragment.IAppLanguageChangerListener, DialogListener,
    ColorPickerDialog.ColorPickerDialogListener {
    /* ОБЪЯСНЕНИЕ НАЛИЧИЯ ЭТОГО КОДА.
        *       Изначально проблема заключалась в том, что для всех фрагментов, даже если они расположены в отдельных табах, по умолчанию идёт один backStack.
        * То есть, если, к примеру, в первом табе перейти из Фрагмент1 в Фрагмент2, добавив при этом Фрагмент2 в backStack(методом addToBackStack(null)),
        * а потом перейти во второй таб, где открыть только один фрагмент, и нажать кнопку "Назад"(при этом предполагая, что по логике приложение закроется),
        * то приложение не закроется, но вместо этого из backStack будет удалён Фрагмент2 из первого таба, и если мы перейдём обратно в первый таб, то увидим, что Фрагмент2 закрыт.
        * Другими словами, в первом табе мы перешли из Фрагмент1 в Фрагмент2, затем открыли второй таб, нажимаем кнопку "Назад", визуально перед пользователем ничего не происходит
        * (а в действительности в первом табе закрывается Фрагмент2, но пользователь этого не видит, потому что перед ним открыт второй таб)
        * и чтобы всё же закрыть приложение, нужно снова нажать кнопку "Назад".
        *       Решением этой проблемы стала реализация отдельного backStack для каждого таба, но реализовано это с некоторыми нюансами.
        * Сделано это вот каким образом: в каждом табе сделан первичный главный Фрагмент(RootFragment), и вместо того,
        * чтобы во всех фрагментах использовать общий FragmentManager(как это обычно делается), в данном случае в главном фрагмент каждого отдельного таба вызывается метод childFragmentManager.
        * Этот childFragmentManager в дальнейшем передаётся во всех последующие фрагменты, которые содержаться в главном фрагменте.
        * И когда в каждом отдельном табе используется свой childFragmentManager, то и backStack у каждого свой, потому что backStack мы получаем из childFragmentManager.
        * Но в этом решении есть нюанс — по непонятной причине, вызывая addToBackStack(null) фрагменты добавляются в backStack, но, нажимая кнопку "Назад",
        * фрагменты не закрываются по тому порядку, в котором они находятся в backStack, вместо этого приложение вовсе закрывается.
        * И чтобы решить эту проблему, пришлось использовать static поля и переопределять метод onBackPressed() в данном активити.
        * Поле var isBackStackNotEmpty: Boolean нужно для того, чтобы указать, есть ли фрагменты в backStack или их нет. Значение в это поле устанавливается в каждом фрагменте.
        * Указывается оно в методе фрагмента onResume() (Ни в каком другом, потому что onResume() вызывается сразу как открывается таб,
        * а нам как раз нужно устанавливать значение каждого таба как только он открыт, потому что onCreate, к примеру, срабатывает только при создании фрагмента, а не при каждом его открытии).
        * Если backStack пустой, то при нажатии "Назад" приложение закроется, если же backStack содержит фрагменты, то сработает код с использованием поля var myFragmentManager: FragmentManager.
        * Поле var myFragmentManager: FragmentManager нужно для того, чтобы присваивать ему childFragmentManager каждого отдельного RootFragment и таким образом очищать стэк в каждом табе поотдельности.
        * В конечном итоге логика работы такова: мы устанавливаем значение true в поле var isBackStackNotEmpty: Boolean в фрагменте,
        * который нужно удалить из backStack, закрывая его кнопкной "Назад" и присваиваем в поле myFragmentManager тот childFragmentManager, в фрагментах которого мы находимся.
        * В override методе onBackPressed() присходит проверка: если isBackStackNotEmpty true,
        * то вызывается метод удаления фрагмента из backStack. А вызывается метод удаления фрагмента в том childFragmentManager, который отправлен в поле myFragmentManager.
        * Если же isBackStackNotEmpty == false, то срабатывает стандартный код системного метода onBackPressed() и приложение просто закрывается.
        * Это единственный удобный способ без кучи кода, который я сам и придумал, перерыв до этого весь интернет по этому вопросу, официальной версии решения этого вопроса нет.*/

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var isBackStackNotEmpty: Boolean = false
    private lateinit var myFragmentManager: FragmentManager
    private var isTranslationDownloaded: Boolean =
        true //Поле сугубо для BibleLanguageFragment, которое нужно, чтобы указывать скачан перевод или нет. Если не скачан, то при нажатии кнопки назад приложение закрывается, если же перевод есть, то при нажатии назад будет открыт предыдущий фрагмент.

    companion object {
        var isBackButtonClicked: Boolean =
            false //Поле сугубо для BibleTextFragment. Оно нужно, чтобы с помощью его значения при закрытии этого фрагмента кнопкой "Назад" очищать данные скролла.
        const val TEXT_SIZE_KEY = "TEXT_SIZE_KEY"

        const val tabArticlesNumber: Int = 0
        const val tabBibleNumber: Int = 1
        const val tabMoreNumber: Int = 2

        //Список ключей SharedPreferences для показа подсказок
        const val TapTargetSelectTestamentFragment = "TapTargetSelectTestamentFragment"
        const val TapTargetMoreFragment = "TapTargetMoreFragment"
        const val TapTargetSelectBibleBookFragment = "TapTargetSelectBibleBookFragment"
        const val TapTargetArticles = "TapTargetArticles"
    }

    private var currentTabNumber: Int =
        1 //Номер таба нужен, чтобы при повороте экрана в вертикальное положение, устанавливать выделенную иконку в том табе, который выбран
    //По умолчанию номер таба 1, это чтобы при открытии приложения, выделялась иконка таба Библия

    private lateinit var noteData: NoteModel
    private var isTabBibleSelected =
        true //Значение по умолчанию должно быть true, так как приложение в любом случае открывается в табе "Библия".
    //При этом по непонятной причине при запуске приложения не срабатывает addOnTabSelectedListener, а только после переключения по табам, потому,
    //опять же, выходом является установать значение true, а уже при переключении по табам значение этого поля будет меняться

    private lateinit var bibleTextFragment: BibleTextFragment //Объект необходим для управления BottomAppBar в BibleTextFragment
    private var isBibleTextFragmentOpened: Boolean = false

    private var isFullScreenEnabled: Boolean = false
    private var isChangeFontSizeEnabled: Boolean = false

    private lateinit var multiSelectedTextsList: ArrayList<BibleTextModel> //Список данных необходим для обработки двух и более выбранных текстов Библии в режиме мульти выбора
    private var isMultiSelectedTextsListContainsHighlightedVerse: Boolean =
        false //Поле нужно для определения того, содержится ли хотя бы один выделенный текст в списке выделенных текстов

    private var articlesInfoDialog: ArticlesInfoDialog? = null
    private var addVerseNoteDialog: AddVerseNoteDialog? = null
    private var colorPickerDialog: ColorPickerDialog? = null

    private var translationY = 100f
    private var interpolator = OvershootInterpolator()
    private var isMenuOpen = false

    private lateinit var saveLoadData: SaveLoadData

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
//        LayoutInflaterCompat.setFactory2(
//                LayoutInflater.from(this),
//                MyLayoutInflater(delegate)
//        )

        super.onCreate(savedInstanceState)
        saveLoadData = SaveLoadData(this)
        loadLocale()

        setContentView(R.layout.activity_main)

        //Отмечаем время захода в приложение, чтобы пуш не приходил тогда, когда юзер уже зашёл в приложение,
        //Потому что пуш должен приходить только тогда, когда юзер не заходил в прилу больше 1 суток
        saveLoadData.saveLong(UnlockBR.SHOWED_PUSH_TIME_KEY, getCurrentTime())
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(PushCreator.pushId)


        when (saveLoadData.loadString(THEME_NAME_KEY)) {
            LIGHT_THEME -> setTheme(ThemeManager.Theme.LIGHT, false)
            DARK_THEME -> setTheme(ThemeManager.Theme.DARK, false)
            BOOK_THEME -> setTheme(ThemeManager.Theme.BOOK, false)
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val a = abs(verticalOffset) - appBarLayout.totalScrollRange
            Utils.log("Offset: $a")
            if (isBibleTextFragmentOpened) {
                if (a != 0) {
                    //Expanded
                    setBottomAppBarVisibility(View.VISIBLE)
                    btnFABPlug.visibility =
                        View.VISIBLE //Этой вью нужно задавать видимость, чтобы не пропадало углубление в BottomAppBar
                } else {
                    //Collapsed
                    setBottomAppBarVisibility(View.GONE)
                    btnFABPlug.visibility = View.GONE
                }
            } else {
                setBottomAppBarVisibility(View.GONE)
                btnFABPlug.visibility = View.GONE
            }
        })

        viewPager.offscreenPageLimit = 2 /*ВАЖНО ПОМНИТЬ, ЕСЛИ КОЛИЧЕСТВО ТАБОВ РАСТЁТ, ТО И ЛИМИТ СОХРАНЁННЫХ ФРАГМЕНТОВ НУЖНО ПОВЫШАТЬ
                                           Объяснение вызова этого метода: https://stackoverflow.com/questions/27601920/android-viewpager-with-tabs-save-state, https://developer.android.com/reference/android/support/v4/view/ViewPager#setoffscreenpagelimit*/
        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)

        if (intent.hasExtra(PUSH_KEY) && intent.getIntExtra(PUSH_KEY, -1) == PUSH_ID_ARTICLES)
            viewPager.currentItem = tabArticlesNumber
        else
            viewPager.currentItem =
                tabBibleNumber //устанавливаем, чтобы при открытии приложения, сразу включался второй таб

        //Этот код нужен, чтобы задать полю isTabBibleSelected значение в тех случаях, когда выбран таб "Библия" и когда он не выбран
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    tabArticlesNumber -> {
                        isTabBibleSelected =
                            false //Указываем, что был выбран другой таб, НЕ таб "Библия"

                        setShowHideDonationLay(View.VISIBLE) //При переходе в таб Статьи и Ещё, устанавливаем видимость кнопке Поддержать на уровне всего раздела, потому что в этих двух табах эта кнопка должна быть видна во всех фрагментах, в отличие от раздела Библия, где видимость кнопки устанавливается в каждом фрагменте отдельно

                        //Код для показа подсказки в разделе "Статьи"
                        //Прописываем условие, чтобы этот код срабатывал только один раз
                        if (!saveLoadData.loadBoolean(TapTargetArticles)) {
                            //Помещаем код в Handler, потому что только так можно получить значение параметров высоты и ширины
                            val mainHandler = Handler(this@MainActivity.mainLooper)
                            val myRunnable =
                                Runnable {
                                    TapTargetSequence(this@MainActivity)
                                        .targets(
                                            Utils.getTapTargetButton(
                                                tabLayout.getTabAt(
                                                    tabArticlesNumber
                                                )?.view!!,
                                                this@MainActivity,
                                                R.string.btn_tab_articles_title,
                                                R.string.btn_tab_articles_description,
                                                Utils.convertPxToDp(
                                                    tabLayout.getTabAt(tabArticlesNumber)?.view!!.width.toFloat(),
                                                    this@MainActivity
                                                ).toInt() - 60
                                            ),
                                        ).start()

                                }
                            mainHandler.post(myRunnable)
                            saveLoadData.saveBoolean(TapTargetArticles, true)
                        }

                    }
                    tabBibleNumber -> {
                        isTabBibleSelected = true //Указываем, что был выбран таб "Библия"
                    }
                    tabMoreNumber -> {
                        isTabBibleSelected =
                            false //Указываем, что был выбран другой таб, НЕ таб "Библия"
                        setShowHideDonationLay(View.VISIBLE) //При переходе в таб Статьи и Ещё, устанавливаем видимость кнопке Поддержать на уровне всего раздела, потому что в этих двух табах эта кнопка должна быть видна во всех фрагментах, в отличие от раздела Библия, где видимость кнопки устанавливается в каждом фрагменте отдельно
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        setupTabIconsContent()

        //BottomAppBar кнопки
        btnFABFullScreen.alpha = 0f
        btnFABChangeFontSize.alpha = 0f

        btnFABFullScreen.translationY = translationY
        btnFABChangeFontSize.translationY = translationY

        btnFAB.setOnClickListener {
            if (isMenuOpen) closeMenu()
            else openMenu()
        }

        btnFABFullScreen.setOnClickListener {
            if (isFullScreenEnabled) setFullScreenMode(
                R.drawable.ic_full_screen,
                true,
                View.VISIBLE,
                false
            )
            else setFullScreenMode(R.drawable.ic_exit_full_screen, false, View.GONE, true)

            //Получаем высоту statusBarHeight
            val rectangle = Rect()
            val window: Window = window
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            val statusBarHeight: Int = rectangle.top

            bibleTextFragment.btnFABFullScreenClicked(
                isFullScreenEnabled,
                statusBarHeight + tabLayout.height
            )
        }

        btnFABChangeFontSize.setOnClickListener {
            isChangeFontSizeEnabled = if (isChangeFontSizeEnabled) {
                val animation1 = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
                val animation2 = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
                btnFABTextSizePlus.visibility = View.INVISIBLE
                btnFABTextSizePlus.startAnimation(animation1)
                clearAnimation(animation1, btnFABTextSizePlus)

                btnFABTextSizeMinus.visibility = View.INVISIBLE
                btnFABTextSizeMinus.startAnimation(animation2)
                clearAnimation(animation2, btnFABTextSizeMinus)
                false
            } else {
                val animation1 = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
                val animation2 = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
                btnFABTextSizePlus.visibility = View.VISIBLE
                btnFABTextSizePlus.startAnimation(animation1)
                clearAnimation(animation1, btnFABTextSizePlus)

                btnFABTextSizeMinus.visibility = View.VISIBLE
                btnFABTextSizeMinus.startAnimation(animation2)
                clearAnimation(animation2, btnFABTextSizeMinus)
                true
            }
        }

        val minTextSize = 12
        val maxTextSize = 32
        btnFABTextSizeMinus.setOnClickListener {
            val currentTextSize = saveLoadData.loadInt(TEXT_SIZE_KEY)
            if (currentTextSize == minTextSize) return@setOnClickListener
            saveLoadData.saveInt(TEXT_SIZE_KEY, currentTextSize - 2)
            bibleTextFragment.getCurrentRecyclerViewAdapter().notifyDataSetChanged()
            bibleTextFragment.notifyDataSetChangedVP()
        }

        btnFABTextSizePlus.setOnClickListener {
            val currentTextSize = saveLoadData.loadInt(TEXT_SIZE_KEY)
            if (currentTextSize == maxTextSize) return@setOnClickListener
            saveLoadData.saveInt(TEXT_SIZE_KEY, currentTextSize + 2)
            bibleTextFragment.getCurrentRecyclerViewAdapter().notifyDataSetChanged()
            bibleTextFragment.notifyDataSetChangedVP()
        }

        btnHome.setOnClickListener {
            bibleTextFragment.btnHomeClicked()

            if (isFullScreenEnabled) setFullScreenMode(
                R.drawable.ic_exit_full_screen,
                true,
                View.VISIBLE,
                false
            ) //Выключаем режим полного экрана, если он включён при нажатии кнопки "Домой"
        }
        btnInterpretation.setOnClickListener { bibleTextFragment.btnInterpretationClicked() }
        btnNotes.setOnClickListener { bibleTextFragment.btnNotesClicked() }
        btnSearch.setOnClickListener { bibleTextFragment.btnSearchClicked() }

        btnBack.setOnClickListener {
            //Если backStack не пустой, то вызываем onBackPressed
            //Эта проверка нужна для того, чтобы btnBack случайно не нажималась даже при видимости GONE, когда анимация пропадания ещё не успела сработать
            if (isBackStackNotEmpty)
                onBackPressed()
        }

        btnSelectTranslation.setOnClickListener {
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

        btnDeleteNote.setOnClickListener {
            val mainHandler = Handler(mainLooper)
            if (noteData.id != -1) {
                //Специальный вызов, для вызова метода в другом потоке с помощью RX
                Completable
                    .fromAction {
                        NotesDBHelper(this).deleteVerse(noteData.id)
                        noteData.id = -1
                        onBackPressed() //Возвращаемся назад, чтобы закрыть фрагмент заметки
                        //Поскольку Toast можно вызывать только в главном потоке, отправляем его в главный поток с помощью Handler
                        val myRunnable = Runnable {
                            StyleableToast.makeText(
                                this,
                                getString(R.string.toast_note_deleted),
                                Toast.LENGTH_SHORT,
                                R.style.my_toast
                            ).show()
                        }
                        mainHandler.post(myRunnable)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            }
        }

        btnShareNote.setOnClickListener {
            val shareBody: String =
                (if (noteData.verseText.isNotEmpty()) noteData.verseText + "\n\n" + noteData.text else noteData.text)

            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)

            startActivity(Intent.createChooser(myIntent, getString(R.string.toast_share_note)))
        }

        btnAddNoteFAB.setOnClickListener {
            val fragment = AddEditNoteFragment()
            fragment.isNoteToAdd = true
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

        //Кнопки панели множественного выбора текстов
        btnShare.setOnClickListener {
            val shareBody = getFormattedMultiSelectedText()

            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(myIntent, getString(R.string.toast_share_verse)))

            //Отключаем режим множественного выбора после выполнения метода
            disableMultiSelection()
        }

        btnCopy.setOnClickListener {
            val textForCopy = getFormattedMultiSelectedText()

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", textForCopy)
            clipboard.setPrimaryClip(clip)

            StyleableToast.makeText(
                this, getString(R.string.verse_copied),
                Toast.LENGTH_SHORT,
                R.style.my_toast
            ).show()

            //Отключаем режим множественного выбора после выполнения метода
            disableMultiSelection()
        }

        btnAddNote.setOnClickListener {
            addVerseNoteDialog = AddVerseNoteDialog(this)
            addVerseNoteDialog!!.setVerse(
                multiSelectedTextsList[0],
                getFormattedMultiSelectedText()
            ) //Отправлем здесь данные только 1-го модела коллекции, чтобы при открытии текста, открывало с первого текста списка.
            addVerseNoteDialog!!.show(
                myFragmentManager,
                "Add Note Dialog"
            ) //По непонятной причине открыть диалог вызываемым здесь childFragmentManager-ом не получается, поэтому приходится использовать переданный объект fragmentManager из другого класса
        }

        btnHighlight.setOnClickListener {
            colorPickerDialog = ColorPickerDialog(this)
            colorPickerDialog!!.setVersesData(multiSelectedTextsList)
            colorPickerDialog!!.show(
                myFragmentManager,
                "Color Picker Dialog"
            ) //По непонятной причине открыть диалог вызываемым здесь childFragmentManager-ом не получается, поэтому приходится использовать переданный объект fragmentManager из другого класса
        }

        btnRemoveHighlight.setOnClickListener {
            for (verseData in multiSelectedTextsList) {
                if (verseData.id != -1L) {
                    HighlightedBibleTextInfoDBHelper.getInstance(this)!!
                        .deleteBibleTextInfo(verseData.id)

                    //Обновляем также и данные в коллекции, чтобы не было проблем при очередной попытке добавить, обновить или удалить текст
                    verseData.textColorHex = null
                    verseData.isTextBold = false
                    verseData.isTextUnderline = false
                    verseData.id = -1
                }
            }

            //Обновляем ранее выделенные айтемы
            bibleTextFragment.getCurrentRecyclerViewAdapter()
                .updateItemColor(multiSelectedTextsList)

            //Отключаем режим множественного выбора после выполнения метода
            disableMultiSelection()
        }

        //Открываем БД с ранее используемым переводом Библии и в кнопку выбора переводов устанавливаем аббревиатуру перевода, который был ранее выбран пользователем
        val jsonBibleInfo = saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO)
        if (jsonBibleInfo != null && jsonBibleInfo.isNotEmpty()) {
            val gson = Gson()
            val bibleTranslationInfo: BibleTranslationModel =
                gson.fromJson(jsonBibleInfo, BibleTranslationModel::class.java)

            //Проверка на то, скачан ли перевод, выбранный ранее, или же перевод удалён и в saveLoadData хранится имя скачанного файла, но его самого не существует.
            //Если эту проверку не осуществлять, то в случае удаления выбранного перевода, программа будет пытаться открыть его, но не сможет,
            //потому что в действительности он будет удалён
            if (Utils.isSelectedTranslationDownloaded(this, bibleTranslationInfo)) {
                val bibleInfoViewModel = ViewModelProvider(this).get(BibleDataViewModel::class.java)
                bibleInfoViewModel.openDatabase(getExternalFilesDir(getString(R.string.folder_name)).toString() + "/" + bibleTranslationInfo.translationDBFileName)

                tvSelectTranslation.text = bibleTranslationInfo.abbreviationTranslationName
            } else {
                btnSelectTranslation.visibility =
                    View.GONE //Если не один перевод не выбран и не скачан, то скрываем кнопку выбора перевода
                btnSelectTranslation.isEnabled = false
            }
        }
    }

    fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED/*
                && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*/) {
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE/*, android.Manifest.permission.READ_EXTERNAL_STORAGE*/),
                1000
            )
        }
    }

    fun showHideBlurBackground(visibility: Int) {
        //Для того, чтобы не прописывать 2 метода на показ и сокрытие blurView, просто прописываем проверку в этом методе, в которой если приходит параметр GONE,
        //мы устанавливаем эту видимость и просто производим выход из метода, чтобы дальнейшее выполнение кода прекратилось,
        //потому как нам нужно просто убрать видимость blurView. Если приходит видимость VISIBLE, то устанавливаем её и дальнейший код отображение blurView тоже срабатывает
        if (visibility == View.GONE) {
            blurView.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.fade_out
                )
            ) //Анимация, чтобы blurView не появлялся и исчезал резко
            blurView.visibility = visibility
            //В случае выключения фокусировки, переводим действие blurView на самого же себя, устанавливая в параметр метод setupWith() тот же blurView.
            //Null установить нельзя, поэтому устанавливаем его же, чтобы оно не мешало другим Views.
            //Если этого не сделать то возникнут баги: некоторые вьюшки при отключении фокуса так и остануться размытыми,
            //или же плохо начинает работать анимация переходов между фрагментами
            blurView.setupWith(blurView)
            return
        }

        blurView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        blurView.visibility = visibility

        val radius = 4f

        val decorView = window.decorView
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        val rootView = decorView.findViewById<View>(android.R.id.content) as ViewGroup
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        val windowBackground = decorView.background

        blurView.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
    }

    private fun openMenu() {
        isMenuOpen = !isMenuOpen
        btnFAB.animate().setInterpolator(interpolator).rotation(45f).setDuration(300).start()

        //Обнуляем обработчики анимации здесь, чтобы у кнопок не срабатывали обработчики при повторном вызове этого метода, установленные этим же кнопкам в методе closeMenu()
        btnFABChangeFontSize
            .animate()
            .setListener(null)
            .start()
        btnFABFullScreen
            .animate()
            .setListener(null)
            .start()

        btnFABChangeFontSize.visibility = View.VISIBLE
        btnFABFullScreen.visibility = View.VISIBLE

        btnFABChangeFontSize.animate().translationY(0f).alpha(1f).setInterpolator(interpolator)
            .setDuration(300).start()
        btnFABFullScreen.animate().translationY(0f).alpha(1f).setInterpolator(interpolator)
            .setDuration(300).start()
    }

    private fun closeMenu() {
        isMenuOpen = !isMenuOpen
        btnFAB.animate().setInterpolator(interpolator).rotation(0f).setDuration(300).start()

        btnFABChangeFontSize
            .animate()
            .translationY(translationY)
            .alpha(0f)
            .setInterpolator(interpolator)
            .setDuration(300)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    btnFABChangeFontSize.visibility = View.INVISIBLE
                    btnFABChangeFontSize.clearAnimation()
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            }).start()

        btnFABFullScreen
            .animate()
            .translationY(translationY)
            .alpha(0f)
            .setInterpolator(interpolator)
            .setDuration(300)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    btnFABFullScreen.visibility = View.INVISIBLE
                    btnFABFullScreen.clearAnimation()
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            }).start()

        if (btnFABTextSizePlus.visibility != View.INVISIBLE && btnFABTextSizeMinus.visibility != View.INVISIBLE) {
            //На каждую View нужно использовать отдельный объект анимации в случае, когда потом нужно очищать анимацию,
            //поскольку при очищении анимации если будет очищаться один и тот же объект анимации, то в первой ситуации она очистится,
            //а во второй метод очищения анимации не сработает, потому что она уже будет очищена в первом случае
            val animation1 = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            val animation2 = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            btnFABTextSizePlus.visibility = View.INVISIBLE
            btnFABTextSizePlus.startAnimation(animation1)
            clearAnimation(animation1, btnFABTextSizePlus)

            btnFABTextSizeMinus.visibility = View.INVISIBLE
            btnFABTextSizeMinus.startAnimation(animation2)
            clearAnimation(animation2, btnFABTextSizeMinus)
            isChangeFontSizeEnabled = false
        }
    }

    fun closeMenuFromBibleTextFragment() {
        if (isMenuOpen) closeMenu()
    }

    override fun disableMultiSelection() {
        if (isMultiSelectionEnabled) {
            //Отключаем режим множественного выбора текстов и убираем видимость кнопок
            isMultiSelectionEnabled = false
            setShowHideMultiSelectionPanel(false)

            //Устанавливаем параметру isTextSelected значение false, чтобы при обновлении списка снялось выделение с выбранных айтемов
            for (selectedText in multiSelectedTextsList) selectedText.isTextSelected = false
            bibleTextFragment.notifyDataSetChanged()
        }
    }

    //Этот метод вызывается в случае, если были нажаты кнопки btnHome, btnNotes, btnSearch в BottomAppBar. Его отличие от метода disableMultiSelection заключается в том,
    //что в данном методе вызывается метод closeMultiSelectionPanelIfIfBottomAppBarBtnClicked в отличие от вызова метода setShowHideMultiSelectionPanel(false) в disableMultiSelection.
    //Вызов метода closeMultiSelectionPanelIfIfBottomAppBarBtnClicked точно так же убирает панель множественного выбора, но, в отличие от вызова метода setShowHideMultiSelectionPanel(false),
    //метод closeMultiSelectionPanelIfIfBottomAppBarBtnClicked не задаёт кнопке btnSelectTranslation видимость VISIBLE,
    //потому что нажатие кнопок btnHome, btnNotes, btnSearch обеспечивает переход из BibleTextFragment в другие фрагмент таба "Библия".
    //А при переходе в другие фрагменты таба "Библия", в которые ведут нажатие кнопок btnHome, btnNotes, btnSearch в BottomAppBar, кнопку btnSelectTranslation нужно скрывать.
    override fun disableMultiSelectionIfBottomAppBarBtnClicked() {
        if (isMultiSelectionEnabled) {
            //Отключаем режим множественного выбора текстов и убираем видимость кнопок
            isMultiSelectionEnabled = false
            closeMultiSelectionPanelIfIfBottomAppBarBtnClicked()

            //Устанавливаем параметру isTextSelected значение false, чтобы при обновлении списка снялось выделение с выбранных айтемов
            for (selectedText in multiSelectedTextsList) selectedText.isTextSelected = false
            bibleTextFragment.notifyDataSetChanged()
        }
    }

    //Метод, для форматирования выбранных текстов Библии.
    //Поскольку этот код используется несколько раз, для предотвращения дубликации кода этот код был вынесен в отдельный метод
    private fun getFormattedMultiSelectedText(): String {
        var selectedText = ""
        multiSelectedTextsList.forEachIndexed { index, selectedTextModel ->
            //Добавляем пробел перед каждым стихом, кроме первого
            selectedText += if (index == 0) selectedTextModel.text
            else " " + selectedTextModel.text
        }

        return "«" + selectedText + "»" + " (" + tvSelectedBibleBook.text + tvSelectedBibleChapter.text + tvSelectedBibleVerse.text + ")"
    }

    //Анимация появления кнопки назад в Toolbar
    override fun setShowHideToolbarBackButton(backButtonVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы тулбар не анимировался каждый раз при переходе между табами. Если в одном табе стрелка включена и во втором табе тоже,
        //то в таком случае выходим из метода без всяких анимаций, чтобы анимация не запускалась без надобности.
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnBackVisibility = btnBack.visibility
        if (btnBackVisibility == backButtonVisibility) {
            return
        }

//        val params: RelativeLayout.LayoutParams = toolbarTitle.layoutParams as RelativeLayout.LayoutParams

        val animationTitle: ObjectAnimator?
        val animationSelectedText: ObjectAnimator?
        val pixels: Float
        if (backButtonVisibility == View.VISIBLE) {
            //Конвертируем db в пиксели, потому что метод анимации на вход принимает пиксели, а нужно устанавливать значение в db
            pixels = convertDpToPx(this, 55f)
            animationSelectedText = viewAnimatorX(
                pixels,
                layTvSelectedBibleText,
                250
            ) //Анимируем TextView выбранного текст Библии вместе с title тулбара
            animationTitle = viewAnimatorX(pixels, toolbarTitle, 250)
            animationTitle?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    val anim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.zoom_in)
                    btnBack.startAnimation(anim)
                    btnBack.visibility = View.VISIBLE
                    clearAnimation(anim, btnBack)
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
            })
        } else {
            //Конвертируем db в пиксели, потому что метод анимации на вход принимает пиксели, а нужно устанавливать значение в db
            pixels = convertDpToPx(this, 14f)
            animationSelectedText = viewAnimatorX(
                pixels,
                layTvSelectedBibleText,
                250
            ) //Анимируем TextView выбранного текст Библии вместе с title тулбара
            animationTitle = viewAnimatorX(pixels, toolbarTitle, 250)
            animationTitle?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {
                    val anim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.zoom_out)
                    btnBack.startAnimation(anim)
                    btnBack.visibility = View.GONE
                    clearAnimation(anim, btnBack)
                }
            })
        }
        animationSelectedText?.start()
        animationTitle?.start()
    }

    override fun setNoteData(noteData: NoteModel) {
        this.noteData = noteData
    }

    //К сожалению, по логике реализация работы панели множественного выбора получилась закрученной,
    //пришлось разбить всё на 2 метода:
    //setShowHideMultiSelectionPanel,
    //closeMultiSelectionPanelIfBottomAppBarBtnClicked
    //поэтому проще прощения у себя будущего или у других возможных разработчиков)
    override fun setShowHideMultiSelectionPanel(isVisible: Boolean) {
        val animationBtnSelectTranslation: Animation

        val animationBtnShare: Animation
        val animationBtnCopy: Animation
        val animationBtnAddNote: Animation
        val animationBtnHighlight: Animation

        val animationBtnRemoveHighlight: Animation

        if (isVisible) {
            //Открываем appBarLayout при включении режима множественного выбора
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) appBarLayout.setExpanded(
//                true,
//                false
//            )
//            else appBarLayout.setExpanded(
//                true,
//                true
//            ) //Открываем appBarLayout при включении режима множественного выбора

            animationBtnSelectTranslation = AnimationUtils.loadAnimation(this, R.anim.fade_out)

            animationBtnShare = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            animationBtnCopy = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            animationBtnAddNote = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            animationBtnHighlight = AnimationUtils.loadAnimation(this, R.anim.zoom_in)

            animationBtnRemoveHighlight = AnimationUtils.loadAnimation(this, R.anim.zoom_in)

            tvSelectedBibleVerse.visibility = View.VISIBLE

            btnSelectTranslation.startAnimation(animationBtnSelectTranslation)
            btnSelectTranslation.visibility = View.GONE
            btnSelectTranslation.isEnabled = false
            animationBtnSelectTranslation?.setAnimationListener(object :
                Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationStart(animation: Animation?) {
                    btnShare.startAnimation(animationBtnShare)
                    btnShare.visibility = View.VISIBLE
                    btnShare.isEnabled = true

                    btnCopy.startAnimation(animationBtnCopy)
                    btnCopy.visibility = View.VISIBLE
                    btnCopy.isEnabled = true

                    btnAddNote.startAnimation(animationBtnAddNote)
                    btnAddNote.visibility = View.VISIBLE
                    btnAddNote.isEnabled = true

                    btnHighlight.startAnimation(animationBtnHighlight)
                    btnHighlight.visibility = View.VISIBLE
                    btnHighlight.isEnabled = true

                    if (isMultiSelectedTextsListContainsHighlightedVerse) {
                        btnRemoveHighlight.startAnimation(animationBtnRemoveHighlight)
                        btnRemoveHighlight.visibility = View.VISIBLE
                        btnRemoveHighlight.isEnabled = true
                    }
                }

                override fun onAnimationEnd(animation: Animation?) {}
            })
        } else {
            animationBtnSelectTranslation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

            animationBtnShare = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            animationBtnCopy = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            animationBtnAddNote = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            animationBtnHighlight = AnimationUtils.loadAnimation(this, R.anim.zoom_out)

            animationBtnRemoveHighlight = AnimationUtils.loadAnimation(this, R.anim.zoom_out)

            tvSelectedBibleVerse.visibility = View.GONE

            //Если панель множественного выбора была открыта на выделенном тексте, то будет показана также btnRemoveHighlight, а если на обычном тексте, то без неё
            if (isMultiSelectedTextsListContainsHighlightedVerse) {
                btnRemoveHighlight.startAnimation(animationBtnRemoveHighlight)
                btnRemoveHighlight.visibility = View.GONE
                btnRemoveHighlight.isEnabled = false
            }

            btnHighlight.startAnimation(animationBtnHighlight)
            btnHighlight.visibility = View.GONE
            btnHighlight.isEnabled = false
            animationBtnHighlight.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    if (isTabBibleSelected) {
                        btnSelectTranslation.startAnimation(animationBtnSelectTranslation)
                        btnSelectTranslation.visibility = View.VISIBLE
                        btnSelectTranslation.isEnabled = true
                    }
                }

                override fun onAnimationStart(animation: Animation?) {}
            })

            btnAddNote.startAnimation(animationBtnAddNote)
            btnAddNote.visibility = View.GONE
            btnAddNote.isEnabled = false

            btnCopy.startAnimation(animationBtnCopy)
            btnCopy.visibility = View.GONE
            btnCopy.isEnabled = false

            btnShare.startAnimation(animationBtnShare)
            btnShare.visibility = View.GONE
            btnShare.isEnabled = false
        }
    }

    //Метод специально для класса BibleTextFragment. При нажатии на кнопку btnHome в BottomAppBar в случае если активирован режим множественного выбора текстов,
    //нужно скрыть панель множественного выбора текстов, но при этом не показывать кнопку выбора переводов Библии (как это реализовано в методе setShowHideMultiSelectionPanel),
    //потому при нажатии на кнопку btnHome идёт возврат в главный фрагмент SelectTestamentFragment где не кнопка выбора переводов не должна отображаться
    private fun closeMultiSelectionPanelIfIfBottomAppBarBtnClicked() {
        val animationBtnShare: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        val animationBtnCopy: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        val animationBtnAddNote: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        val animationBtnHighlight: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)

        val animationBtnRemoveHighlight = AnimationUtils.loadAnimation(this, R.anim.zoom_out)

        tvSelectedBibleVerse.visibility = View.GONE

        //Если панель множественного выбора была открыта на выделенном тексте, то будет показана также btnRemoveHighlight, а если на обычном тексте, то без неё
        if (isMultiSelectedTextsListContainsHighlightedVerse) {
            btnRemoveHighlight.startAnimation(animationBtnRemoveHighlight)
            btnRemoveHighlight.visibility = View.GONE
            btnRemoveHighlight.isEnabled = false
        }

        btnHighlight.startAnimation(animationBtnHighlight)
        btnHighlight.visibility = View.GONE
        btnHighlight.isEnabled = false

        btnAddNote.startAnimation(animationBtnAddNote)
        btnAddNote.visibility = View.GONE
        btnAddNote.isEnabled = false

        btnCopy.startAnimation(animationBtnCopy)
        btnCopy.visibility = View.GONE
        btnCopy.isEnabled = false

        btnShare.startAnimation(animationBtnShare)
        btnShare.visibility = View.GONE
        btnShare.isEnabled = false
    }

    override fun sendMultiSelectedTextsData(multiSelectedTextsList: ArrayList<BibleTextModel>) {
        this.multiSelectedTextsList = multiSelectedTextsList
        setSelectedVerses(multiSelectedTextsList)

        //Делаем проверку того, содержит ли список хотя бы один выделенный текст,
        // если да, то панель множественного выбора будет отображаться с кнопкой btnRemoveHighlight, а если нет, то без неё
        for (item in multiSelectedTextsList) {
            if (item.textColorHex != null && item.textColorHex!!.isNotEmpty()) {
                isMultiSelectedTextsListContainsHighlightedVerse = true
                break
            } else isMultiSelectedTextsListContainsHighlightedVerse = false
        }

        //Код, который показывает кнопку btnRemoveHighlight если есть хотя бы один выделенный текст, а если нет, то скрывает её
        val btnRemoveHighlightVisibility = btnRemoveHighlight.visibility
        if (isMultiSelectedTextsListContainsHighlightedVerse) {
            if (btnRemoveHighlightVisibility == View.VISIBLE) {
                return
            }
            btnRemoveHighlight.visibility = View.VISIBLE
            btnRemoveHighlight.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in))
        } else {
            if (btnRemoveHighlightVisibility == View.GONE) {
                return
            }
            btnRemoveHighlight.visibility = View.GONE
            btnRemoveHighlight.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out))
        }
    }

    //Метод, форматирующий текст нужным образом при выделении стихов.
    //Например, если выделены несколько текстов, идущих подряд друг за другом (например, 4,5,6,7),
    //то отрезок будет отформатирован с помощью дефиза, вот так: 4-7 , то есть с 4 по 7 стихи.
    //Если же выбираются тексты на следующий друг за другом, то они буду разделяться запятой.
    //К примеру, пользователь выделил 4 стих, а потом 7 стих, на выходе будет выглядеть вот так: 4,7, то есть отдельно 4 стих и отдельно 7 стих
    private fun setSelectedVerses(multiSelectedTextsList: ArrayList<BibleTextModel>) {
        var selectedVerses = ":" + multiSelectedTextsList[0].verse_number
        for ((index, bibleTextModel) in multiSelectedTextsList.withIndex()) {
            if (multiSelectedTextsList.size > 1 && index != 0) {
                selectedVerses += if ((bibleTextModel.verse_number - multiSelectedTextsList[index - 1].verse_number) == 1) {
                    if (index + 1 != multiSelectedTextsList.size && (multiSelectedTextsList[index + 1].verse_number - bibleTextModel.verse_number) == 1) {
                        continue
                    }
                    "-" + bibleTextModel.verse_number
                } else {
                    "," + bibleTextModel.verse_number
                }
            }
        }
        tvSelectedBibleVerse.visibility = View.VISIBLE
        tvSelectedBibleVerse.text = selectedVerses
    }

    override fun setShowHideArticlesInfoButton(articlesInfoBtnVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы btnArticlesInfo не анимировался каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение btnSelectTranslationVisibility меняется
//        val btnArticlesInfoVisibility = btnArticlesInfo.visibility
//        if (btnArticlesInfoVisibility == articlesInfoBtnVisibility) {
//            return
//        }
//
//        val animation: Animation?
//        if (articlesInfoBtnVisibility == View.VISIBLE) {
//            btnArticlesInfo.visibility = View.VISIBLE
//            btnArticlesInfo.isEnabled = true
//            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
//        } else {
//            btnArticlesInfo.visibility = View.GONE
//            btnArticlesInfo.isEnabled = false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
//            animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
//        }
//        clearAnimation(animation, btnArticlesInfo)
//        btnArticlesInfo.startAnimation(animation)
    }

    override fun setShowHideDonationLay(donationLayVisibility: Int) {
//        //Этот фрагмент кода нужен, чтобы donationLay не анимировался каждый раз при переходе между табами.
//        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
//        //Анимирование происходит только в том случае, когда значение btnDonationLayVisibility меняется
//        val btnDonationLayVisibility = donationLay.visibility
//        if (btnDonationLayVisibility == donationLayVisibility) {
//            return
//        }
//
//        val animation: Animation?
//        if (donationLayVisibility == View.VISIBLE) {
//            donationLay.visibility = View.VISIBLE
//            donationLay.isEnabled = true
//            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
//        } else {
//            donationLay.visibility = View.GONE
//            donationLay.isEnabled =
//                false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
//            animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
//        }
//        clearAnimation(animation, donationLay)
//        donationLay.startAnimation(animation)
    }

    override fun setShowHideAddNoteButtonFAB(addNoteFABBtnVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы btnAddNoteFAB не анимировался каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение btnSelectTranslationVisibility меняется
        val btnAddNoteFABVisibility = btnAddNoteFAB.visibility
        if (btnAddNoteFABVisibility == addNoteFABBtnVisibility) {
            return
        }

        val animation: Animation?
        if (addNoteFABBtnVisibility == View.VISIBLE) {
            btnAddNoteFAB.visibility = View.VISIBLE
            btnAddNoteFAB.isEnabled = true
            animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        } else {
            btnAddNoteFAB.visibility = View.GONE
            btnAddNoteFAB.isEnabled =
                false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
            animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        }
        clearAnimation(animation, btnAddNoteFAB)
        btnAddNoteFAB.startAnimation(animation)
    }

    override fun setShowHideNoteButtons(noteBtnVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы btnDeleteNote и не btnShareNote анимировался каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение btnSelectTranslationVisibility меняется
        val btnDeleteNoteVisibility = btnDeleteNote.visibility
        val btnShareNoteVisibility = btnShareNote.visibility
        if (btnDeleteNoteVisibility == noteBtnVisibility && btnShareNoteVisibility == noteBtnVisibility) {
            return
        }

        val animationBtnDelete: Animation?
        val animationBtnShare: Animation?
        if (noteBtnVisibility == View.VISIBLE) {
            btnDeleteNote.visibility = View.VISIBLE
            btnDeleteNote.isEnabled = true
            animationBtnDelete = AnimationUtils.loadAnimation(this, R.anim.zoom_in)

            btnShareNote.visibility = View.VISIBLE
            btnShareNote.isEnabled = true
            animationBtnShare = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        } else {
            btnDeleteNote.visibility = View.GONE
            btnDeleteNote.isEnabled =
                false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
            animationBtnDelete = AnimationUtils.loadAnimation(this, R.anim.zoom_out)

            btnShareNote.visibility = View.GONE
            btnShareNote.isEnabled =
                false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
            animationBtnShare = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        }
        clearAnimation(animationBtnDelete, btnDeleteNote)
        btnDeleteNote.startAnimation(animationBtnDelete)

        clearAnimation(animationBtnShare, btnShareNote)
        btnShareNote.startAnimation(animationBtnShare)
    }

    override fun setBtnSelectTranslationVisibility(visibility: Int) {
        //Выставляем цвет иконки для кнопки btnSelectTranslation в случае если тема была поменяна
        when (ThemeManager.theme) {
            ThemeManager.Theme.LIGHT -> ImageViewCompat.setImageTintList(
                ivSelectTranslation,
                ContextCompat.getColorStateList(this, R.color.colorButtonIconLightTheme)
            )
            ThemeManager.Theme.DARK -> ImageViewCompat.setImageTintList(
                ivSelectTranslation,
                ContextCompat.getColorStateList(this, R.color.colorButtonIconDarkTheme)
            )
            ThemeManager.Theme.BOOK -> ImageViewCompat.setImageTintList(
                ivSelectTranslation,
                ContextCompat.getColorStateList(this, R.color.colorButtonIconBookTheme)
            )
        }

        //Этот фрагмент кода нужен, чтобы btnSelectTranslation не анимировался каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение btnSelectTranslationVisibility меняется
        val btnSelectTranslationVisibility = btnSelectTranslation.visibility
        if (btnSelectTranslationVisibility == visibility) {
            return
        }

        val animation: Animation?
        if (visibility == View.VISIBLE) {
            btnSelectTranslation.visibility = View.VISIBLE
            btnSelectTranslation.isEnabled = true
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        } else {
            btnSelectTranslation.visibility = View.GONE
            btnSelectTranslation.isEnabled =
                false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        }
        clearAnimation(animation, btnSelectTranslation)
        btnSelectTranslation.startAnimation(animation)
    }

    private fun clearAnimation(animation: Animation, view: View) {
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                view.clearAnimation()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    //Устанавливаем в текст кнопки выбора переводов аббревиатуру выбранного перевода
    override fun setBtnSelectTranslationText(selectedTranslation: String) {
        tvSelectTranslation.text = selectedTranslation
    }

    override fun setBtnSelectTranslationClickableState(clickableState: Boolean) {
        btnSelectTranslation.isClickable = clickableState
    }

    override fun setBtnDonationClickableState(clickableState: Boolean) {
//        donationLay.isClickable = clickableState
    }

    //Метод нужен, чтобы сменять текст названия приложения на текст выбранного текста Библии
    override fun setTvSelectedBibleTextVisibility(selectedTextVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы tvSelectedBibleText и toolbarTitle не анимировались каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение selectedTextVisibility меняется
        val tvSelectedBibleTextVisibility = layTvSelectedBibleText.visibility
        if (tvSelectedBibleTextVisibility == selectedTextVisibility) {
            return
        }

        val animation: Animation?
        if (selectedTextVisibility == View.VISIBLE) {
            layTvSelectedBibleText.visibility = View.VISIBLE
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    toolbarTitle.clearAnimation()
                }

                override fun onAnimationStart(animation: Animation?) {
                    toolbarTitle.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@MainActivity,
                            R.anim.fade_out
                        )
                    )
                    toolbarTitle.visibility = View.GONE
                }
            })
            layTvSelectedBibleText.startAnimation(animation)
        } else {
            toolbarTitle.visibility = View.VISIBLE
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    toolbarTitle.clearAnimation()
                }

                override fun onAnimationStart(animation: Animation?) {
                    layTvSelectedBibleText.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@MainActivity,
                            R.anim.fade_out
                        )
                    )
                    layTvSelectedBibleText.visibility = View.GONE
                }
            })
            toolbarTitle.startAnimation(animation)
        }
    }

    override fun setTvSelectedBibleText(selectedText: String, isBook: Boolean) {
        //Здесь проихводится проверка на то, название книги сюда приходит, или же номер выбранной главы. Если книга, то устанавливаем в нужный TextView, если номер, то соответственно
        if (isBook) tvSelectedBibleBook.text = selectedText
        else tvSelectedBibleChapter.text = selectedText
    }

    override fun setMyFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun getMyFragmentManager(): FragmentManager {
        return myFragmentManager
    }

    override fun setIsBackStackNotEmpty(isBackStackNotEmpty: Boolean) {
        this.isBackStackNotEmpty = isBackStackNotEmpty
    }

    override fun setIsTranslationDownloaded(isTranslationDownloaded: Boolean) {
        this.isTranslationDownloaded = isTranslationDownloaded
    }

    //Устанавливаем табы в ViewPager, создавая фрагменты каждого таба
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ArticlesRootFragment(), getString(R.string.title_articles))
        adapter.addFragment(BibleRootFragment(), getString(R.string.title_bible))
        adapter.addFragment(MoreRootFragment(), getString(R.string.title_more))
        viewPager.adapter = adapter
    }

    //Устанавливаем иконки и их цвет
    private fun setupTabIconsContent() {
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.icon != null) {
                    if (ThemeManager.theme == ThemeManager.Theme.BOOK) tab.icon?.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.colorTabIndicatorBookTheme
                            ), PorterDuff.Mode.SRC_IN
                        )
                    else tab.icon?.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.colorIconTabLightTheme
                        ), PorterDuff.Mode.SRC_IN
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.icon != null) {
                    if (ThemeManager.theme == ThemeManager.Theme.BOOK) tab.icon?.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.colorUnselectedTabTextBookTheme
                            ), PorterDuff.Mode.SRC_IN
                        )
                    else tab.icon?.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.colorGray
                        ), PorterDuff.Mode.SRC_IN
                    )
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        setIconForTabs()
    }

    private fun setIconForTabs() {
        val tabIcons = intArrayOf(
            R.drawable.ic_aticles,
            R.drawable.ic_bible,
            R.drawable.ic_more
        )
        for (i in 0..3) {

            //Задаём видимость для иконок в табах, чтобы в случае горизонтальной ориентации иконки пропадали, потому что в горизонтальном режиме иконки могут неудобно заслонять текст
            val orientation: Int = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //Отключаем иконки, устаналивая на каждый таб иконкам значение null
                tabLayout.getTabAt(i)?.icon = null
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                tabLayout.getTabAt(i)?.setIcon(tabIcons[i])
            }

            if (ThemeManager.theme == ThemeManager.Theme.BOOK) setTabIconColor(
                i,
                R.color.colorUnselectedTabTextBookTheme
            )
            else setTabIconColor(i, R.color.colorGray)

            if (intent.hasExtra(PUSH_KEY) && intent.getIntExtra(PUSH_KEY, -1) == PUSH_ID_ARTICLES) {
                if (i == tabArticlesNumber) selSelectedTabIconColor(i)
            } else if (i == currentTabNumber) selSelectedTabIconColor(i)
        }
        if (intent.hasExtra(PUSH_KEY) && intent.getIntExtra(PUSH_KEY, -1) == PUSH_ID_ARTICLES) {
            intent.putExtra(PUSH_KEY, "")
        }
    }

    private fun selSelectedTabIconColor(i: Int) {
        if (ThemeManager.theme == ThemeManager.Theme.BOOK) setTabIconColor(
            i,
            R.color.colorIconTabBookTheme
        )
        else setTabIconColor(i, R.color.colorIconTabLightTheme)
    }

    private fun setTabIconColor(i: Int, colorRes: Int) {
        tabLayout.getTabAt(i)?.icon?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(
                this@MainActivity,
                colorRes
            ), PorterDuff.Mode.SRC_IN
        )
    }

    //Обновляем цвета иконок и текста на табах, потому что если в тёмной и светлой теме цвета на текста и иконок одинаковые в TabLayout,
    //то в теме BOOK они отличаются, поэтому их нужно обновлять
    override fun updateTabIconAndTextColor() {
        setIconForTabs()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setIconForTabs() //Здесь этот метод вызывается для того, чтобы включить или отключить иконки в зависимости от выбранной ориентации экрана

        val orientation: Int = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            appBarLayout.setExpanded(false)
        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
            appBarLayout.setExpanded(true)
    }

    override fun setViewPagerSwipeState(viewPagerSwipeState: Boolean) {
        viewPager.setSwipeState(viewPagerSwipeState)
    }

    override fun setTabNumber(tabNumber: Int) {
        this.currentTabNumber = tabNumber
    }

    override fun setBibleTextFragment(bibleTextFragment: BibleTextFragment) {
        this.bibleTextFragment = bibleTextFragment
    }

    //Метод для установления видимости BottomAppBar
    private fun setBottomAppBarVisibility(visibility: Int) {
        val appBarVisibility = appBar.visibility
        if (appBarVisibility == visibility) {
            return
        }

        appBar.visibility = visibility
        fabMenuLayout.visibility = visibility
        val animationBottomAppBar: Animation
        val animationFAB: Animation
        if (visibility == View.VISIBLE) {
            animationBottomAppBar = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            animationFAB = AnimationUtils.loadAnimation(this, R.anim.zoom_in_slow)
        } else {
            animationBottomAppBar = AnimationUtils.loadAnimation(this, R.anim.slide_down)
            animationFAB = AnimationUtils.loadAnimation(this, R.anim.zoom_out_slow)
        }
        appBar.startAnimation(animationBottomAppBar)
        clearAnimation(animationBottomAppBar, appBar)

        fabMenuLayout.startAnimation(animationFAB)
        clearAnimation(animationFAB, fabMenuLayout)
    }

    override fun setIsBibleTextFragmentOpened(isBibleTextFragmentOpened: Boolean) {
        this.isBibleTextFragmentOpened = isBibleTextFragmentOpened
    }

    override fun onStop() {
        super.onStop()
        Utils.log("Activity onStop()")
        //Скорее всего этот код можно будет вовсе удалить, но пока оставляю на всякий случай.
        //Закрываем подключение к Базе данных Статей и удаляем БД. Делать это нужно именно в здесь в активити, потому что onStop вызывается именно при закрытии активити,
        // то есть так, как и надо. Если вызывать в onStop фрагмента, то он будет пересоздавать при каждом открытии и другого фрагмента, а это не подходящий вариант,
        // потому что таким образом трафик толком не экономится
//        ViewModelProvider(this).get(ArticlesViewModel::class.java).closeArticlesDB()
//        deleteDatabase(ARTICLES_DATA_BASE_NAME)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.log("Activity onDestroy()")
        App.articlesData =
            null //Очищаем переменную, хранящую данные статьей при закрытии приложения
    }

    override fun setTheme(theme: ThemeManager.Theme, animate: Boolean) {
        ThemeManager.theme = theme

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            when (theme) {
                ThemeManager.Theme.LIGHT -> window.statusBarColor =
                    ContextCompat.getColor(applicationContext, R.color.colorStatusBarLightTheme)
                ThemeManager.Theme.DARK -> window.statusBarColor =
                    ContextCompat.getColor(applicationContext, R.color.colorStatusBarDarkTheme)
                ThemeManager.Theme.BOOK -> window.statusBarColor =
                    ContextCompat.getColor(applicationContext, R.color.colorStatusBarBookTheme)
            }
        }

        if (!animate) return //Код, отключающий анимацию

        val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        container.startAnimation(animation)
    }

    override fun onBackPressed() {
        //КОД НЕ МЕНЯТЬ!
        /*Получаем имя Фрагмента, из которого мы получили FragmentManager. Если это FragmentManager из BibleRootFragment, то тогда конкретно для этого раздела мы устанавливаем такие условия:
        Если перевод не скачан, то при нажатии кнопки назад приложение закрывается, если же перевод есть, то при нажатии назад будет открыт предыдущий фрагмент.
        В противном случае, если не проверять FragmentManager на имя, то условие будет установлено для всех разделов, а это значит, что в любом из разделов при попытке вернуться назад,
        приложение будет закрываться. Нам же нужно, чтобы это было реализовно только в разделе Библии и в случае отсутствия переводов. Чтобы если перевода нету,
        юзер не мог открыть раздел Библии*/
        if (saveLoadData.loadBoolean(BibleTranslationsRVAdapter.isTranslationDownloading)) {
            StyleableToast.makeText(
                this,
                getString(R.string.toast_please_wait),
                Toast.LENGTH_SHORT,
                R.style.my_toast
            ).show()
            return
        }

        val myFragmentManagerName = myFragmentManager.toString()
        //Вызываем метод contains, потому что помимо имени фрагмента, из которого был взят FragmentManager, там ещё содержатся другая информация, которая не нужна
        if (myFragmentManagerName.contains("BibleRootFragment")) {
            //Если никакой перевод не скачан или скачанный перевод был удалён, то при нажатии кнопки назад в фрагменте BibleTranslationFragment приложение будет закрыто
            if (!isTranslationDownloaded || saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO) != null
                && saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO)!!.isNotEmpty()
                && !Utils.isSelectedTranslationDownloaded(
                    this,
                    Gson().fromJson(
                        saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO),
                        BibleTranslationModel::class.java
                    )
                )
            ) {
                super.onBackPressed()
                return
            }
//            else if (isTranslationDownloaded
//                    && saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO) == null) {
//                super.onBackPressed()
//                return
//            }
        }

        Utils.hideKeyboard(this) //Вызываем этот метод, чтобы при нажатии стрелки назад закрывать клавиатуру, если она открыта

        //Устанавливаем на переменную isBackButtonClicked значение true, когда кнопка "Назад" была нажата. Это значение нужно для BibleTextFragment.
        //А по скольку onBackPressed срабатывает раньше, чем onPause, то после того, как пользователь нажал кнопку "Назад" и сработал onBackPressed,
        //то в onPause, при закрытии BibleTextFragment, происходит проверка и если определяется, что метод onPause срабатывает после нажатия кнопки "Назад",
        //а не после закрытия приложения через диспетчер задач, то данные о сохранённом скролле очищаются в onPause и при следующем открытии,
        //приложение будет открыто в начальном экране SelectTestamentFragment
        isBackButtonClicked = true

        //Если открыт таб "Библия" и включён режим множественного выбора, то при нажатии кнопки "Назад" сначала выключается режим множественного выбора,
        //и только после этого можно как обычно вернуться на предыдущий фрагмент тем же нажатием кнопки "Назад"
        if (isMultiSelectionEnabled && myFragmentManagerName.contains("BibleRootFragment")) {
            disableMultiSelection()
            return
        }

        //Если открыт таб "Библия" и включён режим множественного полного экрана, то при нажатии кнопки "Назад" сначала выключается режим полного экрана,
        //и только после этого можно как обычно вернуться на предыдущий фрагмент тем же нажатием кнопки "Назад"
        if (isFullScreenEnabled && isBibleTextFragmentOpened) {
            setFullScreenMode(R.drawable.ic_full_screen, true, View.VISIBLE, false)
            return
        }

        if (isBackStackNotEmpty)
            myFragmentManager.popBackStack()
        else
            super.onBackPressed()
    }

    private fun setFullScreenMode(
        fullScreenImage: Int,
        isStatusBarVisible: Boolean,
        tabLayoutVisibility: Int,
        fullScreenEnabled: Boolean
    ) {
        btnFABFullScreen.setImageResource(fullScreenImage)

        //Открываем или закрываем statusBar и tabLayout
        if (isStatusBarVisible)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) //Открыть statusBar
        else window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) //Закрыть statusBar

        //Если BibleTextFragment открыт из HighlightedVersesFragment, то устанавливать видимость для TabLayout не нужно никакую,
        //потому что открытый BibleTextFragment из HighlightedVersesFragment должен быть без TabLayout,
        //чтобы пользователь не мог потенциально бесконечно открывать новый BibleTextFragment из таба "Ещё" из фрагмента HighlightedVersesFragment,
        //нажимая на выделенные тексты
        if (!bibleTextFragment.isBibleTextFragmentOpenedFromHighlightedVersesFragment)
            tabLayout.visibility = tabLayoutVisibility

        isFullScreenEnabled = fullScreenEnabled
    }

    override fun changeLanguage(languageCode: String) {
        saveLoadData.saveString(
            AppLanguageFragment.APP_LANGUAGE_CODE_KEY,
            languageCode
        )//Сохраняем выбранный язык, что при повторном запуске приложения загружать его.
        //Сохраняем мы его именно здесь, чтобы язык сохранялся только тогда, когда человек выберет его. Если же человек не выбрал его, то автоматически будет загружаться нужный язык.
        //К примеру, в приложении предусмотрено 3 языка (en, uk, ru), а язык телефона выбран немецкий. Человек заходит в приложение и язык приложения включиться дефолтный английский,
        //потому что немецкий в приложении не предусмотрен. Затем пользователь меняет язык телефона на русский и когда он снова включит приложение,
        //то прописанный алгоритм в методе главного активити onBackPressed вычислит, что данный(русский) язык предусмотрен в приложении и откроет приложение уже на русском языке.
        //Если бы выбранный язык сохранялся в методе setLocale(как это было раньше), то при первом запуске приложения за неимением немецкого языка, по дефолту был бы выбран и сразу сохранён английский язык.
        //И даже если бы пользователь потом переключил бы язык телефона на русский и запустил бы приложение, то приложение осталось бы на английском языке, потому что английский язык сохранился бы при первом запуске.
        setLocale(languageCode)
        recreate()
//        startActivity(intent)
//        finish()
    }

    private fun setLocale(languageCode: String) {
//        saveLoadData.saveString(AppLanguageFragment.APP_LANGUAGE_CODE_FOR_LANGUAGE_LIST_KEY, languageCode)
        saveLoadData.saveString(AppLanguageFragment.APP_LANGUAGE_CODE_KEY, languageCode)

        val activityRes = resources
        val activityConf = activityRes.configuration
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        activityRes.updateConfiguration(activityConf, activityRes.displayMetrics)

        val applicationRes = applicationContext.resources
        val applicationConf = applicationRes.configuration
        applicationConf.setLocale(newLocale)
        applicationRes.updateConfiguration(applicationConf, applicationRes.displayMetrics)
    }

    //Метод, в котором загружается выбранный язык приложения. Если язык не выбран, то, если язык телефона совпадает с имеющимся языком из предоставленных в приложении,
//значит устанавливается он. Если же язык телефона не совпадает ни с каким из предоставленных в приложении, то по дефолту ставится английский
    private fun loadLocale() {
        // Сделать проверку, если язык телефона совпадает с существующим переводом приложения в приложении, то установить его, если такого перевода нет в приложении, то устанавливать дефолтный(английский)
        val languageList = arrayOf("en", "ru", "uk")

        val languageCodeOfSelectedLanguage =
            saveLoadData.loadString(AppLanguageFragment.APP_LANGUAGE_CODE_KEY) //Код языка, который выбраз пользоваетль при переключении языка приложения
        if (languageCodeOfSelectedLanguage == null || languageCodeOfSelectedLanguage.isEmpty()) {
            val languageCode = Locale.getDefault().language
            var isLanguageInList = true
            for (item in languageList) {
                if (languageCode == item) {
                    setLocale(languageCode)
                    isLanguageInList = true
                    break
                } else {
                    isLanguageInList = false
                }
            }

            //Если язык не находится в списке предусмотренных языков в приложении, то ставится язык по умолчанию - Английский
            if (!isLanguageInList) {
                setLocale("en")
            }
        } else
            setLocale(languageCodeOfSelectedLanguage)
    }

    override fun dismissDialog() {
        //Не смотря на жёлтое выделение оператора if, этот код должен быть именно таким.
        //Видимо, система предполагает, что если данный метод вызывается из объекта диалога, то объект диалога не может иметь значение null.
        //Но дело в том, что этот один и тот же метод dismissDialog может быть вызван только в одном из созданных открытых диалогов, в то время как другие диалоги не буду созданы.
        //И если для одного объекта диалога сработает метод dismiss(), то для другого нет, потому как он даже не будет создан.
        //Этот код реализован таким образом для того, чтобы не писать для каждого диалога свой обработчик.
        articlesInfoDialog?.dismiss()

        addVerseNoteDialog?.dismiss()
        //Отключаем режим множественного выбора после добавления заметки
        if (isMultiSelectionEnabled) disableMultiSelection()
    }

    override fun updateItemsColor(bibleTextsForHighlighting: ArrayList<BibleTextModel>) {
        bibleTextFragment.getCurrentRecyclerViewAdapter().updateItemColor(bibleTextsForHighlighting)
    }

    override fun dismissColorPickerDialog(isColorSelected: Boolean) {
        colorPickerDialog?.dismiss()
        if (isColorSelected) disableMultiSelection()
    }
}
