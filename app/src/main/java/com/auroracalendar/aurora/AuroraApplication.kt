package com.auroracalendar.aurora

import android.app.Application
import com.google.dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Hilt generates the DI graph from this annotated class.
 *
 * Decision: Hilt is used instead of manual Dagger/Koin because it is the Android
 * standard, reduces boilerplate, and makes module boundaries (data vs domain vs ui)
 * explicit and testable — important for long-term maintainability and AI handoff.
 */
@HiltAndroidApp
class AuroraApplication : Application()
