import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.ProjePazariApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ProjePazariApplication.class)
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
}
