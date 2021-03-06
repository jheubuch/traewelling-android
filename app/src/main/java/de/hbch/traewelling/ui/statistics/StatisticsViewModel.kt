package de.hbch.traewelling.ui.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.statistics.PersonalStatistics
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class StatisticsViewModel : ViewModel() {

    var dateRange = MutableLiveData<Pair<Date, Date>>()

    init {
        val calendar = GregorianCalendar()
        calendar.time = Date()
        calendar.set(Calendar.DATE, 1)

        dateRange.postValue(Pair(calendar.time, Date()))
    }


    fun getPersonalStatisticsForTimeRange(
        from: Date,
        until: Date,
        successfulCallback: (PersonalStatistics) -> Unit,
        failureCallback: () -> Unit
    ) {
        dateRange.postValue(Pair(from, until))
        getPersonalStatisticsForSelectedTimeRange(
            successfulCallback,
            failureCallback
        )
    }

    fun getPersonalStatisticsForSelectedTimeRange(
        successfulCallback: (PersonalStatistics) -> Unit,
        failureCallback: () -> Unit
    ) {
        val range = dateRange.value ?: return

        val from = range.first
        val until = range.second

        TraewellingApi
            .statisticsService
            .getPersonalStatistics(from, until)
            .enqueue(object: Callback<Data<PersonalStatistics>> {
                override fun onResponse(
                    call: Call<Data<PersonalStatistics>>,
                    response: Response<Data<PersonalStatistics>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        if (data != null) {
                            successfulCallback(data)
                            return
                        }
                    }
                    failureCallback()
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }

                override fun onFailure(call: Call<Data<PersonalStatistics>>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }
}