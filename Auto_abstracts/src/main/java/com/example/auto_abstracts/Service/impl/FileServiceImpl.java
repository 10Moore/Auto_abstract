package com.example.auto_abstracts.service.impl;

import com.example.auto_abstracts.entity.*;
import com.example.auto_abstracts.repository.FileRepository;
import com.example.auto_abstracts.repository.FolderFileRatingRepository;
import com.example.auto_abstracts.repository.FolderRepository;
import com.example.auto_abstracts.repository.SummaryRepository;
import com.example.auto_abstracts.service.FileService;
import com.example.auto_abstracts.service.SummaryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileServiceImpl implements FileService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderFileRatingRepository folderFileRatingRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private SummaryRepository summaryRepository;

    @Value("${deepseek.api.url}")
    private String deepSeekApiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    private static final int TOKEN_LIMIT = 64000;
    private static final int APPROX_CHARS_PER_TOKEN = 1; // 中文1:1估算，英文混排时可改为1.2
    private static final int CHAR_LIMIT = TOKEN_LIMIT * APPROX_CHARS_PER_TOKEN;

    public FileServiceImpl() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("创建上传目录失败", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (!"application/pdf".equals(contentType)) {
                throw new RuntimeException("只支持PDF文件上传");
            }

            String filename = file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            FileEntity entity = new FileEntity();
            entity.setFilename(filename);
            entity.setFilepath(targetLocation.toString());
            entity.setSize(file.getSize());
            entity.setUploadTime(String.valueOf(LocalDateTime.now()));

            FileEntity savedFile = fileRepository.save(entity);

            // 生成摘要
            generateAndSaveSummary(savedFile);

            return filename;
        } catch (IOException ex) {
            throw new RuntimeException("文件保存失败: " + file.getOriginalFilename(), ex);
        }
    }

    private void generateAndSaveSummary(FileEntity file) {
        try {
            // 1. 读取PDF内容
            String pdfContent = extractTextFromPdf(file.getFilepath());

            // 🚨 打印日志监控文件信息
            System.out.println("📄 上传文件名: " + file.getFilename());
            System.out.println("📦 文件大小（字节）: " + file.getSize());
            System.out.println("📝 提取后字符数: " + pdfContent.length());
//            // 2. 调用DeepSeek API生成摘要
//            String summaryContent = callDeepSeekApi(pdfContent);

            // 2. 判断长度并调用不同的摘要方式
            String summaryContent;
            if (pdfContent.length() <= CHAR_LIMIT) {
                summaryContent = callDeepSeekApi(pdfContent); // 使用原始方式
            } else {
                summaryContent = callRefineSummaryViaPython(pdfContent); // 使用Refine模式
            }

            // 3. 保存摘要
            SummaryEntity summary = new SummaryEntity();
            summary.setFile(file);
            summary.setContent(summaryContent);
            summaryRepository.save(summary);
        } catch (Exception e) {
            // 摘要生成失败不影响文件上传
            e.printStackTrace();
        }
    }

    @Override
    public String extractTextFromPdf(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(Paths.get(filePath).toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String callDeepSeekApi(String content) {
        try {
            // 限制内容长度，防止API限制
            if (content.length() > 640000) {
                content = content.substring(0, 640000);
            }

            RestTemplate restTemplate = new RestTemplate();

            // 构建请求体，按照官方格式构造
            Map<String, Object> request = Map.of(
                    "model", "deepseek-chat",
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a helpful assistant that generates concise summaries of the provided text."),
                            Map.of("role", "user", "content", content)
                    ),
                    "stream", false // 根据需求调整，如果不需要流式返回可以设置为 false
            );

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    deepSeekApiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    String.class
            );

            // 解析返回的 JSON
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();

                // 输出返回的 JSON 进行调试
                System.out.println("API返回的原始数据: " + responseBody);

                // 使用 Jackson 解析 JSON 响应
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                // 提取摘要内容，假设它在 "choices" -> "text" 中
                JsonNode choices = jsonResponse.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    String summary = choices.get(0).path("message").path("content").asText();
                    return summary;
                } else {
                    throw new RuntimeException("DeepSeek API返回的数据格式不正确，未能找到摘要");
                }
            } else {
                throw new RuntimeException("DeepSeek API调用失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("调用DeepSeek API时出错", e);
        }
    }

    private String callRefineSummaryViaPython(String content) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> request = Map.of("text", content);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:5000/refine-summary",
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Refine摘要服务返回失败：" + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("调用Refine摘要服务出错", e);
        }
    }



    @Override
    public Resource loadFile(String filename) {
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        return new FileSystemResource(filePath);
    }

    @Override
    public void moveFileToFolder(Long fileId, Long folderId) {
        // 实现文件移动逻辑（如果需要）
        copyFileToFolder(fileId, folderId);
        // 可以选择是否从原文件夹移除
    }

    @Override
    @Transactional
    public void copyFileToFolder(Long fileId, Long folderId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在: " + fileId));

        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("文件夹不存在: " + folderId));

        // 检查是否已存在关联
        boolean exists = folder.getFileRatings().stream()
                .anyMatch(r -> r.getFile().getId().equals(fileId));
        if (exists) {
            throw new RuntimeException("文件已在该文件夹中");
        }

        // 计算相关度
        int relevance = calculateRelevance(fileId, folderId);

        // 创建新的关联评分
        FolderFileRating rating = new FolderFileRating();
        rating.setFolder(folder);
        rating.setFile(file);
        rating.setRelevance(relevance);

        // 添加到集合中
        folder.getFileRatings().add(rating);
        file.getFolderRatings().add(rating);

        // 保存
        folderRepository.save(folder);
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void removeFileFromFolder(Long fileId, Long folderId) {
        // 1. 删除评分记录
        folderFileRatingRepository.deleteById(new FolderFileRatingId(folderId, fileId));

        // 2. 删除关联关系
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));

        file.getFolders().remove(folder);
        folder.getFiles().remove(file);

        // 3. 保存变更
        fileRepository.save(file);
        folderRepository.save(folder);
    }

    @Override
    public List<FileEntity> findAllByFolderId(Long folderId) {

        return fileRepository.findAllByFolderId(folderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntity> findAllByFolderIdWithRelevance(Long folderId) {
        // 直接通过关联表查询，避免N+1问题
        List<FolderFileRating> ratings = folderFileRatingRepository.findByFolderId(folderId);

        return ratings.stream()
                .map(rating -> {
                    FileEntity file = rating.getFile();
                    file.setRelevance(rating.getRelevance());
                    return file;
                })
                .collect(Collectors.toList());
    }

    // FileServiceImpl.java
    @Override
    public int calculateRelevance(Long fileId, Long folderId) {
        Optional<FileEntity> fileOpt = fileRepository.findById(fileId);
        Optional<FolderEntity> folderOpt = folderRepository.findById(folderId);

        if (fileOpt.isEmpty() || folderOpt.isEmpty()) {
            return 1; // 默认1星
        }

        Optional<SummaryEntity> summaryOpt = summaryRepository.findByFileId(fileId);
        if (summaryOpt.isEmpty()) {
            return 1; // 默认1星
        }

        String summaryContent = summaryOpt.get().getContent();
        String folderTheme = folderOpt.get().getAttribute();

        // 调用大模型API计算相关度
        return callRelevanceApi(folderTheme, summaryContent);
    }

    private int callRelevanceApi(String theme, String summary) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 构建请求体
            Map<String, Object> request = Map.of(
                    "model", "deepseek-chat",
                    "messages", List.of(
                            Map.of("role", "system", "content", "你是一个专业的相关性评分系统，请根据给定的主题和摘要内容，给出1-5星的相关性评分，直接返回数字即可。评分标准：\n" +
                                    "1星：完全不相关\n" +
                                    "2星：略微相关\n" +
                                    "3星：中等相关\n" +
                                    "4星：高度相关\n" +
                                    "5星：完全匹配"),
                            Map.of("role", "user", "content",
                                    "主题：" + theme + "\n\n" +
                                            "摘要内容：" + summary + "\n\n" +
                                            "请根据上述主题和摘要内容的相关性，给出1-5星的评分（1星最低，5星最高），只需返回单个数字：")
                    ),
                    "temperature", 0.1, // 降低随机性
                    "max_tokens", 1 // 只需要返回一个数字
            );

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    deepSeekApiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                String ratingStr = jsonResponse.path("choices")
                        .get(0).path("message").path("content").asText().trim();

                try {
                    int rating = Integer.parseInt(ratingStr);
                    return Math.min(5, Math.max(1, rating)); // 确保在1-5范围内
                } catch (NumberFormatException e) {
                    return 1; // 解析失败默认1星
                }
            }
            return 1; // 默认1星
        } catch (Exception e) {
            e.printStackTrace();
            return 1; // 出错默认1星
        }
    }
}