package faas

data class OutputElement(val name: String, val modifiedDate: String, val size: Long, val downloadLink: String? = null)

data class HandlerOutput(val files: List<OutputElement>, val count: Int)