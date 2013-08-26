
package com.shntec.json2action.demo;


public class UnknownAction
    extends ActionHandler
{

    private UnknownAction.Action Action;

    public UnknownAction() {
        this.Action = new UnknownAction.Action();
        this.Action.setName("UNKNOWNACTION");
        this.Action.setCode(5000);
        this.Action.setResult(false);
        this.Action.setMsg("Unknown Action Request!");
    }

    public ResponseBase doAction() {
        return new EmptyResponse();
    }

    public String getJsonSchema() {
        return ("{}");
    }

    public ActionBase getAction() {
        return this.Action;
    }

    public class Action
        extends ActionBase
    {

        private String Name;
        private Integer Code;
        private Boolean Result;
        private String Msg;
        private String SessionID;
        private Integer Flag;

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

        public String getMsg() {
            return Msg;
        }

        public void setMsg(String Msg) {
            this.Msg = Msg;
        }

        public String getSessionID() {
            return SessionID;
        }

        public void setSessionID(String SessionID) {
            this.SessionID = SessionID;
        }

        public Integer getFlag() {
            return Flag;
        }

        public void setFlag(Integer Flag) {
            this.Flag = Flag;
        }

    }

}
