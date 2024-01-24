package html.xhtml.viwer.html.mhtml.editor.Activities

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import html.xhtml.viwer.html.mhtml.editor.databinding.ActivityFilesListViewerBinding
import json.file.viewer.opener.reader.Adaptor.FileAdaptor
import json.file.viewer.opener.reader.ModelClasses.HTMLModelClass
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class FilesListViewerActivity : AppCompatActivity() {
    lateinit var modelArrayList: ArrayList<HTMLModelClass>
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    lateinit var adapter: FileAdaptor
    lateinit var binding:ActivityFilesListViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilesListViewerBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        modelArrayList = ArrayList()

        executor.execute {
            getFilesFromDevice()

            runOnUiThread{
                if (modelArrayList.size < 1) {
                    binding.searchBtn.visibility=View.GONE
                }
            }

            handler.post {
                binding.filesRV.layoutManager = LinearLayoutManager(this)
                binding.filesRV.setHasFixedSize(true)
                Log.d(ContentValues.TAG, "onCreateView: modelarray${modelArrayList.size}")
                adapter = FileAdaptor(this, modelArrayList, "html")
                binding.filesRV.adapter = adapter
                binding.progressBar.visibility = View.GONE
                if (modelArrayList.size > 0) {
                    binding.nofileIV.visibility = View.GONE
                    binding.nofileTV.visibility = View.GONE
                } else {
                    binding.nofileIV.visibility = View.VISIBLE
                    binding.nofileTV.visibility = View.VISIBLE
                }
            }
        }

        binding.searchBoxContainer.clearSearchQueryBtn.setOnClickListener {
            val text=binding.searchBoxContainer.searchEditText.text.toString()
            if (text.equals("")) {
                binding.searchBoxContainer.searchCardView.visibility = View.GONE
                binding.searchBtn.visibility = View.VISIBLE
            } else {
                binding.searchBoxContainer.searchEditText.text = null

            }
        }
        binding.searchBtn.setOnClickListener {
            binding.searchBoxContainer.searchCardView.visibility = View.VISIBLE
        }

        binding.searchBoxContainer.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                // filter your list from your input
                adapter.getFilter().filter(s)
                //  filter(s.toString())
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        })

    }

    private fun getFilesFromDevice() {
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.TITLE,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Images.ImageColumns.DATE_MODIFIED
        )
        val content = contentResolver
        val file_Cursor = content.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            null,
            null,
            null
        )
        if (file_Cursor != null) {
            var counter = 0
            while (file_Cursor.moveToNext()) {
                counter++
                val uri = file_Cursor.getString(0)
                val title = file_Cursor.getString(1)
                val size = file_Cursor.getString(2)
                val date = file_Cursor.getString(3)
                if (uri.endsWith(".html")) {
                    var fileDate=getFileDate(date)
                    val finalSize=getStringSizeLengthFile(size.toLong())
                    modelArrayList.add(HTMLModelClass(title, finalSize, uri,fileDate))

                    Log.d("TAG", "getHtmlFromDevice: "+fileDate)

                }
            }
            file_Cursor.close()
        }

    }

    fun getFileDate(datee: String): String {
        // Create a DateFormatter object for displaying date in specified format.
        val dateFormat = "dd-MM-yyyy"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        var dateee=datee.toLong()
        calendar.timeInMillis = dateee
        return formatter.format(calendar.time)
    }

    fun getStringSizeLengthFile(size: Long): String {
        val df = DecimalFormat("0")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        if (size < sizeMb) return (df.format((size / sizeKb).toDouble()*1024) ).toString()+ " Kb"
        else if (size < sizeGb) return df.format(
            (size / sizeMb).toDouble()
        ) + " Mb" else if (size < sizeTerra) return df.format((size / sizeGb).toDouble()) + " Gb"
        return ""
    }

}