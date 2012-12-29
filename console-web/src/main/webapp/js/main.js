// Set the require.js configuration for your application.
require.config({

    shim: {
        'underscore': {
            exports: '_'
        },
        'underscore.string': {
            deps: [
                'underscore'
            ]
        },
        'handlebars': {
            exports: 'Handlebars'
        },
        'backbone-orig': {
            deps: [
                'underscore',
                'underscore.string',
                'jquery'
            ],
            exports: 'Backbone'
        },
        'backbone-queryparams': {
            deps: [
                'backbone-orig',
                'underscore'
            ]
        },
        'backbone-paginator': {
            deps: [
                'backbone-orig',
                'underscore',
                'jquery'
            ],
            exports: 'Backbone.Paginator'
        },
        'flot': {
            deps: [
                'jquery'
            ]
        },
        'flot-selection': {
            deps: [
                'jquery',
                'flot'
            ]
        },
        'flot-stack': {
            deps: [
                'jquery',
                'flot'
            ]
        },
        'flot-resize': {
            deps: [
                'jquery',
                'flot'
            ]
        }
    },

    // Libraries
    paths: {
        jquery: 'libs/jquery',
        underscore: 'libs/underscore',
        'underscore.string': 'libs/underscore.string',
        'backbone-orig': 'libs/backbone',
        backbone: 'libs/resthub/backbone.ext',
        localstorage: 'libs/localstorage',
        text: 'libs/text',
        i18n: 'libs/i18n',
        pubsub: 'libs/resthub/pubsub',
        'bootstrap': 'libs/bootstrap',
        'backbone-validation-orig': 'libs/backbone-validation',
        'backbone-validation': 'libs/resthub/backbone-validation.ext',
        handlebars: 'libs/handlebars',
        'resthub-handlebars': 'libs/resthub/handlebars-helpers',
        'backbone-queryparams': 'libs/backbone.queryparams',
        'backbone-paginator': 'libs/backbone.paginator',
        async: 'libs/async',
        keymaster: 'libs/keymaster',
        hbs: 'libs/resthub/require-handlebars',
        flot: 'libs/jquery.flot',
        'flot-selection': 'libs/jquery.flot.selection',
        'flot-stack': 'libs/jquery.flot.stack',
        'flot-resize': 'libs/jquery.flot.resize',
        d3: 'libs/d3.v2.min'
    }
});

require(['router', 'handlebars'], function (AppRouter, Handlebars) {

    Handlebars.registerHelper('percent', function (value, total) {
        return new Handlebars.SafeString(value * 100 / total);
    });

    Handlebars.registerHelper('percentComplement', function (value, total) {
        return new Handlebars.SafeString(total == 0 ? 0 : 100 - value);
    });

    Handlebars.registerHelper('iter', function (context, options) {
        var fn = options.fn;
        var ret = "";

        if (context && context.length > 0) {
            for (var i = 0; i < context.length; i++) {
                ret = ret + fn(_.extend({},
                    context[i],
                    {iterStatus: {index: i, first: (i == 0), last: (i == context.length - 1)}}));
            }
        }
        return ret;
    });

    function padWithZero(number) {
        if ((number + '').length === 1) {
            return '0' + number;
        }
        return number;
    }

    Handlebars.registerHelper('prettyDate', function (date) {
        var months = ["Jan", "Feb", "Mar", "Apr", "May"
            , "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        var dateJs = new Date(parseInt(date));
        return dateJs.getUTCDate()
            + ' ' + months[dateJs.getUTCMonth()]
            + ' ' + dateJs.getUTCFullYear()
            + ' ' + padWithZero(dateJs.getUTCHours())
            + ':' + padWithZero(dateJs.getUTCMinutes())
            + ':' + padWithZero(dateJs.getUTCSeconds())
    });

    new AppRouter();
});
