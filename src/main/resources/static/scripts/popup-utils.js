var PopupUtils = {
    showPopup: function(focus) {
        var popup = document.getElementById("popup");
        popup.classList.add("show");
        focus.focus();
    },

    hidePopup: function() {
        var popup = document.getElementById("popup");
        popup.classList.remove("show");
    }
}
