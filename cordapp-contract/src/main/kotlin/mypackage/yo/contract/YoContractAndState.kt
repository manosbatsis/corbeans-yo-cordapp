package mypackage.yo.contract

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.utilities.ProgressTracker
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.transactions.SignedTransaction

// Contract and state.
const val YO_CONTRACT_ID = "mypackage.yo.contract.YoContract"

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
}

// State.
@BelongsToContract(YoContract::class)
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
