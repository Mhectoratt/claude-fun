# Content Extractor - React UI

A simple React-based user interface for extracting text and creating thumbnails from documents and images.

## Features

### Text Extraction
- 📤 Drag and drop file upload
- 📄 Support for multiple document formats (PDF, DOC, DOCX, XLS, XLSX, HTML)
- 📋 Copy extracted text to clipboard
- ⚡ Real-time extraction feedback

### Thumbnail Creation
- 🖼️ Create thumbnails from images (JPG, PNG, GIF, BMP)
- 📑 Generate thumbnails from specific PDF pages
- 🎨 Adjustable width and height
- 📄 Page number selection for PDFs
- 💾 Download generated thumbnails

## How to Use

1. **Start the Backend Server**
   ```bash
   # From the project root directory
   ./gradlew bootRun
   ```
   The server will start on `http://localhost:8080`

2. **Open the UI**
   Simply open `frontend/index.html` in your web browser:
   ```bash
   # On most systems, you can open it directly
   open frontend/index.html

   # Or navigate to the file in your browser
   # file:///path/to/claude-fun/frontend/index.html
   ```

3. **Use the Application**

   **Extract Text:**
   - Select the "Extract Text" tab
   - Click "Choose File" or drag and drop a document
   - Supported formats: PDF, DOC, DOCX, XLS, XLSX, HTML
   - Click "Extract Text" button
   - View the extracted text in the result section
   - Use "Copy to Clipboard" to copy the text

   **Create Thumbnail:**
   - Select the "Create Thumbnail" tab
   - Click "Choose File" or drag and drop an image or PDF
   - Supported formats: PDF, JPG, PNG, GIF, BMP
   - Adjust width and height as desired (default: 200x200)
   - For PDFs: specify the page number to thumbnail
   - Click "Create Thumbnail" button
   - View the generated thumbnail
   - Use "Download" to save the thumbnail

## Technical Details

- **Frontend**: React 18 (loaded via CDN, no build required)
- **Styling**: Pure CSS with gradient backgrounds
- **Backend**: Spring Boot REST API on port 8080
- **CORS**: Enabled for cross-origin requests

## No Build Required

This is a standalone HTML file that uses React from CDN. No npm install or build step is needed. Simply open the HTML file in a browser!

## Backend API

The UI communicates with three endpoints:

1. **Text Extraction**
   - **Endpoint**: `POST /api/content/extractText`
   - **Parameters**: `file` (document)
   - **Response**: JSON with extracted text

2. **Image Thumbnail**
   - **Endpoint**: `POST /api/content/createThumbnail`
   - **Parameters**: `file` (image), `width`, `height`
   - **Response**: JSON with base64-encoded thumbnail

3. **PDF Page Thumbnail**
   - **Endpoint**: `POST /api/content/createPdfPageThumbnail`
   - **Parameters**: `file` (PDF), `page`, `width`, `height`
   - **Response**: JSON with base64-encoded thumbnail

## Troubleshooting

- **Connection Error**: Make sure the backend server is running on port 8080
- **413 Error**: File too large - current limit is 100MB (configurable in `application.properties`)
- **CORS Error**: Verify the `CorsConfig` class is properly configured
