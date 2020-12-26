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
package mypackage.server.yo

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import mypackage.cordapp.workflow.YoStateLiteDto
import mypackage.cordapp.workflow.yoStateQuery
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.PATCH
import org.springframework.web.bind.annotation.RequestMethod.PUT
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Optional
import java.util.UUID


/**
 *  Port of Corda samples' Yo! API to a REST Controller.
 *  Works for both a single/default node and multiples nodes with
 *  `yo` and `yo/{nodeName}` base paths respectively.
 */
@RestController
@Tag(name = "Yo Services", description = "Yo operation endpoints")
@RequestMapping(path = arrayOf("api/yo", "{nodeName}/api/yo"))
class YoController {

    companion object {
        private val logger = LoggerFactory.getLogger(YoController::class.java)
        /** when using `runnodes` */
        private var defaultNodeName: String = "cordform"
    }


    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    lateinit var yoService: YoService


    /**
     * Handle both "api/yo" and "{nodeName}/api/yo" by using `cordform` as the default
     * node name to support optional dedicated webserver per node when using `runnodes`.
     */
    private fun normalizeNodeName(optionalNodeName: Optional<String>): String =
        if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName

    @PostMapping
    @Operation(description = "Send a Yo!", summary = "Create and send a Yo! to another account")
    fun create(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: YoStateLiteDto
    ): YoStateLiteDto = yoService.createAndSend(normalizeNodeName(nodeName), input)

    @RequestMapping(path = ["{id}"], method = [PUT, PATCH])
    @Operation(description = "Reply to a Yo!", summary = "Update and reply to a Yo! from another account")
    fun update(
            @PathVariable nodeName: Optional<String>,
            @PathVariable id: UUID,
            @RequestBody input: YoStateLiteDto
    ): YoStateLiteDto = yoService.updateAndReply(
        normalizeNodeName(nodeName),
        input.copy(linearId = UniqueIdentifier(id = id)))

    @GetMapping
    @Operation(summary = "Get a page of known Sample Requests")
    fun findPaged(
        @PathVariable
        nodeName: Optional<String>,

        @Parameter(description = "The page number, optional", required = false, example = "1")
        @RequestParam("pn", required = false, defaultValue = "1") pn: Int,

        @Parameter(description = "The page size, optional with default being 10", required = false, example = "10")
        @RequestParam("ps", required = false, defaultValue = "10") ps: Int,

        @Parameter(description = "The Yo sender account identifier, i.e. a UUID", required = false)
        @RequestParam("origin", required = false) origin: UUID? = null,

        @Parameter(description = "The Yo recipient account identifier, i.e. a UUID", required = false)
        @RequestParam("target", required = false) target: UUID? = null,

        @Parameter(description = "The Yo message", required = false)
        @RequestParam("message", required = false) message: String? = null,

        @Parameter(description = "The Yo reply message", required = false)
        @RequestParam("replyMessage", required = false) replyMessage: String? = null
    ): ResultsPage<YoStateLiteDto> {

        val query = yoStateQuery {
            status = Vault.StateStatus.UNCONSUMED
            and {
                if(origin != null) fields.origin `==` origin
                if(target != null) fields.target `==` target
                if(message != null) fields.message `==` message
                if(replyMessage != null) fields.replyMessage `==` replyMessage
            }
            orderBy {
                recordedTime sort DESC
            }
        }
        return yoService.findPaged(nodeName.get(),
                query.toCriteria(), PageSpecification(pn, ps))


    }
}