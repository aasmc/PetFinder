package ru.aasmc.petfinder.common.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.aasmc.petfinder.common.databinding.RecyclerViewAnimalItemBinding
import ru.aasmc.petfinder.common.presentation.model.UIAnimal
import ru.aasmc.petfinder.common.utils.setImageWithCrossFade


class AnimalsAdapter : ListAdapter<UIAnimal, AnimalsAdapter.AnimalsViewHolder>(ITEM_COMPARATOR) {

    private var animalClickListener: AnimalClickListener? = null

    inner class AnimalsViewHolder(
        private val binding: RecyclerViewAnimalItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UIAnimal) {
            binding.name.text = item.name
            binding.photo.setImageWithCrossFade(item.photo)

            binding.root.setOnClickListener {
                animalClickListener?.onAnimalClicked(item.id)
            }
        }
    }

    fun setOnAnimalClickListener(animalClickListener: AnimalClickListener) {
        this.animalClickListener = animalClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalsViewHolder {
        val binding = RecyclerViewAnimalItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return AnimalsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimalsViewHolder, position: Int) {
        val item: UIAnimal = getItem(position)
        holder.bind(item)
    }
}

private val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<UIAnimal>() {
    override fun areItemsTheSame(oldItem: UIAnimal, newItem: UIAnimal): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UIAnimal, newItem: UIAnimal): Boolean {
        return oldItem == newItem
    }
}

fun interface AnimalClickListener {
    fun onAnimalClicked(animalId: Long)
}