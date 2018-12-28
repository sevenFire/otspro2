import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;

/**
 * @author liyuhui
 * @date 2018/12/27
 * @description
 */
public class TestJson {
    public static void main(String[] args) {
        String primaryKey = "[\"col1\",\"col2\",\"col3\"]";
        String columns = "[{\"col_name\":\"col1\",\"col_type\":\"String\"},{\"col_name\":\"col2\",\"col_type\":\"String\"},{\"col_name\":\"col3\",\"col_type\":\"String\"},{\"col_name\":\"col4\",\"col_type\":\"String\"}]";
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(primaryKey);
            ArrayNode arrayNode = (ArrayNode)jsonNode;
            System.out.println("pause");

            JsonNode jsonNode2 = mapper.readTree(columns);
            ArrayNode arrayNode2 = (ArrayNode)jsonNode2;
            System.out.println("pause");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
