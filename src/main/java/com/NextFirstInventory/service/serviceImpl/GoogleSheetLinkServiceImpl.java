package com.NextFirstInventory.service.serviceImpl;

import com.NextFirstInventory.dto.GoogleSheetLinkDto;
import com.NextFirstInventory.entity.GoogleSheetLinkEntity;
import com.NextFirstInventory.repository.GoogleSheetLinkRepository;
import com.NextFirstInventory.service.GoogleSheetLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleSheetLinkServiceImpl implements GoogleSheetLinkService {

    private static final Pattern SPREADSHEET_ID_PATTERN = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9_-]+)");
    private static final String GID_QUERY_PARAMETER = "gid=";

    private final GoogleSheetLinkRepository repository;

    public GoogleSheetLinkServiceImpl(GoogleSheetLinkRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<GoogleSheetLinkDto> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public GoogleSheetLinkDto add(String name, String url) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google Sheet name is required");
        }

        String normalizedUrl = normalizeUrl(url);
        String spreadsheetId = extractSpreadsheetId(normalizedUrl);
        Integer sheetId = extractSheetId(normalizedUrl);
        if (repository.existsBySpreadsheetIdAndSheetId(spreadsheetId, sheetId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This Google Sheet tab is already saved");
        }

        return toDto(repository.save(GoogleSheetLinkEntity.builder()
            .name(name.trim())
                .url(normalizedUrl)
                .spreadsheetId(spreadsheetId)
                .sheetId(sheetId)
                .build()));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Google Sheet link not found");
        }
        repository.deleteById(id);
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google Sheet URL is required");
        }

        String normalizedUrl = url.trim();
        try {
            URI parsedUrl = URI.create(normalizedUrl);
            if (!"https".equalsIgnoreCase(parsedUrl.getScheme())
                    || !"docs.google.com".equalsIgnoreCase(parsedUrl.getHost())) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "URL must be a valid Google Sheets link", exception);
        }
        return normalizedUrl;
    }

    private String extractSpreadsheetId(String url) {
        Matcher matcher = SPREADSHEET_ID_PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "URL must contain a Google Sheets spreadsheet ID");
        }
        return matcher.group(1);
    }

    private Integer extractSheetId(String url) {
        String query = URI.create(url).getQuery();
        if (query != null) {
            for (String parameter : query.split("&")) {
                if (parameter.startsWith(GID_QUERY_PARAMETER)) {
                    try {
                        return Integer.valueOf(parameter.substring(GID_QUERY_PARAMETER.length()));
                    } catch (NumberFormatException ignored) {
                        break;
                    }
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "URL must contain a numeric Google Sheets tab gid");
    }

    private GoogleSheetLinkDto toDto(GoogleSheetLinkEntity entity) {
        return GoogleSheetLinkDto.builder()
                .id(entity.getId())
            .name(entity.getName())
                .url(entity.getUrl())
                .spreadsheetId(entity.getSpreadsheetId())
                .sheetId(entity.getSheetId())
                .build();
    }
}