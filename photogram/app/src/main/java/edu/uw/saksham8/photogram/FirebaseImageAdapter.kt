package edu.uw.saksham8.photogram

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions


class FirebaseImageAdapter(private val options: FirebaseRecyclerOptions<PhotoDetail>, private val gallery: Gallery) :
    FirebaseRecyclerAdapter<PhotoDetail, FirebaseImageAdapter.ViewHolder>(options) {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var imageTitle: TextView = view.findViewById(R.id.image_title)
        var imageLikes: TextView = view.findViewById(R.id.image_likes)
        val likeButton: Button = view.findViewById(R.id.like_button)
        val image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(
            R.layout.gallery_item,
            parent,
            false)

        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: PhotoDetail) {
        holder.imageTitle.text = model.title
        Log.i("binder", holder.imageTitle.text as String)
        var count = 0
        for (like in model.likes) {
            if (like.value)
                count++
        }
        holder.imageLikes.text = count.toString()
        Glide.with(gallery).load(model.url).error(R.drawable.error).into(holder.image)

        if (model.likes.containsKey(MainActivity.uid) && model.likes[MainActivity.uid]!!) {
            holder.likeButton.text = "Unlike"
        } else {
            holder.likeButton.text = "Like"
        }

        holder.likeButton.setOnClickListener {
            val imageRef = getRef(position)
            var uid = MainActivity.uid
            if(model.likes.containsKey(MainActivity.uid) && model.likes[uid]!!) {
                if (uid != null) {
                    imageRef.child("likes").child(uid).setValue(false)
                }
            } else {
                if (uid != null) {
                    imageRef.child("likes").child(uid).setValue(true)
                }
            }
        }
    }
}