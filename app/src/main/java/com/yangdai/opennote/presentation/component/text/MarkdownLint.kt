package com.yangdai.opennote.presentation.component.text

class MarkdownLint {
    companion object {
        private val HEADING_REGEX = Regex("^(#{1,6})\\s.+$")
        private val HEADING_WITH_PUNCTUATION = Regex("^#{1,6}\\s.+[.,;:!?]$")
        private val BLOCKQUOTE_REGEX = Regex("^>\\s.+$")
        private val INVALID_HEADING_INDENT = Regex("^\\s+#")
        private val INVALID_HEADING_SPACE = Regex("^#{1,6}\\s{2,}")
        private val INVALID_BLOCKQUOTE_SPACE = Regex("^>\\s{2,}")
        private val TRAILING_SPACES = Regex("\\s{3,}$")
    }

    data class Issue(val startIndex: Int, val endIndex: Int) {
        fun toPair() = Pair(startIndex, endIndex)
    }

    private fun validateConsecutiveBlankLines(
        markdown: String,
        codeRegions: List<Pair<Int, Int>>,
        issues: MutableList<Issue>
    ) {
        val lines = markdown.split("\n")
        var currentIndex = 0
        var blankLineCount = 0
        var blankLineStart = -1
        var prevLineIsCodeBlock = false

        lines.forEach { line ->
            val isInCode = isInCodeRegion(currentIndex, codeRegions)
            val isCodeBlockBoundary = line.trim().startsWith("```")
            val isBlankLine = line.trim().isEmpty()

            if (isBlankLine) {
                if (!isInCode && !isCodeBlockBoundary && !prevLineIsCodeBlock) {
                    if (blankLineCount == 0) {
                        blankLineStart = currentIndex
                    }
                    blankLineCount++
                    if (blankLineCount > 1) {
                        issues.add(Issue(blankLineStart, currentIndex + line.length))
                    }
                }
            } else {
                blankLineCount = 0
            }

            prevLineIsCodeBlock = isCodeBlockBoundary
            currentIndex += line.length + 1
        }
    }

    private fun validateTrailingSpaces(
        markdown: String,
        codeRegions: List<Pair<Int, Int>>,
        issues: MutableList<Issue>
    ) {
        val lines = markdown.split("\n")
        var currentIndex = 0

        lines.forEach { line ->
            if (!isInCodeRegion(currentIndex, codeRegions)) {
                if (TRAILING_SPACES.find(line) != null) {
                    issues.add(Issue(currentIndex, currentIndex + line.length))
                }
            }
            currentIndex += line.length + 1
        }
    }

    private fun detectCodeRegions(markdown: String): List<Pair<Int, Int>> {
        val codeRegions = mutableListOf<Pair<Int, Int>>()
        var inFencedBlock = false
        var fencedBlockStart = -1
        val lines = markdown.split("\n")
        var currentIndex = 0

        lines.forEach { line ->
            if (inFencedBlock) {
                if (line.startsWith("```")) {
                    codeRegions.add(fencedBlockStart to currentIndex + line.length)
                    inFencedBlock = false
                }
            } else if (line.startsWith("```")) {
                fencedBlockStart = currentIndex
                inFencedBlock = true
            }
            currentIndex += line.length + 1
        }

        var i = 0
        var inInlineCode = false
        var inlineStart = -1

        while (i < markdown.length) {
            val isInFenced = codeRegions.any { i >= it.first && i < it.second }
            if (isInFenced) {
                val region = codeRegions.first { i >= it.first && i < it.second }
                i = region.second
                continue
            }
            if (markdown[i] == '`' && !isEscaped(i, markdown)) {
                if (inInlineCode) {
                    codeRegions.add(inlineStart to i + 1)
                    inInlineCode = false
                } else {
                    inlineStart = i
                    inInlineCode = true
                }
            }
            i++
        }

        if (inInlineCode) {
            codeRegions.add(inlineStart to markdown.length)
        }

        return codeRegions
    }

    private fun validateHeadings(
        markdown: String,
        codeRegions: List<Pair<Int, Int>>,
        issues: MutableList<Issue>
    ) {
        val lines = markdown.split("\n")
        var currentIndex = 0

        lines.forEach { line ->
            if (line.isNotEmpty() && line[0] == '#' && !isInCodeRegion(currentIndex, codeRegions)) {
                validateHeading(line, currentIndex, issues)
            }
            currentIndex += line.length + 1
        }
    }

    private fun validateHeading(line: String, lineStart: Int, issues: MutableList<Issue>) {
        // 检查标题缩进
        if (INVALID_HEADING_INDENT.find(line) != null) {
            issues.add(Issue(lineStart, lineStart + line.length))
            return
        }

        // 检查标题格式
        val headingMatch = HEADING_REGEX.find(line)
        if (headingMatch == null) {
            issues.add(Issue(lineStart, lineStart + line.length))
            return
        }

        // 检查#和文字之间的空格数
        if (INVALID_HEADING_SPACE.find(line) != null) {
            issues.add(Issue(lineStart, lineStart + line.length))
            return
        }

        // 检查标题末尾标点
        if (HEADING_WITH_PUNCTUATION.matches(line)) {
            issues.add(Issue(lineStart, lineStart + line.length))
            return
        }

        val level = headingMatch.groupValues[1].length
        if (level > 6) {
            issues.add(Issue(lineStart, lineStart + line.length))
        }
    }

    private fun validateBlockquotes(
        markdown: String,
        codeRegions: List<Pair<Int, Int>>,
        issues: MutableList<Issue>
    ) {
        val lines = markdown.split("\n")
        var currentIndex = 0

        lines.forEach { line ->
            if (!isInCodeRegion(currentIndex, codeRegions)) {
                if (line.startsWith(">")) {
                    // 检查引用块格式
                    if (!BLOCKQUOTE_REGEX.matches(line)) {
                        issues.add(Issue(currentIndex, currentIndex + line.length))
                    }
                    // 检查>后的空格数
                    if (INVALID_BLOCKQUOTE_SPACE.find(line) != null) {
                        issues.add(Issue(currentIndex, currentIndex + line.length))
                    }
                }
            }
            currentIndex += line.length + 1
        }
    }

    fun validate(markdown: String): List<Pair<Int, Int>> {
        val issues = mutableListOf<Issue>()
        val codeRegions = detectCodeRegions(markdown)

        validateHeadings(markdown, codeRegions, issues)
        validateBlockquotes(markdown, codeRegions, issues)
        validateTrailingSpaces(markdown, codeRegions, issues)
        validateConsecutiveBlankLines(markdown, codeRegions, issues)

        return issues.map { it.toPair() }
    }

    private fun isEscaped(index: Int, markdown: String): Boolean {
        if (index <= 0) return false
        var count = 0
        var i = index - 1
        while (i >= 0 && markdown[i] == '\\') {
            count++
            i--
        }
        return count % 2 == 1
    }

    private fun isInCodeRegion(index: Int, codeRegions: List<Pair<Int, Int>>): Boolean {
        return codeRegions.any { index >= it.first && index < it.second }
    }
}
