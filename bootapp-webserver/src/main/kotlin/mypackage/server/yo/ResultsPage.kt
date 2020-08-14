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
