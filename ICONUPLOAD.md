Icon/Image instructions

Add a tiny Gradle task to your app module’s build.gradle.kts. We’re storing images in a top‑level assets/ folder in your EzEats repo (so that e.g. assets/logo.png, assets/ic_search.png, etc. resolve to valid raw URLs):
kotlin

// app/build.gradle.kts

plugins {
  id("com.android.application")
  kotlin("android")
}

android {
  // … your existing android { … } block …
}

// ----------------------------------------------------------------------------
// ↓ add this to the bottom of your build.gradle.kts ↓
// ----------------------------------------------------------------------------

/**
 * Downloads EzEats logo + icons from GitHub into src/main/res/drawable before each build.
 */
val downloadIcons by tasks.registering {
  group = "setup"
  description = "Fetch logo and UI icons from GitHub raw URLs into res/drawable"
  doLast {
    // Map of target‑file → raw GitHub URL
    val assets = mapOf(
      "logo.png"         to "https://raw.githubusercontent.com/YuRoGameDev/EzEats/main/assets/logo.png",
      "ic_search.png"    to "https://raw.githubusercontent.com/YuRoGameDev/EzEats/main/assets/ic_search.png",
      "ic_home.png"      to "https://raw.githubusercontent.com/YuRoGameDev/EzEats/main/assets/ic_home.png",
      "ic_profile.png"   to "https://raw.githubusercontent.com/YuRoGameDev/EzEats/main/assets/ic_profile.png",
      "ic_bookmark.png"  to "https://raw.githubusercontent.com/YuRoGameDev/EzEats/main/assets/ic_bookmark.png"
    )

    assets.forEach { (fileName, url) ->
      ant.withGroovyBuilder {
        "get"(
          mapOf(
            "src"   to url,
            "dest"  to "$projectDir/src/main/res/drawable/$fileName",
            "verbose" to "true"
          )
        )
      }
    }
  }
}

// Make sure we fetch icons before we compile any Android code
tasks.named("preBuild") {
  dependsOn(downloadIcons)
}
How it works
Gradle task
downloadIcons loops over a map of file‑names to raw GitHub URLs and uses Ant’s <get> to pull each one down into your module’s res/drawable/ directory.
Hook into the build
By making preBuild depend on downloadIcons, you guarantee the PNGs are in place before Android’s resource merger runs.
Next steps
Commit your .png (or .svg converted to .png) files into an assets/ folder at the root of your EzEats repo.
Name them exactly logo.png, ic_search.png, etc. (or adjust the map above).
When you run a Gradle build (or ./gradlew assembleDebug), those images get pulled in automatically.
Now you can reference them in your layouts or code as usual:
xml

<ImageView
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:src="@drawable/ic_search" />
and in Kotlin:
kotlin

logoImageView.setImageResource(R.drawable.logo)
