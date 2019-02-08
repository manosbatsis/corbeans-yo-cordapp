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
import com.github.manosbatsis.partiture.flow.PartitureResponderFlow
import com.github.manosbatsis.partiture.flow.call.CallContext
import com.github.manosbatsis.partiture.flow.call.CallContextEntry
import com.github.manosbatsis.partiture.flow.delegate.initiating.PartitureFlowDelegateBase
import com.github.manosbatsis.partiture.flow.io.input.InputConverter
import com.github.manosbatsis.partiture.flow.io.output.SingleFinalizedTxOutputConverter
import com.github.manosbatsis.partiture.flow.tx.TransactionBuilderWrapper
import com.github.manosbatsis.partiture.flow.tx.responder.SimpleTypeCheckingResponderTxStrategy
import mypackage.cordapp.contract.YO_CONTRACT_ID
import mypackage.cordapp.contract.YoContract
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction

/** Create a Yo! transaction/state for each input recipient/party */
@InitiatingFlow
@StartableByRPC
class YoFlow(
        input: Party
) : PartitureFlow<Party, SignedTransaction>(
        input = input, // Input can be anything
        inputConverter = YoInputConverter(),// Our custom IN converter
        outputConverter = SingleFinalizedTxOutputConverter()) // OUT build-in converter

class YoInputConverter : PartitureFlowDelegateBase(), InputConverter<Party> {
    override fun convert(input: Party): CallContext {
        // Prepare a TX builder
        val txBuilder = TransactionBuilderWrapper(clientFlow.getFirstNotary())
                .addOutputState(YoContract.YoState(clientFlow.ourIdentity, input), YO_CONTRACT_ID)
                .addCommand(YoContract.Send())
        // Return a TX context with builder and participants
        return CallContext(CallContextEntry(txBuilder))
    }
}

/**
 * A basic responder for countersigning and listening for finality
 */
@InitiatedBy(YoFlow::class)
class YoFlowResponder(
        otherPartySession: FlowSession
) : PartitureResponderFlow(
        otherPartySession = otherPartySession,
        responderTxStrategy = SimpleTypeCheckingResponderTxStrategy(
                YoContract.YoState::class.java)
)
