if (!Array.prototype.first) {
    Array.prototype.first = function() {
        return this[0];
    };
};

if (!Array.prototype.last) {
    Array.prototype.last = function() {
        return this[this.length - 1];
    };
};

if (!Array.prototype.findFirstAndPop) {
    Array.prototype.findFirstAndPop = function(finder) {
        for (var i = 0; i < this.length; i++) {
            var element = this[i];
            if (finder(element)) {
                this.splice(i, 1);
                return element;
            }
        }
    }
}
