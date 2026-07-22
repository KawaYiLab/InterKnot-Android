package dev.kawayilab.interknot.data.repository

import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.model.Delegation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterknotRepository @Inject constructor(
    private val api: InterknotApi
) {

    suspend fun loadHomeFeed(page: Int = 1): List<Delegation> = api.getDelegations(page)

    suspend fun loadDelegationDetail(id: Int): Delegation = api.getDelegation(id)
}
