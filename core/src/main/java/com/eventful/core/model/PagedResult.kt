package com.eventful.core.model

class PagedResult<T>(val items: List<T>, val currentPage: Int, val totalPages: Int)
