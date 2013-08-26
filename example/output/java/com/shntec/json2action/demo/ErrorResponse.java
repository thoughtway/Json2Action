
package com.shntec.json2action.demo;


public class ErrorResponse
    extends ResponseBase
{

    public String More;
    public int Code;

    public ErrorResponse(String More, int Code) {
        this.More = More;
        this.Code = Code;
    }

    public String getMore() {
        return this.More;
    }

    public int getCode() {
        return this.Code;
    }

}
