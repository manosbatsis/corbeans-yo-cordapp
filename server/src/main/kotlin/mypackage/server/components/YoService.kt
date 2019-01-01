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
