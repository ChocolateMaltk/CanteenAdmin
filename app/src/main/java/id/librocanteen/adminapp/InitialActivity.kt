package id.librocanteen.adminapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class InitialActivity : AppCompatActivity() {

    private val sharedPrefName = "AdminPrefs"
    private val secretCodeKey = "secret_code_validated"

    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)

        settings = Settings()

        if (!isSecretCodeValidated()) {
            showSecretCodeDialog()
        }
    }

    private fun isSecretCodeValidated(): Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(secretCodeKey, false)
    }

    private fun showSecretCodeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Secret Code")

        val input = android.widget.EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val enteredCode = input.text.toString()

            if (enteredCode == settings.secretPhrase) {
                saveSecretCodeValidation(true)
                Toast.makeText(applicationContext, "Secret Code Validated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Incorrect Secret Code", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(applicationContext, "Canceled", Toast.LENGTH_SHORT).show()
            finish()
        }

        builder.show()
    }

    private fun saveSecretCodeValidation(isValid: Boolean) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(secretCodeKey, isValid)
            apply()
        }
    }
}
