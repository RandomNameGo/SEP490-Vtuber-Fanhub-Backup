package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Enum.PostMediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;


// Currently, we are on Free Trial. And as far as I've tried to use the asynchronous approach of video
// processing (in order to process videos longer than 60s), I say that they do not let us use this
// unless we move to starter plan. and EVEN so we could only process 1 video at a time.
// so for now, its better to just stick with videos under 60s.
@Service
public class SightEngineServiceImpl implements SightEngineService{


    @Value("${sightengine.workflow.images}")
    private String imageWorkflowId;

    @Value("${sightengine.workflow.videos}")
    private String videoWorkflowId;

    @Value("${sightengine.user}")
    private String apiUser;

    @Value("${sightengine.secret}")
    private String apiSecret;

    @Value("${spring.app.host}")
    private String appHost;

    @Override
    public JsonNode checkMediaFile(MultipartFile file, PostMediaType mediaType) {
        String apiUrl;
        if(mediaType.equals(PostMediaType.IMAGE)){
            apiUrl = "https://api.sightengine.com/1.0/check-workflow.json";
        }
        else if(mediaType.equals(PostMediaType.VIDEO)){
            apiUrl = "https://api.sightengine.com/1.0/video/check-workflow-sync.json";
        }else throw new RuntimeException("Invalid Media Type");
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            if(mediaType.equals(PostMediaType.IMAGE)){
                body.add("workflow", imageWorkflowId);
            }
            else if(mediaType.equals(PostMediaType.VIDEO)){
                body.add("workflow", videoWorkflowId);
            }
            body.add("api_user", apiUser);
            body.add("api_secret", apiSecret);

            body.add("media", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, requestEntity, JsonNode.class);
            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Error processing image upload", e);
        }
    }

    @Override
    public JsonNode checkMediaUrl(String url, PostMediaType mediaType) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            if (mediaType.equals(PostMediaType.IMAGE)) {
                String apiUrl = "https://api.sightengine.com/1.0/check-workflow.json";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("api_user", apiUser);
                body.add("api_secret", apiSecret);
                body.add("workflow", imageWorkflowId);
                body.add("url", url);

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, requestEntity, JsonNode.class);
                return response.getBody();

            } else if (mediaType.equals(PostMediaType.VIDEO)) {
                String apiUrl = "https://api.sightengine.com/1.0/video/check-workflow-sync.json";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("api_user", apiUser);
                body.add("api_secret", apiSecret);
                body.add("workflow", videoWorkflowId);
                body.add("stream_url", url);

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, requestEntity, JsonNode.class);
                return response.getBody();
            } else {
                throw new RuntimeException("Invalid Media Type");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error processing media url check", e);
        }
    }

    @Override
    public JsonNode checkVideoUrlAsync(String url) {
        String apiUrl = "https://api.sightengine.com/1.0/video/check-workflow.json";
        System.out.println("callback set: " + appHost + "/vhub/api/v1/webhooks/sightengine/video-result");
        RestTemplate restTemplate = new RestTemplate();
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("stream_url", url)
                    .queryParam("workflow", videoWorkflowId)
                    .queryParam("callback_url", appHost + "/vhub/api/v1/webhooks/sightengine/video-result")
                    .queryParam("api_user", apiUser)
                    .queryParam("api_secret", apiSecret);

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(builder.toUriString(), JsonNode.class);
            System.out.println("SightEngine Initial Response: " + response.getBody());
            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Error processing checking video url upload async", e);
        }
    }

}
