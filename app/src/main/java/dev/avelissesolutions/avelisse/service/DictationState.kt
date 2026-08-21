@file:Suppress("unused")

package dev.avelissesolutions.avelisse.service

/**
 * Type alias for backward compatibility.
 *
 * DictationState has been moved to the core module (dev.avelissesolutions.avelisse.core.service)
 * so both app and ime modules can reference it without circular dependencies.
 */
typealias DictationState = dev.avelissesolutions.avelisse.core.service.DictationState
