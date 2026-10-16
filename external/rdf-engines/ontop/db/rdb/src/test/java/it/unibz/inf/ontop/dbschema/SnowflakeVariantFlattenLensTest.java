package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that a Snowflake VARIANT column (JSON category) can be flattened by a flatten lens,
 * including when it is extracted out of a larger document by GET_PATH.
 */
public class SnowflakeVariantFlattenLensTest {

    private static final String LENS_FILE = "src/test/resources/snowflake/variant_flatten_lenses.json";
    private static final String DBMETADATA_FILE = "src/test/resources/snowflake/company-variant.db-extract.json";

    private final ImmutableMap<String, Lens> lenses;

    public SnowflakeVariantFlattenLensTest() throws Exception {
        ImmutableSet<Lens> loadedLenses = LensParsingTest.loadViewDefinitionsSnowflake(LENS_FILE, DBMETADATA_FILE);
        this.lenses = loadedLenses.stream()
                .collect(ImmutableCollectors.toMap(l -> l.getID().getComponents().get(0).getName(), l -> l));
    }

    /**
     * Flattening a VARIANT produces VARIANT items
     */
    @Test
    public void testOutputColumnType() {
        DBTermType outputType = getAttributeType("FLATTENED_WORKERS", "WORKER");
        assertEquals("VARIANT", outputType.getName());
        assertEquals(DBTermType.Category.JSON, outputType.getCategory());
    }

    /**
     * As a VARIANT is not necessarily an array, the lens must filter out the non-array values
     */
    @Test
    public void testIsArrayFilter() {
        String iqString = getLens("FLATTENED_WORKERS").getIQ().toString();
        assertTrue(iqString.contains("VARIANT_IS_ARRAY"), iqString);
    }

    /**
     * GET_PATH is known to return a VARIANT
     */
    @Test
    public void testGetPathType() {
        DBTermType extractedType = getAttributeType("DOC_WORKERS", "WORKERS_FROM_DOC");
        assertEquals("VARIANT", extractedType.getName());
        assertEquals(DBTermType.Category.JSON, extractedType.getCategory());

        String iqString = getLens("DOC_WORKERS").getIQ().toString();
        assertTrue(iqString.contains("GET_PATH"), iqString);
    }

    /**
     * The VARIANT extracted by GET_PATH can be flattened
     */
    @Test
    public void testFlattenAfterGetPath() {
        DBTermType outputType = getAttributeType("FLATTENED_DOC_WORKERS", "WORKER");
        assertEquals("VARIANT", outputType.getName());

        String iqString = getLens("FLATTENED_DOC_WORKERS").getIQ().toString();
        assertTrue(iqString.contains("VARIANT_IS_ARRAY"), iqString);
    }

    private Lens getLens(String name) {
        Lens lens = lenses.get(name);
        if (lens == null)
            throw new AssertionError("No lens " + name + " among " + lenses.keySet());
        return lens;
    }

    private DBTermType getAttributeType(String lensName, String attributeName) {
        Lens lens = getLens(lensName);
        return lens.getAttributes().stream()
                .filter(a -> a.getID().getName().equals(attributeName))
                .map(Attribute::getTermType)
                .findAny()
                .orElseThrow(() -> new AssertionError("No " + attributeName + " attribute in " + lens.getAttributes()));
    }
}
