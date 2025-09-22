package com.example.secure.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    var splashLogicFinished by remember { mutableStateOf(false) } // New state to prevent double calls
    
    // Debug: Log when splash screen is composed
    LaunchedEffect(Unit) {
        println("SplashScreen: Composing splash screen")
    }
    
    // Animation values
    val alphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 2000,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    val scaleAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(
            durationMillis = 2000,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    
    // Gradient colors - using more vibrant colors for better visibility
    val gradientColors = listOf(
        Color(0xFF6200EE), // Purple 500
        Color(0xFF3700B3), // Purple 700
        Color(0xFF018786)  // Teal 700
    )
    
    LaunchedEffect(Unit) {
        println("SplashScreen: Starting splash screen")
        delay(500) // Small delay to ensure proper initialization
        startAnimation = true
        delay(3500) // Show splash for 3.5 seconds total
        
        // Only finish splash if it hasn't been finished by a touch event already
        if (!splashLogicFinished) {
            println("SplashScreen: Finishing splash automatically")
            splashLogicFinished = true
            onSplashFinished()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6200EE)) // Solid fallback background
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .clickable { 
                // Only finish splash if it hasn't been finished automatically already
                if (!splashLogicFinished) {
                    println("SplashScreen: Finishing splash by touch")
                    splashLogicFinished = true
                    onSplashFinished()
                }
            }, // Added clickable modifier
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scaleAnimation)
                .alpha(alphaAnimation)
        ) {
            // App Icon/Logo placeholder
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // App Name
            Text(
                text = "iSecure Vault",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Your Privacy, Our Priority",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Creator credit
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crafted by",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "KING AMRISH",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )
            }
        }
        
        // Loading indicator at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(alphaAnimation)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        }
    }
}
