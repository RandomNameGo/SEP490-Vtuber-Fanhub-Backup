package com.sep490.vtuber_fanhub.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    public String uploadVideo(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video"
                )
        );
        return uploadResult.get("secure_url").toString();
    }

    public String uploadImage(MultipartFile file, boolean resize, int width, int height) throws IOException {
        Map<String, Object> options = ObjectUtils.emptyMap();
        if (resize) {
            options = ObjectUtils.asMap(
                    "transformation", ObjectUtils.asMap(
                            "width", width,
                            "height", height,
                            "crop", "limit"
                    )
            );
        }
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return uploadResult.get("secure_url").toString();
    }
}
