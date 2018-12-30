package mypackage.yo.contract

import net.corda.core.identity.CordaX500Name
import net.corda.testing.contracts.DummyContract
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.DummyCommandData
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

val cordappPackages = listOf("mypackage.yo.contract", "net.corda.testing.contracts")

class YoContractTests {
    private val ledgerServices = MockServices(cordappPackages)
    private val alice = TestIdentity(CordaX500Name("Alice", "New York", "US"))
    private val bob = TestIdentity(CordaX500Name("Bob", "Tokyo", "JP"))
    private val miniCorp = TestIdentity(CordaX500Name("MiniCorp", "New York", "US"))

    @Test
    fun yoTransactionMustBeWellFormed() {
        // A pre-made Yo to Bob.
        val yo = YoState(alice.party, bob.party)
        // Tests.
        ledgerServices.ledger {
            // Input state present.
            transaction {
                input(DummyContract.PROGRAM_ID, DummyState())
                command(alice.publicKey, YoContract.Send())
                output(YO_CONTRACT_ID, yo)
                this.failsWith("There can be no inputs when Yo'ing other parties.")
            }
            // Wrong command.
            transaction {
                output(YO_CONTRACT_ID, yo)
                command(alice.publicKey, DummyCommandData)
                this.failsWith("")
            }
            // Command signed by wrong key.
            transaction {
                output(YO_CONTRACT_ID, yo)
                command(miniCorp.publicKey, YoContract.Send())
                this.failsWith("The Yo! must be signed by the sender.")
            }
            // Sending to yourself is not allowed.
            transaction {
                output(YO_CONTRACT_ID, YoState(alice.party, alice.party))
                command(alice.publicKey, YoContract.Send())
                this.failsWith("No sending Yo's to yourself!")
            }
            transaction {
                output(YO_CONTRACT_ID, yo)
                command(alice.publicKey, YoContract.Send())
                this.verifies()
            }
        }
    }
}