![image](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![image](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)

# TheColor
Cutting-edge native Android app about each and every known color.

<div style="display: flex; align-items: flex-start;">
 <img src="https://github.com/user-attachments/assets/a8caba45-7b72-484b-a68c-eed6ce9dab11" width="200" alt="Screenshot 01-01-2025" />
 <img src="https://github.com/user-attachments/assets/f6166eb8-eda5-445e-9077-06fcda13ffcc" width="200" alt="Screenshot 01-01-2025" />
 <img src="https://github.com/user-attachments/assets/1f84bfc5-7e62-4f69-8a9e-75629b311157" width="200" alt="Screenshot 01-01-2025" />
 <img src="https://github.com/user-attachments/assets/c625e8ca-a294-4e24-9c75-c5cb6b73231c" width="200" alt="Screenshot 01-01-2025" />
 <img src="https://github.com/user-attachments/assets/feff3c7c-172e-4410-a55c-1caf9284ba81" width="200" alt="Screenshot 01-01-2025" />
</div>

## Table of Contents

* [What is it](#what-is-it)
* [Who is it for](#who-is-it-for)
* [Development stack](#development-stack)
* [App features](#app-features)
* [Software approaches](#software-approaches)

-----

## What is it

This is a [native](https://en.wikipedia.org/wiki/Mobile_app#Types:~:text=and%20hybrid%20apps.-,Native%20app,is%20to%20ensure%20best%20performance%20for%20a%20specific%20mobile%20operating%20system.,-Web%2Dbased%20app) Android application about colors.
You can search for a specific color or generate a random one.
Explore detailed color information and create custom color schemes with the color scheme builder.

The app is powered by [The Color API](https://github.com/joshbeckman/thecolorapi).
For a full list of features, check out the [App Features](#app-features) section.

## Who is it for

This project showcases my expertise in developing software in general and
native Android apps in specific.
Originally, it served as a playground, a safe environment to try new things in Android world.
Over the course of years, it evolved into a polished, comprehensive app that I'm proud to share.

I would be flattered if other developers find inspiration in this software, its approaches and 
solutions.
It has open-source code and full commit history, so anyone can trace my decisions and the app's development journey.

## Development stack

- [Kotlin 1.9.x](https://kotlinlang.org/)
- [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- Kotlin [Flows and coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- DI: [Dagger 2](https://dagger.dev) & [Hilt](https://dagger.dev/hilt/)
- Local data: [Room](https://developer.android.com/training/data-storage/room), [Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- Remote data: [Retrofit 2](https://github.com/square/retrofit?tab=readme-ov-file), [Moshi](https://github.com/square/moshi)
- UI: [Jetpack Compose](https://developer.android.com/compose), [Compose Navigation](https://developer.android.com/develop/ui/compose/navigation), [Material 3](https://m3.material.io/)
- Unit testing: [JUnit 4](https://junit.org/junit4/), [MockK](https://mockk.io/), [Kotest Assertions](https://kotest.io/)
- Also: [Debounce](https://github.com/mmolosay/debounce), [Timber](https://github.com/JakeWharton/timber), [Colormath](https://github.com/ajalt/colormath), [Compose Shimmer](https://github.com/valentinilk/compose-shimmer)

## App Features

### Home
The main screen of the app. Displayed after the Splash screen.
Contains *Color Input*, *Color Preview* and *Color Center* features (views).  They are always in sync:

 - entering a valid color in *Color Input* will show *Color Preview* for it.
 - changing or clearing *Color Input* will change / hide *Color Preview*.
 - generating a new random color will populate it into *Color Input*.
 - selecting "go to exact color" or "go to initial color" in *Color Details* will populate *Color Input* with the corresponding color.

Allows to go to the *Settings* screen.
Allows to proceed with the current color. This will fetch data regarding the color and display *Color Center*.
Allows to generate a random color.

Handles keyboard "done" button click of *Color Input*. 
When there's a valid color, the app will proceed with it and a software keyboard (if any) will be hidden. If the color is not valid, then a message will be shown and a software keyboard will remain visible, allowing user to correct the input.

*Color Center* is displayed when a color is proceeded with, hidden when color is changed (but not proceeded with yet) or cleared.
*Color Center* is fully recreated when a color is proceeded with. 
This means that any user input that happened for previous color will be reset to default values (e.g. color scheme mode in *Color Scheme*).

Selecting a swatch in *Color Scheme* displays *Color Details* for this color.

If *"resume from last searched color"* feature from *Settings* is enabled, then the last searched (proceeded with) color will be proceeded with on app startup.

If *"auto proceed with randomized colors"* feature from *Settings* is enabled, then generating a new random color will instantly proceed with it.

### Color Input
Allows user to enter a color in a chosen color space.
Consists of a view for user input and a list of color spaces to choose from.

All color input types are synchronized. 
If user enters a valid color through color input of color space X, then when switching to color space Y they will see the same color. If user clears input in any color input type, all color inputs are cleared.

User can change a preferred type of color input on the *Settings* screen. The chosen type will become the first one in the list of available color input types. The chosen type will be selected on app launch.

Supports *"smart backspace"* and *"select all text on focus"* features from *Settings*.

### Color Preview
A small view that visually displays a color.

### Color Center
A group of two other feature: *Color Details* and *Color Scheme*.

### Color Details
Presents information about a color: 
 - color name
 - translations to different color spaces
 - whether the color is an exact match or not
 - color of the exact match
 - deviation from the exact match

Allows to go to the exact match and view its info.
Allows to go back to the initial color after that.

### Color Scheme
Consists of two parts: color scheme with swatches and color scheme builder. 
List of the swatches has the current color (one that was proceeded with) as a seed. Other parameters of color scheme are configured from the builder.

Allows to choose the mode of the color scheme: monochrome, analogic, etc.
Allows to choose number of swatches to have in the color scheme.

### Settings
Contains items with values. Changing values will change the behaviour of the parts of the app those items relate to.
Allows user to reset their preferences back to default values.

Settings items are listed below.

#### Preferred type of color input
Allows to select a type of color input to be selected by default (on app startup) in *Color Input*. This type will also be the first in the list of available input types.

#### UI theme
A color scheme of the application. The options are:

 - Auto. Resolves into either Light or Dark depending on the device's Dark mode setting.
 - Light, always, irregarding of device's Dark mode setting.
 - Dark, always, irregarding of device's Dark mode setting.
 - Jungle. Always light, irregarding of device's Dark mode setting.
 - Midnight. Always dark, irregarding of device's Dark mode setting.

### Dynamic colors for UI theme
Computes color schemes of some UI themes based on user's device wallpaper in the launcher.
Only works with following UI themes: Auto, Light, Dark.

#### Resume from last searched color
On app startup, the *Home* screen will remember the last color that was proceeded with in the previous app session. This color will be automatically proceeded with.

#### Smart backspace
Go to previous text field when clicking "backspace" in an empty text field. 
Enables easier editing in text fields of *Color Input*. On Android, there's a "next" button on software keyboard. 
It allows to go to the next text field in order to not waste time reaching to it (next text field) manually. This feature allows to go to the previous text field by clicking "backspace" when the text field is already empty.

#### Select all text on focus
Select all text in a text field when it gets focused (e.g. when user clicks it). 
This feature makes it quicker to change the entire value of the text field. With the text pre-selected, there's no need to manually clear it—simply start typing, and the new input will instantly replace the existing text.

#### Auto proceed with randomized colors
Generating a new random color on *Home* screen will automatically proceed with it.

## Software approaches

### Clean Architecture
Clean architecture straight from Robert Martin's book. The app has 3 layers: domain, data and presentation. 
Domain layer is the most important and sensitive one, thus it's the most high-level and doesn't depend on other layers. 
Data layer retrieves and stores data used by domain layer. 
Presentation layer contains user interface (GUI in particular).

There are some curious places in the code architecture-wise, like accessing `Repositories` directly from Presentation and implementing use cases in Data layer. 
I'm giving you the opportunity to find them in code and make sense of them yourself. Documentation and commit history will help you with it.

### Gradle modularization
Each feature or related group of code is located in its own Gradle module. 
This allows to easier trace dependencies between parts of the codebase and restricts access to things that should not be accessed in a particular context. Also improves build time and project structure.

### Single `Activity` approach 
Using a single `Activity` in the app reduces system resource consumption and simplifies navigation and state management. 

Creating an `Activity` in Android is computationally expensive because it involves a series of costly lifecycle and system-level operations.

Single `Activity` approach allows seamless deep-link handling, efficient back stack management, and shared element transitions in UI without dealing with multiple `Activity` lifecycles. 
This approach reduces complexity, avoids context leaks, and spares system resources by minimizing redundant processing and memory allocation associated with creating multiple Activities.

### Event-Command approach
Allows to create highly reusable features and components. Introduces terms *Event* and *Command*.

*Event* is something that originates inside component (feature) and is destined to be broadcast to the outside (hosting components, clients). 
An example of the event in the app is selecting a color swatch in *Color Scheme* feature. When the *Color Scheme* is located on *Home* screen, it will open a *Color Details* dialog for the selected color. 
The *Home* screen decides how to react to the event of selecting a color swatch. This allows to define any behaviour depending on the context where *Color Scheme* is used. For example, in other place selecting a color swatch will open a web browser.

*Command* originates in the outside of the component and destined to be processed (handled, executed, reacted to) by the component itself. 
An example of the command in the app is fetching data in *Color Details* once the color is proceeded with. *Home* screen sends a command "fetch data for this color" to *Color Details*, and the latter obeys.

### Platform-agnostic `ViewModel`s
Platform-agnostic ViewModels, free from Android SDK and UI framework dependencies, improve reusability, testability, and maintainability. 

Decoupling from Android SDK significantly simplifies unit tests, removing the need to have Robolectric to recreate components like `Context` that are present only on real devices.

Freeing from dependencies of specific UI framework keeps ViewModels more stable and protected from changes in the future. 
Opting in for using `Flow` from Kotlin instead of `State` from Jetpack Compose will save your ViewModels from changes if one day you decide to change a UI framework.

Exposing platform-agnostic models from ViewModels enables latter to be used in UI implemented with any framework.
P.S.: in all my years of experience, I've never faced a task that required me to bring platform/framework-specific code into ViewModels.

### Separation in API and implementation in Presentation layer
Few feature modules in Presentation layer are split in two sub-modules: API and implementation.

API module contains components that are exposed to other features and are platform-agnostic.
Usually, there are Presentation layer models and ViewModel-related components in API module. In other words, everything one may need to reuse the __logic__ of feature X in some other feature Y.

Implementation module contains implementations of abstractions defined in API module.
Here you will find all platform/framework-specific code, UI included.

Just as *Gradle modularization*, this approach helps with code exposure and provides better view on project structure.
At the moment of writing this, I'm still experimenting with this approach to understand whether it's worth sticking to.
One's for sure: it is highly optional.

### Independent features
I'm trying to keep features of the app independent from each other on the level of dependencies in code.
Every module in Presentation layer is either an independent feature or a screen that depends on a number of such features.

For example, right now *Color Preview* is only used on *Home* screen to display currently entered color. If you trace the code, you'll find that essentially *Color Preview* takes data from *Color Input*. 
Nonetheless, *Color Preview* doesn't depend on the code of *Color Input*. Instead, *Home* screen (that hosts both features) re-streams data from *Color Input* to *Color Preview*, which doesn't even "know" about the existence of *Color Input*.
This allows to use *Color Preview* in other places and with other color sources.

At the same time, screens depend on features.
It's highly unlikely that you'll be reusing the same screen but with different features on it.
Thus, there's no need to make screen modules as independent and reusable as feature modules.
They (screens) explicitly depend on feature modules they use and act as mediators between features inside.

### Valuable unit tests
Most of the principles I follow when it comes to unit tests are talked about in this brilliant presentation:
[Write awesome tests by Jeroen Mols](https://youtu.be/F8Gc8Nwf0yk?si=M6Z_-75ueUsn4iO_)

Additionally, I have a set of my own rules.
I **only** write (or add new) unit tests if:
 - a piece of code was caught containing a bug
 - a piece of code may be executed in a multitude of ways and yield different results / produce various side-effects
 - a piece of code is vital for the application
 - a piece of code employs API that I'm not entirely sure how it works

When writing a new unit test, I try to put it next to other tests that target the same feature of the component.
Most of the times I have only one assertion in the test. 
I add verbose documentation for tests that have complex *given*, *when* or *then* part(s).
I keep most of mocked behaviour in the body of each test case. I rarely mock dependencies on the level of the class that contains tests.
I almost never use visibility modifiers in unit test classes.

All above allows me to have valuable and readable tests. I don't waste time chasing code coverage, testing that obvious code indeed *does* work. 
If I want to remind myself of the software contract that a component establishes and I feel lazy going through its code, then I may just open its unit tests and see what is the expected behaviour.
