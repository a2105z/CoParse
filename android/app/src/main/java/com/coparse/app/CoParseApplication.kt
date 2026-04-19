package com.coparse.app

import android.app.Application
import com.coparse.app.data.AnalysisRepository
import com.coparse.app.data.DisclaimerStore

class CoParseApplication : Application() {
    lateinit var repository: AnalysisRepository
        private set
    lateinit var disclaimerStore: DisclaimerStore
        private set

    override fun onCreate() {
        super.onCreate()
        repository = AnalysisRepository(this)
        disclaimerStore = DisclaimerStore(this)
    }
}
