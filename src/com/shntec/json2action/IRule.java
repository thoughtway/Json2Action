package com.shntec.json2action;

import com.fasterxml.jackson.databind.JsonNode;

public interface IRule<T, R> {
	R apply(String nodeName, JsonNode node, T generatableType, Schema currentSchema);
}
