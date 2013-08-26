
package com.shntec.json2action.demo;

import org.eel.kitchen.jsonschema.report.ValidationReport;

public class ExceptionAction
    extends ActionHandler
{

    private ActionHandler hSource = null;
    private ValidationReport vReport = null;
    private ErrorResponse eResp = null;

    public ExceptionAction(ValidationReport report, ActionHandler src) {
        hSource = src;
        vReport = report;
        eResp = new ErrorResponse(vReport.getMessages().get(0), 5001);
    }

    public ActionBase getAction() {
        hSource.getAction().setResult(false);
        hSource.getAction().setMsg("Request Format Validate Error.");
        hSource.getAction().setCode(500);
        return ((ActionBase)hSource.getAction());
    }

    public ResponseBase doAction() {
        return ((ResponseBase)eResp);
    }

    public String getJsonSchema() {
        return ("{}");
    }

}
