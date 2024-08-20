@file:OptIn(ExperimentalCompilerApi::class)

package com.amazon.lastmile.kotlin.inject.anvil.processor

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.amazon.lastmile.kotlin.inject.anvil.LOOKUP_PACKAGE
import com.amazon.lastmile.kotlin.inject.anvil.compile
import com.amazon.lastmile.kotlin.inject.anvil.componentInterface
import com.amazon.lastmile.kotlin.inject.anvil.generatedComponent
import com.amazon.lastmile.kotlin.inject.anvil.inner
import com.amazon.lastmile.kotlin.inject.anvil.isAnnotatedWith
import com.amazon.lastmile.kotlin.inject.anvil.origin
import com.amazon.test.SingleInAppScope
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.COMPILATION_ERROR
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class ContributesToProcessorTest {

    @Test
    fun `a component interface is generated in the lookup package for a contributed component interface`() {
        compile(
            """
            package com.amazon.test
    
            import com.amazon.lastmile.kotlin.inject.anvil.ContributesTo

            @ContributesTo
            @SingleInAppScope
            interface ComponentInterface
            """,
        ) {
            val generatedComponent = componentInterface.generatedComponent

            assertThat(generatedComponent.packageName).isEqualTo(LOOKUP_PACKAGE)
            assertThat(generatedComponent.interfaces).containsExactly(componentInterface)
            assertThat(generatedComponent).isAnnotatedWith(SingleInAppScope::class)
            assertThat(generatedComponent.origin).isEqualTo(componentInterface)
        }
    }

    @Test
    fun `a component interface is generated in the lookup package for a contributed inner component interface`() {
        compile(
            """
            package com.amazon.test
    
            import com.amazon.lastmile.kotlin.inject.anvil.ContributesTo

            interface ComponentInterface {
                @ContributesTo
                @SingleInAppScope
                interface Inner
            }
            """,
        ) {
            val generatedComponent = componentInterface.inner.generatedComponent

            assertThat(generatedComponent.packageName).isEqualTo(LOOKUP_PACKAGE)
            assertThat(generatedComponent.interfaces).containsExactly(componentInterface.inner)
            assertThat(generatedComponent).isAnnotatedWith(SingleInAppScope::class)
            assertThat(generatedComponent.origin).isEqualTo(componentInterface.inner)
        }
    }

    @Test
    fun `a contributed component interface must be public`() {
        compile(
            """
            package com.amazon.test
    
            import com.amazon.lastmile.kotlin.inject.anvil.ContributesTo

            @ContributesTo
            @SingleInAppScope
            private interface ComponentInterface
            """,
            exitCode = COMPILATION_ERROR,
        ) {
            assertThat(messages).contains("Contributed component interfaces must be public.")
        }
    }

    @Test
    fun `only interfaces can be contributed`() {
        compile(
            """
            package com.amazon.test
    
            import com.amazon.lastmile.kotlin.inject.anvil.ContributesTo

            @ContributesTo
            @SingleInAppScope
            abstract class ComponentInterface
            """,
            exitCode = COMPILATION_ERROR,
        ) {
            assertThat(messages).contains("Only interfaces can be contributed.")
        }
    }

    @Test
    fun `a scope must be present`() {
        compile(
            """
            package com.amazon.test
    
            import com.amazon.lastmile.kotlin.inject.anvil.ContributesTo

            @ContributesTo
            interface ComponentInterface
            """,
            exitCode = COMPILATION_ERROR,
        ) {
            assertThat(messages).contains("Couldn't find scope annotation for ComponentInterface.")
        }
    }
}
