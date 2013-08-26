(function () {
  gingocards.doqueryconsumeaction = function (opts) {
    var a = new gingocards.actions.queryconsumeaction;
    a.Parameter.StartIndex = opts.startindex;
    a.Parameter.Count = opts.count;
    a.Parameter.Filters = opts.filters.slice(0);
    a.OnComplete(function (action, resp) {
        if (opts && opts.success && action.ActionStatus)
          opts.success(action.ActionPayload);
        else
          if (opts && opts.fail && !action.ActionStatus)
            opts.fail(action.ActionErrorMessage);
      });
    gingocards.actionrunner.run(a);
  };
  gingocards.doquerymerchangtaction = function (opts) {
    var a = new gingocards.actions.querymerchangtaction;
    a.Parameter.Keyword = opts.keyword;
    a.OnComplete(function (action, resp) {
        if (opts && opts.success && action.ActionStatus)
          opts.success(action.ActionPayload);
        else
          if (opts && opts.fail && !action.ActionStatus)
            opts.fail(action.ActionErrorMessage);
      });
    gingocards.actionrunner.run(a);
  };
})();