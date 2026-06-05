package com.ielts.pdfservice.service;

import com.ielts.pdfservice.dto.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfGeneratorService {

    // Brand colors
    private static final DeviceRgb COLOR_PRIMARY    = new DeviceRgb(37, 99, 235);   // blue-600
    private static final DeviceRgb COLOR_SUCCESS    = new DeviceRgb(22, 163, 74);   // green-600
    private static final DeviceRgb COLOR_WARNING    = new DeviceRgb(202, 138, 4);   // yellow-600
    private static final DeviceRgb COLOR_DANGER     = new DeviceRgb(220, 38, 38);   // red-600
    private static final DeviceRgb COLOR_HEADER_BG  = new DeviceRgb(30, 58, 138);   // blue-900
    private static final DeviceRgb COLOR_SECTION_BG = new DeviceRgb(239, 246, 255); // blue-50
    private static final DeviceRgb COLOR_LIGHT_GRAY = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb COLOR_BORDER     = new DeviceRgb(203, 213, 225);
    private static final DeviceRgb COLOR_TEXT_MUTED = new DeviceRgb(100, 116, 139);

    public byte[] generateReport(ReportRequest req) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(0, 0, 36, 0);

        PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        AiEvaluationDto ai = req.getAiEvaluation();
        TeacherReviewDto tr = req.getTeacherReview();

        double overallScore = resolveOverall(ai, tr);
        double fluency      = resolveScore(tr != null ? tr.getAdjustedFluency() : null,    ai != null && ai.getFluencyCoherence()  != null ? ai.getFluencyCoherence().getScore()  : null);
        double lexical      = resolveScore(tr != null ? tr.getAdjustedLexical()  : null,   ai != null && ai.getLexicalResource()  != null ? ai.getLexicalResource().getScore()   : null);
        double grammar      = resolveScore(tr != null ? tr.getAdjustedGrammar()  : null,   ai != null && ai.getGrammar()          != null ? ai.getGrammar().getScore()           : null);
        double pronunciation = resolveScore(tr != null ? tr.getPronunciationScore() : null, ai != null && ai.getPronunciation()   != null ? ai.getPronunciation().getScore()     : null);

        // ── HEADER ──────────────────────────────────────────────────
        addHeader(doc, fontBold, fontRegular, req);

        // ── OVERALL BAND ─────────────────────────────────────────────
        addOverallBand(doc, fontBold, fontRegular, overallScore, tr != null);

        // ── SCORE BREAKDOWN TABLE ────────────────────────────────────
        doc.add(new Paragraph("Score Breakdown")
                .setFont(fontBold).setFontSize(13)
                .setFontColor(COLOR_PRIMARY)
                .setMarginLeft(36).setMarginRight(36).setMarginTop(16).setMarginBottom(8));

        addScoreTable(doc, fontBold, fontRegular, fluency, lexical, grammar, pronunciation);

        // ── CRITERIA DETAIL ──────────────────────────────────────────
        if (ai != null) {
            doc.add(new Paragraph("Detailed Feedback")
                    .setFont(fontBold).setFontSize(13)
                    .setFontColor(COLOR_PRIMARY)
                    .setMarginLeft(36).setMarginRight(36).setMarginTop(16).setMarginBottom(4));

            addCriteriaDetail(doc, fontBold, fontRegular, "Fluency & Coherence", fluency, ai.getFluencyCoherence(), false);
            addCriteriaDetail(doc, fontBold, fontRegular, "Lexical Resource", lexical, ai.getLexicalResource(), false);
            addCriteriaDetail(doc, fontBold, fontRegular, "Grammatical Range & Accuracy", grammar, ai.getGrammar(), false);
            addCriteriaDetail(doc, fontBold, fontRegular, "Pronunciation", pronunciation, ai.getPronunciation(), true);
        }

        // ── KEY MISTAKES & SUGGESTIONS ───────────────────────────────
        if (ai != null) {
            addListSection(doc, fontBold, fontRegular, "Key Mistakes", ai.getKeyMistakes(), COLOR_DANGER);
            addListSection(doc, fontBold, fontRegular, "Improvement Suggestions", ai.getImprovementSuggestions(), COLOR_SUCCESS);
        }

        // ── TRANSCRIPT ───────────────────────────────────────────────
        if (req.getTranscript() != null && !req.getTranscript().isBlank()) {
            addTranscript(doc, fontBold, fontRegular, req.getTranscript());
        }

        // ── TEACHER REVIEW ───────────────────────────────────────────
        if (tr != null) {
            addTeacherReview(doc, fontBold, fontRegular, tr);
        }

        // ── FOOTER ───────────────────────────────────────────────────
        addFooter(doc, fontRegular);

        doc.close();
        return out.toByteArray();
    }

    // ── HEADER ──────────────────────────────────────────────────────
    private void addHeader(Document doc, PdfFont bold, PdfFont regular, ReportRequest req) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setBackgroundColor(COLOR_HEADER_BG)
                .setPadding(0).setMargin(0)
                .setBorder(Border.NO_BORDER);

        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(28).setPaddingBottom(20)
                .setPaddingLeft(36).setPaddingRight(36);

        cell.add(new Paragraph("IELTS Speaking Assessment Report")
                .setFont(bold).setFontSize(20)
                .setFontColor(ColorConstants.WHITE)
                .setMarginBottom(4));

        String partLabel = formatPart(req.getQuestionPart());
        cell.add(new Paragraph(partLabel + "  ·  " + formatDate(req.getSubmittedAt()))
                .setFont(regular).setFontSize(10)
                .setFontColor(new DeviceRgb(186, 230, 253))
                .setMarginBottom(12));

        cell.add(new Paragraph("Student: " + safe(req.getStudentName()))
                .setFont(bold).setFontSize(11)
                .setFontColor(ColorConstants.WHITE)
                .setMarginBottom(4));

        String qText = safe(req.getQuestionText());
        if (qText.length() > 180) qText = qText.substring(0, 177) + "...";
        cell.add(new Paragraph("Question: \"" + qText + "\"")
                .setFont(regular).setFontSize(10)
                .setFontColor(new DeviceRgb(186, 230, 253)));

        header.addCell(cell);
        doc.add(header);
    }

    // ── OVERALL BAND ─────────────────────────────────────────────────
    private void addOverallBand(Document doc, PdfFont bold, PdfFont regular, double score, boolean hasTeacherReview) {
        DeviceRgb scoreColor = scoreColor(score);
        Table t = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setBackgroundColor(COLOR_SECTION_BG)
                .setBorder(new SolidBorder(COLOR_BORDER, 1))
                .setMarginLeft(36).setMarginRight(36).setMarginTop(20);

        // Score circle cell
        Cell scoreCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(20);
        scoreCell.add(new Paragraph(String.format("%.1f", score))
                .setFont(bold).setFontSize(48)
                .setFontColor(scoreColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0));
        scoreCell.add(new Paragraph("/ 9.0")
                .setFont(regular).setFontSize(11)
                .setFontColor(COLOR_TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER));
        t.addCell(scoreCell);

        // Info cell
        Cell infoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(COLOR_BORDER, 1))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(20);
        infoCell.add(new Paragraph("Overall Band Score")
                .setFont(bold).setFontSize(14)
                .setFontColor(COLOR_PRIMARY)
                .setMarginBottom(6));
        infoCell.add(new Paragraph(bandDescriptor(score))
                .setFont(bold).setFontSize(18)
                .setFontColor(scoreColor)
                .setMarginBottom(8));
        infoCell.add(new Paragraph(bandDescription(score))
                .setFont(regular).setFontSize(10)
                .setFontColor(COLOR_TEXT_MUTED)
                .setMarginBottom(8));

        String badge = hasTeacherReview ? "Teacher Reviewed" : "AI Evaluated";
        DeviceRgb badgeColor = hasTeacherReview ? COLOR_SUCCESS : COLOR_PRIMARY;
        infoCell.add(new Paragraph("  " + badge + "  ")
                .setFont(bold).setFontSize(9)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(badgeColor)
                .setPaddingLeft(6).setPaddingRight(6)
                .setPaddingTop(3).setPaddingBottom(3));
        t.addCell(infoCell);

        doc.add(t);
    }

    // ── SCORE TABLE ──────────────────────────────────────────────────
    private void addScoreTable(Document doc, PdfFont bold, PdfFont regular,
                               double fluency, double lexical, double grammar, double pronunciation) {
        float[] cols = {3, 1, 4};
        Table t = new Table(UnitValue.createPercentArray(cols))
                .useAllAvailableWidth()
                .setMarginLeft(36).setMarginRight(36)
                .setBorder(new SolidBorder(COLOR_BORDER, 1));

        // Header row
        addTableHeaderCell(t, bold, "Criterion");
        addTableHeaderCell(t, bold, "Score");
        addTableHeaderCell(t, bold, "Performance Bar");

        addScoreRow(t, bold, regular, "Fluency & Coherence",        fluency);
        addScoreRow(t, bold, regular, "Lexical Resource",           lexical);
        addScoreRow(t, bold, regular, "Grammatical Range & Accuracy", grammar);
        addScoreRow(t, bold, regular, "Pronunciation",              pronunciation);

        doc.add(t);
    }

    private void addTableHeaderCell(Table t, PdfFont bold, String text) {
        t.addHeaderCell(new Cell()
                .setBackgroundColor(COLOR_PRIMARY)
                .setBorder(Border.NO_BORDER)
                .setPadding(8)
                .add(new Paragraph(text).setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE)));
    }

    private void addScoreRow(Table t, PdfFont bold, PdfFont regular, String name, double score) {
        DeviceRgb color = scoreColor(score);

        t.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setPadding(8)
                .add(new Paragraph(name).setFont(bold).setFontSize(10)));

        t.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(String.format("%.1f", score)).setFont(bold).setFontSize(13).setFontColor(color)));

        // Bar cell
        double pct = Math.min(score / 9.0, 1.0);
        int barWidth = (int) Math.round(pct * 100);
        String barText = "█".repeat(Math.max(1, barWidth / 5));

        Cell barCell = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setPadding(8);
        barCell.add(new Paragraph(barText + "  " + bandDescriptor(score))
                .setFont(regular).setFontSize(9).setFontColor(color));
        t.addCell(barCell);
    }

    // ── CRITERIA DETAIL ──────────────────────────────────────────────
    private void addCriteriaDetail(Document doc, PdfFont bold, PdfFont regular,
                                   String title, double score, CriteriaScoreDto criteria, boolean isPronunciation) {
        if (criteria == null) return;

        Table t = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setMarginLeft(36).setMarginRight(36).setMarginTop(10)
                .setBorder(new SolidBorder(COLOR_BORDER, 1));

        // Title row
        DeviceRgb color = scoreColor(score);
        Cell titleCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(COLOR_SECTION_BG)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 1))
                .setPaddingLeft(12).setPaddingRight(12).setPaddingTop(8).setPaddingBottom(8);

        Table titleRow = new Table(UnitValue.createPercentArray(new float[]{5, 1}))
                .useAllAvailableWidth().setBorder(Border.NO_BORDER);
        titleRow.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph(title).setFont(bold).setFontSize(11).setFontColor(COLOR_PRIMARY)));
        titleRow.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(String.format("%.1f / 9.0", score)).setFont(bold).setFontSize(11).setFontColor(color)));
        titleCell.add(titleRow);
        t.addCell(titleCell);

        // Content
        Cell contentCell = new Cell().setBorder(Border.NO_BORDER).setPadding(12);

        if (!isPronunciation) {
            if (criteria.getStrengths() != null && !criteria.getStrengths().isBlank()) {
                contentCell.add(new Paragraph("Strengths").setFont(bold).setFontSize(9).setFontColor(COLOR_SUCCESS).setMarginBottom(2));
                contentCell.add(new Paragraph(criteria.getStrengths()).setFont(regular).setFontSize(9).setMarginBottom(8));
            }
            if (criteria.getWeaknesses() != null && !criteria.getWeaknesses().isBlank()) {
                contentCell.add(new Paragraph("Areas for Improvement").setFont(bold).setFontSize(9).setFontColor(COLOR_WARNING).setMarginBottom(2));
                contentCell.add(new Paragraph(criteria.getWeaknesses()).setFont(regular).setFontSize(9));
            }
        } else {
            String text = criteria.getFeedback() != null ? criteria.getFeedback() : criteria.getStrengths();
            if (text != null && !text.isBlank()) {
                contentCell.add(new Paragraph("Feedback").setFont(bold).setFontSize(9).setFontColor(COLOR_PRIMARY).setMarginBottom(2));
                contentCell.add(new Paragraph(text).setFont(regular).setFontSize(9).setMarginBottom(8));
            }
            if (criteria.getWeaknesses() != null && !criteria.getWeaknesses().isBlank()) {
                contentCell.add(new Paragraph("Weaknesses").setFont(bold).setFontSize(9).setFontColor(COLOR_WARNING).setMarginBottom(2));
                contentCell.add(new Paragraph(criteria.getWeaknesses()).setFont(regular).setFontSize(9));
            }
        }

        t.addCell(contentCell);
        doc.add(t);
    }

    // ── LIST SECTIONS (mistakes / suggestions) ───────────────────────
    private void addListSection(Document doc, PdfFont bold, PdfFont regular, String title, List<String> items, DeviceRgb color) {
        if (items == null || items.isEmpty()) return;

        doc.add(new Paragraph(title)
                .setFont(bold).setFontSize(13)
                .setFontColor(COLOR_PRIMARY)
                .setMarginLeft(36).setMarginRight(36).setMarginTop(16).setMarginBottom(6));

        Table t = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setMarginLeft(36).setMarginRight(36)
                .setBorder(new SolidBorder(COLOR_BORDER, 1));

        for (int i = 0; i < items.size(); i++) {
            Cell row = new Cell().setBorder(Border.NO_BORDER)
                    .setBorderBottom(i < items.size() - 1 ? new SolidBorder(COLOR_BORDER, 0.5f) : Border.NO_BORDER)
                    .setBackgroundColor(i % 2 == 0 ? ColorConstants.WHITE : COLOR_LIGHT_GRAY)
                    .setPaddingLeft(12).setPaddingRight(12).setPaddingTop(7).setPaddingBottom(7);
            row.add(new Paragraph("• " + items.get(i)).setFont(regular).setFontSize(10).setFontColor(color));
            t.addCell(row);
        }
        doc.add(t);
    }

    // ── TRANSCRIPT ───────────────────────────────────────────────────
    private void addTranscript(Document doc, PdfFont bold, PdfFont regular, String transcript) {
        doc.add(new Paragraph("Transcript")
                .setFont(bold).setFontSize(13)
                .setFontColor(COLOR_PRIMARY)
                .setMarginLeft(36).setMarginRight(36).setMarginTop(16).setMarginBottom(6));

        Table t = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setMarginLeft(36).setMarginRight(36)
                .setBorder(new SolidBorder(COLOR_BORDER, 1))
                .setBackgroundColor(COLOR_LIGHT_GRAY);

        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(12)
                .add(new Paragraph(transcript)
                        .setFont(regular).setFontSize(9)
                        .setFontColor(new DeviceRgb(51, 65, 85))));

        doc.add(t);
    }

    // ── TEACHER REVIEW ───────────────────────────────────────────────
    private void addTeacherReview(Document doc, PdfFont bold, PdfFont regular, TeacherReviewDto tr) {
        doc.add(new Paragraph("Teacher's Review")
                .setFont(bold).setFontSize(13)
                .setFontColor(COLOR_PRIMARY)
                .setMarginLeft(36).setMarginRight(36).setMarginTop(20).setMarginBottom(6));

        Table t = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setMarginLeft(36).setMarginRight(36)
                .setBorder(new SolidBorder(COLOR_SUCCESS, 2));

        // Header
        t.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(new DeviceRgb(240, 253, 244))
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 1))
                .setPadding(10)
                .add(new Paragraph("Human Teacher Evaluation")
                        .setFont(bold).setFontSize(11).setFontColor(COLOR_SUCCESS)));

        // Scores grid
        Cell scoresCell = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 1))
                .setPadding(12);

        Table scoresGrid = new Table(UnitValue.createPercentArray(new float[]{2, 1, 2, 1, 2, 1}))
                .useAllAvailableWidth().setBorder(Border.NO_BORDER);

        addTeacherScoreItem(scoresGrid, bold, regular, "Fluency & Coherence", tr.getAdjustedFluency());
        addTeacherScoreItem(scoresGrid, bold, regular, "Lexical Resource", tr.getAdjustedLexical());
        addTeacherScoreItem(scoresGrid, bold, regular, "Grammatical Range", tr.getAdjustedGrammar());
        addTeacherScoreItem(scoresGrid, bold, regular, "Pronunciation", tr.getPronunciationScore());

        scoresCell.add(scoresGrid);
        t.addCell(scoresCell);

        // Final score
        if (tr.getFinalOverallScore() != null) {
            t.addCell(new Cell().setBorder(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(240, 253, 244))
                    .setBorderBottom(new SolidBorder(COLOR_BORDER, 1))
                    .setPadding(10)
                    .add(new Paragraph("Final Overall Band: " + String.format("%.1f", tr.getFinalOverallScore()) + " — " + bandDescriptor(tr.getFinalOverallScore()))
                            .setFont(bold).setFontSize(13).setFontColor(COLOR_SUCCESS)));
        }

        // Feedback
        if (tr.getTeacherFeedback() != null && !tr.getTeacherFeedback().isBlank()) {
            t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(12)
                    .add(new Paragraph("Teacher's Comment").setFont(bold).setFontSize(10).setFontColor(COLOR_TEXT_MUTED).setMarginBottom(4))
                    .add(new Paragraph(tr.getTeacherFeedback()).setFont(regular).setFontSize(10)));
        }

        doc.add(t);
    }

    private void addTeacherScoreItem(Table grid, PdfFont bold, PdfFont regular, String label, Double score) {
        if (score == null) return;
        grid.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(6)
                .add(new Paragraph(label).setFont(regular).setFontSize(9).setFontColor(COLOR_TEXT_MUTED)));
        grid.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(6)
                .add(new Paragraph(String.format("%.1f", score)).setFont(bold).setFontSize(12).setFontColor(scoreColor(score))));
    }

    // ── FOOTER ───────────────────────────────────────────────────────
    private void addFooter(Document doc, PdfFont regular) {
        doc.add(new Paragraph("Generated by IELTS Speaking Practice Platform  ·  This report is for practice purposes only.")
                .setFont(regular).setFontSize(8)
                .setFontColor(COLOR_TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(24).setMarginLeft(36).setMarginRight(36)
                .setBorderTop(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPaddingTop(10));
    }

    // ── HELPERS ──────────────────────────────────────────────────────
    private double resolveOverall(AiEvaluationDto ai, TeacherReviewDto tr) {
        if (tr != null && tr.getFinalOverallScore() != null) return tr.getFinalOverallScore();
        if (ai != null && ai.getOverallBand() != null) return ai.getOverallBand();
        return 0.0;
    }

    private double resolveScore(Double teacherScore, Double aiScore) {
        if (teacherScore != null) return teacherScore;
        if (aiScore != null) return aiScore;
        return 0.0;
    }

    private DeviceRgb scoreColor(double score) {
        if (score < 5.0) return COLOR_DANGER;
        if (score <= 6.5) return COLOR_WARNING;
        return COLOR_SUCCESS;
    }

    private String bandDescriptor(double score) {
        if (score >= 9.0) return "Expert User";
        if (score >= 8.0) return "Very Good User";
        if (score >= 7.0) return "Good User";
        if (score >= 6.0) return "Competent User";
        if (score >= 5.0) return "Modest User";
        if (score >= 4.0) return "Limited User";
        return "Extremely Limited";
    }

    private String bandDescription(double score) {
        if (score >= 8.0) return "Operational command with occasional inaccuracies.";
        if (score >= 7.0) return "Good operational command. Generally handles complex language.";
        if (score >= 6.0) return "Effective command despite some inaccuracies and misunderstandings.";
        if (score >= 5.0) return "Partial command. May make many mistakes.";
        return "Basic competence is limited.";
    }

    private String formatPart(String part) {
        if (part == null) return "Speaking";
        return switch (part.toLowerCase()) {
            case "part1" -> "Speaking — Part 1";
            case "part2" -> "Speaking — Part 2 (Long Turn)";
            case "part3" -> "Speaking — Part 3 (Discussion)";
            default -> part;
        };
    }

    private String formatDate(String raw) {
        if (raw == null) return "";
        try {
            return raw.replace("T", " ").substring(0, Math.min(raw.length(), 19));
        } catch (Exception e) {
            return raw;
        }
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
