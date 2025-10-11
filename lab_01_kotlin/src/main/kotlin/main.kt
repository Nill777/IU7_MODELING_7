import java.io.File
import kotlin.Int
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.random.Random

data class TableRow(
    val tableValue1: Int,
    val tableValue2: Int,
    val tableValue3: Int,
    val algorithmicValue1: Int,
    val algorithmicValue2: Int,
    val algorithmicValue3: Int,
    val userValue: Int
)

enum class DigitSize(val digits: Int) {
    ONE_DIGIT(1),
    TWO_DIGIT(2),
    THREE_DIGIT(3);
}

fun generateNewTable(): MutableList<TableRow> {
    val tableData = mutableListOf<TableRow>()
    val tableSize = getTableSizeFromUser()
    val userNumbers = getUserNumbers(tableSize)

    for (i in 0 until tableSize) {
        tableData.add(
            TableRow(
                tableValue1 = tableGenerateCategorized(DigitSize.ONE_DIGIT),
                tableValue2 = tableGenerateCategorized(DigitSize.TWO_DIGIT),
                tableValue3 = tableGenerateCategorized(DigitSize.THREE_DIGIT),
                algorithmicValue1 = algorithmicGenerateCategorized(DigitSize.ONE_DIGIT),
                algorithmicValue2 = algorithmicGenerateCategorized(DigitSize.TWO_DIGIT),
                algorithmicValue3 = algorithmicGenerateCategorized(DigitSize.THREE_DIGIT),
                userValue = userNumbers[i]
            )
        )
    }
    return tableData
}

fun getTableSizeFromUser(): Int {
    while (true) {
        print("Введите количество строк таблицы (от 2 до 1000): ")
        try {
            val size = readlnOrNull()?.toInt()
            if (size != null && size in 2..1000) {
                return size
            } else {
                println("Ошибка: число должно быть в диапазоне от 2 до 1000.")
            }
        } catch (e: NumberFormatException) {
            println("Ошибка: введите корректное число.")
        }
    }
}

fun getUserNumbers(count: Int): List<Int> {
    val numbers = mutableListOf<Int>()
    for (i in 1..count) {
        while (true) {
            print("Введите цифру #$i: ")
            try {
                val number = readlnOrNull()?.toInt()
                if (number != null && number in 0..9) {
                    numbers.add(number)
                    break
                } else {
                    println("Ошибка: необходима цифра [0, 9].")
                }
            } catch (e: NumberFormatException) {
                println("Ошибка: введите корректную цифру.")
            }
        }
    }
    return numbers
}

// Линейный конгруэнтный генератор
object CustomRandomGenerator {
    private const val A = 1103515245L
    private const val C = 12345L
    private const val M = 2147483648L // 2^31
    private var currentSeed = System.currentTimeMillis()

    private fun next(): Long {
        currentSeed = (A * currentSeed + C) % M
        return currentSeed
    }
    // [min, max)
    fun nextInt(min: Int, max: Int): Int {
        require(min < max) { "min must be less than max" }
        val rangeSize = max - min
        val scaled = (next().absoluteValue % rangeSize).toInt()
        return min + scaled
    }
}

fun algorithmicGenerateCategorized(digits: DigitSize): Int {
    return when (digits) {
        DigitSize.ONE_DIGIT -> CustomRandomGenerator.nextInt(1, 10)
        DigitSize.TWO_DIGIT -> CustomRandomGenerator.nextInt(10, 100)
        DigitSize.THREE_DIGIT -> CustomRandomGenerator.nextInt(100, 1000)
    }
}

object TableBasedGenerator {
    private const val TABLE_SIZE = 97

    private val table: IntArray = intArrayOf(
        78, 23, 56, 89, 12, 45, 9, 67, 34, 91, 5, 58, 81, 2, 29, 72, 48, 19,
        64, 3, 88, 41, 14, 69, 31, 76, 53, 22, 95, 8, 50, 27, 84, 1, 44, 61,
        17, 92, 39, 4, 59, 21, 74, 33, 86, 11, 68, 25, 80, 47, 6, 63, 30, 83,
        16, 51, 96, 20, 73, 40, 7, 60, 28, 79, 42, 15, 66, 37, 94, 0, 49, 24,
        71, 32, 85, 10, 65, 36, 93, 18, 55, 26, 77, 46, 13, 52, 57, 82, 35,
        90, 43, 70, 75, 54, 87, 38
    )

    private var currentIndex = (System.currentTimeMillis() % TABLE_SIZE).toInt()

    // сырое псевдослучайное число от 0 до TABLE_SIZE-1
    private fun next(): Int {
        // прыгнем вперед
        val jumpAmount = table[currentIndex]
        // новый индекс по кольцу
        currentIndex = (currentIndex + jumpAmount) % TABLE_SIZE
        return table[currentIndex]
    }

    fun nextInt(min: Int, max: Int): Int {
        require(min < max) { "min must be less than max" }
        val rangeSize = max - min
        val scaled = next().absoluteValue % rangeSize
        return min + scaled
    }
}

fun tableGenerateCategorized(digits: DigitSize): Int {
    return when (digits) {
        DigitSize.ONE_DIGIT -> TableBasedGenerator.nextInt(1, 10)
        DigitSize.TWO_DIGIT -> TableBasedGenerator.nextInt(10, 100)
        DigitSize.THREE_DIGIT -> TableBasedGenerator.nextInt(100, 1000)
    }
}

fun writeCsv(file: File, data: List<TableRow>, criterionResults: List<Double?>) {
    file.bufferedWriter().use { out ->
        out.write("Табличный метод,,,Алгоритмический метод,,,Пользовательский ввод\n")
        out.write("Одноразрядный,Двухразрядный,Трехразрядный,Одноразрядный,Двухразрядный,Трехразрядный,Одноразрядный\n")

        data.forEach { row ->
            out.write(
                "${row.tableValue1},${row.tableValue2},${row.tableValue3}," +
                        "${row.algorithmicValue1},${row.algorithmicValue2},${row.algorithmicValue3}," +
                        "${row.userValue}\n"
            )
        }

        out.write(criterionResults.joinToString(","))
        out.write("\n")
    }
}

fun readCsv(file: File): MutableList<TableRow> {
    val tableData = mutableListOf<TableRow>()
    val lines = file.readLines()

    if (lines.size < 4) {
        println("Предупреждение: файл '${file.name}' слишком мал для чтения.")
        return tableData
    }

    for (i in 2 until lines.size - 1) {
        val line = lines[i]
        if (line.isBlank()) {
            continue
        }

        val parts = line.split(',')
        if (parts.size >= 7) {
            try {
                // trim почистит пробелы
                val row = TableRow(
                    tableValue1 = parts[0].trim().toInt(),
                    tableValue2 = parts[1].trim().toInt(),
                    tableValue3 = parts[2].trim().toInt(),
                    algorithmicValue1 = parts[3].trim().toInt(),
                    algorithmicValue2 = parts[4].trim().toInt(),
                    algorithmicValue3 = parts[5].trim().toInt(),
                    userValue = parts[6].trim().toInt()
                )
                tableData.add(row)
            } catch (e: NumberFormatException) {
                println("Предупреждение: пропущена некорректная строка в файле: \"$line\"")
            }
        } else {
            println("Предупреждение: в строке неверное количество столбцов, строка пропущена: \"$line\"")
        }
    }
    return tableData
}

fun applyRandomnessCriterion(numbers: List<Int>, minRange: Int, maxRange: Int): Double? {
    if (numbers.size < 2) return null

    val uniqueCount = numbers.toSet().size
    if (uniqueCount == 1) return 0.0

    val n = numbers.size.toDouble()
    val rangeSize = (maxRange - minRange).toDouble()

    // насколько много в последовательности уникальных чисел.
    val uniquenessScore = uniqueCount / n

    // средний прыжок в данных с теоретически ожидаемым
    val jumps = numbers.zipWithNext { a, b -> abs(a - b) }
    val actualAvgDistance = jumps.average()
    val expectedAvgDistance = rangeSize / 3.0 // теоретическое среднее расстояние
    // чем меньше отклонение, тем выше балл
    val distanceDeviation = abs(actualAvgDistance - expectedAvgDistance)
    val distanceVolatilityScore = max(0.0, 1.0 - distanceDeviation / expectedAvgDistance)

    // баланс между короткими, средними и длинными прыжками
    val shortJumpThreshold = rangeSize * 0.15 // до 15% диапазона
    val mediumJumpThreshold = rangeSize * 0.50 // до 50% диапазона

    val shortJumps = jumps.count { it <= shortJumpThreshold }.toDouble()
    val mediumJumps = jumps.count { it > shortJumpThreshold && it <= mediumJumpThreshold }.toDouble()
    val longJumps = jumps.count { it > mediumJumpThreshold }.toDouble()

    val totalJumps = jumps.size.toDouble()
    val idealPercent = 100.0 / 3.0

    val shortDeviation = abs(shortJumps / totalJumps * 100 - idealPercent)
    val mediumDeviation = abs(mediumJumps / totalJumps * 100 - idealPercent)
    val longDeviation = abs(longJumps / totalJumps * 100 - idealPercent)
    // суммарное отклонение от идеального баланса (33% на каждую категорию)
    val totalDeviation = shortDeviation + mediumDeviation + longDeviation
    val transitionVarietyScore = max(0.0, 1.0 - totalDeviation / 100.0)

    val finalScore = (uniquenessScore * 0.35) +
            (distanceVolatilityScore * 0.25) +
            (transitionVarietyScore * 0.40)

    return finalScore
}

fun applyRandomnessCriterionDigitSize(numbers: List<Int>, digits: DigitSize): Double? {
    val (minRange, maxRange) = when (digits) {
        DigitSize.ONE_DIGIT -> 1 to 9
        DigitSize.TWO_DIGIT -> 10 to 99
        DigitSize.THREE_DIGIT -> 100 to 999
    }
    return applyRandomnessCriterion(numbers, minRange, maxRange)
}

fun main() {
    val fileName = "my_table.csv"
    val file = File(fileName)
    var tableData: MutableList<TableRow>

    if (file.exists()) {
        while (true) {
            print("Найден существующий файл '$fileName'. Использовать его? [Y/n]: ")
            val choice = readlnOrNull()?.trim()?.lowercase()
            if (choice == "y" || choice == "") {
                tableData = readCsv(file)
                if (tableData.isEmpty()) {
                    println("Таблица повреждена и будет вновь сгенерирована.")
                    tableData = generateNewTable()
                    break
                } else {
                    println("Данные загружены из файла.")
                    break
                }
            } else if (choice == "n") {
                println("Создание новой таблицы.")
                tableData = generateNewTable()
                break
            } else {
                println("Неверный ввод. Пожалуйста, введите 'y' или 'n'.")
            }
        }
    } else {
        tableData = generateNewTable()
    }

    val tableSeq1 = tableData.map { it.tableValue1 }
    val tableSeq2 = tableData.map { it.tableValue2 }
    val tableSeq3 = tableData.map { it.tableValue3 }
    val algoSeq1 = tableData.map { it.algorithmicValue1 }
    val algoSeq2 = tableData.map { it.algorithmicValue2 }
    val algoSeq3 = tableData.map { it.algorithmicValue3 }
    val userSeq = tableData.map { it.userValue }

    val results = listOf(
        applyRandomnessCriterionDigitSize(tableSeq1, DigitSize.ONE_DIGIT),
        applyRandomnessCriterionDigitSize(tableSeq2, DigitSize.TWO_DIGIT),
        applyRandomnessCriterionDigitSize(tableSeq3, DigitSize.THREE_DIGIT),
        applyRandomnessCriterionDigitSize(algoSeq1, DigitSize.ONE_DIGIT),
        applyRandomnessCriterionDigitSize(algoSeq2, DigitSize.TWO_DIGIT),
        applyRandomnessCriterionDigitSize(algoSeq3, DigitSize.THREE_DIGIT),
        applyRandomnessCriterion(userSeq, 0, 9)
    )

    writeCsv(file, tableData, results)

    println("\nТаблица успешно сохранена в файле '$fileName'.")
}