define(['underscore',
    'backbone',
    'hbs!templates/routeNumbers',
    'hbs!templates/routeBlocks',
    'models/Route'], function (_, Backbone, numbersTemplate, blocksTemplate) {
    var RouteView = Backbone.View.extend({
        tagName:'tr',
        initialize:function () {
            this.numbersTemplate = numbersTemplate;
            this.blocksTemplate = blocksTemplate;
            var view = this;
            this.messagesTooltipTitle = function () {
                return view.model.get('messageCount') + ' messages<br/>' + view.model.get('successCount') + ' successes<br/>' + view.model.get('failureCount') + ' errors';
            }
            this.timesTooltipTitle = function () {
                return 'Average time: ' + view.model.get('averageTime') + ' ms.<br/>Minimum time: ' + view.model.get('minimumTime') + ' ms.<br/>Maximum time: ' + view.model.get('maximumTime') + ' ms.';
            }
            this.model.on('change', this.render, this);
        },
        render:function (event) {
            var html = (this.mode == 'numbers') ? this.numbersTemplate(this.model.toJSON()) : this.blocksTemplate(this.model.toJSON())
            this.$el.html(html);
            this.$('.numblock-messages').tooltip({title:this.messagesTooltipTitle});
            this.$('.numblock-times').tooltip({title:this.timesTooltipTitle});
            this.$('span').tooltip();
            return this;
        },
        setMode:function (mode) {
            this.mode = mode;
            this.render();
        },
        events:{
            'click a': 'displayDetail'
        },
        displayDetail:function () {
            Backbone.history.navigate("console/" + this.model.get('routeId'), true);
        }
    });

    return RouteView;
});