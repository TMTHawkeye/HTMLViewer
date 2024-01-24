package json.file.viewer.opener.reader.Adaptor

import android.app.AlertDialog
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.json.file.opener.json.reader.utility.utils
import html.xhtml.viwer.html.mhtml.editor.Activities.HtmlFileViewerActivity
import html.xhtml.viwer.html.mhtml.editor.R
import html.xhtml.viwer.html.mhtml.editor.databinding.RowForHtmlFileBinding
import io.paperdb.Paper
import json.file.viewer.opener.reader.ModelClasses.HTMLModelClass
import java.io.File

class FileAdaptor(
    val context: Context,
    val arrayList: ArrayList<HTMLModelClass>,
    val type: String
) : RecyclerView.Adapter<FileAdaptor.ViewHolder>() {
    var renamePdf = ""
    var exampleListFull = ArrayList(arrayList)

    class ViewHolder(var binding: RowForHtmlFileBinding) : RecyclerView.ViewHolder(binding.root)

    lateinit var binding: RowForHtmlFileBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = RowForHtmlFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var file: File

        with(arrayList[position]) {
            with(holder) {
                file = File(filePath)
                binding.fileNameTV.text = file.name
                binding.fileSize.text = fileSize
                binding.fileDate.text = fileDate

                if (file.name.contains(".html")
                ) {
                    holder.binding.fileIcon.setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.html_view_svg)
                    )
                } else if (file.name.contains(".pdf")) {
                    holder.binding.fileIcon.setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.convert_pdf_svg)
                    )
                }
                binding.moreOptions.setOnClickListener {
                    val popupMenu = PopupMenu(context, holder.binding.moreOptions)
                    popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {

                            // Toast.makeText(context, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                            R.id.deleteOp -> {

                                val builder1: AlertDialog.Builder = AlertDialog.Builder(context)
                                builder1.setMessage("Are you sure you want to delete this item?")
                                builder1.setCancelable(true)

                                builder1.setPositiveButton(
                                    "Yes"
                                ) { dialog, _ ->
                                    if (type == "recent") {
                                        deleteRecentFile(context, position)
//                                     notifyItemRemoved(position)
//                                     notifyDataSetChanged()

                                    } else if (type == "pdf") {
                                        file.delete()
                                    } else if (type == "html") {
                                        deleteFileFromDevice(context, filePath)
                                    }
                                    arrayList.remove(arrayList[position])
                                    notifyDataSetChanged()
                                }

                                builder1.setNegativeButton(
                                    "No"
                                ) { dialog, id -> dialog.cancel() }

                                val alert11: AlertDialog = builder1.create()
                                alert11.show()
                            }
                            R.id.shareOp -> {
                                shareFileFromDevice(file, context)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }


                holder.binding.fileBtn.setOnClickListener {

                    addToRecent(arrayList[position].filePath)


                    if (file.name.contains(".pdf")) {
                        utils.updateToShPr(context, file.absolutePath)
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                context.getPackageName() + ".provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                        }
                    } else {
                        utils.updateToShPr(context, file.absolutePath)
                        val intent = Intent(context, HtmlFileViewerActivity::class.java)
                        intent.putExtra("path", arrayList[position].filePath)
                        context.startActivity(intent)
//                        globalcontext.furtherCall(intent)
                    }


                }
            }
        }

    }

    private fun addToRecent(filePath: String) {
        var recent_list = Paper.book().read("recent", ArrayList<String>())
        if(recent_list!!.contains(filePath)){
            recent_list.remove(filePath)
        }
        recent_list!!.add(filePath)
        Paper.book().write("recent",recent_list)
    }

    override fun getItemCount() = arrayList.size

    fun deleteRecentFile(context: Context, position: Int) {
        var recent_list = Paper.book().read("recent", ArrayList<String>())
        recent_list!!.remove(arrayList[position].filePath)
        Paper.book().write("recent",recent_list)

    }

    fun deleteFileFromDevice(context: Context, uri: String) {
        val file = File(uri)
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = MediaStore.Files.FileColumns.DATA + " = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        val contentResolver = context.contentResolver
        val c: Cursor? = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )
        if (c!!.moveToFirst()) {
            val id: Long = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val deleteUri: Uri =
                ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            contentResolver.delete(deleteUri, null, null)
            val nfile = File(uri)
            if (nfile.exists()) {
                nfile.delete()
            }
        } else {
            Toast.makeText(context, "file not found", Toast.LENGTH_SHORT).show()
        }
        c.close()
    }

    fun shareFileFromDevice(file: File, context: Context) {
        val photoURI = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName
                .toString() + ".provider",
            file
        )
        val intentShareFile = Intent(Intent.ACTION_SEND)
        intentShareFile.type = "*/*"
        intentShareFile.putExtra(Intent.EXTRA_STREAM, photoURI)

        intentShareFile.putExtra(
            Intent.EXTRA_SUBJECT,
            "Sharing File from" + context.getString(R.string.app_name)
        )

        context.startActivity(
            Intent.createChooser(
                intentShareFile,
                context.getString(R.string.app_name)
            )
        )
    }


    private val SearchFilter: Filter = object : Filter() {
        protected override fun performFiltering(constraint: CharSequence?): FilterResults? {
            val filteredList: ArrayList<HTMLModelClass> = ArrayList()
            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(exampleListFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }
                for (item in exampleListFull) {
                    if (item.fileName.toLowerCase()
                            .contains(filterPattern) || item.fileName.toUpperCase()
                            .contains(filterPattern)
                    ) {
                        filteredList.add(item)
                    }
//                    if (utils.GetAppName(item)!!.toLowerCase().contains(filterPattern)) {
//                        filteredList.add(item.toString())
//                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            arrayList.clear()
            arrayList.addAll(results.values as ArrayList<HTMLModelClass>)
            notifyDataSetChanged()
        }
    }

    fun getFilter(): Filter {
        return SearchFilter
    }


}