define(['underscore', 
        'backbone', 
        'hbs!templates/graph',
        'jquery',
        'flot',
        'flot-stack',
        'flot-resize',
        'bootstrap'], function (_, Backbone, graphTemplate) {
    var GraphView = Backbone.View.extend({
        initialize: function() {
            this.template = graphTemplate;
            this.mode = 'messages';
            this.messagesMode = 'both';
            this.timesMode = 'all';
            this.collection.on('reset', this.reset, this);
            this.timeFormat = '%y-%m-%0d';
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
            'plothover [data-id=graph]': 'plothover'
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
                data: this.collection.getTimeSerie('completed'),
                color: '#57A957',
                label: 'Successes',
                lines: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(98,196,98,0.5)',
                    align: 'center'
                },
                stack: true
            };
            var failureSerie = {
                data: this.collection.getTimeSerie('failed'),
                color: '#C43C35',
                label: 'Failures',
                lines: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(238,95,91,0.5)',
                    align: 'center'
                },
                stack: true
            };
            var averageSerie = {
                data: this.collection.getTimeSerie('average'),
                color: '#AEAEAE',
                label: 'Average time',
                lines: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(206,206,206,0.5)'
                }
            };
            var minimumSerie = {
                data: this.collection.getTimeSerie('min'),
                color: '#D4B989',
                label: 'Minimum time',
                lines: {
                    show: true,
                    fill: true,
                    fillColor: 'rgba(212,185,137,0.5)'
                }
            };
            var maximumSerie = {
                data: this.collection.getTimeSerie('max'),
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
                    mode: 'time'
                    //timeformat: this.timeFormat
                },
                yaxis: {
                    min: 0
                },
                grid: {
                    hoverable: true,
                    clickable: true
                },
                legend: {
                    show: false
                },
                series: {
                    lines: {
                        lineWidth: 2
                    },
                    bars: {
                        lineWidth: 1
                        //barWidth: 0.8 * 24 * 60 * 60 * 1000
                    },
                    shadowSize: 0
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
        plothover: function(event, pos, item) {
            if (item) {
                var label = $.plot.formatDate(new Date(item.datapoint[0]), this.timeFormat, null);
                this.showTooltip(item.pageX, item.pageY - 18, label + ": " + (item.datapoint[1] - item.datapoint[2]));
            }
            else {
                this.removeTooltip();
            }
        }
    });
    
    return GraphView;
});