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
import android.os.Build
import android.util.Base64
import android.util.Base64.NO_WRAP
import android.util.Log
import android.view.Gravity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import ru.aasmc.petfinder.common.utils.DataValidator.Companion.isValidJPEGAtPath
import ru.aasmc.petfinder.common.utils.Encryption
import ru.aasmc.petfinder.common.utils.Encryption.Companion.encryptFile
import ru.aasmc.petfinder.databinding.FragmentReportDetailBinding
import ru.aasmc.petfinder.main.presentation.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
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
        private const val REPORT_APP_ID = 46341
        private const val REPORT_PROVIDER_ID = 46341
        private const val REPORT_SESSION_KEY = "session_for_custom_encryption"
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
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = activity?.contentResolver?.query(
                        selectedImage, filePathColumn,
                        null, null, null
                    )
                    cursor?.moveToFirst()
                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                    var decodableImageString = ""
                    columnIndex?.let {
                        decodableImageString = cursor.getString(it)
                    }
                    cursor?.close()
                    showUploadedFile(selectedImage, decodableImageString)
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
            var success = false

            //1. Save report
            var reportString = binding.categoryEdtxtview.text.toString()
            reportString += " : "
            reportString += binding.detailsEdtxtview.text.toString()
            // sanitize report string here by stripping it of any potentially
            // vulnerable characters that can be used for like SQL injections
            // in the server
            reportString = reportString.replace("\\", "")
                .replace(";", "")
                .replace("%", "")
                .replace("\"", "")
                .replace("\'", "")

            val reportID = UUID.randomUUID().toString()

            context?.let { theContext ->
                val file = File(theContext.filesDir?.absolutePath, "$reportID.txt")
                val encryptedFile = encryptFile(theContext, file)
                encryptedFile.openFileOutput().bufferedWriter().use {
                    it.write(reportString)
                }
            }

            testCustomEncryption(reportString)

            ReportTracker.reportNumber.incrementAndGet()

            //2. Send report
            val mainActivity = activity as MainActivity
            var requestSignature = ""
            // 1: concatenate the parameters of the request string
            val stringToSign = "$REPORT_APP_ID+$reportID+$reportString"
            // 2: convert the string into a ByteArray
            val bytesToSign = stringToSign.toByteArray(Charsets.UTF_8)
            // 3: sign the bytes using private key and return the signature bytes
            val signedData = mainActivity.clientAuthenticator.sign(bytesToSign)
            // 4: turn the signature bytes into a Base64 string that can be easily
            // sent over the network.
            requestSignature = Base64.encodeToString(signedData, Base64.NO_WRAP)
            val postParameters = mapOf(
                "application_id" to REPORT_APP_ID,
                "report_id" to reportID,
                "report" to reportString,
                "signature" to requestSignature
            )
            if (postParameters.isNotEmpty()) {
                //send report
                mainActivity.reportManager.sendReport(postParameters) {
                    val reportSent: Boolean = it["success"] as Boolean
                    if (reportSent) {
                        // todo verify signature here
                        val serverSignature = it["signature"] as String
                        val signatureBytes = Base64.decode(serverSignature, Base64.NO_WRAP)

                        val confirmationCode = it["confirmation_code"] as String
                        val confirmationBytes = confirmationCode.toByteArray(Charsets.UTF_8)

                        success = mainActivity.clientAuthenticator.verify(
                            signatureBytes,
                            confirmationBytes, mainActivity.serverPublicKeyString
                        )
                    }
                    onReportReceived(success)
                }
            }
        }
    }

    private fun onReportReceived(success: Boolean) {
        isSendingReport = false
        if (success) {
            context?.let {
                val report = "Report: ${ReportTracker.reportNumber.get()}"
                val toast = Toast.makeText(
                    it, "Thank you for your report.$report", Toast
                        .LENGTH_LONG
                )
                toast.show()
            }
        } else {
            val toast = Toast.makeText(
                context, "There was a problem sending the report", Toast
                    .LENGTH_LONG
            )
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        }
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun testCustomEncryption(reportString: String) {
        val password = REPORT_SESSION_KEY.toCharArray()
        val bytes = reportString.toByteArray(Charsets.UTF_8)
        val map = Encryption.encrypt(bytes, password)
        val reportId = UUID.randomUUID().toString()
        val outFile = File(activity?.filesDir?.absolutePath, "$reportId.txt")
        ObjectOutputStream(FileOutputStream(outFile)).use {
            it.writeObject(map)
        }

        // TEST decrypt
        val decryptedBytes = Encryption.decrypt(map, password)
        Log.d("Encryption test", "before showing dectypted bytes, size: ${decryptedBytes?.size}")
        decryptedBytes?.let {
            val decryptedString = String(it, Charsets.UTF_8)
            Log.d("Encryption test", "the decrypted string is: $decryptedString")
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

    private fun showUploadedFile(selectedImage: Uri, decodableImageString: String?) {
        val isValid = isValidJPEGAtPath(decodableImageString)
        if (isValid) {
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
        } else {
            Toast.makeText(context, "Please choose a JPEG image", Toast.LENGTH_SHORT).show()
        }
    }
}





























