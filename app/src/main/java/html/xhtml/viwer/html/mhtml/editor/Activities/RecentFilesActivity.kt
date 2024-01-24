package html.xhtml.viwer.html.mhtml.editor.Activities

import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import html.xhtml.viwer.html.mhtml.editor.databinding.ActivityRecentFilesBinding
import io.paperdb.Paper
import json.file.viewer.opener.reader.Adaptor.FileAdaptor
import json.file.viewer.opener.reader.ModelClasses.HTMLModelClass
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class RecentFilesActivity : AppCompatActivity() {
    lateinit var binding:ActivityRecentFilesBinding
    lateinit var arrayList: ArrayList<HTMLModelClass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRecentFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrayList = ArrayList()


        var array=Paper.book().read("recent",ArrayList<String>())
        if (array!!.size > 0) {
            for (i in 0 until array.size) {
                val file = File(array[i])
                val file_Name = file.name
                val file_size = getStringSizeLengthFile(file.length())
                val file_Path = file.absolutePath
                var file_date=file.lastModified()
                val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = df.format(file_date)
                arrayList.add(HTMLModelClass(file_Name, file_size.toString(), file_Path,formattedDate))

                Log.d(ContentValues.TAG, "data:${array.get(i)}")
            }
        } else {
            binding.nofileTV.visibility = View.VISIBLE
            binding.nofileIV.visibility = View.VISIBLE
        }

        if (arrayList.size < 1) {
            binding.searchBtn.visibility=View.GONE
        }

        binding.recentRV.layoutManager = LinearLayoutManager(this)
        binding.recentRV.setHasFixedSize(true)
        val adapter = FileAdaptor(this, arrayList, "recent")
        binding.recentRV.adapter = adapter

        binding.backBtn.setOnClickListener{
            finish()
        }


        binding.searchBoxContainer.clearSearchQueryBtn.setOnClickListener {
            val text=binding.searchBoxContainer.searchEditText.text.toString()
            Log.e("ffftxxxt", "onCreate: "+text )

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