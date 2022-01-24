package edu.uw.saksham8.photogram

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.android.synthetic.main.fragment_gallery.*

class Gallery : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_gallery, container, false)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        observeViewModel()

        rootView.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val action = GalleryDirections.actionToUploadFrag()
            findNavController().navigate(action)
        }

        val query: Query = FirebaseDatabase.getInstance()
            .reference
            .child("images")

        val options: FirebaseRecyclerOptions<PhotoDetail> = FirebaseRecyclerOptions.Builder<PhotoDetail>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(query, PhotoDetail::class.java)
            .build()

        val adapter = FirebaseImageAdapter(options, this)
        val recycler = rootView.findViewById<RecyclerView>(R.id.gallery_images)

        val mLayoutManager = LinearLayoutManager(context)
        mLayoutManager.reverseLayout = true
        mLayoutManager.stackFromEnd = true

        recycler.layoutManager = mLayoutManager
        recycler.adapter = adapter

        if (pref.all["dark mode"] == true) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        return rootView
    }

    private fun observeViewModel() {
        viewModel.firebaseLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                fab.hide()
            } else {
                fab.show()
            }
        })
    }
}