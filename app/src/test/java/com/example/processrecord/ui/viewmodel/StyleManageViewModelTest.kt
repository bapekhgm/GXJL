package com.example.processrecord.ui.viewmodel

import com.example.processrecord.data.StyleRepository
import com.example.processrecord.data.dao.StyleDao
import com.example.processrecord.data.entity.Style
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class StyleManageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addStyle_returnsEmpty_whenNameIsBlank() {
        val viewModel = createViewModel().first
        var result: StyleManageViewModel.AddStyleResult? = null

        viewModel.addStyle("   ") { result = it }

        assertEquals(StyleManageViewModel.AddStyleResult.EmptyName, result)
    }

    @Test
    fun addStyle_returnsAdded_whenInsertSucceeds() {
        val (viewModel, _) = createViewModel()
        var result: StyleManageViewModel.AddStyleResult? = null

        viewModel.addStyle("  S-001  ") { result = it }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(StyleManageViewModel.AddStyleResult.Added("S-001"), result)
    }

    @Test
    fun addStyle_returnsDuplicate_whenNameAlreadyExists() {
        val existing = Style(id = 1, name = "S-001")
        val (viewModel, _) = createViewModel(initialStyles = listOf(existing))
        var result: StyleManageViewModel.AddStyleResult? = null

        viewModel.addStyle("  s-001  ") { result = it }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(StyleManageViewModel.AddStyleResult.Duplicate("s-001"), result)
    }

    @Test
    fun addStyle_returnsDuplicate_whenInsertIgnoredByConflict() {
        val (viewModel, _) = createViewModel(
            forceConflictOnInsert = setOf("S-001")
        )
        var result: StyleManageViewModel.AddStyleResult? = null

        viewModel.addStyle("S-001") { result = it }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(StyleManageViewModel.AddStyleResult.Duplicate("S-001"), result)
    }

    @Test
    fun batchAddStyles_countsAddedAndSkipped() {
        val existing = Style(id = 1, name = "A")
        val (viewModel, _) = createViewModel(initialStyles = listOf(existing))
        var added = -1
        var skipped = -1

        viewModel.batchAddStyles("a, B, b, C") { a, s ->
            added = a
            skipped = s
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, added)
        assertEquals(1, skipped)
    }

    @Test
    fun splitStyleNames_removesCaseInsensitiveDuplicates() {
        val output = splitStyleNames("A, a, A1, a1, b")

        assertEquals(listOf("A", "A1", "b"), output)
    }

    @Test
    fun batchAddStyles_returnsZero_whenInputEmpty() {
        val (viewModel, _) = createViewModel()
        var added = -1
        var skipped = -1

        viewModel.batchAddStyles(" , \n\t  ") { a, s ->
            added = a
            skipped = s
        }

        assertEquals(0, added)
        assertEquals(0, skipped)
    }

    private fun createViewModel(
        initialStyles: List<Style> = emptyList(),
        forceConflictOnInsert: Set<String> = emptySet()
    ): Pair<StyleManageViewModel, FakeStyleDaoForStyleManageTest> {
        val dao = FakeStyleDaoForStyleManageTest(initialStyles, forceConflictOnInsert)
        val repository = StyleRepository(dao)
        return StyleManageViewModel(repository) to dao
    }
}

private class FakeStyleDaoForStyleManageTest(
    initialStyles: List<Style>,
    private val forceConflictOnInsert: Set<String>
) : StyleDao {
    private val stylesFlow = MutableStateFlow(initialStyles)
    private var nextId = (initialStyles.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun getAllStyles(): Flow<List<Style>> = stylesFlow

    override suspend fun insertStyle(style: Style): Long {
        if (style.name in forceConflictOnInsert) return -1L
        if (stylesFlow.value.any {
                it.name.trim().equals(style.name.trim(), ignoreCase = true)
            }
        ) return -1L
        val inserted = style.copy(id = nextId++)
        stylesFlow.value = stylesFlow.value + inserted
        return inserted.id
    }

    override suspend fun deleteStyle(style: Style) {
        stylesFlow.value = stylesFlow.value.filterNot { it.id == style.id }
    }

    override suspend fun deleteStyleByName(name: String) {
        stylesFlow.value = stylesFlow.value.filterNot {
            it.name.trim().equals(name.trim(), ignoreCase = true)
        }
    }

    override suspend fun getStyleByName(name: String): Style? {
        return stylesFlow.value.firstOrNull {
            it.name.trim().equals(name.trim(), ignoreCase = true)
        }
    }
}
