package com.yangdai.opennote.domain.operations

sealed class OrderType {
    data object Ascending: OrderType()
    data object Descending: OrderType()
}