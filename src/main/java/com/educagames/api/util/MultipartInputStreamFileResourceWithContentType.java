package com.educagames.api.util;

import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;

public class MultipartInputStreamFileResourceWithContentType extends InputStreamResource {

    private final String filename;
    private final String contentType;

    public MultipartInputStreamFileResourceWithContentType(InputStream inputStream, String filename, String contentType) {
        super(inputStream);
        this.filename = filename;
        this.contentType = contentType;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public long contentLength() {
        return -1;
    }

    public String getContentType() {
        return contentType;
    }
}

