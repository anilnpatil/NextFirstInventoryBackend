package com.NextFirstInventory.service.serviceImpl;

import com.NextFirstInventory.dto.InventoryItemDto;
import com.NextFirstInventory.dto.InventorySearchRequest;
import com.NextFirstInventory.dto.InventorySearchResponse;
import com.NextFirstInventory.entity.InventoryItemEntity;
import com.NextFirstInventory.repository.InventoryRepository;
import com.NextFirstInventory.service.InventorySearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

@Service
public class InventorySearchServiceImpl implements InventorySearchService {

    private static final Map<String, String> FIELDS = Map.ofEntries(
            Map.entry("stockGroup", "stockGroup"),
            Map.entry("make", "make"),
            Map.entry("batchName", "batchName"),
            Map.entry("itemName", "itemName"),
            Map.entry("partNumber", "partNumber"),
            Map.entry("quantity", "quantity"),
            Map.entry("rate", "rate"),
            Map.entry("value", "value"),
            Map.entry("rackNumber", "rackNumber"));

    private final InventoryRepository inventoryRepository;

    public InventorySearchServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public InventorySearchResponse search(InventorySearchRequest request) {
        int page = request.page() == null ? 0 : Math.max(0, request.page());
        int size = request.size() == null ? 25 : Math.max(1, Math.min(request.size(), 100));
        String sortBy = request.sortBy() == null || request.sortBy().isBlank()
            ? "itemName"
                : request.sortBy();
        String entitySortField = FIELDS.get(sortBy);
        if (entitySortField == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported inventory sort column");
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(request.sortDirection() == null
                    ? "asc"
                    : request.sortDirection());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sort direction must be asc or desc");
        }

        Specification<InventoryItemEntity> specification = (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            String globalQuery = normalize(request.query());
            if (!globalQuery.isEmpty()) {
                String pattern = containsPattern(globalQuery);
                predicates.add(criteriaBuilder.or(FIELDS.values().stream()
                        .distinct()
                        .map(field -> criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(field).as(String.class)), pattern, '\\'))
                        .toArray(Predicate[]::new)));
            }

            if (request.filters() != null) {
                request.filters().forEach((field, value) -> {
                    String entityField = FIELDS.get(field);
                    if (entityField == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Unsupported inventory filter column: " + field);
                    }
                    String filter = normalize(value);
                    if (!filter.isEmpty()) {
                        predicates.add(criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(entityField).as(String.class)),
                                containsPattern(filter), '\\'));
                    }
                });
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, entitySortField));
        Page<InventoryItemEntity> results = inventoryRepository.findAll(specification, pageable);
        return new InventorySearchResponse(
                results.getContent().stream().map(this::toDto).toList(),
                results.getTotalElements(),
                results.getTotalPages(),
                results.getNumber(),
                results.getSize());
    }

    private InventoryItemDto toDto(InventoryItemEntity item) {
        return InventoryItemDto.builder()
                .id(item.getId())
            .stockGroup(item.getStockGroup())
                .make(item.getMake())
                .batchName(item.getBatchName())
                .itemName(item.getItemName())
                .partNumber(item.getPartNumber())
                .quantity(item.getQuantity())
                .rate(item.getRate())
                .value(item.getValue())
                .rackNumber(item.getRackNumber())
                .build();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String containsPattern(String value) {
        return "%" + value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}