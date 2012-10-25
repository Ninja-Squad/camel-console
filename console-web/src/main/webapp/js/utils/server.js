define([
], function () {
    var Server = {
        messagesPerSecond: function(callback){
            $.getJSON('api/message/second', function(data){
                callback(data);
            });
        }
    }
    return Server;
});
