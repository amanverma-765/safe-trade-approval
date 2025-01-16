package com.webxela.sta.backend.utils

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.http.codec.multipart.FilePart
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


fun generatePdfReport(
    templatePath: String,
    replacements: Map<String, String>,
    outputPath: String
): ByteArray {
    FileInputStream(templatePath).use { inputStream ->
        XWPFDocument(inputStream).use { document ->
            // Replace text in paragraphs
            document.paragraphs.forEach { paragraph ->
                paragraph.runs.forEach { run ->
                    run.getText(0)?.takeIf { text -> replacements.keys.any { text.contains(it) } }?.let { text ->
                        var modifiedText = text
                        replacements.forEach { (placeholder, replacement) ->
                            modifiedText = modifiedText.replace(placeholder, replacement)
                        }
                        run.setText(modifiedText, 0)
                    }
                }
            }

            // Replace text in tables
            document.tables.forEach { table ->
                table.rows.forEach { row ->
                    row.tableCells.forEach { cell ->
                        cell.paragraphs.forEach { paragraph ->
                            paragraph.runs.forEach { run ->
                                run.getText(0)?.takeIf { text -> replacements.keys.any { text.contains(it) } }
                                    ?.let { text ->
                                        var modifiedText = text
                                        replacements.forEach { (placeholder, replacement) ->
                                            modifiedText = modifiedText.replace(placeholder, replacement)
                                        }
                                        run.setText(modifiedText, 0)
                                    }
                            }
                        }
                    }
                }
            }

            val pdfOutputStream = ByteArrayOutputStream()
            val options = PdfOptions.create()

            pdfOutputStream.use { pdfOutput ->
                PdfConverter.getInstance().convert(document, pdfOutput, options)
                FileOutputStream(outputPath).use { fileOutput ->
                    fileOutput.write(pdfOutput.toByteArray())
                }
                return pdfOutput.toByteArray()
            }
        }
    }
}



fun isExcelFile(file: FilePart): Boolean {
    val filename = file.filename()
    return filename.endsWith(".xlsx", ignoreCase = true) || filename.endsWith(".xls", ignoreCase = true)
}

suspend fun extractNumbersFromExcel(file: FilePart): List<String> {
    val tempFile = withContext(Dispatchers.IO) {
        File.createTempFile("upload", ".xlsx")
    }

    withContext(Dispatchers.IO) {
        file.transferTo(tempFile).block()
    }

    val numbers = mutableListOf<String>()

    tempFile.inputStream().use { inputStream ->
        XSSFWorkbook(inputStream).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            for (row in sheet) {
                for (cell in row) {
                    when (cell.cellType) {
                        CellType.NUMERIC -> {
                            val cellValue = cell.numericCellValue.toLong().toString()
                            if (cellValue.length == 7) {
                                numbers.add(cellValue)
                            }
                        }
                        CellType.STRING -> {
                            val cellValue = cell.stringCellValue
                            val regex = Regex("\\b\\d{7}\\b")
                            regex.findAll(cellValue).forEach {
                                numbers.add(it.value)
                            }
                        }
                        else -> continue
                    }
                }
            }
        }
    }

    tempFile.delete()
    return numbers
}



fun extractNumbersFromPDF(savedFilePathList: List<String>): List<String> {
    val numbers = mutableListOf<String>()
    val classPattern = Regex("Class \\d+") // Pattern to match "Class {num}" anywhere on the page

    savedFilePathList.forEach { path ->
        Loader.loadPDF(File(path)).use { document ->
            val pdfStripper = PDFTextStripper()
            for (page in 1..document.numberOfPages) {
                pdfStripper.startPage = page
                pdfStripper.endPage = page

                val text = pdfStripper.getText(document).trim()
                if (classPattern.containsMatchIn(text)) { // Check if "Class {num}" is present
                    val regex = "\\d{7}".toRegex()
                    val extractedNumbers = regex.findAll(text).map { it.value }.toList()
                    numbers.addAll(extractedNumbers)
                }
            }
        }
    }
    return numbers
}