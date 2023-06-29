package com.example.pdf_reader
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var pdfView: PDFView
    private lateinit var openPdfButton: Button
    private val STORAGE_PERMISSION_REQUEST_CODE = 1
    private val PICK_PDF_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pdfView = findViewById(R.id.pdf_view)
        openPdfButton = findViewById(R.id.btn_open_pdf)

        openPdfButton.setOnClickListener {
            openPDFPicker()
        }

        if (checkStoragePermission()) {
            openPdfButton.isEnabled = true
        } else {
            requestStoragePermission()
        }
    }

    private fun checkStoragePermission(): Boolean {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        val result = ContextCompat.checkSelfPermission(this, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        ActivityCompat.requestPermissions(this, arrayOf(permission), STORAGE_PERMISSION_REQUEST_CODE)
    }

    private fun openPDFPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, PICK_PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val file = getFileFromUri(uri)
                displayPDFFile(file)
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val fileName = it.getString(nameIndex)
                val file = File(cacheDir, fileName)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                return file
            }
        }
        return null
    }


    private fun displayPDFFile(file: File?) {
        file?.let {
            pdfView.fromFile(file)
                .onPageChange { page, pageCount ->
                    val pageNumberTextView = findViewById<TextView>(R.id.page_number_text_view)
                    val pageText = "Page ${page + 1} of $pageCount"
                    pageNumberTextView.text = pageText
                }
                .load()
        }
    }



}