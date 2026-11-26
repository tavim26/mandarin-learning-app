package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.entity.LessonMaterialEntity;
import com.chineselearning.contentservice.exception.InvalidRequestException;
import com.chineselearning.contentservice.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class StorageService
{
    public static final String FILES_PATH = "/api/content/files/";

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,199}$");
    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;

    private final Path uploadPath;
    private final String baseUrl;

    public StorageService(@Value("${storage.upload-dir}") String uploadDir,
                          @Value("${storage.base-url}") String baseUrl) throws IOException
    {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        Files.createDirectories(uploadPath);
    }

    public String upload(MultipartFile file) throws IOException
    {
        if (file.isEmpty())
        {
            throw new InvalidRequestException("The file is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES)
        {
            throw new InvalidRequestException("The file exceeds the maximum allowed size of 50MB");
        }

        // The client-supplied Content-Type is ignored on purpose: it can be forged.
        AllowedFileType type = AllowedFileType.fromFileName(file.getOriginalFilename())
                .orElseThrow(() -> new InvalidRequestException("File extension not allowed"));

        byte[] header;
        try (InputStream in = file.getInputStream())
        {
            header = in.readNBytes(AllowedFileType.HEADER_SIZE);
        }
        if (!type.matchesContent(header))
        {
            throw new InvalidRequestException("File content does not match its extension");
        }

        String fileName = UUID.randomUUID() + "." + type.canonicalExtension();
        file.transferTo(resolve(fileName));
        return baseUrl + FILES_PATH + fileName;
    }

    public StoredFile load(String fileName)
    {
        AllowedFileType type = storedFileType(fileName)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        return new StoredFile(new FileSystemResource(resolve(fileName)), type);
    }

    /** Returns the type of an existing file, or empty if it is missing, unsafe or not an allowed type. */
    public Optional<AllowedFileType> storedFileType(String fileName)
    {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches())
        {
            return Optional.empty();
        }
        return AllowedFileType.fromFileName(fileName)
                .filter(type -> Files.isRegularFile(resolve(fileName)));
    }

    public Optional<String> fileNameFromUrl(String url)
    {
        String prefix = baseUrl + FILES_PATH;
        if (url == null || !url.startsWith(prefix))
        {
            return Optional.empty();
        }
        String fileName = url.substring(prefix.length());
        return SAFE_FILE_NAME.matcher(fileName).matches() ? Optional.of(fileName) : Optional.empty();
    }

    /**
     * Deletes the uploaded files behind the given materials once the current transaction has
     * committed, so a rollback never leaves a material pointing to a file that is already gone.
     * Links are skipped: they may point to a file that belongs to another material.
     */
    public void deleteFilesAfterCommit(Collection<LessonMaterialEntity> materials)
    {
        List<String> fileNames = materials.stream()
                .filter(material -> !material.isLink())
                .map(material -> fileNameFromUrl(material.getUrl()))
                .flatMap(Optional::stream)
                .toList();

        if (fileNames.isEmpty())
        {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            fileNames.forEach(this::delete);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                fileNames.forEach(StorageService.this::delete);
            }
        });
    }

    private void delete(String fileName)
    {
        try
        {
            Files.deleteIfExists(resolve(fileName));
        }
        catch (IOException e)
        {
            log.warn("Could not delete file {}", fileName, e);
        }
    }

    private Path resolve(String fileName)
    {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches())
        {
            throw new IllegalArgumentException("Invalid file name");
        }
        Path path = uploadPath.resolve(fileName).normalize();
        if (!uploadPath.equals(path.getParent()))
        {
            throw new IllegalArgumentException("Invalid file name");
        }
        return path;
    }

    public record StoredFile(Resource resource, AllowedFileType type) {}
}