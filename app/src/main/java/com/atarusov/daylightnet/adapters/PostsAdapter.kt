package com.atarusov.daylightnet.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.databinding.PostItemBinding
import com.atarusov.daylightnet.model.Post
import com.atarusov.daylightnet.model.PostCard
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.util.Date
import java.util.Locale


class PostsAdapter(
    private val context: Context,
    private val onLikeButtonClick: (PostCard) -> Unit,
) : RecyclerView.Adapter<PostsAdapter.PostsViewHolder>() {

    class PostsViewHolder(
        val binding: PostItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    var postCards: List<PostCard> = emptyList()
        set(value) {
            val diffUtil = PostCardsDiffUtil(field, value)
            val diffResults = DiffUtil.calculateDiff(diffUtil)
            field = value
            diffResults.dispatchUpdatesTo(this)
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
        val postCard = postCards[position]
        holder.binding.authorNameTv.text = context.getString(
            R.string.profile_username, postCard.author.firstName, postCard.author.lastName
        )

        holder.binding.timestampTv.text =
            getFormattedDateString(postCard.post.timestamp, "dd/MM/yyyy HH:mm:ss")
        holder.binding.contentTv.text = postCard.post.content
        holder.binding.likeBtn.text = postCard.post.likes.toString()
        setLikeButtonColor(holder.binding.likeBtn, postCard.isLikedByCurrentUser)

        holder.binding.likeBtn.setOnClickListener {
            onLikeButtonClick(postCard)
        }
    }

    override fun getItemCount(): Int = postCards.size

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

    class PostCardsDiffUtil(
        private val oldList: List<PostCard>, private val newList: List<PostCard>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].post.id == newList[newItemPosition].post.id &&
                    oldList[oldItemPosition].author.uid == newList[newItemPosition].author.uid
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return when {
                oldList[oldItemPosition].post.content != newList[newItemPosition].post.content -> false
                oldList[oldItemPosition].post.timestamp != newList[newItemPosition].post.timestamp -> false
                oldList[oldItemPosition].post.idsOfUsersLiked != newList[newItemPosition].post.idsOfUsersLiked -> false

                oldList[oldItemPosition].author.firstName != newList[newItemPosition].author.firstName -> false
                oldList[oldItemPosition].author.lastName != newList[newItemPosition].author.lastName -> false
                oldList[oldItemPosition].author.avatar != newList[newItemPosition].author.avatar -> false

                else -> true
            }
        }
    }

    companion object {
        val TAG = "PostsAdapter"
    }
}