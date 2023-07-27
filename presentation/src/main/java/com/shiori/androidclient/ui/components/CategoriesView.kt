package com.shiori.androidclient.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shiori.model.Tag

enum class CategoriesType {
    SELECTABLES, REMOVEABLES
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun Categories(
    categoriesType: CategoriesType = CategoriesType.SELECTABLES,
    showCategories: Boolean,
    uniqueCategories: MutableState<List<Tag>>,
    selectedTags: MutableState<List<Tag>> = mutableStateOf(emptyList<Tag>())
) {
    AnimatedVisibility(showCategories) {
        Column() {
            FlowRow() {
                uniqueCategories.value.forEach { category ->
                    val selected = category in selectedTags.value
                    FilterChip(
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = Color.Black,
                            iconColor = Color.Gray,
                            disabledContainerColor = Color.LightGray,
                            disabledLabelColor = Color.Gray,
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            disabledSelectedContainerColor = Color.LightGray,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                        ),
                        selected = selected,
                        label = { Text(category.name) },
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            when (categoriesType) {
                                CategoriesType.SELECTABLES -> {
                                    if (selected) {
                                        selectedTags.value = selectedTags.value - category
                                    } else {
                                        selectedTags.value = selectedTags.value + category
                                    }
                                }
                                CategoriesType.REMOVEABLES -> {
                                    uniqueCategories.value = uniqueCategories.value.filter { it != category }
                                }
                            }
                        },
                        leadingIcon = {
                            when(categoriesType){
                                CategoriesType.SELECTABLES -> {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                }
                                CategoriesType.REMOVEABLES -> {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            }
//                            if (selected) {
//                                Icon(
//                                    imageVector = Icons.Filled.Done,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
//                                )
//                            } else {
//                                null
//                            }
                        }
                    )
                }
            }
        }
    }
}