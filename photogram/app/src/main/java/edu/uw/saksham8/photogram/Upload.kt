package edu.uw.saksham8.photogram

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter

class Upload : Fragment() {

    private lateinit var viewModel: LoginViewModel
    lateinit var storage: FirebaseStorage
    lateinit var photoUri: Uri

    companion object {
        const val PICK_PHOTO_CODE = 1046
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = FirebaseStorage.getInstance()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_upload, container, false)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        observeViewModel()

        // upload button click
        rootView.findViewById<Button>(R.id.upload_btn).setOnClickListener {
            val storageRef = storage.reference

            val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            val photoRef = storageRef.child("images/Upload $timestamp")
            var uploadTask = photoRef.putFile(photoUri)

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                Toast.makeText(context, "Unable to post image", Toast.LENGTH_LONG).show()
            }.addOnSuccessListener { taskSnapshot -> val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    photoRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        val title = rootView.findViewById<EditText>(R.id.img_title).text.toString()
                        val uid = MainActivity.uid.toString()
                        val likes = mutableMapOf<String, Boolean>()
                        likes[uid] = false

                        writeToDatabase(downloadUri, title, uid, likes)

                        Log.i(TAG, "download successful")
                    } else {
                        Log.e(TAG, "download failed")
                    }
                }
            }
            val action = UploadDirections.actionToGalleryFrag()
            findNavController().navigate(action)
        }

        rootView.findViewById<ImageView>(R.id.upload_img).setOnClickListener{
            onPickPhoto()
        }

        return rootView
    }

    private fun writeToDatabase(downloadUri: String, title: String, uid: String, likes: Map<String, Boolean>) {
        val uploadedImg = PhotoDetail(downloadUri, title, uid, likes)
        Firebase.database.reference.child("images").push().setValue(uploadedImg)
    }

    private fun observeViewModel() {
        viewModel.firebaseLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                val action = UploadDirections.actionToGalleryFrag()
                findNavController().navigate(action)
            }
        })
    }

    fun onPickPhoto() {
        val intent = Intent(ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (intent.resolveActivity(activity?.packageManager!!) != null) {
            startActivityForResult(intent, PICK_PHOTO_CODE)
        } else {
            Log.e(TAG, "Upload failed")
        }
    }

    fun loadFromUri(photoUri: Uri?): Bitmap? {
        var image: Bitmap? = null
        try {
            image = if (Build.VERSION.SDK_INT > 27) {
                val source: ImageDecoder.Source = ImageDecoder.createSource(context?.contentResolver!!, photoUri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context?.contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && requestCode == PICK_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
            photoUri = (data.data)!!

            // Load the image located at photoUri into selectedImage
            val selectedImage = loadFromUri(photoUri)

            activity?.findViewById<ImageView>(R.id.upload_img)!!.setImageBitmap(selectedImage)
        }
    }
}