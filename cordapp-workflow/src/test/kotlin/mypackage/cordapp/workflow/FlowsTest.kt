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
package mypackage.cordapp.workflow

import com.github.manosbatsis.corbeans.corda.common.test.CorbeansMockNetworkFlowTest
import com.github.manosbatsis.corbeans.corda.common.test.CorbeansMockNodeParametersConfig
import com.github.manosbatsis.vaultaire.plugin.accounts.dto.AccountInfoLiteDto
import com.github.manosbatsis.vaultaire.plugin.accounts.dto.AccountInfoService
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@Suppress("DEPRECATION")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allow non-static @BeforeAll etc.
class FlowsTest : CorbeansMockNetworkFlowTest(
        CorbeansMockNodeParametersConfig(requireApplicationProperties = true)
) {

    companion object {
        private val logger = LoggerFactory.getLogger(FlowsTest::class.java)
    }

    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var aYoStateService: YoStateService
    lateinit var bYoStateService: YoStateService
    lateinit var aAccountInfoService: AccountInfoService
    lateinit var bAccountInfoService: AccountInfoService

    init {
        val config = CorbeansMockNodeParametersConfig()
        logger.info("FlowsTest, config: ${config}")
        println("FlowsTest, config: ${config}")
    }

    override fun postSetup() {
        a = nodeMap["partyA"] ?: error("Cannot find node partyA, nodes(${nodeMap.keys.size}): ${nodeMap.keys.joinToString(",")}")
        aYoStateService = YoStateService(a.services)
        aAccountInfoService = AccountInfoService(a.services)

        b = nodeMap["partyB"] ?: error("Cannot find node partyB, nodes(${nodeMap.keys.size}): ${nodeMap.keys.joinToString(",")}")
        bYoStateService = YoStateService(b.services)
        bAccountInfoService = AccountInfoService(b.services)
    }

    @Test
    fun flowWorksCorrectly() {

        // Create accounts A and B
        val aAccountInfo = aAccountInfoService.createAccount("accountA").state.data
        val bAccountInfo = bAccountInfoService.createAccount("accountB").state.data

        // Send Yo from Account A to Account B
        val sentYoDto = a.startFlow(CreateYoFlow(YoStateLiteDto(
                AccountInfoLiteDto.mapToDto(aAccountInfo, aAccountInfoService),
                AccountInfoLiteDto.mapToDto(bAccountInfo, bAccountInfoService),
                "A sent Yo! to B")))
                .getOrThrow()
        network.waitQuiescent()

        // Reply from Account B tp Account A
        val replyMessage = "B replied Yo! to A"
        b.startFlow(UpdateYoFlow(sentYoDto.copy(
                replyMessage = replyMessage)))
                .getOrThrow()
        network.waitQuiescent()

        // Query node vaults
        validateQueryResults(aYoStateService, sentYoDto.linearId!!, replyMessage)
        validateQueryResults(bYoStateService, sentYoDto.linearId!!, replyMessage)

    }

    /** Ensure proper Vault storage */
    fun validateQueryResults(
            stateService: YoStateService,
            linearId: UniqueIdentifier,
            replyMessage: String){

        // Ensure yo state can be retrieved from vaults,
        // 1st by id.
        stateService.getByLinearId(linearId)

        // 2nd by query
        val criteria = yoStateQuery {
            and {
                fields.replyMessage `==` replyMessage
            }
        }.toCriteria()

        val aResults = stateService.queryBy(criteria)
        assertTrue(aResults.totalStatesAvailable.toInt() == 1)
        assertEquals(linearId, aResults.states.single().state.data.linearId)

    }
}
