var FormUtils = {
    submitForm: function(formId) {
        var form = document.getElementById(formId);
        form.submit();
    },

    submitOnEnterPressed: function(event, formId) {
        if (event.keyCode == 13) {
            FormUtils.submitForm(formId);
        }
    }
}
