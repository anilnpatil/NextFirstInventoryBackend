package com.NextFirstInventory.service.serviceImpl;

import com.NextFirstInventory.dto.InventoryItemDto;
import com.NextFirstInventory.dto.InventorySyncResult;
import com.NextFirstInventory.entity.GoogleSheetLinkEntity;
import com.NextFirstInventory.entity.InventoryEntity;
import com.NextFirstInventory.repository.GoogleSheetLinkRepository;
import com.NextFirstInventory.repository.InventoryRepository;
import com.NextFirstInventory.service.InventoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String SHEETS_READONLY_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly";

    private final InventoryRepository inventoryRepository;
    private final GoogleSheetLinkRepository googleSheetLinkRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    @Value("${google.sheets.credentials-path:}")
    private String googleCredentialsPath = "";

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                GoogleSheetLinkRepository googleSheetLinkRepository) {
        this.inventoryRepository = inventoryRepository;
        this.googleSheetLinkRepository = googleSheetLinkRepository;
    }

    @Override
    public List<InventoryItemDto> search(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return inventoryRepository.findAll().stream()
                .filter(item -> normalizedQuery.isBlank() || searchableText(item).contains(normalizedQuery))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Scheduled(initialDelayString = "${inventory.sync.initial-delay-ms:1000}",
            fixedDelayString = "${inventory.sync.interval-ms:900000}")
    @Transactional
    public InventorySyncResult syncFromGoogleSheet() {
        List<GoogleSheetLinkEntity> savedLinks = googleSheetLinkRepository.findAll();
        if (savedLinks.isEmpty()) {
            return new InventorySyncResult(0, 0);
        }

        try {
            List<InventoryEntity> importedItems = new ArrayList<>();
            int tabCount = 0;
            for (GoogleSheetLinkEntity link : savedLinks) {
                importedItems.addAll(parseCsv(
                        downloadTab(link.getSpreadsheetId(), link.getSheetId()),
                        "Sheet " + link.getSheetId()));
                tabCount++;
            }

            inventoryRepository.deleteAllInBatch();
            inventoryRepository.saveAll(importedItems);
            log.info("Imported {} inventory items from {} Google Sheet tabs", importedItems.size(), tabCount);
            return new InventorySyncResult(importedItems.size(), tabCount);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Google Sheet inventory sync failed; keeping the previous database snapshot", exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Google Sheets could not be loaded: " + exception.getMessage()
                        + " Existing inventory data was kept.", exception);
        }
    }

    private String downloadTab(String spreadsheetId, int sheetId) throws IOException, InterruptedException {
        if (googleCredentialsPath != null && !googleCredentialsPath.isBlank()) {
            return downloadAuthenticatedTab(spreadsheetId, sheetId, Path.of(googleCredentialsPath));
        }

        return downloadPublicTab(spreadsheetId, sheetId);
    }

    private String downloadAuthenticatedTab(String spreadsheetId, int sheetId, Path credentialsPath)
            throws IOException, InterruptedException {
        ServiceAccountCredentials serviceAccount;
        try (InputStream credentialsFile = Files.newInputStream(credentialsPath)) {
            serviceAccount = ServiceAccountCredentials.fromStream(credentialsFile);
        } catch (IOException | IllegalArgumentException exception) {
            throw new IOException("Could not load the Google service-account credentials file. Check "
                    + "GOOGLE_APPLICATION_CREDENTIALS and the JSON key file.", exception);
        }

        GoogleCredentials credentials = serviceAccount.createScoped(List.of(SHEETS_READONLY_SCOPE));
        credentials.refreshIfExpired();
        String accessToken = credentials.getAccessToken().getTokenValue();

        String spreadsheetUrl = "https://sheets.googleapis.com/v4/spreadsheets/" + spreadsheetId
                + "?fields=sheets.properties(sheetId,title)";
        JsonNode spreadsheet = getGoogleApiJson(spreadsheetUrl, accessToken, serviceAccount.getClientEmail());
        String tabTitle = findTabTitle(spreadsheet.path("sheets"), sheetId);
        String range = "'" + tabTitle.replace("'", "''") + "'!A:ZZ";
        String encodedRange = URLEncoder.encode(range, StandardCharsets.UTF_8).replace("+", "%20");
        String valuesUrl = "https://sheets.googleapis.com/v4/spreadsheets/" + spreadsheetId
                + "/values/" + encodedRange + "?majorDimension=ROWS&valueRenderOption=FORMATTED_VALUE";
        JsonNode values = getGoogleApiJson(valuesUrl, accessToken, serviceAccount.getClientEmail()).path("values");
        return valuesToCsv(values);
    }

    private JsonNode getGoogleApiJson(String url, String accessToken, String serviceAccountEmail)
            throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create(url))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String apiMessage;
            try {
                apiMessage = JSON.readTree(response.body()).path("error").path("message").asText();
            } catch (IOException ignored) {
                apiMessage = "Google Sheets API request failed";
            }
            String sharingHint = response.statusCode() == 403
                    ? ". Share the sheet with service account " + serviceAccountEmail + " as a viewer, "
                        + "and confirm the Google Sheets API is enabled."
                    : "";
            throw new IOException("Google Sheets API returned HTTP " + response.statusCode() + ": "
                    + apiMessage + sharingHint);
        }
        return JSON.readTree(response.body());
    }

    private String findTabTitle(JsonNode sheets, int sheetId) throws IOException {
        for (JsonNode sheet : sheets) {
            JsonNode properties = sheet.path("properties");
            if (properties.path("sheetId").asInt(-1) == sheetId) {
                return properties.path("title").asText();
            }
        }
        throw new IOException("Google Sheet tab " + sheetId + " was not found in this spreadsheet.");
    }

    private String valuesToCsv(JsonNode rows) {
        StringBuilder csv = new StringBuilder();
        for (JsonNode row : rows) {
            boolean firstCell = true;
            for (JsonNode cell : row) {
                if (!firstCell) {
                    csv.append(',');
                }
                appendCsvCell(csv, cell.asText());
                firstCell = false;
            }
            csv.append('\n');
        }
        return csv.toString();
    }

    private void appendCsvCell(StringBuilder csv, String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            csv.append('"').append(value.replace("\"", "\"\"")).append('"');
        } else {
            csv.append(value);
        }
    }

    private String downloadPublicTab(String spreadsheetId, int sheetId) throws IOException, InterruptedException {
        String csvUrl = "https://docs.google.com/spreadsheets/d/" + spreadsheetId
                + "/gviz/tq?tqx=out:csv&gid=" + sheetId;
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create(csvUrl)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Google Sheet tab returned HTTP " + response.statusCode()
                    + ". For a restricted sheet, configure GOOGLE_APPLICATION_CREDENTIALS with a service-account "
                    + "JSON key and share the sheet with that account.");
        }
        String body = response.body();
        if (body.stripLeading().toLowerCase(Locale.ROOT).startsWith("<!doctype html")
                || body.stripLeading().toLowerCase(Locale.ROOT).startsWith("<html")) {
            throw new IOException("Google returned a sign-in page instead of CSV. Share the sheet with "
                    + "anyone who has the link as a viewer, or configure GOOGLE_APPLICATION_CREDENTIALS "
                    + "for restricted-sheet access.");
        }
        return body;
    }

    List<InventoryEntity> parseCsv(String csv, String tabTitle) {
        List<String> lines = csv.lines().filter(line -> !line.isBlank()).toList();
        if (lines.size() < 2) {
            return List.of();
        }

        List<String> headers = parseLine(lines.get(0)).stream().map(this::normalize).toList();
        Map<String, Integer> columns = headers.stream()
                .collect(Collectors.toMap(Function.identity(), headers::indexOf, (first, ignored) -> first));

        List<InventoryEntity> items = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            List<String> values = parseLine(line);
            items.add(InventoryEntity.builder()
                    .description(value(values, columns, "description"))
                        .modelNo(value(values, columns, "modelnumber", "modelno", "model"))
                        .partNo(value(values, columns, "partnumber", "partno", "part"))
                    .make(value(values, columns, "make", "manufacturer", "brand"))
                        .rackNo(value(values, columns, "racknumber", "rackno", "racknoa", "racknob", "rack"))
                        .stockQty(parseQuantity(value(values, columns, "quantity", "qty", "stockqty", "stockquantity")))
                        .stockStatus(value(values, columns, "condition", "stockstatus", "status"))
                    .location(defaultValue(value(values, columns, "location", "warehouse"), tabTitle))
                    .build());
        }
        return items;
    }

    private String defaultValue(String value, String fallback) {
        return value.isBlank() ? fallback : value;
    }

    private InventoryItemDto toDto(InventoryEntity item) {
        return InventoryItemDto.builder()
                .id(item.getId())
                .description(item.getDescription())
                .modelNumber(item.getModelNo())
                .partNumber(item.getPartNo())
                .make(item.getMake())
                .rackNumber(item.getRackNo())
                .quantity(item.getStockQty())
                .condition(item.getStockStatus())
                .location(item.getLocation())
                .build();
    }

    private String searchableText(InventoryEntity item) {
        return String.join(" ", item.getDescription(), item.getModelNo(), item.getPartNo(),
                item.getMake(), item.getRackNo())
                .toLowerCase(Locale.ROOT);
    }

    private String value(List<String> values, Map<String, Integer> columns, String... names) {
        for (String name : names) {
            Integer index = columns.get(name);
            if (index != null && index < values.size()) {
                return values.get(index).trim();
            }
        }
        return "";
    }

    private Integer parseQuantity(String value) {
        try {
            return value.isBlank() ? 0 : Integer.valueOf(value.replace(",", "").trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String normalize(String value) {
        return value.replace("\uFEFF", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        values.add(value.toString());
        return values;
    }
}
