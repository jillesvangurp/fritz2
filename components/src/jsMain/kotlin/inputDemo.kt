import dev.fritz2.binding.Store
import dev.fritz2.binding.const
import dev.fritz2.binding.handledBy
import dev.fritz2.binding.storeOf
import dev.fritz2.components.flexBox
import dev.fritz2.components.inputField
import dev.fritz2.components.lineUp
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.values
import dev.fritz2.styling.StyleClass
import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.theme.theme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun HtmlElements.inputDemo(): Div {

    val user = storeOf("John Doe")

    return div {
        flexBox({
            direction { column }
            padding { normal }
        }) {
            h1 { +"Input Showcase" }

            //text.apply { +"Basic" }
            inputField {
                //type = const("text")
                placeholder = const("Placeholder")
            }

            //f2Text().apply { +"Basic + Readonly + Custom Styling" }
            inputField({
                //background { color { "lightgrey" } }
                focus {
                    border {
                        color { dark }
                    }
                    boxShadow { none }
                }
            }) {
                type = const("text")
                value = const("Readonly!")
                readOnly = const(true)
            }


            //f2Text().apply { +"Password" }
            inputField {
                type = const("password")
                placeholder = const("Password")
            }

            //f2Text().apply { +"Basic + Store" }
            inputField(store = user) {
                placeholder = const("Name")
            }
            //f2Text().apply { +"changes manually applied to store's update" }
            inputField {
                placeholder = const("Name")
                changes.values() handledBy user.update
            }
            lineUp({
                margins { vertical { tiny } }
            }) {
                spacing { tiny }
                //f2Text().apply { +"given Name:" }
                /*
                f2Text {
                    background { color { "lightgrey" } }
                    radius { normal }
                    paddings { horizontal { tiny } }
                }.apply {
                    user.data.bind()
                }
                */
            }

            //f2Text().apply { +"Sizes" }
            inputField({ theme().input.large() }) {
                placeholder = const("large")
            }
            inputField({ theme().input.normal() }) {
                placeholder = const("normal")
            }
            inputField({ theme().input.small() }) {
                placeholder = const("small")
            }

            //f2Text().apply { +"Variants" }
            inputField({ theme().input.outline() }) {
                placeholder = const("outline")
            }
            inputField({ theme().input.filled() }) {
                placeholder = const("filled")
            }

            // Put it all together:

            // in real life, put it into your theme!
            val ourInputStyle: BasicParams.() -> Unit = {
                theme().input.large()
                theme().input.filled()
                border {
                    color { "lime" }
                    width { "3px" }
                    style { double }
                }
                background {
                    color { dark }
                }
                radius { "1rem" }
                color { "pink" }

                focus {
                    background {
                        color { light }
                    }
                    color { "purple" }
                }
            }

            // Extend base component
            fun HtmlElements.ourInputField(
                styling: BasicParams.() -> Unit = {},
                store: Store<String>? = null,
                baseClass: StyleClass? = null,
                id: String? = null,
                prefix: String = "ourInputField",
                init: Input.() -> Unit
            ) {
                inputField({
                    // always use corporate styling automatically!
                    ourInputStyle()
                    // still apply call-side defined styling!
                    styling()
                }, store, baseClass, id, prefix, init)
            }

            lineUp {
                spacing { tiny }
                children {

                    ourInputField {
                        type = const("text")
                        placeholder = const("user")
                    }
                    ourInputField({
                        // Passwords are dangerous -> so style ad hoc!!!
                        border {
                            color { danger }
                        }
                    }) {
                        type = const("password")
                        placeholder = const("password")
                    }
                }
            }
        }
    }
}
