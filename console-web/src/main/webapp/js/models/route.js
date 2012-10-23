define(['underscore', 'backbone'], function(_, Backbone) {
  var RouteModel = Backbone.Model.extend({

    defaults: {
      routeId: "",
      uri: "",
      exchangesCompleted: "",
      exchangesFailed: "",
      exchangesTotal: ""
    }

  });
  return RouteModel;
});
