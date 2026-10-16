package it.unibz.inf.ontop.docker.lightweight.snowflake;

import com.google.common.collect.ImmutableMultiset;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;

/**
 * Expected values shared by the Snowflake nested data tests, whatever the nested datatype
 * (ARRAY or VARIANT) used by the flatten lenses.
 */
public abstract class AbstractNestedDataSnowflakeTest extends AbstractNestedDataTest {

    @Override
    protected ImmutableMultiset<String> getFlattenWithPositionExpectedValues() {
        return ImmutableMultiset.of( "\"1\"^^xsd:integer", "\"1\"^^xsd:integer", "\"1\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"2\"^^xsd:integer");
    }

    @Override
    protected ImmutableMultiset<String> getFlattenTimestampExpectedValues() {
        return ImmutableMultiset.of( "\"2023-01-01 18:00:00.000\"^^xsd:dateTime", "\"2023-01-15 18:00:00.000\"^^xsd:dateTime", "\"2023-01-29 12:00:00.000\"^^xsd:dateTime",
                "\"2023-02-12 18:00:00.000\"^^xsd:dateTime", "\"2023-02-26 18:00:00.000\"^^xsd:dateTime",
                "\"2023-03-12 18:00:00.000\"^^xsd:dateTime", "\"2023-03-26 18:00:00.000\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableMultiset<String> getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 15000.000000\"^^xsd:string", "\"Jim: 15666.666667\"^^xsd:string",
                "\"Cynthia: 13000.000000\"^^xsd:string", "\"Sam: 10000.000000\"^^xsd:string",
                "\"Bob: 17666.666667\"^^xsd:string");
    }
}
