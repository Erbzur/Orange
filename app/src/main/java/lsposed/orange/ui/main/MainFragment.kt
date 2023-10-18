package lsposed.orange.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import lsposed.orange.R
import lsposed.orange.ui.MainActivity
import lsposed.orange.ui.setting.SettingFragment

class MainFragment : Fragment(R.layout.fragment_main), AppListAdapter.EventListener {

    companion object {
        val TAG = MainFragment::class.simpleName!!
        private const val MIN_LOADING_DURATION = 1000L
        private const val MIN_ITEM_CLICK_DURATION = 500L
    }

    private val mainViewModel by viewModels<MainViewModel>()
    private var lastLoadStartTime = 0L
    private var lastItemClickTime = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val appListView = view.findViewById<RecordScrollRecyclerView>(R.id.app_list)
        val progressBarView = view.findViewById<ContentLoadingProgressBar>(R.id.progress_bar)
        val appListAdapter = AppListAdapter()
        appListAdapter.eventListener = this
        appListView.apply {
            adapter = appListAdapter
            itemAnimator = null
        }
        mainViewModel.appListLiveData.observe(viewLifecycleOwner) {
            appListAdapter.submitList(it) {
                appListView.scrollToLastPosition()
            }
        }
        mainViewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) {
                appListView.isVisible = false
                progressBarView.show()
                lastLoadStartTime = System.currentTimeMillis()
            } else {
                progressBarView.postDelayed({
                    progressBarView.hide()
                    progressBarView.postDelayed({ appListView.isVisible = true }, 50)
                }, MIN_LOADING_DURATION - (System.currentTimeMillis() - lastLoadStartTime))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            it.title = "${getString(R.string.app_name)}${
                if (!isModuleActive()) " (${getString(R.string.label_module_not_active)})" else ""
            }"
        }
        mainViewModel.loadAppList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main, menu)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                mainViewModel.queryText = newText
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> mainViewModel.loadAppList(true)
            R.id.setting -> MainActivity.gotoFragment(
                parentFragmentManager,
                SettingFragment(),
                SettingFragment.TAG
            )
            R.id.exit -> activity?.finishAffinity()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onAppListItemClick(appListItem: AppListItem) {
        val activity = activity
        val currentTime = System.currentTimeMillis()
        if (activity != null && currentTime - lastItemClickTime > MIN_ITEM_CLICK_DURATION) {
            AlertDialog.Builder(activity)
                .setSingleChoiceItems(
                    R.array.orientation_options,
                    appListItem.orientation.ordinal
                ) { dialog, which ->
                    mainViewModel.updateConfigApp(appListItem.packageName, which)
                    dialog.dismiss()
                }.show()
            lastItemClickTime = currentTime
        }
    }

    private fun isModuleActive() = false
}