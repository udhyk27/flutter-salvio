package com.ydh.salvio.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 화면 전반에서 쓰던 날짜 포맷 로직을 한곳으로 모은 헬퍼.
 * 모든 함수는 ISO-8601 문자열을 받아 파싱 실패 시 앞 10자(yyyy-MM-dd)로 폴백한다.
 */

private fun String.toInstantOrNull(): Instant? =
    try { Instant.parse(this) } catch (e: Exception) { null }

/** 절대 시각을 지정 패턴으로 포맷 (시스템 타임존). */
fun formatAbsolute(dateStr: String, pattern: String): String {
    val instant = dateStr.toInstantOrNull() ?: return dateStr.take(10)
    return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault()).format(instant)
}

/** 분/시간 단위 상대 시간, 하루 이상은 MM/dd. (알림 등 최근 이벤트용) */
fun relativeTime(dateStr: String): String {
    val instant = dateStr.toInstantOrNull() ?: return dateStr.take(10)
    val now = Instant.now()
    val hours = ChronoUnit.HOURS.between(instant, now)
    return when {
        hours < 1 -> "${ChronoUnit.MINUTES.between(instant, now)}분 전"
        hours < 24 -> "${hours}시간 전"
        else -> DateTimeFormatter.ofPattern("MM/dd").withZone(ZoneId.systemDefault()).format(instant)
    }
}

/** 오늘/어제/N일 전/N주 전/N개월 전. (일 단위 경과 표시용) */
fun relativeDays(dateStr: String): String {
    val instant = dateStr.toInstantOrNull() ?: return dateStr.take(10)
    val days = ChronoUnit.DAYS.between(instant, Instant.now())
    return when {
        days == 0L -> "오늘"
        days == 1L -> "어제"
        days < 7L -> "${days}일 전"
        days < 30L -> "${days / 7}주 전"
        else -> "${days / 30}개월 전"
    }
}
