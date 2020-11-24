import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.components.*
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.values
import dev.fritz2.styling.StyleClass
import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.theme.Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun RenderContext.inputDemo(): Div {

    val user = storeOf("Jon Snoe")

    return stackUp({
        alignItems { start }
        padding { "1rem" }
    }) {
        items {
            h1 { +"Inputs Showcase" }

            h3 { +"A basic Input needs no Store" }
            inputField {
                content {
                    placeholder("Placeholder")
                }
            }

            h3 { +"A disabled component is skipped by the TAB key, but readonly isn't." }
            lineUp {
                items {
                    inputField {
                        content {
                            value("disabled")
                            disabled(true)
                        }
                    }
                    inputField({
                        focus {
                            border {
                                color { dark }
                            }
                            boxShadow { none }
                        }
                    }) {
                        content {
                            value("readonly")
                            readOnly(true)
                        }
                    }
                }
            }

            h3 { +"Password" }
            inputField {
                content {
                    type("password")
                    placeholder("Password")
                }
            }

            h3 { +"Inputs with store connect events automatically." }
            inputField(store = user) {
                content {
                    placeholder("Name")
                }
            }

            h3 { +"Inputs without stores need manual event collection." }
            inputField {
                content {
                    placeholder("Name")
                    changes.values() handledBy user.update
                }
            }

            (::p.styled {
                background { color { light } }
                fontWeight { bold }
                radius { "5%" }
                paddings {
                    left { "0.3rem" }
                    right { "0.3rem" }
                }
            }) {
                +"Name in Store: "
                user.data.asText()
            }

            h3 { +"Sizes" }
            lineUp {
                items {
                    inputField {
                        size { large }
                        content {
                            placeholder("large")
                        }
                    }
                    inputField {
                        size { normal }
                        content {
                            placeholder("normal")
                        }
                    }
                    inputField {
                        size { small }
                        content {
                            placeholder("small")
                        }
                    }
                }
            }

            h3 { +"Variants" }
            lineUp {
                items {
                    inputField {
                        variant { outline }
                        content {
                            placeholder("outline")
                        }
                    }
                    inputField {
                        variant { filled }
                        content {
                            placeholder("filled")
                        }
                    }
                }
            }

            h2 { +"Input fields go to town" }

            val ourInputStyle: BasicParams.() -> Unit = {
                Theme().input.sizes.large()
                Theme().input.variants.filled()
                border {
                    color { warning }
                    width { "3px" }
                    style { double }
                }
                background {
                    color { dark }
                }
                radius { "1rem" }
                color { light }

                focus {
                    background {
                        color { light }
                    }
                    color { warning }
                }
            }

            // Extend base component
            fun RenderContext.ourInputField(
                styling: BasicParams.() -> Unit = {},
                store: Store<String>? = null,
                baseClass: StyleClass? = null,
                id: String? = null,
                prefix: String = "ourInputField",
                init: InputFieldComponent.() -> Unit
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
                items {
                    // use our component instead of built-in one!
                    ourInputField {
                        content {
                            type("text")
                            placeholder("user")
                        }
                    }
                    ourInputField({
                        // Passwords are dangerous -> so style ad hoc!!!
                        border {
                            color { danger }
                        }
                    }) {
                        content {
                            type("password")
                            placeholder("password")
                        }
                    }
                }
            }
            br {}
        }
    }
}
