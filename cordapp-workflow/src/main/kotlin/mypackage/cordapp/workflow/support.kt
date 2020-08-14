package mypackage.cordapp.workflow

import co.paralleluniverse.fibers.Suspendable
import com.github.manosbatsis.partiture.flow.PartitureResponderFlow
import com.github.manosbatsis.partiture.flow.call.CallContext
import com.github.manosbatsis.partiture.flow.call.CallContextEntry
import com.github.manosbatsis.partiture.flow.delegate.initiating.PartitureFlowDelegateBase
import com.github.manosbatsis.partiture.flow.io.input.InputConverter
import com.github.manosbatsis.partiture.flow.io.output.OutputConverter
import com.github.manosbatsis.partiture.flow.io.output.TypedOutputSingleStateConverter
import com.github.manosbatsis.partiture.flow.tx.TransactionBuilderWrapper
import com.github.manosbatsis.partiture.flow.tx.responder.SimpleTypeCheckingResponderTxStrategy
import com.github.manosbatsis.vaultaire.annotation.VaultaireDtoStrategyKeys
import com.github.manosbatsis.vaultaire.annotation.VaultaireGenerateDtoForDependency
import com.github.manosbatsis.vaultaire.annotation.VaultaireGenerateForDependency
import com.github.manotbatsis.kotlin.utils.api.DefaultValue
import mypackage.cordapp.contract.YO_CONTRACT_ID
import mypackage.cordapp.contract.YoContract.Commands
import mypackage.cordapp.contract.YoContract.Companion.unrecognisedCommand
import mypackage.cordapp.contract.YoContract.YoState
import mypackage.cordapp.contract.YoContract.YoState.YoSchemaV1.PersistentYoState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowSession

/** Used to trigger Vaultaire's Service, Query DSL and DTOs generation */
@VaultaireGenerateForDependency(
        contractStateType = YoState::class,
        persistentStateType = PersistentYoState::class)
@VaultaireGenerateDtoForDependency(
        contractStateType = YoState::class,
        persistentStateType = PersistentYoState::class,
        strategies = [VaultaireDtoStrategyKeys.DEFAULT, VaultaireDtoStrategyKeys.LITE])
class YoMixin(
        @DefaultValue("UniqueIdentifier()")
        val linearId: UniqueIdentifier
)

/** Input converter for Yo! workflows */
class YoInputConverter(
        val command: TypeOnlyCommandData
) : PartitureFlowDelegateBase(), InputConverter<YoStateLiteDto> {

        @Suspendable
        override fun convert(input: YoStateLiteDto): CallContext {

                // Obtain a Yo! state service
                val stateService = YoStateService(clientFlow.serviceHub)

                // Prepare a TX builder
                val txBuilder = TransactionBuilderWrapper(clientFlow.getFirstNotary())

                // Convert input DTO to state
                val state = when(command){
                        // Create the initial state if command is "Send"
                        is Commands.Send -> input.toTargetType(stateService)
                        // Patch the initial state if "Reply"
                        is Commands.Reply -> {
                                // Obtain the initial state, add it as input
                                val existingState = stateService
                                        .getByLinearId(input.linearId
                                                ?: error("A linearId is required"))
                                txBuilder.addInputState(existingState)
                                // Patch to create the updated state
                                input.toPatched(existingState.state.data, stateService)
                        }
                        else -> throw FlowException(unrecognisedCommand)
                }

                // Finalize the TX builder
                txBuilder.addOutputState(state, YO_CONTRACT_ID)
                        .addCommand(command)

                // Return a TX context with builder and participants
                return CallContext(CallContextEntry(txBuilder))
        }
}

/** Output converter for Yo! workflows */
open class YoOutputConverter :
        PartitureFlowDelegateBase(),
        OutputConverter<YoStateLiteDto> {

        private val innerConverter =
                TypedOutputSingleStateConverter(YoState::class.java)

        @Suspendable
        override  fun convert(input: CallContext): YoStateLiteDto {
                val outState = innerConverter.convert(input)
                val stateService = YoStateService(this.clientFlow.serviceHub)
                return YoStateLiteDto.mapToDto(outState, stateService)
        }

}

/**
 * Basic responder for countersigning and listening for finality
 */
open class YoFlowResponder(
        otherPartySession: FlowSession
) : PartitureResponderFlow(
        otherPartySession = otherPartySession,
        responderTxStrategy = SimpleTypeCheckingResponderTxStrategy(
                YoState::class.java)
)
