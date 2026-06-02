package com.cortisense.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay

private val SAGE = Color(0xFF5E9B72)
private val AMBER = Color(0xFFF5A623)
private val CORAL = Color(0xFFE8714A)
private val CRIMSON = Color(0xFFD63E3E)

// ─── Stress Level Palette ─────────────────────────────────────────────────────

// ─── Stress Level Palette ─────────────────────────────────────────────────────

private fun levelColor(index: Int): Color = when {
    index <= 25 -> SAGE
    index <= 50 -> AMBER
    index <= 75 -> CORAL
    else        -> CRIMSON
}

@Composable
private fun levelLabel(index: Int): String = when {
    index <= 25 -> stringResource(R.string.stress_level_low)
    index <= 50 -> stringResource(R.string.stress_level_moderate)
    index <= 75 -> stringResource(R.string.stress_level_high)
    else        -> stringResource(R.string.stress_level_critical)
}

private fun levelEmoji(index: Int): String = when {
    index <= 25 -> "🌿"
    index <= 50 -> "🌤"
    index <= 75 -> "🌩"
    else        -> "🚨"
}

/** Translates a reason key (e.g. "reason_cognitive") to its localized string. */
@Composable
private fun translateReason(key: String): String {
    val resId = when (key) {
        "reason_cognitive" -> R.string.reason_cognitive
        "reason_emotional" -> R.string.reason_emotional
        "reason_physical" -> R.string.reason_physical
        "reason_focus" -> R.string.reason_focus
        "reason_decision" -> R.string.reason_decision
        "reason_memory" -> R.string.reason_memory
        "reason_irritability" -> R.string.reason_irritability
        "reason_motivation" -> R.string.reason_motivation
        "reason_overwhelm" -> R.string.reason_overwhelm
        "reason_muscle" -> R.string.reason_muscle
        "reason_rushing" -> R.string.reason_rushing
        "reason_sleep" -> R.string.reason_sleep
        "reason_sentiment" -> R.string.reason_sentiment
        "reason_healthy" -> R.string.reason_healthy
        else -> -1
    }
    return if (resId != -1) stringResource(resId) else key
}

// ─── Clinical Result Screen ───────────────────────────────────────────────────

@Composable
fun ClinicalResultScreen(
    viewModel: MainViewModel,
    onRecheck: () -> Unit,
    onViewInsights: () -> Unit,
    onStartBreathing: () -> Unit,
    onGrounding: () -> Unit
) {
    val score = viewModel.stressScore
    val level = levelLabel(score)
    val color = levelColor(score)
    val emoji = levelEmoji(score)
    val reasons = viewModel.reasons
    val cogScore = viewModel.cognitiveScore
    val emoScore = viewModel.emotionalScore
    val physScore = viewModel.physicalScore
    val safetyValve = viewModel.triggerSafetyBreathing

    // Safety valve: auto-prompt breathing before the page settles
    var showSafetyPrompt by remember { mutableStateOf(safetyValve) }

    if (showSafetyPrompt) {
        SafetyValveDialog(
            onDismiss = { showSafetyPrompt = false },
            onBreath = { showSafetyPrompt = false; onStartBreathing() }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // ── Level badge ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(emoji, fontSize = 18.sp)
                Text(level, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            // ── Score ring ────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val ringBg = MaterialTheme.colorScheme.outlineVariant
                Canvas(modifier = Modifier.size(180.dp)) {
                    drawArc(ringBg, 0f, 360f, false,
                        style = Stroke(16.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color, -90f, score / 100f * 360f, false,
                        style = Stroke(16.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(score.toString(), fontSize = 52.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text(stringResource(R.string.stress_index_label), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("/100", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── 3D Category Breakdown ─────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.three_d_breakdown), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text(stringResource(R.string.brain_heart_body), fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    CategoryBar(stringResource(R.string.brain_label), cogScore, 12, Color(0xFF4E7FD4))
                    Spacer(Modifier.height(10.dp))
                    CategoryBar(stringResource(R.string.heart_label), emoScore, 12, CORAL)
                    Spacer(Modifier.height(10.dp))
                    CategoryBar(stringResource(R.string.body_label), physScore, 12, SAGE)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Reasons ───────────────────────────────────────────────────
            if (reasons.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.key_signals_detected), fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(10.dp))
                        reasons.forEach { reasonKey ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                                Spacer(Modifier.width(10.dp))
                                Text(translateReason(reasonKey), fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Phase 2: Actionable Recommendations ──────────────────────
            Text(
                "Actionable Recommendations",
                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))

            if (viewModel.aiActions.isNotEmpty()) {
                viewModel.aiActions.forEach { action ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = action.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = action.description,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.completeAiAction(action.id) },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = color)
                            ) {
                                Text("Mark as Done", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = viewModel.aiRecommendation,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            if (viewModel.isEscalated) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CRIMSON.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CRIMSON)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Escalation status active: High stress levels detected.",
                            color = CRIMSON,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Bottom actions ────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onRecheck,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(stringResource(R.string.recheck_btn)) }
                Button(
                    onClick = onViewInsights,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) { Text(stringResource(R.string.view_insights_btn), color = Color.White, fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Category Bar ─────────────────────────────────────────────────────────────

@Composable
private fun CategoryBar(label: String, value: Int, max: Int, color: Color) {
    val frac = (value.toFloat() / max).coerceIn(0f, 1f)
    val animFrac by animateFloatAsState(frac, animationSpec = tween(800), label = "bar")
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("$value/$max", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFrac)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

// ─── Level 1: Maintenance ────────────────────────────────────────────────────

@Composable
private fun Level1Recommendations() {
    val green = SAGE
    RecoCard(
        icon = Icons.Default.EmojiEvents,
        color = green,
        title = stringResource(R.string.reco_l1_t1),
        body = stringResource(R.string.reco_l1_b1),
        action = stringResource(R.string.reco_l1_a1),
        onAction = null
    )
    Spacer(Modifier.height(10.dp))
    RecoCard(
        icon = Icons.Default.MusicNote,
        color = green,
        title = stringResource(R.string.reco_l1_t2),
        body = stringResource(R.string.reco_l1_b2),
        action = null, onAction = null
    )
}

// ─── Level 2: Prevention ─────────────────────────────────────────────────────

@Composable
private fun Level2Recommendations() {
    val amber = AMBER
    RecoCard(
        icon = Icons.Default.Psychology,
        color = amber,
        title = stringResource(R.string.reco_l2_t1),
        body = stringResource(R.string.reco_l2_b1),
        action = null, onAction = null
    )
    Spacer(Modifier.height(10.dp))
    RecoCard(
        icon = Icons.Default.NightsStay,
        color = amber,
        title = stringResource(R.string.reco_l2_t2),
        body = stringResource(R.string.reco_l2_b2),
        action = null, onAction = null
    )
    Spacer(Modifier.height(10.dp))
    RecoCard(
        icon = Icons.Default.NotificationsOff,
        color = amber,
        title = stringResource(R.string.reco_l2_t3),
        body = stringResource(R.string.reco_l2_b3),
        action = null, onAction = null
    )
}

// ─── Level 3: Recovery ───────────────────────────────────────────────────────

@Composable
private fun Level3Recommendations(onStartBreathing: () -> Unit) {
    val coral = CORAL
    RecoCard(
        icon = Icons.Default.Air,
        color = coral,
        title = stringResource(R.string.reco_l3_t1),
        body = stringResource(R.string.reco_l3_b1),
        action = stringResource(R.string.reco_l3_a1),
        onAction = onStartBreathing
    )
    Spacer(Modifier.height(10.dp))
    RecoCard(
        icon = Icons.Default.FitnessCenter,
        color = coral,
        title = stringResource(R.string.reco_l3_t2),
        body = stringResource(R.string.reco_l3_b2),
        action = null, onAction = null
    )
    Spacer(Modifier.height(10.dp))
    RecoCard(
        icon = Icons.Default.FilterList,
        color = coral,
        title = stringResource(R.string.reco_l3_t3),
        body = stringResource(R.string.reco_l3_b3),
        action = null, onAction = null
    )
}

// ─── Level 4: Crisis ─────────────────────────────────────────────────────────

@Composable
private fun Level4Recommendations(onGrounding: () -> Unit, onSOS: () -> Unit) {
    // Command mode header
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CRIMSON.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CRIMSON.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(R.string.command_mode_active), fontWeight = FontWeight.Bold, fontSize = 15.sp,
                color = CRIMSON)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.command_mode_desc), fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            listOf(
                stringResource(R.string.crisis_step1),
                stringResource(R.string.crisis_step2),
                stringResource(R.string.crisis_step3),
                stringResource(R.string.crisis_step4)
            ).forEachIndexed { i, step ->
                Row(modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top) {
                    Text("${i + 1}.", fontWeight = FontWeight.Bold,
                        color = CRIMSON, fontSize = 14.sp, modifier = Modifier.width(22.dp))
                    Text(step, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground, lineHeight = 20.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    // Grounding button (large)
    Button(
        onClick = onGrounding,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CRIMSON)
    ) {
        Icon(Icons.Default.Psychology, null, tint = Color.White)
        Spacer(Modifier.width(10.dp))
        Text(stringResource(R.string.start_grounding), color = Color.White, fontWeight = FontWeight.Bold,
            fontSize = 16.sp)
    }

    Spacer(Modifier.height(10.dp))

    // SOS row
    OutlinedButton(
        onClick = onSOS,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CRIMSON)
    ) {
        Icon(Icons.Default.Phone, null, tint = CRIMSON, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.connect_trusted), color = CRIMSON, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Reusable Recommendation Card ────────────────────────────────────────────

@Composable
private fun RecoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    body: String,
    action: String?,
    onAction: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(Modifier.height(10.dp))
            Text(body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp)
            if (action != null && onAction != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Text(action, color = Color.White, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp)
                }
            }
        }
    }
}

// ─── Safety Valve Dialog ─────────────────────────────────────────────────────

@Composable
private fun SafetyValveDialog(onDismiss: () -> Unit, onBreath: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⚠️", fontSize = 36.sp) },
        title = {
            Text(stringResource(R.string.safety_valve_title),
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Text(
                stringResource(R.string.safety_valve_desc),
                textAlign = TextAlign.Center, lineHeight = 22.sp, fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onBreath,
                colors = ButtonDefaults.buttonColors(containerColor = CORAL)
            ) { Text(stringResource(R.string.breathe_with_me), color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.skip_for_now)) }
        }
    )
}

// ─── Grounding Screen (5-4-3-2-1) ────────────────────────────────────────────

@Composable
fun GroundingScreen(onComplete: () -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }

    val steps = listOf(
        Triple("👁", stringResource(R.string.grounding_step1_t), stringResource(R.string.grounding_step1_d)),
        Triple("🤚", stringResource(R.string.grounding_step2_t), stringResource(R.string.grounding_step2_d)),
        Triple("👂", stringResource(R.string.grounding_step3_t), stringResource(R.string.grounding_step3_d)),
        Triple("👃", stringResource(R.string.grounding_step4_t), stringResource(R.string.grounding_step4_d)),
        Triple("👅", stringResource(R.string.grounding_step5_t), stringResource(R.string.grounding_step5_d))
    )

    var current by remember { mutableStateOf(0) }
    val inputs = remember { mutableStateListOf<String>().also { repeat(5) { _ -> it.add("") } } }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.grounding_progress, current + 1, 5), fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            LinearProgressIndicator(
                progress = { (current + 1) / 5f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = CRIMSON
            )

            Spacer(Modifier.height(40.dp))

            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith
                            (slideOutVertically { -it } + fadeOut())
                }, label = "grounding"
            ) { idx ->
                val step = steps[idx]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(step.first, fontSize = 64.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(20.dp))
                    Text(step.second, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(step.third, fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center, lineHeight = 24.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = inputs[current],
                onValueChange = { inputs[current] = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(stringResource(R.string.grounding_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                },
                shape = RoundedCornerShape(14.dp),
                maxLines = 2
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (current < 4) current++ else onComplete()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (current < 4) CRIMSON else SAGE
                )
            ) {
                Text(
                    if (current < 4) stringResource(R.string.grounding_next) else stringResource(R.string.grounding_finish),
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
            }
        }
    }
}
