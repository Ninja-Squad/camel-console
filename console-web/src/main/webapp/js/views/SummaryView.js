define(['underscore',
    'backbone',
    'hbs!templates/summary'
], function (_, Backbone, summaryTemplate) {
    var SummaryView = Backbone.View.extend({
        initialize: function () {
            this.template = summaryTemplate;
        },
        events: {
        },
        render: function () {
            this.model.set("total", this.model.get("completed") + this.model.get("failed"));
            this.$el.html(this.template(this.model.toJSON()));
        }
    });

    return SummaryView;
});