package com.contentExtractor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/content")
class ContentExtractorController {

    @Autowired
    ContentExtractorService contentExtractorService

    @PostMapping(value = "/extractText", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ExtractionResponse> extractText(@RequestParam("file") MultipartFile file) {
        try {
            def inputStream = file.getInputStream()
            def extractedText = contentExtractorService.extractText(inputStream, file.getOriginalFilename())

            def response = new ExtractionResponse(
                success: true,
                text: extractedText,
                fileName: file.getOriginalFilename(),
                fileSize: file.getSize()
            )

            return ResponseEntity.ok(response)
        } catch (Exception e) {
            def errorResponse = new ExtractionResponse(
                success: false,
                text: null,
                fileName: file.getOriginalFilename(),
                error: e.getMessage()
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }

    @PostMapping(value = "/createThumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ThumbnailResponse> createThumbnail(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "width", defaultValue = "150") Integer width,
            @RequestParam(value = "height", defaultValue = "150") Integer height) {
        try {
            def inputStream = file.getInputStream()
            def thumbnailData = contentExtractorService.createThumbnail(
                inputStream,
                file.getOriginalFilename(),
                width,
                height
            )

            def response = new ThumbnailResponse(
                success: true,
                thumbnailBase64: thumbnailData.base64Image,
                originalFileName: file.getOriginalFilename(),
                originalFileSize: file.getSize(),
                thumbnailWidth: thumbnailData.width,
                thumbnailHeight: thumbnailData.height,
                format: thumbnailData.format
            )

            return ResponseEntity.ok(response)
        } catch (Exception e) {
            def errorResponse = new ThumbnailResponse(
                success: false,
                thumbnailBase64: null,
                originalFileName: file.getOriginalFilename(),
                error: e.getMessage()
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }

    @PostMapping(value = "/createPdfPageThumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ThumbnailResponse> createPdfPageThumbnail(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
            @RequestParam(value = "width", defaultValue = "150") Integer width,
            @RequestParam(value = "height", defaultValue = "150") Integer height) {
        try {
            def inputStream = file.getInputStream()
            def thumbnailData = contentExtractorService.createPdfPageThumbnail(
                inputStream,
                file.getOriginalFilename(),
                pageNumber,
                width,
                height
            )

            def response = new ThumbnailResponse(
                success: true,
                thumbnailBase64: thumbnailData.base64Image,
                originalFileName: file.getOriginalFilename(),
                originalFileSize: file.getSize(),
                thumbnailWidth: thumbnailData.width,
                thumbnailHeight: thumbnailData.height,
                format: thumbnailData.format
            )

            return ResponseEntity.ok(response)
        } catch (Exception e) {
            def errorResponse = new ThumbnailResponse(
                success: false,
                thumbnailBase64: null,
                originalFileName: file.getOriginalFilename(),
                error: e.getMessage()
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }

    @PostMapping(value = "/convertPdfPageToJpg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ThumbnailResponse> convertPdfPageToJpg(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
            @RequestParam(value = "dpi", defaultValue = "300") Integer dpi) {
        try {
            def inputStream = file.getInputStream()
            def imageData = contentExtractorService.convertPdfPageToJpg(
                inputStream,
                file.getOriginalFilename(),
                pageNumber,
                dpi
            )

            def response = new ThumbnailResponse(
                success: true,
                thumbnailBase64: imageData.base64Image,
                originalFileName: file.getOriginalFilename(),
                originalFileSize: file.getSize(),
                thumbnailWidth: imageData.width,
                thumbnailHeight: imageData.height,
                format: imageData.format
            )

            return ResponseEntity.ok(response)
        } catch (Exception e) {
            def errorResponse = new ThumbnailResponse(
                success: false,
                thumbnailBase64: null,
                originalFileName: file.getOriginalFilename(),
                error: e.getMessage()
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}
