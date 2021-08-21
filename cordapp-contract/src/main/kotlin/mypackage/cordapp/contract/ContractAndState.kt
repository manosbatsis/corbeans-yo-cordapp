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
package mypackage.cordapp.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

// Contract and state.
val YO_CONTRACT_PACKAGE: String = YoContract::class.java.`package`.name
val YO_CONTRACT_ID: String = YoContract::class.java.canonicalName

class YoContract : Contract {

    companion object{
        const val mustBeSignedBySender = "The Yo! must be signed by the sender."
        const val mustBeSignedByRecepient = "The Yo! must be signed by the recipient."
        const val noSendingToSelf = "No sending Yo's to yourself!"
        const val thereMustBeOneOutput = "There must be one output: The Yo!"
        const val thereCanBeNoInputsWhenSending = "There can be no inputs when Yo'ing other parties."
        const val unrecognisedCommand = "Unrecognised command."
    }

    /** Contract commands */
    interface Commands : CommandData {
        /** Create and send the initial Yo! message */
        class Send : TypeOnlyCommandData(), Commands

        /** Update and reply to the initial Yo! message */
        class Reply : TypeOnlyCommandData(), Commands
    }

    /** Verify transactions */
    override fun verify(tx: LedgerTransaction) {
        // Ensure only one of this contract's commands is present
        val command = tx.commands.requireSingleCommand<Commands>()
        // Get the signing participants
        val signers = command.signers.toSet()
        // Perform common checks
        thereMustBeOneOutput using (tx.outputs.size == 1)
        val yo = tx.outputsOfType<YoState>().single()
        noSendingToSelf using (yo.target != yo.origin)
        mustBeSignedBySender using (command.signers.contains(yo.origin.party.owningKey))
        mustBeSignedByRecepient using (command.signers.contains(yo.target.party.owningKey))
        // Forward to command-specific verification
        when (command.value) {
            is Commands.Send -> verifySend(tx, signers)
            is Commands.Reply -> verifyReply(tx, signers)
            else -> throw IllegalArgumentException(unrecognisedCommand)
        }
    }

    /** Verify send */
    private fun verifySend(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val command = tx.commands.requireSingleCommand<Commands.Send>()
        // Perform checks for command
        thereCanBeNoInputsWhenSending using (tx.inputs.isEmpty())
    }

    /** Verify reply */
    private fun verifyReply(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val command = tx.commands.requireSingleCommand<Commands.Reply>()
    }

}


