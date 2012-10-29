define(['underscore', 'backbone'], function(_, Backbone) {
  var RouteModel = Backbone.Model.extend({

    urlRoot: '/api/route',

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
