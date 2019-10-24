package com.leyou.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.config.UploadProperties;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 上传图片至FastDFS
 */
@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private UploadProperties uploadProperties;

//    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg", "image/png", "image/bmp");
    public String uploadImg(MultipartFile file) {
        try {
//        校验文件类型
            String contentType = file.getContentType();
            if(!uploadProperties.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
//        校验文件内容
            BufferedImage read = ImageIO.read(file.getInputStream());
            if(read == null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

//         上传到本地
//            File dest = new File("D:\\IdeaProject\\leyou\\upload\\",file.getOriginalFilename());
//            file.transferTo(dest);

//        上传到FastDFS
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = fastFileStorageClient.uploadImageAndCrtThumbImage(file.getInputStream(), file.getSize(), extension, null);
//        返回路径
//            return "http://image.leyou.com/" + storePath.getFullPath();
            return  uploadProperties.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
//            上传失败
            log.error("上传文件失败",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

    }
}
