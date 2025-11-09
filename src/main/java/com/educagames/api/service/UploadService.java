package com.educagames.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.educagames.api.exception.BadRequestException;
import com.educagames.api.util.MultipartInputStreamFileResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    @Value("${storage.url}")
    private String storageUrl;

    @Value("${storage.bucket}")
    private String bucketName;

    @Value("${storage.key}")
    private String serviceKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            String extension = getExtension(file);
            String path = "avatars/" + userId + "/avatar-" + System.currentTimeMillis() + extension;

            String url = storageUrl + "/storage/v1/object/" + bucketName + "/" + path;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(serviceKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(
                file.getInputStream(),
                file.getOriginalFilename()
            ));

            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            return storageUrl + "/storage/v1/object/public/" + bucketName + "/" + path;

        } catch (Exception e) {
            log.error("Erro ao fazer upload do avatar para o usuário {}: {}", userId, e.getMessage());
            throw new BadRequestException("Não foi possível salvar o avatar.");
        }
    }

    public void deleteFile(String publicUrl) {
        try {
            if (publicUrl == null) return;

            String relativePath = publicUrl.substring(publicUrl.indexOf(bucketName) + bucketName.length() + 1);
            String url = storageUrl + "/storage/v1/object/" + bucketName + "/" + relativePath;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(serviceKey);

            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        } catch (Exception e) {
            log.warn("Erro ao deletar arquivo do storage: {}", e.getMessage());
        }
    }

    private String getExtension(MultipartFile file) {
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        }
        return ".png";
    }
}
