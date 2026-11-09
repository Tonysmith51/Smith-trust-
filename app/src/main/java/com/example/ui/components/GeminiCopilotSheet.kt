package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiAssistantState
import com.example.ai.TrustGeminiAssistant
import com.example.data.TrustState
import com.example.ui.theme.LocalTrustColors
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "gemini"
    val text: String,
    val timestamp: String = "Just now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiCopilotSheet(
    state: TrustState,
    assistant: TrustGeminiAssistant,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    val coroutineScope = rememberCoroutineScope()
    var promptText by remember { mutableStateOf("") }
    var assistantState by remember { mutableStateOf<GeminiAssistantState>(GeminiAssistantState.Idle) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "gemini",
                text = "Greetings Anthony. I am your Gemini Sovereign Copilot. I have full real-time visibility into the Anthony Smith Master Trust blueprint—including the \$100M vault corpus, 11 Chase Sapphire Reserve family cards, Aunt Amy's minors escrow, Michigan real estate compounds, and executive aircraft maintenance schedules. How can I assist you today?"
            )
        )
    }

    val sampleQueries = listOf(
        "Summarize Trust Net Worth",
        "Check Aircraft Maintenance Due",
        "Explain Aunt Amy's Minors Escrow",
        "How much is left on Family Sapphire Cards?",
        "Review Monthly Cash Sweep ($300k+)"
    )

    fun sendQuery(query: String) {
        if (query.isBlank()) return
        messages.add(ChatMessage(sender = "user", text = query))
        promptText = ""
        assistantState = GeminiAssistantState.Loading

        coroutineScope.launch {
            try {
                val response = assistant.askGemini(state, query)
                messages.add(ChatMessage(sender = "gemini", text = response))
                assistantState = GeminiAssistantState.Success(response)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to connect to Gemini."
                messages.add(
                    ChatMessage(
                        sender = "gemini",
                        text = "Notice: $errorMsg\n(You can ask about any aspect of the trust vault, family cards, real estate, or fleet schedules)."
                    )
                )
                assistantState = GeminiAssistantState.Error(errorMsg)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = tokens.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = tokens.gold) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = tokens.accent.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = tokens.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Gemini Sovereign Copilot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = tokens.gold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "gemini-3.8-flash",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.gold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "AI Office Assistant for Anthony Smith Master Trust",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = tokens.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Query Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sampleQueries) { query ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tokens.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.accent.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { sendQuery(query) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = query, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tokens.textPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Conversation Thread
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(messages) { msg ->
                    val isGemini = msg.sender == "gemini"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isGemini) Arrangement.Start else Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isGemini) 4.dp else 16.dp,
                                bottomEnd = if (isGemini) 16.dp else 4.dp
                            ),
                            color = if (isGemini) tokens.surfaceElevated else tokens.accent.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isGemini) tokens.border else tokens.accent.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (isGemini) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Gemini", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tokens.accent)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = tokens.textPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                if (assistantState is GeminiAssistantState.Loading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.surfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = tokens.accent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini is analyzing trust blueprint & accounts...",
                                fontSize = 11.sp,
                                color = tokens.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = { Text("Ask Gemini about your trust...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gemini_prompt_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.accent,
                        unfocusedBorderColor = tokens.border
                    )
                )

                IconButton(
                    onClick = { sendQuery(promptText) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(tokens.accent)
                        .testTag("gemini_send_button"),
                    enabled = promptText.isNotBlank() && assistantState !is GeminiAssistantState.Loading
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
