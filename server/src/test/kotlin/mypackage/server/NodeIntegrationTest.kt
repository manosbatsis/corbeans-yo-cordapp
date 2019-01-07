/*
 * 	Corbeans Yo! Cordapp: Sample/Template project for Corbeans.
 * 	https://manosbatsis.github.io/corbeans/
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
package mypackage.server

import com.github.manosbatsis.corbeans.test.integration.WithImplicitNetworkIT
import mypackage.server.components.YoService
import net.corda.core.identity.Party
import net.corda.core.utilities.NetworkHostAndPort
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertTrue

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(
        classes = arrayOf(Application::class),
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class NodeIntegrationTest : WithImplicitNetworkIT() {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeIntegrationTest::class.java)

    }

    // autowire all created services, mapped by name
    @Autowired
    lateinit var services: Map<String, YoService>

    // autowire the same services individually
    @Autowired
    @Qualifier("partyANodeService")
    lateinit var partyAService: YoService
    @Autowired
    @Qualifier("partyBNodeService")
    lateinit var partyBService: YoService

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `Can use both default node and multiple node controller endpoints`() {
        val defaultNodeMe = this.restTemplate.getForObject("/api/node/me", Map::class.java)
        Assertions.assertEquals("me", defaultNodeMe.keys.first())
        val partyANodeMe = this.restTemplate.getForObject("/api/nodes/partyA/me", Map::class.java)
        Assertions.assertEquals("me", partyANodeMe.keys.first())
    }


    @Test
    fun `Can inject custom services`() {
        logger.info("services: {}", services)
        assertNotNull(this.services)
        assertNotNull(this.partyAService)
        assertTrue(this.services.keys.isNotEmpty())
    }


    @Test
    fun `Can retrieve notaries`() {
        val notaries: List<Party> = partyAService.notaries()
        assertNotNull(notaries)
    }

    @Test
    fun `Can retrieve flows`() {
        val flows: List<String> = partyAService.flows()
        assertNotNull(flows)
    }

    @Test
    fun `Can retrieve addresses`() {
        val addresses: List<NetworkHostAndPort> = partyAService.addresses()
        assertNotNull(addresses)
    }

}