package ai.yoofi.app.ui.me

import ai.yoofi.app.R
import ai.yoofi.app.domain.profile.MineProfilePresence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MineProfileStrategyTest {

    @Test
    fun `空态铅笔走完善且不挂复制`() {
        val strategy = MineProfilePresence.Vacant.strategy()
        var setup = false
        var edit = false
        strategy.onPencil(
            onEditProfile = { edit = true },
            onSetupProfile = { setup = true },
        )()
        assertEquals(R.string.cd_complete_profile, strategy.pencilCdRes)
        assertTrue(setup)
        assertFalse(edit)
        assertNull(strategy.wrapIdTrailing { })
    }

    @Test
    fun `主态铅笔走编辑且挂复制`() {
        val strategy = MineProfilePresence.Populated.strategy()
        var setup = false
        var edit = false
        strategy.onPencil(
            onEditProfile = { edit = true },
            onSetupProfile = { setup = true },
        )()
        assertEquals(R.string.cd_edit_profile, strategy.pencilCdRes)
        assertTrue(edit)
        assertFalse(setup)
        assertNotNull(strategy.wrapIdTrailing { })
    }
}
