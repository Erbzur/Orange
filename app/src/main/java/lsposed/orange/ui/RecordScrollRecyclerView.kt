package lsposed.orange.ui

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecordScrollRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private var lastTopPositon = 0
    private var lastTopOffset = 0

    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (layoutManager is LinearLayoutManager) {
                    val linearLayoutManager = layoutManager as LinearLayoutManager
                    linearLayoutManager.getChildAt(0)?.let {
                        lastTopPositon = linearLayoutManager.getPosition(it)
                        lastTopOffset = it.top
                    }
                }
            }
        })
    }

    fun scrollToLastPosition() {
        if (layoutManager is LinearLayoutManager) {
            val linearLayoutManager = layoutManager as LinearLayoutManager
            lastTopPositon.takeIf { it >= 0 }?.let {
                linearLayoutManager.scrollToPositionWithOffset(it, lastTopOffset)
            }
        }
    }
}