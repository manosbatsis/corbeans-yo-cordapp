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
package mypackage.server.innertests


import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import com.github.manosbatsis.vaultaire.plugin.accounts.dto.AccountInfoStateClientDto
import com.github.manosbatsis.vaultaire.plugin.accounts.dto.AccountInfoService
import com.github.manosbatsis.vaultaire.registry.Registry
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import mypackage.cordapp.workflow.YoStateClientDto
import mypackage.server.yo.ResultsPage
import net.corda.core.contracts.UniqueIdentifier
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Test swagger and actuator endpoints */
open class StateIntegrationTests(
        val restTemplate: TestRestTemplate,
        val networkService: CordaNetworkService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StateIntegrationTests::class.java)
    }

    inline fun <reified T> parameterizedTypeReference() = object : ParameterizedTypeReference<T>() {}

    @Test
    fun `Can use state service to query and track states`() {
        networkService.getService(AccountInfoService::class.java, "partya")
        val services = Registry.getServices()
        logger.info("Services (${services.size}): $services")
        services.forEach { (state, service) ->
            logger.info("Service state: ${state.javaClass.canonicalName}, class: ${service.canonicalName}")
        }
        logger.info("Network service class: ${networkService.javaClass.canonicalName}")
        // Init services
        val aNodeService = networkService.getNodeService("partya")
        logger.info("Node service class: ${aNodeService.javaClass.canonicalName}")
        //val aStateService: YoStateService = aNodeService.createStateService(YoState::class.java)
        val aAccountInfoService: AccountInfoService = aNodeService.createStateService(AccountInfo::class.java)

        val bNodeService = networkService.getNodeService("partyb")
        //val bStateService: YoStateService = bNodeService.createStateService(YoState::class.java)
        val bAccountInfoService: AccountInfoService = bNodeService.createStateService(AccountInfo::class.java)

        // Create accounts A and B
        val aAccountInfo = aAccountInfoService.createAccount("accountA").state.data
        val bAccountInfo = bAccountInfoService.createAccount("accountB").state.data

        // Send Yo from Account A to Account B
        val aCoountInfoDto = AccountInfoStateClientDto.from(aAccountInfo, aAccountInfoService)
        val bCoountInfoDto = AccountInfoStateClientDto.from(bAccountInfo, bAccountInfoService)
        val message = "AYo"
        var dto = YoStateClientDto(
            origin = aCoountInfoDto,
            target = bCoountInfoDto,
            message = message)
        logger.info("Sending DTO: ${dto}")
        val sentYoDto = this.restTemplate.postForObject(
            "/partya/api/yo", dto, YoStateClientDto::class.java)
        logger.info("Sent DTO: ${sentYoDto}")
        assertEquals(aCoountInfoDto, sentYoDto.origin)
        assertEquals(bCoountInfoDto, sentYoDto.target)
        assertEquals(message, sentYoDto.message)

        // Give some time to the async process
        Thread.sleep(3000);

        // Reply from Account B tp Account A
        logger.info("Reply from Account B tp Account A")
        val replyMessage = "BYo"
        dto = dto.copy(replyMessage = replyMessage)
        logger.info("Updating DTO: ${dto}")
        val updatedYoDto = this.restTemplate.exchange(
                "/partyb/api/yo/${sentYoDto.linearId.id}", HttpMethod.PUT,
                HttpEntity(dto),
                YoStateClientDto::class.java).body
        logger.info("Updated DTO: ${updatedYoDto}")

        // Give some time to the async process
        Thread.sleep(3000)
        assertEquals(sentYoDto!!.linearId, updatedYoDto!!.linearId)
        assertEquals(replyMessage, updatedYoDto.replyMessage)

        logger.info("validateQueryResults")
        // Query node vaults
        validateQueryResults("partya", updatedYoDto.linearId!!, replyMessage)
        validateQueryResults("partyb", updatedYoDto.linearId!!, replyMessage)

    }

    /** Ensure proper Vault storage */
    private fun validateQueryResults(
        nodeName: String,
        linearId: UniqueIdentifier,
        replyMessage: String) {
        logger.info("validateQueryResults nodeName: ${nodeName}, " +
                "linearId: $linearId, replyMessage: $replyMessage")
        // Ensure yo state can be retrieved from vault,
        // 1st by id.
        val yoDto = this.restTemplate.getForObject(
            "/$nodeName/api/yo/${linearId.id}",
            YoStateClientDto::class.java)
        assertNotNull(yoDto)

        logger.info("validateQueryResults yoDto: ${yoDto}")
        // 2nd by query
        val yoDtoPage = restTemplate.exchange(
            "/$nodeName/api/yo?replyMessage=${replyMessage}", HttpMethod.GET,
            HttpEntity.EMPTY,
            parameterizedTypeReference<ResultsPage<YoStateClientDto>>())
            .body

        logger.info("validateQueryResults yoDtoPage: ${yoDtoPage}")
        assertEquals(linearId, yoDtoPage?.content?.single()?.linearId)

    }


}
