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
import mypackage.server.innertests.InfoIntegrationTests
import mypackage.server.innertests.NodeIntegrationTests
import mypackage.server.innertests.StateIntegrationTests
import org.junit.jupiter.api.Nested
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate


/**
 * Runs the nested inner tests. Extended by [NodeDriverTest]
 * and [TestContainersTest] to back the tests using a node driver
 * or testcontainers network respectively.
 */
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
abstract class AbstractRootTest {

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractRootTest::class.java)
    }

    // autowire a network service, used to access node services
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var networkService: CordaNetworkService

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Nested
    inner class `Can access Actuator and Swagger` : InfoIntegrationTests(restTemplate, networkService)

    @Nested
    inner class `Can access Node APIs` : NodeIntegrationTests(restTemplate, networkService)

    @Nested
    inner class `Can query and track states` : StateIntegrationTests(restTemplate, networkService)


}
