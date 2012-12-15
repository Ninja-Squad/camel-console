define(['underscore',
    'backbone',
    'hbs!templates/summary'
], function (_, Backbone, summaryTemplate) {
    var SummaryView = Backbone.View.extend({
        initialize:function () {
            this.template = summaryTemplate;
        },
        events:{
        },
        render:function () {
            this.$el.html(this.template());
        }
    });

    return SummaryView;
});