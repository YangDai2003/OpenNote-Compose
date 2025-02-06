package com.yangdai.opennote.presentation.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TemplateProcessor(
    defaultDateFormat: String = "yyyy-MM-dd",
    defaultTimeFormat: String = "HH:mm"
) {
    private var defaultDateFormat: String = defaultDateFormat.takeIf { it.isNotBlank() } ?: "yyyy-MM-dd"
    private var defaultTimeFormat: String = defaultTimeFormat.takeIf { it.isNotBlank() } ?: "HH:mm"

    // 修改正则表达式以先匹配简单格式
    private val simpleDatePattern = "\\{\\{date\\}\\}"
    private val simpleTimePattern = "\\{\\{time\\}\\}"
    private val dateWithFormatPattern = "\\{\\{date:([^}]+)\\}\\}"
    private val timeWithFormatPattern = "\\{\\{time:([^}]+)\\}\\}"

    fun process(content: String): String {
        if (content.isBlank()) return content

        var result = content

        // 先处理简单格式
        result = result.replace(simpleDatePattern.toRegex()) {
            LocalDate.now().format(DateTimeFormatter.ofPattern(defaultDateFormat))
        }

        result = result.replace(simpleTimePattern.toRegex()) {
            LocalTime.now().format(DateTimeFormatter.ofPattern(defaultTimeFormat))
        }

        // 再处理带格式的模式
        result = result.replace(dateWithFormatPattern.toRegex()) { matchResult ->
            val format = matchResult.groupValues[1]
            try {
                LocalDate.now().format(DateTimeFormatter.ofPattern(format))
            } catch (_: Exception) {
                LocalDate.now().format(DateTimeFormatter.ofPattern(defaultDateFormat))
            }
        }

        result = result.replace(timeWithFormatPattern.toRegex()) { matchResult ->
            val format = matchResult.groupValues[1]
            try {
                LocalTime.now().format(DateTimeFormatter.ofPattern(format))
            } catch (_: Exception) {
                LocalTime.now().format(DateTimeFormatter.ofPattern(defaultTimeFormat))
            }
        }

        return result
    }
}
