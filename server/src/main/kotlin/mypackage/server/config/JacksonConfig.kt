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
package com.github.manosbatsis.corbeans.corda.webserver.config

import net.corda.client.jackson.JacksonSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jackson.JsonComponentModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

/**
 * Configure Corda RPC ObjectMapper for Jackson
 */
@Configuration
class JacksonConfig {

    /** Force Spring/Jackson to use the provided Corda ObjectMapper for serialization */
    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun mappingJackson2HttpMessageConverter(
            @Autowired jsonComponentModule: JsonComponentModule
    ): MappingJackson2HttpMessageConverter {
        var mapper = JacksonSupport.createNonRpcMapper()
        mapper.registerModule(jsonComponentModule)

        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = mapper
        return converter
    }
}