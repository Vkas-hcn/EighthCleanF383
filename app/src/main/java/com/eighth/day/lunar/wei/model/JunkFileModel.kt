package com.eighth.day.lunar.wei.model

import java.io.File


data class JunkFileModel(
    val file: File,
    val name: String = file.name,
    val size: Long = file.length(),
    var isSelected: Boolean = true
) {

    val path: String
        get() = file.absolutePath
    

    fun exists(): Boolean = file.exists()
    

    fun delete(): Boolean = try {
        file.delete()
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
    

    fun getFormattedSize(): Pair<Double, String> {
        return when {
            size < 1000 -> Pair(size.toDouble(), "B")
            size < 1000 * 1000 -> Pair(size / 1000.0, "KB")
            size < 1000 * 1000 * 1000 -> Pair(size / (1000.0 * 1000.0), "MB")
            else -> Pair(size / (1000.0 * 1000.0 * 1000.0), "GB")
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JunkFileModel) return false
        return path == other.path
    }
    
    override fun hashCode(): Int = path.hashCode()
}


data class JunkCategoryModel(
    val type: JunkCategoryType,
    val files: MutableList<JunkFileModel> = mutableListOf(),
    var isExpanded: Boolean = false,
    var isSelected: Boolean = false
) {

    val displayName: String
        get() = type.displayName
    

    val iconRes: Int
        get() = type.iconRes
    

    fun getTotalSize(): Long = files.sumOf { it.size }
    

    fun getSelectedSize(): Long = files.filter { it.isSelected }.sumOf { it.size }
    

    fun getSelectedCount(): Int = files.count { it.isSelected }
    

    fun getFileCount(): Int = files.size
    

    fun hasFiles(): Boolean = files.isNotEmpty()
    

    fun addFile(file: JunkFileModel): Boolean {
        if (files.contains(file)) {
            return false
        }
        return files.add(file)
    }
    

    fun deleteSelectedFiles(): Int {
        var deletedCount = 0
        val filesToRemove = mutableListOf<JunkFileModel>()
        
        files.filter { it.isSelected }.forEach { fileModel ->
            if (fileModel.delete()) {
                deletedCount++
                filesToRemove.add(fileModel)
            }
        }
        
        files.removeAll(filesToRemove)
        return deletedCount
    }
    

    fun toggleSelectAll() {
        isSelected = !isSelected
        files.forEach { it.isSelected = isSelected }
    }
    

    fun updateSelectionState() {
        isSelected = files.isNotEmpty() && files.all { it.isSelected }
    }
}

