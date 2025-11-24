package com.example.placementmanagementsystem.service;

import org.apache.pdfbox.Loader;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Service class for file to string conversion using Apache tools
 */
@Service
public class FileService {

    /**
     * Extract text content from a DOCX file using Apache POI
     *
     * @param file DOCX file
     * @return Text content of the DOCX file
     */
    public String extractTextFromDocx(MultipartFile file) {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            StringBuilder content = new StringBuilder();

            // Extract text from all paragraphs
            doc.getParagraphs().forEach(paragraph -> content.append(paragraph.getText()).append("\n"));

            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading DOCX file", e);
        }
    }

    /**
     * Extract text content from a PDF file using Apache PDFBox
     *
     * @param file PDF file
     * @return Text content of the PDF file
     */
    public String extractTextFromPdf(MultipartFile file) {
        try (PDDocument pdfDocument = Loader.loadPDF(file.getInputStream().readAllBytes())) {
            PDFTextStripper textStripper = new PDFTextStripper();

            // Extract text content from PDF
            return textStripper.getText(pdfDocument);
        } catch (IOException e) {
            throw new RuntimeException("Error reading PDF file", e);
        }
    }
}