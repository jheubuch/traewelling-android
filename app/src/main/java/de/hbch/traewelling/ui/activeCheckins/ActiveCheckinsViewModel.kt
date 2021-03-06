package de.hbch.traewelling.ui.activeCheckins

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusPage
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActiveCheckinsViewModel : ViewModel() {
    fun getActiveCheckins(
        successCallback: (List<Status>) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.checkInService.getStatuses()
            .enqueue(object: Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    if (response.isSuccessful) {
                        val statuses = response.body()
                        if (statuses != null) {
                            successCallback(statuses.data)
                            return
                        }
                    }
                    failureCallback()
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }
                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }
}