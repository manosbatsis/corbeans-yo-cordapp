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

import com.github.manosbatsis.partiture.flow.PartitureFlow
import mypackage.cordapp.contract.YO_CONTRACT_PACKAGE
import mypackage.cordapp.contract.YoContract
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp.Companion.findCordapp
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals


@Suppress("DEPRECATION")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allow non-static @BeforeAll etc.
class YoFlowTests {

    // Works as long as the main and test package names are  in sync
    val cordappPackages = listOf(
            YO_CONTRACT_PACKAGE,
            this.javaClass.`package`.name)
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode

    @BeforeAll
    fun setup() {

        network = MockNetwork(MockNetworkParameters(
                cordappsForAllNodes = cordappPackages.map {
                    findCordapp(it)
                }))

        a = network.createPartyNode()
        b = network.createPartyNode()
        c = network.createPartyNode()
        network.runNetwork()
    }

    @AfterAll
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun flowsWorkCorrectly() {
        val aId = a.info.legalIdentities.first()
        val bId = b.info.legalIdentities.first()
        val cId = c.info.legalIdentities.first()

        print("aId == $aId\n")
        print("bId == $bId\n")
        print("cId == $cId\n")
        val yo1 = YoContract.YoState(aId, bId)
        val yo2 = YoContract.YoState(aId, cId)
        val recepients = listOf(bId, cId)
        // test flow
        val flow = YoFlow(recepients)
        checkFlow(flow)
        // Check yo states are stored in the vault.
        checkVaultStorage(b, yo1)
        checkVaultStorage(c, yo2)
    }

    private fun checkFlow(flow: PartitureFlow<*, List<SignedTransaction>>) {
        val future = a.startFlow(flow)
        network.runNetwork()
        val stx = future.getOrThrow()
        // Check yo transactions are stored in the storage service of the appropriate receipient node
        val bTx = b.services.validatedTransactions.getTransaction(stx.first().id)
        val cTx = c.services.validatedTransactions.getTransaction(stx.last().id)
        assertEquals(bTx, stx.first())
        assertEquals(cTx, stx.last())
        print("bTx == $stx\n")
    }

    private fun checkVaultStorage(node: StartedMockNode, yo: YoContract.YoState) {
        node.transaction {
            // Simple query.
            val bYo = b.services.vaultService.queryBy<YoContract.YoState>().states.single().state.data

            print("bYo == $bYo\n")
            assertEquals(yo.toString(), bYo.toString())
            // Using a custom criteria directly referencing schema entity attribute.
            val expression = builder { YoContract.YoState.YoSchemaV1.PersistentYoState::yo.equal("Yo!") }
            val customQuery = VaultCustomQueryCriteria(expression)
            val bYo2 = b.services.vaultService.queryBy<YoContract.YoState>(customQuery).states.single().state.data
            assertEquals(bYo2.yo, yo.yo)
            print("$bYo2 == $yo\n")
        }
    }
}
