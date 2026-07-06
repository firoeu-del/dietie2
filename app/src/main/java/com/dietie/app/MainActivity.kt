package com.dietie.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { DietieRoot() }
    }
}

@Composable
private fun DietieRoot() {
    val context = LocalContext.current
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    var darkMode by remember { mutableStateOf(loadDarkMode(context) ?: systemDark) }

    LaunchedEffect(darkMode) { saveDarkMode(context, darkMode) }

    DietieTheme(darkTheme = darkMode) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            DietieApp(
                darkMode = darkMode,
                onToggleTheme = { darkMode = !darkMode }
            )
        }
    }
}

@Composable
private fun DietieTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme(
            primary = Color(0xFFC7B6FF),
            onPrimary = Color(0xFF27115A),
            secondary = Color(0xFF9FD7FF),
            tertiary = Color(0xFF8CE5B0),
            background = Color(0xFF10131E),
            surface = Color(0xFF181B27),
            surfaceVariant = Color(0xFF252A38),
            primaryContainer = Color(0xFF4A2FA5),
            secondaryContainer = Color(0xFF113C61),
            tertiaryContainer = Color(0xFF164A35)
        )
        else -> androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF6A4BD4),
            secondary = Color(0xFF1F6FC2),
            tertiary = Color(0xFF2F9464),
            background = Color(0xFFF8F7FF),
            surface = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE7DEFF),
            secondaryContainer = Color(0xFFD6EBFF),
            tertiaryContainer = Color(0xFFD9F7E7)
        )
    }

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(12.dp),
        small = RoundedCornerShape(16.dp),
        medium = RoundedCornerShape(22.dp),
        large = RoundedCornerShape(30.dp),
        extraLarge = RoundedCornerShape(38.dp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        content = content
    )
}

@Composable
private fun DietieApp(darkMode: Boolean, onToggleTheme: () -> Unit) {
    val context = LocalContext.current

    var gender by rememberSaveable { mutableStateOf(Gender.Male) }
    var goalIndex by rememberSaveable { mutableIntStateOf(2) }
    var activityIndex by rememberSaveable { mutableIntStateOf(1) }
    var heightText by rememberSaveable { mutableStateOf("") }
    var weightText by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val savedMacro = remember { loadMacroPrefs(context) }
    var choPct by rememberSaveable { mutableIntStateOf(savedMacro.cho) }
    var proPct by rememberSaveable { mutableIntStateOf(savedMacro.pro) }
    var fatPct by rememberSaveable { mutableIntStateOf(savedMacro.fat) }

    var result by remember { mutableStateOf<DietResult?>(null) }
    var selectedSection by rememberSaveable { mutableStateOf(ResultSection.All) }
    var history by remember { mutableStateOf(loadHistory(context)) }

    LaunchedEffect(choPct, proPct, fatPct) {
        saveMacroPrefs(context, MacroPrefs(choPct, proPct, fatPct))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackground(darkMode = darkMode)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                TopBar(darkMode = darkMode, onToggleTheme = onToggleTheme)
            }

            item { HeroCard() }

            item {
                InputCard(
                    gender = gender,
                    onGenderChange = { gender = it },
                    goalIndex = goalIndex,
                    onGoalChange = { goalIndex = it },
                    activityIndex = activityIndex,
                    onActivityChange = { activityIndex = it },
                    heightText = heightText,
                    onHeightChange = { heightText = it.take(5) },
                    weightText = weightText,
                    onWeightChange = { weightText = it.take(5) },
                    errorMessage = errorMessage,
                    onCalculate = {
                        val height = heightText.toLatinNumberOrNull()
                        val weight = weightText.toLatinNumberOrNull()

                        if (height == null || height !in 120.0..220.0 || weight == null || weight !in 30.0..250.0) {
                            errorMessage = "لطفاً قد را بین ۱۲۰ تا ۲۲۰ سانتی‌متر و وزن را بین ۳۰ تا ۲۵۰ کیلوگرم وارد کن."
                            return@InputCard
                        }

                        val newResult = calculateDiet(
                            gender = gender,
                            heightCm = height,
                            weightKg = weight,
                            goal = goals[goalIndex],
                            activity = activityLevels[activityIndex]
                        )
                        result = newResult
                        selectedSection = ResultSection.All
                        errorMessage = null

                        val newItem = newResult.toHistoryItem(gender, height, weight, goals[goalIndex], activityLevels[activityIndex])
                        history = (listOf(newItem) + history).take(15)
                        saveHistory(context, history)
                    }
                )
            }

            item {
                AnimatedVisibility(visible = result == null) {
                    StarterTips()
                }
            }

            item {
                AnimatedVisibility(visible = result != null) {
                    ResultFilter(
                        selected = selectedSection,
                        onSelected = { selectedSection = it }
                    )
                }
            }

            result?.let { safeResult ->
                if (selectedSection == ResultSection.All || selectedSection == ResultSection.Stats) {
                    item { StatsSection(result = safeResult) }
                }
                if (selectedSection == ResultSection.All || selectedSection == ResultSection.Macros) {
                    item {
                        MacroSection(
                            targetCalories = safeResult.targetCalories,
                            choPct = choPct,
                            proPct = proPct,
                            fatPct = fatPct,
                            onChoChange = { choPct = it },
                            onProChange = { proPct = it },
                            onFatChange = { fatPct = it },
                            onReset = {
                                choPct = 53
                                proPct = 17
                                fatPct = 30
                            }
                        )
                    }
                }
                if (selectedSection == ResultSection.All || selectedSection == ResultSection.Units) {
                    item { UnitsSection(result = safeResult) }
                }
                if (selectedSection == ResultSection.All || selectedSection == ResultSection.Meals) {
                    item { MealsSection(result = safeResult) }
                }
                if (selectedSection == ResultSection.All || selectedSection == ResultSection.Guide) {
                    item { GuideSection() }
                    item { FormulaSection() }
                    item { DisclaimerCard() }
                }
            }

            if (history.isNotEmpty()) {
                item {
                    HistoryHeader(onClear = {
                        history = emptyList()
                        saveHistory(context, history)
                    })
                }
                items(history, key = { it.timestamp }) { item ->
                    HistoryRow(
                        item = item,
                        onDelete = {
                            history = history.filterNot { it.timestamp == item.timestamp }
                            saveHistory(context, history)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
    }
}

@Composable
private fun AppBackground(darkMode: Boolean) {
    val bg = if (darkMode) {
        Brush.linearGradient(
            listOf(Color(0xFF0E1220), Color(0xFF161022), Color(0xFF0D1714)),
            start = Offset.Zero,
            end = Offset(1000f, 1800f)
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0xFFEAF2FF), Color(0xFFF3ECFF), Color(0xFFE6F7F0)),
            start = Offset.Zero,
            end = Offset(1000f, 1800f)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = 10.dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(Color(0x556A4BD4))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 90.dp, y = 60.dp)
                .size(340.dp)
                .clip(CircleShape)
                .background(Color(0x4434A853))
        )
    }
}

@Composable
private fun TopBar(darkMode: Boolean, onToggleTheme: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "dietie",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-1.2).sp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "رژیم‌یار خوشگل و هوشمند شما",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = CircleShape,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)
        ) {
            IconButton(onClick = onToggleTheme) {
                Text(text = if (darkMode) "☀️" else "🌙", fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun HeroCard() {
    val shape = RoundedCornerShape(36.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.88f)
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), shape)
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f)
            ) {
                Text(
                    text = "✨ محاسبه‌گر هوشمند رژیم غذایی",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "کالری هدف، درشت‌مغذی‌ها و برنامه‌ی وعده‌ها را با طراحی Material 3 Expressive محاسبه کن.",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 32.sp
            )
            Text(
                text = "فرمول‌ها از نسخه‌ی وب خودت منتقل شده‌اند؛ فقط ظاهر، تجربه کاربری و حس اپ کاملاً Native و شیک‌تر شده.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun InputCard(
    gender: Gender,
    onGenderChange: (Gender) -> Unit,
    goalIndex: Int,
    onGoalChange: (Int) -> Unit,
    activityIndex: Int,
    onActivityChange: (Int) -> Unit,
    heightText: String,
    onHeightChange: (String) -> Unit,
    weightText: String,
    onWeightChange: (String) -> Unit,
    errorMessage: String?,
    onCalculate: () -> Unit
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            SectionTitle(emoji = "🧑‍🍳", title = "مشخصات شما")

            Text(
                text = "جنسیت",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = gender == Gender.Male,
                    onClick = { onGenderChange(Gender.Male) },
                    label = { Text("👨 آقا") }
                )
                FilterChip(
                    selected = gender == Gender.Female,
                    onClick = { onGenderChange(Gender.Female) },
                    label = { Text("👩 خانم") }
                )
            }

            PrettyDropdown(
                label = "🎯 هدف",
                selectedText = goals[goalIndex].title,
                options = goals.map { it.title },
                onSelected = onGoalChange
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrettyNumberField(
                    modifier = Modifier.weight(1f),
                    value = heightText,
                    onValueChange = onHeightChange,
                    label = "📏 قد",
                    placeholder = "مثلاً ۱۷۰"
                )
                PrettyNumberField(
                    modifier = Modifier.weight(1f),
                    value = weightText,
                    onValueChange = onWeightChange,
                    label = "⚖️ وزن",
                    placeholder = "مثلاً ۸۲"
                )
            }

            PrettyDropdown(
                label = "🔥 سطح فعالیت روزانه",
                selectedText = activityLevels[activityIndex].title,
                options = activityLevels.map { it.title },
                onSelected = onActivityChange
            )

            AnimatedVisibility(visible = errorMessage != null) {
                ErrorCard(text = errorMessage.orEmpty())
            }

            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("محاسبه کالری و برنامه غذایی", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun PrettyNumberField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
private fun PrettyDropdown(
    label: String,
    selectedText: String,
    options: List<String>,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = selectedText,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
                Text("⌄", fontSize = 18.sp)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.88f)
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                        onClick = {
                            onSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StarterTips() {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(emoji = "💡", title = "شروع سریع")
            Text(
                text = "قد، وزن، هدف و سطح فعالیت را وارد کن. dietie وزن ایده‌آل، وزن مبنای محاسبه، کالری هدف، واحدهای غذایی و وعده‌ها را همان‌جا نشان می‌دهد.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 25.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("بدون اینترنت") })
                AssistChip(onClick = {}, label = { Text("RTL") })
                AssistChip(onClick = {}, label = { Text("تاریخچه") })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultFilter(selected: ResultSection, onSelected: (ResultSection) -> Unit) {
    GlassCard(contentPadding = PaddingValues(12.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResultSection.entries.forEach { section ->
                FilterChip(
                    selected = selected == section,
                    onClick = { onSelected(section) },
                    label = { Text("${section.emoji} ${section.title}") }
                )
            }
        }
    }
}

@Composable
private fun StatsSection(result: DietResult) {
    SectionContainer(emoji = "📊", title = "نتایج محاسبه") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🏅",
                    title = "وزن ایده‌آل",
                    value = result.idealWeight.faKg(),
                    unit = "IBW"
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⚖️",
                    title = result.basisLabel,
                    value = result.basisWeight.faKg(),
                    unit = "مبنای انرژی"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🔥",
                    title = "کالری نگهدارنده",
                    value = result.maintenanceCalories.fa(),
                    unit = "کیلوکالری"
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⚡",
                    title = "کالری هدف",
                    value = result.targetCalories.fa(),
                    unit = "در روز",
                    highlighted = true
                )
            }
            NoteCard(text = "💡 ${result.basisNote}")
            if (result.floored) {
                WarningCard(text = "⚠️ کالری محاسبه‌شده کمتر از ۱۰۰۰ بود؛ برای حفظ ایمنی، برنامه روی حداقل ۱۰۰۰ کیلوکالری تنظیم شد.")
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    value: String,
    unit: String,
    highlighted: Boolean = false
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (highlighted) {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                        )
                    )
                }
            )
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f), shape)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "$emoji $title", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(text = unit, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MacroSection(
    targetCalories: Double,
    choPct: Int,
    proPct: Int,
    fatPct: Int,
    onChoChange: (Int) -> Unit,
    onProChange: (Int) -> Unit,
    onFatChange: (Int) -> Unit,
    onReset: () -> Unit
) {
    val sum = choPct + proPct + fatPct
    val macros = MacroResult(
        carbsGram = targetCalories * choPct / 100.0 / 4.0,
        proteinGram = targetCalories * proPct / 100.0 / 4.0,
        fatGram = targetCalories * fatPct / 100.0 / 9.0
    )

    SectionContainer(emoji = "⚡", title = "درشت‌مغذی‌های روزانه") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroDonut(
                    choPct = choPct,
                    proPct = proPct,
                    fatPct = fatPct,
                    modifier = Modifier.weight(0.9f)
                )
                Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniMacroCard("🍞", "کربوهیدرات", macros.carbsGram.fa(), "$choPct٪")
                    MiniMacroCard("🍗", "پروتئین", macros.proteinGram.fa(), "$proPct٪")
                    MiniMacroCard("🥑", "چربی", macros.fatGram.fa(), "$fatPct٪")
                }
            }

            MacroSlider("🍞 کربوهیدرات", choPct, onChoChange)
            MacroSlider("🍗 پروتئین", proPct, onProChange)
            MacroSlider("🥑 چربی", fatPct, onFatChange)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onReset, shape = RoundedCornerShape(20.dp)) { Text("↺ بازنشانی") }
                if (sum != 100) {
                    Text(
                        text = "جمع درصدها ${sum.fa()}٪ است؛ بهتر است ۱۰۰٪ باشد.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMacroCard(emoji: String, title: String, grams: String, pct: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("$emoji $title", style = MaterialTheme.typography.labelLarge)
                Text("$grams گرم", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Text(pct, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MacroSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text("${value.fa()}٪", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt().coerceIn(0, 100)) },
            valueRange = 0f..100f
        )
    }
}

@Composable
private fun MacroDonut(choPct: Int, proPct: Int, fatPct: Int, modifier: Modifier = Modifier) {
    val animatedCho by animateFloatAsState(targetValue = choPct.toFloat(), label = "cho")
    val animatedPro by animateFloatAsState(targetValue = proPct.toFloat(), label = "pro")
    val animatedFat by animateFloatAsState(targetValue = fatPct.toFloat(), label = "fat")
    val total = (animatedCho + animatedPro + animatedFat).coerceAtLeast(1f)
    val colors = listOf(Color(0xFFD5803B), Color(0xFFE56458), Color(0xFF78A7FF))

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val stroke = Stroke(width = size.minDimension * 0.14f, cap = StrokeCap.Round)
            val arcSize = Size(size.width, size.height)
            var start = -90f
            listOf(animatedCho, animatedPro, animatedFat).forEachIndexed { index, amount ->
                val sweep = amount / total * 360f
                drawArc(
                    color = colors[index],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset.Zero,
                    size = arcSize,
                    style = stroke
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(choPct + proPct + fatPct).fa()}٪", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            Text("تقسیم انرژی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnitsSection(result: DietResult) {
    SectionContainer(emoji = "🧩", title = "واحدهای غذایی روزانه", subtitle = "نزدیک‌ترین برنامه: حدود ${result.planCalories.fa()} کیلوکالری") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            result.totals.forEach { (group, unit) ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(group.argb).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(group.argb).copy(alpha = 0.22f))
                ) {
                    Text(
                        text = "${group.icon} ${group.title}  ${unit.fa()} واحد",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun MealsSection(result: DietResult) {
    SectionContainer(emoji = "🍽️", title = "برنامه وعده‌های غذایی") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            result.plan.forEach { (slot, meal) ->
                MealCard(slot = slot, meal = meal)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealCard(slot: MealSlot, meal: Meal) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${slot.icon} ${slot.title}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)) {
                    Text("≈ ${mealKcal(meal).fa()} کالری", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meal.forEach { (group, unit) ->
                    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(group.argb)))
                            Spacer(Modifier.width(6.dp))
                            Text("${group.title} × ${unit.fa()}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideSection() {
    SectionContainer(emoji = "📖", title = "هر واحد غذایی یعنی چقدر؟") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FoodGroup.entries.forEach { group ->
                ReferenceCard(group = group)
            }
        }
    }
}

@Composable
private fun ReferenceCard(group: FoodGroup) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, Color(group.argb).copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${group.icon} ${group.title}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text("هر واحد ≈ ${group.kcal.fa()} کیلوکالری", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            group.references.forEach { ref ->
                Text("• $ref", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
            }
        }
    }
}

@Composable
private fun FormulaSection() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    GlassCard(contentPadding = PaddingValues(0.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.20f)).padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ℹ️ فرمول‌های استفاده‌شده", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                OutlinedButton(onClick = { expanded = !expanded }, shape = RoundedCornerShape(18.dp)) {
                    Text(if (expanded) "بستن" else "نمایش")
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormulaLine("وزن ایده‌آل (IBW)", "آقایان: ۲۳ × قد²(متر) | خانم‌ها: ۲۲ × قد²(متر)")
                    FormulaLine("وزن تعدیل‌شده (AIBW)", "اگر وزن فعلی بیشتر از وزن ایده‌آل باشد: IBW + (وزن فعلی − IBW) ÷ ۳")
                    FormulaLine("انرژی نگهدارنده", "وزن مبنا × ۲۴ (خانم‌ها × ۰٫۹) × ۱٫۱ × ضریب فعالیت")
                    FormulaLine("کاهش/افزایش وزن", "کاهش ۳ کیلو: ۷۰۰− | کاهش ۴ کیلو: ۱۰۰۰− | افزایش ۲ کیلو: ۵۰۰+ | افزایش ۳ کیلو: ۷۰۰+")
                    FormulaLine("درشت‌مغذی‌ها", "کربوهیدرات و پروتئین: هر گرم ۴ کالری | چربی: هر گرم ۹ کالری")
                }
            }
        }
    }
}

@Composable
private fun FormulaLine(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, lineHeight = 23.sp)
    }
}

@Composable
private fun DisclaimerCard() {
    WarningCard(text = "این برنامه جنبه‌ی آموزشی و راهنمای کلی دارد و جایگزین مشاوره متخصص تغذیه نیست. در صورت بارداری، شیردهی، دیابت، بیماری کلیوی یا هر بیماری زمینه‌ای، حتماً با پزشک یا متخصص تغذیه مشورت کن.")
}

@Composable
private fun HistoryHeader(onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SectionTitle(emoji = "🕘", title = "تاریخچه محاسبات")
        OutlinedButton(onClick = onClear, shape = RoundedCornerShape(18.dp)) { Text("پاک کردن همه") }
    }
}

@Composable
private fun HistoryRow(item: HistoryItem, onDelete: () -> Unit) {
    GlassCard(contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.displayDate, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${item.genderIcon}  قد ${item.height.fa()} | وزن ${item.weight.fa()} | ${item.goalLabel}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("⚡ ${item.targetCalories.fa()} کیلوکالری در روز", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onDelete) { Text("✕") }
        }
    }
}

@Composable
private fun SectionContainer(
    emoji: String,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionTitle(emoji = emoji, title = title, subtitle = subtitle)
            content()
        }
    }
}

@Composable
private fun SectionTitle(emoji: String, title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "$emoji $title",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(contentPadding)) {
            content()
        }
    }
}

@Composable
private fun NoteCard(text: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.13f))
    ) {
        Text(modifier = Modifier.padding(14.dp), text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
    }
}

@Composable
private fun WarningCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f))
    ) {
        Text(modifier = Modifier.padding(14.dp), text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
    }
}

@Composable
private fun ErrorCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.22f))
    ) {
        Text(modifier = Modifier.padding(14.dp), text = "⚠️ $text", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
    }
}

private enum class Gender(val idealBmi: Double, val energyFactor: Double, val icon: String) {
    Male(23.0, 1.0, "👨"),
    Female(22.0, 0.9, "👩")
}

private data class Goal(val title: String, val deficit: Double, val shortLabel: String)
private data class ActivityLevel(val title: String, val factor: Double)

private val goals = listOf(
    Goal("تثبیت وزن (بدون کسر کالری)", 0.0, "تثبیت وزن"),
    Goal("کاهش ۳ کیلوگرم در ماه (−۷۰۰ کالری)", 700.0, "کاهش ۳ کیلو (−۷۰۰)"),
    Goal("کاهش ۴ کیلوگرم در ماه (−۱۰۰۰ کالری)", 1000.0, "کاهش ۴ کیلو (−۱۰۰۰)"),
    Goal("افزایش ۲ کیلوگرم در ماه (+۵۰۰ کالری)", -500.0, "افزایش ۲ کیلو (+۵۰۰)"),
    Goal("افزایش ۳ کیلوگرم در ماه (+۷۰۰ کالری)", -700.0, "افزایش ۳ کیلو (+۷۰۰)")
)

private val activityLevels = listOf(
    ActivityLevel("بی‌تحرک / استراحت — ضریب ۱٫۲", 1.2),
    ActivityLevel("فعالیت سبک — کار نشسته، پیاده‌روی کم — ضریب ۱٫۳", 1.3),
    ActivityLevel("فعالیت متوسط — ورزش منظم یا کار نیمه‌فعال — ضریب ۱٫۴", 1.4),
    ActivityLevel("فعالیت زیاد — کار بدنی یا ورزش سنگین — ضریب ۱٫۵", 1.5)
)

private enum class FoodGroup(
    val title: String,
    val icon: String,
    val kcal: Int,
    val argb: Long,
    val references: List<String>
) {
    Bread(
        "نان و غلات",
        "🍞",
        80,
        0xFFD5803B,
        listOf("یک کف دست نان سنگک، بربری یا تافتون (۳۰ گرم)", "۴ کف دست نان لواش", "نصف لیوان برنج یا ماکارونی پخته", "یک عدد سیب‌زمینی متوسط")
    ),
    Meat(
        "گوشت و جانشین‌ها",
        "🍗",
        55,
        0xFFE56458,
        listOf("۳۰ گرم گوشت قرمز، مرغ یا ماهی پخته", "یک عدد تخم‌مرغ", "۳۰ گرم پنیر", "نصف لیوان حبوبات پخته")
    ),
    Milk(
        "شیر و لبنیات",
        "🥛",
        120,
        0xFF5E9FE8,
        listOf("یک لیوان شیر کم‌چرب", "یک لیوان ماست کم‌چرب", "نصف لیوان کشک")
    ),
    Fruit(
        "میوه",
        "🍎",
        60,
        0xFFBF8EDA,
        listOf("یک عدد سیب، پرتقال یا هلوی متوسط", "نصف موز بزرگ", "یک لیوان هندوانه یا طالبی خردشده", "۳ عدد خرما")
    ),
    Veg(
        "سبزی",
        "🥦",
        25,
        0xFF46A171,
        listOf("یک لیوان سبزی خام", "نصف لیوان سبزی پخته", "یک عدد گوجه‌فرنگی متوسط")
    ),
    Fat(
        "چربی",
        "🫒",
        45,
        0xFFEAC26B,
        listOf("یک قاشق چای‌خوری روغن مایع یا روغن زیتون", "۵ عدد بادام یا فندق", "۱۰ عدد پسته", "یک قاشق غذاخوری تخمه")
    )
}

private enum class MealSlot(val title: String, val icon: String) {
    Breakfast("صبحانه", "🌅"),
    MorningSnack("میان‌وعده صبح", "🍵"),
    Lunch("ناهار", "🍽️"),
    EveningSnack("میان‌وعده عصر", "🍏"),
    Dinner("شام", "🌙"),
    NightSnack("میان‌وعده شب", "✨")
}

private enum class ResultSection(val title: String, val emoji: String) {
    All("همه", "✅"),
    Stats("نتایج", "📊"),
    Macros("درشت‌مغذی", "⚡"),
    Units("واحدها", "🧩"),
    Meals("وعده‌ها", "🍽️"),
    Guide("راهنما", "📖")
}

private typealias Meal = Map<FoodGroup, Int>
private typealias Plan = Map<MealSlot, Meal>

private data class DietResult(
    val idealWeight: Double,
    val basisWeight: Double,
    val basisLabel: String,
    val basisNote: String,
    val maintenanceCalories: Double,
    val targetCalories: Double,
    val floored: Boolean,
    val planKey: Int,
    val planCalories: Int,
    val plan: Plan,
    val totals: Map<FoodGroup, Int>
)

private data class MacroResult(val carbsGram: Double, val proteinGram: Double, val fatGram: Double)
private data class MacroPrefs(val cho: Int, val pro: Int, val fat: Int)
private data class HistoryItem(
    val timestamp: Long,
    val displayDate: String,
    val genderIcon: String,
    val height: Double,
    val weight: Double,
    val goalLabel: String,
    val targetCalories: Double
)

private fun meal(vararg units: Pair<FoodGroup, Int>): Meal = units.toMap()

private val plans: Map<Int, Plan> = mapOf(
    1000 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 1, FoodGroup.Meat to 1, FoodGroup.Fat to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Fruit to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 1, FoodGroup.Veg to 2, FoodGroup.Fat to 1),
        MealSlot.EveningSnack to meal(FoodGroup.Milk to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 1, FoodGroup.Meat to 1, FoodGroup.Veg to 1),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1)
    ),
    1200 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 1, FoodGroup.Fat to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Fruit to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 1, FoodGroup.Veg to 2, FoodGroup.Fat to 1),
        MealSlot.EveningSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 1, FoodGroup.Meat to 1, FoodGroup.Veg to 1, FoodGroup.Fat to 1),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1)
    ),
    1400 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 1, FoodGroup.Fat to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Fruit to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 2, FoodGroup.Veg to 2, FoodGroup.Fat to 2),
        MealSlot.EveningSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 1, FoodGroup.Veg to 2, FoodGroup.Fat to 1),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1)
    ),
    1600 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 1, FoodGroup.Fat to 1, FoodGroup.Fruit to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Bread to 1, FoodGroup.Fruit to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 2, FoodGroup.Veg to 2, FoodGroup.Fat to 2),
        MealSlot.EveningSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 2, FoodGroup.Veg to 2, FoodGroup.Fat to 2),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1)
    ),
    1800 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 2, FoodGroup.Milk to 1, FoodGroup.Meat to 1, FoodGroup.Fat to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Bread to 1, FoodGroup.Fruit to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 3, FoodGroup.Meat to 2, FoodGroup.Veg to 2, FoodGroup.Fat to 2),
        MealSlot.EveningSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 2, FoodGroup.Veg to 3, FoodGroup.Fat to 1),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 2)
    ),
    2000 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 3, FoodGroup.Milk to 1, FoodGroup.Meat to 1, FoodGroup.Fat to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Bread to 1, FoodGroup.Fruit to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 3, FoodGroup.Meat to 3, FoodGroup.Veg to 2, FoodGroup.Fat to 2),
        MealSlot.EveningSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 2, FoodGroup.Veg to 3, FoodGroup.Fat to 2),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 2)
    ),
    2200 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 3, FoodGroup.Milk to 1, FoodGroup.Meat to 1, FoodGroup.Fat to 1, FoodGroup.Fruit to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Bread to 1, FoodGroup.Fruit to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 3, FoodGroup.Meat to 3, FoodGroup.Veg to 2, FoodGroup.Fat to 2),
        MealSlot.EveningSnack to meal(FoodGroup.Bread to 1, FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 3, FoodGroup.Veg to 3, FoodGroup.Fat to 3),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 2)
    ),
    2500 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 3, FoodGroup.Milk to 1, FoodGroup.Meat to 2, FoodGroup.Fat to 1, FoodGroup.Fruit to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Bread to 2, FoodGroup.Fruit to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 4, FoodGroup.Meat to 3, FoodGroup.Veg to 3, FoodGroup.Fat to 3),
        MealSlot.EveningSnack to meal(FoodGroup.Bread to 1, FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 2, FoodGroup.Meat to 3, FoodGroup.Veg to 3, FoodGroup.Fat to 3),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 2)
    ),
    2800 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 3, FoodGroup.Milk to 1, FoodGroup.Meat to 2, FoodGroup.Fat to 2, FoodGroup.Fruit to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Bread to 2, FoodGroup.Fruit to 1, FoodGroup.Fat to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 4, FoodGroup.Meat to 4, FoodGroup.Veg to 3, FoodGroup.Fat to 3),
        MealSlot.EveningSnack to meal(FoodGroup.Bread to 1, FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 3, FoodGroup.Meat to 3, FoodGroup.Veg to 3, FoodGroup.Fat to 3),
        MealSlot.NightSnack to meal(FoodGroup.Milk to 1, FoodGroup.Fruit to 2)
    ),
    3000 to mapOf(
        MealSlot.Breakfast to meal(FoodGroup.Bread to 4, FoodGroup.Milk to 1, FoodGroup.Meat to 2, FoodGroup.Fat to 2, FoodGroup.Fruit to 1),
        MealSlot.MorningSnack to meal(FoodGroup.Bread to 2, FoodGroup.Fruit to 1, FoodGroup.Fat to 1),
        MealSlot.Lunch to meal(FoodGroup.Bread to 4, FoodGroup.Meat to 4, FoodGroup.Veg to 3, FoodGroup.Fat to 3),
        MealSlot.EveningSnack to meal(FoodGroup.Bread to 1, FoodGroup.Milk to 1, FoodGroup.Fruit to 1),
        MealSlot.Dinner to meal(FoodGroup.Bread to 3, FoodGroup.Meat to 3, FoodGroup.Veg to 3, FoodGroup.Fat to 3),
        MealSlot.NightSnack to meal(FoodGroup.Bread to 1, FoodGroup.Milk to 1, FoodGroup.Fruit to 2)
    )
)

private fun calculateDiet(
    gender: Gender,
    heightCm: Double,
    weightKg: Double,
    goal: Goal,
    activity: ActivityLevel
): DietResult {
    val heightM = heightCm / 100.0
    val idealWeight = gender.idealBmi * heightM * heightM

    val (basis, basisLabel, noteStart) = when {
        weightKg > idealWeight -> Triple(
            idealWeight + (weightKg - idealWeight) / 3.0,
            "وزن تعدیل‌شده",
            "چون وزن فعلی بیشتر از وزن ایده‌آل است، یک‌سوم اختلاف به وزن ایده‌آل اضافه و وزن تعدیل‌شده (AIBW) مبنای محاسبه شد."
        )
        weightKg < idealWeight -> Triple(
            weightKg,
            "وزن فعلی",
            "چون وزن فعلی کمتر از وزن ایده‌آل است، کالری بر اساس وزن فعلی محاسبه شد."
        )
        else -> Triple(
            idealWeight,
            "وزن ایده‌آل",
            "وزن در محدوده‌ی وزن ایده‌آل است و کالری بر اساس وزن ایده‌آل محاسبه شد."
        )
    }

    val maintenance = basis * 24.0 * gender.energyFactor * 1.1 * activity.factor
    var target = maintenance - goal.deficit
    val floored = target < 1000.0
    if (floored) target = 1000.0

    val planKey = plans.keys.minBy { abs(it - target) }
    val plan = plans.getValue(planKey)
    val planCalories = plan.values.sumOf { mealKcal(it) }
    val note = "$noteStart انرژی نگهدارنده = ${basis.fa()} × ۲۴${if (gender == Gender.Female) " × ۰٫۹" else ""} × ۱٫۱ × ${activity.factor.faOneDecimal()} = ${maintenance.fa()} کیلوکالری."

    return DietResult(
        idealWeight = idealWeight,
        basisWeight = basis,
        basisLabel = basisLabel,
        basisNote = note,
        maintenanceCalories = maintenance,
        targetCalories = target,
        floored = floored,
        planKey = planKey,
        planCalories = planCalories,
        plan = plan,
        totals = planTotals(plan)
    )
}

private fun DietResult.toHistoryItem(
    gender: Gender,
    height: Double,
    weight: Double,
    goal: Goal,
    activity: ActivityLevel
): HistoryItem {
    val now = System.currentTimeMillis()
    return HistoryItem(
        timestamp = now,
        displayDate = formatDateFa(now),
        genderIcon = gender.icon,
        height = height,
        weight = weight,
        goalLabel = "${goal.shortLabel} | ضریب ${activity.factor.faOneDecimal()}",
        targetCalories = targetCalories
    )
}

private fun mealKcal(meal: Meal): Int = meal.entries.sumOf { (group, unit) -> group.kcal * unit }

private fun planTotals(plan: Plan): Map<FoodGroup, Int> {
    val totals = linkedMapOf<FoodGroup, Int>()
    plan.values.forEach { meal ->
        meal.forEach { (group, unit) ->
            totals[group] = (totals[group] ?: 0) + unit
        }
    }
    return totals
}

private val faLocale = Locale("fa", "IR")
private val faNumber: NumberFormat = NumberFormat.getIntegerInstance(faLocale)

private fun Double.fa(): String = faNumber.format(roundToInt())
private fun Int.fa(): String = faNumber.format(this)
private fun Double.faKg(): String = "${fa()} کیلو"
private fun Double.faOneDecimal(): String = NumberFormat.getNumberInstance(faLocale).apply {
    maximumFractionDigits = 1
    minimumFractionDigits = 0
}.format(this)

private fun String.toLatinNumberOrNull(): Double? {
    val normalized = buildString {
        this@toLatinNumberOrNull.trim().forEach { ch ->
            append(
                when (ch) {
                    '۰', '٠' -> '0'
                    '۱', '١' -> '1'
                    '۲', '٢' -> '2'
                    '۳', '٣' -> '3'
                    '۴', '٤' -> '4'
                    '۵', '٥' -> '5'
                    '۶', '٦' -> '6'
                    '۷', '٧' -> '7'
                    '۸', '٨' -> '8'
                    '۹', '٩' -> '9'
                    '٫' -> '.'
                    else -> ch
                }
            )
        }
    }
    return normalized.toDoubleOrNull()
}

private fun prefs(context: Context) = context.getSharedPreferences("dietie_prefs", Context.MODE_PRIVATE)

private fun loadDarkMode(context: Context): Boolean? {
    val p = prefs(context)
    return if (p.contains("darkMode")) p.getBoolean("darkMode", false) else null
}

private fun saveDarkMode(context: Context, value: Boolean) {
    prefs(context).edit().putBoolean("darkMode", value).apply()
}

private fun loadMacroPrefs(context: Context): MacroPrefs = MacroPrefs(
    cho = prefs(context).getInt("macro_cho", 53),
    pro = prefs(context).getInt("macro_pro", 17),
    fat = prefs(context).getInt("macro_fat", 30)
)

private fun saveMacroPrefs(context: Context, macro: MacroPrefs) {
    prefs(context).edit()
        .putInt("macro_cho", macro.cho)
        .putInt("macro_pro", macro.pro)
        .putInt("macro_fat", macro.fat)
        .apply()
}

private fun loadHistory(context: Context): List<HistoryItem> {
    val raw = prefs(context).getString("history", "[]") ?: "[]"
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val timestamp = o.getLong("timestamp")
                add(
                    HistoryItem(
                        timestamp = timestamp,
                        displayDate = o.optString("displayDate", formatDateFa(timestamp)),
                        genderIcon = o.optString("genderIcon", "👤"),
                        height = o.optDouble("height"),
                        weight = o.optDouble("weight"),
                        goalLabel = o.optString("goalLabel"),
                        targetCalories = o.optDouble("targetCalories")
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun saveHistory(context: Context, history: List<HistoryItem>) {
    val array = JSONArray()
    history.forEach { item ->
        array.put(
            JSONObject()
                .put("timestamp", item.timestamp)
                .put("displayDate", item.displayDate)
                .put("genderIcon", item.genderIcon)
                .put("height", item.height)
                .put("weight", item.weight)
                .put("goalLabel", item.goalLabel)
                .put("targetCalories", item.targetCalories)
        )
    }
    prefs(context).edit().putString("history", array.toString()).apply()
}

private fun formatDateFa(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val datePart = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, faLocale).format(date)
    val timePart = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, faLocale).format(date)
    return "🕘 $datePart — $timePart"
}

@Preview(showBackground = true, locale = "fa-rIR")
@Composable
private fun DietiePreview() {
    DietieTheme(darkTheme = false) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HeroCard()
        }
    }
}
