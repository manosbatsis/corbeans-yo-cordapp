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

import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File


/**
 * Executes the [AbstractRootTest] testsuite
 * against a testcontainers-based Corda network.
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = [
            "corbeans.nodes.default.disableGracefulReconnect=true",
            "spring.liquibase.enabled=false",
            "logging.level.com.github.manosbatsis=DEBUG",
            "logging.level.mypackage=DEBUG"])
@ExtendWith(SpringExtension::class)
class TestContainersTest: AbstractRootTest() {

    companion object {
        private val logger = LoggerFactory.getLogger(TestContainersTest::class.java)

        @JvmStatic
        val network = Network.newNetwork()

        @Container
        @JvmStatic
        val cordformNetworkContainer = CordformNetworkContainer(
            imageName = NodeImageNameConfig.CORDA_OS_ZULU_4_8,
            nodesDir = File(System.getProperty("user.dir"))
                .parentFile.resolve("build/nodes"),
            cloneNodesDir = true,
            network = network,
            imageCordaArgs = "--logging-level DEBUG"
            // Uncomment to use Postgres containers instead of embedded H2
            //databaseSettings = CordformDatabaseSettingsFactory.POSTGRES
        )

        /** Apply nodes' container info to application properties */
        @DynamicPropertySource
        @JvmStatic
        fun applytestProperties(registry: DynamicPropertyRegistry) {
            cordformNetworkContainer.nodes
                .filterNot { (nodeName, instance) ->
                    nodeName.toLowerCase().contains("notary")
                            || instance.config.hasPath("notary")
                }
                .forEach { (nodeName, container) ->
                    val nodeConf = container.simpleNodeConfig
                    val user = container.getDefaultRpcUser()
                    val prefix = "corbeans.nodes.$nodeName"

                    with(registry) {
                        add("$prefix.partyName") { "${nodeConf.myLegalName}" }
                        add("$prefix.username") { user.username }
                        add("$prefix.password") { user.password }
                        add("$prefix.address") { container.rpcAddress }
                        add("$prefix.adminAddress") { container.rpcAddress }
                        add("$prefix.admin-address") { container.rpcAddress }
                    }
                }
        }
    }

}
