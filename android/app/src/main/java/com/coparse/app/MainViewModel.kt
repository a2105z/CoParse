package com.coparse.app

import androidx.lifecycle.ViewModel
import com.coparse.app.data.remote.AnalysisResponse

class MainViewModel : ViewModel() {
    var hintContractType: String? = null
    var hintRole: String? = null

    var lastDocumentId: String? = null
    var lastAnalysis: AnalysisResponse? = null
}
