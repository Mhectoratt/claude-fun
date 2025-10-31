package com.contentExtractor

import groovy.transform.Canonical

@Canonical
class ExtractionResponse {
    boolean success
    String text
    String fileName
    Long fileSize
    String error
}
