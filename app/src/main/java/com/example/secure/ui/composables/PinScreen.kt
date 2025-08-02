package com.example.secure.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.example.secure.R
import com.example.secure.ui.pin.PinViewModel
import com.example.secure.ui.theme.ISecureTheme // Assuming your theme is ISecureTheme
import androidx.biometric.BiometricPrompt
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    viewModel: PinViewModel = viewModel(),
    activity: FragmentActivity, // Required for BiometricPrompt
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    // val lifecycleOwner = LocalLifecycleOwner.current // Not directly used here but good for awareness

    // Biometric Prompt
    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = remember(executor, activity, viewModel) { // Add viewModel to remember key if its methods change
        BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel.onBiometricAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onBiometricAuthenticationSucceeded()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    viewModel.onBiometricAuthenticationFailed()
                }
            })
    }

    val promptInfo = remember(context) { // Context can be a key if strings change with locale
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.prompt_fingerprint_title))
            .setSubtitle(context.getString(R.string.prompt_fingerprint_subtitle))
            .setNegativeButtonText(context.getString(R.string.fingerprint_dialog_negative_button_text))
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    }

    LaunchedEffect(uiState.navigateToMain) {
        if (uiState.navigateToMain) {
            onNavigateToMain()
            viewModel.resetNavigation() // Reset navigation trigger
        }
    }

    LaunchedEffect(uiState.requestBiometricPrompt) {
        if (uiState.requestBiometricPrompt) {
            biometricPrompt.authenticate(promptInfo)
            viewModel.biometricPromptShown() // Signal that prompt has been shown
        }
    }


    ISecureTheme { // Apply the theme
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottiePinAnimation(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier.weight(2f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.promptText.ifEmpty {
                            stringResource(id = R.string.prompt_enter_pin) // Fallback
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    uiState.errorText?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    PinIndicator(currentLength = uiState.enteredPin.length, maxLength = 4)

                    Spacer(modifier = Modifier.height(32.dp))

                    if (uiState.isLockedOut) {
                        LockoutView(remainingSeconds = uiState.lockoutRemainingSeconds)
                    } else {
                        NumericKeypad(
                            onDigitClick = { digit -> viewModel.onDigitEntered(digit) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FingerprintAndBackspaceRow(
                            isFingerprintVisible = uiState.isFingerprintVisible,
                            onFingerprintClick = { viewModel.requestBiometricPrompt() },
                            onZeroClick = { viewModel.onDigitEntered("0") },
                            onBackspaceClick = { viewModel.onBackspace() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockoutView(remainingSeconds: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Lockout Icon",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Try again in $remainingSeconds seconds",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LottiePinAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.login_animation))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PinIndicator(currentLength: Int, maxLength: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (i in 0 until maxLength) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(
                        1.dp,
                        if (i < currentLength) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.3f
                        ),
                        shape = MaterialTheme.shapes.small
                    )
                    .then(
                        if (i < currentLength) Modifier.background(
                            MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) else Modifier
                    )
            )
        }
    }
}

@Composable
fun NumericKeypad(onDigitClick: (String) -> Unit) {
    val numbers = (1..9).map { it.toString() }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.width(240.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(numbers) { number ->
            KeypadButton(text = number, onClick = { onDigitClick(number) })
        }
    }
}

@Composable
fun FingerprintAndBackspaceRow(
    isFingerprintVisible: Boolean,
    onFingerprintClick: () -> Unit,
    onZeroClick: () -> Unit,
    onBackspaceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(240.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Distributes space
    ) {
        // Conditionally render Fingerprint icon or a Spacer to maintain layout consistency
        if (isFingerprintVisible) {
            IconButton(onClick = onFingerprintClick, modifier = Modifier.size(48.dp)) { // Standard IconButton size
                Icon(Icons.Default.Fingerprint, contentDescription = stringResource(R.string.prompt_fingerprint_title), modifier = Modifier.size(36.dp))
            }
        } else {
            // Spacer to take up the place of the fingerprint icon
            Spacer(Modifier.size(48.dp))
        }

        KeypadButton(text = "0", onClick = onZeroClick, modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) // Give some padding

        IconButton(onClick = onBackspaceClick, modifier = Modifier.size(48.dp)) { // Standard IconButton size
            Icon(Icons.Default.Backspace, contentDescription = "Backspace", modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1.5f)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun PinScreenPreview_Basic() {
    ISecureTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottiePinAnimation(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Enter your PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                PinIndicator(currentLength = 2, maxLength = 4)
                Spacer(modifier = Modifier.height(32.dp))
                NumericKeypad(onDigitClick = {})
                Spacer(modifier = Modifier.height(16.dp))
                FingerprintAndBackspaceRow(
                    isFingerprintVisible = true,
                    onFingerprintClick = {},
                    onZeroClick = {},
                    onBackspaceClick = {}
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Pin Indicator Preview (2/4)")
@Composable
fun PinIndicatorPreview() {
    ISecureTheme {
        PinIndicator(currentLength = 2, maxLength = 4)
    }
}

@Preview(showBackground = true, name = "Keypad Preview")
@Composable
fun KeypadPreview() {
    ISecureTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NumericKeypad (onDigitClick = {})
            Spacer(modifier = Modifier.height(16.dp))
            FingerprintAndBackspaceRow(isFingerprintVisible = true, onFingerprintClick = { }, onZeroClick = { }, onBackspaceClick = { })
            Spacer(modifier = Modifier.height(16.dp))
            FingerprintAndBackspaceRow(isFingerprintVisible = false, onFingerprintClick = { }, onZeroClick = { }, onBackspaceClick = { })
        }
    }
}
