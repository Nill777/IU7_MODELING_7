from PyQt6.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QPushButton, QTableWidget, 
    QTableWidgetItem, QHeaderView, QLabel, QSpinBox, QMessageBox, QFileDialog, 
    QDialog, QDialogButtonBox
)
from logic import *

class UserInputDialog(QDialog):
    def __init__(self, count, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Ввод пользовательских данных")
        self.setMinimumWidth(300)
        
        self.count = count
        self.current_step = 1
        self.numbers = []
        
        self.layout = QVBoxLayout(self)
        self.label = QLabel(f"Введите цифру #{self.current_step} из {self.count} (от 0 до 9):")
        self.spin_box = QSpinBox()
        self.spin_box.setRange(0, 9)
        
        self.buttons = QDialogButtonBox(QDialogButtonBox.StandardButton.Ok | QDialogButtonBox.StandardButton.Cancel)
        self.buttons.accepted.connect(self.next_step)
        self.buttons.rejected.connect(self.reject)
        
        self.layout.addWidget(self.label)
        self.layout.addWidget(self.spin_box)
        self.layout.addWidget(self.buttons)

    def next_step(self):
        self.numbers.append(self.spin_box.value())
        if self.current_step == self.count:
            self.accept()
        else:
            self.current_step += 1
            self.label.setText(f"Введите цифру #{self.current_step} из {self.count} (от 0 до 9):")
            self.spin_box.setValue(0)

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setGeometry(100, 100, 1000, 600)
        
        self.table_data = []
        self.file_path = "my_table.csv"
        
        central_widget = QWidget()
        main_layout = QVBoxLayout(central_widget)
        self.setCentralWidget(central_widget)

        controls_layout = QHBoxLayout()
        label_rows = QLabel("Количество строк (2-1000):")
        self.spin_rows = QSpinBox()
        self.spin_rows.setRange(2, 1000)
        self.btn_generate = QPushButton("Сгенерировать/Загрузить")
        self.btn_save = QPushButton("Сохранить в CSV")
        self.btn_save.setEnabled(False)
        
        controls_layout.addWidget(label_rows)
        controls_layout.addWidget(self.spin_rows)
        controls_layout.addStretch()
        controls_layout.addWidget(self.btn_generate)
        controls_layout.addWidget(self.btn_save)

        self.table_widget = QTableWidget()
        self.table_widget.setColumnCount(7)
        self.setup_table_headers()
        
        main_layout.addLayout(controls_layout)
        main_layout.addWidget(self.table_widget)

        self.btn_generate.clicked.connect(self.run_generation_flow)
        self.btn_save.clicked.connect(self.save_data)
        
    def setup_table_headers(self):
        sub_headers = ['Одноразрядный', 'Двухразрядный', 'Трехразрядный', 
                       'Одноразрядный', 'Двухразрядный', 'Трехразрядный', 'Ввод']
        self.table_widget.setHorizontalHeaderLabels(sub_headers)
        self.table_widget.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)

    def run_generation_flow(self):
        try:
            with open(self.file_path, 'r'):
                reply = QMessageBox.question(self, "Файл найден", 
                                             f"Найден файл '{self.file_path}'.\nХотите загрузить данные?",
                                             QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No)
                if reply == QMessageBox.StandardButton.Yes:
                    self.table_data = read_csv(self.file_path)
                    if not self.table_data:
                        QMessageBox.warning(self, "Ошибка", "Файл поврежден. Будет создана новая таблица.")
                        self.generate_new_data()
                    else:
                        self.update_ui_with_data()
                else:
                    self.generate_new_data()
        except FileNotFoundError:
            self.generate_new_data()
    
    def generate_new_data(self):
        rows_count = self.spin_rows.value()
        dialog = UserInputDialog(rows_count, self)
        if dialog.exec():
            user_numbers = dialog.numbers
            self.table_data = []
            for num in user_numbers:
                row = TableRow(
                    table_value1=table_generate_categorized(DigitSize.ONE_DIGIT),
                    table_value2=table_generate_categorized(DigitSize.TWO_DIGIT),
                    table_value3=table_generate_categorized(DigitSize.THREE_DIGIT),
                    algorithmic_value1=algorithmic_generate_categorized(DigitSize.ONE_DIGIT),
                    algorithmic_value2=algorithmic_generate_categorized(DigitSize.TWO_DIGIT),
                    algorithmic_value3=algorithmic_generate_categorized(DigitSize.THREE_DIGIT),
                    user_value=num
                )
                self.table_data.append(row)
            self.update_ui_with_data()
    
    def update_ui_with_data(self):
        self.populate_table()
        self.calculate_and_display_criteria()
        self.btn_save.setEnabled(True)

    def populate_table(self):
        self.table_widget.setRowCount(0)
        self.table_widget.setRowCount(len(self.table_data))
        for r, row_data in enumerate(self.table_data):
            for c, value in enumerate(row_data.__dict__.values()):
                self.table_widget.setItem(r, c, QTableWidgetItem(str(value)))
    
    def calculate_and_display_criteria(self):
        if not self.table_data: return

        sequences = [[] for _ in range(7)]
        for row in self.table_data:
            for i, val in enumerate(row.__dict__.values()):
                sequences[i].append(val)
        
        ranges = [
            get_ranges_from_digit_size(DigitSize.ONE_DIGIT), get_ranges_from_digit_size(DigitSize.TWO_DIGIT),
            get_ranges_from_digit_size(DigitSize.THREE_DIGIT), get_ranges_from_digit_size(DigitSize.ONE_DIGIT),
            get_ranges_from_digit_size(DigitSize.TWO_DIGIT), get_ranges_from_digit_size(DigitSize.THREE_DIGIT),
            (0, 9)
        ]

        self.results = []
        for seq, (min_r, max_r) in zip(sequences, ranges):
            score = apply_criterion(seq, min_r, max_r)
            self.results.append(f"{score:.4f}" if score is not None else "N/A")

        current_rows = self.table_widget.rowCount()
        self.table_widget.setRowCount(current_rows + 1)
        self.table_widget.setVerticalHeaderItem(current_rows, QTableWidgetItem("Критерий"))
        
        for c, res in enumerate(self.results):
            self.table_widget.setItem(current_rows, c, QTableWidgetItem(res))

    def save_data(self):
        path, _ = QFileDialog.getSaveFileName(self, "Сохранить файл", self.file_path, "CSV Files (*.csv)")
        if path:
            try:
                write_csv(path, self.table_data, self.results)
                QMessageBox.information(self, "Успех", f"Таблица сохранена в файл:\n{path}")
            except Exception as e:
                QMessageBox.critical(self, "Ошибка", f"Не удалось сохранить файл.\nОшибка: {e}")