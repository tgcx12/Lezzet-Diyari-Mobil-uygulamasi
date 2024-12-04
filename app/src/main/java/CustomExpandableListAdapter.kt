package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.myapplication.model.Category
import com.example.myapplication.model.Recipe

class CustomExpandableListAdapter(
    context: Context,
    private val categories: List<Category>
) : BaseExpandableListAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // Grup sayısını döndüren metot
    override fun getGroupCount(): Int {
        return categories.size
    }

    // Çocuk öğe sayısını döndüren metot
    override fun getChildrenCount(groupPosition: Int): Int {
        return categories[groupPosition].recipes.size
    }

    // Grupların (kategori başlıklarının) verisini döndüren metot
    override fun getGroup(groupPosition: Int): Any {
        return categories[groupPosition]
    }

    // Çocuk öğelerin (tariflerin) verisini döndüren metot
    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return categories[groupPosition].recipes[childPosition]
    }

    // Grup ID'sini döndüren metot
    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    // Çocuk öğe ID'sini döndüren metot
    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    // Sabit ID'ler kullanılıyor mu kontrolü
    override fun hasStableIds(): Boolean {
        return false
    }

    // Grup öğesinin görünümünü döndüren metot
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var groupView = convertView
        if (groupView == null) {
            groupView = inflater.inflate(R.layout.list_group, parent, false)
        }
        val categoryNameTextView: TextView = groupView!!.findViewById(R.id.tvGroup)
        categoryNameTextView.text = categories[groupPosition].name // Kategorinin adı
        return groupView
    }

    // Çocuk öğelerinin (tariflerin) görünümünü döndüren metot
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var childView = convertView
        if (childView == null) {
            childView = inflater.inflate(R.layout.list_item, parent, false)
        }
        val recipeNameTextView: TextView = childView!!.findViewById(R.id.tvItem)
        recipeNameTextView.text = categories[groupPosition].recipes[childPosition].title // Tarifin adı
        return childView
    }

    // Çocuk öğesinin seçilebilir olup olmadığını kontrol eden metot
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
