package com.vnpt.mini_project_java.service.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
@Service

public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    public String uploadBase64(String base64Image) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    base64Image,
                    ObjectUtils.asMap("resource_type", "auto")
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload lên Cloudinary: " + e.getMessage());
        }
    }
}
