package com.contentExtractor

import groovy.transform.Canonical

@Canonical
class ThumbnailResponse {
    boolean success
    String thumbnailBase64
    String originalFileName
    Long originalFileSize
    Integer thumbnailWidth
    Integer thumbnailHeight
    String format
    String error
}
