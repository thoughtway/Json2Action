module.exports = {
  name: 'ACTION_CODE_QUERYMERCHANT',
  run: function (header, param) {
    this._actionH = header;
    this.RequestSchema = '{\"type\":\"object\",\"properties\":{\"Action\":{\"type\":\"object\",\"properties\":{\"Name\":{\"type\":\"string\",\"required\":true},\"Code\":{\"type\":\"integer\",\"required\":true},\"Result\":{\"type\":\"boolean\",\"required\":true},\"Flag\":{\"type\":\"integer\",\"required\":true},\"SessionID\":{\"type\":\"string\",\"required\":true},\"Msg\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"Parameter\":{\"type\":\"object\",\"properties\":{\"Keyword\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"Response\":{\"type\":\"object\",\"properties\":{\"Merchants\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"ID\":{\"type\":\"integer\",\"required\":true},\"Merchant\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"required\":true}},\"required\":true}},\"required\":true}';
    this.ResponseSchema = '{\"type\":\"object\",\"properties\":{\"Merchants\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"ID\":{\"type\":\"integer\",\"required\":true},\"Merchant\":{\"type\":\"string\",\"required\":true}},\"required\":true},\"required\":true}},\"required\":true}';
    return {
      Merchants: []
    };
  },
  header: function () {
    return this._actionH;
  }
};