package com.miojo.cerebelo.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.NoteRepository
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.data.db.TagWithCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.ZoneId

/** [people]/[events] nulos = carregando. */
data class PeopleState(
    val people: List<TagWithCount>? = null,
    val events: List<TagWithCount>? = null,
)

/** Lista de pessoas e acontecimentos, de todas as datas ou de um mês. */
@OptIn(ExperimentalCoroutinesApi::class)
class PeopleViewModel(private val repository: NoteRepository) : ViewModel() {

    /** null = todas as datas. */
    private val _month = MutableStateFlow<YearMonth?>(null)
    val month: StateFlow<YearMonth?> = _month.asStateFlow()

    val state: StateFlow<PeopleState> = _month
        .flatMapLatest { month ->
            if (month == null) {
                repository.observeTagsWithCounts()
            } else {
                val zone = ZoneId.systemDefault()
                val from = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val to = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                repository.observeTagsWithCountsBetween(from, to)
            }
        }
        .map { all ->
            PeopleState(
                people = all.filter { it.tag.type == TagType.PERSON },
                events = all.filter { it.tag.type == TagType.EVENT },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeopleState())

    fun showAllDates() {
        _month.value = null
    }

    /** Mostra um mês; sem argumento, o mês atual. */
    fun showMonth(month: YearMonth = YearMonth.now()) {
        _month.value = minOf(month, YearMonth.now())
    }

    fun previousMonth() = showMonth((_month.value ?: YearMonth.now()).minusMonths(1))

    fun nextMonth() = showMonth((_month.value ?: YearMonth.now()).plusMonths(1))
}
