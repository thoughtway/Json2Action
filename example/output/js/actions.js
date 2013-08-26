(function () {
  gingocards.actions.queryconsumeaction = function (opts) {
    this.ActionName = 'ACTION_CODE_QUERYCONSUME';
    this.ActionCode = '-1';
    this.ResponseSchema = '{\"type\":\"object\",\"properties\":{\"Total\":{\"type\":\"integer\",\"required\":true},\"FeeTotal\":{\"type\":\"integer\",\"required\":true},\"FeeSubTotal\":{\"type\":\"integer\",\"required\":true},\"Records\":{\"type\":\"array\",\"required\":true}},\"required\":true}';
    this.RequestSchema = '{\"type\":\"object\",\"properties\":{\"Action\":{\"type\":\"object\",\"properties\":{\"Name\":{\"type\":\"string\",\"required\":true},\"Code\":{\"type\":\"integer\",\"required\":true},\"Result\":{\"type\":\"boolean\",\"required\":true},\"Flag\":{\"type\":\"integer\",\"required\":true},\"SessionID\":{\"type\":\"string\",\"required\":true},\"Msg\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"Parameter\":{\"type\":\"object\",\"properties\":{\"StartIndex\":{\"type\":\"integer\",\"required\":true},\"Count\":{\"type\":\"integer\",\"required\":true},\"Filters\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"OrderID\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"required\":true}},\"required\":true}},\"required\":true}';
    this.Parameter = {
      StartIndex: opts.startindex,
      Count: opts.count,
      Filters: opts.Filters.slice(0)
    };
  };
  gingocards.actions.querymerchangtaction = function (opts) {
    this.ActionName = 'ACTION_CODE_QUERYMERCHANT';
    this.ActionCode = '-1';
    this.ResponseSchema = '{\"type\":\"object\",\"properties\":{\"Merchants\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"ID\":{\"type\":\"integer\",\"required\":true},\"Merchant\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"required\":true}},\"required\":true}';
    this.RequestSchema = '{\"type\":\"object\",\"properties\":{\"Action\":{\"type\":\"object\",\"properties\":{\"Name\":{\"type\":\"string\",\"required\":true},\"Code\":{\"type\":\"integer\",\"required\":true},\"Result\":{\"type\":\"boolean\",\"required\":true},\"Flag\":{\"type\":\"integer\",\"required\":true},\"SessionID\":{\"type\":\"string\",\"required\":true},\"Msg\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"Parameter\":{\"type\":\"object\",\"properties\":{\"Keyword\":{\"type\":\"string\",\"required\":true}},\"required\":true}},\"required\":true}';
    this.Parameter = {
      Keyword: opts.keyword
    };
  };
})();