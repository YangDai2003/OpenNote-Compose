package com.yangdai.opennote.presentation.util.extension.properties

/**
 * Utility class for parsing note properties in YAML format
 */
object Properties {
    // Regex pattern to extract the YAML block between "---" markers at the beginning of a note
    private val YAML_BLOCK_PATTERN =
        "\\A---\\s*\\n([\\s\\S]*?)\\n---".toRegex(RegexOption.MULTILINE)

    /**
     * 获取YAML属性块的范围
     * @param noteContent 笔记的完整内容
     * @return 属性块的范围，如果没有属性块则返回null
     */
    fun getPropertiesRange(noteContent: String): IntRange? {
        val matchResult = YAML_BLOCK_PATTERN.find(noteContent)
        return matchResult?.range
    }

    /**
     * 将笔记内容分离为YAML属性部分和正文内容部分
     * @param noteContent 完整的笔记内容
     * @return 包含属性和内容的Pair对象，如果没有属性块则属性部分为null
     */
    fun splitPropertiesAndContent(noteContent: String): Pair<String, String> {
        val matchResult = YAML_BLOCK_PATTERN.find(noteContent)

        return if (matchResult != null) {
            // 提取YAML属性部分（包括分隔符）
            val propertiesWithDelimiters = noteContent.substring(matchResult.range)
            // 提取正文内容（不包括属性块）
            val content = noteContent.substring(matchResult.range.last + 1).trim()

            Pair(propertiesWithDelimiters, content)
        } else {
            // 没有找到属性块，返回null作为属性部分，完整内容作为正文部分
            Pair("", noteContent)
        }
    }
}
