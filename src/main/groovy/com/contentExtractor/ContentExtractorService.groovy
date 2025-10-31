package com.contentExtractor

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.imageio.ImageIO

@Service
class ContentExtractorService {

    /**
     * Extracts text from the provided input stream
     *
     * @param inputStream The stream containing the file data
     * @param fileName The original file name (used to determine file type)
     * @return The extracted text content
     */
    String extractText(InputStream inputStream, String fileName) {
        // TODO: Implement actual text extraction logic based on file type

        // Stub implementation
        def fileExtension = getFileExtension(fileName)

        switch (fileExtension?.toLowerCase()) {
            case 'pdf':
                return extractTextFromPdf(inputStream)
            case 'doc':
            case 'docx':
                return extractTextFromWord(inputStream, fileExtension?.toLowerCase())
            case 'xls':
            case 'xlsx':
                return extractTextFromExcel(inputStream)
            case 'html':
            case 'htm':
                return extractTextFromHtml(inputStream)
            default:
                return "Stubbed text extraction for file: ${fileName}. File type: ${fileExtension}"
        }
    }

    private String getFileExtension(String fileName) {
        if (!fileName || !fileName.contains('.')) {
            return null
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1)
    }

    private String extractTextFromPdf(InputStream inputStream) {
        PDDocument document = null
        try {
            // Load the PDF document from the input stream
            document = Loader.loadPDF(inputStream.readAllBytes())

            // Create PDFTextStripper to extract text
            PDFTextStripper stripper = new PDFTextStripper()

            // Extract text from all pages
            String text = stripper.getText(document)

            return text ?: "[PDF contains no extractable text]"
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from PDF: ${e.message}", e)
        } finally {
            // Close the document to free resources
            if (document != null) {
                try {
                    document.close()
                } catch (Exception e) {
                    // Log but don't throw - we already have the text
                }
            }
        }
    }

    private String extractTextFromWord(InputStream inputStream, String extension) {
        try {
            if (extension == 'docx') {
                // Handle .docx files (Office Open XML format)
                XWPFDocument document = new XWPFDocument(inputStream)
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)
                try {
                    String text = extractor.getText()
                    return text ?: "[Word document contains no extractable text]"
                } finally {
                    extractor.close()
                    document.close()
                }
            } else if (extension == 'doc') {
                // Handle .doc files (older binary format)
                HWPFDocument document = new HWPFDocument(inputStream)
                WordExtractor extractor = new WordExtractor(document)
                try {
                    String text = extractor.getText()
                    return text ?: "[Word document contains no extractable text]"
                } finally {
                    extractor.close()
                    document.close()
                }
            } else {
                throw new IllegalArgumentException("Unsupported Word document format: ${extension}")
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from Word document: ${e.message}", e)
        }
    }

    private String extractTextFromExcel(InputStream inputStream) {
        Workbook workbook = null
        try {
            // Create workbook from input stream (handles both .xls and .xlsx)
            workbook = WorkbookFactory.create(inputStream)

            // DataFormatter to properly format cell values
            DataFormatter dataFormatter = new DataFormatter()

            StringBuilder textBuilder = new StringBuilder()

            // Iterate through all sheets
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i)

                // Add sheet name
                if (workbook.getNumberOfSheets() > 1) {
                    textBuilder.append("Sheet: ${sheet.getSheetName()}\n")
                }

                // Iterate through all rows
                for (Row row : sheet) {
                    List<String> cellValues = []

                    // Iterate through all cells in the row
                    for (Cell cell : row) {
                        // Format cell value as string
                        String cellValue = dataFormatter.formatCellValue(cell)
                        if (cellValue?.trim()) {
                            cellValues.add(cellValue.trim())
                        }
                    }

                    // Add row content if not empty
                    if (cellValues) {
                        textBuilder.append(cellValues.join(' | ')).append('\n')
                    }
                }

                // Add spacing between sheets
                if (i < workbook.getNumberOfSheets() - 1) {
                    textBuilder.append('\n')
                }
            }

            String text = textBuilder.toString().trim()
            return text ?: "[Excel document contains no extractable text]"
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from Excel: ${e.message}", e)
        } finally {
            // Close the workbook to free resources
            if (workbook != null) {
                try {
                    workbook.close()
                } catch (Exception e) {
                    // Log but don't throw - we already have the text
                }
            }
        }
    }

    private String extractTextFromHtml(InputStream inputStream) {
        try {
            // Read the HTML content from the input stream
            String html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)

            // Parse HTML and extract text
            org.jsoup.nodes.Document document = Jsoup.parse(html)

            // Extract text content (removes HTML tags)
            String text = document.text()

            return text ?: "[HTML document contains no extractable text]"
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from HTML: ${e.message}", e)
        }
    }

    /**
     * Creates a thumbnail from an image file
     *
     * @param inputStream The stream containing the image data
     * @param fileName The original file name
     * @param targetWidth The desired thumbnail width
     * @param targetHeight The desired thumbnail height
     * @return A map containing the base64-encoded thumbnail and metadata
     */
    Map<String, Object> createThumbnail(InputStream inputStream, String fileName, Integer targetWidth, Integer targetHeight) {
        try {
            // Read the original image
            BufferedImage originalImage = ImageIO.read(inputStream)

            if (originalImage == null) {
                throw new IllegalArgumentException("Unable to read image from file: ${fileName}. File may not be a valid image format.")
            }

            // Calculate thumbnail dimensions while maintaining aspect ratio
            int originalWidth = originalImage.getWidth()
            int originalHeight = originalImage.getHeight()

            double aspectRatio = (double) originalWidth / originalHeight
            int thumbnailWidth = targetWidth
            int thumbnailHeight = targetHeight

            // Adjust dimensions to maintain aspect ratio
            if (originalWidth > originalHeight) {
                thumbnailHeight = (int) (thumbnailWidth / aspectRatio)
            } else {
                thumbnailWidth = (int) (thumbnailHeight * aspectRatio)
            }

            // Create the thumbnail
            Image scaledImage = originalImage.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH)
            BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB)

            // Draw the scaled image onto the thumbnail
            def graphics = thumbnailImage.createGraphics()
            graphics.drawImage(scaledImage, 0, 0, null)
            graphics.dispose()

            // Determine output format (default to jpg for compatibility)
            String format = getImageFormat(fileName) ?: 'jpg'

            // Convert thumbnail to base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            ImageIO.write(thumbnailImage, format, outputStream)
            byte[] imageBytes = outputStream.toByteArray()
            String base64Image = Base64.getEncoder().encodeToString(imageBytes)

            return [
                base64Image: base64Image,
                width: thumbnailWidth,
                height: thumbnailHeight,
                format: format
            ]
        } catch (Exception e) {
            throw new RuntimeException("Failed to create thumbnail: ${e.message}", e)
        }
    }

    /**
     * Creates a thumbnail from a specific page of a PDF document
     *
     * @param inputStream The stream containing the PDF data
     * @param fileName The original file name
     * @param pageNumber The page number to render (1-indexed)
     * @param targetWidth The desired thumbnail width
     * @param targetHeight The desired thumbnail height
     * @return A map containing the base64-encoded thumbnail and metadata
     */
    Map<String, Object> createPdfPageThumbnail(InputStream inputStream, String fileName, Integer pageNumber, Integer targetWidth, Integer targetHeight) {
        PDDocument document = null
        try {
            // Load the PDF document
            document = Loader.loadPDF(inputStream.readAllBytes())

            // Validate page number (convert from 1-indexed to 0-indexed)
            int pageIndex = pageNumber - 1
            if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                throw new IllegalArgumentException("Invalid page number: ${pageNumber}. PDF has ${document.getNumberOfPages()} pages.")
            }

            // Create PDF renderer
            PDFRenderer pdfRenderer = new PDFRenderer(document)

            // Render the page to an image at 150 DPI (good quality for thumbnails)
            BufferedImage pageImage = pdfRenderer.renderImageWithDPI(pageIndex, 150)

            // Calculate thumbnail dimensions while maintaining aspect ratio
            int originalWidth = pageImage.getWidth()
            int originalHeight = pageImage.getHeight()

            double aspectRatio = (double) originalWidth / originalHeight
            int thumbnailWidth = targetWidth
            int thumbnailHeight = targetHeight

            // Adjust dimensions to maintain aspect ratio
            if (originalWidth > originalHeight) {
                thumbnailHeight = (int) (thumbnailWidth / aspectRatio)
            } else {
                thumbnailWidth = (int) (thumbnailHeight * aspectRatio)
            }

            // Create the thumbnail
            Image scaledImage = pageImage.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH)
            BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB)

            // Draw the scaled image onto the thumbnail
            def graphics = thumbnailImage.createGraphics()
            graphics.drawImage(scaledImage, 0, 0, null)
            graphics.dispose()

            // Convert thumbnail to base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            ImageIO.write(thumbnailImage, 'jpg', outputStream)
            byte[] imageBytes = outputStream.toByteArray()
            String base64Image = Base64.getEncoder().encodeToString(imageBytes)

            return [
                base64Image: base64Image,
                width: thumbnailWidth,
                height: thumbnailHeight,
                format: 'jpg'
            ]
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PDF page thumbnail: ${e.message}", e)
        } finally {
            // Close the document to free resources
            if (document != null) {
                try {
                    document.close()
                } catch (Exception e) {
                    // Log but don't throw - we already have the thumbnail
                }
            }
        }
    }

    /**
     * Converts a specific page of a PDF document to a full-size JPG image
     *
     * @param inputStream The stream containing the PDF data
     * @param fileName The original file name
     * @param pageNumber The page number to render (1-indexed)
     * @param dpi The DPI (dots per inch) for rendering quality (default 300)
     * @return A map containing the base64-encoded image and metadata
     */
    Map<String, Object> convertPdfPageToJpg(InputStream inputStream, String fileName, Integer pageNumber, Integer dpi) {
        PDDocument document = null
        try {
            // Load the PDF document
            document = Loader.loadPDF(inputStream.readAllBytes())

            // Validate page number (convert from 1-indexed to 0-indexed)
            int pageIndex = pageNumber - 1
            if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                throw new IllegalArgumentException("Invalid page number: ${pageNumber}. PDF has ${document.getNumberOfPages()} pages.")
            }

            // Validate DPI
            if (dpi < 72 || dpi > 600) {
                throw new IllegalArgumentException("DPI must be between 72 and 600. Provided: ${dpi}")
            }

            // Create PDF renderer
            PDFRenderer pdfRenderer = new PDFRenderer(document)

            // Render the page to a full-size image at the specified DPI
            BufferedImage pageImage = pdfRenderer.renderImageWithDPI(pageIndex, dpi as float)

            // Convert to JPG and encode as base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            ImageIO.write(pageImage, 'jpg', outputStream)
            byte[] imageBytes = outputStream.toByteArray()
            String base64Image = Base64.getEncoder().encodeToString(imageBytes)

            return [
                base64Image: base64Image,
                width: pageImage.getWidth(),
                height: pageImage.getHeight(),
                format: 'jpg'
            ]
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert PDF page to JPG: ${e.message}", e)
        } finally {
            // Close the document to free resources
            if (document != null) {
                try {
                    document.close()
                } catch (Exception e) {
                    // Log but don't throw - we already have the image
                }
            }
        }
    }

    private String getImageFormat(String fileName) {
        def extension = getFileExtension(fileName)?.toLowerCase()

        switch (extension) {
            case 'jpg':
            case 'jpeg':
                return 'jpg'
            case 'png':
                return 'png'
            case 'gif':
                return 'gif'
            case 'bmp':
                return 'bmp'
            default:
                return 'jpg'  // Default to jpg if unknown
        }
    }
}
