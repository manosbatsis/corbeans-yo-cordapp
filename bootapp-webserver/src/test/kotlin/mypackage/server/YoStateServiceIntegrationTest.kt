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
package mypackage.server

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService
import com.github.manosbatsis.corbeans.test.integration.CorbeansSpringExtension
import mypackage.cordapp.contract.YoContract
import mypackage.server.components.YoService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

/**
 * Tests Yo! querying and tracking using a state service
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Note we are using CorbeansSpringExtension Instead of SpringExtension
@ExtendWith(CorbeansSpringExtension::class)
class YoStateServiceIntegrationTest {

    companion object {
        private val logger = LoggerFactory.getLogger(YoStateServiceIntegrationTest::class.java)
    }

    // autowire a network service, used to access node services
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var networkService: CordaNetworkService
    // autowire all created node services directly, mapped by name
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var services: Map<String, CordaNodeService>

    // autowire a node service for a specific node
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("partyANodeService")
    lateinit var service: CordaNodeService

    // autowire a unique custom service
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("partyBNodeService")
    lateinit var customCervice: YoService
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Nested
    inner class `Can access Actuator and Swagger` : InfoIntegrationTests(restTemplate, networkService)

    @Nested
    inner class `Can access Node APIs` : NodeIntegrationTests(restTemplate, networkService, services, customCervice, service)


    @Test
    fun `Can use state service to query and track states`() {
        // Get a state service
        val yoStateService = networkService.getNodeService("partyA")
                .createStateService(YoContract.YoState::class.java)
        // Observe and count Yo! updates
        val yoUpdates = mutableListOf<YoContract.YoState>()
        val yoStateVaultObservable = yoStateService.trackBy().updates
        yoStateVaultObservable.subscribe { update ->
            update.produced.forEach { (state) ->
                yoUpdates.add(state.data)
            }
        }
        // Send a Yo!
        this.restTemplate.getForObject("/api/yo/partyB/yo?target=partyA", Map::class.java)
        // Give some time to the async tracking process
        Thread.sleep(1000);
        var yoStates = yoStateService.queryBy()
        val yoCount = yoStates.states.size
        Assertions.assertTrue(yoCount > 0)
        // Send a second Yo!
        this.restTemplate.getForObject("/api/yo/partyB/yo?target=partyA", Map::class.java)
        // Give some time to the async tracking process
        Thread.sleep(1000);
        yoStates = yoStateService.queryBy()
        // New query should return "previous count + 1" results
        Assertions.assertEquals(yoCount + 1, yoStates.states.size)
    }


}
