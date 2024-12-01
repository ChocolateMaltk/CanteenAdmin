package id.librocanteen.adminapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import id.librocanteen.adminapp.R

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val sharedPrefName = "AdminPrefs"
    private val secretCodeKey = "secret_code_validated"
    private lateinit var settings: Settings
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = Settings()
        navController = findNavController()

        showSecretCodeDialog()

        // Check if the secret code is already validated
        if (!isSecretCodeValidated()) {
            // If not validated, show the dialog for code entry
            showSecretCodeDialog()
        }
    }

    private fun isSecretCodeValidated(): Boolean {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(secretCodeKey, false)
    }

    private fun showSecretCodeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter Secret Code")

        // Set up the input field for the secret code
        val input = EditText(requireContext())
        builder.setView(input)

        builder.setCancelable(false)

        builder.setPositiveButton("OK") { dialog, which ->
            val enteredCode = input.text.toString()

            if (enteredCode == settings.secretPhrase) {
                saveSecretCodeValidation(true)
                Toast.makeText(requireContext(), "Secret Code Validated", Toast.LENGTH_SHORT).show()
                // After validation, proceed with navigation
                navigateToNextScreen()
            } else {
                input.text.clear()
                Toast.makeText(requireContext(), "Invalid Code! Try again.", Toast.LENGTH_SHORT).show()
                showSecretCodeDialog()  // Keep showing the dialog until validated
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }

        builder.show()
    }

    private fun saveSecretCodeValidation(isValid: Boolean) {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(secretCodeKey, isValid)
            apply()
        }
    }

    private fun navigateToNextScreen() {
        navController.navigate(R.id.dashboardFragment)
    }
}
