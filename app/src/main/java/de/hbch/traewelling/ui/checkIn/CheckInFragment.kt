package de.hbch.traewelling.ui.checkIn

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentCheckInBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.checkInSuccessful.CheckInSuccessfulBottomSheet
import de.hbch.traewelling.ui.include.selectBusinessType.SelectBusinessTypeBottomSheet
import de.hbch.traewelling.ui.include.selectEvent.SelectEventBottomSheet
import de.hbch.traewelling.ui.include.selectStatusVisibility.SelectStatusVisibilityBottomSheet
import kotlinx.coroutines.*


class CheckInFragment : Fragment() {

    private lateinit var binding: FragmentCheckInBinding
    private val args: CheckInFragmentArgs by navArgs()
    private val checkInViewModel: CheckInViewModel by activityViewModels()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckInBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            layoutCheckIn.transitionName = args.transitionName
            destination = args.destination
            viewModel = checkInViewModel
            checkInFragment = this@CheckInFragment
            eventViewModel = this@CheckInFragment.eventViewModel
            btnSendToot.visibility =
                when (loggedInUserViewModel.loggedInUser.value?.mastodonUrl != null) {
                    true -> VISIBLE
                    false -> GONE
                }
            btnSendTweet.visibility =
                when (loggedInUserViewModel.loggedInUser.value?.twitterUrl != null) {
                    true -> VISIBLE
                    false -> GONE
                }
            toggleGroupSocialMedia.addOnButtonCheckedListener { _, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_send_toot -> checkInViewModel.toot.value = isChecked
                    R.id.btn_send_tweet -> checkInViewModel.tweet.value = isChecked
                }
            }
        }

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            val color = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.windowBackground, color, true)
            if (color.type >= TypedValue.TYPE_FIRST_COLOR_INT && color.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                setAllContainerColors(color.data)
            }
        }
        return binding.root
    }

    fun selectStatusVisibility() {
        val bottomSheet = SelectStatusVisibilityBottomSheet { statusVisibility ->
            checkInViewModel.statusVisibility.postValue(statusVisibility)
        }
        bottomSheet.show(parentFragmentManager, SelectStatusVisibilityBottomSheet.TAG)
    }

    fun selectStatusBusiness() {
        val bottomSheet = SelectBusinessTypeBottomSheet { business ->
            checkInViewModel.statusBusiness.postValue(business)
        }
        bottomSheet.show(parentFragmentManager, SelectBusinessTypeBottomSheet.TAG)
    }

    fun selectEvent() {
        val bottomSheet = SelectEventBottomSheet(eventViewModel.activeEvents.value ?: listOf()) {
            this@CheckInFragment.checkInViewModel.event.postValue(it)
        }
        bottomSheet.show(parentFragmentManager, SelectEventBottomSheet.TAG)
    }

    fun checkIn() {
        checkInViewModel.checkIn({ response ->
            if (response != null) {
                val checkInSuccessfulBottomSheet = CheckInSuccessfulBottomSheet(response)
                checkInSuccessfulBottomSheet.show(
                    parentFragmentManager,
                    CheckInSuccessfulBottomSheet.TAG
                )
                CoroutineScope(Dispatchers.Main).launch {
                    findNavController().navigate(CheckInFragmentDirections.actionCheckInFragmentToDashboardFragment())
                    delay(3000)
                    checkInSuccessfulBottomSheet.dismiss()
                }
                checkInViewModel.reset()
            }
        }, { statusCode ->
            val alertBottomSheet = AlertBottomSheet(
                AlertType.ERROR,
                requireContext().getString(when(statusCode) {
                    409 -> R.string.check_in_conflict
                    else -> R.string.check_in_failure
                }),
                3000
            )
            alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
        })
    }
}