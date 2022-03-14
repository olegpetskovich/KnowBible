package com.app.bible.knowbible.mvvm.view.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.ArticleModel
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.app.bible.knowbible.mvvm.view.fragment.articles_section.ArticleFragment
import com.app.bible.knowbible.mvvm.view.fragment.more_section.AppLanguageFragment.Companion.APP_LANGUAGE_CODE_KEY
import com.app.bible.knowbible.utility.SaveLoadData
import com.muddzdev.styleabletoast.StyleableToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//Поле isDataFromLocalDB нужна, чтобы определить, откуда взяты данные, с Firebase или с сохранённой БД
class ArticlesRVAdapter(private val context: Context, private val models: ArrayList<ArticleModel>) :
    RecyclerView.Adapter<ArticlesRVAdapter.MyViewHolder>() {
    private val saveLoadData = SaveLoadData(context)

    private lateinit var fragmentChanger: IChangeFragment
    fun setFragmentChangerListener(fragmentChanger: IChangeFragment) {
        this.fragmentChanger = fragmentChanger
    }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_article, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //Устанавливаем анимацию на item
        setAnimation(holder.itemView, position)

        when (saveLoadData.loadString(APP_LANGUAGE_CODE_KEY)) {
            "en" -> {
                holder.tvArticleName.text = models[position].article_name_en

                models[position].article_text_en =
                    models[position].article_text_en.replace("NL", "\n") //NL - New Line
            }
            "ru" -> {
                holder.tvArticleName.text = models[position].article_name_ru

                models[position].article_text_ru =
                    models[position].article_text_ru.replace("NL", "\n") //NL - New Line
            }
            "uk" -> {
                holder.tvArticleName.text = models[position].article_name_uk

                models[position].article_text_uk =
                    models[position].article_text_uk.replace("NL", "\n") //NL - New Line
            }

        }

        if (models[position].isIs_article_new) {
            holder.tvNewArticle.visibility = View.VISIBLE
            holder.tvNewArticle.setTextColor(Color.parseColor(models[position].new_article_text_color))
        } else holder.tvNewArticle.visibility = View.GONE

        holder.ivArticleImage.setImageBitmap(models[position].imageBitmap)

        //Весь этот код необходим потому, что в Книжной теме непонятно почему высота устанавливается на всю высоту экрана, даже если общая высота всех View намного меньше.
        //Из-за этого на экране получается огромный пробел.
        //Поскольку параметры View такие как высота, ширина и т.д. можно получить именно тогда, когда они уже отрисованы, необходимо их запрашивать именно в этот момент.
        //Поэтому мы используем обработчик изменения layout, которые позволяет нам получить необходимые параметры тогда, когда все View уже отрисованы.
        holder.itemView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                holder.itemView.removeOnLayoutChangeListener(this) //На stackOverflow советуют удалять обработчик после первого вызова.
                //В этих двух полях собираем вместе параметры, которые дадут общую нужную высоту layout, которая будет установлена далее в mainLayout.layoutParams
                val layHeightValue = holder.ivArticleImage.height + holder.tvArticleName.height
                val laysMarginsValue =
                    (holder.tvArticleName.layoutParams as ConstraintLayout.LayoutParams).bottomMargin + (holder.tvArticleName.layoutParams as ConstraintLayout.LayoutParams).topMargin

                val mainHandler = Handler(context.mainLooper)
                val myRunnable = Runnable {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(25)
                        val params: ViewGroup.LayoutParams = holder.itemView.layoutParams
                        params.height = layHeightValue + laysMarginsValue + 50
                        holder.itemView.layoutParams = params
                    }
                }
                mainHandler.post(myRunnable)
            }
        })
    }

    private var lastPosition = -1
    private fun setAnimation(
        viewToAnimate: View,
        position: Int
    ) { // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNewArticle: TextView = itemView.findViewById(R.id.tvNewArticle)
        val ivArticleImage: ImageView = itemView.findViewById(R.id.ivArticleImage)
        val tvArticleName: TextView = itemView.findViewById(R.id.tvArticleName)

        init {
            themeChanger.changeItemTheme() //Смена темы для айтемов

            itemView.setOnClickListener {
                //Отключать возможность открывать статью, пока идёт скачивание. Потому что если этого не делать, то статья откроется,
                //но нажать кнопку "Назад" не получится, потому что во время скачивания кнопка "Назад" отключается
                if (saveLoadData.loadBoolean(BibleTranslationsRVAdapter.isTranslationDownloading)) {
                    StyleableToast.makeText(
                        context,
                        context.getString(R.string.toast_please_wait),
                        Toast.LENGTH_SHORT,
                        R.style.my_toast
                    ).show()
                    return@setOnClickListener
                }

                val bitmap = (ivArticleImage.drawable as BitmapDrawable).bitmap
                if (bitmap != null) {
                    val articleFragment = ArticleFragment()
                    when (saveLoadData.loadString(APP_LANGUAGE_CODE_KEY)) {
                        "ru" -> {
                            articleFragment.setArticleData(
                                bitmap,
                                models[absoluteAdapterPosition].article_name_ru,
                                models[absoluteAdapterPosition].article_text_ru,
                                models[absoluteAdapterPosition].author_name_ru,
                                models[absoluteAdapterPosition].telegram_link,
                                models[absoluteAdapterPosition].instagram_link
                            )
                        }
                        "uk" -> {
                            articleFragment.setArticleData(
                                bitmap,
                                models[absoluteAdapterPosition].article_name_uk,
                                models[absoluteAdapterPosition].article_text_uk,
                                models[absoluteAdapterPosition].author_name_uk,
                                models[absoluteAdapterPosition].telegram_link,
                                models[absoluteAdapterPosition].instagram_link
                            )
                        }
                        "en" -> {
                            articleFragment.setArticleData(
                                bitmap,
                                models[absoluteAdapterPosition].article_name_en,
                                models[absoluteAdapterPosition].article_text_en,
                                models[absoluteAdapterPosition].author_name_en,
                                models[absoluteAdapterPosition].telegram_link,
                                models[absoluteAdapterPosition].instagram_link
                            )
                        }
                    }
                    fragmentChanger.changeFragment(articleFragment)
                }
            }
        }
    }
}