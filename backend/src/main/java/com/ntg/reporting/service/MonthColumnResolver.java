package com.ntg.reporting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MonthColumnResolver - Resolves dashboard column letters for a given entity and month.
 *
 * Supports all 9 NSG entities across 12 months (Jan–Dec).
 * Provides column lookup, validation, reverse lookup, and fiscal-year utilities.
 */
@Service
public class MonthColumnResolver {

    private static final Logger log = LoggerFactory.getLogger(MonthColumnResolver.class);

    // ── Month name ↔ number ────────────────────────────────────────────────────

    private static final Map<String, Integer> MONTH_NAME_TO_NUMBER = new LinkedHashMap<>();
    private static final Map<Integer, String> MONTH_NUMBER_TO_NAME  = new LinkedHashMap<>();

    // ── Entity → (monthNumber → dashboardColumn) ──────────────────────────────

    private static final Map<String, Map<Integer, String>> ENTITY_MONTH_COLUMNS = new LinkedHashMap<>();

    static {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for (int i = 0; i < months.length; i++) {
            MONTH_NAME_TO_NUMBER.put(months[i], i + 1);
            MONTH_NUMBER_TO_NAME.put(i + 1, months[i]);
        }
        initializeEntityMappings();
    }

    private static void initializeEntityMappings() {
        addEntity("NSG Bermuda",    "D");
        addEntity("NSG UK",         "Q");
        addEntity("NSG Cayman",     "AD");
        addEntity("NSG BVI",        "AQ");
        addEntity("NSG Gibraltar",  "BD");
        addEntity("NSG Jersey",     "BQ");
        addEntity("NSG Guernsey",   "CD");
        addEntity("NSG Isle of Man","CQ");
        addEntity("Banking Group",  "DD");
    }

    /**
     * Generates 12 consecutive Excel-style column letters starting from {@code startCol},
     * skipping every 14th column (gap column between entities), and registers them
     * for the given entity.
     */
    private static void addEntity(String entity, String startCol) {
        Map<Integer, String> monthMap = new LinkedHashMap<>();
        String col = startCol;
        for (int month = 1; month <= 12; month++) {
            monthMap.put(month, col);
            col = nextColumn(col);
        }
        ENTITY_MONTH_COLUMNS.put(entity, monthMap);
    }

    /** Increments an Excel column label by 1 (e.g. "Z" → "AA", "AZ" → "BA"). */
    private static String nextColumn(String col) {
        char[] chars = col.toCharArray();
        int i = chars.length - 1;
        while (i >= 0) {
            if (chars[i] < 'Z') {
                chars[i]++;
                return new String(chars);
            }
            chars[i] = 'A';
            i--;
        }
        // Overflow: prepend 'A'
        return "A" + new String(chars);
    }

    // ── Core resolution ───────────────────────────────────────────────────────

    /**
     * Resolve the dashboard column letter for a given entity and month number (1–12).
     *
     * @param entity      Entity name (e.g. "NSG Bermuda")
     * @param monthNumber Month number 1–12
     * @return Column letter (e.g. "D", "AB"), or {@code null} if not found
     */
    public String resolveColumn(String entity, Integer monthNumber) {
        if (entity == null || monthNumber == null) {
            log.error("[RESOLVER] Null parameters: entity={}, monthNumber={}", entity, monthNumber);
            return null;
        }
        if (monthNumber < 1 || monthNumber > 12) {
            log.error("[RESOLVER] Invalid month number: {}", monthNumber);
            return null;
        }
        Map<Integer, String> entityMonthMap = ENTITY_MONTH_COLUMNS.get(entity);
        if (entityMonthMap == null) {
            log.warn("[RESOLVER] Unknown entity: '{}'", entity);
            return null;
        }
        String column = entityMonthMap.get(monthNumber);
        log.debug("[RESOLVER] {} / Month {} → Column {}", entity, monthNumber, column);
        return column;
    }

    /**
     * Resolve column using a month abbreviation string (e.g. "Jan", "Dec").
     *
     * @param entity    Entity name
     * @param monthName Three-letter month abbreviation
     * @return Column letter, or {@code null} if not found
     */
    public String resolveColumn(String entity, String monthName) {
        Integer monthNumber = getMonthNumber(monthName);
        if (monthNumber == null) {
            log.error("[RESOLVER] Unknown month name: '{}'", monthName);
            return null;
        }
        return resolveColumn(entity, monthNumber);
    }

    // ── Bulk / range resolution ───────────────────────────────────────────────

    /**
     * Returns a map of monthNumber → column for all 12 months for the given entity.
     *
     * @param entity Entity name
     * @return Unmodifiable map, or empty map if entity not found
     */
    public Map<Integer, String> resolveAllMonths(String entity) {
        Map<Integer, String> result = ENTITY_MONTH_COLUMNS.get(entity);
        if (result == null) {
            log.warn("[RESOLVER] resolveAllMonths: unknown entity '{}'", entity);
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns columns for a month range (inclusive) for the given entity.
     *
     * @param entity     Entity name
     * @param fromMonth  Start month number (1–12)
     * @param toMonth    End month number (1–12), must be ≥ fromMonth
     * @return Ordered map of monthNumber → column
     */
    public Map<Integer, String> resolveMonthRange(String entity, int fromMonth, int toMonth) {
        if (fromMonth < 1 || toMonth > 12 || fromMonth > toMonth) {
            log.error("[RESOLVER] Invalid range: {}-{}", fromMonth, toMonth);
            return Collections.emptyMap();
        }
        Map<Integer, String> entityMap = ENTITY_MONTH_COLUMNS.get(entity);
        if (entityMap == null) return Collections.emptyMap();

        Map<Integer, String> result = new LinkedHashMap<>();
        for (int m = fromMonth; m <= toMonth; m++) {
            result.put(m, entityMap.get(m));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns columns for the same month across all entities — useful for
     * cross-entity reporting on a single month.
     *
     * @param monthNumber Month number 1–12
     * @return Map of entityName → column
     */
    public Map<String, String> resolveAllEntitiesForMonth(Integer monthNumber) {
        if (monthNumber == null || monthNumber < 1 || monthNumber > 12) {
            log.error("[RESOLVER] Invalid month: {}", monthNumber);
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Integer, String>> entry : ENTITY_MONTH_COLUMNS.entrySet()) {
            String col = entry.getValue().get(monthNumber);
            if (col != null) result.put(entry.getKey(), col);
        }
        return Collections.unmodifiableMap(result);
    }

    // ── Reverse lookup ────────────────────────────────────────────────────────

    /**
     * Reverse-lookup: given a dashboard column letter, find which entity + month it belongs to.
     *
     * @param column Column letter (e.g. "AB")
     * @return Optional array {@code [entityName, monthName]}, or empty if not found
     */
    public Optional<String[]> resolveFromColumn(String column) {
        if (column == null) return Optional.empty();
        for (Map.Entry<String, Map<Integer, String>> entityEntry : ENTITY_MONTH_COLUMNS.entrySet()) {
            for (Map.Entry<Integer, String> monthEntry : entityEntry.getValue().entrySet()) {
                if (column.equalsIgnoreCase(monthEntry.getValue())) {
                    String monthName = MONTH_NUMBER_TO_NAME.get(monthEntry.getKey());
                    return Optional.of(new String[]{entityEntry.getKey(), monthName});
                }
            }
        }
        log.warn("[RESOLVER] No entity/month found for column '{}'", column);
        return Optional.empty();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Returns true if the entity name is recognised.
     */
    public boolean isValidEntity(String entity) {
        return entity != null && ENTITY_MONTH_COLUMNS.containsKey(entity);
    }

    /**
     * Returns true if the entity + month combination is valid and resolvable.
     */
    public boolean isValidEntityMonth(String entity, Integer monthNumber) {
        if (entity == null || monthNumber == null || monthNumber < 1 || monthNumber > 12) return false;
        Map<Integer, String> entityMap = ENTITY_MONTH_COLUMNS.get(entity);
        return entityMap != null && entityMap.containsKey(monthNumber);
    }

    // ── Month helpers ─────────────────────────────────────────────────────────

    /** Convert a three-letter abbreviation to a month number (1–12), or {@code null}. */
    public Integer getMonthNumber(String monthName) {
        if (monthName == null) return null;
        return MONTH_NAME_TO_NUMBER.get(monthName);
    }

    /** Convert a month number (1–12) to its three-letter abbreviation, or {@code null}. */
    public String getMonthName(Integer monthNumber) {
        if (monthNumber == null) return null;
        return MONTH_NUMBER_TO_NAME.get(monthNumber);
    }

    /** Returns an unmodifiable ordered list of all month abbreviations (Jan … Dec). */
    public List<String> getAllMonthNames() {
        return Collections.unmodifiableList(new ArrayList<>(MONTH_NAME_TO_NUMBER.keySet()));
    }

    // ── Entity helpers ────────────────────────────────────────────────────────

    /** Returns an unmodifiable set of all registered entity names. */
    public Set<String> getAllEntities() {
        return Collections.unmodifiableSet(ENTITY_MONTH_COLUMNS.keySet());
    }

    /**
     * Returns a full summary string for the given entity listing every
     * month → column mapping — useful for debugging.
     */
    public String getEntitySummary(String entity) {
        Map<Integer, String> map = ENTITY_MONTH_COLUMNS.get(entity);
        if (map == null) return "Entity not found: " + entity;
        StringBuilder sb = new StringBuilder(entity).append(" mappings:\n");
        map.forEach((month, col) ->
            sb.append("  ").append(MONTH_NUMBER_TO_NAME.get(month)).append(" → ").append(col).append("\n")
        );
        return sb.toString();
    }
}