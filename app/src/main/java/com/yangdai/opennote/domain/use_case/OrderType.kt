package com.yangdai.opennote.domain.use_case

sealed class OrderType {
    data object Ascending: OrderType()
    data object Descending: OrderType()
}