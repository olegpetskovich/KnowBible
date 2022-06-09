package com.app.bible.knowbible.mvvm.view.fragment.articles_section

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.bible.knowbible.App
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.ArticleModel
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabArticlesNumber
import com.app.bible.knowbible.mvvm.view.adapter.ArticlesRVAdapter
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.mvvm.viewmodel.ArticlesViewModel
import com.app.bible.knowbible.utility.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.muddzdev.styleabletoast.StyleableToast
import io.grpc.Status
import java.net.ConnectException
import javax.net.ssl.SSLHandshakeException

class ArticlesFragment : Fragment(), IThemeChanger, IChangeFragment {
    private lateinit var listener: IActivityCommunicationListener

    private lateinit var myFragmentManager: FragmentManager

    private val firestoreDB = FirebaseFirestore.getInstance() //Cloud Firestore DB field
    private val fireBaseStorage = FirebaseStorage.getInstance()
    private lateinit var auth: FirebaseAuth

    //    private val dataRefArticles = firestoreDB.collection("articles_test") //Тестовое API
    private val dataRefArticles = firestoreDB.collection("articles") //

    private lateinit var articlesViewModel: ArticlesViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var progressBarArticle: ProgressBar
    private lateinit var layNoInternet: RelativeLayout

    private lateinit var recyclerView: RecyclerView
    private var articlesRVAdapter: ArticlesRVAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.log("onCreate()")
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_articles, container, false)
        Utils.log("onCreateView()")
        listener.setTheme(
            ThemeManager.theme,
            false
        ) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение
        recyclerView = myView.findViewById(R.id.recyclerView)

        // Initialize Firebase Auth
        auth = Firebase.auth

        swipeRefreshLayout = myView.findViewById(R.id.swipeRefreshLayout)
        progressBarArticle = myView.findViewById(R.id.progressBarArticle)
        layNoInternet = myView.findViewById(R.id.layNoInternet)

        swipeRefreshLayout.setOnRefreshListener { context?.let { loadData(it) } }

//        //Этот код нужен для того, чтобы приложение открывалось на эмуляторе без ошибки,
//        //но если его оставлять, то на телефонах приложение запускаться не будет, поэтому закомментировали
//        // 10.0.2.2 is the special IP address to connect to the 'localhost' of
//        // the host computer from an Android emulator.
//        firestoreDB.useEmulator("10.0.2.2", 8080)
//        val settings = FirebaseFirestoreSettings.Builder()
//                .setPersistenceEnabled(false)
//                .build()
//        firestoreDB.firestoreSettings = settings

        return myView
    }

    private fun isSslHandshakeError(status: Status): Boolean {
        val code: Status.Code = status.code
        val t: Throwable? = status.cause
        return (code == Status.Code.UNAVAILABLE
                && (t is SSLHandshakeException
                || t is ConnectException && t.message!!.contains("EHOSTUNREACH")))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
            val articlesFragment = ArticlesFragment()
            articlesFragment.setRootFragmentManager(it)
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_articles, articlesFragment)
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
        articlesViewModel = ViewModelProvider(this)[ArticlesViewModel::class.java]
    }

    //Загружать данные в onStart, потому что загружая в onCreate выдаёт ошибку | ЗДЕСЬ ЗАГРУЖАЕМ ДАННЫЕ В onStart(), А ОБНОВЛЯЕМ СПИСОК В onResume(), ЧТОБЫ ПОЛУЧАТЬ ДАННЫЕ ТОЛЬКО ОДНАЖДЫ, А ОБНОВЛЯТЬ СПИСОК ДЛЯ ОБНОВЛЕНИЯ ТЕМЫ АЙТЕМОВ
    override fun onStart() {
        super.onStart()
        //Реализовываем анонимную аутентификацию пользователя
        //Если пользователь авторизировался, то просто загружаем статьи, если нет, то сначала аутентификация, а потом загрузка статей
        val currentUser = auth.currentUser
        if (currentUser != null)
            loadData(requireContext())
        else {
            activity?.let {
                auth.signInAnonymously()
                    .addOnCompleteListener(it) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Utils.log("signInAnonymously:success")
//                            Toast.makeText(context, "Authentication success.", Toast.LENGTH_SHORT).show()
                            loadData(requireContext())
                        } else {
                            // If sign in fails, display a message to the user.
                            Utils.log("signInAnonymously:failure: " + task.exception)
//                            Toast.makeText(context, "Authentication failed: " + task.exception, Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        Utils.log("onStart()")
    }

    override fun onResume() {
        super.onResume()
        Utils.log("onResume()")

        if (articlesRVAdapter != null)
            articlesRVAdapter!!.notifyDataSetChanged()

        listener.setShowHideArticlesInfoButton(View.VISIBLE) //Устанавливаем видимость кнопки btnArticlesInfo

        listener.setTabNumber(tabArticlesNumber)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(false)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.GONE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        Utils.log("onPause()")

        listener.setShowHideArticlesInfoButton(View.GONE) //Устанавливаем видимость кнопки btnArticlesInfo
    }

    //В данном методем реализован алгоритм кеширования, в котором данные сохраняются при первой их загрузке, а потом загружаются из локальной БД. При закрытии приложения локальная БД удаляется.
    private fun loadData(context: Context) {
        //Выставляем количество столбиков в RecyclerView
        val orientation = context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) recyclerView.layoutManager =
            GridLayoutManager(context, 2)
        else recyclerView.layoutManager = GridLayoutManager(context, 3)

        if (App.articlesData == null) {
            if (Utils.isNetworkAvailable(context)) { //Если интернет есть - грузим данные, если интернета нет, то работа метода прекращается вызовом return
                //Убираем swipeRefreshLayout в случае, когда интернет есть, чтобы пользователь не делал не нужных запросов и не тратил запас предоставленных ресурсов Firebase.
                //То есть обновление списка можно только в случае отсутствия интернета
                swipeRefreshLayout.isRefreshing = false
                swipeRefreshLayout.visibility = View.GONE
                dataRefArticles
                    .get()
                    .addOnSuccessListener { data ->
                        Utils.log("Internet request")
                        var articlesData =
                            data.toObjects(ArticleModel::class.java) //Здесь ради удобства использования используется модел на Java

                        //Фильтрация списка
                        //Делаем проверку на наличие значения в поле new_article_text_color у каждой их статей, чтобы тем самым определять, можно ли оставлять статью в списке
                        //Потому что в Cloud Firestore в моделе статьи поле new_article_text_color стоит последним и если оно заполнено,
                        //значит остальнные данные тоже заполнены, а это значит, что статью можно принимать и публиковать,
                        //иначе же отсутствие данных в поле new_article_text_color говорит о том, что прошлые поля тоже не заполнены,
                        //а значит при попытке отобразить статью, приложение будет падать,
                        //поэтому если данные в CloudFirestore для статьи не заполнены - она удаляется из списка,
                        //чтобы приложение не падало из-за отстутствия данных
                        articlesData = articlesData.filter { obj -> obj.new_article_text_color != null && obj.new_article_text_color.isNotEmpty() }

                        articlesData.reverse() //Переворачиваем список статей, чтобы последняя опубликованная статья была вверху списка

                        articlesData.forEachIndexed { index, articleModel ->

                            val listRef = fireBaseStorage.reference.child(articleModel.image)
                            listRef.downloadUrl.addOnSuccessListener { pictureUri ->
                                if (isAdded && !requireActivity().isDestroyed) {
                                    Glide.with(context)
                                        .asBitmap()
                                        .load(pictureUri)
                                        .into(object : CustomTarget<Bitmap>(700, 400) {
                                            override fun onLoadCleared(placeholder: Drawable?) {}

                                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                articleModel.imageBitmap =
                                                    resource //Устаналиваем картинку в виде Bitmap, чтобы сразу отобразить в списке и сохранить в бд
                                                progressBarArticle.visibility = View.GONE
                                                layNoInternet.visibility = View.GONE

                                                val listSize = articlesData.size - 1
                                                if (index == listSize) {
                                                    articlesRVAdapter = ArticlesRVAdapter(
                                                        context,
                                                        articlesData as ArrayList<ArticleModel>
                                                    )
                                                    articlesRVAdapter!!.setRecyclerViewThemeChangerListener(
                                                        this@ArticlesFragment
                                                    )
                                                    articlesRVAdapter!!.setFragmentChangerListener(this@ArticlesFragment)
                                                    recyclerView.adapter = articlesRVAdapter

                                                    App.articlesData =
                                                        articlesData //Сохраняем данные в статическое поле, чтобы закешировать данные на время работы приложения. Это сэкономит количество делаемых запросов для получения данных. Один раз получили данные и они используется на протяжении всего времени, когда приложение включено.
                                                }
                                            }
                                        })
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        progressBarArticle.visibility = View.GONE
                        layNoInternet.visibility = View.GONE

                        StyleableToast.makeText(
                            context,
                            e.message,
                            Toast.LENGTH_SHORT,
                            R.style.my_toast
                        ).show()

                        Utils.log(e.toString())
                    }
            } else {
                swipeRefreshLayout.isRefreshing = false
                swipeRefreshLayout.visibility = View.VISIBLE
                progressBarArticle.visibility = View.GONE
                layNoInternet.visibility = View.VISIBLE

                StyleableToast.makeText(
                    context,
                    context.getString(R.string.toast_no_internet_connection),
                    Toast.LENGTH_SHORT,
                    R.style.my_toast
                ).show()
                return
            }
        } else {
            Utils.log("Local request")

            swipeRefreshLayout.isRefreshing = false
            swipeRefreshLayout.visibility = View.GONE
            progressBarArticle.visibility = View.GONE
            articlesRVAdapter = ArticlesRVAdapter(context, App.articlesData!!)
            articlesRVAdapter!!.setRecyclerViewThemeChangerListener(this)
            articlesRVAdapter!!.setFragmentChangerListener(this)

            recyclerView.adapter = articlesRVAdapter
        }
    }

    override fun changeItemTheme() {
        listener.setTheme(
            ThemeManager.theme,
            false
        ) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение
    }

    override fun changeFragment(fragment: Fragment) {
        myFragmentManager.let {
            val myFragment = fragment as ArticleFragment
            myFragment.setRootFragmentManager(myFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
//            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
//            transaction.setCustomAnimations( R.animator.slide_up, 0, 0, R.animator.slide_down)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container_articles, myFragment)
            transaction.commit()
        }
    }

    override fun onStop() {
        super.onStop()
        Utils.log("onStop()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Utils.log("onDestroyView()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.log("onDestroy()")
    }
}
