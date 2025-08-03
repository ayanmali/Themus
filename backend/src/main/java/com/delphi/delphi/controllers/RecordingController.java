package com.delphi.delphi.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.delphi.delphi.dtos.RecordingResponseDto;

import reactor.core.publisher.Mono;

/*
 * RPC controller for recording endpoints - directed to Python service
 * TODO: replace w/ protobuf + grpc
 */
@RestController
@RequestMapping("/api/recordings")
public class RecordingController {
    private final WebClient webClient;
    private final String PYTHON_SERVICE_URL;
    
    public RecordingController(@Value("${python.service.url}") String pythonServiceUrl) {
        this.PYTHON_SERVICE_URL = pythonServiceUrl;
        // sending HTTP requests to Python service
        this.webClient = WebClient.builder().baseUrl(PYTHON_SERVICE_URL).build();
    }

    @PostMapping("/")
    public Mono<ResponseEntity<RecordingResponseDto>> saveRecording(
            @RequestParam("video") MultipartFile video,
            @RequestParam("metadata") String metadata) {
        
        try {
            // Create multipart form data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Add video file
            ByteArrayResource videoResource = new ByteArrayResource(video.getBytes()) {
                @Override
                public String getFilename() {
                    return video.getOriginalFilename();
                }
            };
            body.add("video", videoResource);
            
            // Add metadata
            body.add("metadata", metadata);
            
            // Set headers for multipart form data
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            return webClient.post()
                .uri("/api/recordings/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(requestEntity)
                .retrieve()
                .bodyToMono(RecordingResponseDto.class)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
                
        } catch (IOException e) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
    }
    
    @GetMapping("/")
    public Mono<ResponseEntity<Object>> getRecordings(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String format,
            @RequestParam(value = "has_audio", required = false) Boolean hasAudio,
            @RequestParam(required = false) String search) {
        
        StringBuilder uriBuilder = new StringBuilder("/api/recordings/?");
        uriBuilder.append("skip=").append(skip).append("&limit=").append(limit);
        
        if (format != null) {
            uriBuilder.append("&format=").append(format);
        }
        if (hasAudio != null) {
            uriBuilder.append("&has_audio=").append(hasAudio);
        }
        if (search != null) {
            uriBuilder.append("&search=").append(search);
        }
        
        return webClient.get()
            .uri(uriBuilder.toString())
            .retrieve()
            .bodyToMono(Object.class)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/storage/stats")
    public Mono<ResponseEntity<Object>> getStorageStats() {
        return webClient.get()
            .uri("/api/recordings/storage/stats")
            .retrieve()
            .bodyToMono(Object.class)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{recordingId}")
    public Mono<ResponseEntity<RecordingResponseDto>> getRecordingById(@PathVariable Integer recordingId) {
        return webClient.get()
            .uri("/api/recordings/" + recordingId)
            .retrieve()
            .bodyToMono(RecordingResponseDto.class)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{recordingId}/download")
    public Mono<ResponseEntity<Object>> downloadRecording(@PathVariable Integer recordingId) {
        return webClient.get()
            .uri("/api/recordings/" + recordingId + "/download")
            .retrieve()
            .bodyToMono(Object.class)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{recordingId}/stream")
    public Mono<ResponseEntity<Object>> streamRecording(@PathVariable Integer recordingId) {
        return webClient.get()
            .uri("/api/recordings/" + recordingId + "/stream")
            .retrieve()
            .bodyToMono(Object.class)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{recordingId}")
    public Mono<ResponseEntity<RecordingResponseDto>> updateRecording(
            @PathVariable Integer recordingId,
            @RequestBody Object recordingData) {
        return webClient.put()
            .uri("/api/recordings/" + recordingId)
            .bodyValue(recordingData)
            .retrieve()
            .bodyToMono(RecordingResponseDto.class)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{recordingId}")
    public Mono<ResponseEntity<Object>> deleteRecording(@PathVariable Integer recordingId) {
        return webClient.delete()
            .uri("/api/recordings/" + recordingId)
            .retrieve()
            .bodyToMono(Object.class)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
