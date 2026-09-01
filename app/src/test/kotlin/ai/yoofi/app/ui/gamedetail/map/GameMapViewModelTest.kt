package ai.yoofi.app.ui.gamedetail.map

import ai.yoofi.app.domain.gamedetail.GetGameMapsUseCase
import ai.yoofi.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameMapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `初始化落在 Map 01 且列表关闭`() {
        val viewModel = viewModel()
        val state = viewModel.uiState.value
        assertEquals("map-01", state.currentMapId)
        assertEquals("Map 01", state.currentMap?.title)
        assertEquals(4, state.maps.size)
        assertFalse(state.listOpen)
        assertFalse(state.loading)
    }

    @Test
    fun `点芯片开合列表`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameMapIntent.ToggleList)
        assertTrue(viewModel.uiState.value.listOpen)
        viewModel.onIntent(GameMapIntent.ToggleList)
        assertFalse(viewModel.uiState.value.listOpen)
    }

    @Test
    fun `选当前地图只关列表不进 loading`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameMapIntent.ToggleList)
        viewModel.onIntent(GameMapIntent.SelectMap("map-01"))
        assertFalse(viewModel.uiState.value.listOpen)
        assertFalse(viewModel.uiState.value.loading)
        assertEquals("map-01", viewModel.uiState.value.currentMapId)
    }

    @Test
    fun `选其他地图走完假进度后切换`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(GameMapIntent.SelectMap("map-03"))
        assertTrue(viewModel.uiState.value.loading)
        assertEquals("map-01", viewModel.uiState.value.currentMapId)
        advanceTimeBy(MapSwitchDurationMs)
        runCurrent()
        assertFalse(viewModel.uiState.value.loading)
        assertEquals("map-03", viewModel.uiState.value.currentMapId)
        assertEquals(0, viewModel.uiState.value.loadingProgress)
    }

    @Test
    fun `点红钉弹出 Go 再点一次收起`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameMapIntent.SelectLocation("pin-1"))
        assertEquals("pin-1", viewModel.uiState.value.selectedLocationId)
        viewModel.onIntent(GameMapIntent.SelectLocation("pin-1"))
        assertEquals("", viewModel.uiState.value.selectedLocationId)
    }

    @Test
    fun `点 Go 发出聊天文案并清选中`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        val effects = mutableListOf<GameMapSideEffect>()
        val job = launch { viewModel.sideEffect.collect { effects += it } }
        viewModel.onIntent(GameMapIntent.SelectLocation("loc-3"))
        viewModel.onIntent(GameMapIntent.ConfirmGo)
        runCurrent()
        assertEquals("", viewModel.uiState.value.selectedLocationId)
        val go = effects.single() as GameMapSideEffect.GoToChat
        assertEquals("Go to location.", go.text)
        assertEquals("demo-scene", go.backgroundKey)
        job.cancel()
    }

    @Test
    fun `未选地点点 Go 不发副作用`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        val effects = mutableListOf<GameMapSideEffect>()
        val job = launch { viewModel.sideEffect.collect { effects += it } }
        viewModel.onIntent(GameMapIntent.ConfirmGo)
        runCurrent()
        assertTrue(effects.isEmpty())
        job.cancel()
    }

    @Test
    fun `取消切换保留原图`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(GameMapIntent.SelectMap("map-02"))
        assertTrue(viewModel.uiState.value.loading)
        viewModel.onIntent(GameMapIntent.CancelLoading)
        assertFalse(viewModel.uiState.value.loading)
        assertEquals("map-01", viewModel.uiState.value.currentMapId)
        advanceTimeBy(MapSwitchDurationMs)
        runCurrent()
        assertEquals("map-01", viewModel.uiState.value.currentMapId)
    }

    private fun viewModel(): GameMapViewModel = GameMapViewModel(GetGameMapsUseCase())
}
