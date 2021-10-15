package de.hbch.traewelling.ui.searchConnection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.ConnectionAdapter
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.trip.HafasTripPage
import de.hbch.traewelling.databinding.FragmentSearchConnectionBinding
import de.hbch.traewelling.models.Connection
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SearchConnectionFragment : Fragment() {

    private lateinit var binding: FragmentSearchConnectionBinding
    private lateinit var searchStationCard: SearchStationCard
    private val args: SearchConnectionFragmentArgs by navArgs()
    private val viewModel: SearchConnectionViewModel by viewModels()
    private val checkInViewModel: CheckInViewModel by activityViewModels()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchCard.viewModel.removeLocationUpdates()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchConnectionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val connectionRecyclerView = binding.recyclerViewConnections
        connectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        connectionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        viewModel.departures.observe(viewLifecycleOwner) { connections ->
            binding.recyclerViewConnections.adapter =
                ConnectionAdapter(connections.data) { itemView, connection ->
                    checkInViewModel.reset()
                    checkInViewModel.lineName = connection.line.name
                    checkInViewModel.tripId = connection.tripId
                    checkInViewModel.startStationId = connection.station.id
                    checkInViewModel.departureTime = connection.plannedDeparture

                    val transitionName = connection.tripId
                    val extras = FragmentNavigatorExtras(itemView to transitionName)
                    val action =
                        SearchConnectionFragmentDirections.actionSearchConnectionFragmentToSelectDestinationFragment(
                            transitionName,
                            connection.destination
                        )
                    findNavController().navigate(action, extras)
                }
            binding.stationName = connections.meta.station.name
            binding.executePendingBindings()
        }
        viewModel.searchConnections(args.stationName, Date())

        binding.stationName = args.stationName
        searchStationCard = SearchStationCard(this, binding.searchCard, args.stationName)
        loggedInUserViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                searchStationCard.homelandStation.postValue(user.home!!.name)
            }
        }

        binding.apply {
            searchCard.viewModel = searchStationCard
            viewModel = (this@SearchConnectionFragment).viewModel
        }
        return binding.root
    }


}