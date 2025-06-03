package com.example.chatdocuemysi.model

data class GroupDetail(
    val groupId: String,
    val groupName: String,
    val photoUrl: String,
    val members: List<String>,
    val admins: List<String>
)