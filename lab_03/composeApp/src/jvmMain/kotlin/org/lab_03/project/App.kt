package org.lab_03.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomStep
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.themes.themeMinimal
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import javax.swing.JLabel
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun AllDistributionPlots() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { UniformDistributionView() }
        item { PoissonDistributionView() }
        item { ExponentialDistributionView() }
        item { NormalDistributionView() }
        item { ErlangDistributionView() }
    }
}

@Composable
fun UniformDistributionView() {
    var aText by remember { mutableStateOf("0.0") }
    var bText by remember { mutableStateOf("10.0") }
    var pdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var cdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    
    // Парсинг и валидация
    val a = aText.toDoubleOrNull()
    val b = bText.toDoubleOrNull()
    val isValid = a != null && b != null && b > a
    
    // пересчет графиков при изменении валидных параметров
    LaunchedEffect(a, b, isValid) {
        if (isValid) {
            val xRange = (a - 1.0)..(b + 1.0)
            withContext(Dispatchers.Default) {
                val (pdf, cdf) = calculatePoints(xRange, isDiscrete = false) { x ->
                    Pair(Distributions.uniformPDF(x, a, b), Distributions.uniformCDF(x, a, b))
                }
                pdfPoints = pdf
                cdfPoints = cdf
            }
        }
    }
    
    DistributionCard(
        title = "1. Равномерное распределение",
        parameterInputs = {
            ParameterTextField(label = "A", value = aText, onValueChange = { aText = it })
            ParameterTextField(label = "B", value = bText, onValueChange = { bText = it })
        },
        stats = {
            if (isValid) {
                StatsText("Мат. ожидание", Distributions.uniformMean(a, b))
                StatsText("Дисперсия", Distributions.uniformVariance(a, b))
            } else {
                Text("Ошибка: B должно быть больше A", color = MaterialTheme.colors.error)
            }
        },
        charts = {
            DistributionCharts(
                pdfFormula = """f(x) = \begin{cases} \frac{1}{b-a}, & a \le x \le b \\ 0, & \text{иначе} \end{cases}""",
                cdfFormula = """F(x) = \begin{cases} 0, & x \lt a \\ \frac{x-a}{b-a}, & a \le x \lt b \\ 1, & иначе \end{cases}""",
                pdfPoints = pdfPoints,
                cdfPoints = cdfPoints,
                isDiscrete = false
            )
        }
    )
}

@Composable
fun PoissonDistributionView() {
    var lambdaText by remember { mutableStateOf("4.0") }

    val lambda = lambdaText.toDoubleOrNull()
    val isValid = lambda != null && lambda > 0

    var pdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var cdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    LaunchedEffect(lambda, isValid) {
        if (isValid) {
            val xRange = 0.0..(lambda * 3 + 5) // Динамический диапазон
            withContext(Dispatchers.Default) {
                val (pdf, cdf) = calculatePoints(xRange, isDiscrete = true) { x ->
                    val k = x.toInt()
                    Pair(Distributions.poissonPMF(k, lambda), Distributions.poissonCDF(k, lambda))
                }
                pdfPoints = pdf
                cdfPoints = cdf
            }
        }
    }

    DistributionCard(
        title = "2. Пуассоновское распределение",
        parameterInputs = {
            ParameterTextField(label = "λ", value = lambdaText, onValueChange = { lambdaText = it })
        },
        stats = {
            if (isValid) {
                StatsText("Мат. ожидание", Distributions.poissonMean(lambda))
                StatsText("Дисперсия", Distributions.poissonVariance(lambda))
            } else {
                Text("Ошибка: λ должна быть > 0", color = MaterialTheme.colors.error)
            }
        },
        charts = {
            DistributionCharts(
                pdfFormula = """P(k) = \frac{\lambda^k e^{-\lambda}}{k!}, k = 0, 1, 2, ...""",
                cdfFormula = """F(k) = e^{-\lambda} \sum_{i=0}^{k} \frac{\lambda^i}{i!}, k = 0, 1, 2, ...""",
                pdfPoints = pdfPoints,
                cdfPoints = cdfPoints,
                isDiscrete = true
            )
        }
    )
}

@Composable
fun ExponentialDistributionView() {
    var lambdaText by remember { mutableStateOf("0.5") }
    val lambda = lambdaText.toDoubleOrNull()
    val isValid = lambda != null && lambda > 0

    var pdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var cdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    LaunchedEffect(lambda, isValid) {
        if (isValid) {
            val xRange = 0.0..(5.0 / lambda) // Динамический диапазон
            withContext(Dispatchers.Default) {
                val (pdf, cdf) = calculatePoints(xRange, isDiscrete = false) { x ->
                    Pair(Distributions.exponentialPDF(x, lambda), Distributions.exponentialCDF(x, lambda))
                }
                pdfPoints = pdf
                cdfPoints = cdf
            }
        }
    }

    DistributionCard(
        title = "3. Экспоненциальное распределение",
        parameterInputs = { ParameterTextField(label = "λ", value = lambdaText, onValueChange = { lambdaText = it }) },
        stats = {
            if (isValid) {
                StatsText("Мат. ожидание", Distributions.exponentialMean(lambda))
                StatsText("Дисперсия", Distributions.exponentialVariance(lambda))
            } else {
                Text("Ошибка: λ должна быть > 0", color = MaterialTheme.colors.error)
            }
        },
        charts = {
            DistributionCharts(
                pdfFormula = """f(x) = \begin{cases} \lambda e^{-\lambda x}, & x \ge 0 \\ 0, & \text{иначе} \end{cases}""",
                cdfFormula = """F(x) = \begin{cases} 1 - e^{-\lambda x}, & x \ge 0 \\ 0, & \text{иначе} \end{cases}""",
                pdfPoints = pdfPoints, cdfPoints = cdfPoints, isDiscrete = false
            )
        }
    )
}

@Composable
fun NormalDistributionView() {
    var muText by remember { mutableStateOf("0.0") }
    var sigmaText by remember { mutableStateOf("1.0") }

    val mu = muText.toDoubleOrNull()
    val sigma = sigmaText.toDoubleOrNull()
    val isValid = mu != null && sigma != null && sigma > 0

    var pdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var cdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    LaunchedEffect(mu, sigma, isValid) {
        if (isValid) {
            val xRange = (mu - 4 * sigma)..(mu + 4 * sigma) // Правило 4-х сигм
            withContext(Dispatchers.Default) {
                val (pdf, cdf) = calculatePoints(xRange, isDiscrete = false) { x ->
                    Pair(Distributions.normalPDF(x, mu, sigma), Distributions.normalCDF(x, mu, sigma))
                }
                pdfPoints = pdf
                cdfPoints = cdf
            }
        }
    }

    DistributionCard(
        title = "4. Нормальное распределение",
        parameterInputs = {
            ParameterTextField(label = "μ", value = muText, onValueChange = { muText = it })
            ParameterTextField(label = "σ", value = sigmaText, onValueChange = { sigmaText = it })
        },
        stats = {
            if (isValid) {
                StatsText("Мат. ожидание", Distributions.normalMean(mu, sigma))
                StatsText("Дисперсия", Distributions.normalVariance(mu, sigma))
            } else {
                Text("Ошибка: μ должна быть не пустой, σ должна быть > 0", color = MaterialTheme.colors.error)
            }
        },
        charts = {
            DistributionCharts(
                pdfFormula = """f(x) = \frac{1}{\sigma\sqrt{2\pi}} e^{ -\frac{1}{2}(\frac{x-\mu}{\sigma})^2 }""",
                cdfFormula = """F(x) = \frac{1}{\sigma\sqrt{2\pi}}\int_{-\infty}^{x} e^{ -\frac{1}{2}(\frac{t-\mu}{\sigma})^2}""",
                pdfPoints = pdfPoints, cdfPoints = cdfPoints, isDiscrete = false
            )
        }
    )
}

@Composable
fun ErlangDistributionView() {
    var kText by remember { mutableStateOf("3") }
    var lambdaText by remember { mutableStateOf("1.0") }

    val k = kText.toIntOrNull()
    val lambda = lambdaText.toDoubleOrNull()
    val isValid = k != null && k > 0 && lambda != null && lambda > 0

    var pdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var cdfPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    LaunchedEffect(k, lambda, isValid) {
        if (isValid) {
            val xRange = 0.0..(k / lambda + 5 * sqrt(k / lambda.pow(2))) // Динамический диапазон
            withContext(Dispatchers.Default) {
                val (pdf, cdf) = calculatePoints(xRange, isDiscrete = false) { x ->
                    Pair(Distributions.erlangPDF(x, k, lambda), Distributions.erlangCDF(x, k, lambda))
                }
                pdfPoints = pdf
                cdfPoints = cdf
            }
        }
    }

    DistributionCard(
        title = "5. Распределение Эрланга",
        parameterInputs = {
            ParameterTextField(label = "k (целое)", value = kText, onValueChange = { kText = it }, isIntegerOnly = true)
            ParameterTextField(label = "λ", value = lambdaText, onValueChange = { lambdaText = it })
        },
        stats = {
            if (isValid) {
                StatsText("Мат. ожидание", Distributions.erlangMean(k, lambda))
                StatsText("Дисперсия", Distributions.erlangVariance(k, lambda))
            } else {
                Text("Ошибка: k (целое) > 0, λ > 0", color = MaterialTheme.colors.error)
            }
        },
        charts = {
            DistributionCharts(
                pdfFormula = """f(x) = \begin{cases} \frac{\lambda^k x^{k-1} e^{-\lambda x}}{(k-1)!}, & x \ge 0 \\ 0, & \text{иначе} \end{cases}""",
                cdfFormula = """F(x) = \begin{cases} 1 - e^{-\lambda x} \sum_{n=0}^{k-1} \frac{(\lambda x)^n}{n!}, & x \ge 0 \\ 0, & \text{иначе} \end{cases}""",
                pdfPoints = pdfPoints, cdfPoints = cdfPoints, isDiscrete = false
            )
        }
    )
}

@Composable
fun DistributionCard(
    title: String,
    parameterInputs: @Composable RowScope.() -> Unit,
    stats: @Composable ColumnScope.() -> Unit,
    charts: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(0.8f)) {
            Text(title, style = MaterialTheme.typography.h5)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            parameterInputs()
        }
        Spacer(Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth(0.8f)) {
            stats()
        }
        Spacer(Modifier.height(16.dp))
        charts()
        if (title != "5. Распределение Эрланга")
            Divider(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                thickness = 2.dp,
                modifier = Modifier.fillMaxWidth()
            )
    }
}

@Composable
fun DistributionCharts(
    pdfFormula: String,
    cdfFormula: String,
    pdfPoints: List<Offset>,
    cdfPoints: List<Offset>,
    isDiscrete: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.weight(1f)) {
            LetsPlotChart(
                title = if (isDiscrete) "Probability Mass Function" else  "Probability Density Function",
                formula = pdfFormula,
                points = pdfPoints,
                isDiscrete = isDiscrete,
                xLabel = if (isDiscrete) "k" else "x",
                yLabel = if (isDiscrete) "P(k)" else "f(x)"
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            LetsPlotChart(
                title = "Cumulative Distribution Function",
                formula = cdfFormula,
                points = cdfPoints,
                isDiscrete = isDiscrete,
                xLabel = if (isDiscrete) "k" else "x",
                yLabel = "F(x)"
            )
        }
    }
}

@Composable
fun ParameterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isIntegerOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val regex = if (isIntegerOnly) {
                Regex("^\\d*$")
            } else {
                Regex("^-?\\d*\\.?\\d*$")
            }
            if (newValue.matches(regex)) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        modifier = Modifier.width(150.dp)
    )
}

@Composable
fun StatsText(label: String, value: Double) {
    val df = DecimalFormat("#.####")
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$label: ")
            }
            append(df.format(value))
        },
        fontSize = 16.sp
    )
}

fun calculatePoints(
    xRange: ClosedFloatingPointRange<Double>,
    isDiscrete: Boolean,
    calculator: (Double) -> Pair<Double, Double>
): Pair<List<Offset>, List<Offset>> {
    val steps = if (isDiscrete) (xRange.endInclusive - xRange.start).toInt().coerceAtLeast(1) else 400
    val calculatedPdfPoints = mutableListOf<Offset>()
    val calculatedCdfPoints = mutableListOf<Offset>()
    val stepSize = (xRange.endInclusive - xRange.start) / steps

    for (i in 0..steps) {
        val x = xRange.start + i * stepSize
        val (pdfValue, cdfValue) = calculator(x)
        calculatedPdfPoints.add(Offset(x.toFloat(), pdfValue.toFloat()))
        calculatedCdfPoints.add(Offset(x.toFloat(), cdfValue.toFloat()))
    }
    return Pair(calculatedPdfPoints, calculatedCdfPoints)
}

@Composable
fun LetsPlotChart(title: String, formula: String, points: List<Offset>, isDiscrete: Boolean, xLabel: String, yLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Text(title, style = MaterialTheme.typography.subtitle1)
        LatexFormula(formula, modifier = Modifier.height(80.dp).padding(vertical = 8.dp))
        Spacer(Modifier.height(8.dp))

        if (points.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                Text("Ожидание корректных параметров...")
            }
            return
        }

        val plotSpec = remember(points) {
            val data = mapOf("x" to points.map { it.x }, "y" to points.map { it.y })
            var plot = letsPlot(data) { x = "x"; y = "y" }
            plot += if (isDiscrete) geomStep() else geomLine()
            plot += labs(x = xLabel, y = yLabel)
            plot += themeMinimal()
            plot
        }

        PlotPanel(
            figure = plotSpec,
            modifier = Modifier.fillMaxWidth().height(300.dp),
            computationMessagesHandler = { messages -> messages.forEach(::println) }
        )
    }
}

@Composable
fun LatexFormula(formula: String, modifier: Modifier = Modifier) {
    val imageBitmap by produceState<ImageBitmap?>(null, formula) {
        withContext(Dispatchers.IO) {
            try {
                val texFormula = TeXFormula(formula)
                val icon = texFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20f)
                val image = BufferedImage(icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_ARGB)
                val g2 = image.createGraphics()
                icon.paintIcon(JLabel(), g2, 0, 0)
                g2.dispose()
                value = image.toComposeImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                value = null
            }
        }
    }
    if (imageBitmap != null) {
        Image(bitmap = imageBitmap!!, contentDescription = "Формула", modifier = modifier)
    } else {
        Text(formula, modifier = modifier)
    }
}
