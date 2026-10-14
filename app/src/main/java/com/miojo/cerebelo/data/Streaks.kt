package com.miojo.cerebelo.data

import java.time.LocalDate

/** Cálculo de sequências de dias seguidos. Dias = LocalDate.toEpochDay(). */
object Streaks {

    /**
     * Sequência atual. Se hoje ainda não foi marcado, conta até ontem
     * (o dia ainda não acabou, então a sequência não está quebrada).
     */
    fun current(days: Set<Long>, today: LocalDate): Int {
        var day = today.toEpochDay()
        if (day !in days) day -= 1
        var count = 0
        while (day in days) {
            count++
            day--
        }
        return count
    }

    /**
     * Dias "cumpridos" do checklist inteiro: todos os itens que já existiam
     * naquele dia foram marcados. [habitStartDays] = id do item -> dia em que foi criado.
     */
    fun fulfilledDays(habitStartDays: Map<Long, Long>, checks: Map<Long, Set<Long>>): Set<Long> =
        checks.filter { (day, doneIds) ->
            val required = habitStartDays.filterValues { start -> start <= day }.keys
            required.isNotEmpty() && doneIds.containsAll(required)
        }.keys

    /** Maior sequência de todos os tempos. */
    fun best(days: Collection<Long>): Int {
        var best = 0
        var run = 0
        var previous: Long? = null
        for (day in days.toSortedSet()) {
            run = if (previous != null && day == previous + 1) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }
        return best
    }
}
