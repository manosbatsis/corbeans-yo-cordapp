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
import com.github.manosbatsis.vaultaire.plugin.accounts.dto.AccountInfoLiteDto
import com.github.manosbatsis.vaultaire.plugin.accounts.dto.AccountInfoService
import com.github.manosbatsis.vaultaire.registry.Registry
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import mypackage.cordapp.workflow.YoStateLiteDto
import mypackage.server.yo.ResultsPage
import net.corda.core.contracts.UniqueIdentifier
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.RequestEntity
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        networkService.getService(AccountInfoService::class.java, "partyA")
        val services = Registry.getServices()
        logger.info("Services (${services.size}): $services")
        services.forEach { (state, service) ->
            logger.info("Service state: ${state.javaClass.canonicalName}, class: ${service.canonicalName}")
        }

        logger.info("Network service class: ${networkService.javaClass.canonicalName}")
        // Init services
        val aNodeService = networkService.getNodeService("partyA")
        logger.info("Node service class: ${aNodeService.javaClass.canonicalName}")
        //val aStateService: YoStateService = aNodeService.createStateService(YoState::class.java)
        val aAccountInfoService: AccountInfoService = aNodeService.createStateService(AccountInfo::class.java)

        val bNodeService = networkService.getNodeService("partyB")
        //val bStateService: YoStateService = bNodeService.createStateService(YoState::class.java)
        val bAccountInfoService: AccountInfoService = bNodeService.createStateService(AccountInfo::class.java)

        // Create accounts A and B
        val aAccountInfo = aAccountInfoService.createAccount("accountA").state.data
        val bAccountInfo = bAccountInfoService.createAccount("accountB").state.data

        // Send Yo from Account A to Account B
        val aCoountInfoDto = AccountInfoLiteDto.mapToDto(aAccountInfo, aAccountInfoService)
        val bCoountInfoDto = AccountInfoLiteDto.mapToDto(bAccountInfo, bAccountInfoService)
        val message = "A sent Yo! to B"
        val dto = YoStateLiteDto(
            origin = aCoountInfoDto,
            target = bCoountInfoDto,
            message = message)
        logger.info("Sending DTO: ${dto}")
        val sentYoDto = this.restTemplate.postForObject(
            "/partyA/api/yo", dto, YoStateLiteDto::class.java)
        logger.info("Sent DTO: ${sentYoDto}")
        assertEquals(aCoountInfoDto, sentYoDto.origin)
        assertEquals(bCoountInfoDto, sentYoDto.target)
        assertEquals(message, sentYoDto.message)
/*
        // Give some time to the async tracking process
        Thread.sleep(3000);

        // Reply from Account B tp Account A
        val replyMessage = "B replied Yo! to A"
        this.restTemplate.exchange(
                "/partyB/api/yo${sentYoDto.linearId!!.id}", PUT,
                HttpEntity(sentYoDto.copy(replyMessage = replyMessage)),
                YoStateLiteDto::class.java)

        // Query node vaults
        validateQueryResults("partyA", sentYoDto.linearId!!, replyMessage)
        validateQueryResults("partyB", sentYoDto.linearId!!, replyMessage)
*/
    }

    /** Ensure proper Vault storage */
    fun validateQueryResults(
        nodeName: String,
        linearId: UniqueIdentifier,
        replyMessage: String) {

        // Ensure yo state can be retrieved from vault,
        // 1st by id.
        val yoDto = this.restTemplate.getForObject(
            "/partyA/api/yo/${linearId.id}",
            YoStateLiteDto::class.java)
        assertNotNull(yoDto)

        // 2nd by query
        val yoDtoPage: ResultsPage<YoStateLiteDto>? = this.restTemplate.exchange(
            RequestEntity<Any>(GET, URI.create("/partyA/api/yo?replyMessage=${replyMessage}")),
            parameterizedTypeReference<ResultsPage<YoStateLiteDto>>()
        ).body

        assertTrue(yoDtoPage!!.totalResults.toInt() == 1)
        assertEquals(linearId, yoDtoPage!!.content.single().linearId)

    }


}
