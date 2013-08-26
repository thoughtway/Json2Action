module.exports = {
  name: 'ACTION_CODE_QUERYCONSUME',
  run: function (header, param) {
    this._actionH = header;
    this.RequestSchema = '{\"type\":\"object\",\"properties\":{\"Action\":{\"type\":\"object\",\"properties\":{\"Name\":{\"type\":\"string\",\"required\":true},\"Code\":{\"type\":\"integer\",\"required\":true},\"Result\":{\"type\":\"boolean\",\"required\":true},\"Flag\":{\"type\":\"integer\",\"required\":true},\"SessionID\":{\"type\":\"string\",\"required\":true},\"Msg\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"Parameter\":{\"type\":\"object\",\"properties\":{\"StartIndex\":{\"type\":\"integer\",\"required\":true},\"Count\":{\"type\":\"integer\",\"required\":true},\"Filters\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"OrderID\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"required\":true}},\"required\":true},\"Response\":{\"type\":\"object\",\"properties\":{\"Total\":{\"type\":\"integer\",\"required\":true},\"FeeTotal\":{\"type\":\"integer\",\"required\":true},\"FeeSubTotal\":{\"type\":\"integer\",\"required\":true},\"Records\":{\"type\":\"array\",\"required\":true}},\"required\":true}},\"required\":true}';
    this.ResponseSchema = '{\"type\":\"object\",\"properties\":{\"Total\":{\"type\":\"integer\",\"required\":true},\"FeeTotal\":{\"type\":\"integer\",\"required\":true},\"FeeSubTotal\":{\"type\":\"integer\",\"required\":true},\"Records\":{\"type\":\"array\",\"required\":true}},\"required\":true}';
    return {
      Total: 0,
      FeeTotal: 0,
      FeeSubTotal: 0,
      Records: []
    };
  },
  header: function () {
    return this._actionH;
  }
};