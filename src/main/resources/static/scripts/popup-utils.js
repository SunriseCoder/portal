var PopupUtils = {
    showPopup: function() {
        var popup = document.getElementById("popup");
        popup.classList.add("show");
    },

    hidePopup: function() {
        var popup = document.getElementById("popup");
        popup.classList.remove("show");
    }
}
