define(['underscore',
    'backbone',
    'hbs!templates/summary'
], function (_, Backbone, summaryTemplate) {
    var SummaryView = Backbone.View.extend({
        initialize: function () {
            this.template = summaryTemplate;
            this.model.on("change", this.render, this);
        },
        events: {
        },
        render: function () {
            // default from to 01/01/2012
            if (this.model.get("from") === undefined) {
                this.model.set("from", new Date(2012, 1, 1).getTime())
            }
            // default to to now
            if (this.model.get("to") === undefined) {
                this.model.set("to", new Date().getTime())
            }
            this.model.set("total", this.model.get("completed") + this.model.get("failed"));
            this.$el.html(this.template(this.model.toJSON()));
        }
    });

    return SummaryView;
});