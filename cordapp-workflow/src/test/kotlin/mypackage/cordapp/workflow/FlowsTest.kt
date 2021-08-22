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

import com.github.manosbatsis.corda.testacles.mocknetwork.NodeHandles
import com.github.manosbatsis.corda.testacles.mocknetwork.config.MockNetworkConfig
import com.github.manosbatsis.corda.testacles.mocknetwork.config.OrgNames
import com.github.manosbatsis.corda.testacles.mocknetwork.jupiter.MockNetworkExtension
import com.github.manosbatsis.corda.testacles.mocknetwork.jupiter.MockNetworkExtensionConfig
import com.github.manosbatsis.vaultaire.plugin.accounts.dto.AccountInfoStateClientDto
import com.github.manosbatsis.vaultaire.plugin.accounts.dto.AccountInfoService
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/** Sample test using [MockNetworkExtension] */
@ExtendWith(MockNetworkExtension::class)
class FlowsTest{

    companion object {
        private val logger = LoggerFactory.getLogger(FlowsTest::class.java)
        // Marks the field as a config for the extension
        @MockNetworkExtensionConfig
        @JvmStatic
        val mockNetworkConfig = MockNetworkConfig (
            // The nodes to build as an OrgNames instance
            names = OrgNames(listOf("PartyA", "PartyB")),
            // The current cordapp module package
            cordappProjectPackage = "mypackage.cordapp.contract",
            // Other cordapps
            cordappPackages = listOf(
                "mypackage.cordapp.workflow",
                "com.github.manosbatsis.partiture.flow",
                "com.github.manosbatsis.vaultaire.dto",
                "com.github.manosbatsis.vaultaire.plugin.accounts",
                "com.r3.corda.lib.accounts.contracts",
                "com.r3.corda.lib.accounts.workflows",
                "com.r3.corda.lib.ci.workflows")
        )
    }

    @Test
    fun flowWorksCorrectly(nodeHandles: NodeHandles) {

        val a: StartedMockNode = nodeHandles.getNode("partya")
        val b: StartedMockNode = nodeHandles.getNode("partyb")

        val aYoStateService = YoStateService(a.services)
        val bYoStateService = YoStateService(b.services)

        val aAccountInfoService = AccountInfoService(a.services)
        val bAccountInfoService = AccountInfoService(b.services)

        // Create accounts A and B
        val aAccountInfo = aAccountInfoService.createAccount("accountA").state.data
        val bAccountInfo = bAccountInfoService.createAccount("accountB").state.data

        // Send Yo from Account A to Account B
        val sentYoDto = a.startFlow(CreateYoFlow(YoStateClientDto(
                origin = AccountInfoStateClientDto.from(aAccountInfo, aAccountInfoService),
                target = AccountInfoStateClientDto.from(bAccountInfo, bAccountInfoService),
                message = "A sent Yo! to B")))
                .getOrThrow()
        nodeHandles.network.waitQuiescent()

        // Reply from Account B tp Account A
        val replyMessage = "B replied Yo! to A"
        b.startFlow(UpdateYoFlow(sentYoDto.copy(
                replyMessage = replyMessage)))
                .getOrThrow()
        nodeHandles.network.waitQuiescent()

        // Query node vaults
        validateQueryResults(aYoStateService, sentYoDto.linearId, replyMessage)
        validateQueryResults(bYoStateService, sentYoDto.linearId, replyMessage)

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
        assertTrue(aResults.states.isNotEmpty())
        // https://r3-cev.atlassian.net/browse/CORDA-2601
        //assertEquals(1, aResults.totalStatesAvailable.toInt())
        assertEquals(linearId, aResults.states.single().state.data.linearId)

    }
}
