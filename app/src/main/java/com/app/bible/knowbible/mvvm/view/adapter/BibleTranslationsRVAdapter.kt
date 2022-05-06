package com.app.bible.knowbible.mvvm.view.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.BibleTranslationModel
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utils
import com.google.android.material.button.MaterialButton
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.muddzdev.styleabletoast.StyleableToast
import java.io.File
import java.io.FileOutputStream

class BibleTranslationsRVAdapter(
    private val context: Context,
    private val models: ArrayList<BibleTranslationModel>
) : RecyclerView.Adapter<BibleTranslationsRVAdapter.MyViewHolder>() {
    private val saveLoadData = SaveLoadData(context)

    companion object {
        const val isTranslationDownloading = "isTranslationDownloadingKey"
    }

    private lateinit var fragmentCommunication: IFragmentCommunication
    private lateinit var downloadedTranslationsList: ArrayList<String>

    interface IFragmentCommunication {
        fun openDatabase(translationObject: BibleTranslationModel)
        fun changeItemTheme()
        fun setIsTranslationDownloaded(isTranslationDownloaded: Boolean)
        fun setFilePathForDeleting(
            fileForDeleting: File?,
            fileToCancel: StorageTask<FileDownloadTask.TaskSnapshot>?
        )
    }

    fun setRecyclerViewFragmentCommunicationListener(themeChanger: IFragmentCommunication) {
        this.fragmentCommunication = themeChanger
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_bible_translation, parent, false)
        downloadedTranslationsList =
            ArrayList() //создавать объект коллекции нужно здесь, а не на уровне класса. Если на уровне класса, то возникает ошибка: объект не удаляются нормально
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //В зависимости от выбранной темы, выставляем нужные цвета для Views
//        if (ThemeManager.theme == ThemeManager.Theme.BOOK) {
//            holder.tvLanguageName.setTextColor(ContextCompat.getColor(context, R.color.colorGrayBookTheme))
//            holder.btnDownloadBibleTranslate.setTextColor(ContextCompat.getColor(context, android.R.color.black))
//            holder.downloadProgressBar.progressDrawable = ContextCompat.getDrawable(context, R.drawable.circle_progress_bar_book_theme)
//            holder.progressCountTitle.setTextColor(ContextCompat.getColor(context, android.R.color.black))
//            holder.ivDownloaded.setColorFilter(ContextCompat.getColor(context, android.R.color.black))
//        } else {
//            holder.tvLanguageName.setTextColor(ContextCompat.getColor(context, R.color.colorGray))
//            holder.btnDownloadBibleTranslate.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
//            holder.downloadProgressBar.progressDrawable = ContextCompat.getDrawable(context, R.drawable.circle_progress_bar)
//            holder.progressCountTitle.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
//            holder.ivDownloaded.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
//        }

        holder.tvTranslateName.text = models[position].translationName
        holder.tvAbbreviationName.text = models[position].abbreviationTranslationName
        holder.tvLanguageName.text = models[position].languageName

        val applicationFile = File(
            context.getExternalFilesDir(context.getString(R.string.folder_name))
                .toString() + "/" + models[position].translationDBFileName
        )
        if (!applicationFile.exists()) {
            holder.layProgressBar.visibility = View.GONE
            holder.ivDownloaded.visibility = View.GONE

            holder.btnDownloadBibleTranslate.visibility = View.VISIBLE
            //Это чтобы иконка ivDownloaded не показывалась, пока показывается ProgressBar
        } else if (applicationFile.exists() && !saveLoadData.loadBoolean(isTranslationDownloading)) {
            holder.btnDownloadBibleTranslate.visibility = View.GONE
            holder.layProgressBar.visibility = View.GONE

            holder.ivDownloaded.visibility = View.VISIBLE
        } else if (applicationFile.exists() && saveLoadData.loadBoolean(isTranslationDownloading)) {
            if (downloadedTranslationsList.contains(models[position].translationDBFileName)) {
                Utils.log(
                    "tag1",
                    "onBind downloadedTranslationsList size: " + downloadedTranslationsList.size.toString() + " | and data: " + downloadedTranslationsList[0]
                )
                Utils.log("tag1", "Adapter position: $position")
//                holder.btnDownloadBibleTranslate.visibility = View.GONE
//                holder.layProgressBar.visibility = View.VISIBLE
//
//                holder.ivDownloaded.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTranslateName: TextView = itemView.findViewById(R.id.tvTranslateName)
        var tvAbbreviationName: TextView = itemView.findViewById(R.id.tvAbbreviationName)
        var tvLanguageName: TextView = itemView.findViewById(R.id.tvLanguageName)

        var btnDownloadBibleTranslate: MaterialButton =
            itemView.findViewById(R.id.btnDownloadBibleTranslate)

        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        var layProgressBar: RelativeLayout = itemView.findViewById(R.id.layProgressBar)
        var downloadProgressBar: ProgressBar = itemView.findViewById(R.id.downloadProgressBar)
        var progressCountTitle: TextView = itemView.findViewById(R.id.progressCountTitle)

        var ivDownloaded: ImageView = itemView.findViewById(R.id.ivDownloaded)

        init {
            btnDownloadBibleTranslate.visibility = View.VISIBLE
            btnDownloadBibleTranslate.setOnClickListener {
                if (Utils.isNetworkAvailable(context)!!) {
                    downloadedTranslationsList.add(models[adapterPosition].translationDBFileName)
                    Utils.log(
                        "tag1",
                        "btnClicked downloadedTranslationsList size: " + downloadedTranslationsList.size.toString()
                    )
                    Utils.log("tag1", "Adapter position: $adapterPosition")
                    downloadFiles(
                        models[adapterPosition].translationDBFileName,
                        btnDownloadBibleTranslate,
                        progressBar,
                        layProgressBar,
                        downloadProgressBar,
                        progressCountTitle,
                        ivDownloaded
                    )
                } else {
                    StyleableToast.makeText(
                        context,
                        context.getString(R.string.toast_no_internet_connection),
                        Toast.LENGTH_SHORT,
                        R.style.my_toast
                    ).show()
                }
            }

            itemView.setOnClickListener {
                //Прописать здесь выбор перевода Библии. Если перевод не скачан, ничего не происходит и выводить Toast, что перевод не скачан
                val translate = File(
                    context.getExternalFilesDir(context.getString(R.string.folder_name))
                        .toString() + "/" + models[adapterPosition].translationDBFileName
                )
                if (translate.exists()) {
                    //Не позволяем выбрать перевод, пока хотя бы один перевод скачивается
                    if (saveLoadData.loadBoolean(isTranslationDownloading)) {
                        StyleableToast.makeText(
                            context,
                            context.getString(R.string.toast_please_wait),
                            Toast.LENGTH_SHORT,
                            R.style.my_toast
                        ).show()

                        return@setOnClickListener
                    }

                    (context as Activity).onBackPressed() //После выбора перевода закрываем фрагмент выбора переводов и человек переходит к фрагменту выбора Заветов, чтобы начать читать Библию
                    fragmentCommunication.openDatabase(models[adapterPosition])
                } else {
                    StyleableToast.makeText(
                        context, context.getString(R.string.toast_download_translation),
                        Toast.LENGTH_SHORT,
                        R.style.my_toast
                    ).show()
                }
            }

            fragmentCommunication.changeItemTheme() //Смена темы для айтемов
        }

        private val fireBaseStorage = FirebaseStorage.getInstance()
        private lateinit var storageReference: StorageReference //Это поле нужно в том случае, когда мы загружаем данные в FireBase

        //Скачиваем нужный перевод, при этом управляя видом айтема, показывая те вью, которые нужно
        private fun downloadFiles(
            translationFileName: String,
            btnDownloadBibleTranslate: MaterialButton,
            progressBar: ProgressBar,
            layProgressBar: RelativeLayout,
            downloadProgressBar: ProgressBar,
            progressCountTitle: TextView,
            ivDownloaded: ImageView
        ) {
            val listRef = fireBaseStorage.reference.child("bible_translations/")
            val applicationFile = File(
                context.getExternalFilesDir(context.getString(R.string.folder_name))
                    .toString() + "/" + translationFileName
            )

            //Между тем, как нажата кнопка и начинается загрузка, мы выключаем кнопку и включаем progressBar, показывающий ожидание
            btnDownloadBibleTranslate.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            //Можно было сразу скачивать нужный файл, а не сначала получать список, потом находить нужный файл. Но так сделано по причине того,
            //что список моделов хранится также и локально, чтобы можно было отобразить список даже когда нет интернета.
            listRef.listAll()
                .addOnSuccessListener { listResult ->
                    //Берём текстовое название файла и добавляем в коллекцию
//                                val secondList: ArrayList<String> = ArrayList()
//                                listResult.items.forEach { item ->
//                                secondList.add(item.toString())
//                          }

//                                val twentyMegabytes: Long = 20971520 //это 20мб в байтах, нужно, чтобы установить лимит на скачивание данных

                    //Сравниваем, какое имя файла из нажатого айтема соответствует имени файла в FireBase. Находим и уже тогда скачиваем.
                    var item: StorageReference? = null
                    listResult.items.forEach { element ->
                        //Тут приходит имя файла вместе со всей остальной адресной строкой, оно очищается и потом сравниваются уже сугубо имена файлов.
                        var fileNameOnFireBase = element.toString()
                        val index = fileNameOnFireBase.lastIndexOf('/')
                        fileNameOnFireBase =
                            fileNameOnFireBase.substring(index + 1, fileNameOnFireBase.length)
                        if (fileNameOnFireBase == translationFileName) {
                            item = element
                        }
                    }

                    item
                        ?.getFile(applicationFile)
                        ?.addOnProgressListener {
                            //Отправляем данные, скачивание которых в случае резкого закрытия приложения будет приостановлено.
                            //Данные отправляются при каждом срабатывании метода addOnProgressListener, а срабатывает он много раз в процессе скачиваня.
                            //К сожалению, только такой вариант возможен, потому что отправлять нужно объект, который приходит на вход именно этому методу.
                            fragmentCommunication.setFilePathForDeleting(applicationFile, it.task)
                            saveLoadData.saveBoolean(
                                isTranslationDownloading,
                                true
                            ) //Устаналиваем значение, которое говорит о том, что хотя бы один перевод скачивается

                            progressBar.visibility = View.GONE
                            layProgressBar.visibility = View.VISIBLE

                            val progress: Double = 100.0 * it.bytesTransferred / it.totalByteCount

                            progressCountTitle.text = progress.toInt().toString() + "%"
                            downloadProgressBar.progress = progress.toInt()

                            Utils.log("Uploaded " + progress.toInt() + "%")
                        }?.addOnSuccessListener {
                            fragmentCommunication.setFilePathForDeleting(
                                null,
                                null
                            ) //Файл скачан, удалять его не надо при закрытии приложения, а значит устанавливаем пустое значение
                            saveLoadData.saveBoolean(
                                isTranslationDownloading,
                                false
                            ) //Устаналиваем значение, которое говорит о том, что перевод скачан

                            downloadedTranslationsList.remove(models[adapterPosition].translationDBFileName)
                            Utils.log(
                                "tag1",
                                "downloadSuccess downloadedTranslationsList size: " + downloadedTranslationsList.size.toString()
                            )
                            Utils.log("tag1", "Adapter position: $adapterPosition")

                            StyleableToast.makeText(
                                context,
                                context.getString(R.string.toast_translation_saved),
                                Toast.LENGTH_SHORT,
                                R.style.my_toast
                            ).show()

                            layProgressBar.visibility = View.GONE
                            ivDownloaded.visibility = View.VISIBLE

                            fragmentCommunication.setIsTranslationDownloaded(true)
                        }?.addOnFailureListener {
                            Utils.log(it.toString())
                        }
                }
                .addOnFailureListener {}
        }

        private fun saveFile(byteArray: ByteArray, applicationFile: File) {
            val fos = FileOutputStream(applicationFile)
            fos.write(byteArray)
            fos.flush()
            fos.close()

            StyleableToast.makeText(
                context, "File saved", Toast.LENGTH_SHORT,
                R.style.my_toast
            ).show()
        }
    }
}