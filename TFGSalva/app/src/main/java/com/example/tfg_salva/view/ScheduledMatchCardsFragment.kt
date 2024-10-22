package com.example.tfg_salva.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tfg_salva.adapter.MatchCardAdapter
import com.example.tfg_salva.R
import com.example.tfg_salva.dataClasses.*
import com.example.tfg_salva.localDatabase.AppDatabase
import com.example.tfg_salva.viewModel.CompletedMatchViewModel
import com.example.tfg_salva.viewModel.ScheduledMatchViewModel
import kotlin.concurrent.thread

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MatchCardsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScheduledMatchCardsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var recyclerViewMatches: RecyclerView
    lateinit var matchesCardAdapter: MatchCardAdapter
    lateinit var scheduledMatchViewModel: ScheduledMatchViewModel
    lateinit var refreshSwipe: SwipeRefreshLayout
    private var TAG = "Scheduled Match Cards Fragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.scheduled_match_cards_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewMatches = requireView().findViewById(R.id.recyclerViewScheduledMatchCards)
        recyclerViewMatches.layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        scheduledMatchViewModel = ScheduledMatchViewModel

        scheduledMatchViewModel.scheduledMatches.observe(viewLifecycleOwner, Observer {
            matchesCardAdapter = MatchCardAdapter(it, requireContext())
            recyclerViewMatches.adapter = matchesCardAdapter
            refreshSwipe.isRefreshing = false
        })

        setRefreshSwipeAction()
    }

    fun setRefreshSwipeAction(){
        refreshSwipe = requireView().findViewById(R.id.scheduled_matches_refresh_swipe)

        refreshSwipe.setOnRefreshListener{
            ScheduledMatchViewModel.databaseAdapter.getScheduledMatches()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MatchCardsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScheduledMatchCardsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}