package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
            val isDarkTheme = isDarkOverride ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                CalculatorScreen(
                    isDark = isDarkTheme,
                    onThemeToggle = {
                        isDarkOverride = !isDarkTheme
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    isDark: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showHistory by remember { mutableStateOf(false) }
    
    val displayScrollState = rememberScrollState()
    
    LaunchedEffect(uiState.expression) {
        displayScrollState.animateScrollTo(displayScrollState.maxValue)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWide = maxWidth > 600.dp
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isWide) {
                            Modifier
                                .widthIn(max = 600.dp)
                                .align(Alignment.Center)
                                .padding(24.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        } else {
                            Modifier
                        }
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Action Row (Artistic Flair representation)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Brand indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.secondary,
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = "CALCULATOR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // Right actions (Theme selector and history)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onThemeToggle,
                                modifier = Modifier
                                    .testTag("theme_toggle_button")
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isDark) SunIcon else MoonIcon,
                                    contentDescription = if (isDark) "Светлая тема" else "Темная тема",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { showHistory = true },
                                modifier = Modifier
                                    .testTag("history_button")
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = HistoryIcon,
                                    contentDescription = "История",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Display sector
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(displayScrollState),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.expression.ifEmpty { "0" },
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Light,
                                    color = if (uiState.expression.isEmpty()) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier
                                    .testTag("expression_display")
                                    .padding(vertical = 4.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedVisibility(
                            visible = uiState.realtimePreview.isNotEmpty() || uiState.result.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val displayText = when {
                                uiState.realtimePreview.isNotEmpty() -> "= ${uiState.realtimePreview}"
                                uiState.result == "Divide by zero" -> "Деление на ноль"
                                uiState.result.isNotEmpty() -> uiState.result
                                else -> ""
                            }
                            
                            val displayColor = if (uiState.realtimePreview.isNotEmpty()) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }

                            val displaySize = if (uiState.realtimePreview.isNotEmpty()) {
                                MaterialTheme.typography.headlineLarge
                            } else {
                                MaterialTheme.typography.displaySmall
                            }

                            Text(
                                text = displayText,
                                style = displaySize.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = displayColor
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .testTag("result_display")
                                    .padding(vertical = 4.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Keypad overlapping Container (rounded-t-40px from theme spec)
                    Column(
                        modifier = Modifier
                            .weight(2.0f)
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        val buttons = listOf(
                            listOf("AC", "⌫", "%", "÷"),
                            listOf("7", "8", "9", "×"),
                            listOf("4", "5", "6", "−"),
                            listOf("1", "2", "3", "+"),
                            listOf("+/-", "0", ".", "=")
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            buttons.forEach { rowButtons ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    rowButtons.forEach { key ->
                                        val isOperator = key in listOf("÷", "×", "−", "+")
                                        val isControl = key in listOf("AC", "C", "⌫", "%", "+/-")
                                        val isEquals = key == "="
                                        
                                        val (containerColor, textColor) = when {
                                            isEquals -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                                            isOperator -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                                            isControl -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                                            else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                                        }

                                        val buttonLabel = if (key == "AC" && uiState.expression.isNotEmpty()) {
                                            "C"
                                        } else {
                                            key
                                        }

                                        // Apply 24dp rounding squircle for equals button as requested by aesthetic
                                        val buttonShape = if (isEquals) {
                                            RoundedCornerShape(24.dp)
                                        } else {
                                            CircleShape
                                        }

                                        CalculatorButton(
                                            text = if (buttonLabel != "⌫") buttonLabel else null,
                                            icon = if (buttonLabel == "⌫") BackspaceIcon else null,
                                            textColor = textColor,
                                            containerColor = containerColor,
                                            shape = buttonShape,
                                            onClick = { viewModel.onKeyPress(buttonLabel) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_${
                                                    when (key) {
                                                        "+" -> "plus"
                                                        "−" -> "minus"
                                                        "×" -> "mult"
                                                        "÷" -> "div"
                                                        "=" -> "equals"
                                                        "⌫" -> "backspace"
                                                        "%" -> "percent"
                                                        "+/-" -> "plus_minus"
                                                        "AC" -> "ac"
                                                        "." -> "dot"
                                                        else -> key
                                                    }
                                                }")
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Home Indicator layout decoration (from theme spec)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(128.dp)
                                    .height(4.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showHistory) {
        ModalBottomSheet(
            onDismissRequest = { showHistory = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentWindowInsets = { WindowInsets.navigationBars }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "История вычислений",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    
                    if (uiState.history.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearHistory() }
                        ) {
                            Text(
                                text = "Очистить",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (uiState.history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = HistoryIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Нет недавних вычислений",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .testTag("history_list"),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.history, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.selectHistoryItem(item)
                                        showHistory = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = item.expression,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "= ${item.result}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String? = null,
    icon: ImageVector? = null,
    textColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = CircleShape
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Smooth press scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ButtonPressScale"
    )
    
    // Transparent color layer blending on press
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isPressed) {
            containerColor.copy(alpha = 0.82f)
        } else {
            containerColor
        },
        animationSpec = tween(durationMillis = 80),
        label = "ButtonContainerColor"
    )

    Box(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f) // Maintain precise circular constraints
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(animatedContainerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = textColor
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Custom handcraft Vector representations for clock / history
val HistoryIcon: ImageVector
    get() = ImageVector.Builder(
        name = "History",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(11.99f, 4f)
            curveTo(7.58f, 4f, 4f, 7.58f, 4f, 12f)
            curveTo(4f, 16.42f, 7.58f, 20f, 11.99f, 20f)
            curveTo(16.4f, 20f, 20f, 16.42f, 20f, 12f)
            curveTo(20f, 7.58f, 16.4f, 4f, 11.99f, 4f)
            close()
        }
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 8f)
            lineTo(12f, 12f)
            lineTo(15f, 14f)
        }
    }.build()

// Custom handcraft Vector representation for darkmode sun
val SunIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Sun",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f
        ) {
            moveTo(12f, 8f)
            curveTo(14.21f, 8f, 16f, 9.79f, 16f, 12f)
            curveTo(16f, 14.21f, 14.21f, 16f, 12f, 16f)
            curveTo(9.79f, 16f, 8f, 14.21f, 8f, 12f)
            curveTo(8f, 9.79f, 9.79f, 8f, 12f, 8f)
            close()
        }
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 2f); lineTo(12f, 4f)
            moveTo(12f, 20f); lineTo(12f, 22f)
            moveTo(2f, 12f); lineTo(4f, 12f)
            moveTo(20f, 12f); lineTo(22f, 12f)
            moveTo(4.93f, 4.93f); lineTo(6.34f, 6.34f)
            moveTo(17.66f, 17.66f); lineTo(19.07f, 19.07f)
            moveTo(4.93f, 19.07f); lineTo(6.34f, 17.66f)
            moveTo(17.66f, 4.93f); lineTo(19.07f, 6.34f)
        }
    }.build()

// Custom handcraft Vector representation for lightmode moon
val MoonIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Moon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 12.79f)
            curveTo(20.89f, 12.8f, 20.78f, 12.8f, 20.67f, 12.8f)
            curveTo(15.72f, 12.8f, 11.7f, 8.78f, 11.7f, 3.83f)
            curveTo(11.7f, 3.39f, 11.73f, 2.96f, 11.8f, 2.53f)
            curveTo(6.86f, 3.14f, 3f, 7.35f, 3f, 12.5f)
            curveTo(3f, 17.75f, 7.25f, 22f, 12.5f, 22f)
            curveTo(17.65f, 22f, 21.86f, 18.14f, 21.47f, 13.2f)
            close()
        }
    }.build()

// Custom handcraft Vector representation for Backspace symbol
val BackspaceIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Backspace",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(22f, 19f)
            lineTo(9f, 19f)
            lineTo(2f, 12f)
            lineTo(9f, 5f)
            lineTo(22f, 5f)
            close()
        }
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 9f); lineTo(18f, 15f)
            moveTo(18f, 9f); lineTo(12f, 15f)
        }
    }.build()
