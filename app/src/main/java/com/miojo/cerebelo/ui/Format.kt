package com.miojo.cerebelo.ui

import com.miojo.cerebelo.data.db.TagType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", ptBr)
private val dayFormat = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ptBr)
private val dayYearFormat = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", ptBr)
private val shortDateTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", ptBr)

fun millisToDateTime(millis: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

fun formatTime(millis: Long): String = millisToDateTime(millis).format(timeFormat)

fun formatDateTime(millis: Long): String = millisToDateTime(millis).format(shortDateTimeFormat)

/** "Hoje", "Ontem" ou "Terça-feira, 13 de outubro". */
fun formatDay(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Hoje"
    today.minusDays(1) -> "Ontem"
    else -> formatFullDay(date, today)
}

/** Sempre a data por extenso: "Terça-feira, 13 de outubro". */
fun formatFullDay(date: LocalDate, today: LocalDate = LocalDate.now()): String {
    val format = if (date.year == today.year) dayFormat else dayYearFormat
    return date.format(format).replaceFirstChar { it.titlecase(ptBr) }
}

val TagType.label: String
    get() = when (this) {
        TagType.PERSON -> "Pessoa"
        TagType.EVENT -> "Acontecimento"
    }

/** Texto do chip: pessoas pelo nome, acontecimentos com "#". */
fun tagChipText(name: String, type: TagType): String =
    if (type == TagType.EVENT) "#$name" else name
