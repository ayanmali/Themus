// package com.delphi.delphi.handlers;

// import java.time.LocalDateTime;

// import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
// import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

// import com.delphi.delphi.entities.User;
// import com.delphi.delphi.services.EncryptionService;

// @RepositoryEventHandler(User.class)
// public class UserEventHandler {

//     private final EncryptionService encryptionService;

//     public UserEventHandler(EncryptionService encryptionService) {
//         this.encryptionService = encryptionService;
//     }
    
//     @HandleBeforeCreate
//     public void handleBeforeCreate(User user) {
//         // encrypt password
//         try {
//             user.setPassword(encryptionService.encrypt(user.getPassword()));
//         } catch (Exception e) {
//             throw new RuntimeException("Error encrypting password", e);
//         }
//     }
    
// }
