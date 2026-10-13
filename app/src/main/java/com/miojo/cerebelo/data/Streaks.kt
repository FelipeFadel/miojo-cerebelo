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
