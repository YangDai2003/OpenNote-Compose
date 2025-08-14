package com.yangdai.opennote.domain.usecase

sealed class OrderType {
    data object Ascending : OrderType()
    data object Descending : OrderType()
}