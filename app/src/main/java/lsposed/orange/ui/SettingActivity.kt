package lsposed.orange.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import lsposed.orange.R
import lsposed.orange.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private val activitySettingBinding by lazy { ActivitySettingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activitySettingBinding.root)
        setTitle(R.string.title_category_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (supportFragmentManager.findFragmentByTag(SettingFragment.TAG) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingFragment(), SettingFragment.TAG)
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}