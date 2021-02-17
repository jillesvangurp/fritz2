package dev.fritz2.components

import org.w3c.dom.*
import dev.fritz2.dom.Listener
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.html.EventType
import dev.fritz2.dom.html.Events
import org.w3c.dom.events.MouseEvent
import dev.fritz2.components.validation.Severity
import dev.fritz2.dom.WithEvents
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.StyleClass
import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.params.BoxParams
import dev.fritz2.styling.params.Style
import dev.fritz2.styling.staticStyle
import dev.fritz2.styling.theme.SeverityStyles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.w3c.dom.Element
import dev.fritz2.identification.uniqueId

/**
 * A marker to separate the layers of calls in the type-safe-builder pattern.
 */
@DslMarker
annotation class ComponentMarker


class ComponentProperty<T>(var value: T) {
    operator fun invoke(newValue: T) {
        value = newValue
    }
}

class DynamicComponentProperty<T>(var values: Flow<T>) {
    operator fun invoke(newValue: T) {
        values = flowOf(newValue)
    }

    operator fun invoke(newValues: Flow<T>) {
        values = newValues
    }
}

class NullableDynamicComponentProperty<T>(var values: Flow<T?>) {
    operator fun invoke(newValue: T?) {
        values = flowOf(newValue)
    }

    operator fun invoke(newValues: Flow<T?>) {
        values = newValues
    }
}

/**
 * Marker interface that *every* component should implement, so that the central render method appears in a unified way
 * throughout this framework.
 *
 * The render method has to be implemented in order to do the actual rendering of one component.
 * This reduces the boilerplate code within the corresponding factory function(s):
 * ```
 * open MyComponent: Component {
 *      override fun render(...) {
 *          // some content rendering
 *      }
 * }
 *
 * RenderContext.myComponent(
 *      // most params omitted
 *      build: MyComponent.() -> Unit = {}
 * ) {
 *      MyComponent().apply(build).render(this, /* params */)
 *      //                         ^^^^^^
 *      //                         just start the rendering by one additional call!
 * }
 * ```
 *
 * Often a component needs *additional* parameters that are passed into the factory functions (remember that those
 * should be located starting after the ``styling`` parameter in first position and before the ``baseClass`` parameter)
 * Typical use cases are [Store]s or list of items, as for [RenderContext.checkboxGroup] for example.
 * Those additional parameters should be passed via contructor injection into the component class:
 * ```
 * open MyComponent(protected val items: List<String>, protected val store: Store<String>?): Component {
 *      override fun render(...) {
 *          // some content rendering with access to the ``items`` and the ``store``
 *      }
 * }
 *
 * RenderContext.myComponent(
 *      styling: BasicParams.() -> Unit,
 *      items: List<String>,          // two additional parameters
 *      store: Store<String>? = null, // after ``styling`` and before ``baseClass``!
 *      baseClass: StyleClass?,
 *      id: String?,
 *      prefix: String
 *      build: MyComponent.() -> Unit = {}
 * ) {
 *      MyComponent(items, store) // inject additional parameters
 *          .apply(build)
 *          .render(this, styling, baseClass, id, prefix) // pass context + regular parameters
 * }
 * ```
 */
interface Component<T> {

    /**
     * Central method that should do the actual rendering of a component.
     *
     * Consider to declare your implementation as ``open`` in order to allow the customization of a component.
     *
     * @param context the receiver to render the component into
     * @param styling a lambda expression for declaring the styling as fritz2's styling DSL
     * @param baseClass optional CSS class that should be applied to the element
     * @param id the ID of the element
     * @param prefix the prefix for the generated CSS class resulting in the form ``$prefix-$hash``
     */
    fun render(
        context: RenderContext,
        styling: BoxParams.() -> Unit,
        baseClass: StyleClass?,
        id: String?,
        prefix: String
    ): T
}


interface ElementProperties<T> {
    val element: ComponentProperty<T.() -> Unit>
}

// TODO: Constraint für Typ: T : Tag<E> ?
class ElementMixin<T> : ElementProperties<T> {
    override val element: ComponentProperty<T.() -> Unit> = ComponentProperty {}
}


interface EventProperties<T : Element> {
    val events: ComponentProperty<WithEvents<T>.() -> Unit>
}

class EventMixin<T : Element> : EventProperties<T> {
    override val events: ComponentProperty<WithEvents<T>.() -> Unit> = ComponentProperty {}
}


interface FormProperties {
    val disabled: DynamicComponentProperty<Boolean>

    fun enabled(value: Flow<Boolean>) {
        disabled(value.map { !it })
    }

    fun enabled(value: Boolean) {
        enabled(flowOf(value))
    }
}

open class FormMixin : FormProperties {
    override val disabled = DynamicComponentProperty(flowOf(false))
}

interface InputFormProperties : FormProperties {
    val readonly: DynamicComponentProperty<Boolean>
}

class InputFormMixin : InputFormProperties, FormMixin() {
    override val readonly = DynamicComponentProperty(flowOf(false))
}

interface SeverityProperties {
    val severity: NullableDynamicComponentProperty<Severity?>

    class SeverityContext {
        val info: Severity = Severity.Info
        val success: Severity = Severity.Success
        val warning: Severity = Severity.Warning
        val error: Severity = Severity.Error
    }

    fun severity(value: SeverityContext.() -> Severity) {
        severity(value(SeverityContext()))
    }

    fun severityClassOf(
        severityStyle: SeverityStyles,
        prefix: String
    ): Flow<StyleClass> =
        severity.values.map {
            when (it) {
                Severity.Info -> staticStyle("${prefix}-severity-info", severityStyle.info)
                Severity.Success -> staticStyle("${prefix}-severity-success", severityStyle.success)
                Severity.Warning -> staticStyle("${prefix}-severity-warning", severityStyle.warning)
                Severity.Error -> staticStyle("${prefix}-severity-error", severityStyle.error)
                else -> StyleClass.None
            }
        }
}

class SeverityMixin : SeverityProperties {
    override val severity = NullableDynamicComponentProperty<Severity?>(emptyFlow())
}

interface TextInputFormProperties : InputFormProperties {
    // TODO Some further properties are equal between input type=text and textarea; could be worth to centralize!
}

class MultiSelectionStore<T> : RootStore<List<T>>(emptyList()) {
    val toggle = handleAndEmit<T, List<T>> { selectedRows, new ->
        val newSelection = if (selectedRows.contains(new))
            selectedRows - new
        else
            selectedRows + new
        emit(newSelection)
        newSelection
    }
}

class SingleSelectionStore : RootStore<Int?>(null) {
    val toggle = handleAndEmit<Int, Int> { _, new ->
        emit(new)
        new
    }
}

interface CloseButtonProperty {
    val prefix: String
    val closeButtonStyle: ComponentProperty<Style<BasicParams>>
    val hasCloseButton: ComponentProperty<Boolean>
    val closeButtonRendering: ComponentProperty<RenderContext.() -> Listener<MouseEvent, HTMLElement>>
}

class CloseButtonMixin(
    override val prefix: String = "close-button",
    override val closeButtonStyle: ComponentProperty<Style<BasicParams>>
) : CloseButtonProperty {
    override val hasCloseButton = ComponentProperty(true)
    override val closeButtonRendering = ComponentProperty<RenderContext.() -> Listener<MouseEvent, HTMLElement>> {
        clickButton({
            closeButtonStyle.value()
        }, id = "close-button-${uniqueId()}", prefix = prefix) {
            variant { ghost }
            icon { fromTheme { close } }
        }
    }
}
