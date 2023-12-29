package waltid.openbadgecredential.cli.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val prettyJson = Json { prettyPrint = true }
inline fun <reified T> toPrettyJson(content : T): String = prettyJson.encodeToString(content)
