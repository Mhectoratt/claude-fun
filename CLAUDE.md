# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot REST API service written in Groovy that extracts text content from various document formats. The service accepts file uploads and returns extracted text.

## Build System

- **Build Tool**: Gradle with Groovy
- **Java Version**: 17
- **Groovy Version**: 5.0.1
- **Spring Boot Version**: 3.2.0

### Common Commands

```bash
# Build the project
./gradlew build

# Refresh dependencies (useful after dependency changes)
./gradlew build --refresh-dependencies

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

## Architecture

### Application Structure

The application follows standard Spring Boot REST architecture with three main layers:

1. **Controller Layer** (`ContentExtractorController`): Handles HTTP requests and multipart file uploads
2. **Service Layer** (`ContentExtractorService`): Contains business logic for text extraction
3. **Model Layer** (`ExtractionResponse`): DTOs for API responses

### Entry Point

- Main class: `com.contentExtractor.ContentExtractorApplication`
- Runs on port 8080 by default

### API Endpoints

**POST** `/api/content/extractText`
- Accepts: `multipart/form-data` with a `file` parameter
- Returns: JSON with extracted text and metadata
- Supported formats: PDF, DOC, DOCX, HTML/HTM (HTML is currently stubbed)

Example:
```bash
curl -X POST http://localhost:8080/api/content/extractText -F "file=@document.pdf"
```

### Document Processing

The service uses file extension-based routing to determine the appropriate extraction method:

- **PDF**: Apache PDFBox 3.0.1 (`extractTextFromPdf`)
- **DOC/DOCX**: Apache POI 5.2.5 (`extractTextFromWord`)
  - `.docx`: Uses `XWPFDocument` and `XWPFWordExtractor`
  - `.doc`: Uses `HWPFDocument` and `WordExtractor`
- **HTML/HTM**: Stubbed (TODO: implement with Jsoup)

All extraction methods handle InputStreams directly and include proper resource cleanup in finally blocks.

### Key Dependencies

- `spring-boot-starter-web`: REST API support
- `org.apache.pdfbox:pdfbox:3.0.1`: PDF text extraction
- `org.apache.poi:poi:5.2.5`: Word document processing
- `org.apache.poi:poi-ooxml:5.2.5`: DOCX support
- `org.apache.poi:poi-scratchpad:5.2.5`: DOC support

## Development Notes

### Eclipse Integration

This project is configured for Eclipse with Groovy support. If encountering Groovy plugin issues in Eclipse 4.37:
- Uninstall existing Groovy plugins
- Install from: `https://groovy.jfrog.io/artifactory/plugins-release/e4.37/`

### Adding New Document Format Support

To add support for a new document format:
1. Add the extraction library dependency to `build.gradle`
2. Add import statements in `ContentExtractorService`
3. Add new case(s) to the switch statement in `extractText()`
4. Implement a new private extraction method following the pattern of existing methods (proper resource cleanup, exception handling)

### Testing Locally

The service requires actual document files for testing. Create test files or use existing documents:
```bash
curl -v -X POST http://localhost:8080/api/content/extractText -F "file=@test.pdf"
```

Note: The `@` symbol before the filename is required for curl to upload file contents.
- Each time the code change, run gradlew build and then restart the gradlew bootRun task.