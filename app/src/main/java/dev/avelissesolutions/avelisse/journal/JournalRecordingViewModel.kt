package dev.avelissesolutions.avelisse.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Files what was just said into one of the two logs.
 *
 * WHY a ViewModel rather than saving from the screen: the save has to outlive the
 * recording screen. Someone who taps back the instant the transcript appears should
 * still find the entry there, and a coroutine started in the composition would be
 * cancelled with it.
 */
@HiltViewModel
class JournalRecordingViewModel @Inject constructor(
    private val repository: JournalRepository,
) : ViewModel() {

    /** Blank transcripts are dropped by the repository, so nothing is stored for silence. */
    fun save(kind: JournalKind, text: String) {
        viewModelScope.launch {
            repository.saveEntry(kind, text)
        }
    }
}
