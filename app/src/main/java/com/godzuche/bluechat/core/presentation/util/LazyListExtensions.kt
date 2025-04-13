package com.godzuche.bluechat.core.presentation.util

import androidx.annotation.IntRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastSumBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Animate (smooth scroll) to the given item, only works for forward scrolling.
 *
 * @param index the index to which to scroll. Must be non-negative.
 * @param animationSpec [AnimationSpec] to be used for each scroll operation.
 */
suspend fun LazyListState.animateScrollToItem(
    @IntRange(from = 0)
    index: Int,
    animationSpec: AnimationSpec<Float> = spring(),
) = withContext(Dispatchers.Main) {
    // Index should be greater than the first visible item index and not more than the last item index in the list
    if (index < 0 || index <= firstVisibleItemIndex || index > layoutInfo.totalItemsCount - 1) {
        return@withContext
    }

    val total = index + 1
    val spacing = layoutInfo.mainAxisItemSpacing
    var current = firstVisibleItemIndex

    while (current < total) {
        // get all the visible items that don't surpass the total
        val visibleItems = layoutInfo.visibleItemsInfo.fastFilter { it.index < total }

        // calculate the total height of the visible items and the spacing between them
        val height = visibleItems.fastSumBy { it.size } + (visibleItems.size - 1) * spacing

        debugLog { "scrolling to $index, current: $current, height: $height" }

        // animate the scroll to the height of the visible items
        animateScrollBy(
            height.toFloat(),
            animationSpec = animationSpec
        )

        // get the current index
        current = when (firstVisibleItemIndex) {
            // if the current index didn't change, break the loop
            current -> break
            else -> firstVisibleItemIndex
        }
    }
}

/**
 * Animate (smooth scroll) to the given item, only works for forward scrolling.
 *
 * @param index the index to which to scroll. Must be non-negative.
 * @param animationSpec [AnimationSpec] to be used for each scroll operation.
 */
suspend fun LazyListState.animateScrollToItemV2(
    @IntRange(from = 0)
    index: Int,
    animationSpec: AnimationSpec<Float> = spring(),
) = withContext(Dispatchers.Main) {
    // Todo: Support reverse scroll
    require(index >= 0f) { "Index should be non-negative ($index)" }

    // Index should be greater than the first visible item index and not more than the last item index in the list
    if (index <= firstVisibleItemIndex || index > layoutInfo.totalItemsCount - 1) {
        return@withContext
    }

    var currentFirstVisibleIndex = firstVisibleItemIndex

    // Infinite loop till the end of the list
    while (currentFirstVisibleIndex < index) {
        // calculate the total distance of the target index
        val targetDistance = calculateDistanceTo(targetIndex = index)
        println("scrolling to $index, current: $currentFirstVisibleIndex, targetDistance: $targetDistance")

        // animate the scroll to the distance of the visible items
        animateScrollBy(
            targetDistance,
            animationSpec = animationSpec
        )

        // get the current index
        currentFirstVisibleIndex = when (firstVisibleItemIndex) {
            // if the current index didn't change, break the loop (when the list is at the end item already)
            currentFirstVisibleIndex -> break
            else -> firstVisibleItemIndex
        }
    }
}

fun LazyListState.isItemVisible(index: Int): Boolean {
    return index in firstVisibleItemIndex..lastVisibleItemIndex
}

val LazyListState.lastVisibleItemIndex: Int
    get() = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

fun LazyListState.calculateDistanceTo(targetIndex: Int): Float {
    val layoutInfo = layoutInfo
    if (layoutInfo.visibleItemsInfo.isEmpty()) return 0f
    val visibleItem = layoutInfo.visibleItemsInfo.fastFirstOrNull { it.index == targetIndex }
    return if (visibleItem == null) {
        val averageSize = calculateVisibleItemsAverageSize(layoutInfo)
        val indexesDiff = targetIndex - firstVisibleItemIndex
        (averageSize * indexesDiff).toFloat() - firstVisibleItemScrollOffset
    } else {
        visibleItem.offset.toFloat()
    }
}

private fun calculateVisibleItemsAverageSize(layoutInfo: LazyListLayoutInfo): Int {
    val visibleItems = layoutInfo.visibleItemsInfo
    val itemsSum = visibleItems.fastSumBy { it.size }
    return itemsSum / visibleItems.size + layoutInfo.mainAxisItemSpacing
}


//private class ItemFoundInScroll(
//    val itemOffset: Int,
//    val previousAnimation: AnimationState<Float, AnimationVector1D>,
//) : CancellationException()
//
//private val TargetDistance = 2500.dp
//private val BoundDistance = 1500.dp
//private val MinimumDistance = 50.dp

