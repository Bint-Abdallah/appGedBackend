package org.example.appgedbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = System.getProperty("user.dir");
        String uploadPath = "file:" + projectPath + "/src/main/resources/static/uploads/";

        System.out.println("=== CONFIGURATION UPLOADS ===");
        System.out.println("Project path: " + projectPath);
        System.out.println("Upload path: " + uploadPath);

        // Vérifiez si le dossier existe
        File uploadDir = new File(projectPath + "/src/main/resources/static/uploads/");
        System.out.println("Dossier existe: " + uploadDir.exists());
        System.out.println("Dossier readable: " + uploadDir.canRead());

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
