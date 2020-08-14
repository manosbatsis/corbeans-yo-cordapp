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
package mypackage.server.testcontrollers

import io.swagger.v3.oas.annotations.Hidden
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import org.apache.logging.log4j.LogManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Used for conversion/formatter testing
 */
@RestController
@Hidden
@RequestMapping("api/echo")
class EchoController {

    class EchoModel() {
        var secureHash: SecureHash? = null
        var cordaX500Name: CordaX500Name? = null
    }

    companion object {
        private val logger = LogManager.getLogger(EchoController::class.java)
    }

    @GetMapping("echoSecureHash/{value}")
    fun echoSecureHash(@PathVariable value: SecureHash): SecureHash {
        logger.info("SecureHash: {}", value)
        return value
    }

    @GetMapping("echoUniqueIdentifier/{value}")
    fun echoUniqueIdentifier(@PathVariable value: UniqueIdentifier): UniqueIdentifier {
        logger.info("UniqueIdentifier: {}", value)
        return value
    }

    @GetMapping("echoCordaX500Name/{value}")
    fun echoCordaX500Name(@PathVariable value: CordaX500Name): CordaX500Name {
        logger.info("CordaX500Name: {}", value)
        return value
    }

    @PostMapping("echoModel")
    fun echoModel(value: EchoModel): EchoModel {
        logger.info("EchoModel: {}", value)
        logger.info("EchoModel secureHash: {}", value.secureHash)
        logger.info("EchoModel cordaX500Name: {}", value.cordaX500Name)
        return value
    }


}
