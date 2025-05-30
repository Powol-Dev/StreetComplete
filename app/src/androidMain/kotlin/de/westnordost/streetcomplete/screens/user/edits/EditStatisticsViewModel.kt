package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.flags.FlagAlignments
import de.westnordost.streetcomplete.data.flags.readFlagAlignments
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class EditStatisticsViewModel : ViewModel() {

    abstract val isSynchronizingStatistics: StateFlow<Boolean>

    abstract val hasEdits: StateFlow<Boolean>
    abstract val countryStatistics: StateFlow<List<CountryStatistics>?>
    abstract val editTypeStatistics: StateFlow<List<EditTypeStatistics>?>

    abstract val hasEditsCurrentWeek: StateFlow<Boolean>
    abstract val countryStatisticsCurrentWeek: StateFlow<List<CountryStatistics>?>
    abstract val editTypeStatisticsCurrentWeek: StateFlow<List<EditTypeStatistics>?>

    abstract fun queryCountryStatistics()
    abstract fun queryEditTypeStatistics()

    abstract val flagAlignments: StateFlow<FlagAlignments?>
}

@Immutable
data class EditTypeStatistics(val type: EditType, val count: Int)

@Stable
class EditStatisticsViewModelImpl(
    private val statisticsSource: StatisticsSource,
    private val allEditTypes: AllEditTypes,
    private val res: Res,
) : EditStatisticsViewModel() {

    override val isSynchronizingStatistics = MutableStateFlow(statisticsSource.isSynchronizing)

    override val hasEdits = MutableStateFlow(true)
    override val countryStatistics = MutableStateFlow<List<CountryStatistics>?>(null)
    override val editTypeStatistics = MutableStateFlow<List<EditTypeStatistics>?>(null)

    override val hasEditsCurrentWeek = MutableStateFlow(true)
    override val countryStatisticsCurrentWeek = MutableStateFlow<List<CountryStatistics>?>(null)
    override val editTypeStatisticsCurrentWeek = MutableStateFlow<List<EditTypeStatistics>?>(null)

    override val flagAlignments = MutableStateFlow<FlagAlignments?>(null)

    // no updating of data implemented (because actually not needed. Not possible to add edits
    // while in this screen)

    init {
        launch(IO) { hasEdits.value = statisticsSource.getEditCount() > 0 }
        launch(IO) { hasEditsCurrentWeek.value = statisticsSource.getCurrentWeekEditCount() > 0 }
        launch(IO) { flagAlignments.value = res.readFlagAlignments() }
    }

    override fun queryCountryStatistics() {
        if (countryStatistics.value == null) {
            launch(IO) {
                countryStatistics.value = statisticsSource
                    .getCountryStatistics()
                    .sortedByDescending { it.count }
            }
        }
        if (countryStatisticsCurrentWeek.value == null) {
            launch(IO) {
                countryStatisticsCurrentWeek.value = statisticsSource
                    .getCurrentWeekCountryStatistics()
                    .sortedByDescending { it.count }
            }
        }
    }

    override fun queryEditTypeStatistics() {
        if (editTypeStatistics.value == null) {
            launch(IO) {
                editTypeStatistics.value = statisticsSource
                    .getEditTypeStatistics()
                    .mapNotNull { createCompleteEditTypeStatistics(it.type, it.count) }
                    .sortedByDescending { it.count }
            }
        }
        if (editTypeStatisticsCurrentWeek.value == null) {
            launch(IO) {
                editTypeStatisticsCurrentWeek.value = statisticsSource
                    .getCurrentWeekEditTypeStatistics()
                    .mapNotNull { createCompleteEditTypeStatistics(it.type, it.count) }
                    .sortedByDescending { it.count }
            }
        }
    }

    private fun createCompleteEditTypeStatistics(typeName: String, count: Int): EditTypeStatistics? {
        val editType = allEditTypes.getByName(typeName) ?: return null
        return EditTypeStatistics(editType, count)
    }
}
