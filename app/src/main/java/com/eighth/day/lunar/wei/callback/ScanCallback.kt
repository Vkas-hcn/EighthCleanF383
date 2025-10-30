package com.eighth.day.lunar.wei.callback

import com.eighth.day.lunar.wei.model.JunkFileModel


interface ScanCallback {
    

    fun onScanStarted(totalCategories: Int)
    

    fun onScanProgress(currentPath: String, categoryName: String)
    

    fun onFileFound(file: JunkFileModel, categoryName: String)
    

    fun onSizeChanged(totalSize: Long)
    

    fun onScanCompleted(totalFiles: Int, totalSize: Long)
    

    fun onScanError(error: Throwable)
}


interface SelectionCallback {
    

    fun onSelectionChanged(selectedCount: Int, totalCount: Int, selectedSize: Long)
    

    fun onCategorySelectionChanged(categoryName: String, isSelected: Boolean)
}




interface UIInteractionCallback {
    

    fun onCategoryExpandChanged(categoryName: String, isExpanded: Boolean)
    

    fun onCategoryClicked(categoryName: String)
    

    fun onFileClicked(file: JunkFileModel)
}

