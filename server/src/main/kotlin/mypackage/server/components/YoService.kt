/*
 * 	Corbeans Yo! Cordapp: Sample/Template project for Corbeans.
 * 	https://manosbatsis.github.io/corbeans/
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
package mypackage.server.components

import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection
import mypackage.yo.workflow.YoFlow
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow


class YoService(
        nodeRpcConnection: NodeRpcConnection
) : CordaNodeServiceImpl(nodeRpcConnection) {

    /** Send a Yo! */
    fun sendYo(target: String): SignedTransaction {
        val proxy = this.nodeRpcConnection.proxy
        // Look-up the 'target'.
        val matches = proxy.partiesFromName(target, exactMatch = true)
        // We only want one result!
        val to: Party = when {
            matches.isEmpty() -> throw IllegalArgumentException("Target string doesn't match any nodes on the network.")
            matches.size > 1 -> throw IllegalArgumentException("Target string matches multiple nodes on the network.")
            else -> matches.single()
        }
        // Start the flow, block and wait for the response.
        return proxy.startFlowDynamic(YoFlow::class.java, to).returnValue.getOrThrow()
    }

}
