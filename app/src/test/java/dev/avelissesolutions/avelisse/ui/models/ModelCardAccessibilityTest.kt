package dev.avelissesolutions.avelisse.ui.models

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import dev.avelissesolutions.avelisse.core.theme.AvelisseTheme
import dev.avelissesolutions.avelisse.model.ModelCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Whether a model card can be operated without being able to swipe.
 *
 * Deleting a model is a horizontal drag, which is a gesture some of the people this app
 * is for cannot perform at all. The card offers the same thing as a named action, so it
 * can be reached from the screen reader's menu instead.
 */
@RunWith(RobolectricTestRunner::class)
class ModelCardAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val model = ModelCatalog.findByKey(ModelCatalog.DEFAULT_KEY)!!

    private fun show(canDelete: Boolean) {
        composeTestRule.setContent {
            AvelisseTheme {
                ModelCard(
                    model = model,
                    isDownloaded = true,
                    isActive = false,
                    downloadProgress = null,
                    canDelete = canDelete,
                    onDownload = {},
                    onDelete = {},
                    onRetry = {},
                    onSelect = {},
                )
            }
        }
    }

    /** The rendered semantics tree, which lists any custom actions by name. */
    private fun tree(): String = composeTestRule.onRoot(useUnmergedTree = false).printToString()

    @Test
    fun `a deletable model offers delete as an action`() {
        show(canDelete = true)
        val rendered = tree()

        assertTrue("no custom actions on the card: " + rendered, rendered.contains("CustomActions"))
        assertTrue("the action is not delete: " + rendered, rendered.contains("Delete", ignoreCase = true))
    }

    @Test
    fun `the model in use offers no delete action`() {
        show(canDelete = false)

        // Not a gap: deleting the model currently in use is refused everywhere else too,
        // so offering it here would be offering something that would then be declined.
        assertFalse(tree().contains("CustomActions"))
    }

    @Test
    fun `the card announces itself as a button`() {
        show(canDelete = true)

        assertTrue("card is not a button: " + tree(), tree().contains("Role = 'Button'"))
    }
}
