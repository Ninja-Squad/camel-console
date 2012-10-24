function (k, v) {
    var count = 0;
    print(v);
    v.forEach(function (value) {
        count += value.count
    });
    return count;
}