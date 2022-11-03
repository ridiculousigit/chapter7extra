package binar.academy.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import binar.academy.myapplication.databinding.ActivityMainBinding
import binar.academy.myapplication.viewmodel.UserViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var image: MultipartBody.Part
    private var imageRes = registerForActivityResult(ActivityResultContracts.GetContent()) { result->
        val contentResolver = this.contentResolver
        val contentType = contentResolver.getType(result!!)
        binding.ivProfile.setImageURI(result)

        val tempFile = File.createTempFile("image", "jpg",null)
        val inputStream = contentResolver.openInputStream(result!!)
        tempFile.outputStream().use {
            inputStream?.copyTo(it)
        }
        val reqBody : RequestBody = tempFile.asRequestBody(contentType!!.toMediaType())
        image = MultipartBody.Part.createFormData("image", tempFile.name, reqBody)
    }
    private val REQUEST_CODE_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(UserViewModel :: class.java)

        binding.btnEdit.setOnClickListener {
            uploadImage()
        }

        binding.btnRegister.setOnClickListener {
            signupUser()
        }
    }

    private fun signupUser() {
        val name = binding.etName.text.toString().toRequestBody("multipart/form-data".toMediaType())
        val email = binding.etEmail.text.toString().toRequestBody("multipart/form-data".toMediaType())
        val pass = binding.etPass.text.toString().toRequestBody("multipart/form-data".toMediaType())
        val phone = binding.etPhone.text.toString().toRequestBody("multipart/form-data".toMediaType())
        val address = binding.etAddress.text.toString().toRequestBody("multipart/form-data".toMediaType())
        val city = binding.etCity.text.toString().toRequestBody("multipart/form-data".toMediaType())

        viewModel.postAPIUser(name, email, pass, phone, address, city, image)
        viewModel.registeredUser.observe(this){
            if(it != null){
                Toast.makeText(this,"Registration Success", Toast.LENGTH_SHORT)
            }else{
                Toast.makeText(this,"Registration Failed", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun uploadImage() {
        appsPermissions()
    }

    private fun appsPermissions() {
        if (appsGranted(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_PERMISSION,)
        ){
            galleryAccess()
        }
    }

    private fun appsGranted(
        activity: Activity,
        permission: String,
        permissions: Array<String>,
        request: Int,
    ): Boolean {
        val permissionCheck = ActivityCompat.checkSelfPermission(activity, permission)
        return if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                appsDenied()
            } else {
                ActivityCompat.requestPermissions(activity, permissions, request)
            }
            false
        } else {
            true
        }
    }

    private fun appsDenied() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton(
                "App Settings"
            ) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun galleryAccess() {
        this.intent.type = "image/*"
        imageRes.launch("image/*")
    }

}