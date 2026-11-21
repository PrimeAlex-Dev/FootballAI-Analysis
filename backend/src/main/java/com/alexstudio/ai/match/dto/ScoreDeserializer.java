package com.alexstudio.ai.match.dto;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
public class ScoreDeserializer extends StdDeserializer<Integer> {
    public ScoreDeserializer() {
        super(Integer.class);
    }
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) {
        if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
            return context.reportInputMismatch(Integer.class, "Score must be an integer");
        }
        return parser.getIntValue();
    }
}
