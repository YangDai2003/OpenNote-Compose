package com.yangdai.opennote.presentation.state

import androidx.compose.runtime.Stable

@Stable
data class TextState(
    val charCount: Int = 0,
    val wordCountWithPunctuation: Int = 0,
    val wordCountWithoutPunctuation: Int = 0,
    val lineCount: Int = 0,
    val paragraphCount: Int = 0
) {
    companion object {
        private val cjkRanges = arrayOf(
            0x4E00..0x9FFF,  // CJK 统一汉字
            0x3400..0x4DBF,  // CJK 扩展 A
            0x20000..0x2A6DF,// CJK 扩展 B
            0xF900..0xFAFF,  // CJK 兼容汉字
            0x2F800..0x2FA1F // CJK 兼容扩展
        )

        private fun isCJK(ch: Int): Boolean {
            return cjkRanges.any { ch in it }
        }

        private fun isPunctuation(ch: Char): Boolean {
            return ch in "，。、：；？！\"'（）《》「」【】!§$%&/()=?`*_:;><|,.#+~\\´{[]}"
        }

        fun fromText(text: CharSequence): TextState {
            if (text.isBlank()) return TextState()

            val charCount = text.length
            var lineCount = 1
            var paragraphCount = 1
            var punctuationIncludedWordCount = 0
            var nonPunctuationWordCount = 0

            var state = 0
            val inWord = 1
            val previousWasNewline = 2

            var i = 0
            while (i < text.length) {
                val ch = text[i]
                when {
                    ch == '\n' -> {
                        state = state and inWord.inv()
                        lineCount++

                        if ((state and previousWasNewline) != 0) {
                            paragraphCount++
                        }
                        state = state or previousWasNewline
                    }

                    ch.isWhitespace() -> {
                        state = state and (inWord or previousWasNewline).inv()
                    }

                    isCJK(ch.code) -> {
                        punctuationIncludedWordCount++
                        nonPunctuationWordCount++
                        state = state and (inWord or previousWasNewline).inv()
                    }

                    isPunctuation(ch) -> {
                        punctuationIncludedWordCount++
                        state = state and (inWord or previousWasNewline).inv()
                    }

                    else -> {
                        if ((state and inWord) == 0) {
                            state = state or inWord
                            punctuationIncludedWordCount++
                            nonPunctuationWordCount++
                        }
                        state = state and previousWasNewline.inv()
                    }
                }
                i++
            }

            return TextState(
                charCount = charCount,
                wordCountWithPunctuation = punctuationIncludedWordCount,
                wordCountWithoutPunctuation = nonPunctuationWordCount,
                lineCount = lineCount,
                paragraphCount = paragraphCount
            )
        }
    }
}
