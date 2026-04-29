package com.moneyvisor.feature.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyvisor.core.designsystem.theme.BrandBlue

@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mock Phone Image Placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // In a real app, we'd use a real image resource. 
            // Here we'll simulate the phone mockup visually with shapes if needed, 
            // but for this task I'll just use a placeholder Box with a border.
            Surface(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .aspectRatio(0.5f)
                    .clip(RoundedCornerShape(32.dp)),
                color = Color(0xFFF0F2F5)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(BrandBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💰", fontSize = 48.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Manage and Organize Your Finance",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1A1A1A),
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Use the possibilities of a personal accounts, save and invest.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFF757575),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Page Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 2) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(if (index == 2) BrandBlue else Color(0xFFE0E0E0))
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onGetStartedClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlue
            )
        ) {
            Text(
                text = "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
