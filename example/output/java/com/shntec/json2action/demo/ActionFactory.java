
package com.shntec.json2action.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eel.kitchen.jsonschema.main.JsonSchema;
import org.eel.kitchen.jsonschema.main.JsonSchemaFactory;
import org.eel.kitchen.jsonschema.report.ValidationReport;

public class ActionFactory {

    private Map<String, Class> ActionClassMap = new HashMap<String, Class>();

    public ActionFactory() {
        ActionClassMap.put("ACTION_CODE_QUERYMERCHANT", QueryMerchangtAction.class);
        ActionClassMap.put("ACTION_CODE_QUERYCONSUME", QueryConsumeAction.class);
    }

    public void InitParameter() {
    }

    public ActionHandler initQueryMerchangtAction(ActionHandler handler, String rawdata)
        throws JsonProcessingException, IOException, IllegalAccessException, InstantiationException
    {
        QueryMerchangtAction phandler = ((QueryMerchangtAction)handler);
        if (("UnknownAction" != handler.getClass().getSimpleName() && "ExceptionAction" != handler.getClass().getSimpleName())) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = null;
            root = mapper.readTree(rawdata);
            phandler.getAction().setSessionID(root.get("Action").get("SessionID").asText());
            phandler.getAction().setFlag(root.get("Action").get("Flag").asInt());
            ArrayList list;
            phandler.getParameter().setKeyword(root.get("Parameter").get("Keyword").asText());
        } else {
            return (handler);
        }
        return phandler;
    }

    public ActionHandler initQueryConsumeAction(ActionHandler handler, String rawdata)
        throws JsonProcessingException, IOException, IllegalAccessException, InstantiationException
    {
        QueryConsumeAction phandler = ((QueryConsumeAction)handler);
        if (("UnknownAction" != handler.getClass().getSimpleName() && "ExceptionAction" != handler.getClass().getSimpleName())) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = null;
            root = mapper.readTree(rawdata);
            phandler.getAction().setSessionID(root.get("Action").get("SessionID").asText());
            phandler.getAction().setFlag(root.get("Action").get("Flag").asInt());
            ArrayList list;
            phandler.getParameter().setStartIndex(root.get("Parameter").get("StartIndex").asInt());
            phandler.getParameter().setCount(root.get("Parameter").get("Count").asInt());
            phandler.getParameter().setFilters(new ArrayList<com.shntec.json2action.demo.QueryConsumeAction.Parameter.Filter>());
            for (Iterator it2 = (root.get("Parameter").get("Filters").elements()); (it2.hasNext()); ) {
                JsonNode node2 = ((JsonNode)it2.next());
                com.shntec.json2action.demo.QueryConsumeAction.Parameter.Filter item2 = (phandler.getParameter().new Filter());
                item2.setOrderID(node2.get("OrderID").asText());
                phandler.getParameter().getFilters().add(item2);
            }
        } else {
            return (handler);
        }
        return phandler;
    }

    private ValidationReport validate(String rawdata, String schemastr)
        throws IOException
    {
        ObjectMapper objectmap = new ObjectMapper();
        JsonNode schemanode = (objectmap.readTree(schemastr));
        JsonSchemaFactory factory = (JsonSchemaFactory.defaultFactory());
        JsonSchema schema = (factory.fromSchema(schemanode));
        JsonNode request = (objectmap.readTree(rawdata));
        return (schema.validate(request));
    }

    public ActionHandler getAction(String RequestJSON)
        throws JsonProcessingException, IOException, IllegalAccessException, IllegalArgumentException, InstantiationException, NoSuchMethodException, SecurityException, InvocationTargetException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        root = mapper.readTree(RequestJSON);
        if ((root.has("Action") && root.get("Action").has("Name"))) {
            if ((null != ActionClassMap.get(root.get("Action").get("Name").asText()))) {
                ValidationReport report;
                ActionHandler handler = ((ActionHandler)(ActionClassMap.get(root.get("Action").get("Name").asText()).newInstance()));
                JsonNode schemanode = (mapper.readTree(handler.getJsonSchema()));
                ObjectNode propertiesnode = ((ObjectNode)(schemanode.get("properties")));
                if ((propertiesnode.has("Response"))) {
                    propertiesnode.remove("Response");
                }
                report = validate(RequestJSON, schemanode.toString());
                if ((report.isSuccess())) {
                    String methodName = "init" + ActionClassMap.get(root.get("Action").get("Name").asText()).getSimpleName();
                    this.getClass().getMethod(methodName, ActionHandler.class, String.class).invoke(this, handler, RequestJSON);
                    return handler;
                } else {
                    return (new ExceptionAction(report, handler));
                }
            } else {
                return new UnknownAction();
            }
        }
        return new UnknownAction();
    }

}
