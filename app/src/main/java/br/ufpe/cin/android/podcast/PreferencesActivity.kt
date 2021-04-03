package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class PreferencesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)
        //Após criar o fragmento, use o código abaixo para exibir
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.preferencias,PrefsFragment())
                .commit()
    }

    class PrefsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_screen)
        }

    }
}