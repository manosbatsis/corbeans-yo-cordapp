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


import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.Sort

// TODO: implement org.springframework.data.domain.Page
/** Wraps paged results */
open class ResultsPage<T>(
        var content: List<T> = emptyList(),
        var pageNumber: Int = 1,
        var pageSize: Int = 10,
        var totalResults: Long = 0,
        var sort: String = "none",
        var sortDirection: Sort.Direction?
) {

    constructor(
            content: List<T> = emptyList(),
            pageSpecification: PageSpecification,
            sort: Sort,
            totalResults: Long
    ) : this(
            content,
            pageSpecification.pageNumber,
            pageSpecification.pageSize,
            totalResults,
            sort.columns.first().sortAttribute.toString(),
            sort.columns.first().direction)
}
