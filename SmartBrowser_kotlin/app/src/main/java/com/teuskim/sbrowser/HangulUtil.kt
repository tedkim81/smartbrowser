package com.teuskim.sbrowser


object HangulUtil {

    private val JASO_START_INDEX = 0x3131
    private val HANGUL_START_INDEX = 0xAC00
    private val JAUM_FULL_SIZE = 30  // 곁자음 포함한 수

    // 곁자음/곁모음 포함한 총 51개의 자소 중 키보드에 들어간 33개의 자소에 대한 인덱스
    val JASO_ARR = intArrayOf(0, 1, 3, 6, 7, 8, 16, 17, 18, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 42, 43, 47, 48, 50, 39, 40, 41, 44, 45, 46, 49)//  ㄱㄲ ㄴㄷㄸ ㄹ ㅁ ㅂ  ㅃ ㅅ ㅆ  ㅇ ㅈ  ㅉ ㅊ  ㅋ ㅌ ㅍ  ㅎ
    //   ㅏ  ㅐ ㅑ  ㅒ ㅓ ㅔ  ㅕ ㅖ ㅗ  ㅛ  ㅜ ㅠ ㅡ  ㅣ
    //   ㅘ ㅙ  ㅚ ㅝ  ㅞ ㅟ  ㅢ

    // 중성에 들어갈 수 있는 모음들의 인덱스
    var JUNGSUNG_ARR = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 12, 13, 17, 18, 20, 9, 10, 11, 14, 15, 16, 19)
    //  ㅏㅐ ㅑㅒㅓ ㅔㅕ ㅖㅗ ㅛ  ㅜ ㅠ  ㅡ ㅣ ㅘ ㅙ ㅚ  ㅝ ㅞ  ㅟ ㅢ

    // 종성에 들어갈 수 있는 자음들의 인덱스. ㄱ이 1이다. -1인 경우 종성에 들어갈 수 없으니 다음 음절 초성으로 분리한다.
    var JONGSUNG_ARR = intArrayOf(1, 2, 4, 7, -1, 8, 16, 17, -1, 19, 20, 21, 22, -1, 23, 24, 25, 26, 27)
    //  ㄱㄲ ㄴㄷ ㄸ ㄹ ㅁ ㅂ  ㅃ ㅅ ㅆ  ㅇ ㅈ  ㅉ ㅊ  ㅋ ㅌ  ㅍ ㅎ

    fun separateJaso(word: String): String {
        val sb = StringBuilder()
        for (i in 0 until word.length) {
            val ch = word[i]
            if (ch.toInt() >= HANGUL_START_INDEX) {  // 조합형 한글이면 분리하여 반환한다.
                val index = ch.toInt() - HANGUL_START_INDEX
                val chosungIndex = index / (21 * 28)
                val jungsungIndex = index % (21 * 28) / 28
                val jongsungIndex = index % 28

                sb.append((JASO_START_INDEX + JASO_ARR[chosungIndex]).toChar())
                sb.append((JASO_START_INDEX + JAUM_FULL_SIZE + jungsungIndex).toChar())
                if (jongsungIndex > 0)
                    sb.append((JASO_START_INDEX + JASO_ARR[jongToCho(jongsungIndex)]).toChar())
            } else
                sb.append(ch)
        }
        return sb.toString()
    }

    private fun jongToCho(jong: Int): Int {
        var choIdx = 0
        for (i in JONGSUNG_ARR.indices) {
            if (jong == JONGSUNG_ARR[i]) {
                choIdx = i
                break
            }
        }
        return choIdx
    }
}
