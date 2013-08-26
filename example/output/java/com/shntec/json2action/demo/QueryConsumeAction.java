
package com.shntec.json2action.demo;

import java.util.List;

public class QueryConsumeAction
    extends ActionHandler
{

    private QueryConsumeAction.Action Action;
    private QueryConsumeAction.Parameter Parameter;
    private QueryConsumeAction.Response Response;
    private final String JSONSCHEMA = "{\"type\":\"object\",\"properties\":{\"Action\":{\"type\":\"object\",\"properties\":{\"Name\":{\"type\":\"string\",\"required\":true},\"Code\":{\"type\":\"integer\",\"required\":true},\"Result\":{\"type\":\"boolean\",\"required\":true},\"Flag\":{\"type\":\"integer\",\"required\":true},\"SessionID\":{\"type\":\"string\",\"required\":true},\"Msg\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"Parameter\":{\"type\":\"object\",\"properties\":{\"StartIndex\":{\"type\":\"integer\",\"required\":true},\"Count\":{\"type\":\"integer\",\"required\":true},\"Filters\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"OrderID\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"required\":true}},\"required\":true},\"Response\":{\"type\":\"object\",\"properties\":{\"Total\":{\"type\":\"integer\",\"required\":true},\"FeeTotal\":{\"type\":\"integer\",\"required\":true},\"FeeSubTotal\":{\"type\":\"integer\",\"required\":true},\"Records\":{\"type\":\"array\",\"required\":true}},\"required\":true}},\"required\":true}";

    public QueryConsumeAction() {
        Action = new Action();
        Action.setName("ACTION_CODE_QUERYCONSUME");
        Action.setCode(-1);
        Action.setResult(true);
        Action.setFlag(0);
        Action.setSessionID("");
        Action.setMsg("");
        Parameter = new Parameter();
    }

    public QueryConsumeAction.Action getAction() {
        return Action;
    }

    public void setAction(QueryConsumeAction.Action Action) {
        this.Action = Action;
    }

    public QueryConsumeAction.Parameter getParameter() {
        return Parameter;
    }

    public void setParameter(QueryConsumeAction.Parameter Parameter) {
        this.Parameter = Parameter;
    }

    public QueryConsumeAction.Response getResponse() {
        return Response;
    }

    public void setResponse(QueryConsumeAction.Response Response) {
        this.Response = Response;
    }

    public String getJsonSchema() {
        return this.JSONSCHEMA;
    }

    public ResponseBase doAction() {
        return new EmptyResponse();
    }

    public class Action
        extends ActionBase
    {

        private String Name;
        private Integer Code;
        private Boolean Result;
        private Integer Flag;
        private String SessionID;
        private String Msg;

        public String getName() {
            return Name;
        }

        public void setName(String Name) {
            this.Name = Name;
        }

        public Integer getCode() {
            return Code;
        }

        public void setCode(Integer Code) {
            this.Code = Code;
        }

        public Boolean getResult() {
            return Result;
        }

        public void setResult(Boolean Result) {
            this.Result = Result;
        }

        public Integer getFlag() {
            return Flag;
        }

        public void setFlag(Integer Flag) {
            this.Flag = Flag;
        }

        public String getSessionID() {
            return SessionID;
        }

        public void setSessionID(String SessionID) {
            this.SessionID = SessionID;
        }

        public String getMsg() {
            return Msg;
        }

        public void setMsg(String Msg) {
            this.Msg = Msg;
        }

    }

    public class Parameter {

        private Integer StartIndex;
        private Integer Count;
        private List<QueryConsumeAction.Parameter.Filter> Filters;

        public Integer getStartIndex() {
            return StartIndex;
        }

        public void setStartIndex(Integer StartIndex) {
            this.StartIndex = StartIndex;
        }

        public Integer getCount() {
            return Count;
        }

        public void setCount(Integer Count) {
            this.Count = Count;
        }

        public List<QueryConsumeAction.Parameter.Filter> getFilters() {
            return Filters;
        }

        public void setFilters(List<QueryConsumeAction.Parameter.Filter> Filters) {
            this.Filters = Filters;
        }

        public class Filter {

            private String OrderID;

            public String getOrderID() {
                return OrderID;
            }

            public void setOrderID(String OrderID) {
                this.OrderID = OrderID;
            }

        }

    }

    public class Response
        extends ResponseBase
    {

        private Integer Total;
        private Integer FeeTotal;
        private Integer FeeSubTotal;
        private List<Object> Records;

        public Integer getTotal() {
            return Total;
        }

        public void setTotal(Integer Total) {
            this.Total = Total;
        }

        public Integer getFeeTotal() {
            return FeeTotal;
        }

        public void setFeeTotal(Integer FeeTotal) {
            this.FeeTotal = FeeTotal;
        }

        public Integer getFeeSubTotal() {
            return FeeSubTotal;
        }

        public void setFeeSubTotal(Integer FeeSubTotal) {
            this.FeeSubTotal = FeeSubTotal;
        }

        public List<Object> getRecords() {
            return Records;
        }

        public void setRecords(List<Object> Records) {
            this.Records = Records;
        }

    }

}
