package org.example.testlab5.controller;


import org.example.testlab5.model.Photo;
import org.example.testlab5.model.User;
import org.example.testlab5.repositories.PhotoRepository;
import org.example.testlab5.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/photos")
public class PhotoController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Value("uploads")
    private String uploadDir;

    @GetMapping
    public String getUploadPage(Model model) {
        File folder = new File(uploadDir);
        File[] files = folder.listFiles();
        if (files != null) {
            List<String> fileNames = Arrays.stream(files)
                    .map(File::getName)
                    .collect(Collectors.toList());
            model.addAttribute("photos", fileNames);
        } else {
            model.addAttribute("photos", new ArrayList<>());
        }
        return "photos";
    }

    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file, Model model, Principal principal) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Выберите файл для загрузки!");
            return "photos";
        }
        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username); // Получаем текущего пользователя

            Path path = Paths.get(uploadDir, file.getOriginalFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Создаем и сохраняем фотографию
            Photo photo = new Photo();
            photo.setFileName(file.getOriginalFilename());
            photo.setFilePath(path.toString());
            photo.setUser(user); // Привязываем фотографию к пользователю
            photoRepository.save(photo);

            user.setPhoto(photo); // Обновляем пользователя
            userRepository.save(user);

            model.addAttribute("message", "Файл успешно загружен!");
        } catch (IOException e) {
            model.addAttribute("message", "Ошибка при загрузке файла!");
        }
        return "redirect:/photos";
    }
    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> viewPhoto(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String mediaType = Files.probeContentType(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mediaType != null ? mediaType : "image/jpeg"))
                        .body(resource);
            }
        } catch (  IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }



}

