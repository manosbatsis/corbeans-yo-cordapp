/*
 *     Corbeans Yo! Cordapp: Sample/Template project for Corbeans.
 *     https://manosbatsis.github.io/corbeans/
 *
 *     Copyright (C) 2018 Manos Batsis.
 *     Parts are Copyright 2016, R3 Limited.
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance 	with the License.
 *     You may obtain a copy of the License at
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
package mypackage.yo.workflow

import co.paralleluniverse.fibers.Suspendable
import mypackage.yo.contract.YO_CONTRACT_ID
import mypackage.yo.contract.YoContract
import mypackage.yo.contract.YoState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 * Create a Yo! between sender (initiating) and target parties
 */
@InitiatingFlow
@StartableByRPC
class YoFlow(val target: Party) : FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker = tracker()

    companion object {
        object CREATING : ProgressTracker.Step("Creating a new Yo!")
        object SIGNING : ProgressTracker.Step("Verifying the Yo!")
        object VERIFYING : ProgressTracker.Step("Verifying the Yo!")
        object FINALISING : ProgressTracker.Step("Sending the Yo!") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(CREATING, SIGNING, VERIFYING, FINALISING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = CREATING

        val me = serviceHub.myInfo.legalIdentities.first()
        // Retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        // Create the transaction components.
        val state = YoState(me, target)
        val requiredSigners = listOf(ourIdentity.owningKey, target.owningKey)
        val command = Command(YoContract.Send(), requiredSigners)
        // Create a transaction builder and add the components.
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(state, YO_CONTRACT_ID)
                .addCommand(command)
        // Verify the transaction.
        progressTracker.currentStep = VERIFYING
        txBuilder.verify(serviceHub)

        // Sign the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Create a session with the other party.
        val otherPartySession = initiateFlow(target)

        // Obtain the counter party's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(
                signedTx, listOf(otherPartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx, otherPartySession))
        return fullySignedTx
    }
}

/**
 * A basic responder for countersigning and listening for finality
 */
@InitiatedBy(YoFlow::class)
class YoFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Create our custom SignTransactionFlow
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Yo transaction." using (output is YoState)
            }
        }
        // Sign if the check is successful
        subFlow(signTransactionFlow)
        // Receive an update when done
        subFlow(ReceiveFinalityFlow(otherPartySession))
    }
}
