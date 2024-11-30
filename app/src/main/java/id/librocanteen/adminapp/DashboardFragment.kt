package id.librocanteen.adminapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.android.material.textfield.TextInputEditText

class DashboardFragment : Fragment() {

    // Declare the views
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerButton: Button

    private val database = FirebaseDatabase.getInstance()
    private val adminsRef: DatabaseReference = database.getReference("admins")
    private val settings = Settings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize views
        usernameInput = view.findViewById(R.id.usernameInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        registerButton = view.findViewById(R.id.registerButton)

        // Set OnClickListener for Register/Login Button
        registerButton.setOnClickListener {
            checkAdmins()
        }

        return view
    }

    private fun checkAdmins() {
        // Check the admins node in Firebase Realtime Database
        adminsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // No admins found, allow registration of the first admin
                    registerAdmin()
                } else {
                    // Admins found, proceed with login logic
                    loginAdmin(dataSnapshot)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Error checking admins: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerAdmin() {
        // If no admins exist, check the max limit (maxAdmins in settings)
        adminsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adminCount = dataSnapshot.childrenCount.toInt()
                if (adminCount < settings.maxAdmins) {
                    // Proceed to register as admin
                    val username = usernameInput.text.toString().trim()
                    val password = passwordInput.text.toString().trim()

                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(requireContext(), "Please fill in both fields.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Save the admin data to Realtime Database
                        val adminData = hashMapOf(
                            "username" to username,
                            "password" to password
                        )

                        adminsRef.push().setValue(adminData)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Admin registered successfully", Toast.LENGTH_SHORT).show()
                                // Proceed to the next screen or logic
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(requireContext(), "Error registering admin: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "Max admin limit reached.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Error checking admin count: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loginAdmin(dataSnapshot: DataSnapshot) {
        // If admins exist, check if the entered credentials match
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in both fields.", Toast.LENGTH_SHORT).show()
            return
        }

        var validLogin = false
        for (adminSnapshot in dataSnapshot.children) {
            val storedUsername = adminSnapshot.child("username").getValue(String::class.java)
            val storedPassword = adminSnapshot.child("password").getValue(String::class.java)

            if (username == storedUsername && password == storedPassword) {
                validLogin = true
                break
            }
        }

        if (validLogin) {
            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
            // Go to home or something idk.
        } else {
            Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
    }
}
