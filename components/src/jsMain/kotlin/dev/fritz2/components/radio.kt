package dev.fritz2.components

import dev.fritz2.components.RadioComponent.Companion.radioLabel
import dev.fritz2.dom.WithEvents
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.StyleClass
import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.params.Style
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.staticStyle
import dev.fritz2.styling.theme.IconDefinition
import dev.fritz2.styling.theme.RadioSizes
import dev.fritz2.styling.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLInputElement

class RadioComponent {
    companion object {
        val radioInputStaticCss = staticStyle(
            "radioInput",
            """
            position: absolute;
            height: 1px; 
            width: 1px;
            overflow: hidden;
            clip: rect(1px 1px 1px 1px); /* IE6, IE7 */
            clip: rect(1px, 1px, 1px, 1px);
            outline: none;
            &:focus{
                outline: none;
            }
            &:focus + label::before {
                box-shadow: 0 0 1px ${Theme().colors.dark};
            }
            &:disabled + label {
                color: ${Theme().colors.disabled};
                cursor: not-allowed;
            }
            &:disabled + label::before {
                opacity: 0.3;
                cursor: not-allowed;
                boxShadow: none;
                color: ${Theme().colors.disabled};
            }
            """
        )

        val radioLabelStaticCss = staticStyle(
            "radiolabel",
            """
            display: block;
            position: relative;
            &::before {
                content: '';
                outline: none;
                position: relative;
                display: inline-block;
                vertical-align: middle;
                box-shadow: 0 0 1px ${Theme().colors.dark} inset;
            }
            """
        )
        val radioLabel = staticStyle("radioComponent", """
            &[data-disabled] {
                opacity: .5    
            }
        """)
        val radioIconStaticCss = staticStyle("radioIcon",
            """
            &[data-disabled] {
                background-color:var(--cb-disabled) !important;
            }
        """
        )
    }

    var size: RadioSizes.() -> Style<BasicParams> = { Theme().radio.sizes.normal }
    fun size(value: RadioSizes.() -> Style<BasicParams>) {
        size = value
    }

    var icon: IconDefinition? = null
    fun icon(value: () -> IconDefinition) {
        icon = value()
    }

    var label: (Div.() -> Unit)? = null
    fun label(value: String) {
        label = {
            +value
        }
    }
    fun label(value: Flow<String>) {
        label = {
            value.asText()
        }
    }
    fun label(value: (Div.() -> Unit)) {
        label = value
    }
    var labelStyle: Style<BasicParams> = { Theme().radio.label() }
    fun labelStyle(value: () -> Style<BasicParams>) {
        labelStyle = value()
    }

    var selectedStyle: Style<BasicParams> = { Theme().radio.selected() }
    fun selectedStyle(value: () -> Style<BasicParams>) {
        selectedStyle = value()
    }

    var events: (WithEvents<HTMLInputElement>.() -> Unit)? = null // @input
    fun events(value: WithEvents<HTMLInputElement>.() -> Unit) {
        events = value
    }

    var selected: Flow<Boolean> = flowOf(false) // @input
    fun selected(value: () -> Flow<Boolean>) {
        selected = value()
    }

    var disabled: Flow<Boolean> = flowOf(false) // @input
    fun disabled(value: () -> Flow<Boolean>) {
        disabled = value()
    }

    var groupName: Flow<String> = flowOf("")
    fun groupName(value: () -> String) {
        groupName = flowOf(value())
    }
    fun groupName(value: () -> Flow<String>) {
        groupName = value()
    }


}

fun RenderContext.radio(
    styling: BasicParams.() -> Unit = {},
    baseClass: StyleClass? = null,
    id: String? = null,
    prefix: String = "radioComponent",
    build: RadioComponent.() -> Unit = {}
): Label {
    val component = RadioComponent().apply(build)
    val inputId = id?.let { "$it-input" }
    val alternativeGroupname = id?.let { "$it-groupName" }
    val inputName = component.groupName.map {
        if(it.isEmpty()) {
            alternativeGroupname ?: ""
        } else {
            it
        }
    }
    val labelClass = if( baseClass == null ) {
        radioLabel
    } else {
        baseClass + radioLabel
    }

    return (::label.styled(
        baseClass = labelClass,
        id = id,
        prefix = prefix
    ) {
        component.size.invoke(Theme().radio.sizes)()
    }) {
        inputId?.let {
            `for`(inputId)
        }
        attr("data-disabled", component.disabled)
        (::input.styled(
            baseClass = RadioComponent.radioInputStaticCss,
            prefix = prefix,
            id = inputId
        ){ children("&:focus + div") {
            border {
                color { "#3182ce" }
            }
            boxShadow { outline }
        }}) {
            type("radio")
            name(inputName)
            checked(component.selected)
            disabled(component.disabled)
            value("X")
            component.events?.invoke(this)
        }

        component.selected.render { selected ->
            (::div.styled(){
                Theme().radio.default()
                styling()
                if( selected ) {
                    component.selectedStyle()
                }
            }) {
                attr("data-disabled", component.disabled)
            }
        }
        component.label?.let {
            (::div.styled() {
                component.labelStyle()
            }){
                it(this)
            }
        }
    }
}
