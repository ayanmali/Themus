// package com.delphi.delphi.controllers;

// import java.io.IOException;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.io.ByteArrayResource;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.util.LinkedMultiValueMap;
// import org.springframework.util.MultiValueMap;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.reactive.function.client.WebClient;

// import com.delphi.delphi.dtos.RecordingResponseDto;

// import reactor.core.publisher.Mono;

// /*
//  * RPC controller for recording endpoints - directed to Python service
//  * TODO: replace w/ protobuf + grpc
//  */
// @RestController
// @RequestMapping("/api/recordings")
// public class RecordingController {
//     private final WebClient webClient;
//     private final String PYTHON_SERVICE_URL;
//     private final Logger logger = LoggerFactory.getLogger(RecordingController.class);
    
//     public RecordingController(@Value("${python.service.url}") String pythonServiceUrl) {
//         this.PYTHON_SERVICE_URL = pythonServiceUrl;
//         // sending HTTP requests to Python service
//         this.webClient = WebClient.builder()
//             .baseUrl(PYTHON_SERVICE_URL)
//             .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(500 * 1024 * 1024)) // 500MB
//             .build();
//     }

//     @PostMapping
//     public Mono<ResponseEntity<RecordingResponseDto>> saveRecording(
//             @RequestParam("video") MultipartFile video,
//             @RequestParam("metadata") String metadata) {
        
//         try {
//             logger.info("Received video file: " + video.getOriginalFilename() + 
//                               ", size: " + video.getSize() + " bytes");
//             logger.info("Received metadata: " + metadata);
            
//             // Create multipart form data
//             MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
//             // Add video file with proper filename
//             ByteArrayResource videoResource = new ByteArrayResource(video.getBytes()) {
//                 @Override
//                 public String getFilename() {
//                     return video.getOriginalFilename();
//                 }
//             };
//             body.add("video", videoResource);
            
//             // Add metadata as string
//             body.add("metadata", metadata);
            
//             logger.info("Forwarding to Python service at: " + PYTHON_SERVICE_URL + "/api/recordings/");
            
//             return webClient.post()
//                 .uri("/api/recordings/")
//                 .contentType(MediaType.MULTIPART_FORM_DATA)
//                 .bodyValue(body)
//                 .retrieve()
//                 .bodyToMono(RecordingResponseDto.class)
//                 .map(ResponseEntity::ok)
//                 .defaultIfEmpty(ResponseEntity.notFound().build())
//                 .doOnError(error -> {
//                     logger.error("Error forwarding to Python service: " + error.getMessage());
//                     logger.error("Error details: ", error);
//                 });
                
//         } catch (IOException e) {
//             logger.error("Error processing video file: " + e.getMessage());
//             return Mono.just(ResponseEntity.badRequest().build());
//         }
//     }
    
//     @GetMapping("/")
//     public Mono<ResponseEntity<Object>> getRecordings(
//             @RequestParam(defaultValue = "0") int skip,
//             @RequestParam(defaultValue = "100") int limit,
//             @RequestParam(required = false) String format,
//             @RequestParam(value = "has_audio", required = false) Boolean hasAudio,
//             @RequestParam(required = false) String search) {
        
//         StringBuilder uriBuilder = new StringBuilder("/api/recordings/?");
//         uriBuilder.append("skip=").append(skip).append("&limit=").append(limit);
        
//         if (format != null) {
//             uriBuilder.append("&format=").append(format);
//         }
//         if (hasAudio != null) {
//             uriBuilder.append("&has_audio=").append(hasAudio);
//         }
//         if (search != null) {
//             uriBuilder.append("&search=").append(search);
//         }
        
//         return webClient.get()
//             .uri(uriBuilder.toString())
//             .retrieve()
//             .bodyToMono(Object.class)
//             .map(ResponseEntity::ok)
//             .defaultIfEmpty(ResponseEntity.notFound().build());
//     }
    
//     @GetMapping("/storage/stats")
//     public Mono<ResponseEntity<Object>> getStorageStats() {
//         logger.info("Testing connection to Python service at: " + PYTHON_SERVICE_URL + "/api/recordings/storage/stats");
//         return webClient.get()
//             .uri("/api/recordings/storage/stats")
//             .retrieve()
//             .bodyToMono(Object.class)
//             .map(ResponseEntity::ok)
//             .defaultIfEmpty(ResponseEntity.notFound().build())
//             .doOnError(error -> {
//                 logger.error("Error connecting to Python service: " + error.getMessage());
//             });
//     }
    
//     @GetMapping("/{recordingId}")
//     public Mono<ResponseEntity<RecordingResponseDto>> getRecordingById(@PathVariable Integer recordingId) {
//         return webClient.get()
//             .uri("/api/recordings/" + recordingId)
//             .retrieve()
//             .bodyToMono(RecordingResponseDto.class)
//             .map(ResponseEntity::ok)
//             .defaultIfEmpty(ResponseEntity.notFound().build());
//     }
    
//     @GetMapping("/{recordingId}/download")
//     public Mono<ResponseEntity<Object>> downloadRecording(@PathVariable Integer recordingId) {
//         return webClient.get()
//             .uri("/api/recordings/" + recordingId + "/download")
//             .retrieve()
//             .bodyToMono(Object.class)
//             .map(ResponseEntity::ok)
//             .defaultIfEmpty(ResponseEntity.notFound().build());
//     }
    
//     @GetMapping("/{recordingId}/stream")
//     public Mono<ResponseEntity<Object>> streamRecording(@PathVariable Integer recordingId) {
//         return webClient.get()
//             .uri("/api/recordings/" + recordingId + "/stream")
//             .retrieve()
//             .bodyToMono(Object.class)
//             .map(ResponseEntity::ok)
//             .defaultIfEmpty(ResponseEntity.notFound().build());
//     }
    
//     @PutMapping("/{recordingId}")
//     public Mono<ResponseEntity<RecordingResponseDto>> updateRecording(
//             @PathVariable Integer recordingId,
//             @RequestBody Object recordingData) {
//         return webClient.put()
//             .uri("/api/recordings/" + recordingId)
//             .bodyValue(recordingData)
//             .retrieve()
//             .bodyToMono(RecordingResponseDto.class)
//             .map(ResponseEntity::ok)
//             .defaultIfEmpty(ResponseEntity.notFound().build());
//     }
    
//     @DeleteMapping("/{recordingId}")
//     public Mono<ResponseEntity<Object>> deleteRecording(@PathVariable Integer recordingId) {
//         return webClient.delete()
//             .uri("/api/recordings/" + recordingId)
//             .retrieve()
//             .bodyToMono(Object.class)
//             .map(ResponseEntity::ok)
//             .defaultIfEmpty(ResponseEntity.notFound().build());
//     }
// }
