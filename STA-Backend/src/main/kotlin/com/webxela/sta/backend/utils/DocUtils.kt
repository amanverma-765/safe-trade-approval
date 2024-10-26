package com.webxela.sta.backend.utils

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.ByteArrayOutputStream
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
