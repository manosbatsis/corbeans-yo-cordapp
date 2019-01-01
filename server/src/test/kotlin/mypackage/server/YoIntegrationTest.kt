/**
 *     Corbeans: Corda integration for Spring Boot
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 3 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */
package mypackage.server

import com.github.manosbatsis.corbeans.corda.webserver.Application
import com.github.manosbatsis.corbeans.test.integration.WithImplicitNetworkIT
import mypackage.server.components.YoService
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
class YoIntegrationTest : WithImplicitNetworkIT() {

    companion object {
        private val logger = LoggerFactory.getLogger(YoIntegrationTest::class.java)

    }

    override fun getCordappPackages(): List<String> = listOf("net.corda.finance")

    // autowire all created services, mapped by name
    @Autowired
    lateinit var services: Map<String, YoService>

    // autowire custom services for specific nodes
    @Autowired
    @Qualifier("partyANodeService")
    lateinit var partyAService: YoService
    @Autowired
    @Qualifier("partyBNodeService")
    lateinit var partyBService: YoService

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `Can inject services`() {
        logger.info("services: {}", services)
        assertNotNull(this.partyAService)
        assertNotNull(this.partyBService)
        assertTrue(this.services.keys.isNotEmpty())
    }

    @Test
    fun `Can send Yo!`() {
        val yo = this.restTemplate.getForObject("/yo/partyA/yo?target=PartyB", Map::class.java)
        logger.debug("Yo response: {}", yo)
        Assertions.assertTrue(yo.keys.contains("message"))
    }


}