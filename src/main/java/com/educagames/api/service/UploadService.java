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

    @Value("${storage.lesson_bucket}")
    private String lessonBucketName;

    @Value("${storage.key}")
    private String serviceKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envia o avatar do usuário para o storage configurado e retorna sua URL pública.
     * <p>
     * Validações de tipo e tamanho do arquivo devem ser feitas pelo chamador.
     * Em caso de falha de I/O ou resposta inválida do provedor, uma {@link BadRequestException}
     * é lançada.
     *
     * @param userId ID do usuário
     * @param file arquivo multipart do avatar
     * @return URL pública do avatar salvo
     * @throws BadRequestException em erros de upload
     */
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

    /**
     * Envia um arquivo de material de aula para o bucket de conteúdos de lição
     * e retorna sua URL pública.
     * Valida o content-type com base nos tipos permitidos.
     */
    public String uploadLessonContent(Long moduleId, Long lessonId, MultipartFile file) {
        try {
            String extension = getExtensionForLesson(file);
            String filename = sanitizeFilename(file.getOriginalFilename(), extension);
            String path = "modules/" + moduleId + "/lessons/" + lessonId + "/" + filename;

            String url = storageUrl + "/storage/v1/object/" + lessonBucketName + "/" + path;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(serviceKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(
                file.getInputStream(),
                filename
            ));

            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            return storageUrl + "/storage/v1/object/public/" + lessonBucketName + "/" + path;

        } catch (Exception e) {
            log.error("Erro ao fazer upload de material da lição {} do módulo {}: {}", lessonId, moduleId, e.getMessage());
            throw new BadRequestException("Não foi possível salvar o arquivo do material.");
        }
    }

    /**
     * Remove do storage o arquivo referenciado pela URL pública.
     * <p>
     * Ignora quando a URL é nula. Em caso de erro, registra um aviso e não lança exceção.
     *
     * @param publicUrl URL pública do arquivo
     */
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

    /**
     * Determina a extensão do arquivo com base em seu content-type.
     * <p>
     * Mapeia `image/png` para `.png` e `image/jpeg`/`image/jpg` para `.jpg`. Em
     * caso de content-type nulo ou não mapeado, retorna `.png` como padrão.
     *
     * @param file arquivo multipart
     * @return extensão do arquivo (inclui o ponto) ou `.png` por padrão
     */
    private String getExtension(MultipartFile file) {
        String ct = file.getContentType();
        if ("image/png".equals(ct)) return ".png";
        if ("image/jpeg".equals(ct) || "image/jpg".equals(ct)) return ".jpg";
        return ".png";
    }

    private String getExtensionForLesson(MultipartFile file) {
        String ct = file.getContentType();
        if ("image/png".equals(ct)) return ".png";
        if ("image/jpeg".equals(ct) || "image/jpg".equals(ct)) return ".jpg";
        if ("image/gif".equals(ct)) return ".gif";
        if ("image/webp".equals(ct)) return ".webp";
        if ("application/pdf".equals(ct)) return ".pdf";
        if ("application/zip".equals(ct)) return ".zip";
        return ".bin";
    }

    private String sanitizeFilename(String original, String extension) {
        String base = (original != null ? original : "file")
            .replaceAll("[^a-zA-Z0-9._-]", "_");
        // Remover extensão duplicada e acrescentar timestamp para unicidade
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);
        return System.currentTimeMillis() + "-" + base + extension;
    }
}
