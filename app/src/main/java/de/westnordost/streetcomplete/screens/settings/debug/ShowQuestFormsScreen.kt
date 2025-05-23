package de.westnordost.streetcomplete.screens.settings.debug

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.common.ExpandableSearchField
import de.westnordost.streetcomplete.ui.common.SearchIcon

/** Searchable and clickable quest list as a full screen */
@Composable
fun ShowQuestFormsScreen(
    viewModel: ShowQuestFormsViewModel,
    onClickQuestType: (QuestType) -> Unit,
    onClickBack: () -> Unit,
) {
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()

    val filteredQuests by viewModel.filteredQuests.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ShowQuestFormsTopAppBar(
            onClickBack = onClickBack,
            search = searchText,
            onSearchChange = viewModel::updateSearchText,
        )

        if (filteredQuests.isEmpty()) {
            CenteredLargeTitleHint(stringResource(R.string.no_search_results))
        } else {
            val insets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ).asPaddingValues()
            QuestList(
                items = filteredQuests,
                onClickQuestType = onClickQuestType,
                contentPadding = insets,
                modifier = Modifier.consumeWindowInsets(insets)
            )
        }
    }
}

@Composable
private fun ShowQuestFormsTopAppBar(
    onClickBack: () -> Unit,
    search: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSearch by remember { mutableStateOf(false) }

    fun setShowSearch(value: Boolean) {
        showSearch = value
        if (!value) onSearchChange("")
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.primarySurface,
        elevation = AppBarDefaults.TopAppBarElevation,
    ) {
        Column {
            TopAppBar(
                title = { Text("Show Quest Forms") },
                windowInsets = AppBarDefaults.topAppBarWindowInsets,
                navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
                actions = { IconButton(onClick = { setShowSearch(!showSearch) }) { SearchIcon() } },
                elevation = 0.dp
            )
            ExpandableSearchField(
                expanded = showSearch,
                onDismiss = { setShowSearch(false) },
                search = search,
                onSearchChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = MaterialTheme.colors.surface
                ),
                keyboardOptions = KeyboardOptions(hintLocales = LocaleList.current),
            )
        }
    }
}

@Composable
private fun QuestList(
    items: List<QuestType>,
    onClickQuestType: (QuestType) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        itemsIndexed(items, key = { _, it -> it.name }) { index, item ->
            Column(Modifier.clickable { onClickQuestType(item) }) {
                if (index > 0) Divider()
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(item.icon),
                        contentDescription = item.name,
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        text = stringResource(item.title),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}
