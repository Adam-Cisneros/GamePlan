package com.twig.gameplan

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Plan",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    onAuthSuccess()
                                } else {
                                    Toast.makeText(context, "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }

            TextButton(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    onAuthSuccess()
                                } else {
                                    Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
            ) {
                Text("Register")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Button(
                onClick = {
                    scope.launch {
                        signInWithGoogle(context as Activity, auth, onAuthSuccess)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Sign in with Google")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    signInWithGitHub(context as Activity, auth, onAuthSuccess)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Sign in with GitHub")
            }
        }
    }
}

private suspend fun signInWithGoogle(
    activity: Activity,
    auth: FirebaseAuth,
    onAuthSuccess: () -> Unit
) {
    val credentialManager = CredentialManager.create(activity)
    
    // Note: You need to have default_web_client_id in your strings.xml 
    // usually generated by the Google Services plugin
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(activity.getString(activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)))
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(activity, request)
        val credential = GoogleAuthProvider.getCredential(result.credential.data.getString("androidx.credentials.BUNDLE_KEY_ID_TOKEN"), null)
        auth.signInWithCredential(credential).await()
        onAuthSuccess()
    } catch (e: Exception) {
        Toast.makeText(activity, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun signInWithGitHub(
    activity: Activity,
    auth: FirebaseAuth,
    onAuthSuccess: () -> Unit
) {
    val provider = OAuthProvider.newBuilder("github.com")
    
    // Check for pending result
    val pendingResultTask = auth.pendingAuthResult
    if (pendingResultTask != null) {
        pendingResultTask.addOnSuccessListener {
            onAuthSuccess()
        }.addOnFailureListener {
            Toast.makeText(activity, "GitHub sign-in failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
    } else {
        auth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener {
                onAuthSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "GitHub sign-in failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
