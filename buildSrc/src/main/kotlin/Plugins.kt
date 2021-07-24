object Plugins {
    object Ktlint {
        const val version = "10.0.0"
        const val id = "org.jlleitschuh.gradle.ktlint"
    }

    object Kotlin {
        const val gradle =
            "org.jetbrains.kotlin:kotlin-gradle-plugin:${Libs.Kotlin.version}"
    }

    object Android {
        private const val version = "7.0.0-rc01"

        const val gradle = "com.android.tools.build:gradle:$version"
    }
}