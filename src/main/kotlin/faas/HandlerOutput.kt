package faas

import java.util.*

data class OutputElement(val name: String, val modifiedDate: Date, val size: Long, val downloadLink: String? = null)

data class HandlerOutput(val files: List<OutputElement>)