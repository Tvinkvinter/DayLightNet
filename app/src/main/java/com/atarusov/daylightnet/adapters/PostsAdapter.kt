package com.atarusov.daylightnet.adapters

import android.R.attr.button
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.databinding.PostItemBinding
import com.atarusov.daylightnet.model.Post
import com.atarusov.daylightnet.model.User
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.util.Date
import java.util.Locale


class PostsAdapter(
    private val context: Context,
    private val onLikeButtonClick: (Post) -> Unit
) :
    RecyclerView.Adapter<PostsAdapter.PostsViewHolder>() {

    class PostsViewHolder(
        val binding: PostItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = Firebase.firestore

    var posts: List<Post> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value.sortedByDescending { it.timestamp }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostsAdapter.PostsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PostItemBinding.inflate(inflater, parent, false)
        return PostsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val post = posts[position]
        var postAuthor: User? = null
        val postAuthorRef = database.collection("users").document(post.userId)
        postAuthorRef.get().addOnSuccessListener { authorSnapshot ->
            postAuthor = authorSnapshot.toObject<User>()
            holder.binding.authorNameTv.text = context.getString(
                R.string.profile_username,
                postAuthor?.firstName, postAuthor?.lastName
            )
            Log.i(TAG, "Post author sucessfully loaded")

        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents: ", exception)
        }

        holder.binding.timestampTv.text = getFormattedDateString(post.timestamp, "dd/MM/yyyy HH:mm")
        holder.binding.contentTv.text = post.content
        holder.binding.likeBtn.text = post.likes.toString()
        setLikeButtonColor(holder.binding.likeBtn, isPostLiked(post))

        holder.binding.likeBtn.setOnClickListener {
            onLikeButtonClick(post)
            setLikeButtonColor(it as MaterialButton, isPostLiked(post))
        }
    }

    override fun getItemCount(): Int = posts.size

    fun isPostLiked(post: Post) = post.idsOfUsersLiked.contains(firebaseAuth.uid)

    fun setLikeButtonColor(button: MaterialButton, isPostLiked: Boolean) {
        val button_color: ColorStateList
        if (isPostLiked) {
            button_color = ColorStateList.valueOf(context.getColor(R.color.primary))
        } else {
            button_color = ColorStateList.valueOf(context.getColor(R.color.secondary))
        }

        (button).iconTint = button_color
        button.setTextColor(button_color)
    }

    fun getFormattedDateString(timestamp: Long, format: String): String {
        val sdf: SimpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    companion object {
        val TAG = "PostsAdapter"
    }
}