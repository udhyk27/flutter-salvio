package com.ydh.salvio.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ydh.salvio.ui.component.SalvioCard
import com.ydh.salvio.ui.theme.GitHubBlue
import com.ydh.salvio.ui.theme.GitHubTextSecondary
import com.ydh.salvio.ui.theme.Radius
import com.ydh.salvio.ui.theme.Spacing
import com.ydh.salvio.viewmodel.AuthState
import com.ydh.salvio.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    var token by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Salvio",
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "GitHub 프로젝트 모니터링",
                style = MaterialTheme.typography.bodyMedium,
                color = GitHubTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xxl + Spacing.sm))

            SalvioCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Personal Access Token",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx", color = GitHubTextSecondary) },
                        visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showToken = !showToken }) {
                                Icon(
                                    imageVector = if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = GitHubTextSecondary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GitHubBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true,
                        shape = Radius.field
                    )

                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = { authViewModel.loginWithToken(token.trim()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = token.isNotBlank() && authState !is AuthState.Loading,
                        shape = Radius.button
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("로그인", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Settings → Developer settings → Personal access tokens에서 발급\n필요 권한: repo, read:user",
                style = MaterialTheme.typography.labelMedium,
                color = GitHubTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
