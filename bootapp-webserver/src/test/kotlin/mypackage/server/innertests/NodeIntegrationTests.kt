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
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import java.util.UUID
import kotlin.test.assertTrue


/** Test node services */
open class NodeIntegrationTests(
        val restTemplate: TestRestTemplate,
        val networkService: CordaNetworkService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeIntegrationTests::class.java)

    }
    @Test
    fun `Can retrieve node identity`() {
        val entity = this.restTemplate.getForEntity("/api/nodes/partya/whoami", Any::class.java)
        Assertions.assertEquals(OK, entity.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
    }

    @Test
    fun `Can retrieve identities`() {
        val entity = this.restTemplate.getForEntity("/api/nodes/partya/identities", JsonNode::class.java)
        Assertions.assertEquals(OK, entity.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
        assertTrue {entity.body!!.toList().isNotEmpty() }
    }

    @Test
    fun `Can retrieve nodes`() {
        val entity = this.restTemplate.getForEntity("/api/nodes/partya/nodes", JsonNode::class.java)
        Assertions.assertEquals(OK, entity.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
        assertTrue {entity.body!!.toList().isNotEmpty() }
    }

    @Test
    fun `Can retrieve peers`() {
        val entity = this.restTemplate.getForEntity("/api/nodes/partya/peers", JsonNode::class.java)
        Assertions.assertEquals(OK, entity.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
        assertTrue {entity.body!!.toList().isNotEmpty() }
    }

    @Test
    fun `Can retrieve notaries`() {
        val entity = this.restTemplate.getForEntity("/api/nodes/partya/notaries", JsonNode::class.java)
        Assertions.assertEquals(OK, entity.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
        assertTrue {entity.body!!.toList().isNotEmpty() }
    }

    @Test
    fun `Can retrieve flows`() {
        val entity = this.restTemplate.getForEntity("/api/nodes/partya/flows", Any::class.java)
        Assertions.assertEquals(OK, entity.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
    }

    @Test
    fun `Can retrieve addresses`() {
        val entity = this.restTemplate.getForEntity("/api/nodes/partya/addresses", Any::class.java)
        Assertions.assertEquals(OK, entity.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
    }


    @Test
    fun `Can handle object conversions`() {
        // convert to<>from SecureHash
        val hash = "6D1687C143DF792A011A1E80670A4E4E0C25D0D87A39514409B1ABFC2043581F"
        val hashEcho = this.restTemplate.getForEntity("/api/echo/echoSecureHash/${hash}", Any::class.java)
        logger.info("hashEcho body:  ${hashEcho.body}")
        Assertions.assertEquals(hash, hashEcho.body)
        // convert to<>from UniqueIdentifier, including external ID with underscore
        val linearId = UniqueIdentifier("foo_bar-baz", UUID.randomUUID())
        val linearIdEcho = this.restTemplate.getForEntity("/api/echo/echoUniqueIdentifier/${linearId}", UniqueIdentifier::class.java)
        logger.info("linearIdEcho body:  ${linearIdEcho.body}")
        Assertions.assertEquals(linearId, linearIdEcho.body)
        // convert to<>from CordaX500Name
        val cordaX500Name = CordaX500Name.parse("O=Bank A, L=New York, C=US, OU=Org Unit, CN=Service Name")
        val cordaX500NameEcho = this.restTemplate
                .getForEntity("/api/echo/echoCordaX500Name/$cordaX500Name", Any::class.java)
        logger.info("cordaX500NameEcho body: ${cordaX500NameEcho.body}")
        Assertions.assertEquals(cordaX500Name, CordaX500Name.parse(cordaX500NameEcho.body.toString()))
    }


    @Test
    @Throws(Exception::class)
    fun `Can save and retrieve regular files as attachments`() {
        // Upload a couple of files
        var attachmentReceipt: JsonNode = uploadAttachmentFiles(
                Pair("test.txt", "text/plain"),
                Pair("test.png", "image/png"))
        // Make sure the attachment has a hash, is not marked as original and contains all uploaded files
        val hash = attachmentReceipt.get("hash").asText()
        Assertions.assertNotNull(hash)
        // TODO assertTrue(attachmentReceipt.files.containsAll(listOf("test.txt", "test.png")))
        // Test archive download
        var attachment = this.restTemplate.getForEntity("/api/nodes/partya/attachments/${hash}", ByteArray::class.java)
        Assertions.assertEquals(OK, attachment.statusCode)
        // Test archive file entry download
        attachment = this.restTemplate.getForEntity("/api/nodes/partya/attachments/${hash}/test.txt", ByteArray::class.java)
        Assertions.assertEquals(OK, attachment.statusCode)

        // Test archive file browsing
        val paths = this.restTemplate.getForObject(
                "/api/nodes/partya/attachments/${hash}/paths",
                List::class.java)
        logger.info("attachment paths: $paths")
        assertTrue(paths.containsAll(listOf("test.txt", "test.png")))
    }

    //@Test
    @Throws(Exception::class)
    fun `Can save and retrieve single zip and jar files as attachments`() {
        testArchiveUploadAndDownload("test.zip", "application/zip")
        testArchiveUploadAndDownload("test.jar", "application/java-archive")
        // Ensure a proper 404
        val attachment = this.restTemplate.getForEntity("/api/nodes/partya/attachments/${SecureHash.randomSHA256()}", ByteArray::class.java)
        Assertions.assertEquals(NOT_FOUND, attachment.statusCode)

    }

    private fun uploadAttachmentFiles(vararg files: Pair<String, String>): JsonNode {
        val parameters = LinkedMultiValueMap<String, Any>()
        files.forEach {
            parameters.add("file", ClassPathResource("/uploadfiles/${it.first}"))
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val entity = HttpEntity(parameters, headers)

        val response = this.restTemplate.exchange("/api/nodes/partya/attachments",
                HttpMethod.POST, entity, JsonNode::class.java, "")

        var attachmentReceipt: JsonNode? = response.body
        logger.info("uploadAttachmentFiles, attachmentReceipt: ${attachmentReceipt.toString()}")
        return attachmentReceipt!!
    }

    private fun testArchiveUploadAndDownload(fileName: String, mimeType: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        // Add the archive to the upload
        // Test upload
        var attachmentReceipt: JsonNode = uploadAttachmentFiles(Pair(fileName, mimeType))
        // Make sure the attachment has a hash, is marked as original and contains the uploaded archive
        val hash = attachmentReceipt.get("hash").asText()
        Assertions.assertNotNull(hash)
        assertTrue(attachmentReceipt.get("savedOriginal").asBoolean())
        //TODO assertNotNull(attachmentReceipt.withArray("files").toList().find{it == fileName}.singleOrNull())
        // Test archive download
        val attachment = this.restTemplate.getForEntity("/api/nodes/partya/attachments/${hash}", ByteArray::class.java)
        Assertions.assertEquals(OK, attachment.statusCode)
    }



}
