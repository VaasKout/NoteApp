package com.example.noteexample.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


//TODO write unit test for this
class ItemHelperCallback {
    fun getHelper(
        startIndex: Int,
        firstListSize: Int = -1,
        swapActionFirstList: (Int, Int) -> Unit,
        swapActionSecondList: (Int, Int) -> Unit,
        clearViewAction: () -> Unit,
    ):
            ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.absoluteAdapterPosition
                val to = target.absoluteAdapterPosition

                if (from >= startIndex &&
                    to >= startIndex &&
                    from <= firstListSize &&
                    to <= firstListSize
                ) {
                    swapActionFirstList(from - startIndex, to - startIndex)
                    recyclerView.adapter?.notifyItemMoved(from, to)
                } else if (
                    from >= firstListSize + 1 &&
                    to >= firstListSize + 1
                ) {
                    swapActionSecondList(from - firstListSize, to - firstListSize)
                    recyclerView.adapter?.notifyItemMoved(
                        from + firstListSize,
                        to + firstListSize
                    )
                }
                return true
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                clearViewAction()
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
            }
        })
    }
}