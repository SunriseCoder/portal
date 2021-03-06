var PopupUtils = {
    showPopup: function(relative, focus) {
        var relativesBounds = $('#' + relative)[0].getBoundingClientRect();
        var relativesMiddle = relativesBounds.left + relativesBounds.width / 2;
        var popup = document.getElementById("popup");
        var popupsBounds = popup.getBoundingClientRect();
        var popupsMiddle = popupsBounds.left + popupsBounds.width / 2;
        var delta = relativesMiddle - popupsMiddle;
        if (delta != 0) {
            popup.style.left = delta + 'px';
        }

        popup.classList.add("show");
        if (focus != undefined) {
            focus.focus();
        }
    },

    hidePopup: async function() {
        this.sleep(300).then(() => {
            var popup = document.getElementById("popup");
            popup.classList.remove("show");
        });
    },

    sleep: function(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}
