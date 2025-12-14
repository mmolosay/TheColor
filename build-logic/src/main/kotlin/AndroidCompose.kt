import org.gradle.api.Project
import com.android.build.api.dsl.CommonExtension as AndroidCommonExtension

@Suppress("UnusedReceiverParameter")
internal fun Project.configureAndroidCompose(
    androidExtension: AndroidCommonExtension<*, *, *, *, *, *>,
) {
    androidExtension.apply {
        buildFeatures {
            compose = true
        }
    }
}