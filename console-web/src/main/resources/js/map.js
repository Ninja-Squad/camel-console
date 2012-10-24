function () {
    emit(parseInt(this.timestamp.substring(0, 10))*1000, {count:1});
}
