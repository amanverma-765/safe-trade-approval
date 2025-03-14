package com.webxela.sta.backend.utils

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.BodyType
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.springframework.http.codec.multipart.FilePart
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


fun generatePdfReport(
    templatePath: String,
    replacements: Map<String, String>,
    outputPath: String,
    imageReplacements: Map<String, String>
): ByteArray {
    // Process text and image replacements in paragraphs or table cells
    fun processRun(run: org.apache.poi.xwpf.usermodel.XWPFRun, paragraph: XWPFParagraph) {
        val runText = run.getText(0) ?: return

        // Check for image replacements
        for ((placeholder, imagePath) in imageReplacements) {
            if (runText.contains(placeholder)) {

                if (paragraph.partType != BodyType.TABLECELL) {
                    run.setText("\n\n", 0)
                } else {
                    run.setText("", 0)
                }

                FileInputStream(imagePath).use { imageStream ->
                    val (width, height) = calculatePreservedAspectRatioDimensions(imagePath, 100.0)
                    run.addPicture(
                        imageStream,
                        XWPFDocument.PICTURE_TYPE_JPEG,
                        imagePath,
                        width,
                        height
                    )
                }
                paragraph.spacingAfter = 100
                return
            }
        }

        // Process text replacements if no image was found
        if (replacements.keys.any { runText.contains(it) }) {
            var modifiedText = runText
            replacements.forEach { (placeholder, replacement) ->
                modifiedText = modifiedText.replace(placeholder, replacement)
            }
            run.setText(modifiedText, 0)
        }
    }

    return FileInputStream(templatePath).use { inputStream ->
        XWPFDocument(inputStream).use { document ->
            // Process paragraphs
            document.paragraphs.forEach { paragraph ->
                paragraph.runs.forEach { run ->
                    processRun(run, paragraph)
                }
            }

            // Process tables
            document.tables.forEach { table ->
                table.rows.forEach { row ->
                    row.tableCells.forEach { cell ->
                        cell.paragraphs.forEach { paragraph ->
                            paragraph.runs.forEach { run ->
                                processRun(run, paragraph)
                            }
                        }
                    }
                }
            }

            // Generate PDF output
            ByteArrayOutputStream().use { pdfOutput ->
                val options = PdfOptions.create()
                PdfConverter.getInstance().convert(document, pdfOutput, options)

                // Write to file
                FileOutputStream(outputPath).use { fileOutput ->
                    fileOutput.write(pdfOutput.toByteArray())
                }

                pdfOutput.toByteArray()
            }
        }
    }
}


// Helper function to calculate dimensions that preserve aspect ratio
private fun calculatePreservedAspectRatioDimensions(imagePath: String, targetWidth: Double): Pair<Int, Int> {
    val image = javax.imageio.ImageIO.read(File(imagePath))
    val aspectRatio = image.height.toDouble() / image.width.toDouble()

    val width = org.apache.poi.util.Units.toEMU(targetWidth)
    val height = org.apache.poi.util.Units.toEMU(targetWidth * aspectRatio)

    return Pair(width, height)
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