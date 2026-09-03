package dev.avelissesolutions.avelisse.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the entries screen: what is on it, what is being searched for, and deletion.
 *
 * WHY no debounce on the query: the search runs against one indexed column in a local
 * database, so results land within a frame. Waiting before showing them would only make
 * the screen feel slower than it is.
 */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class EntriesViewModel @Inject constructor(
    private val repository: JournalRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    /** What the person has typed into the search field. Blank means show everything. */
    val query: StateFlow<String> = _query.asStateFlow()

    /**
     * The entries to show, newest first, re-queried whenever the search term changes and
     * re-emitted whenever an entry is added or removed.
     */
    val entries: StateFlow<List<JournalEntry>> = _query
        .flatMapLatest { repository.search(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    fun setQuery(value: String) {
        _query.value = value
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }

    private companion object {
        /** Keeps the query alive across a rotation instead of tearing it down and re-running it. */
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
