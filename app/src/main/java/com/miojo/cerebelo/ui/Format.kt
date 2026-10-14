package com.miojo.cerebelo.ui

import com.miojo.cerebelo.data.db.TagType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", ptBr)
private val dayMonthFormat = DateTimeFormatter.ofPattern("d 'de' MMMM", ptBr)
private val dayMonthYearFormat = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ptBr)
private val shortDayFormat = DateTimeFormatter.ofPattern("d MMM", ptBr)
private val shortDayYearFormat = DateTimeFormatter.ofPattern("d MMM yyyy", ptBr)

fun millisToDateTime(millis: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

fun millisToDate(millis: Long): LocalDate = millisToDateTime(millis).toLocalDate()

fun formatTime(millis: Long): String = millisToDateTime(millis).format(timeFormat)

private fun String.capitalized() = replaceFirstChar { it.titlecase(ptBr) }

/** Nome do mês sem o ponto da abreviação: "out". */
private fun LocalDate.shortDay(today: LocalDate): String =
    format(if (year == today.year) shortDayFormat else shortDayYearFormat).replace(".", "")

/** "Terça, 13 de outubro" (cabeçalhos). */
fun formatLongDay(date: LocalDate, today: LocalDate = LocalDate.now()): String {
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, ptBr).substringBefore("-").capitalized()
    val rest = date.format(if (date.year == today.year) dayMonthFormat else dayMonthYearFormat)
    return "$weekday, $rest"
}

/** Título de grupo da linha do tempo: "Hoje · 13 out", "Ontem · 12 out", "Sábado · 10 out". */
fun formatGroupDay(date: LocalDate, today: LocalDate = LocalDate.now()): String {
    val name = when (date) {
        today -> "Hoje"
        today.minusDays(1) -> "Ontem"
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, ptBr).substringBefore("-").capitalized()
    }
    return "$name · ${date.shortDay(today)}"
}

/** Hora de uma nota nas listas: "19:04" hoje, "Ontem", ou "10 out". */
fun formatNoteWhen(millis: Long, today: LocalDate = LocalDate.now()): String {
    val date = millisToDate(millis)
    return when (date) {
        today -> formatTime(millis)
        today.minusDays(1) -> "Ontem"
        else -> date.shortDay(today)
    }
}

/** "13 out" (datas curtas). */
fun formatShortDay(millis: Long, today: LocalDate = LocalDate.now()): String = millisToDate(millis).shortDay(today)

/** "02 de setembro" (contador da pessoa). */
fun formatSince(millis: Long, today: LocalDate = LocalDate.now()): String {
    val date = millisToDate(millis)
    val pattern = if (date.year == today.year) "dd 'de' MMMM" else "dd 'de' MMMM 'de' yyyy"
    return date.format(DateTimeFormatter.ofPattern(pattern, ptBr))
}

/** "13" e "out" para a coluna de datas da pessoa. */
fun formatDayNumber(millis: Long): String = millisToDate(millis).dayOfMonth.toString().padStart(2, '0')

fun formatMonthShort(millis: Long): String =
    millisToDate(millis).month.getDisplayName(TextStyle.SHORT, ptBr).replace(".", "")

/** "Outubro 2026". */
fun formatMonth(month: YearMonth): String =
    "${month.month.getDisplayName(TextStyle.FULL_STANDALONE, ptBr).capitalized()} ${month.year}"

/** Inicial do dia da semana: S T Q Q S S D. */
fun weekdayInitial(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "S"
    DayOfWeek.TUESDAY -> "T"
    DayOfWeek.WEDNESDAY -> "Q"
    DayOfWeek.THURSDAY -> "Q"
    DayOfWeek.FRIDAY -> "S"
    DayOfWeek.SATURDAY -> "S"
    DayOfWeek.SUNDAY -> "D"
}

/** Abreviação do dia da semana: SEG TER QUA QUI SEX SÁB DOM. */
fun weekdayShort(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "Seg"
    DayOfWeek.TUESDAY -> "Ter"
    DayOfWeek.WEDNESDAY -> "Qua"
    DayOfWeek.THURSDAY -> "Qui"
    DayOfWeek.FRIDAY -> "Sex"
    DayOfWeek.SATURDAY -> "Sáb"
    DayOfWeek.SUNDAY -> "Dom"
}

fun notesCount(count: Int): String = if (count == 1) "1 nota" else "$count notas"

fun daysInRow(count: Int): String = if (count == 1) "1 dia seguido" else "$count dias seguidos"

val TagType.label: String
    get() = when (this) {
        TagType.PERSON -> "Pessoa"
        TagType.EVENT -> "Acontecimento"
    }
