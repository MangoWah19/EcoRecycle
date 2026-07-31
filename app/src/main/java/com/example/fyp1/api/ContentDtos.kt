package com.example.fyp1.api

import com.google.gson.annotations.SerializedName

data class ContentListData(
    val content: List<BackendContent>
)

data class ContentData(
    val content: BackendContent
)

data class BackendContent(
    val id: String,
    val slug: String,
    val title: String,
    val body: String,
    val summary: String? = null,
    @SerializedName(value = "imageUrl", alternate = ["image_url"])
    val imageUrl: String? = null,
    val estimatedReadMinutes: Int? = null,
    val contentBlocks: List<BackendContentBlock>? = null,
    val tags: List<String> = emptyList(),
    val status: String,
    val version: Int,
    val createdById: String,
    val createdAt: String,
    val updatedAt: String
)

data class BackendContentBlock(
    val type: String,
    val text: String? = null,
    val url: String? = null,
    val alt: String? = null
)
