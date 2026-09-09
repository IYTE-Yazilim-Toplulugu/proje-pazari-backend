import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ApiResponseSerializationTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void shouldSerializeResponseCodeAsInteger() throws Exception {
        String json = om.writeValueAsString(ResponseCode.SUCCESS);
        assertEquals("0", json);
    }

    @Test
    void shouldDeserializeIntegerToEnum() throws Exception {
        ResponseCode code = om.readValue("0", ResponseCode.class);
        assertEquals(ResponseCode.SUCCESS, code);
    }

    @Test
    void shouldSerializeRegisteredNeedsVerificationAsInteger() throws Exception {
        String json = om.writeValueAsString(ResponseCode.REGISTERED_NEEDS_VERIFICATION);
        assertEquals("11", json);
    }

    @Test
    void shouldDeserializeRegisteredNeedsVerificationIntegerToEnum() throws Exception {
        ResponseCode code = om.readValue("11", ResponseCode.class);
        assertEquals(ResponseCode.REGISTERED_NEEDS_VERIFICATION, code);
    }

    @Test
    void shouldHaveUniqueNumericStatuses() {
        Set<Integer> statuses = new HashSet<>();

        for (ResponseCode code : ResponseCode.values()) {
            assertTrue(
                    statuses.add(code.getStatus()),
                    () -> "Duplicate numeric response status: " + code.getStatus());
        }
    }
}
