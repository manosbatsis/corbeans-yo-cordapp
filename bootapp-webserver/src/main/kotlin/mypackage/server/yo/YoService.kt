/*
 * 	Corbeans Yo! Cordapp: Sample/Template project for Corbeans,
 * 	see https://manosbatsis.github.io/corbeans
 *
 * 	Copyright (C) 2018 Manos Batsis.
 * 	Parts are Copyright 2016, R3 Limited.
 *
 * 	This library is free software; you can redistribute it and/or
 * 	modify it under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance 	with the License.
 * 	You may obtain a copy of the License at
 *
 * 	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing,
 * 	software distributed under the License is distributed on an
 * 	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * 	KIND, either express or implied.  See the License for the
 * 	specific language governing permissions and limitations
 * 	under the License.
 */
package mypackage.server.yo

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import mypackage.cordapp.workflow.CreateYoFlow
import mypackage.cordapp.workflow.UpdateYoFlow
import mypackage.cordapp.workflow.YoStateLiteDto
import mypackage.cordapp.workflow.YoStateService
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.Sort
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class YoService {

    companion object {
        private val logger = LoggerFactory.getLogger(YoService::class.java)
    }

    @Autowired
    lateinit var networkService: CordaNetworkService

    /**
     * Send a Yo!
     * @param nodeName the current node name
     * @param input the Yo! to create
     */
    fun createAndSend(
            nodeName: String?,
            input: YoStateLiteDto
    ): YoStateLiteDto {
        val nodeService = networkService.getNodeService(nodeName)
        // Use an RPC connection pool
        return nodeService.withNodeRpcConnection {
            // Start the flow, block and wait for the response.
            it.proxy.startFlowDynamic(CreateYoFlow::class.java, input)
                .returnValue.getOrThrow()
        }
    }

    /**
     * Reply to a Yo!
     * @param nodeName the current node name
     * @param input the Yo! to update
     */
    fun updateAndReply(
            nodeName: String?,
            input: YoStateLiteDto
    ): YoStateLiteDto {
        val nodeService = networkService.getNodeService(nodeName)
        // Use an RPC connection pool
        return nodeService.withNodeRpcConnection {
            // Start the flow, block and wait for the response.
            it.proxy.startFlowDynamic(UpdateYoFlow::class.java, input)
                .returnValue.getOrThrow()
        }
    }

    /**
     * Find paged Yo!s
     * @param nodeName the current node name
     * @param criteria the query criteria
     */
    fun findPaged(
        nodeName: String?,
        criteria: QueryCriteria,
        sort: Sort,
        pageSpecification: PageSpecification
    ): ResultsPage<YoStateLiteDto> {
        val stateService: YoStateService = YoStateService(networkService.getNodeRpcPool(nodeName))
        val vaultPage = stateService.queryBy(criteria, pageSpecification, sort)

        // Map results and return
        return ResultsPage(
                content = vaultPage.states.map {
                    YoStateLiteDto.mapToDto(it.state.data, stateService)
                },
                pageNumber = pageSpecification.pageNumber,
                pageSize = pageSpecification.pageSize,
                totalResults = vaultPage.totalStatesAvailable,
                sort = "none",
                sortDirection = null)
    }

}
