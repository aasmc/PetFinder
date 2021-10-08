package ru.aasmc.petfinder.report

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import ru.aasmc.petfinder.databinding.FragmentReportDetailBinding
import java.io.File
import java.io.RandomAccessFile
import java.net.URL
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.HttpsURLConnection

@AndroidEntryPoint
class ReportDetailFragment : Fragment() {
    companion object {
        private const val API_URL = "https://example.com/?send_report"
        private const val PIC_FROM_GALLERY = 2
        private const val REPORT_APP_ID = 46341L
        private const val REPORT_PROVIDER_ID = 46341L
        private const val REPORT_SESSION_KEY = "session_key_in_next_chapter"
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchGalleryIntent()
            } else {
                // todo handle denial of permission by user
            }
        }

    private val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //image from gallery
                val selectedImage = result?.data?.data
                selectedImage?.let {
                    showUploadedFile(selectedImage)
                }
            } else {
                // todo handle result not OK
            }
        }

    object ReportTracker {
        var reportNumber = AtomicInteger()
    }

    @Volatile
    private var isSendingReport = false

    private var _binding: FragmentReportDetailBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)

        binding.sendButton.setOnClickListener {
            sendReportPressed()
        }

        binding.uploadPhotoButton.setOnClickListener {
            uploadPhotoPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    override fun onPause() {
        clearCaches()
        super.onPause()
    }

    private fun clearCaches() {
        context?.cacheDir?.deleteRecursively()
        context?.externalCacheDir?.deleteRecursively()
    }

    private fun setupUI() {
        binding.detailsEdtxtview.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.detailsEdtxtview.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    private fun launchGalleryIntent() {
        val galleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(galleryIntent)
    }

    private fun sendReportPressed() {
        if (!isSendingReport) {
            isSendingReport = true

            //1. Save report
            var reportString = binding.categoryEdtxtview.text.toString()
            reportString += " : "
            reportString += binding.detailsEdtxtview.text.toString()
            val reportID = UUID.randomUUID().toString()

            context?.let { theContext ->
                val file = File(theContext.filesDir?.absolutePath, "$reportID.txt")
                file.bufferedWriter().use {
                    it.write(reportString)
                }
            }

            ReportTracker.reportNumber.incrementAndGet()

            //2. Send report
            val postParameters = mapOf(
                "application_id" to REPORT_APP_ID * REPORT_PROVIDER_ID,
                "report_id" to reportID,
                "report" to reportString
            )
            if (postParameters.isNotEmpty()) {
                //send report
                val connection = URL(API_URL).openConnection() as HttpsURLConnection
                //...
            }

            isSendingReport = false
            context?.let {
                val report = "Report: ${ReportTracker.reportNumber.get()}"
                val toast = Toast.makeText(
                    it, "Thank you for your report.$report", Toast
                        .LENGTH_LONG
                )
                toast.show()
            }

            val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    private fun uploadPhotoPressed() {
        context?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                launchGalleryIntent()
            }
        }
    }

    private fun showUploadedFile(selectedImage: Uri) {
        // get filename
        val fileNameColumn = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val nameCursor = activity?.contentResolver?.query(
            selectedImage, fileNameColumn, null, null, null
        )

        nameCursor?.moveToFirst()
        val nameIndex = nameCursor?.getColumnIndex(fileNameColumn[0])
        var fileName = ""
        nameIndex?.let {
            fileName = nameCursor.getString(it)
        }
        nameCursor?.close()

        // update UI with filename
        binding.uploadStatusTextview.text = fileName
    }
}





























