package lsposed.orange.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import lsposed.orange.R
import lsposed.orange.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), AppListAdapter.EventListener {

    private companion object {
        const val MIN_LOADING_DURATION = 1000L
        const val MIN_ITEM_CLICK_DURATION = 500L
    }

    private val mainViewModel by viewModels<MainViewModel>()
    private val activityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var lastLoadStartTime = 0L
    private var lastItemClickTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        if (!isModuleActive()) {
            supportActionBar?.apply {
                title = "$title (${getString(R.string.label_module_not_active)})"
            }
        }

        val appListAdapter = AppListAdapter()
        appListAdapter.eventListener = this
        activityMainBinding.appList.apply {
            adapter = appListAdapter
            itemAnimator = null
        }

        mainViewModel.appListLiveData.observe(this) {
            appListAdapter.submitList(it) {
                activityMainBinding.appList.scrollToLastPosition()
            }
        }
        mainViewModel.isLoadingLiveData.observe(this) {
            if (it) {
                activityMainBinding.appList.isVisible = false
                activityMainBinding.progressBar.show()
                lastLoadStartTime = System.currentTimeMillis()
            } else {
                activityMainBinding.appList.postDelayed({
                    activityMainBinding.appList.isVisible = true
                    activityMainBinding.progressBar.hide()
                }, MIN_LOADING_DURATION - (System.currentTimeMillis() - lastLoadStartTime))
            }
        }
        mainViewModel.loadAppList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> mainViewModel.loadAppList()
            R.id.setting -> startActivity(Intent(this, SettingActivity::class.java))
            R.id.exit -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun isModuleActive(): Boolean {
        return false
    }

    override fun onAppListItemClick(appListItem: AppListItem) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastItemClickTime > MIN_ITEM_CLICK_DURATION) {
            AlertDialog.Builder(this)
                .setSingleChoiceItems(
                    R.array.orientation_options,
                    appListItem.orientation.ordinal
                ) { dialog, which ->
                    mainViewModel.updateConfigApp(appListItem.packageName, which)
                    dialog.dismiss()
                }
                .show()
            lastItemClickTime = currentTime
        }
    }
}