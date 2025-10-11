import csv
import time
from enum import Enum
from dataclasses import dataclass
from typing import List, Optional, Tuple

@dataclass
class TableRow:
    table_value1: int
    table_value2: int
    table_value3: int
    algorithmic_value1: int
    algorithmic_value2: int
    algorithmic_value3: int
    user_value: int

class DigitSize(Enum):
    ONE_DIGIT = 1
    TWO_DIGIT = 2
    THREE_DIGIT = 3

class CustomRandomGenerator:
    def __init__(self):
        self._a = 1103515245
        self._c = 12345
        self._m = 2**31
        self._seed = int(time.time() * 1000)

    def _next_long(self):
        self._seed = (self._a * self._seed + self._c) % self._m
        return self._seed

    def next_int(self, min_val: int, max_val: int) -> int:
        if min_val >= max_val:
            raise ValueError("min_val must be less than max_val")
        range_size = max_val - min_val
        scaled = abs(self._next_long()) % range_size
        return min_val + scaled

class TableBasedGenerator:
    def __init__(self):
        self._table = [
            78, 23, 56, 89, 12, 45, 9, 67, 34, 91, 5, 58, 81, 2, 29, 72, 48, 19,
            64, 3, 88, 41, 14, 69, 31, 76, 53, 22, 95, 8, 50, 27, 84, 1, 44, 61,
            17, 92, 39, 4, 59, 21, 74, 33, 86, 11, 68, 25, 80, 47, 6, 63, 30, 83,
            16, 51, 96, 20, 73, 40, 7, 60, 28, 79, 42, 15, 66, 37, 94, 0, 49, 24,
            71, 32, 85, 10, 65, 36, 93, 18, 55, 26, 77, 46, 13, 52, 57, 82, 35,
            90, 43, 70, 75, 54, 87, 38
        ]
        self._table_size = len(self._table)
        self._current_index = int(time.time() * 1000) % self._table_size

    def _next_raw(self) -> int:
        jump_amount = self._table[self._current_index]
        self._current_index = (self._current_index + jump_amount) % self._table_size
        return self._table[self._current_index]

    def next_int(self, min_val: int, max_val: int) -> int:
        if min_val >= max_val:
            raise ValueError("min_val must be less than max_val")
        range_size = max_val - min_val
        scaled = abs(self._next_raw()) % range_size
        return min_val + scaled

lcg_generator = CustomRandomGenerator()
table_generator = TableBasedGenerator()

def algorithmic_generate_categorized(digits: DigitSize) -> int:
    ranges = {
        DigitSize.ONE_DIGIT: (1, 10),
        DigitSize.TWO_DIGIT: (10, 100),
        DigitSize.THREE_DIGIT: (100, 1000),
    }
    min_val, max_val = ranges[digits]
    return lcg_generator.next_int(min_val, max_val)

def table_generate_categorized(digits: DigitSize) -> int:
    ranges = {
        DigitSize.ONE_DIGIT: (1, 10),
        DigitSize.TWO_DIGIT: (10, 100),
        DigitSize.THREE_DIGIT: (100, 1000),
    }
    min_val, max_val = ranges[digits]
    return table_generator.next_int(min_val, max_val)

def apply_criterion(numbers: List[int], min_range: int, max_range: int) -> Optional[float]:
    if len(numbers) < 2: return None
    if len(set(numbers)) == 1: return 0.0

    n = float(len(numbers))
    range_size = float(max_range - min_range)
    if range_size == 0: return 0.0

    uniqueness_score = len(set(numbers)) / n
    
    jumps = [abs(b - a) for a, b in zip(numbers, numbers[1:])]
    actual_avg_distance = sum(jumps) / len(jumps)
    expected_avg_distance = range_size / 3.0
    
    distance_deviation = abs(actual_avg_distance - expected_avg_distance)
    distance_volatility_score = max(0.0, 1.0 - distance_deviation / expected_avg_distance) if expected_avg_distance > 0 else 0.0

    short_jump_threshold = range_size * 0.15
    medium_jump_threshold = range_size * 0.50
    short_jumps = sum(1 for j in jumps if j <= short_jump_threshold)
    medium_jumps = sum(1 for j in jumps if short_jump_threshold < j <= medium_jump_threshold)
    long_jumps = sum(1 for j in jumps if j > medium_jump_threshold)

    total_jumps = float(len(jumps))
    ideal_percent = 100.0 / 3.0
    short_dev = abs(short_jumps / total_jumps * 100 - ideal_percent)
    medium_dev = abs(medium_jumps / total_jumps * 100 - ideal_percent)
    long_dev = abs(long_jumps / total_jumps * 100 - ideal_percent)
    
    total_deviation = short_dev + medium_dev + long_dev
    transition_variety_score = max(0.0, 1.0 - total_deviation / 200.0)

    return (uniqueness_score * 0.35 +
            distance_volatility_score * 0.25 +
            transition_variety_score * 0.40)

def get_ranges_from_digit_size(digits: DigitSize) -> Tuple[int, int]:
    return {
        DigitSize.ONE_DIGIT: (1, 9),
        DigitSize.TWO_DIGIT: (10, 99),
        DigitSize.THREE_DIGIT: (100, 999),
    }[digits]

def read_csv(file_path: str) -> List[TableRow]:
    table_data = []
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = list(csv.reader(f))
            if len(lines) < 4: return []
            for parts in lines[2:-1]:
                if len(parts) >= 7:
                    table_data.append(TableRow(*[int(p.strip()) for p in parts[:7]]))
    except (IOError, ValueError):
        return []
    return table_data

def write_csv(file_path: str, data: List[TableRow], results: List[str]):
    with open(file_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['Табличный метод', '', '', 'Алгоритмический метод', '', '', 'Пользовательский ввод'])
        writer.writerow(['Одноразрядный', 'Двухразрядный', 'Трехразрядный', 'Одноразрядный', 'Двухразрядный', 'Трехразрядный', 'Ввод пользователя'])
        for row in data:
            writer.writerow(list(row.__dict__.values()))
        writer.writerow(['Критерий случайности'] + results)