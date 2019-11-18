package edu.stanford.biosearch.model.localstorage;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.stanford.biosearch.config.DefaultExceptionHandler;
import java.io.IOException;
import java.util.Map;

public class StorageObjectDeserializer extends StdDeserializer<StorageObject> {

  public StorageObjectDeserializer() {
    this(null);
  }

  public StorageObjectDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public StorageObject deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
    StorageObject storageObject = new StorageObject();
    ObjectCodec codec = parser.getCodec();
    JsonNode node = codec.readTree(parser);

    // try catch block
    JsonNode type = node.get("type");
    if (type == null) {
      throw new IllegalArgumentException(DefaultExceptionHandler.INVALID_BODY);
    }
    storageObject.type = node.get("type").asText();

    JsonNode data = node.get("data");
    if (data == null) {
      throw new IllegalArgumentException(DefaultExceptionHandler.INVALID_BODY);
    }

    ObjectMapper mapper = new ObjectMapper();
    storageObject.data = mapper.convertValue(node.get("data"), Map.class);

    return storageObject;
  }
}
