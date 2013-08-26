
package com.shntec.json2action.demo;

import java.util.List;

public class QueryMerchangtAction
    extends ActionHandler
{

    private QueryMerchangtAction.Action Action;
    private QueryMerchangtAction.Parameter Parameter;
    private QueryMerchangtAction.Response Response;
    private final String JSONSCHEMA = "{\"type\":\"object\",\"properties\":{\"Action\":{\"type\":\"object\",\"properties\":{\"Name\":{\"type\":\"string\",\"required\":true},\"Code\":{\"type\":\"integer\",\"required\":true},\"Result\":{\"type\":\"boolean\",\"required\":true},\"Flag\":{\"type\":\"integer\",\"required\":true},\"SessionID\":{\"type\":\"string\",\"required\":true},\"Msg\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"Parameter\":{\"type\":\"object\",\"properties\":{\"Keyword\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"Response\":{\"type\":\"object\",\"properties\":{\"Merchants\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"ID\":{\"type\":\"integer\",\"required\":true},\"Merchant\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"required\":true}},\"required\":true}},\"required\":true}";

    public QueryMerchangtAction() {
        Action = new Action();
        Action.setName("ACTION_CODE_QUERYMERCHANT");
        Action.setCode(-1);
        Action.setResult(true);
        Action.setFlag(0);
        Action.setSessionID("");
        Action.setMsg("");
        Parameter = new Parameter();
    }

    public QueryMerchangtAction.Action getAction() {
        return Action;
    }

    public void setAction(QueryMerchangtAction.Action Action) {
        this.Action = Action;
    }

    public QueryMerchangtAction.Parameter getParameter() {
        return Parameter;
    }

    public void setParameter(QueryMerchangtAction.Parameter Parameter) {
        this.Parameter = Parameter;
    }

    public QueryMerchangtAction.Response getResponse() {
        return Response;
    }

    public void setResponse(QueryMerchangtAction.Response Response) {
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

        private String Keyword;

        public String getKeyword() {
            return Keyword;
        }

        public void setKeyword(String Keyword) {
            this.Keyword = Keyword;
        }

    }

    public class Response
        extends ResponseBase
    {

        private List<QueryMerchangtAction.Response.Merchant> Merchants;

        public List<QueryMerchangtAction.Response.Merchant> getMerchants() {
            return Merchants;
        }

        public void setMerchants(List<QueryMerchangtAction.Response.Merchant> Merchants) {
            this.Merchants = Merchants;
        }

        public class Merchant {

            private Integer ID;
            private String Merchant;

            public Integer getID() {
                return ID;
            }

            public void setID(Integer ID) {
                this.ID = ID;
            }

            public String getMerchant() {
                return Merchant;
            }

            public void setMerchant(String Merchant) {
                this.Merchant = Merchant;
            }

        }

    }

}
