package com.yangdai.opennote.presentation.component.note

class MarkdownLint {
    companion object {
        private val HEADING_REGEX = Regex("^(#{1,6})\\s.+$")
        private val HEADING_WITH_PUNCTUATION = Regex("^#{1,6}\\s.+[.,;:!?]$")
        private val BLOCKQUOTE_REGEX = Regex("^>\\s.+$")
        private val INVALID_HEADING_SPACE = Regex("^#{1,6}\\s{2,}")
        private val INVALID_BLOCKQUOTE_SPACE = Regex("^>\\s{2,}")
        private val TRAILING_SPACES = Regex("\\s{3,}$")
        private val CHINESE_LINK_REGEX = Regex("\\[[^\\[\\]]*]（[^（）]*）")
        private val CHINESE_EXCLAMATION_LINK = Regex("！\\[[^\\[\\]]*]")
    }

    data class Issue(val startIndex: Int, val endIndex: Int) {
        fun toPair() = Pair(startIndex, endIndex)
    }

    fun validate(markdown: String): List<Pair<Int, Int>> {
        val issues = mutableListOf<Issue>()
        val codeRegions = detectCodeRegions(markdown)
        val nonCodeSegments = getNonCodeSegments(markdown, codeRegions)

        validateMarkdownElements(nonCodeSegments, issues)
        validateLineBasedElements(markdown, codeRegions, issues)

        return issues.map { it.toPair() }
    }

    private fun validateMarkdownElements(
        segments: List<Pair<Int, String>>,
        issues: MutableList<Issue>
    ) {
        segments.forEach { (offset, text) ->
            validateSegment(text, offset, issues)
        }
    }

    private fun validateSegment(text: String, offset: Int, issues: MutableList<Issue>) {
        var currentIndex = 0
        while (currentIndex < text.length) {
            currentIndex = validateLinks(text, currentIndex, offset, issues)
            currentIndex = validateChineseExclamation(text, currentIndex, offset, issues)
        }
    }

    private fun validateLineBasedElements(
        markdown: String,
        codeRegions: List<Pair<Int, Int>>,
        issues: MutableList<Issue>
    ) {
        val lines = markdown.split("\n")
        var currentIndex = 0
        var blankLineCount = 0
        var blankLineStart = -1

        lines.forEach { line ->
            if (!isInCodeRegion(currentIndex, codeRegions)) {
                when {
                    line.startsWith("#") -> validateHeading(line, currentIndex, issues)
                    line.startsWith(">") -> validateBlockquote(line, currentIndex, issues)
                    TRAILING_SPACES.find(line) != null ->
                        issues.add(Issue(currentIndex, currentIndex + line.length))
                }

                // 检查连续空行
                if (line.trim().isEmpty()) {
                    if (blankLineCount == 0) blankLineStart = currentIndex
                    blankLineCount++
                    if (blankLineCount > 3) {
                        issues.add(Issue(blankLineStart, currentIndex + line.length))
                    }
                } else {
                    blankLineCount = 0
                }
            }
            currentIndex += line.length + 1
        }
    }

    private fun validateLinks(
        text: String,
        startIndex: Int,
        offset: Int,
        issues: MutableList<Issue>
    ): Int {
        val match = CHINESE_LINK_REGEX.find(text, startIndex) ?: return text.length
        val fullMatch = match.value
        val matchStart = match.range.first

        val leftParenIndex = fullMatch.indexOf('（')
        val rightParenIndex = fullMatch.indexOf('）')

        issues.add(
            Issue(
                offset + matchStart + leftParenIndex,
                offset + matchStart + leftParenIndex + 1
            )
        )
        issues.add(
            Issue(
                offset + matchStart + rightParenIndex,
                offset + matchStart + rightParenIndex + 1
            )
        )

        return match.range.last + 1
    }

    private fun validateChineseExclamation(
        text: String,
        startIndex: Int,
        offset: Int,
        issues: MutableList<Issue>
    ): Int {
        val match = CHINESE_EXCLAMATION_LINK.find(text, startIndex) ?: return text.length
        issues.add(
            Issue(
                offset + match.range.first,
                offset + match.range.first + 1
            )
        )
        return match.range.last + 1
    }

    private fun detectCodeRegions(markdown: String): List<Pair<Int, Int>> {
        val codeRegions = mutableListOf<Pair<Int, Int>>()

        // 首先检测围栏式代码块 (```)
        detectFencedCodeBlocks(markdown, codeRegions)

        // 然后检测行内代码块 (` 或 '')
        detectInlineCodeBlocks(markdown, codeRegions)

        return codeRegions.sortedBy { it.first }
    }

    private fun detectFencedCodeBlocks(markdown: String, codeRegions: MutableList<Pair<Int, Int>>) {
        var inFencedBlock = false
        var fencedBlockStart = -1
        val lines = markdown.split("\n")
        var currentIndex = 0

        lines.forEach { line ->
            if (inFencedBlock) {
                if (line.trim().startsWith("```")) {
                    codeRegions.add(fencedBlockStart to currentIndex + line.length)
                    inFencedBlock = false
                }
            } else if (line.trim().startsWith("```")) {
                fencedBlockStart = currentIndex
                inFencedBlock = true
            }
            currentIndex += line.length + 1
        }
    }

    private fun detectInlineCodeBlocks(markdown: String, codeRegions: MutableList<Pair<Int, Int>>) {
        var i = 0
        while (i < markdown.length) {
            // 跳过已检测到的代码区域
            if (isInCodeRegion(i, codeRegions)) {
                val region = codeRegions.first { i >= it.first && i < it.second }
                i = region.second
                continue
            }

            when (markdown[i]) {
                '`' -> {
                    if (!isEscaped(i, markdown)) {
                        // 处理连续的反引号
                        var count = 1
                        while (i + count < markdown.length && markdown[i + count] == '`') {
                            count++
                        }

                        // 寻找对应数量的结束反引号
                        val endIndex = findClosingBackticks(markdown, i + count, count)
                        if (endIndex != -1) {
                            codeRegions.add(i to endIndex + count)
                            i = endIndex + count
                            continue
                        }
                    }
                }

                '\'' -> {
                    if (!isEscaped(i, markdown)) {
                        val endIndex = markdown.indexOf('\'', i + 1)
                        if (endIndex != -1 && !isEscaped(endIndex, markdown)) {
                            codeRegions.add(i to endIndex + 1)
                            i = endIndex + 1
                            continue
                        }
                    }
                }
            }
            i++
        }
    }

    private fun findClosingBackticks(markdown: String, startIndex: Int, count: Int): Int {
        var i = startIndex
        while (i < markdown.length) {
            if (markdown[i] == '`' && !isEscaped(i, markdown)) {
                var matchCount = 1
                while (i + matchCount < markdown.length && markdown[i + matchCount] == '`') {
                    matchCount++
                }
                if (matchCount == count) {
                    return i
                }
                i += matchCount
            } else {
                i++
            }
        }
        return -1
    }

    private fun validateBlockquote(line: String, lineStart: Int, issues: MutableList<Issue>) {
        // 检查引用块基本格式
        if (!BLOCKQUOTE_REGEX.matches(line)) {
            issues.add(Issue(lineStart, lineStart + line.length))
            return
        }

        // 检查>后的空格数
        val invalidSpaceMatch = INVALID_BLOCKQUOTE_SPACE.find(line)
        if (invalidSpaceMatch != null) {
            issues.add(
                Issue(
                    lineStart + 1,  // 从>后开始
                    lineStart + invalidSpaceMatch.range.last + 1  // 到最后一个多余空格
                )
            )
        }
    }

    private fun validateHeading(line: String, lineStart: Int, issues: MutableList<Issue>) {

        // 检查标题格式
        val headingMatch = HEADING_REGEX.find(line)

        if (headingMatch == null) {
            issues.add(Issue(lineStart, lineStart + line.length))
            return
        }

        // 检查#和文字之间的空格数
        val invalidSpaceMatch = INVALID_HEADING_SPACE.find(line)
        if (invalidSpaceMatch != null) {
            // 只标记多余的空格部分
            val hashCount = line.takeWhile { it == '#' }.length
            issues.add(
                Issue(
                    lineStart + hashCount,  // 从#号后开始
                    lineStart + invalidSpaceMatch.range.last + 1 // 到最后一个多余空格
                )
            )
            return
        }

        // 检查标题末尾标点
        val punctuationMatch = HEADING_WITH_PUNCTUATION.find(line)
        if (punctuationMatch != null) {
            // 只标记最后的标点符号
            issues.add(
                Issue(
                    lineStart + line.length - 1,  // 最后一个字符
                    lineStart + line.length
                )
            )
            return
        }
    }

    private fun getNonCodeSegments(
        markdown: String,
        codeRegions: List<Pair<Int, Int>>
    ): List<Pair<Int, String>> {
        val segments = mutableListOf<Pair<Int, String>>()
        var currentIndex = 0

        codeRegions.sortedBy { it.first }.forEach { (start, end) ->
            if (currentIndex < start) {
                segments.add(currentIndex to markdown.substring(currentIndex, start))
            }
            currentIndex = end
        }

        if (currentIndex < markdown.length) {
            segments.add(currentIndex to markdown.substring(currentIndex))
        }

        return segments
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
