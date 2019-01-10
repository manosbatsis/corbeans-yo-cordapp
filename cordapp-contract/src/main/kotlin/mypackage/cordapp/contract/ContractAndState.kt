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

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.LedgerTransaction
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

// Contract and state.
val YO_CONTRACT_PACKAGE = YoContract::class.java.`package`.name
val YO_CONTRACT_ID = YoContract::class.java.canonicalName

class YoContract: Contract {

    // Command.
    class Send : TypeOnlyCommandData()

    // Contract code.
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when Yo'ing other parties." using (tx.inputs.isEmpty())
        "There must be one output: The Yo!" using (tx.outputs.size == 1)
        val yo = tx.outputsOfType<YoState>().single()
        "No sending Yo's to yourself!" using (yo.target != yo.origin)
        "The Yo! must be signed by the sender." using (command.signers.contains(yo.origin.owningKey))
        //"The Yo! must be signed by the recipient." using (command.signers.contains(yo.target.owningKey))
    }

    // State.
    data class YoState(val origin: Party,
                       val target: Party,
                       val yo: String = "Yo!") : ContractState, QueryableState {
        override val participants get() = listOf(target)
        override fun toString() = "${origin.name}: $yo"
        override fun supportedSchemas() = listOf(YoSchemaV1)
        override fun generateMappedObject(schema: MappedSchema) = YoSchemaV1.PersistentYoState(
                origin.name.toString(), target.name.toString(), yo)

        object YoSchema

        object YoSchemaV1 : MappedSchema(YoSchema.javaClass, 1, listOf(PersistentYoState::class.java)) {
            @Entity
            @Table(name = "yos")
            class PersistentYoState(
                    @Column(name = "origin")
                    var origin: String = "",
                    @Column(name = "target")
                    var target: String = "",
                    @Column(name = "yo")
                    var yo: String = ""
            ) : PersistentState()
        }
    }
}


