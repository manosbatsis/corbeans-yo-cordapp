package mypackage.server.components

import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeService
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.internal.extractFile
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.util.*
import java.util.jar.JarInputStream
import javax.annotation.PostConstruct
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import mypackage.yo.contract.YoState
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 *  Port of Corda samples Yo! API to a REST Controller.
 *  Works for both a single/default node and multiples nodes with
 *  `yo` and `yo/{nodeName}` base paths respectively.
 */
@RestController
@Api(tags = arrayOf("Yo!"),  description = "Send Yo!")
@RequestMapping(path = arrayOf("yo", "yo/{nodeName}"))
open class YoController {

    companion object {
        private val logger = LoggerFactory.getLogger(YoController::class.java)
        val NODE_NAME_DEFAULT = "default"
    }

    @Autowired
    protected lateinit var services: Map<String, YoService>

    @PostConstruct
    fun postConstruct() {
        logger.debug("Yo! services for Corda nodes:: {}", services.keys)
    }

    fun getService(optionalNodeName: Optional<String>): YoService {
        val nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else NODE_NAME_DEFAULT
        return this.services.get("${nodeName}NodeService") ?: throw IllegalArgumentException("Node not found: $nodeName")
    }

    @GetMapping("yo")
    @ApiOperation(value = "Send a Yo! the target party")
    fun yo(@PathVariable nodeName: Optional<String>,
           @ApiParam(value = "The target Party. You do not need to use the whole X500 name, using only the organisation works. In this case: PartyA, PartyB, Controller, etc.")
           @RequestParam(required = true) target: String): ResponseEntity<String> {
        val (status, message) = try {
            val result = getService(nodeName).sendYo(target)
            // Return the response.
            HttpStatus.CREATED to "You just sent a Yo! to ${target} (Transaction ID: ${result.tx.id})"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }
        return ResponseEntity.status(status).body(message)
    }

    @GetMapping("yos")
    @ApiOperation(value = "Get every Yo!")
    fun yos(@PathVariable nodeName: Optional<String>) =
            this.getService(nodeName).proxy().vaultQuery(YoState::class.java).states
}