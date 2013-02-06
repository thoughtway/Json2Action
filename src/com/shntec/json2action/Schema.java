package com.shntec.json2action;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

/**
 * A JSON Schema document.
 */
public class Schema {

    private URI id;
    private JsonNode content;
    private JType javaType;

    protected Schema(URI id, JsonNode content) {
        this.id = id;
        this.content = content;
    }

    public JType getJavaType() {
        return javaType;
    }

    public void setJavaType(JType javaType) {
        this.javaType = javaType;
    }

    public void setJavaTypeIfEmpty(JType javaType) {
        if (this.getJavaType() == null) {
            this.setJavaType(javaType);
        }
    }

    public URI getId() {
        return id;
    }

    public JsonNode getContent() {
        return content;
    }

    public boolean isGenerated() {
        return (javaType != null);
    }

}