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
package mypackage.server.components

import com.github.manosbatsis.corbeans.spring.boot.corda.config.NodeParams
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import mypackage.cordapp.YoContract
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.annotation.PostConstruct


/**
 *  Port of Corda samples' Yo! API to a REST Controller.
 *  Works for both a single/default node and multiples nodes with
 *  `yo` and `yo/{nodeName}` base paths respectively.
 */
@RestController
@Api(tags = arrayOf("Yo!"),  description = "Send Yo!")
@RequestMapping(path = arrayOf("api/yo", "api/yo/{nodeName}"))
class YoController {

    companion object {
        private val logger = LoggerFactory.getLogger(YoController::class.java)
    }

    protected lateinit var defaultNodeName: String

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    protected lateinit var services: Map<String, YoService>

    @PostConstruct
    fun postConstruct() {
        // if single node config, use the only node name as default, else reserve explicitly for cordform
        defaultNodeName = if (services.keys.size == 1) services.keys.first() else NodeParams.NODENAME_CORDFORM
        logger.debug("Auto-configured RESTful services for Corda nodes:: {}, default node: {}", services.keys, defaultNodeName)
    }

    /**
     * Handle both "api/yo" and "api/yo/{nodeName}" by using `cordform` as the default
     * node name to support optional dedicated webserver per node when using `runnodes`.
     */
    fun getService(optionalNodeName: Optional<String>): YoService {
        val nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName
        return this.services.get("${nodeName}NodeService") ?: throw IllegalArgumentException("Node not found: $nodeName")
    }

    @GetMapping("yo")
    @ApiOperation(value = "Send a Yo! to the target party")
    fun yo(@PathVariable nodeName: Optional<String>,
           @ApiParam(value = "The target Party. You do not need to use the whole X500 name, using only the organisation works. In this case: PartyA, PartyB, Controller, etc.")
           @RequestParam(required = true) target: String): ResponseEntity<*> {
        val (status, message) = try {
            val result = getService(nodeName).sendYo(target)
            // Return the response.
            HttpStatus.CREATED to mapOf<String, String>(
                    "message" to "You just sent a Yo! to ${target} (Transaction ID: ${result.tx.id})",
                    "sendingNode" to "$nodeName",
                    "target" to "$target",
                    "transactionId" to "${result.tx.id}"
            )
        } catch (e: Exception) {
            logger.error("Error sending Yo! to ${target}", e)
            e.printStackTrace()
            HttpStatus.BAD_REQUEST to e.message
        }
        return ResponseEntity.status(status).body(message)
    }

    @GetMapping("yos")
    @ApiOperation(value = "Get every Yo!")
    fun yos(@PathVariable nodeName: Optional<String>) =
            this.getService(nodeName).proxy().vaultQuery(YoContract.YoState::class.java).states

}