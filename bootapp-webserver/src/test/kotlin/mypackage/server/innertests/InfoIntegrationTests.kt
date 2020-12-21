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
package mypackage.server.innertests


import com.fasterxml.jackson.databind.JsonNode
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

/** Test swagger and actuator endpoints */
open class InfoIntegrationTests(
        val restTemplate: TestRestTemplate,
        val networkService: CordaNetworkService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InfoIntegrationTests::class.java)

    }

    @Test
    fun `Can access swagger UI`() {
        // Check swagger endpoint
        var apiDocs = restTemplate
                .getForEntity("/v3/api-docs", JsonNode::class.java)
        // Ensure a 200 OK
        Assertions.assertEquals(HttpStatus.OK, apiDocs.statusCode)
        // Check Swagger UI
        var swaggerUi = restTemplate.getForEntity("/swagger-ui.html", String::class.java)
        // Ensure a 200 OK
        //Assertions.assertEquals(HttpStatus.OK, swaggerUi.statusCode)
    }

    @Test
    fun `Can see Corda details within Actuator info endpoint response`() {
        logger.info("testInfoContributor, called")
        val entity = this.restTemplate
                .getForEntity("/actuator/info", Map::class.java)
        // Ensure a 200 OK
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)

        val body = entity.body
        Assertions.assertNotNull(body, "Actuator info must not be null")
        val corda = body!!["corda"] as Map<*, *>?
        // Validate corda information
        validateCordaInfo(corda)
    }

    @Test
    fun `Can access Corda custom Actuator endpoint`() {
        logger.info("testCordaEndpoint, called")
        val entity = this.restTemplate
                .getForEntity("/actuator/corda", Map::class.java)
        // Ensure a 200 OK
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        val corda = entity.body
        // Validate corda information
        validateCordaInfo(corda)
    }


    private fun validateCordaInfo(corda: Map<*, *>?) {
        Assertions.assertNotNull(corda, "Actuator corda info must not be null")
        val cordaNodes = corda!!["nodes"] as Map<String, Any>
        Assertions.assertNotNull(cordaNodes["partya"])
        Assertions.assertNotNull(cordaNodes["partyb"])
    }


}
