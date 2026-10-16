package be.ugent.rml;

import org.junit.jupiter.api.Test;

public class MapperCrossConcatSequenceTest extends TestCore {

    @Test
    public void testConcatIRI() {
        doMapping("./cross-concat-sequence/mapping.rml.ttl", "./cross-concat-sequence/output.nq");
    }
}
