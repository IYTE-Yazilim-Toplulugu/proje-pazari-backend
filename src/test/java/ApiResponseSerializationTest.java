import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iyte_yazilim.proje_pazari.ProjePazariApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ProjePazariApplication.class)
public class ApiResponseSerializationTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void shouldSerializeResponseCodeAsInteger() throws Exception{
        String json = om.writeValueAsString(ResponseCode.SUCCESS);
        assertEquals("0",json);

    }
    @Test
    void shouldDeserializeIntegerToEnum() throws Exception {
        ResponseCode code = om.readValue("0", ResponseCode.class);
        assertEquals(ResponseCode.SUCCESS, code);
    }

}
