import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.findByType
import com.android.build.api.dsl.ApplicationExtension as AndroidApplicationExtension
import com.android.build.api.dsl.CommonExtension as AndroidCommonExtension
import com.android.build.api.dsl.LibraryExtension as AndroidLibraryExtension

internal fun ExtensionContainer.findAndroidExtension(): AndroidCommonExtension<*, *, *, *, *, *>? =
    null
        ?: findByType<AndroidLibraryExtension>()
        ?: findByType<AndroidApplicationExtension>()
        // may also be Dynamic Feature extension, but there are none of such in the project atm