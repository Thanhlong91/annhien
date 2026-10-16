package com.annhien.womenshealth.core.reports

import android.content.Context
import com.annhien.womenshealth.domain.model.CycleSummary
import com.annhien.womenshealth.domain.model.JournalEntry
import com.annhien.womenshealth.domain.model.PeriodEpisode
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun exportHealthReportPdf(
        episodes: List<PeriodEpisode>,
        cycles: List<CycleSummary>,
        entries: List<JournalEntry>,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val writer = PdfWriter(outputFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Header
            document.add(Paragraph("AN NHIÊN — BÁO CÁO SỨC KHỎE PHỤ NỮ").setBold().setFontSize(18f))
            document.add(Paragraph("Báo cáo xuất ngoại tuyến từ ứng dụng An Nhiên").setFontSize(10f))
            document.add(Paragraph("Lưu ý: Báo cáo này dùng để theo dõi cá nhân hoặc tham khảo khi trao đổi với bác sĩ, không phải chẩn đoán y khoa.").setItalic().setFontSize(9f))
            document.add(Paragraph(" "))

            // Cycle Summary Section
            document.add(Paragraph("1. Tóm tắt chu kỳ gần đây").setBold().setFontSize(14f))
            if (cycles.isNotEmpty()) {
                val cycleTable = Table(floatArrayOf(150f, 150f, 100f, 100f))
                cycleTable.addHeaderCell("Bắt đầu")
                cycleTable.addHeaderCell("Kết thúc")
                cycleTable.addHeaderCell("Số ngày hành kinh")
                cycleTable.addHeaderCell("Độ dài chu kỳ")

                cycles.forEach { cycle ->
                    cycleTable.addCell(cycle.startDate.toString())
                    cycleTable.addCell(cycle.endDate.toString())
                    cycleTable.addCell("${cycle.periodLength} ngày")
                    cycleTable.addCell("${cycle.cycleLength} ngày")
                }
                document.add(cycleTable)
            } else {
                document.add(Paragraph("Chưa có đủ dữ liệu chu kỳ hoàn chỉnh để thống kê."))
            }

            document.add(Paragraph(" "))

            // Journal Records Section
            document.add(Paragraph("2. Nhật ký triệu chứng & ghi chú gần đây").setBold().setFontSize(14f))
            if (entries.isNotEmpty()) {
                val journalTable = Table(floatArrayOf(120f, 120f, 260f))
                journalTable.addHeaderCell("Ngày")
                journalTable.addHeaderCell("Loại nhật ký")
                journalTable.addHeaderCell("Ghi chú")

                entries.take(30).forEach { entry ->
                    journalTable.addCell(entry.eventDate.toString())
                    journalTable.addCell(entry.entryType.labelVi)
                    journalTable.addCell(entry.note ?: "—")
                }
                document.add(journalTable)
            } else {
                document.add(Paragraph("Không có bản ghi nhật ký trong khoảng thời gian đã chọn."))
            }

            document.close()
            outputFile
        }
    }

    suspend fun exportCyclesCsv(episodes: List<PeriodEpisode>, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val writer = CSVWriter(FileWriter(outputFile))
            val header = arrayOf("Mã kỳ", "Ngày bắt đầu", "Ngày kết thúc", "Đang diễn ra", "Dữ liệu không chắc chắn")
            writer.writeNext(header)

            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            episodes.forEach { episode ->
                writer.writeNext(
                    arrayOf(
                        episode.id,
                        episode.startDate.format(formatter),
                        episode.endDate?.format(formatter) ?: "",
                        if (episode.isOngoing) "Có" else "Không",
                        if (episode.isUncertain) "Có" else "Không"
                    )
                )
            }
            writer.close()
            outputFile
        }
    }
}
