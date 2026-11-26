package com.chineselearning.contentservice.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public enum AllowedFileType
{
    JPEG("image", "image/jpeg", true, List.of("jpg", "jpeg"),
            h -> startsWith(h, 0, 0xFF, 0xD8, 0xFF)),
    PNG("image", "image/png", true, List.of("png"),
            h -> startsWith(h, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)),
    GIF("image", "image/gif", true, List.of("gif"),
            h -> startsWithAscii(h, 0, "GIF87a") || startsWithAscii(h, 0, "GIF89a")),
    PDF("pdf", "application/pdf", true, List.of("pdf"),
            h -> startsWithAscii(h, 0, "%PDF-")),
    DOC("word", "application/msword", false, List.of("doc"),
            h -> startsWith(h, 0, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)),
    DOCX("word", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", false, List.of("docx"),
            h -> startsWith(h, 0, 0x50, 0x4B, 0x03, 0x04)),
    MP3("audio", "audio/mpeg", true, List.of("mp3"),
            h -> startsWithAscii(h, 0, "ID3") || (h.length > 1 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xE0) == 0xE0)),
    WAV("audio", "audio/wav", true, List.of("wav"),
            h -> startsWithAscii(h, 0, "RIFF") && startsWithAscii(h, 8, "WAVE")),
    MP4("video", "video/mp4", true, List.of("mp4"),
            h -> startsWithAscii(h, 4, "ftyp"));

    /** Number of leading bytes needed to check every signature. */
    public static final int HEADER_SIZE = 16;

    private final String materialType;
    private final String mediaType;
    private final boolean inline;
    private final List<String> extensions;
    private final Predicate<byte[]> signature;

    AllowedFileType(String materialType, String mediaType, boolean inline,
                    List<String> extensions, Predicate<byte[]> signature)
    {
        this.materialType = materialType;
        this.mediaType = mediaType;
        this.inline = inline;
        this.extensions = extensions;
        this.signature = signature;
    }

    public static Optional<AllowedFileType> fromFileName(String fileName)
    {
        if (fileName == null)
        {
            return Optional.empty();
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1)
        {
            return Optional.empty();
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> type.extensions.contains(extension))
                .findFirst();
    }

    public boolean matchesContent(byte[] header)
    {
        return header != null && signature.test(header);
    }

    public String materialType() { return materialType; }
    public String mediaType() { return mediaType; }
    public boolean isInline() { return inline; }
    public String canonicalExtension() { return extensions.get(0); }

    private static boolean startsWith(byte[] data, int offset, int... expected)
    {
        if (data.length < offset + expected.length)
        {
            return false;
        }
        for (int i = 0; i < expected.length; i++)
        {
            if ((data[offset + i] & 0xFF) != expected[i])
            {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWithAscii(byte[] data, int offset, String expected)
    {
        byte[] bytes = expected.getBytes(StandardCharsets.US_ASCII);
        if (data.length < offset + bytes.length)
        {
            return false;
        }
        return Arrays.equals(data, offset, offset + bytes.length, bytes, 0, bytes.length);
    }
}