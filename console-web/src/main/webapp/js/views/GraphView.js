define(['underscore', 
        'backbone', 
        'hbs!templates/graph',
        'flot', 
        'flot-stack',
        'bootstrap',
        'models/Statistics'], function (_, Backbone, graphTemplate) {
    var GraphView = Backbone.View.extend({
        initialize: function() {
            this.template = graphTemplate;
            this.mode = 'messages';
            this.messagesMode = 'both';
            this.timesMode = 'all';
            this.model.on('reset', this.reset, this);
        },
        events: {
            'click [data-id=messages]': function() {this.setMode('messages')},
            'click [data-id=times]': function() {this.setMode('times')},
            'click [data-id=messages-successes]': function() {this.setMessagesMode('successes')},
            'click [data-id=messages-failures]': function() {this.setMessagesMode('failures')},
            'click [data-id=messages-both]': function() {this.setMessagesMode('both')},
            'click [data-id=times-average]': function() {this.setTimesMode('average')},
            'click [data-id=times-minimum]': function() {this.setTimesMode('minimum')},
            'click [data-id=times-maximum]': function() {this.setTimesMode('maximum')},
            'click [data-id=times-all]': function() {this.setTimesMode('all')},
            'plotclick [data-id=graph]': 'plotclick'
        },
        render: function(event) {
            this.$el.html(this.template());
            this.$('.btn-group').button();
            var view = this;
            this.renderGraph();
        },
        renderGraph: function() {
            this.$('[data-id=messages-modes]').toggle(this.mode == 'messages');
            this.$('[data-id=times-modes]').toggle(this.mode == 'times');
            this.$('[data-id=' + this.mode + ']').button('toggle');
            this.$('[data-id=messages-' + this.messagesMode + ']').button('toggle');
            this.$('[data-id=times-' + this.timesMode + ']').button('toggle');
            
            var successSerie = {
                data: this.model.get('successes'),
                color: '#57A957',
                label: 'Successes',
                bars: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(98,196,98,0.5)',
                    align: 'center'
                },
                stack: true
            };
            var failureSerie = {
                data: this.model.get('failures'),
                color: '#C43C35',
                label: 'Failures',
                bars: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(238,95,91,0.5)',
                    align: 'center'
                },
                stack: true
            };
            var averageSerie = {
                data: this.model.get('averageTimes'),
                color: '#AEAEAE',
                label: 'Average time',
                lines: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(206,206,206,0.5)'
                }
            };
            var minimumSerie = {
                data: this.model.get('minimumTimes'),
                color: '#D4B989',
                label: 'Minimum time',
                lines: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(212,185,137,0.5)'
                }
            };
            var maximumSerie = {
                data: this.model.get('maximumTimes'),
                color: '#8F6E34',
                label: 'Maximum time',
                lines: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(143,110,52,0.5)'
                }
            };
            var options = {
                xaxis: {
                    tickSize: 1
                },
                yaxis: {
                    min: 0
                },
                grid: {
                    clickable: true
                },
                legend: {
                    show: false
                }
            };
            
            var data = [];
            if (this.mode == 'messages') {
                if (this.messagesMode == 'both') {
                    data = [successSerie, failureSerie];
                }
                else if (this.messagesMode == 'successes') {
                    data = [successSerie];
                }
                else {
                    data = [failureSerie];
                }
            } 
            else {
                if (this.timesMode == 'average') {
                    data = [averageSerie];
                }
                else if (this.timesMode == 'minimum') {
                    data = [minimumSerie];
                }
                else if (this.timesMode == 'maximum') {
                    data = [maximumSerie];
                }
                else {
                    data = [minimumSerie, averageSerie, maximumSerie];
                }
            }
            $.plot(this.$('[data-id=graph]'), data, options);
            this.removeTooltip();
            return this;
        },
        setMode: function(mode) {
            this.mode = mode;
            this.renderGraph();
        },
        setMessagesMode: function(messagesMode) {
            this.messagesMode = messagesMode;
            this.renderGraph();
        },
        setTimesMode: function(timesMode) {
            this.timesMode = timesMode;
            this.renderGraph();
        },
        showTooltip: function(x, y, contents) {
            $("#graphTooltip").remove();
            $('<div id="graphTooltip" class="tooltip fade right in"><div class="tooltip-arrow"></div><div class="tooltip-inner">' + contents + '</div></div>').css({
                position: 'absolute',
                display: 'none',
                top: y,
                left: x
            }).appendTo("body").fadeIn(200);
        },
        removeTooltip: function() {
            $("#graphTooltip").remove();
        },
        plotclick: function(event, pos, item) {
            if (item) {
                this.showTooltip(item.pageX, item.pageY - 18, item.datapoint[1] - item.datapoint[2]);
            }
            else {
                this.removeTooltip();
            }
        }
    });
    
    return GraphView;
});