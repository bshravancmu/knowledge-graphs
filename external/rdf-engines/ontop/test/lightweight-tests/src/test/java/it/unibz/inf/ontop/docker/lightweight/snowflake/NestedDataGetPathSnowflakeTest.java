package it.unibz.inf.ontop.docker.lightweight.snowflake;

import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Same as {@link NestedDataVariantSnowflakeTest}, but the flattened VARIANT columns are extracted
 * out of a larger document by GET_PATH.
 */
@SnowflakeLightweightTest
public class NestedDataGetPathSnowflakeTest extends AbstractNestedDataSnowflakeTest {

    private static final String PROPERTIES_FILE = "/nested/snowflake/nested-snowflake.properties";
    private static final String OBDA_FILE = "/nested/nested.obda";
    private static final String LENS_FILE = "/nested/snowflake/nested-lenses-getpath.json";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
}
