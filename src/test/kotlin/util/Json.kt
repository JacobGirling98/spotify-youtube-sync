package util

fun String?.jsonify() = this?.let { "\"$it\"" } ?: "null"